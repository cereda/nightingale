/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "indent",
    name : "Indent",
    arguments : [ "silent", "overwrite", "trace", "settings", "cruft", "output" ],
    description : "Indent",
    authors : [
		"Chris Hughes",
        "Paulo Roberto Massa Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "The latexindent.pl script",
        command : {

			ensure('cruft', '')
			ensure('settings', '')
			ensure('output', '')

			def file = filename(obtain('file'))
			def silent = defined('silent') ? check(true, 'silent', '-s') : ''
			def overwrite = defined('overwrite') ? check(true, 'overwrite', '-w') : ''
			def trace = defined('trace') ? check(true, 'trace', '-t') : ''
			
			def settings = obtain('settings')
			if (!empty(settings)) {
				if (!(settings in [ "local", "onlydefault" ])) {
					error("I am sorry, you provided an invalid value for settings: ${settings}")
				}
				else {
					settings = conditional(settings == "local", "-l", "-d")
				}
			}
			
			def cruft = obtain('cruft')
			if (!empty(cruft)) {
				cruft = "-c=${cruft}"
			}
			
			def output = obtain('output')
			if (!empty(output)) {
				output = [ '-o', output ]
			}
			
			return new Command('latexindent', silent, trace, settings, cruft, overwrite, output, file)
			
		},
        exit : { value -> return value == 0 }
    ]
]

