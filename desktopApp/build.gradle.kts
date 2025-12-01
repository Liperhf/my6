import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared"))

                // LWJGL core + OpenGL + GLFW
                val lwjglVersion = "3.3.3"
                implementation("org.lwjgl:lwjgl:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
                implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")

                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows")
                runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
    }
}
