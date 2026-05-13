# Dashboard Widget Layout Bugfix Design

## Overview

The dashboard currently suffers from five distinct defects: full-grid recomposition during resize
(lag), incorrect edit-mode UI (drawer icons and per-card remove icons), full-width default cards,
per-frame reorder commits, and silently-dropped resize persistence on edit-mode exit.

The fix strategy is:
1. Push draft resize state into each card's local composable scope so only that card recomposes.
2. Replace the per-card edit-mode concept with a single screen-level `isEditMode` flag.
3. Change the `widthFor()` fallback from `1.0` to `0.5`.
4. Use a local `SnapshotStateList` for draft order and commit only on drag-end.
5. Call `saveDashboardCardShape()` inside `onDragEnd` of the resize handle gesture, before clearing edit state.

---

## Glossary

- **Bug_Condition (C)**: The set of inputs / code paths that trigger one of the five defects.
- **Property (P)**: The desired correct behavior for each defect class.
- **Preservation**: Existing behaviors (tap-to-trend, settings launch, back-to-exit, persistence) that must remain unchanged.
- **`DashboardFragment`**: The `Fragment` that hosts the Compose UI for the home dashboard.
- **`DashboardViewModel`**: `AndroidViewModel` that owns `dashboardUiConfig: StateFlow<DashboardUiConfig>` and the `saveDashboard*` persistence methods.
- **`DashboardUiConfig`**: Immutable data class holding `order`, `hiddenIds`, `widthFractions`, `heightScales`, and `gridSize`.
- **`DashboardSection`**: Enum of all widget types; each has a stable `persistenceId` used as the `LazyVerticalGrid` item key.
- **`SnapshotStateMap`**: Compose-observable `MutableMap` backed by Compose snapshot system; mutations trigger only the composables that read the changed key.
- **`SnapshotStateList`**: Compose-observable `MutableList`; mutations trigger only composables that read the changed index.
- **`derivedStateOf`**: Compose API that memoizes a derived value and only invalidates downstream reads when the derived value actually changes.
- **`selectedSection`**: Screen-level state (`DashboardSection?`) identifying which card is in resize/edit mode; `null` means normal mode.
- **`isEditMode`**: Derived boolean — `selectedSection != null`.

---

## Bug Details

### Bug Condition

The five defects share a common root: state that should be local to a single card is hoisted to
the screen level, and state that should be committed lazily is committed eagerly.

**Formal Specification:**

```
FUNCTION isBugCondition(input)
  INPUT: input of type UserInteraction
  OUTPUT: boolean

  RETURN (
    -- Defect 2.1: resize drag mutates screen-level draftWidths/draftHeightScales
    (input.type == RESIZE_DRAG AND stateOwner(draftWidth) == SCREEN_LEVEL)
    OR
    -- Defect 2.2: edit mode shows drawer/remove icons
    (input.type == LONG_PRESS AND editModeUI contains DRAWER_ICONS OR PER_CARD_REMOVE_ICON)
    OR
    -- Defect 2.3: first load with no saved layout renders full-width cards
    (input.type == FIRST_LOAD AND widthFor(section) == MAX_CARD_WIDTH_FRACTION)
    OR
    -- Defect 2.4: reorder commits on every drag frame
    (input.type == REORDER_DRAG AND commitTarget == VIEW_MODEL AND commitTiming == ON_MOVE)
    OR
    -- Defect 2.5: resize shape not persisted on drag-end
    (input.type == RESIZE_DRAG_END AND saveDashboardCardShape NOT called)
  )
END FUNCTION
```

### Examples

- **Defect 2.1**: User drags the right resize handle of the SUMMARY_RING card. Every `onDrag`
  callback calls `onShapeChange`, which replaces `draftWidths` (a plain `Map`) at the screen
  level. Because `draftWidths` is read by the `span` lambda of every item in the grid, all 13
  visible cards recompose on every pointer-move frame → visible jank.

- **Defect 2.2**: User long-presses TODAY_STATS. The current code sets `selectedResizeSection =
  section` and shows resize handles, but the surrounding `DashboardWidgetCard` also renders a
  drawer icon row and a cross/remove icon that are not part of the Android widget paradigm.

- **Defect 2.3**: Fresh install, no saved layout. `widthFor(section)` returns
  `MAX_CARD_WIDTH_FRACTION` (1.0) for every section → all 13 cards stack vertically at full
  width; the 4-column grid is never used.

- **Defect 2.4**: User drags STEPS_WEEK over ENERGY_WEEK. `onMove` fires ~30 times per second;
  each call rebuilds `draftOrder` (a plain `List`) and triggers a full `items { }` block
  recomposition.

- **Defect 2.5**: User resizes NUTRIENTS card, releases pointer. `onShapeChangeFinished` is
  called, but if the user then presses Back, `selectedResizeSection` is cleared and the
  `LaunchedEffect(dashConfig, selectedResizeSection)` resets `draftWidths`/`draftHeightScales`
  back to the persisted values — the resize is lost because `saveDashboardCardShape` was not
  called inside `onDragEnd`.

---

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Tapping a card with an associated trend metric (ACTIVE_ENERGY, CALORIE_BALANCE, WEIGHT_DELTA,
  WEIGHT_PROJECTION) must continue to launch `DashboardTrendActivity`.
- Drag-to-reorder must continue to persist the new order to `PreferencesRepository` and restore
  it on next launch.
- Card width and height saved via resize handles must continue to be persisted and restored.
- The greeting card must continue to span the full grid width and be unaffected by edit mode.
- Pressing Back in edit mode must continue to exit edit mode without navigating away.
- Opening Settings from the top bar must continue to launch `SettingsActivity`.
- In normal mode, no resize handles or edit-mode indicators must be visible.

**Scope:**
All interactions that do NOT involve a resize drag or a reorder drag are completely unaffected by
this fix. This includes tap-to-trend, settings launch, data observation, and the greeting card.

---

## Hypothesized Root Cause

### Defect 2.1 — Lag during resize

`draftWidths` and `draftHeightScales` are `var` properties backed by plain Kotlin `Map` objects
held at the screen level inside `setContent { }`. The `span` lambda of `LazyVerticalGrid.items`
reads `draftWidths[section]` for every item. Because the entire map is replaced on each drag
frame (Kotlin immutable map copy), Compose sees the whole `draftWidths` state as changed and
invalidates every span lambda → full grid recomposition at pointer-move frequency.

### Defect 2.2 — Incorrect edit-mode UI

The current code does not have a dedicated edit-mode UI layer. The `DashboardWidgetCard` composable
receives a `selected` flag and renders resize handles, but the surrounding layout still includes
icon rows that were part of an earlier design iteration and were never removed.

### Defect 2.3 — Default full-width cards

`DashboardUiConfig.widthFor()` falls back to `MAX_CARD_WIDTH_FRACTION` (1.0) when no saved width
exists for a section. This means a fresh install always renders every card at full grid width.

### Defect 2.4 — Per-frame reorder commits

`onMove` in `rememberReorderableLazyGridState` is called on every drag frame. The current
implementation rebuilds `draftOrder` (a plain `List`) on each call. Because `draftOrder` is read
by the `items { }` block, every frame triggers a full items recomposition. Additionally, the
`onDragEnd` callback calls `viewModel.saveDashboardLayout`, but the intermediate `onMove` calls
also mutate `draftOrder` in a way that can cause the `LaunchedEffect` to reset it.

### Defect 2.5 — Resize not persisted on drag-end

`onShapeChangeFinished` is wired to `onDragEnd` of `detectDragGestures` inside
`DashboardResizeHandle`. However, the `LaunchedEffect(dashConfig, selectedResizeSection)` block
resets `draftWidths` and `draftHeightScales` whenever `selectedResizeSection` becomes `null`.
If the user presses Back immediately after releasing the resize handle, the Back handler clears
`selectedResizeSection` before `saveDashboardCardShape` has had a chance to update `dashConfig`
via the `StateFlow`, so the reset fires with the old values.

---

## Component Architecture

```
DashboardFragment.setContent {
  FitAppTheme {
    ┌─────────────────────────────────────────────────────────────────┐
    │  Screen level (DashboardFragment.setContent scope)              │
    │                                                                 │
    │  val dashConfig: DashboardUiConfig   ← StateFlow (ViewModel)   │
    │  var selectedSection: DashboardSection?  ← mutableStateOf      │
    │  val isEditMode = selectedSection != null  ← derivedStateOf    │
    │                                                                 │
    │  val draftOrder: SnapshotStateList<DashboardSection>            │
    │    ← initialized from dashConfig.order                         │
    │    ← mutated in onMove (local only)                             │
    │    ← committed in onDragEnd → viewModel.saveDashboardLayout()   │
    │                                                                 │
    │  val draftWidths: SnapshotStateMap<DashboardSection, Float>     │
    │    ← initialized from dashConfig.widthFractions                 │
    │    ← NOT mutated during resize (stays at last-committed value)  │
    │    ← updated only when dashConfig changes (LaunchedEffect)      │
    │                                                                 │
    │  val draftHeightScales: SnapshotStateMap<DashboardSection,Float>│
    │    ← same lifecycle as draftWidths                              │
    │                                                                 │
    │  ┌──────────────────────────────────────────────────────────┐   │
    │  │  LazyVerticalGrid                                        │   │
    │  │  span lambda reads draftWidths[section]                  │   │
    │  │  (SnapshotStateMap → only affected cell recomposes)      │   │
    │  │                                                          │   │
    │  │  ┌────────────────────────────────────────────────────┐  │   │
    │  │  │  DashboardWidgetCard (per section)                 │  │   │
    │  │  │                                                    │  │   │
    │  │  │  val draftWidth: Float  ← remember/mutableStateOf │  │   │
    │  │  │  val draftHeightScale: Float  ← remember/mutable  │  │   │
    │  │  │    initialized from draftWidths[section]           │  │   │
    │  │  │    mutated on every drag frame (local only)        │  │   │
    │  │  │                                                    │  │   │
    │  │  │  onShapeChange → updates local draftWidth/Scale    │  │   │
    │  │  │  onShapeChangeFinished →                           │  │   │
    │  │  │    draftWidths[section] = snappedWidth  (map write)│  │   │
    │  │  │    draftHeightScales[section] = snappedHeight      │  │   │
    │  │  │    viewModel.saveDashboardCardShape(...)           │  │   │
    │  │  │                                                    │  │   │
    │  │  │  ┌──────────────────────────────────────────────┐  │  │   │
    │  │  │  │  DashboardResizeHandle (×4 when selected)    │  │  │   │
    │  │  │  │  detectDragGestures:                         │  │  │   │
    │  │  │  │    onDrag → onShapeChange(w, h)              │  │  │   │
    │  │  │  │    onDragEnd → onShapeChangeFinished(w, h)   │  │  │   │
    │  │  │  └──────────────────────────────────────────────┘  │  │   │
    │  │  └────────────────────────────────────────────────────┘  │   │
    │  └──────────────────────────────────────────────────────────┘   │
    └─────────────────────────────────────────────────────────────────┘
  }
}
```

---

## State Ownership Table

| State | Owner | Type | Lifecycle |
|---|---|---|---|
| `dashConfig` | `DashboardViewModel` | `StateFlow<DashboardUiConfig>` | Survives config change; source of truth |
| `selectedSection` | Screen (`setContent`) | `mutableStateOf<DashboardSection?>` | Cleared on Back or tap-deselect |
| `isEditMode` | Screen (`setContent`) | `derivedStateOf { selectedSection != null }` | Derived; no extra allocation |
| `draftOrder` | Screen (`setContent`) | `SnapshotStateList<DashboardSection>` | Reset from `dashConfig.order` when not dragging |
| `draftWidths` | Screen (`setContent`) | `SnapshotStateMap<DashboardSection, Float>` | Updated only on `onShapeChangeFinished`; reset from `dashConfig` on config change |
| `draftHeightScales` | Screen (`setContent`) | `SnapshotStateMap<DashboardSection, Float>` | Same as `draftWidths` |
| `draftWidth` (per card) | `DashboardWidgetCard` | `remember { mutableStateOf(…) }` | Local; drives visual during drag; NOT read by span lambda |
| `draftHeightScale` (per card) | `DashboardWidgetCard` | `remember { mutableStateOf(…) }` | Local; drives visual during drag |
| `dragStartWidth` | `DashboardResizeHandle` | `remember { mutableStateOf(…) }` | Captured at `onDragStart`; unchanged during drag |
| `dragDistanceXPx` / `YPx` | `DashboardResizeHandle` | `remember { mutableStateOf(…) }` | Accumulated during drag; reset at `onDragStart` |

---

## Edit-Mode State Machine

```
         ┌──────────────────────────────────────────────────────┐
         │                    NORMAL MODE                       │
         │  selectedSection = null                              │
         │  No resize handles visible                           │
         │  No edit-mode indicator                              │
         │  Tap on tappable card → DashboardTrendActivity       │
         └──────────────────────────────────────────────────────┘
                    │                          ▲
          Long-press any card         Back pressed  OR
          selectedSection = card      Tap selected card
                    │                 selectedSection = null
                    ▼                          │
         ┌──────────────────────────────────────────────────────┐
         │                    EDIT MODE                         │
         │  selectedSection = <card>                            │
         │  Selected card shows 4 resize handles + border       │
         │  Subtle "editing" indicator in top bar               │
         │  No drawer icons, no per-card remove icons           │
         │  Tap on non-selected card → select that card         │
         │  (does NOT navigate to trend)                        │
         └──────────────────────────────────────────────────────┘
                    │
          Drag resize handle
                    │
                    ▼
         ┌──────────────────────────────────────────────────────┐
         │                  RESIZING (sub-state of EDIT)        │
         │  Local draftWidth / draftHeightScale update per frame│
         │  Only the card being resized recomposes              │
         │  span lambda NOT re-evaluated (reads draftWidths map,│
         │  which is NOT mutated during drag)                   │
         └──────────────────────────────────────────────────────┘
                    │
          Pointer released (onDragEnd)
                    │
                    ▼
         ┌──────────────────────────────────────────────────────┐
         │  onShapeChangeFinished fires:                        │
         │  1. Snap width/height to grid                        │
         │  2. Write snapped values into draftWidths/Heights    │
         │     (SnapshotStateMap — triggers span recompose for  │
         │      this card only)                                 │
         │  3. viewModel.saveDashboardCardShape(...)            │
         │  Returns to EDIT MODE (selectedSection unchanged)    │
         └──────────────────────────────────────────────────────┘
```

---

## Sequence Diagrams

### Resize Flow (Fixed)

```
User                DashboardResizeHandle    DashboardWidgetCard    LazyVerticalGrid    ViewModel
 │                         │                        │                      │               │
 │── drag start ──────────►│                        │                      │               │
 │                         │ onDragStart:            │                      │               │
 │                         │  dragStartWidth=current │                      │               │
 │                         │  dragDistanceXPx=0      │                      │               │
 │                         │                        │                      │               │
 │── drag move (×N) ──────►│                        │                      │               │
 │                         │ compute nextWidth       │                      │               │
 │                         │ onShapeChange(w,h) ────►│                      │               │
 │                         │                        │ draftWidth = w        │               │
 │                         │                        │ draftHeightScale = h  │               │
 │                         │                        │ (local recompose only)│               │
 │                         │                        │ span lambda NOT called│               │
 │                         │                        │                      │               │
 │── pointer up ──────────►│                        │                      │               │
 │                         │ onDragEnd:              │                      │               │
 │                         │ onShapeChangeFinished──►│                      │               │
 │                         │                        │ snap(w) → snappedW    │               │
 │                         │                        │ snap(h) → snappedH    │               │
 │                         │                        │ draftWidths[s]=snappedW──────────────►│
 │                         │                        │ (SnapshotStateMap write)              │
 │                         │                        │ span lambda re-eval  │               │
 │                         │                        │ for THIS card only   │               │
 │                         │                        │ saveDashboardCardShape────────────────►│
 │                         │                        │                      │  persist to   │
 │                         │                        │                      │  PrefsRepo    │
```

### Reorder Flow (Fixed)

```
User              reorderState (library)    draftOrder (SnapshotStateList)    ViewModel
 │                       │                            │                           │
 │── drag start ────────►│                            │                           │
 │                       │                            │                           │
 │── drag move (×N) ────►│ onMove(from, to)           │                           │
 │                       │ draftOrder.move(from, to) ►│                           │
 │                       │                            │ SnapshotStateList mutation│
 │                       │                            │ only items at from/to     │
 │                       │                            │ indices recompose         │
 │                       │                            │                           │
 │── pointer up ────────►│ onDragEnd                  │                           │
 │                       │ viewModel.saveDashboardLayout(draftOrder.toList(), …) ►│
 │                       │                            │                  persist  │
 │                       │                            │                  to Prefs │
```

---

## Correctness Properties

Property 1: Bug Condition — Resize Causes Only Local Recomposition

_For any_ drag event on a resize handle where `isBugCondition` holds (screen-level draft state is
mutated on every frame), the fixed implementation SHALL confine all per-frame state mutations to
the `DashboardWidgetCard` composable's local `remember` scope, so the `LazyVerticalGrid` span
lambda is NOT re-evaluated during the drag, and the frame rate SHALL remain at or above 60 fps.

**Validates: Requirements 2.1**

Property 2: Bug Condition — Edit Mode Shows Only Resize Handles

_For any_ long-press event on a dashboard card, the fixed implementation SHALL enter a global
edit mode (`selectedSection != null`) that shows exactly four resize handles and a border on the
selected card, with NO drawer icons and NO per-card remove/cross icons anywhere on the screen.

**Validates: Requirements 2.2**

Property 3: Bug Condition — Default Width Is 0.5

_For any_ first-load state where no saved width exists for a section, `DashboardUiConfig.widthFor()`
SHALL return `0.5f`, so cards appear side-by-side on a standard 4-column grid.

**Validates: Requirements 2.3**

Property 4: Bug Condition — Reorder Commits Only on Drag-End

_For any_ reorder drag sequence of N intermediate `onMove` frames followed by one `onDragEnd`,
the fixed implementation SHALL call `viewModel.saveDashboardLayout()` exactly once (in
`onDragEnd`), not N times.

**Validates: Requirements 2.4**

Property 5: Bug Condition — Resize Shape Persisted on Drag-End

_For any_ resize drag where the user releases the pointer, the fixed implementation SHALL call
`viewModel.saveDashboardCardShape()` inside `onDragEnd` before any edit-mode state is cleared,
so the new shape is always persisted regardless of subsequent Back presses.

**Validates: Requirements 2.5**

Property 6: Preservation — Non-Resize/Reorder Behavior Unchanged

_For any_ input that does NOT involve a resize drag or a reorder drag (tap-to-trend, settings
launch, Back press, greeting card display, data observation), the fixed code SHALL produce
exactly the same behavior as the original code.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

---

## Fix Implementation

### Changes Required

#### File: `DashboardSection.kt`

**Function**: `DashboardUiConfig.widthFor()`

**Change 1 — Default width fallback**

Replace the fallback constant from `MAX_CARD_WIDTH_FRACTION` (1.0) to `0.5f`:

```kotlin
// Before
fun widthFor(section: DashboardSection): Float =
    clampWidthFraction(widthFractions[section] ?: MAX_CARD_WIDTH_FRACTION)

// After
fun widthFor(section: DashboardSection): Float =
    clampWidthFraction(widthFractions[section] ?: DEFAULT_CARD_WIDTH_FRACTION)

// Add companion constant:
const val DEFAULT_CARD_WIDTH_FRACTION = 0.5f
```

---

#### File: `DashboardFragment.kt`

**Change 2 — Screen-level state: replace plain Maps with SnapshotStateMaps and SnapshotStateList**

```kotlin
// Before (screen level)
var draftWidths by remember { mutableStateOf(dashConfig.widthFractions) }
var draftHeightScales by remember { mutableStateOf(dashConfig.heightScales) }
var draftOrder by remember { mutableStateOf(dashConfig.order) }

// After (screen level)
val draftWidths: SnapshotStateMap<DashboardSection, Float> = remember {
    mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.widthFractions) }
}
val draftHeightScales: SnapshotStateMap<DashboardSection, Float> = remember {
    mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.heightScales) }
}
val draftOrder: SnapshotStateList<DashboardSection> = remember {
    mutableStateListOf<DashboardSection>().also { it.addAll(dashConfig.order) }
}
```

The `LaunchedEffect` that resets draft state on config change must be updated to mutate the maps
in-place (not replace them), so the `remember` references remain stable:

```kotlin
LaunchedEffect(dashConfig) {
    // Only reset when not actively editing to avoid clobbering in-progress drags
    if (selectedSection == null) {
        draftWidths.clear()
        draftWidths.putAll(dashConfig.widthFractions)
        draftHeightScales.clear()
        draftHeightScales.putAll(dashConfig.heightScales)
        draftOrder.clear()
        draftOrder.addAll(dashConfig.order)
    }
}
```

**Change 3 — Edit-mode state: single screen-level flag**

```kotlin
// Before
var selectedResizeSection by remember { mutableStateOf<DashboardSection?>(null) }

// After
var selectedSection by remember { mutableStateOf<DashboardSection?>(null) }
val isEditMode by remember { derivedStateOf { selectedSection != null } }
```

Remove all per-card drawer icons and per-card remove/cross icons from `DashboardWidgetCard`.
The composable receives only `isSelected: Boolean` (true when `selectedSection == section`).

**Change 4 — Reorder: commit only on drag-end**

```kotlin
val reorderState = rememberReorderableLazyGridState(
    gridState = lazyGridState,
    onMove = { from, to ->
        // Mutate SnapshotStateList in-place — only from/to indices recompose
        draftOrder.move(from.index, to.index)
    },
    onDragEnd = { _, _ ->
        viewModel.saveDashboardLayout(draftOrder.toList(), dashConfig.hiddenIds)
    },
)
```

`SnapshotStateList.move(fromIndex, toIndex)` is the Compose extension from
`androidx.compose.runtime.snapshots`. It performs a single atomic mutation that Compose tracks
at the element level, so only the two affected items recompose.

**Change 5 — Resize: push draft state into card local scope**

`DashboardWidgetCard` gains two local state variables:

```kotlin
@Composable
private fun DashboardWidgetCard(
    section: DashboardSection,
    isSelected: Boolean,
    committedWidthFraction: Float,   // from draftWidths[section] — stable between drags
    committedHeightScale: Float,     // from draftHeightScales[section] — stable between drags
    onShapeChangeFinished: (Float, Float) -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    reorderModifier: Modifier,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Local draft — only this composable reads these during drag
    var draftWidth by remember(committedWidthFraction) { mutableStateOf(committedWidthFraction) }
    var draftHeightScale by remember(committedHeightScale) { mutableStateOf(committedHeightScale) }

    // ... render using draftWidth / draftHeightScale for visual size
    // span lambda at grid level reads committedWidthFraction (stable during drag)
}
```

The `span` lambda at the `LazyVerticalGrid` level reads `draftWidths[section]` (the
`SnapshotStateMap`), which is only written in `onShapeChangeFinished` (drag-end), not during
drag. This means the span lambda is never re-evaluated during a drag.

**Change 6 — Resize persistence: call saveDashboardCardShape in onDragEnd**

Inside `DashboardResizeHandle.detectDragGestures`:

```kotlin
onDragEnd = {
    val snappedWidth = widthForGridSpan(gridSpanForWidth(lastWidth, gridSize.columns), gridSize.columns)
    val snappedHeight = heightForGridUnits(lastHeightScale, gridSize.heightUnits)
    // 1. Update SnapshotStateMap (triggers span recompose for this card only)
    draftWidths[section] = snappedWidth
    draftHeightScales[section] = snappedHeight
    // 2. Persist immediately — before any edit-mode state changes
    viewModel.saveDashboardCardShape(section, snappedWidth, snappedHeight)
    // onShapeChangeFinished is no longer needed as a separate callback
},
```

**Change 7 — Edit-mode UI: remove drawer icons and per-card remove icons**

In `DashboardWidgetCard`, the `if (selected)` block must contain ONLY:
- A `Box` with a `border` stroke (the selection indicator).
- Four `DashboardResizeHandle` composables (left, right, top, bottom).

No drawer icon rows. No cross/remove icons. No other overlays.

The top bar `DashboardTopBar` gains a subtle "Editing" text label that is visible only when
`isEditMode == true`:

```kotlin
@Composable
private fun DashboardTopBar(
    isEditMode: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(...) {
        if (isEditMode) {
            Text(
                text = stringResource(R.string.dashboard_editing_label),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.weight(1f).padding(start = 16.dp),
            )
        }
        IconButton(onClick = onOpenSettings) { /* settings icon */ }
    }
}
```

---

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate
each defect on the unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate each defect BEFORE implementing the fix.
Confirm or refute the root cause analysis.

**Test Plan**: Write Compose UI tests and unit tests that exercise each defect path on the
unfixed code. Observe failures to confirm root cause.

**Test Cases**:
1. **Recomposition count test** (Defect 2.1): Instrument the `span` lambda with a recomposition
   counter. Simulate 10 drag events on a resize handle. Assert counter == 1 (only on drag-end).
   On unfixed code this will fail with counter == 10.
2. **Edit-mode UI test** (Defect 2.2): Long-press a card. Assert no drawer icon or remove icon
   is present in the composition tree. On unfixed code this will fail.
3. **Default width test** (Defect 2.3): Create a `DashboardUiConfig` with empty `widthFractions`.
   Assert `widthFor(SUMMARY_RING) == 0.5f`. On unfixed code this returns 1.0f.
4. **Reorder commit count test** (Defect 2.4): Simulate 5 `onMove` calls followed by `onDragEnd`.
   Assert `saveDashboardLayout` called exactly once. On unfixed code it may be called on each move.
5. **Resize persistence test** (Defect 2.5): Simulate resize drag-end, then immediately simulate
   Back press. Assert `saveDashboardCardShape` was called with the new dimensions. On unfixed code
   the shape is lost.

**Expected Counterexamples**:
- Span lambda recomposition count > 1 per drag sequence (Defect 2.1).
- Drawer/remove icons present in composition after long-press (Defect 2.2).
- `widthFor()` returns 1.0f for unsaved sections (Defect 2.3).
- `saveDashboardLayout` called more than once per drag (Defect 2.4).
- `saveDashboardCardShape` not called when Back is pressed after resize (Defect 2.5).

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed code produces the
expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := fixedDashboard(input)
  ASSERT expectedBehavior(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed code
produces the same result as the original code.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT originalDashboard(input) == fixedDashboard(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many random card configurations and interaction sequences automatically.
- It catches edge cases (e.g., single-card grid, all cards hidden, max height scale) that manual
  tests miss.
- It provides strong guarantees that tap-to-trend, settings launch, and persistence are unchanged.

**Test Plan**: Observe behavior on unfixed code for tap, settings, and Back interactions, then
write property-based tests capturing that behavior.

**Test Cases**:
1. **Tap-to-trend preservation**: For any tappable section (ACTIVE_ENERGY, CALORIE_BALANCE,
   WEIGHT_DELTA, WEIGHT_PROJECTION), tapping in normal mode must launch `DashboardTrendActivity`.
2. **Settings launch preservation**: Tapping the settings icon must always launch `SettingsActivity`.
3. **Back-in-edit-mode preservation**: Pressing Back in edit mode must clear `selectedSection`
   without navigating away.
4. **Greeting card preservation**: The greeting card must always span full grid width regardless
   of edit mode or card configuration.
5. **Persistence round-trip preservation**: Saving a custom order and reloading must restore the
   same order.

### Unit Tests

- `DashboardUiConfig.widthFor()` returns `0.5f` when `widthFractions` is empty.
- `DashboardUiConfig.widthFor()` returns the stored value when one exists.
- `gridSpanForWidth(0.5f, 4)` returns `2`.
- `widthForGridSpan(2, 4)` returns `0.5f`.
- `moveDashboardSection` correctly moves a section from index 0 to index 2.
- `SnapshotStateList.move` produces the correct order for all valid from/to pairs.

### Property-Based Tests

- For any `widthFraction` in `[MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION]`,
  `widthForGridSpan(gridSpanForWidth(w, cols), cols)` snaps to a valid grid-aligned value.
- For any sequence of `onMove` calls followed by `onDragEnd`, `saveDashboardLayout` is called
  exactly once with the final order.
- For any `DashboardUiConfig` with arbitrary `widthFractions`, all `widthFor()` results are in
  `[MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION]`.
- For any resize drag sequence, the `draftWidths` SnapshotStateMap contains the snapped value
  after `onDragEnd` and the pre-drag value during the drag.

### Integration Tests

- Full resize flow: long-press → drag handle → release → verify persisted shape matches snapped
  dimensions → press Back → verify shape is still persisted.
- Full reorder flow: long-press → drag card → release → verify persisted order matches final
  visual order.
- Edit-mode entry/exit: long-press → verify `isEditMode == true` and only resize handles visible
  → press Back → verify `isEditMode == false` and no handles visible.
- Default layout: fresh `DashboardUiConfig` with empty maps → verify at least one card has
  `widthFraction == 0.5f` and two cards appear side-by-side.
