import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

stonecutter active "1.21.11"

val versionTypeRaw = property("mod.version_type") as String
val versionType: ReleaseType = when {
    versionTypeRaw.lowercase() == "stable" -> ReleaseType.STABLE
    versionTypeRaw.lowercase() == "beta" -> ReleaseType.BETA
    else -> ReleaseType.ALPHA
}

publishMods {
    displayName = "${property("mod.name")} ${property("mod.version")}"
    changelog = rootProject.file("CHANGELOG.md").readText()
    version = property("mod.version") as String
    type = versionType
    dryRun = property("publish.dry_run") == "true" || env.GITHUB_TOKEN.orElse("") == ""

    github {
        accessToken = env.GITHUB_TOKEN.orElse("")
        repository = property("publish.github_repo") as String
        commitish = "main"
        tagName = version

        allowEmptyFiles = true
    }
}

// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

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