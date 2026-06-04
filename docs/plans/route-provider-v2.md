# Routing-provider API (compute-track) — evolution plan

Forward-looking plan for the compute-track / routing-provider API: a proposed V2 contract and the
decision on when — and whether — to build it.

> Status: **proposal / deferred.** No V2 is scheduled. Revisit per the trigger below.

## Background — the design problem

A routing provider is a third-party app exposing a bound `ComputeTrackService` (AIDL
`IComputeTrackService`) that Locus binds to turn via-points into a `Track`. See
[`../android/guides/routing-apps/`](../android/guides/routing-apps/).

The current API puts **static capability metadata behind an async binding**: to learn a provider's
attribution, supported route types, and transit-point count, the consumer must bind the service and
make live AIDL calls. Binding is asynchronous, so a consumer that needs those capabilities
synchronously (to populate a picker or a planner UI) has to wait for, or race, the bind.

Capabilities are *static* (they don't change between calls); only `computeTrack` is *dynamic*. The
API conflates the two — that is the root limitation V2 would remove.

## Current state

The reliability of consuming this API in the host app has been addressed app-side; that handling is
internal to Locus and out of scope for this contract doc. On the public-API side, two low-risk,
**non-breaking** changes have shipped (locus-api
[#73](https://github.com/asamm/locus-api/pull/73)):

- `ComputeTrackService.numOfTransitPoints` is now `open`, so a provider can advertise via-point
  support (it was previously impossible to override).
- A working provider sample and a developer guide ([`../android/guides/routing-apps/`](../android/guides/routing-apps/)).

No wire-format / AIDL change was made. The async-capability limitation above remains the open design
question that V2 addresses.

## Proposed V2 (deferred)

Bring the routing surface up to the model the sensor-adapter framework already validated
([`../android/guides/adapter-apps/`](../android/guides/adapter-apps/)): **declare static
capabilities in a manifest XML read before binding; keep the bound service for the dynamic call
only.**

| Concern | Today (V1) | V2 |
|---|---|---|
| Discovery | intent-filter only | intent-filter + `<meta-data>` XML |
| Static capabilities | over AIDL, after bind | declared in `res/xml`, read **pre-bind, synchronously** |
| Versioning | none | `apiVersion` in the XML → incompatible providers filtered before bind (cf. `AdapterApi.VERSION`) |
| AIDL surface | 5 methods (4 are metadata getters) | just the dynamic op: `computeTrack` (+ async result) |
| Result | blocking call returning `Track?` / null | async result code (`OK` / `NO_ROUTE` / `ERROR` / `CANCELLED`), cancellable, timeout-able |

Decisive win: capabilities become a synchronous, always-available manifest read with **no bind** —
a consumer can list and configure a provider without ever touching the service. Binding happens only
when a route is computed.

**Migration is additive — never mutate `IComputeTrackService`.** Introduce V2 alongside V1 (a
`RouteProviderApi.VERSION` constant, a new `res/xml` schema, a slim V2 AIDL, a new base class). A
consumer prefers a provider's V2 metadata when declared and otherwise uses V1 as today. Existing
providers keep working untouched.

## Decision & trigger

**The reliability concerns that motivated a rethink were solvable without a contract change** (done
app-side), so they do not on their own justify V2. The marginal API wins (bind-free first-use
metadata, pre-bind version filtering) are real but modest.

**The real trigger is _functionality_, not reliability.** Build V2 when the contract needs to grow
in a way V1 can't carry cleanly — e.g. no-go areas, route alternatives, isochrones, structured
error / partial results, or cancellation. At that point the manifest-XML + slim-async-AIDL design
above is the blueprint. Until then, prefer additive evolution of V1.

## Do not break V1

A breaking change to this contract would strand existing *and future* third-party integrations
built against the published artifact, regardless of current adoption. Adoption is assessed
privately before any such decision is considered; this doc's default stance is **evolve additively,
do not break V1**.

## Open questions / next steps

- If a functional driver appears, draft the V2 `res/xml` schema + slim AIDL + base class as a
  concrete strawman.
