# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.13] - 2019-04-23
### Changed
- support for storing reference to Locus Store for `Track` objects
- `PeriodicUpdates` container converted to Kotlin
- `PeriodicUpdatesHandler` made deprecated. Requesting container directly recommended.

### Fixed
- start of Locus Store over API

## [0.3.11] - 2019-04-13
### Changed
- removed modification of geocache listings upon set

### Fixed
- problem with `ActionsBasic.getPoint()` function

## [0.3.10] - 2019-03-21
### Fixed
- missing `extraInfo` in `NavPoint` containers

## [0.3.9] - 2019-03-18
### Added
- `UpdateContainerGuidePoint` and `UpdateContainerGuideTrack` containers in `UpdateContainer`
- extra metadata for `NavPoint` in `UpdateContainerGuideTrack`

## [0.3.8] - 2019-03-13
### Added
- `gpsLocValid` parameter in `UpdateContainer`

### Changed
- few more functions moved from `ActionTools` to Kotlin based `ActionBasics`

## [0.3.7] - 2019-02-28
### Changed
- united adding attachments to `GeoDataExtra`
- removed support for GcImages descriptions (deprecated in new gc.com API)

### Fixed
- detection of altitude values in `TrackStats`

## [0.3.6] - 2019-02-21
### Changed
- updated Kotlin, Gradle & build tools
- `pickFile`, `pickDir` moved to `ActionFiles.kt`
- `ActionDisplayX` classes converted to Kotlin & minor updates

### Fixed
- sharing of files over `ActionFiles.importFileLocus`
- permission for `ActionDisplayPoints.sendPacksFile`

## [0.3.5] - 2019-02-08
### Added
- requesting tracks in certain file format over `ActionBasics.getTrackInFormat`

### Changed
- part of `LocusUtils` functions converted to `IntentHelper` object 

## [0.3.4] - 2019-01-18
### Added
- search for visible points on the map based on coordinates & radius
- option to start navigation/guiding on point defined by it's ID 

### Changed
- part of `ActionTools` moved to `ActionBasics`. Kotlin improved version

## [0.3.3] - 2018-12-20
### Added
- handling files over FileProvider API
- samples for new "broadcasts API"
### Changed
- base java API renamed to "locus-api-core" (no change in your app needed, if you use `locus-api-android`)
### Fixed
- static access to `ActionDisplayPoints.removePackFromLocus` method

## [0.3.2] - 2018-11-12
### Fixed
- ID of guidance target

## [0.3.1] - 2018-11-12
### Changed
- 'PackWaypoints' renamed to 'PackPoints'
- 'UpdateContainer' now contains point/track ID of active guide
- updated samples to AndroidX

## [0.3.0] - 2018-10-05
### Changed
- 'Storable' class variables have to be initialized in constructor, not in removed 'reset' call
- removed other constructors for 'Storable' class. Use Storable.read if creating instance from existing data
- renamed 'Waypoint' class to 'Point'
- other minor refractoring

## [0.2.25] - 2018-08-14
### Changed
- updated loading of last field note log
- add active dashboard and active live tracking ID into update container
- changes in gradle build system versions
### Fixed
- problem with missing locus-api dependency in locus-api-android module
- geotour attribute for geocaches

## [0.2.22] - 2018-05-30
### Added
- support for Geocaching trackables over API
- `GeoDataExtra.PAR_POI_ALERT_INCLUDE` flag to manually include/exluce certain point from POI Alert

## [0.2.21] - 2018-04-05
### Changed
- [fixed #9](https://github.com/asamm/locus-api/issues/9) - getting info about available Locus versions 

## [0.2.20] - 2018-03-04
### Changed
- updated versions of relevant libraries
### Deprecated
- TracksStats > cumulative elevation parameters

## [pre 0.2.20]
### Long history
