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

import com.github.cereda.nightingale.Nightingale;
import com.github.cereda.nightingale.controller.LanguageController;
import com.github.cereda.nightingale.model.NightingaleException;
import com.github.cereda.nightingale.model.FileType;
import com.github.cereda.nightingale.model.Language;
import com.github.cereda.nightingale.model.Messages;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Implements configuration utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class ConfigurationUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Gets the configuration file located at the user home directory, if any.
     * @return The file reference to the external configuration, if any.
     */
    public static File getConfigFile() {
        List<String> names = Arrays.asList(
                ".nightingalerc.groovy",
                "nightingalerc.groovy"
        );
        for (String name : names) {
            String path = CommonUtils.buildPath(SystemUtils.USER_HOME, name);
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Validates the configuration file.
     * @param file The configuration file.
     * @return The configuration file properly parsed as a map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static Map<String, Object> validateConfiguration(File file)
            throws NightingaleException {
        
        // create a new shell to evaluate
        // the configuration script
        Binding binding = new Binding();
        CompilerConfiguration configuration = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell(binding, configuration);
        try {
            shell.evaluate(file);
            Object holder = shell.getVariable("config");
            ConfigurationUtils.checkConfiguration(holder);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) holder;
            return result;
        } catch (CompilationFailedException cfexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATECONFIGURATION_COMPILATION_FAILED
                    ),
                    cfexception
            );
        } catch (IOException ioexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATECONFIGURATION_IO_EXCEPTION
                    ),
                    ioexception
            );
        } catch (UnsupportedCharsetException ucexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATECONFIGURATION_UNSUPPORTED_CHARSET
                    ),
                    ucexception
            );
        } catch (Exception exception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATECONFIGURATION_GENERIC_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Checks if the configuration is valid.
     * @param holder The object holding the configuration map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkConfiguration(Object holder)
            throws NightingaleException {
        if (holder == null) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_CHECKCONFIGURATION_NULL_VARIABLE
                    )
            );
        } else {
            if (!CommonUtils.checkClass(Map.class, holder)) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_CHECKCONFIGURATION_NOT_A_MAP
                        )
                );
            } else {
                
                // the mapping contains all possible
                // entries in the configuration path
                // and their respective classes
                Map<String, Class> mapping = new HashMap<String, Class>();
                mapping.put("rule paths", List.class);
                mapping.put("file types", List.class);
                mapping.put("directive charset", Charset.class);
                mapping.put("maximum number of loops", Long.class);
                mapping.put("halt on error", Boolean.class);
                mapping.put("database name", String.class);
                mapping.put("log name", String.class);
                mapping.put("enable timeout", Boolean.class);
                mapping.put("timeout value", Long.class);
                mapping.put("timeout unit", TimeUnit.class);
                mapping.put("verbose mode", Boolean.class);
                mapping.put("enable logging", Boolean.class);
                mapping.put("language", Language.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) holder;
                for (String key : mapping.keySet()) {
                    if (config.containsKey(key)) {
                        if (!CommonUtils.checkClass(
                                mapping.get(key),
                                config.get(key))) {
                            throw new NightingaleException(
                                    messages.getMessage(
                                            Messages.ERROR_CHECKCONFIGURATION_WRONG_CLASS_TYPE,
                                            key,
                                            mapping.get(key).getSimpleName()
                                    )
                            );
                        }
                    }
                }
                checkFileTypes(config.get("file types"));
                checkPaths(config.get("rule paths"));
                if (!CommonUtils.checkMaps(config, mapping)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_CHECKCONFIGURATION_UNKNOWN_KEYS,
                                    CommonUtils.getCollectionElements(
                                            CommonUtils.getUnknownKeys(
                                                    config,
                                                    mapping
                                            ),
                                            "(",
                                            ")",
                                            ", "
                                    )
                            )
                    );
                }
            }
        }
    }

    /**
     * Checks if the file types defined in the configuration file are actually
     * file types.
     * @param holder The file types list from the map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkFileTypes(Object holder)
            throws NightingaleException {
        if (holder != null) {
            List list = (List) holder;
            for (Object element : list) {
                if (!CommonUtils.checkClass(FileType.class, element)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_CHECKFILETYPES_NOT_A_FILETYPE
                            )
                    );
                }
            }
        }
    }

    /**
     * Checks if the rule paths defined in the configuration file are actually
     * a list of strings.
     * @param holder The rule paths list from the map.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void checkPaths(Object holder) throws NightingaleException {
        if (holder != null) {
            List list = (List) holder;
            for (Object element : list) {
                if (!CommonUtils.checkClass(String.class, element)) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_CHECKPATHS_NOT_A_STRING
                            )
                    );
                }
            }
        }
    }

    /**
     * Gets the list of default file types provided by nightingale, in order.
     * @return The list of default file types, in order.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static List<FileType> getDefaultFileTypes()
            throws NightingaleException {
        return Arrays.asList(
                new FileType("tex"),
                new FileType("dtx"),
                new FileType("ltx"),
                new FileType("drv"),
                new FileType("ins")
        );
    }

    /**
     * Normalize a list of rule paths, removing all duplicates.
     * @param paths The list of rule paths.
     * @return A list of normalized paths, without duplicates.
     * @throws NightingaleException 
     */
    public static List<String> normalizePaths(List<String> paths)
            throws NightingaleException {
        paths.add(CommonUtils.buildPath(getApplicationPath(), "rules"));
        Set<String> set = new LinkedHashSet<String>(paths);
        List<String> result = new ArrayList<String>(set);
        return result;
    }

    /**
     * Normalize a list of file types, removing all duplicates.
     * @param types The list of file types.
     * @return A list of normalized file types, without duplicates.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static List<FileType> normalizeFileTypes(List<FileType> types)
            throws NightingaleException {
        types.addAll(getDefaultFileTypes());
        Set<FileType> set = new LinkedHashSet<FileType>(types);
        List<FileType> result = new ArrayList<FileType>(set);
        return result;
    }

    /**
     * Gets the canonical absolute application path.
     * @return The string representation of the canonical absolute application
     * path.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static String getApplicationPath() throws NightingaleException {
        try {
            String path = Nightingale.class.
                    getProtectionDomain().
                    getCodeSource().
                    getLocation().
                    getPath();
            path = URLDecoder.decode(path, "UTF-8");
            path = new File(path).getParentFile().getPath();
            return path;
        } catch (UnsupportedEncodingException exception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Cleans the file name to avoid invalid entries.
     * @param name The file name.
     * @return A cleaned file name.
     */
    public static String cleanFileName(String name) {
        String result = (new File(name)).getName().trim();
        if (CommonUtils.checkEmptyString(result)) {
            return "nightingale";
        } else {
            return result.trim();
        }
    }

}
