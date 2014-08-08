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
package com.github.cereda.nightingale.utils;

import com.github.cereda.nightingale.controller.ConfigurationController;
import com.github.cereda.nightingale.controller.LanguageController;
import com.github.cereda.nightingale.model.Command;
import com.github.cereda.nightingale.model.NightingaleException;
import com.github.cereda.nightingale.model.Conditional;
import com.github.cereda.nightingale.model.Messages;
import groovy.lang.Closure;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer;

/**
 * Implements interpreter utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class InterpreterUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // get the logger context from a factory
    private static final Logger logger =
            LoggerFactory.getLogger(InterpreterUtils.class);

    /**
     * Checks if the rule header is valid, including class types and keys.
     * @param holder An object representing the rule header map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static void checkHeader(Object holder) throws NightingaleException {
        if (holder == null) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(
                            messages.getMessage(
                                    Messages.ERROR_CHECKHEADER_NULL_RULE_HEADER
                            )
                    )
            );
        } else {
            if (!CommonUtils.checkClass(Map.class, holder)) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(
                                messages.getMessage(
                                        Messages.ERROR_CHECKHEADER_RULE_HEADER_NOT_A_MAP
                                )
                        )
                );
            } else {
                Map<String, Class> mapping = new HashMap<String, Class>();
                mapping.put("id", String.class);
                mapping.put("name", String.class);
                mapping.put("description", String.class);
                mapping.put("arguments", List.class);
                mapping.put("authors", List.class);
                mapping.put("requires", BigDecimal.class);
                @SuppressWarnings("unchecked")
                Map<String, ? extends Object> rule =
                        (Map<String, ? extends Object>) holder;
                for (String key : mapping.keySet()) {
                    if (rule.containsKey(key)) {
                        if (!CommonUtils.checkClass(
                                mapping.get(key),
                                rule.get(key))) {
                            throw new NightingaleException(
                                    CommonUtils.getRuleErrorHeader().
                                    concat(
                                            messages.getMessage(
                                                    Messages.ERROR_CHECKHEADER_RULE_HEADER_WRONG_CLASS_TYPE,
                                                    key,
                                                    mapping.get(key).
                                                            getSimpleName()
                                            )
                                    )
                            );
                        }
                    } else {
                        throw new NightingaleException(
                                CommonUtils.getRuleErrorHeader().
                                concat(
                                        messages.getMessage(
                                                Messages.ERROR_CHECKHEADER_RULE_HEADER_MISSING_KEY,
                                                key
                                        )
                                )
                        );
                    }
                }
                if (!CommonUtils.checkMaps(rule, mapping)) {
                    throw new NightingaleException(
                            CommonUtils.getRuleErrorHeader().
                            concat(
                                    messages.getMessage(
                                            Messages.ERROR_CHECKHEADER_RULE_HEADER_UNKNOWN_KEYS,
                                            CommonUtils.getCollectionElements(
                                                    CommonUtils.getUnknownKeys(
                                                            rule,
                                                            mapping
                                                    ),
                                                    "(",
                                                    ")",
                                                    ", "
                                            )
                                    )
                            )
                    );
                }
                checkArguments(rule);
                checkAuthors(rule);
                checkKeywords(rule);
                checkDuplicates(rule);
            }
        }
    }

    /**
     * Checks if the rule commands are valid, including class types and keys.
     * @param holder An object representing the rule commands map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static void checkCommands(Object holder) throws NightingaleException {
        if (holder == null) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(
                            messages.getMessage(
                                    Messages.ERROR_CHECKCOMMANDS_NULL_COMMANDS
                            )
                    )
            );

        } else {
            if (!CommonUtils.checkClass(List.class, holder)) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(
                                messages.getMessage(
                                        Messages.ERROR_CHECKCOMMANDS_COMMANDS_NOT_A_LIST
                                )
                        )
                );
            } else {
                Map<String, Class> mapping = new HashMap<String, Class>();
                mapping.put("name", String.class);
                mapping.put("command", Closure.class);
                mapping.put("exit", Closure.class);
                List commands = (List) holder;
                for (Object entry : commands) {
                    if (!CommonUtils.checkClass(Map.class, entry)) {
                        throw new NightingaleException(
                                CommonUtils.getRuleErrorHeader().
                                concat(
                                        messages.getMessage(
                                                Messages.ERROR_CHECKCOMMANDS_LIST_ELEMENTS_NOT_A_MAP
                                        )
                                )
                        );
                    } else {
                        @SuppressWarnings("unchecked")
                        Map<String, ? extends Object> command =
                                (Map<String, ? extends Object>) entry;
                        for (String key : mapping.keySet()) {
                            if (command.containsKey(key)) {
                                if (!CommonUtils.checkClass(
                                        mapping.get(key),
                                        command.get(key))) {
                                    throw new NightingaleException(
                                            CommonUtils.getRuleErrorHeader().
                                            concat(
                                                    messages.getMessage(
                                                            Messages.ERROR_CHECKCOMMANDS_LIST_ELEMENT_WRONG_CLASS_TYPE,
                                                            key,
                                                            mapping.get(key).
                                                                    getSimpleName()
                                                    )
                                            )
                                    );
                                }
                            } else {
                                throw new NightingaleException(
                                        CommonUtils.getRuleErrorHeader().
                                        concat(
                                                messages.getMessage(
                                                        Messages.ERROR_CHECKCOMMANDS_LIST_ELEMENT_MISSING_KEY,
                                                        key
                                                )
                                        )
                                );
                            }
                        }
                        if (!CommonUtils.checkMaps(command, mapping)) {
                            throw new NightingaleException(
                                    CommonUtils.getRuleErrorHeader().
                                    concat(
                                            messages.getMessage(
                                                    Messages.ERROR_CHECKCOMMANDS_LIST_ELEMENT_UNKNOWN_KEYS,
                                                    CommonUtils.getCollectionElements(
                                                            CommonUtils.getUnknownKeys(
                                                                    command,
                                                                    mapping
                                                            ),
                                                            "(",
                                                            ")",
                                                            ", "
                                                    )
                                            )
                                    )
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the rule header arguments are valid.
     * @param map The map representing the rule header.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkArguments(Map<String, ? extends Object> map)
            throws NightingaleException {
        if (map.containsKey("arguments")) {
            List arguments = (List) map.get("arguments");
            for (Object object : arguments) {
                if (!CommonUtils.checkClass(String.class, object)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_CHECKHEADER_ARGUMENTS_NOT_A_STRING_LIST
                            )
                    );
                } else {
                    if (CommonUtils.checkEmptyString(((String) object).trim())) {
                        throw new NightingaleException(
                                messages.getMessage(
                                        Messages.ERROR_CHECKHEADER_ARGUMENTS_EMPTY_STRING
                                )
                        );
                    }
                }
            }
        }
    }

    /**
     * Checks if the rule header authors list is valid.
     * @param map The rule header map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkAuthors(Map<String, ? extends Object> map)
            throws NightingaleException {
        if (map.containsKey("authors")) {
            List authors = (List) map.get("authors");
            for (Object object : authors) {
                if (!CommonUtils.checkClass(String.class, object)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_CHECKHEADER_AUTHORS_NOT_A_STRING_LIST
                            )
                    );
                } else {
                    if (CommonUtils.checkEmptyString(((String) object).trim())) {
                        throw new NightingaleException(
                                messages.getMessage(
                                        Messages.ERROR_CHECKHEADER_AUTHORS_EMPTY_STRING
                                )
                        );
                    }
                }
            }
        }
    }

    /**
     * Checks if the rule header arguments list has valid keys.
     * @param map The rule header map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkKeywords(Map<String, ? extends Object> map)
            throws NightingaleException {
        List<String> keywords = Arrays.asList("file", "files");
        if (map.containsKey("arguments")) {
            List arguments = (List) map.get("arguments");
            for (String keyword : keywords) {
                if (arguments.contains(keyword)) {
                    throw new NightingaleException(
                            CommonUtils.getRuleErrorHeader().
                            concat(
                                    messages.getMessage(
                                            Messages.ERROR_CHECKKEYWORDS_ELEMENT_IS_RESERVED,
                                            keyword
                                    )
                            )
                    );
                }
            }
        }
    }

    /**
     * Checks if the rule header arguments list has duplicate elements.
     * @param map The rule header map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkDuplicates(Map<String, ? extends Object> map)
            throws NightingaleException {
        if (map.containsKey("arguments")) {
            List arguments = (List) map.get("arguments");
            int size = arguments.size();
            @SuppressWarnings("unchecked")
            Set set = new LinkedHashSet(arguments);
            if (size != set.size()) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(
                                messages.getMessage(
                                        Messages.ERROR_CHECKDUPLICATES_DUPLICATE_ELEMENTS
                                )
                        )
                );
            }
        }
    }

    /**
     * Checks if the first object is a map and the keys are valid when analyzed
     * with the second provided map.
     * @param holder The first object, being a map.
     * @param map2 The second map, acting as a reference.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    @SuppressWarnings("unchecked")
    public static void validateArguments(Object holder, Map map2)
            throws NightingaleException {
        Map map1 = (Map) holder;
        Set keys1 = new HashSet((List) map1.get("arguments"));
        Set keys2 = new HashSet(map2.keySet());
        keys2.remove("file");
        Collection difference = CollectionUtils.subtract(keys2, keys1);
        if (!difference.isEmpty()) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATEARGUMENTS_INVALID_KEYS,
                            getDirectiveLineNumbers(),
                            CommonUtils.getCollectionElements(
                                    difference,
                                    "(",
                                    ")",
                                    ", "
                            )
                    )
            );
        }
    }

    /**
     * Validates the rule header map with the rule file.
     * @param holder The rule header map.
     * @param rule The rule file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static void validateIdentifier(Object holder, File rule)
            throws NightingaleException {
        Map map = (Map) holder;
        String identifier = (String) map.get("id");
        String basename = CommonUtils.getBasename(rule);
        if (!identifier.equals(basename)) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATEIDENTIFIER_WRONG_IDENTIFIER,
                                    basename,
                                    identifier
                            )
                    )
            );
        }
    }

    /**
     * Gets the list of maps from the rule commands.
     * @param holder The rule commands.
     * @return A list of maps.
     */
    public static List<Map> getCommands(Object holder) {
        @SuppressWarnings("unchecked")
        List<Map> result = (List<Map>) holder;
        return result;
    }

    /**
     * Runs the command in the underlying operating system.
     * @param command An object representing the command.
     * @return An integer value representing the exit code.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static int run(Object command) throws NightingaleException {
        boolean verbose = (Boolean) ConfigurationController.
                getInstance().
                get("execution.verbose");
        boolean timeout = (Boolean) ConfigurationController.
                getInstance().
                get("execution.timeout");
        long value = (Long) ConfigurationController.
                getInstance().
                get("execution.timeout.value");
        TimeUnit unit = (TimeUnit) ConfigurationController.
                getInstance().
                get("execution.timeout.unit");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ProcessExecutor executor = new ProcessExecutor();
        if (CommonUtils.checkClass(Command.class, command)) {
            executor = executor.command(((Command) command).getElements());
        }
        else {
            executor = executor.commandSplit((String) command);
        }
        if (timeout) {
            if (value == 0) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_RUN_TIMEOUT_INVALID_RANGE
                        )
                );
            }
            executor = executor.timeout(value, unit);
        }
        TeeOutputStream tee;
        if (verbose) {
            tee = new TeeOutputStream(System.out, buffer);
            executor = executor.redirectInput(System.in);
        } else {
            tee = new TeeOutputStream(buffer);
        }
        executor = executor.redirectOutput(tee).redirectError(tee);

        ShutdownHookProcessDestroyer hook = new ShutdownHookProcessDestroyer();
        executor = executor.addDestroyer(hook);

        try {
            int exit = executor.execute().getExitValue();

            logger.info(
                    DisplayUtils.displayOutputSeparator(
                            messages.getMessage(
                                    Messages.LOG_INFO_BEGIN_BUFFER
                            )
                    )
            );
            logger.info(buffer.toString());
            logger.info(
                    DisplayUtils.displayOutputSeparator(
                            messages.getMessage(
                                    Messages.LOG_INFO_END_BUFFER
                            )
                    )
            );

            return exit;
        } catch (IOException ioexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_RUN_IO_EXCEPTION
                    ),
                    ioexception
            );
        } catch (InterruptedException iexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INTERRUPTED_EXCEPTION
                    ),
                    iexception
            );
        } catch (InvalidExitValueException ievexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                    ),
                    ievexception
            );
        } catch (TimeoutException texception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_RUN_TIMEOUT_EXCEPTION
                    ),
                    texception
            );
        } catch (Exception exception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_RUN_GENERIC_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Checks if the rule version is compatible with the application version.
     * @param holder The rule header map.
     * @throws NightingaleException 
     */
    public static void checkVersion(Object holder)
            throws NightingaleException {
        BigDecimal current = (BigDecimal) ConfigurationController.
                getInstance().
                get("application.version");
        BigDecimal version = (BigDecimal) ((Map) holder).get("requires");
        if (version.compareTo(current) > 0) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_CHECKVERSION_VERSION_TOO_OLD,
                            String.format(
                                    new Locale("en"),
                                    "%1.1f",
                                    current
                            ),
                            String.format(
                                    new Locale("en"),
                                    "%1.1f",
                                    version
                            )
                    )
            );
        }
    }

    /**
     * Gets the rule name from the rule header map.
     * @param holder The rule header map.
     * @return The rule name.
     */
    public static String getRuleName(Object holder) {
        return (String) ((Map) holder).get("name");
    }

    /**
     * Gets the list of authors from the rule header map.
     * @param holder The rule header map.
     * @return The list of authors.
     */
    public static List<String> getAuthors(Object holder) {
        @SuppressWarnings("unchecked")
        List<String> authors = (List<String>) ((Map) holder).get("authors");
        return authors;
    }

    /**
     * Checks if the current conditional has a prior evaluation.
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    public static boolean runPriorEvaluation(Conditional conditional) {
        if (((Boolean) ConfigurationController.
                getInstance().
                get("execution.dryrun")) == true) {
            return false;
        }

        switch (conditional.getType()) {
            case IF:
            case WHILE:
            case UNLESS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Builds the rule path based on the rule name and returns the corresponding
     * file location.
     * @param name The rule name.
     * @return The rule file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static File buildRulePath(String name) throws NightingaleException {
        @SuppressWarnings("unchecked")
        List<String> paths = (List<String>) ConfigurationController.
                getInstance().
                get("execution.rule.paths");
        for (String path : paths) {
            File location = new File(construct(path, name));
            if (location.exists()) {
                return location;
            }
        }
        return null;
    }

    /**
     * Constructs the path given the current path and the rule name.
     * @param path The current path.
     * @param name The rule name.
     * @return The constructed path.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static String construct(String path, String name)
            throws NightingaleException {
        name = name.concat(".groovy");
        File location = new File(path);
        if (location.isAbsolute()) {
            return CommonUtils.buildPath(path, name);
        } else {
            File reference = (File) ConfigurationController.
                    getInstance().
                    get("execution.reference");
            String parent = CommonUtils.buildPath(
                    CommonUtils.getParentCanonicalPath(reference),
                    path
            );
            return CommonUtils.buildPath(parent, name);
        }
    }

    /**
     * Gets a textual representation of the current directive line numbers.
     * @return A string representing the current directive line numbers.
     */
    public static String getDirectiveLineNumbers() {
        if (ConfigurationController.
                getInstance().
                contains("execution.directive.lines")) {
            @SuppressWarnings("unchecked")
            List<Integer> numbers = (List<Integer>) ConfigurationController.
                    getInstance().
                    get("execution.directive.lines");
            return CommonUtils.getCollectionElements(
                    numbers,
                    "(",
                    ")",
                    ", "
            );
        } else {
            return "";
        }
    }

    /**
     * Gets the list of arguments from the rule header map, plus the reserved
     * keywords.
     * @param holder The rule header map.
     * @return A list of arguments found in the rule header map plus the
     * reserved keywords.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRuleArguments(Object holder)
            throws NightingaleException {
        Map map = (Map) holder;
        List<String> arguments = (List<String>) map.get("arguments");
        arguments.add("file");
        return arguments;
    }

}
