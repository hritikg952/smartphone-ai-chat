import java.util.zip.ZipFile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.smartphoneaichat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartphoneaichat"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "HF_TOKEN", "\"${project.findProperty("HF_TOKEN") ?: ""}\"")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "LEGACY_RUNTIME_ENABLED", "false")
        }
        create("legacy") {
            initWith(getByName("debug"))
            isDebuggable = true
            matchingFallbacks += listOf("debug")
            buildConfigField("boolean", "LEGACY_RUNTIME_ENABLED", "true")
        }
        release {
            isMinifyEnabled = true
            buildConfigField("boolean", "LEGACY_RUNTIME_ENABLED", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    // Compose BOM — manages all Compose library versions
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // Activity Compose — single-activity entry point
    implementation("androidx.activity:activity-compose:1.9.0")

    // Lifecycle — ViewModel + collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    // Core KTX
    implementation("androidx.core:core-ktx:1.13.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Debug tooling
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Legacy AI compiles for preservation but is packaged only in the explicit legacy build.
    compileOnly("com.google.ai.edge.litertlm:litertlm-android:latest.release")
    add("legacyImplementation", "com.google.ai.edge.litertlm:litertlm-android:latest.release")

    // CameraX
    val cameraxVersion = "1.3.4"
    compileOnly("androidx.camera:camera-camera2:$cameraxVersion")
    compileOnly("androidx.camera:camera-lifecycle:$cameraxVersion")
    compileOnly("androidx.camera:camera-view:$cameraxVersion")
    add("legacyImplementation", "androidx.camera:camera-camera2:$cameraxVersion")
    add("legacyImplementation", "androidx.camera:camera-lifecycle:$cameraxVersion")
    add("legacyImplementation", "androidx.camera:camera-view:$cameraxVersion")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.3")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.register("verifyHealthVaultArtifact") {
    group = "verification"
    description = "Verifies that the default Health Vault APK excludes legacy native runtimes."
    dependsOn("assembleDebug")

    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile
        val forbiddenNames = listOf("liblitertlm_jni.so", "libimage_processing_util_jni.so")
        val packagedForbiddenLibraries = ZipFile(apk).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { entry -> forbiddenNames.any { forbidden -> entry.endsWith(forbidden) } }
                .toList()
        }
        check(packagedForbiddenLibraries.isEmpty()) {
            "Default Health Vault APK packages legacy native libraries: $packagedForbiddenLibraries"
        }
    }
}

val verifyHealthVaultBoundaries by tasks.registering {
    group = "verification"
    description = "Rejects platform, UI, data, and legacy-runtime imports from the active Health Vault core."

    doLast {
        val coreFiles = listOf(
            file("src/main/java/com/smartphoneaichat/domain/model/AppSessionState.kt"),
            file("src/main/java/com/smartphoneaichat/domain/repository/AppSessionStore.kt"),
        )
        val forbiddenImports = listOf(
            "import android.",
            "import androidx.",
            "import com.google.ai.edge.litertlm",
            "import com.smartphoneaichat.data.",
            "import com.smartphoneaichat.presentation.",
            "import com.smartphoneaichat.ui.",
        )
        val violations = coreFiles.flatMap { sourceFile ->
            sourceFile.readLines().mapIndexedNotNull { index, line ->
                if (forbiddenImports.any(line::startsWith)) {
                    "${sourceFile.relativeTo(projectDir)}:${index + 1}: $line"
                } else {
                    null
                }
            }
        }
        check(violations.isEmpty()) {
            "Health Vault core boundary violations:\n${violations.joinToString("\n")}"
        }
    }
}

tasks.named("check").configure {
    dependsOn(verifyHealthVaultBoundaries)
}
