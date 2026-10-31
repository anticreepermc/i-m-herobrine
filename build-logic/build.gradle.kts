plugins {
	`kotlin-dsl`
	kotlin("plugin.serialization") version embeddedKotlinVersion
}

gradlePlugin {
	plugins {
		register("modPlatform") {
			id = "mod-platform"
			implementationClass = "ModPlatformPlugin"
		}
		register("modRoot") {
			id = "mod-root"
			implementationClass = "ModRootPlugin"
		}
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
	maven("https://maven.fabricmc.net/") { name = "Fabric" }
	maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
	maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
	maven("https://jitpack.io") { name = "Jitpack" }
}

dependencies {
	implementation(libs.kikugie.postprocess)
	implementation(libs.kikugie.stonecutter)
	implementation(libs.kikugie.loomx)
	implementation(libs.mod.publish.plugin)
	implementation(libs.fletching.table)
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.jvm.get()}")
	implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.versions.devtools.ksp.get()}")
	implementation(libs.vanniktech.maven.publish)
	implementation(libs.serialization.json)
	implementation(libs.serialization.toml)
}
