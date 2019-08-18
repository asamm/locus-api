[ ![Download](https://api.bintray.com/packages/asammsoft/maven/locus-api-android/images/download.svg) ](https://bintray.com/asammsoft/maven/locus-api-android/_latestVersion)

# Locus API

Library for [Locus Map](http://www.locusmap.eu) application for Android devices.

## Current version

Latest stable LT version: **0.2.25**

Versions 0.9.x are in rewrite-mode, so use on own risk (& report issues if found) 

How to **update to new 0.9.x** version? More about it [here](https://github.com/asamm/locus-api/wiki/Update-to-version-0.9.0).

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

Add dependency to your `build.gradle` module config

```gradle
dependencies {
     // get locus API
     compile 'com.asamm:locus-api-android:[latest]'
}
```

Check for sample use-cases in Locus API - Android sample project
