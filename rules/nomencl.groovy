/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "nomencl",
    name : "Nomencl",
    arguments : [ "options", "style" ],
    description : "Nomencl",
    authors : [
        "Paulo Roberto Massa Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "Nomenclature",
        command : {

			def file = basename(obtain('file'))
			def style = defined('style') ? [ '-s', obtain('style') ] : ''
			def options = ''
			
			if (defined('options')) {
				options = obtain('options')
				if (!(options instanceof List)) {
					error("I am sorry, but I expecting a list when using the 'options' argument.")
				}
			}
			
			return new Command('makeindex', options, "${file}.nlo", style, '-o', "${file}.nls")

		},
        exit : { value -> return value == 0 }
    ]
]

