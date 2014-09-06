/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "tex",
    name : "TeX",
    arguments : [ "interaction", "shell", "options" ],
    description : "The TeX engine",
    authors : [
	"Marco Daniel",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "TeX engine",
        command : {

		def file = basename(obtain('file'))
		def shell = defined('shell') ? check(true, 'shell', '--shell-escape', '--no-shell-escape') : ''	
		def options = ''
		def interaction = ''

		if (defined('interaction')) {
			interaction = obtain('interaction')
			if (!( interaction in [ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" ] )) {
				error("I am sorry, but you provided an invalid interaction mode: ${interaction}")
			}
			else {
				interaction = "--interaction=${interaction}"
			}
		}

		if (defined('options')) {
			options = obtain('options')
			if (!(options instanceof List)) {
				error("I am sorry, but I was expecting a list when using the 'options' argument.")
			}
		}

		return new Command('tex', interaction, shell, options, file)

	},
        exit : { value -> return value == 0 }
    ]
]

