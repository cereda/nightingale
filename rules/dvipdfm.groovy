/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "dvipdfm",
    name : "DVIPDFM",
    arguments : [ "options", "output" ],
    description : "DVIPDFM",
    authors : [
		"Marco Daniel",
        "Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "DVIPDFM",
        command : {

			ensure('output', basename(obtain('file')))

			def file = "${basename(obtain('file'))}.dvi"
			def output = "${obtain('output')}.ps"
			def options = ''
			
			if (defined('options')) {
				options = obtain('options')
				if (!(options instanceof List)) {
					error("I am sorry, but I expecting a list when using the 'options' argument.")
				}
			}
			
			return new Command('dvipdfm', file, '-o', output, options)

		},
        exit : { value -> return value == 0 }
    ]
]

