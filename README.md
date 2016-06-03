# Locus API

[ ![Download](https://api.bintray.com/packages/asammsoft/maven/locus-api/images/download.svg) ](https://bintray.com/asammsoft/maven/locus-api-android/_latestVersion)

Core library for [Locus Map](http://www.locusmap.eu) application for Android devices.

## What does it do

Library serve mainly as transport tool for various objects in Locus API - Android project. Because of usage also in different Locus Map > Server libraries (and some others), it's separated from Locus API - Android module.

**For creating of Locus Map add-ons, you do not need to handle this library directly. It's automatically added as dependency to [Locus API - Android](https://github.com/asamm/locus-api-android) project.**

## Installation

Add dependency to your `build.gradle` module config

```gradle
dependencies {
     // get locus API
     compile 'com.asamm:locus-api:0.2.3'
}
```

