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
import com.github.cereda.nightingale.model.NightingaleException;
import com.github.cereda.nightingale.model.Database;
import com.github.cereda.nightingale.model.Messages;
import java.io.File;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Implements database utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class DatabaseUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Loads the XML file representing the database.
     * @return The database object.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static Database load() throws NightingaleException {
        if (!exists()) {
            return new Database();
        } else {
            File file = new File(getPath());
            try {
                Serializer serializer = new Persister();
                Database database = serializer.read(Database.class, file);
                return database;
            } catch (Exception exception) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_LOAD_COULD_NOT_LOAD_XML,
                                file.getName()
                        ),
                        exception
                );
            }
        }
    }

    /**
     * Saves the database on a XML file.
     * @param database The database object.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static void save(Database database) throws NightingaleException {
        File file = new File(getPath());
        try {
            Serializer serializer = new Persister();
            serializer.write(database, file);
        } catch (Exception exception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_SAVE_COULD_NOT_SAVE_XML,
                            file.getName()
                    ),
                    exception
            );
        }
    }

    /**
     * Checks if the XML file representing the database exists.
     * @return A boolean value indicating if the XML file exists.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static boolean exists() throws NightingaleException {
        File file = new File(getPath());
        return file.exists();
    }

    /**
     * Gets the path to the XML file representing the database.
     * @return A string representing the path to the XML file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static String getPath() throws NightingaleException {
        String name = ((String) ConfigurationController.
                getInstance().
                get("execution.database.name")
                ).concat(".xml");
        String path = CommonUtils.getParentCanonicalPath(getReference());
        return CommonUtils.buildPath(path, name);
    }

    /**
     * Gets the main file reference.
     * @return The main file reference.
     */
    private static File getReference() {
        return (File) ConfigurationController.
                getInstance().
                get("execution.reference");
    }

}
