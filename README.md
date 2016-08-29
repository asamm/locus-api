[ ![Download](https://api.bintray.com/packages/asammsoft/maven/locus-api/images/download.svg) ](https://bintray.com/asammsoft/maven/locus-api/_latestVersion)
[ ![Download](https://api.bintray.com/packages/asammsoft/maven/locus-api-android/images/download.svg) ](https://bintray.com/asammsoft/maven/locus-api-android/_latestVersion)

# Locus API

Library for [Locus Map](http://www.locusmap.eu) application for Android devices.

Whole API is divided into two separate parts:

- library written in pure Java - **Locus API**
- it's extension for Android devices - **Locus API - Android**

In most case, Android version is only interesting here.

## What does it do

- main purpose is transport tool for various objects (points/tracks)
- allows to check state of certain functions like Periodic updates, units defined by user and more 
- allows to control track recording and partially also navigation features
- allows to completely handle Field notes
- allows to generate map preview for certain area & zoom level

## What it isn't

- replacement for Google Maps API or other map library that substitude map core to your own application
- standalone library that may works without Locus Map application

**For creating of Locus Map add-ons, it is needed to handle only Locus API - Android library. Locus API is automatically added as dependency.**

## Quick start

Add dependency to your `build.gradle` module config

```gradle
dependencies {
     // get locus API
     compile 'com.asamm:locus-api-android:0.2.6'
}
```

Check for sample use-cases in Locus API - Android sample project
