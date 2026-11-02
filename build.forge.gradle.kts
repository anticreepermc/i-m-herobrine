plugins {
	id("mod-platform")
	alias(libs.plugins.forgegradle)
}

stonecutter {
	val (version, loader) = current.project.split('-', limit = 2)
	properties.tags(version, loader)

	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace("location()", "identifier()")
	}
}

platform {
	loader = "forge"
	dependencies {
		required("minecraft") {
			forgeLikeVersionRange = prop("deps.minecraft")
		}
		required("forge") {
			forgeLikeVersionRange.set("[1,)")
		}
	}
}

minecraft {
	mappings("official", prop("deps.minecraft"))
	accessTransformers = files(rootProject.file("src/main/resources/aw/${sc.current.version}.cfg"))
	runs {
		configureEach {
			args("--mixin.config=${prop("mod.id")}.mixins.json")
		}
		register("client") {
			workingDir.convention(layout.projectDirectory.dir("run"))
			args("--username=Dev")
		}
		register("server") {
			workingDir.convention(layout.projectDirectory.dir("run"))
			args("--nogui")
		}
	}
}

repositories {
	minecraft.mavenizer(this)
	maven(fg.forgeMaven)
	maven(fg.minecraftLibsMaven)
	mavenCentral()
	mavenLocal()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
}

dependencies {
	implementation(minecraft.dependency("net.minecraftforge:forge:${prop("deps.minecraft")}-${prop("deps.forge")}"))
	annotationProcessor("net.minecraftforge:eventbus-validator:7.0.5")
	annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")

	// implementation(libs.moulberry.mixinconstraints)
	// jarJar(libs.moulberry.mixinconstraints)
}

sourceSets {
	main {
		resources.srcDir(
			"${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated"
		)
	}
}

