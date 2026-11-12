# Action tasks — overview

Control a running Locus Map from another app, Tasker or adb by a broadcast carrying JSON tasks.
The list of tasks and their parameters is in the [task reference](tasks.md).

## Sending

| Part | Value |
|---|---|
| Intent action | `com.asamm.locus.ACTION_TASK` |
| Delivery | broadcast (`Context.sendBroadcast`), in Tasker a `Broadcast Receiver` target |
| Package | `menion.android.locus` (Locus Map); required on Android 8+ |
| Extra `tasks` | JSON object: each key is a task id, its value an object of task parameters |

```kotlin
val intent = Intent("com.asamm.locus.ACTION_TASK")
    .setPackage("menion.android.locus")
    .putExtra("tasks", """{ "map_zoom": { "action": "+" } }""")
context.sendBroadcast(intent)
```

From adb:

```
adb shell am broadcast -p menion.android.locus -a com.asamm.locus.ACTION_TASK --es tasks '{"map_zoom":{"action":"+"}}'
```

## Delivery

- **No reply.** Locus Map does not confirm a task or report its result.
- Locus Map waits for its own start-up: up to 8 s for a broadcast sent with
  `Intent.FLAG_RECEIVER_FOREGROUND`, otherwise up to 30 s. If it is not ready by then, it drops
  the tasks and shows a notification asking the user to open the app.
- Tasks that open a screen (`function`, `navigate_to`, `weather`, …) need an app screen to be open;
  without one they are ignored.
- An unknown task id is ignored. An unknown `function` value shows a short message in the app.
- Several tasks may be sent in one broadcast, but their **order is not guaranteed**. Send dependent
  tasks as separate broadcasts.

## Values

- Numbers and booleans may be sent as JSON values or as strings: `"zoom": 15` and `"zoom": "15"`
  are the same.
- Coordinates are WGS84 decimal degrees, `lat` in -90..90, `lon` in -180..180.

## Samples

### Zoom in the map

_extra_: `{ "map_zoom": { "action": "+" } }`

### Move with the map

to the right and bottom by 25% of the screen

_extra_: `{ "map_move_x": { "value": 25, "unit": "%" }, "map_move_y": { "value": 25, "unit": "%" } }`

### Start track recording

_extra_: `{ "track_record": { "action": "start", "name": "Hiking" } }`

### Start a function

_extra_: `{ "function": { "value": "floating_menu" } }`

### Start guidance to a point

_extra_: `{ "guide_to": { "name": "My point", "lat": 50.12345, "lon": 15.12345 } }`
