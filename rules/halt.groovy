/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Trigger

rule = [
    id : "halt",
    name : "Halt",
    arguments : [],
    description : "The halt trigger",
    authors : [
	"Heiko Oberdiek",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "Halt trigger",
        command : {

		return new Trigger('halt', [])

	},
        exit : { value -> return true }
    ]
]

