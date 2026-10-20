import java.io.ByteArrayOutputStream

plugins {
    antlr
    java
}

spotless {
    java {
        targetExclude(fileTree(layout.buildDirectory) { include("**/*.java") })
    }
}

dependencies {
    implementation("antlr:antlr:2.7.7")
    implementation(project(":asn1bean"))
}

val filesToConfig = mapOf(
    setOf("tagging-test.asn") to arrayOf("-dv"),
    setOf("compile-test.asn") to arrayOf("-dv", "-l"),
    setOf("modules1.asn", "modules2.asn") to arrayOf("-dv"),
    setOf("iso-presentation-layer.asn") to arrayOf("-dv", "-l"),
    setOf("x690-ber-example.asn") to arrayOf("-dv"),
    setOf("mobile/PKIXExplicit88.asn", "mobile/PKIXImplicit88.asn", "mobile/RSPDefinitionsV2.2.asn") to arrayOf("-dv", "-l"),
    setOf("mobile/PEDefinitionsV2.3.1.asn") to arrayOf("-dv", "-l"),
    setOf("various-tests.asn") to arrayOf("-dv", "-l"),
    setOf("information-object-class.asn") to arrayOf("-dv", "-l"),
    setOf("extension-test1.asn") to arrayOf("-dv"),
    setOf("extension-test2.asn") to arrayOf("-dv", "-e"),
    setOf("enum-test.asn") to arrayOf("-dv"),
    setOf("cross-module-types.asn") to arrayOf("-dv"),
    setOf("explicit-extension-marker.asn") to arrayOf("-dv", "-e"),
)

val configuredAsn1BeanOutputs = filesToConfig.entries.mapIndexed { index, (files, params) ->
    val outputDir = layout.buildDirectory.dir("generated-src/asn1bean-compiler/test/${index + 1}").get()
    val generatorTask = tasks.register<JavaExec>("generateConfiguredAsn1Beans${index + 1}") {
        dependsOn("compileJava")
        inputs.files(files.map { file("src/test/resources/$it") })
        outputs.dir(outputDir)

        val taskErrorOutput = ByteArrayOutputStream()
        errorOutput = taskErrorOutput
        doFirst {
            outputDir.asFile.deleteRecursively()
            taskErrorOutput.reset()
        }

        jvmArgs("-Dfile.encoding=UTF8")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("com.beanit.asn1bean.compiler.Compiler")
        args("-p", "com.beanit.asn1bean.compiler")
        args("-o", outputDir.asFile.absolutePath)
        args("-f")
        files.forEach { args("src/test/resources/$it") }
        params.forEach { args(it) }

        doLast {
            if (taskErrorOutput.size() != 0) {
                throw RuntimeException(
                    "Error generating ASN.1 file using asn1bean:\n$taskErrorOutput")
            }
        }
    }
    generatorTask to outputDir
}

tasks.register("generateConfiguredAsn1Beans") {
    dependsOn(configuredAsn1BeanOutputs.map { it.first })
}

tasks.named("compileTestJava") {
    dependsOn("generateConfiguredAsn1Beans")
}

java.sourceSets["test"].java {
    srcDirs(configuredAsn1BeanOutputs.map { it.second })
}

tasks.matching { it.name == "sourcesJar" }.configureEach {
    dependsOn("generateGrammarSource")
}
project.extra["cfgModuleName"] = "com.beanit.asn1bean.compiler"

mavenPublishing {

    coordinates(
        project.group.toString(),
        project.name,
        project.version.toString()
    )

    pom {
        name.set("ASN1bean Compiler")
        description.set("ASN1bean Compiler generates Java classes out of ASN.1 code that can be used to encode/decode BER data.")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}
