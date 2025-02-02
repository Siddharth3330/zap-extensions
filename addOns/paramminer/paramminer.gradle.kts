description = "Identify hidden, unlinked parameters. Useful for finding web cache poisoning vulnerabilities."

zapAddOn {
    addOnName.set("Parameter Miner")
    zapVersion.set("2.11.1")

    manifest {
        author.set("ZAP Dev Team and Arkaprabha Chakraborty")

        dependencies {
            addOns {
                register("commonlib") {
                    version.set(">= 1.9.0 & < 2.0.0")
                }
            }
        }
    }
}

dependencies {
    compileOnly(parent!!.childProjects.get("commonlib")!!)

    testImplementation(parent!!.childProjects.get("commonlib")!!)
    testImplementation(project(":testutils"))
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
        tokens.put("%helpPath%", resourcesPath)
    }
}
