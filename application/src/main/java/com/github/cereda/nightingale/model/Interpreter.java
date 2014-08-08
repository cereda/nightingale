/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.nightingale.model;

import com.github.cereda.nightingale.controller.ConfigurationController;
import com.github.cereda.nightingale.controller.LanguageController;
import com.github.cereda.nightingale.utils.CommonUtils;
import com.github.cereda.nightingale.utils.DisplayUtils;
import com.github.cereda.nightingale.utils.InterpreterUtils;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interprets the list of directives.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Interpreter {

    // list of directives to be
    // interpreted in here
    private List<Directive> directives;
    
    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // the class logger obtained from
    // the logger factory
    private static final Logger logger =
            LoggerFactory.getLogger(Interpreter.class);

    /**
     * Sets the list of directives.
     * @param directives The list of directives.
     */
    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public void execute() throws NightingaleException {
        
        
        // for every directive in the list of
        // directives, this method will evaluate
        // the rules and tasks and run them
        // accordingly
        for (Directive directive : directives) {

            // add the current info to
            // the logging framework
            logger.info(messages.getMessage(
                    Messages.LOG_INFO_INTERPRET_RULE,
                    directive.getIdentifier())
            );

            // set the current file being executed;
            // this is needed in order to make methods
            // like changed() and exists() to work for
            // file extensions; note that this value might
            // be different from the main file being
            // processed by nightingale
            ConfigurationController.
                    getInstance().
                    put("execution.file",
                            directive.getParameters().get("file")
                    );
            
            // obtain rule from a list of paths
            // (at least the application path)
            // or throw an error in case the
            // rule is not found
            File rule = getRule(directive);

            // add the current info to the
            // logging framework
            logger.info(messages.getMessage(
                    Messages.LOG_INFO_RULE_LOCATION,
                    rule.getParent())
            );

            // set the current rule identifier,
            // path and lines to the configuration
            // controller
            ConfigurationController.
                    getInstance().
                    put("execution.info.rule.id",
                            directive.getIdentifier()
                    );
            ConfigurationController.
                    getInstance().
                    put("execution.info.rule.path",
                            rule.getParent()
                    );
            ConfigurationController.
                    getInstance().
                    put("execution.directive.lines",
                            directive.getLineNumbers()
                    );

            // set the shell configuration, binding,
            // configuration and base class
            String base = "com.github.cereda.nightingale.model.BaseRule";
            Binding binding = new Binding();
            BaseRule.setParameters(directive.getParameters());
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setScriptBaseClass(base);
            GroovyShell shell = new GroovyShell(binding, configuration);

            try {
                
                // evaluate rule, checking header, validating
                // identifier, version and arguments
                shell.evaluate(rule);
                Object holder = shell.getVariable("rule");
                InterpreterUtils.checkHeader(holder);
                InterpreterUtils.validateIdentifier(holder, rule);
                InterpreterUtils.checkVersion(holder);
                InterpreterUtils.validateArguments(
                        holder,
                        directive.getParameters()
                );

                // get the rule name, the list of authors, and
                // put the arguments list in the configuration
                // controller in order to make methods like
                // ensure() to not accept unknown keys
                String name = InterpreterUtils.getRuleName(holder);
                List<String> authors = InterpreterUtils.getAuthors(holder);
                ConfigurationController.
                        getInstance().
                        put("execution.rule.arguments",
                                InterpreterUtils.getRuleArguments(holder)
                        );

                // get the rule commands, check them
                // and create a new evaluator
                holder = shell.getVariable("commands");
                InterpreterUtils.checkCommands(holder);
                Evaluator evaluator = new Evaluator();

                // check if the current directive has a prior
                // evaluation; if so, we need to evaluate it
                // right now
                boolean available = true;
                if (InterpreterUtils.runPriorEvaluation(
                        directive.getConditional())) {
                    available = evaluator.evaluate(directive.getConditional());
                }

                // we are good to go, so let's
                // evaluate each command
                if (available) {
                    
                    // it's a loop because we can have conditionals that
                    // might require repeating such executions; in the end
                    // of this block, there is a conditional evaluation
                    do {
                        
                        // get the list of commands, so
                        // we can evaluate them
                        List<Map> commands = InterpreterUtils
                                .getCommands(holder);
                        
                        // for each command found in the
                        // list of commands of the current
                        // rule, let's evaluate it
                        for (Map command : commands) {

                            // get the first command and
                            // run the closure
                            Closure closure = (Closure) command.get("command");
                            Object result = closure.call();

                            // this list will hold the
                            // commands execution
                            List<Object> execution = new ArrayList<Object>();

                            // if we got a list as result,
                            // let's flatten the list and
                            // add each command to the list;
                            // otherwise, we simply add the
                            // result to the list
                            if (CommonUtils.checkClass(List.class, result)) {
                                execution = CommonUtils.
                                        flatten((List<?>) result);
                            } else {
                                execution.add(result);
                            }

                            // for each command in the
                            // execution list, we check
                            // if it's not null and if
                            // types are valid
                            for (Object current : execution) {
                                if (current == null) {
                                    throw new NightingaleException(
                                            messages.getMessage(
                                                    Messages.ERROR_INTERPRETER_NULL_COMMAND_LIST
                                            )
                                    );
                                } else {
                                    
                                    // check if it's not an empty string,
                                    // otherwise nothing happends
                                    if (!CommonUtils.
                                            checkEmptyString(
                                                    String.valueOf(current))
                                            ) {
                                        DisplayUtils.printEntry(
                                                name,
                                                (String) command.get("name")
                                        );

                                        boolean success = true;
                                        
                                        // we check if it's a boolean result,
                                        // that is, the computation happened
                                        // inside the command closure
                                        if (CommonUtils.checkClass(
                                                Boolean.class,
                                                current)) {
                                            if (((Boolean) ConfigurationController.
                                                    getInstance().
                                                    get("execution.dryrun")) == false) {
                                                if (((Boolean) ConfigurationController.
                                                        getInstance().
                                                        get("execution.verbose")) == true) {
                                                    DisplayUtils.wrapText(
                                                            messages.getMessage(
                                                                    Messages.INFO_INTERPRETER_VERBOSE_MODE_BOOLEAN_MODE
                                                            )
                                                    );
                                                }
                                            } else {
                                                DisplayUtils.printAuthors(authors);
                                                DisplayUtils.wrapText(
                                                        messages.getMessage(
                                                                Messages.INFO_INTERPRETER_DRYRUN_MODE_BOOLEAN_MODE
                                                        )
                                                );
                                                DisplayUtils.printConditional(
                                                        directive.getConditional()
                                                );
                                            }
                                            
                                            success = (Boolean) current;
                                            
                                        } else {
                                            
                                            // let's check if it's a trigger,
                                            // which can alter the application
                                            // behaviour
                                            if (CommonUtils.checkClass(
                                                    Trigger.class,
                                                    current)) {
                                                if (((Boolean) ConfigurationController.
                                                        getInstance().
                                                        get("execution.dryrun")) == false) {
                                                    if (((Boolean) ConfigurationController.
                                                            getInstance().
                                                            get("execution.verbose")) == true) {
                                                        DisplayUtils.wrapText(
                                                                messages.getMessage(
                                                                        Messages.INFO_INTERPRETER_VERBOSE_MODE_TRIGGER_MODE
                                                                )
                                                        );
                                                    }
                                                } else {
                                                    DisplayUtils.printAuthors(authors);
                                                    DisplayUtils.wrapText(
                                                            messages.getMessage(
                                                                    Messages.INFO_INTERPRETER_DRYRUN_MODE_TRIGGER_MODE
                                                            )
                                                    );
                                                    DisplayUtils.printConditional(
                                                            directive.getConditional()
                                                    );
                                                }

                                                Trigger trigger = (Trigger) current;
                                                trigger.process();

                                            } else {
                                                
                                                // not a boolean nor a trigger,
                                                // let's simply get a string
                                                // representation of the command
                                                String representation =
                                                        String.valueOf(current);

                                                logger.info(
                                                        messages.getMessage(
                                                                Messages.LOG_INFO_SYSTEM_COMMAND,
                                                                representation
                                                        )
                                                );

                                                if (((Boolean) ConfigurationController.
                                                        getInstance().get("execution.dryrun")) == false) {

                                                    int code = InterpreterUtils.run(representation);
                                                    Closure evaluation = (Closure) command.get("exit");
                                                    Object check = evaluation.call(code);
                                                    if (CommonUtils.checkClass(
                                                            Boolean.class,
                                                            check)) {
                                                        success = (Boolean) check;
                                                    } else {
                                                        throw new NightingaleException(
                                                                messages.getMessage(
                                                                        Messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
                                                                )
                                                        );
                                                    }
                                                } else {
                                                    DisplayUtils.printAuthors(authors);
                                                    DisplayUtils.wrapText(
                                                            messages.getMessage(
                                                                    Messages.INFO_INTERPRETER_DRYRUN_MODE_SYSTEM_COMMAND,
                                                                    representation
                                                            )
                                                    );
                                                    DisplayUtils.printConditional(directive.getConditional());
                                                }

                                            }
                                        }

                                        DisplayUtils.printEntryResult(success);

                                        // two situations might make the current
                                        // rule evaluation stop: a failure (if
                                        // the flag on halting on errors is
                                        // enabled) or through a 'halt' trigger
                                        if (((Boolean) ConfigurationController.
                                                getInstance().
                                                get("trigger.halt"))
                                                || (((Boolean) ConfigurationController.
                                                        getInstance().
                                                        get("execution.errors.halt")
                                                && !success))) {
                                            return;
                                        }
                                    }
                                }
                            }

                        }
                    } while (evaluator.evaluate(directive.getConditional()));
                }
            } catch (CompilationFailedException cfexception) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(messages.getMessage(
                                Messages.ERROR_INTERPRETER_COMPILATION_FAILED)
                        ),
                        cfexception
                );
            } catch (IOException ioexception) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(messages.getMessage(
                                Messages.ERROR_INTERPRETER_IO_EXCEPTION)
                        ),
                        ioexception
                );
            } catch (MissingPropertyException mpexception) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(messages.getMessage(
                                Messages.ERROR_INTERPRETER_MISSING_PROPERTY)
                        ),
                        mpexception
                );
            } catch (Exception exception) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(messages.getMessage(
                                Messages.ERROR_INTERPRETER_GENERIC_EXCEPTION)
                        ),
                        exception
                );
            }
        }
    }

    /**
     * Gets the rule according to the provided directive.
     * @param directive The provided directive.
     * @return The absolute canonical path of the rule, given the provided
     * directive.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private File getRule(Directive directive) throws NightingaleException {
        File file = InterpreterUtils.buildRulePath(directive.getIdentifier());
        if (file == null) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_INTERPRETER_RULE_NOT_FOUND,
                            directive.getIdentifier(),
                            CommonUtils.getCollectionElements(
                                    CommonUtils.getAllRulePaths(),
                                    "(",
                                    ")",
                                    "; "
                            )
                    )
            );
        } else {
            return file;
        }
    }
}
