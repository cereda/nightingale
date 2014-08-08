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
import groovy.lang.Script;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class BaseRule extends Script {

    // these are the rule parameters, set as an static
    // attribute in order to avoid the explicit need
    // to call a setter in the rule context
    private static Map<String, Object> parameters;
    
    // the file reference, not necessarily the current file
    // being processed; this file represents the main file
    // from which the directives were extracted (the main
    // file provided in the command line)
    private final File reference;
    
    // the session object which holds the session map
    // in order to provide data exchange for rules
    private final Session session;
    
    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Constructor. It sets the file reference obtained from the configuration
     * controller and creates a new session (actually, it's not new, but you
     * probably got the idea).
     */
    public BaseRule() {
        reference = (File) ConfigurationController.
                getInstance().
                get("execution.reference");
        session = new Session();
    }

    /**
     * Gets the file reference.
     * @return A file representing the main file provided to nightingale.
     */
    public File reference() {
        return reference;
    }

    /**
     * Implements the interface method and returns the current reference.
     * @return The current reference to this very own object.
     */
    @Override
    public Object run() {
        return this;
    }

    /**
     * Sets the parameters accordingly.
     * @param parameters A map containing the rule parameters.
     */
    public static void setParameters(Map<String, Object> parameters) {
        BaseRule.parameters = parameters;
    }

    /**
     * Checks if every key in the array of strings is available in the
     * parameters map.
     * @param keys Array of strings containing the keys to be analyzed.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public void required(String... keys) throws NightingaleException {
        for (String key : keys) {
            if (!parameters.containsKey(key)) {
                throw new NightingaleException(
                        CommonUtils.getRuleErrorHeader().
                        concat(messages.getMessage(
                                Messages.ERROR_REQUIRED_INVALID_KEY,
                                key
                        ))
                );
            }
        }
    }

    /**
     * Checks if the provided key is defined.
     * @param key The key.
     * @return A boolean value indicating if the key is defined.
     */
    public boolean defined(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Checks if the provided key is not defined.
     * @param key The key.
     * @return A boolean value indicating if the key is not defined.
     */
    public boolean undefined(String key) {
        return !defined(key);
    }

    /**
     * Ensures that, if not defined, the key will hold at least the provided
     * object value.
     * @param key The key.
     * @param value The object value.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public void ensure(String key, Object value) throws NightingaleException {
        if (CommonUtils.isValidArgumentKey(key)) {
            if (undefined(key)) {
                parameters.put(key, value);
            }
        } else {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_ENSURE_UNKNOWN_KEY,
                            key
                    )
            );
        }
    }

    /**
     * Ensure that, if not defined, the list of keys will hold at least their
     * corresponding object values.
     * @param keys The list of keys.
     * @param values The list of object values.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public void ensure(List<String> keys, List<Object> values)
            throws NightingaleException {
        if (keys.size() != values.size()) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(messages.getMessage(
                            Messages.ERROR_ENSURE_DIFFERENT_SIZES,
                            keys.size(),
                            values.size())
                    )
            );
        } else {
            for (int i = 0; i < keys.size(); i++) {
                if (CommonUtils.isValidArgumentKey(keys.get(i))) {
                    if (undefined(keys.get(i))) {
                        parameters.put(keys.get(i), values.get(i));
                    }
                } else {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_ENSURE_UNKNOWN_KEY,
                                    keys.get(i)
                            )
                    );
                }
            }
        }
    }

    /**
     * Obtains the object value indexed by the provided key.
     * @param key The key.
     * @return The object value indexed by the provided key.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public Object obtain(String key) throws NightingaleException {
        if (undefined(key)) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(messages.getMessage(
                            Messages.ERROR_OBTAIN_INVALID_KEY,
                            key)
                    )
            );
        } else {
            return parameters.get(key);
        }
    }

    /**
     * Obtains the object indexed by the provided key or the default value if
     * the key is undefined.
     * @param key The key.
     * @param value The default value if the key is undefined.
     * @return The object indexed by the provided key or the default value if
     * the key is undefined.
     */
    public Object obtain(String key, Object value) {
        return (undefined(key) ? value : parameters.get(key));
    }

    /**
     * Check if the provided key holds the provided boolean value.
     * @param expected The provided boolean value.
     * @param key The provided key.
     * @return A boolean value indicating if the key holds the provided boolean
     * value.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean check(boolean expected, String key)
            throws NightingaleException {
        if (undefined(key)) {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(messages.getMessage(
                            Messages.ERROR_CHECK_INVALID_KEY,
                            key)
                    )
            );
        } else {
            Object value = parameters.get(key);
            if (value instanceof Boolean) {
                return (expected == (Boolean) value);
            } else {
                if (value instanceof String) {
                    return (expected ==
                            CommonUtils.checkBoolean((String) value));
                } else {
                    throw new NightingaleException(
                            CommonUtils.getRuleErrorHeader().
                            concat(messages.getMessage(
                                    Messages.ERROR_CHECK_UNKNOWN_TYPE,
                                    key)
                            )
                    );
                }
            }
        }
    }

    /**
     * Returns a string if the provided key holds the provided boolean value.
     * @param expected Boolean value.
     * @param key The key.
     * @param value The string.
     * @return The string if the provided key holds the provided boolean value,
     * or an empty string otherwise.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String check(boolean expected, String key, String value)
            throws NightingaleException {
        return (check(expected, key) ? value : "");
    }

    /**
     * Returns a string according to the evaluation of the provided key against
     * the provided boolean value.
     * @param expected The boolean value.
     * @param key The key.
     * @param value1 A string in case the evaluation holds.
     * @param value2 A string in case the evaluation does not hold.
     * @return A string according to the evaluation of the provided key against
     * the provided boolean value.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String check(boolean expected, String key,
            String value1, String value2) throws NightingaleException {
        return (check(expected, key) ? value1 : value2);
    }

    /**
     * Evaluates the conditional expression and returns a string value if the
     * evaluation holds true, or an empty string otherwise.
     * @param operation A boolean expression.
     * @param value The string value to be returned if the provided expression
     * evaluation holds true.
     * @return A string, result of the expression evaluation.
     */
    public String conditional(boolean operation, String value) {
        return (operation ? value : "");
    }

    /**
     * Evaluates the conditional expression and returns a string according to
     * the result.
     * @param operation A boolean expression.
     * @param value1 A string value to be returned if the provided expression
     * holds true.
     * @param value2 A string value to be returned if the provided expression
     * holds false.
     * @return A string, result of the expression evaluation.
     */
    public String conditional(boolean operation,
            String value1, String value2) {
        return (operation ? value1 : value2);
    }

    /**
     * Checks if a file exists based on the provided file extension. The file
     * name is obtained from the current file (might not be the reference file)
     * and the provided extension.
     * @param extension The file extension.
     * @return A boolean value indicating if the file exists.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean exists(String extension) throws NightingaleException {
        return CommonUtils.exists(extension);
    }

    /**
     * Checks if a file is missing based on the provided file extension. The
     * file name is obtained from the current file (might not be the reference
     * file) and the provided extension.
     * @param extension The file extension.
     * @return A boolean value indicating if the file is missing.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean missing(String extension) throws NightingaleException {
        return !exists(extension);
    }

    /**
     * Checks if a file has changed from the last run based on the provided file
     * extension. The file name is obtained from the current file (might not be
     * the reference file) and the provided extension.
     * @param extension The file extension.
     * @return A boolean value indicating if the file has changed from the last
     * run.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean changed(String extension) throws NightingaleException {
        return CommonUtils.hasChanged(extension);
    }

    /**
     * Checks if a file has not changed from the last run based on the provided
     * file extension. The file name is obtained from the current file (might
     * not be the reference file) and the provided extension.
     * @param extension The file extension.
     * @return A boolean value indicating if the file has not changed from the
     * last run.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean unchanged(String extension) throws NightingaleException {
        return !changed(extension);
    }

    /**
     * Checks if the provided file exists.
     * @param filename The file.
     * @return A boolean value indicating if the provided file exists.
     */
    public boolean exists(File filename) {
        return CommonUtils.exists(filename);
    }

    /**
     * Checks if the provided file is missing.
     * @param filename The file.
     * @return A boolean value indicating if the provided file is missing.
     */
    public boolean missing(File filename) {
        return !exists(filename);
    }

    /**
     * Checks if a file has changed from the last run based on the provided
     * file.
     * @param filename The file.
     * @return A boolean value indicating if the file has changed from the last
     * run.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean changed(File filename) throws NightingaleException {
        return CommonUtils.hasChanged(filename);
    }

    /**
     * Checks if a file has not changed from the last run based on the provided
     * file.
     * @param filename The file.
     * @return A boolean value indicating if the file has not changed from the
     * last run.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean unchanged(File filename) throws NightingaleException {
        return !changed(filename);
    }

    /**
     * Returns a file based on the provided string.
     * @param filename The provided string.
     * @return A file obtained from the provided string.
     */
    public File file(String filename) {
        return new File(filename);
    }

    /**
     * Gets the file name based on the provided file.
     * @param file The file.
     * @return A string representation containing the file name based on the
     * provided file.
     */
    public String filename(File file) {
        return file.getName();
    }

    /**
     * Builds a string with all objects separated by a space. Empty objects are
     * not considered.
     * @param objects An array of objects.
     * @return A string with all objects separated by a space.
     */
    public String build(Object... objects) {
        return CommonUtils.generateString(objects);
    }

    /**
     * Encloses the object in double quotes.
     * @param object The object.
     * @return A string representation of the object enclosed in double quotes.
     */
    public String quote(Object object) {
        return CommonUtils.addQuotes(object);
    }

    /**
     * Returns the basename of the provided file.
     * @param file The provided file.
     * @return A string representing the base name of the provided file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String basename(File file) throws NightingaleException {
        if (file.isFile()) {
            return CommonUtils.getBasename(file);
        } else {
            throw new NightingaleException(
                    CommonUtils.getRuleErrorHeader().
                    concat(messages.getMessage(
                            Messages.ERROR_BASENAME_NOT_A_FILE,
                            file.getName())
                    )
            );
        }
    }

    /**
     * Returns the base name of the provided string representing a file.
     * @param file A string representing a file.
     * @return A string representing the base name of the provided string
     * representing a file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String basename(String file) throws NightingaleException {
        return basename(new File(file));
    }

    /**
     * Obtains the file type of the provided file.
     * @param file The file.
     * @return A string representing the file type of the provided file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String filetype(File file) throws NightingaleException {
        if (file.isFile()) {
            return CommonUtils.getFiletype(file);
        } else {
            throw new NightingaleException(CommonUtils.getRuleErrorHeader().
                    concat(messages.getMessage(
                            Messages.ERROR_FILETYPE_NOT_A_FILE,
                            file.getName())
                    )
            );
        }
    }

    /**
     * Obtains the file type of the provided string representing a file.
     * @param file The string representing a file.
     * @return A string representing the file type of the provided file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String filetype(String file) throws NightingaleException {
        return filetype(new File(file));
    }

    /**
     * Checks if the provided string is empty.
     * @param value The provided string.
     * @return A boolean value indicating if the provided string is empty.
     */
    public boolean empty(String value) {
        return CommonUtils.checkEmptyString(value);
    }

    /**
     * Replicates the string pattern for every value in the list of objects.
     * @param pattern The string pattern to be replicated.
     * @param values The list of objects contained the value to be used in the
     * string pattern.
     * @return A list of objects containing the values merged to the pattern.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public List<Object> replicate(String pattern, List<Object> values)
            throws NightingaleException {
        return CommonUtils.replicateList(pattern, values);
    }

    /**
     * Checks if a file contains the provided regex based on the provided file
     * extension. The file name is obtained from the current file (might
     * not be the reference file) and the provided extension.
     * @param extension A string representing the file extension.
     * @param regex The regex.
     * @return A boolean value indicating if the regex was found.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean contains(String extension, String regex)
            throws NightingaleException {
        return CommonUtils.checkRegex(extension, regex);
    }

    /**
     * Checks if a file contains the provided regex based on the provided file.
     * The file name is obtained from the current file (might not be the
     * reference file) and the provided extension.
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value indicating if the regex was found.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean contains(File file, String regex)
            throws NightingaleException {
        return CommonUtils.checkRegex(file, regex);
    }

    /**
     * Throws the provided text as an exception.
     * @param text A string containing the error text.
     * @throws NightingaleException An exception is thrown with the provided
     * text.
     */
    public void error(String text) throws NightingaleException {
        throw new NightingaleException(text);
    }

    /**
     * Gets the current session object.
     * @return The current session object.
     */
    public Session session() {
        return session;
    }
    
    /**
     * Checks if the provided string matches with the underlying operating
     * system.
     * @param check The provided string.
     * @return A boolean value indicating if the underlying operating system
     * matches with the provided string.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean operatingsystem(String check)
            throws NightingaleException {
        return CommonUtils.checkOS(check);
    }
    
    /**
     * Returns the provided string if the check matches with the underlying
     * operating system.
     * @param check The provided check.
     * @param value The provided string to be returned if the check matches.
     * @return The provided string if the check matches with the underlying
     * operating system.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public String operatingsystem(String check, String value)
            throws NightingaleException {
        return CommonUtils.checkOS(check) ? value : "";
    }
    
    /**
     * Returns the first string if the provided check matches with the
     * underlying operating system, or the second string otherwise.
     * @param check The provided check.
     * @param value1 The first string.
     * @param value2 The second string.
     * @return One of the strings, according to the underlying operating system
     * match.
     * @throws NightingaleException 
     */
    public String operatingsystem(String check, String value1, String value2)
            throws NightingaleException {
        return CommonUtils.checkOS(check) ? value1 : value2;
    }

}
