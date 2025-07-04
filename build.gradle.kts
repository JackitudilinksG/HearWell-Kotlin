// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application")           version "8.11.0" apply false
    id("org.jetbrains.kotlin.android")      version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}