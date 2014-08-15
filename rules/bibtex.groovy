/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "bibtex",
    name : "BibTeX",
    arguments : [ "options" ],
    description : "The BibTeX reference management software",
    authors : [
		"Marco Daniel",
        "Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "BibTeX reference management",
        command : {

			def file = basename(obtain('file'))
			def options = ''
			
			if (defined('options')) {
				options = obtain('options')
				if (!(options instanceof List)) {
					error("I am sorry, but I expecting a list when using the 'options' argument.")
				}
			}

			return new Command('bibtex', options, file)

		},
        exit : { value -> return value == 0 }
    ]
]

