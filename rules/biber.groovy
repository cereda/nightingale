/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "biber",
    name : "Biber",
    arguments : [ "options" ],
    description : "The Biber reference management software",
    authors : [
	"Marco Daniel",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "Biber reference management",
        command : {

		def file = basename(obtain('file'))
		def options = ''

		if (defined('options')) {
			options = obtain('options')
			if (!(options instanceof List)) {
				error("I am sorry, but I expecting a list when using the 'options' argument.")
			}
		}

		return new Command('biber', options, file)

	},
        exit : { value -> return value == 0 }
    ]
]

