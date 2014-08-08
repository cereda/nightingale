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
import com.github.cereda.nightingale.utils.ConfigurationUtils;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implements the configuration model, which holds the default settings and can
 * load the configuration file.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Configuration {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Loads the application configuration.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static void load() throws NightingaleException {
        
        // initialize both file type and language models,
        // since we can track errors from there instead
        // of relying on a check on this level
        FileType.init();
        Language.init();
        
        // reset everything
        reset();
        
        // get the configuration file, if any
        File file = ConfigurationUtils.getConfigFile();
        if (file != null) {
            
            // then validate it and update the
            // configuration accordingly
            Map<String, Object> mapping = ConfigurationUtils.
                    validateConfiguration(file);
            update(mapping);
        }
        
        // just to be sure, update the
        // current locale in order to
        // display localized messages
        Locale locale = ((Language) ConfigurationController.
                getInstance().get("execution.language")).getLocale();
        LanguageController.getInstance().setLocale(locale);
    }

    /**
     * Resets the configuration to initial settings.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void reset() throws NightingaleException {
        
        // put everything in a map to be
        // later assigned to the configuration
        // controller, which holds the settings
        Map<String, Object> mapping = new HashMap<String, Object>();

        mapping.put("execution.loops", 10L);
        mapping.put("directives.charset", Charset.forName("UTF-8"));
        mapping.put("execution.errors.halt", true);
        mapping.put("execution.timeout", false);
        mapping.put("execution.timeout.value", 0L);
        mapping.put("execution.timeout.unit", TimeUnit.MILLISECONDS);
        mapping.put("application.version", new BigDecimal(1.0));
        mapping.put("directives.linebreak.pattern", "^\\s*-->\\s(.*)$");
        
        String directive = "^\\s*(\\w+)\\s*(:\\s*(\\{.*\\})\\s*)?";
        String pattern = "(\\s+(if|while|until|unless)\\s+(\\S.*))?$";
        
        mapping.put("directives.pattern", directive.concat(pattern));
        mapping.put("application.pattern", "nightingale:\\s");
        mapping.put("application.width", 65);
        mapping.put("execution.database.name", "nightingale");
        mapping.put("execution.log.name", "nightingale");
        mapping.put("execution.verbose", false);
        mapping.put("trigger.halt", false);
        mapping.put("execution.language", new Language("en"));
        mapping.put("execution.logging", false);
        mapping.put("execution.dryrun", false);
        mapping.put("application.copyright.year", "2014");
        mapping.put("execution.filetypes", ConfigurationUtils.
                getDefaultFileTypes()
        );
        mapping.put("execution.rule.paths", Arrays.asList(
                CommonUtils.buildPath(ConfigurationUtils.getApplicationPath(),
                        "rules"))
        );

        // get the configuration controller and
        // set every map key to it
        ConfigurationController controller = ConfigurationController.
                getInstance();
        for (String key : mapping.keySet()) {
            controller.put(key, mapping.get(key));
        }
    }

    /**
     * Update the configuration based on the provided map.
     * @param data Map containing the new configuration settings.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static void update(Map<String, Object> data)
            throws NightingaleException {

        // get the configuration controller
        // and assign it to a local variable
        ConfigurationController controller =
                ConfigurationController.getInstance();
        
        // add new rule paths
        if (data.containsKey("rule paths")) {
            @SuppressWarnings("unchecked")
            List<String> paths = (List<String>) data.get("rule paths");
            paths = ConfigurationUtils.normalizePaths(paths);
            controller.put("execution.rule.paths", paths);
        }

        // add new file types
        if (data.containsKey("file types")) {
            @SuppressWarnings("unchecked")
            List<FileType> types = (List<FileType>) data.get("file types");
            types = ConfigurationUtils.normalizeFileTypes(types);
            controller.put("execution.filetypes", types);
        }

        // enable timeout
        if (data.containsKey("enable timeout")) {
            controller.put("execution.timeout",
                    (Boolean) data.get("enable timeout")
            );
        }

        // set the timeout value
        if (data.containsKey("timeout value")) {
            long value = (Long) data.get("timeout value");
            if (value > 0) {
                controller.put("execution.timeout.value", value);
            } else {
                throw new NightingaleException(messages.getMessage(
                        Messages.ERROR_CONFIGURATION_TIMEOUT_INVALID_RANGE)
                );
            }
        }

        // set the timeout unit
        if (data.containsKey("timeout unit")) {
            controller.put("execution.timeout.unit",
                    (TimeUnit) data.get("timeout unit")
            );
        }

        // enable logging
        if (data.containsKey("enable logging")) {
            controller.put("execution.logging",
                    (Boolean) data.get("enable logging")
            );
        }

        // set the verbose mode
        if (data.containsKey("verbose mode")) {
            controller.put("execution.verbose",
                    (Boolean) data.get("verbose mode")
            );
        }

        // set the database name
        if (data.containsKey("database name")) {
            controller.put("execution.database.name",
                    ConfigurationUtils.cleanFileName(
                            (String) data.get("database name")
                    )
            );
        }

        // set the log name
        if (data.containsKey("log name")) {
            controller.put("execution.log.name",
                    ConfigurationUtils.cleanFileName(
                            (String) data.get("log name")
                    )
            );
        }

        // set the new application language
        if (data.containsKey("language")) {
            controller.put("execution.language",
                    (Language) data.get("language")
            );
        }

        // set a flag that indicates if nightingale
        // should stop when an error is found
        if (data.containsKey("halt on error")) {
            controller.put("execution.errors.halt",
                    (Boolean) data.get("halt on error")
            );
        }

        // set the directive charset
        if (data.containsKey("directive charset")) {
            controller.put("directives.charset",
                    (Charset) data.get("directive charset")
            );
        }

        // set the maximum number of loops
        if (data.containsKey("maximum number of loops")) {
            long value = (Long) data.get("maximum number of loops");
            if (value > 0) {
                controller.put("execution.loops", value);
            } else {
                throw new NightingaleException(messages.getMessage(
                        Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE)
                );
            }
        }
    }

}
