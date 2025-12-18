plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

android {
    namespace = "com.mapbox.mcp"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Mapbox Maps SDK
    api(libs.mapbox.maps)

    // JSON Serialization
    implementation(libs.kotlinx.serialization.json)

    // Android Basics
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Maven publishing configuration for JitPack
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.mapbox.mcp"
                artifactId = "mapbox-mcp-tools"
                version = "1.0.0"

                pom {
                    name.set("Mapbox MCP Tools")
                    description.set("Android library that wraps Mapbox Maps SDK functionality as MCP tools for LLM control")
                    url.set("https://github.com/moritzzzzz/android_mapbox_mcp_wrapper")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("moritzzzzz")
                            name.set("Moritz Foerster")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/moritzzzzz/android_mapbox_mcp_wrapper.git")
                        developerConnection.set("scm:git:ssh://github.com/moritzzzzz/android_mapbox_mcp_wrapper.git")
                        url.set("https://github.com/moritzzzzz/android_mapbox_mcp_wrapper")
                    }
                }
            }
        }
    }
}
