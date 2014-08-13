/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "makeindex",
    name : "MakeIndex",
    arguments : [ "german", "style" ],
    description : "The MakeIndex software",
    authors : [
        "Paulo Roberto Massa Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "MakeIndex",
        command : {

			def file = basename(obtain('file'))
			def german = defined('german') ? check(true, 'german', '-g') : ''
			def style = defined('style') ? [ '-s', obtain('style') ] : ''
			
			return new Command('makeindex', german, style, file)

		},
        exit : { value -> return value == 0 }
    ]
]

