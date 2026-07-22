# Workout Logging UI - Design

## Context

The backend for workouts and sets is being finished now (`WorkoutController`,
`SetController`, `WorkoutService`/`WorkoutServiceImpl`, repositories - all
mid-implementation on this branch). There is no frontend for workouts yet.
The app has no SPA framework or build step: pages are server-rendered
Thymeleaf templates styled with a small inline `<style>` block per page, with
vanilla JS `fetch()` calls handling all CRUD interactions. This design adds a
workouts section following the same conventions already used by
`measurement.html` / `measurementDay.html` (calendar -> day detail) and
`workoutType.html` (status-card CRUD list).

## Goals

- Support both logging a workout in the moment and reviewing past workouts,
  equally.
- One workout per calendar day per user.
- Within a workout, sets are grouped and entered by exercise.
- Reuse existing visual/interaction conventions; no new styling system.

## Non-goals

- Querying workouts by date range on the backend. Client-side fetch-all
  (matching `measurement.html`'s existing approach) is fine for now; revisit
  once a user's workout history is large enough that fetching all workouts
  on every calendar/day load becomes a real cost.
- Multiple workouts per day.
- A flat/ungrouped set-entry mode.

## Pages & routing

Two new page routes added to `WorkoutController` (it currently only exposes
`/api/workouts/*`, no view routes):

- `GET /workout` -> `workout.html` (month calendar)
- `GET /workout/{date}` -> `workoutDay.html` (day detail)

Both follow `MeasurementController`'s pattern exactly: the controller method
only builds view-model data (month grid / the requested date), and the page's
own JS fetches everything else from the JSON API and filters client-side.

No new backend query methods are needed for either page. The day page passes
`date` into the model and, like `measurementDay.html`, filters the full
`GET /api/workouts` response client-side for the entry matching that date
(there is at most one, since a day has at most one workout). The calendar
page also fetches `GET /api/workouts` once and colors client-side.

A link to `/workout` is added to `home.html` next to the existing
Measurements / Workout Types links.

## Calendar page (`workout.html`)

Same month-grid structure and nav (prev/next month links, "today" highlight)
as `measurement.html`, reusing its CSS. Each day cell is colored by fetching
`GET /api/workouts` plus, for coloring precision, whether that workout has
any sets:

- **Red** - no workout logged that day
- **Yellow** - workout exists (a type was picked) but has zero sets
- **Green** - workout has at least one set

## Day-detail page (`workoutDay.html`)

**No workout yet for this date:** a single card with a dropdown of the
user's workout types (`GET /api/workoutType`) and a "Start Workout" button
-> `POST /api/workouts {date, workoutTypeId}`. On success, re-render into
the logging view below without a full page reload.

**Workout exists:**

- **Header card** - workout type name, with inline edit (change type) and
  delete (remove the whole workout) controls, matching the
  edit/delete/save button pattern used in `workoutType.html` and
  `home.html`.
- **Exercise cards** - one per exercise that has at least one set in this
  workout. Built by fetching `GET /api/workouts/{id}/sets` and grouping the
  results by `exerciseId` (using the new `exerciseName` field - see Backend
  changes). Each card shows:
  - the exercise name as its header
  - existing sets as rows (set number, reps, RIR, weight) with inline
    edit/delete controls
  - an "add set" form at the bottom (reps, RIR, weight) ->
    `POST /api/sets`
- **Add Exercise control** - a dropdown of the user's exercises
  (`GET /api/exercise`) plus an "Add" button. Selecting an exercise either
  creates a new, empty exercise card (client-side only - no API call until
  the first set is actually submitted) or scrolls to the existing card if
  that exercise already has sets in this workout.

All mutations re-fetch and re-render the affected card(s) only, matching the
`refreshCard()` pattern in `measurementDay.html`.

## Backend changes

**`SetResponseDTO` gains `exerciseName`.** Populated from
`set.getExercise().getName()` everywhere `WorkoutServiceImpl` builds one
(`addSetToWorkout`, `updateSet`, `getSetsByWorkoutId`, `getSetById`). Needed
so the frontend can render exercise-card headers without a second
cross-referencing fetch against `/api/exercise`.

**`setNumber` is auto-incremented on creation.** Currently
`addSetToWorkout` never calls `set.setSetNumber(...)`, so every set is
persisted with the default `0`. Fix: before saving, count existing sets for
the same `(workoutId, exerciseId)` pair and use `count + 1`. Requires one
new repository method:
`SetRepository.countByWorkout_IdAndExercise_Id(Long workoutId, Long exerciseId)`.
Sets are only ever appended within an exercise, never inserted mid-sequence,
so a simple count is sufficient - no renumbering logic needed.

## Styling

`workout.html` and `workoutDay.html` reuse the existing inline `<style>`
conventions verbatim (`.status-card`, `.badge`, `.calendar` table,
`edit`/`delete`/`save` buttons) rather than introducing anything new,
keeping the zero-build, zero-framework approach consistent across all four
feature areas (exercises, workout types, measurements, workouts).
