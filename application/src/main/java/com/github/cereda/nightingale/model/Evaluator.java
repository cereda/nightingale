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
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Implements the evaluator model, on which a conditional can be analyzed and
 * processed.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Evaluator {

    // this attribute holds the maximum number of
    // loops nightingale will accept; it's like
    // reaching infinity
    private final long loops;
    
    // the counter for the current execution, it
    // helps us keep track of the number of times
    // this evaluation has happened, and also to
    // prevent potential infinite loops
    private long counter;
    
    // a flag that indicates the
    // evaluation to halt regardless
    // of the the result
    private boolean halt;
    
    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Constructor. It gets the application maximum number of loops and reset
     * all counters.
     */
    public Evaluator() {
        loops = (Long) ConfigurationController.
                getInstance().
                get("execution.loops");
        counter = 0;
        halt = false;
    }

    /**
     * Evaluate the provided conditional.
     * @param conditional The conditional object.
     * @return A boolean value indicating if the conditional holds.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean evaluate(Conditional conditional)
            throws NightingaleException {

        // when in dry-run mode, nightingale
        // always ignore conditional evaluations
        if (((Boolean) ConfigurationController.getInstance().
                get("execution.dryrun")) == true) {
            return false;
        }

        // check the conditional type and
        // make decisions based on the
        // current flags
        switch (conditional.getType()) {
            case NONE:
                return false;
            case IF:
            case UNLESS:
                if (!halt) {
                    halt = true;
                } else {
                    return false;
                }
                break;
        }

        // check counters and see if the execution
        // has reached our concept of infinity,
        // thus breaking the cycles
        counter++;
        if (((conditional.getType() == Conditional.ConditionalType.WHILE) &&
                (counter > loops))
                || ((conditional.getType() == Conditional.ConditionalType.UNTIL)
                && (counter >= loops))) {
            return false;
        } else {

            // create a new evaluation context,
            // set the base class and evaluate
            // the expression
            String base = "com.github.cereda.nightingale.model.BaseConditional";
            Binding binding = new Binding();
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setScriptBaseClass(base);
            GroovyShell shell = new GroovyShell(binding, configuration);

            try {

                // get the result and try to analyze
                // it according to its class type
                // and conditional type
                Object result = shell.evaluate(conditional.getCondition());
                if (!CommonUtils.checkClass(Boolean.class, result)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_EVALUATE_NOT_BOOLEAN_VALUE
                            )
                    );
                } else {
                    boolean value = (Boolean) result;
                    switch (conditional.getType()) {
                        case UNLESS:
                        case UNTIL:
                            value = !value;
                            break;
                    }
                    return value;
                }
            } catch (CompilationFailedException cfexception) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_EVALUATE_COMPILATION_FAILED
                        ),
                        cfexception
                );
            } catch (Exception exception) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_EVALUATE_GENERIC_EXCEPTION
                        ),
                        exception
                );
            }

        }
    }

}
