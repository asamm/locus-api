# Action tasks — reference

Every task of the `com.asamm.locus.ACTION_TASK` broadcast. How to send it and what to expect on
delivery: [overview](README.md).

Most tasks take an `action` parameter that selects the sub-action; other parameters list the
actions they apply to under Dependency.

## Tasks

### `dashboard`
> Locus Map/Classic 3.38+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Show the dashboard `name` over the main map screen |
| | `off` | - | Hide the active dashboard; nothing happens if none is active |
| | `toggle` | - | Show or hide the last selected dashboard (Locus Map 4.28+) |
| `name` | text | `action`:`on`, required | Name of the dashboard |

Sample: `{ "dashboard": { "action": "on", "name": "Cycling" } }`

### `function`

Start a "big" function (from those available for the functions panel) by its text id.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `value` | `altitude_manager` | - | Altitude manager |
| | `data_manager` | - | Screen with the user's points and tracks |
| | `floating_menu` | - | Menu for fast access to predefined functions (Locus Map 4.2+/Classic 3.53+) |
| | `quick_action_menu` | - | Deprecated alias of `floating_menu` |
| | `screen_compass` | - | Compass |
| | `screen_gps` | - | GPS skyplot |
| | `screen_map_items` | - | "Map items" |
| | `screen_maps` | - | Map manager |
| | `screen_store` | - | Locus Store |

Sample: `{ "function": { "value": "screen_compass" } }`

### `guide_to`
> Locus Map/Classic 3.49+

Start guidance to a point.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `name` | text | optional | Name of the point |
| `lat` | -90..90 | required | Latitude |
| `lon` | -180..180 | required | Longitude |

Sample: `{ "guide_to": { "name": "Hut", "lat": 50.12345, "lon": 15.12345 } }`

### `gps_on_off`
> Locus Map 4.17+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Turn the GNSS unit on |
| | `off` | - | Turn the GNSS unit off |
| | `toggle` | - | Toggle the GNSS unit |

Sample: `{ "gps_on_off": { "action": "toggle" } }`

### `live_tracking_asamm`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Start Asamm Live tracking with the configuration `name` |
| | `stop` | - | Stop Asamm Live tracking; with `name`, only if that configuration is running |
| `name` | text | `action`:`start`, required; `stop`, optional | Name of the configuration, as shown in the app |

Sample: `{ "live_tracking_asamm": { "action": "start", "name": "Family" } }`

### `live_tracking_custom`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Start custom Live tracking with the configuration `name`; nothing if it does not exist |
| | `stop` | - | Stop custom Live tracking; with `name`, only if that configuration is running |
| `name` | text | `action`:`start`, required; `stop`, optional | Name of the configuration |

Sample: `{ "live_tracking_custom": { "action": "stop" } }`

### `map_center`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Enable map centering |
| | `off` | - | Disable map centering |
| | `toggle` | - | Toggle map centering |

Sample: `{ "map_center": { "action": "on" } }`

### `map_hold_center`
> Locus Map 4.28+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Enable "hold map center" |
| | `off` | - | Disable "hold map center" |
| | `toggle` | - | Toggle "hold map center" |

Sample: `{ "map_hold_center": { "action": "toggle" } }`

### `map_layer_base`
> Locus Map 4.18+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `toggle_last` | - | Switch to the previously selected map |

Sample: `{ "map_layer_base": { "action": "toggle_last" } }`

### `map_move_x`

Move the map horizontally.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `value` | integer, positive or negative | required | Distance; positive moves right |
| `unit` | `%`, `dp`, `px`; default `px` | optional | Unit of `value` |
| `keep_centering` | `true`/`false`; default `false` | optional | `true` keeps map centering on; `false` turns it off by the move |

Sample: `{ "map_move_x": { "value": 25, "unit": "%" } }`

### `map_move_y`

Move the map vertically.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `value` | integer, positive or negative | required | Distance; positive moves down |
| `unit` | `%`, `dp`, `px`; default `px` | optional | Unit of `value` |
| `keep_centering` | `true`/`false`; default `false` | optional | `true` keeps map centering on; `false` turns it off by the move |

Sample: `{ "map_move_y": { "value": -100, "unit": "dp" } }`

### `map_move_zoom`
> Locus Map 4.2+/Classic 3.53+

Move and zoom the map in one task.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `move_by_x` | integer, positive or negative | optional | Relative horizontal move |
| `move_by_x_unit` | `%`, `dp`, `px`; default `px` | `move_by_x`, optional | Unit of `move_by_x` |
| `move_by_y` | integer, positive or negative | optional | Relative vertical move |
| `move_by_y_unit` | `%`, `dp`, `px`; default `px` | `move_by_y`, optional | Unit of `move_by_y` |
| `move_to_lat` | -90..90 | with `move_to_lon`; both non-zero | Absolute move, latitude |
| `move_to_lon` | -180..180 | with `move_to_lat`; both non-zero | Absolute move, longitude |
| `zoom` | 0..24; default `-1` (keep) | optional | New zoom level |
| `animate` | `true`/`false`; default `true` | optional | Animate the change |
| `keep_centering` | `true`/`false`; default `false` | optional | `true` keeps map centering on; `false` turns it off by the move |

Sample: `{ "map_move_zoom": { "move_to_lat": 50.08, "move_to_lon": 14.42, "zoom": 15 } }`

- `move_by_*` wins when both a relative and an absolute move are set.
- Without a move, only the zoom changes.
- Not for repeated calls (a held hardware button): the previous task may not have finished. Use
  `map_move_x` and `map_move_y` for that.

### `map_overlay`
> Locus Map 4.18+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Turn the map overlay on |
| | `off` | - | Turn the map overlay off |
| | `toggle` | - | Toggle the map overlay |

Sample: `{ "map_overlay": { "action": "toggle" } }`

### `map_reload_theme`
> Locus Map 4.18+

No parameters. Reloads the current map with its theme from disk; for theme authors, right after
copying an updated theme into the app directory.

Sample: `{ "map_reload_theme": {} }`

### `map_rotate`
> Locus Map/Classic 3.38+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Enable map rotation |
| | `off` | - | Disable map rotation |
| | `toggle` | - | Toggle map rotation |

Sample: `{ "map_rotate": { "action": "toggle" } }`

### `map_zoom`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `+` | - | Zoom in |
| | `-` | - | Zoom out |
| | `value` | - | Zoom to the level `value` |
| `value` | 0..24 | `action`:`value`, required | Zoom level |

Sample: `{ "map_zoom": { "action": "value", "value": 15 } }`

### `navigate_to`
> Locus Map/Classic 3.51+

Open the "Navigate to" screen with a point preset; navigation is started by the user.

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `name` | text | optional | Name of the point |
| `lat` | -90..90 | required | Latitude |
| `lon` | -180..180 | required | Longitude |

Sample: `{ "navigate_to": { "name": "Hut", "lat": 50.12345, "lon": 15.12345 } }`

### `navigation`
> Locus Map/Classic 3.51+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `nearest_point` | - | Set the nearest point on the active route as the navigation target |
| | `recalculate` | - | Recalculate the active navigation |
| | `stop` | - | Stop the active navigation |

Sample: `{ "navigation": { "action": "recalculate" } }`

### `open`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `menu` | - | Open the main menu |

Sample: `{ "open": { "action": "menu" } }`

### `poi_alert`
> Locus Map 4.2+/Classic 3.53+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Start POI Alert with the last used settings |
| | `stop` | - | Stop POI Alert |

Sample: `{ "poi_alert": { "action": "start" } }`

### `preset`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Activate the preset `name` |
| `name` | text | `action`:`start`, required | Name of the preset |

Sample: `{ "preset": { "action": "start", "name": "Hiking" } }`

### `quick_bookmark`
> Locus Map 4.0+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Open the quick bookmark `name` |
| `name` | text | `action`:`start`, required | Name of the bookmark |

Sample: `{ "quick_bookmark": { "action": "start", "name": "Wikipedia" } }`

### `screen_lock`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Lock the main screen |
| | `off` | - | Unlock the main screen |
| | `toggle` | - | Toggle the screen lock |

Sample: `{ "screen_lock": { "action": "toggle" } }`

### `screen_on_off`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `on` | - | Turn the screen on |
| | `off` | - | Turn the screen off |
| | `toggle` | - | Toggle the screen |

Sample: `{ "screen_on_off": { "action": "off" } }`

### `track_record`

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Start recording, or resume a paused one |
| | `stop` | - | Stop recording |
| | `pause` | - | Pause recording |
| | `discard` | - | Discard the recording |
| | `toggle` | - | Pause a running recording, resume a paused one |
| | `add_wpt` | - | Add a waypoint to the recording |
| `name` | text | `action`:`start`, optional | Switch to this profile first; the task ends if there is none |
| | | `action`:`stop`, `pause`, `discard`, optional | Act only if this profile is active |
| | | `action`:`add_wpt`, optional | Name of the waypoint |
| `auto_save` | `true`/`false` | `action`:`stop`, optional | Save the track; without it the profile's setting applies |
| | | `action`:`add_wpt`, optional | Save the waypoint at once; `action_after` is then ignored |
| `confirm` | `true`/`false`; default `true` | `action`:`discard`, optional | Ask the user first; never asked when the app is not visible |
| `allow_start` | `true`/`false`; default `false` | `action`:`toggle`, optional | Start recording when it is not running (Locus Map 4.36.1.6+; ignored before) |
| `description` | text | `action`:`add_wpt`, optional | Description of the waypoint |
| `action_after` | `audio`, `photo`, `video` | `action`:`add_wpt`, optional | Take that kind of waypoint |

Sample: `{ "track_record": { "action": "start", "name": "Cycling" } }`

### `weather`
> Locus Map 4.15+

| Parameter | Value | Dependency | Effect |
|---|---|---|---|
| `action` | `start` | - | Open the weather for `lat`, `lon`; the map center without valid coordinates |
| `lat` | -90..90 | `action`:`start`, optional | Latitude |
| `lon` | -180..180 | `action`:`start`, optional | Longitude |

Sample: `{ "weather": { "action": "start", "lat": 50.08, "lon": 14.42 } }`
