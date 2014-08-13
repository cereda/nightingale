/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "ps2pdf",
    name : "PS2PDF",
    arguments : [ "options", "output" ],
    description : "PS2PDF",
    authors : [
        "Paulo Roberto Massa Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "PS2PDF",
        command : {

			ensure('output', basename(obtain('file')))

			def file = "${basename(obtain('file'))}.ps"
			def output = "${obtain('output')}.pdf"
			def options = ''
			
			if (defined('options')) {
				options = obtain('options')
				if (!(options instanceof List)) {
					error("I am sorry, but I expecting a list when using the 'options' argument.")
				}
			}
			
			return new Command('ps2pdf', options, file, output)

		},
        exit : { value -> return value == 0 }
    ]
]

