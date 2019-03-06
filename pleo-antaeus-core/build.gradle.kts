plugins {
    kotlin("jvm")
}

dataLibs()

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    implementation("com.sun.mail:javax.mail:1.6.2")

    compile(project(":pleo-antaeus-models"))
}