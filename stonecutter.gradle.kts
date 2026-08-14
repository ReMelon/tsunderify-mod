plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"

/*
// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
 */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "tsunderify"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
        }
    }
}