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

/**
 * Implements the base directive conditional. Every directive conditional will
 * have this class set as base, so all methods enclosed here will be available
 * in the conditional context.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class BaseConditional extends Script {

    // the file reference, not necessarily the current file
    // being processed; this file represents the main file
    // from which the directives were extracted (the main
    // file provided in the command line)
    private final File reference;
    
    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Constructor. It sets the file reference obtained from the configuration
     * controller.
     */
    public BaseConditional() {
        reference = (File) ConfigurationController.
                getInstance().
                get("execution.reference");
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
     * Evaluates the conditional expression and returns an object value if the
     * evaluation holds true, or an empty string otherwise.
     * @param operation A boolean expression.
     * @param value The object value to be returned if the provided expression
     * evaluation holds true.
     * @return Am object, result of the expression evaluation.
     */
    public Object conditional(boolean operation, Object value) {
        return (operation ? value : "");
    }

    /**
     * Evaluates the conditional expression and returns an object according to
     * the result.
     * @param operation A boolean expression.
     * @param value1 An object value to be returned if the provided expression
     * holds true.
     * @param value2 An object value to be returned if the provided expression
     * holds false.
     * @return An object, result of the expression evaluation.
     */
    public Object conditional(boolean operation,
            Object value1, Object value2) {
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
                    messages.getMessage(
                            Messages.ERROR_BASENAME_NOT_A_FILE,
                            file.getName()
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
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_FILETYPE_NOT_A_FILE,
                            file.getName()
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
     * Gets the file name based on the provided file.
     * @param file The file.
     * @return A string representation containing the file name based on the
     * provided file.
     */
    public String filename(File file) {
        return file.getName();
    }

}
