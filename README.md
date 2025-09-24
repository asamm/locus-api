<p align="center">
    <a href="#current-version">Current version</a> | 
    <a href="#structure">Structure</a> | 
    <a href="#what-does-it-do">What does it do?</a> | 
    <a href="#what-it-isnt">What it isn't</a> | 
    <a href="#quick-start">Quick start</a>
</p>

# Locus API

Library for [Locus Map](https://www.locusmap.app) application for Android devices.

## Current version

Latest stable LT version: [![](https://jitpack.io/v/asamm/locus-api.svg)](https://jitpack.io/#asamm/locus-api)

## Structure

Whole API is divided into two separate parts:

- library written in pure Java - **Locus API - Core**
- its extension for Android devices - **Locus API - Android**

In most cases, Android version is the only interesting one here.

## What does it do?

- main purpose is a transport tool for various objects (points/tracks)
- allows to check state of certain functions like periodic updates, units defined by user and more 
- allows to control track recording and partially also navigation features
- allows to handle field notes completely
- allows to generate map preview of a certain area & zoom level

## What it isn't

- a replacement for Google Maps API or other map library that substitutes map core to your own application
- a standalone library that may work without Locus Map application

**For creating Locus Map add-ons it is needed to handle only Locus API - Android library. Locus API is automatically added as dependency.**

## Quick start

Add [JitPack](https://jitpack.io) repository to your root `build.gradle` module config.

```gradle.kts
allprojects {
  repositories {
    maven(url = "https://jitpack.io")
  }
}
```

Add dependency to your `build.gradle` module config

```gradle.kts
dependencies {
     // get Locus API (Java only)
     implementation('com.github.asamm.locus-api:locus-api-core:[latest]')
     
     // or Locus Android API (for Android apps, core included)
     implementation('com.github.asamm.locus-api:locus-api-android:[latest]')
}
```

Check for sample use-cases in Locus API - Android sample project.

### Proguard

When using Proguard/R8, test release build properly. 

It may be necessary to keep API internal object untouched. To do this, add following proguard config into your module "proguard-rules.pro" file.

```
# Keep Locus API objects
-keep class locus.api.objects.** { *; }
```

## New version release steps

1. Raise version in the `gradle.properties`
   * `API_CODE`
   * `API_VERSION`
2. update "CHANGELOG" news file
3. commit changes to GitHub
4. tag commit with "X.X.X" pattern
