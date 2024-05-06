# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.9.55]
### Fixed
- [#54](https://github.com/asamm/locus-api/issues/54), missing "Virtual" cache size implementation

## [0.9.54]
### Added
- `GeocachingData.CACHE_SIZE_VIRTUAL` new size value

### Changed
- `Location` object internal rework & breaking compatibility changes

## [0.9.52]
### Added
- `GeocachingData.cacheUrlFull` support for Lab caches
- `GeoDataExtra.PAR_LOMEDIA` as container for LoMedia objects

## [0.9.51]
### Added
- `GeoDataExtra.PAR_RTE_WARNINGS` as container with Route warnings
- `GeoDataExtra.SOURCE_ROUTE_WARNING` as source ID for warning points

### Changed
- updated `GeoDataExtra.RTE_TYPES` values
- updated dependencies

## [0.9.50]
### Changed
- removed custom implementation of the Logger and using public "Asamm logger" instead
- refactoring in the `GeoDataExtra` class
- separating extensions to Point & Track into separate helper class

## [0.9.49]
### Changed
- split geocache notes to local and external

### Removed
- `ballonStyle` and `listStyle` from `GeoDataStyle`

## [0.9.48]
### Added
- temperature and power to track statistics

## [0.9.47]
### Added
- via-point notified during navigation (`PAR_RTE_POINT_PASS_PLACE_NOTIFY` in [GeoDataExtra])
- cross-country skiing profile (`VALUE_RTE_TYPE_SKI_CROSS_COUNTRY` in [GeoDataExtra])

### Changed
- removed deprecated track statistics methods
- minor [GeoDataExtra] refactoring

## [0.9.46]
### Changed
- updated work with parameters in [GeoData] objects

## [0.9.45]
### Changed
- reverted `Location` object close to old system due to high memory footprint in new solution

## [0.9.44]
### Changed
- reduced memory footprint for `Location` object
- `Storable` object now correctly accept serialized size up to 50MB (on own risk)

## [0.9.43]
### Changed
- updates in the `Location` serialization

## [0.9.42]
### Fixed
- `Location` incorrect order of objects
- validation of `Location` variables

## [0.9.41]
### Changed
- `Location` accuracy split to horizontal and vertical

### Fixed
- build problems in the Android module

## [0.9.40]
### Changed
- `Location` object has completely new structure for meta-data. Changes in clients code needed.
- `SparseArrayCompat` updated to latest version

## [0.9.39]
### Added
- `units` parameter for Circle object radius

## [0.9.38]
### Added
- "next `ViaPoint`" info in `UpdateContainer` for active navigation

## [0.9.37]
### Changed
- updated IDs for LoPoints objects

## [0.9.36]
### Fixed
- [#36](https://github.com/asamm/locus-api/issues/36), queries for A11+ package visibility
- fixed sending of files for import into system/app (Sample app)
- [#37](https://github.com/asamm/locus-api/issues/37), fixed, incorrect description for map zoom level the `UpdateContainer`

## [0.9.35] - 2021-05-03
### Changed
- increased size limit of the `Storable` objects to 50MB

## [0.9.34] - 2021-04-25
### Changed
- [#35](https://github.com/asamm/locus-api/issues/35), reverted removed of the "Huge" size of geocaches (used in OpenCaching)

## [0.9.33] - 2021-04-23
### Changed
- validation of geocaches
- removed "Huge" size of geocaches (not used in Geocaching)

## [0.9.32] - 2021-04-07
### Changed
- [#33](https://github.com/asamm/locus-api/issues/33), setup of new system for Mavencentral repository

## [0.9.29] - 2021-04-07
### Added
- new `SendTrack` and `SendTracks` API objects to simplify sending tracks to the app

### Changed
- minor refactoring in `Logger` instance

## [0.9.28] - 2021-03-09
### Changed
- added `SHARED_URL` privacy option for GeoData
- removed `PUBLIC` privacy option for GeoData

## [0.9.27] - 2021-02-17
### Fixed
- [#32](https://github.com/asamm/locus-api/issues/32), incorrect creating of GeocachingAttributes

## [0.9.26] - 2021-02-16
### Added
- parameters for online LoPoints TimeZone

## [0.9.25] - 2021-02-09
### Added
- parameters for online LoPoints metadata

## [0.9.24] - 2021-01-21
### Added
- 4 new geocaching attributes

## [0.9.23] - 2021-01-18
### Changed
- updates in sample project dependencies
- making sample app works with recent API 30+

### Fixed
- problem in heart rate values obtained from stats

## [0.9.22] - 2021-01-04
### Changed
- [#30](https://github.com/asamm/locus-api/issues/30), made `Location` object open

## [0.9.21] - 2020-12-18
### Changed
- cacheUrl generated with `https` protocol

## [0.9.20] - 2020-11-25
### Changed
- readWriteMode parameter changed to "protected" parameter
- minor updates in TrackStats (mainly naming)

## [0.9.17] - 2020-09-21
### Changed
- updated extension helper for work with FieldNotes

## [0.9.16] - 2020-09-10
### Changed
- `GeoDataExtra.PAR_DB_POI_ONLINE_ID` parameter for online POI (internal feature)

## [0.9.15] - 2020-08-27
### Added
- support for "privacy" settings for `GeoData`
- storage for special surface & way types for routes in `GeoDataExtra` class

## [0.9.13] - 2020-05-21
### Changed
- better Java support for `GeoData`

## [0.9.12] - 2020-05-21
### Added
- `timeUpdated` parameter to `GeoData` object
### Changed
- simplified work with `GeoData` parameters

## [0.9.10] - 2020-05-12
### Added
- language parameter to `ExtraData`, to specify language of the content

### Changed
- updated list of possible geocache types

## [0.9.9] - 2020-04-14
### Added
- `GeoDataExtra.PAR_LANGUAGE` to set a language of the GeoData content

## [0.9.8] - 2019-11-20
### Added
- option to get all keys attached to `GeoData` object
- new key value for metadata in `GeoDataExtra` object

## [0.9.7] - 2019-10-16
### Fixed
- incorrect setup of point symbol scale

## [0.9.6] - 2019-10-07
### Fixed
- problem with conversion of old Line styles to new system

## [0.9.5] - 2019-09-13
### Changed
- license changed to *MIT*
- improved API to request on map preview

## [0.9.4] - 2019-09-09
### Fixed
- restored internal support for old deprecated LineStyleOld and PolyStyleOld classes

## [0.9.3] - 2019-08-22
### Fixed
- minor Nullability issues

## [0.9.2] - 2019-08-19
### Changed
- united function that share points over file

### Fixed
- geocaching cacheID
- obtain of parameters from GeoDataExtra

## [0.9.0] - 2019-08-18
### Changed
- major conversion of most of Locus API (core) code into Kotlin
- modifications in API in Locus API Android
- removed `PeriodicUpdates` system, `UpdateContainer` now needs to be fetched manually (see Dashboard sample)
- major cleanup: removed most of deprecated code 

## [0.3.17] - 2019-07-24
### Changed
- minor update in PackPoints class construction

### Fixed
- `getPointsId` call, fix #23
- compile problem with Sample app

## [0.3.15] - 2019-07-16
### Added
- track source definition over GeoDataExtra

### Changed
- few classes converted to Kotlin

## [0.3.14] - 2019-04-23
### Added
- adaptive icon in sample app `_data` directory

### Fixed
- obtain of trackable code from Trackable url
- "extra callback" mallfunction

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
- [fixed #9](https://github.com/asamm/locus-api/issues/9), getting info about available Locus versions 

## [0.2.20] - 2018-03-04
### Changed
- updated versions of relevant libraries
### Deprecated
- TracksStats > cumulative elevation parameters

## [pre 0.2.20]
### Long history
