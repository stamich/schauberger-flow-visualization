import org.gradle.api.tasks.ScalaSourceDirectorySet

plugins {
    scala
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("me.champeau.jmh") version "0.7.3"
}

group = "io.codeswarm"
version = "0.6.0-buildfix1"

repositories {
    mavenCentral()
}

val scalaVersion = "2.13.15"
val scalaFxVersion = "21.0.0-R32"
val scalaTestVersion = "3.2.19"

dependencies {
    implementation("org.scala-lang:scala-library:$scalaVersion")
    implementation("org.scalafx:scalafx_2.13:$scalaFxVersion")

    testImplementation("org.scalatest:scalatest_2.13:$scalaTestVersion")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.19.0")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_2.13:2.3.0")
}


sourceSets.named("jmh") {
    extensions.getByType(ScalaSourceDirectorySet::class.java).srcDir("src/jmh/scala")
}

jmh {
    warmupIterations = 5
    iterations = 8
    fork = 1
    resultFormat = "JSON"
    resultsFile = project.file("benchmark/results/jmh-0.6.0.json")
}

javafx {
    version = "21.0.5"
    modules("javafx.controls")
}

application {
    mainClass.set("io.codeswarm.schauberger.application.SchaubergerFlowApplication")
}

tasks.test {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<JavaExec>("benchmark") {
    group = "verification"
    description = "Runs milestone 0.6 statistical simulation, geometry, profile and accuracy benchmarks and writes JSON results."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.codeswarm.schauberger.benchmark.SimulationBenchmark")
}
