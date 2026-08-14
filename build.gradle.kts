import me.modmuss50.mpp.ReleaseType

plugins {
    // This plugin applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_targets")
    ?.asList().orEmpty().map { it.toString() }

val compatibleRange = if (compatibleVersions.size == 1) {
    compatibleVersions.single()
} else {
    "${compatibleVersions.first()}-${compatibleVersions.last()}"
}
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val versionTypeRaw = property("mod.version_type") as String
val versionType: ReleaseType = when {
    versionTypeRaw.lowercase() == "stable" -> ReleaseType.STABLE
    versionTypeRaw.lowercase() == "beta" -> ReleaseType.BETA
    else -> ReleaseType.ALPHA
}

val modId = property("mod.id") as String
val modVersion = property("mod.version") as String

loomx.modJar.configure {
    archiveFileName.set("$modId-$modVersion+$compatibleRange.jar")
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    mavenCentral()

    // YACL
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }

    // Mod Menu
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
}

dependencies {
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, sc.properties["deps.fabric_api"]))
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")

    if (sc.current.parsed < "26.1") {
        fapi("fabric-key-binding-api-v1")
    } else {
        fapi("fabric-key-mapping-api-v1")
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Publishes builds to Modrinth, Curseforge and GitHub with changelog from the CHANGELOG.md file
publishMods {
    file = loomx.modJar.map { it.archiveFile.get() }
    displayName = "${property("mod.name")} ${property("mod.version")} for $compatibleRange"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = versionType
    modLoaders.add("fabric")

    dryRun = property("publish.dry_run") == "true"

    if (dryRun.get() || env.MODRINTH_TOKEN.orElse("") != "") {
        modrinth {
            projectId = property("publish.modrinth") as String
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            accessToken = env.MODRINTH_TOKEN.orElse("")
            minecraftVersions.addAll(compatibleVersions)

            requires("fabric-api", "yacl", "modmenu")

            announcementTitle = "Modrinth"
        }
    }

    github {
        accessToken = env.GITHUB_TOKEN.orElse("")
        parent(rootProject.tasks.named("publishGithub"))
        announcementTitle = "GitHub"
    }
    /*
    if (dryRun.get() || env.CURSEFORGE_TOKEN.orElse("") != "") {
        curseforge {
            changelog = changelogSimple
            projectId = property("publish.curseforge_id") as String
            projectSlug = property("publish.curseforge_slug") as String
            accessToken = env.CURSEFORGE_TOKEN.orElse("")
            minecraftVersions.addAll(property("mc_targets").toString().split(' '))

            requires("fabric-api", "yacl")
            optional("modmenu")

            announcementTitle = "CurseForge"
        }
    }


     */
}

/*
// Publishes builds to Modrinth and Curseforge with changelog from the CHANGELOG.md file
publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("mod.mc_title")}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
        || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
        requires {
            slug = "fabric-api"
        }
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))
        requires {
            slug = "fabric-api"
        }
    }
}
 */
/*
// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
publishing {
    repositories {
        maven("https://maven.example.com/releases") {
            name = "myMaven"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.id") as String
            version = project.version

            from(components["java"])
        }
    }
}
 */