/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "makeglossaries",
    name : "MakeGlossaries",
    arguments : [ "options" ],
    description : "The MakeGlossaries software",
    authors : [
	"Marco Daniel",
	"Nicola Talbot",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "MakeGlossaries",
        command : {

		def file = basename(obtain('file'))
		def options = ''

		if (defined('options')) {
			options = obtain('options')
			if (!(options instanceof List)) {
				error("I am sorry, but I expecting a list when using the 'options' argument.")
			}
		}

		return new Command('makeglossaries', options, file)

	},
        exit : { value -> return value == 0 }
    ]
]

