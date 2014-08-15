/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "clean",
    name : "Clean",
    arguments : [ "extensions" ],
    description : "A rule to clean files according to a certain criteria",
    authors : [
        "Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "Cleaning feature",
        command : {

			def file = obtain('file')
			def prefix = []
			
			if (operatingsystem('unix')) {
				prefix = [ 'rm', '-rf' ]
			}
			else {
				prefix = [ 'cmd', '/c', 'del' ]
			}
			
			
			if (undefined('extensions')) {
				if (file == reference()) {
					error("I am sorry, but I cannot remove the main file reference.")
				}
				
				return new Command(prefix, filename(file))
			}
			else {
				def extensions = obtain('extensions')
				if (!(extensions instanceof List)) {
					error("I am sorry, but I was expecting 'extensions' to be a list.")
				}
				
				def removals = []
				def element = basename(file)
				
				for (extension in extensions) {
					removals.add(new Command(prefix, "${element}.${extension}"))
				}
				
				return removals
				
			}
		},
        exit : { value -> return true }
    ]
]

