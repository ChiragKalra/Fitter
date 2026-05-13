# Implementation Plan

## Overview

This task list implements the five-defect dashboard widget layout bugfix following the exploratory bugfix workflow: write tests before the fix to understand the bugs, preserve existing behavior, apply the fix, then validate. The implementation spans `DashboardSection.kt` and `DashboardFragment.kt`.

## Notes

- Tasks 1 and 2 are property-based tests that must be written and run on the **unfixed** code before any implementation begins. Task 1 is expected to fail (confirming the bugs exist); task 2 is expected to pass (establishing the preservation baseline).
- Tasks 3–11 are the implementation steps ordered by dependency: `DashboardSection.kt` first (task 3), then screen-level state (tasks 4–6), then card-local state (task 7), then resize persistence (task 8), then reorder (task 9), then UI cleanup (tasks 10–11).
- Tasks 12 and 13 add automated test coverage after the fix. Task 14 is the final checkpoint.
- All instrumented tests (task 13) require a connected Android device via ADB. Run with `./gradlew connectedDebugAndroidTest`.
- JVM unit tests (task 12) run on the host JVM. Run with `./gradlew test`.
- `SnapshotStateList.move` is the Compose extension from `androidx.compose.runtime.snapshots`; it performs a single atomic mutation tracked at the element level.
- The `dashboard_editing_label` string resource must be added to `app/src/main/res/values/strings.xml` as part of task 11.

## Task Dependency Graph

```json
{
  "waves": [
    { "wave": 1, "tasks": ["1", "2"] },
    { "wave": 2, "tasks": ["3"] },
    { "wave": 3, "tasks": ["4", "5", "6"] },
    { "wave": 4, "tasks": ["7", "8", "9", "10", "11"] },
    { "wave": 5, "tasks": ["12", "13"] },
    { "wave": 6, "tasks": ["14"] }
  ]
}
```

## Tasks

- [x] 1. Write bug condition exploration tests (BEFORE implementing any fix)
  - **Property 1: Bug Condition** - Five Dashboard Defects
  - **CRITICAL**: These tests MUST FAIL on unfixed code — failure confirms the bugs exist
  - **DO NOT attempt to fix the tests or the code when they fail**
  - **NOTE**: These tests encode the expected behavior — they will validate the fix when they pass after implementation
  - **GOAL**: Surface counterexamples that demonstrate each of the five defects
  - **Scoped PBT Approach**: Each property is scoped to the concrete failing case for reproducibility
  - Create `DashboardBugConditionTest` in `app/src/test/java/com/bruhascended/fitapp/ui/dashboard/`
  - Test 1.1 — Default width: create `DashboardUiConfig` with empty `widthFractions`; assert `widthFor(SUMMARY_RING) == 0.5f` (from Bug Condition 2.3 in design). On unfixed code returns `1.0f` (`MAX_CARD_WIDTH_FRACTION`).
  - Test 1.2 — Reorder commit count: simulate 5 `onMove` calls followed by one `onDragEnd`; assert `saveDashboardLayout` called exactly once (from Bug Condition 2.4 in design). On unfixed code it is called on every `onMove`.
  - Test 1.3 — Resize persistence: simulate resize drag-end then immediately simulate Back press; assert `saveDashboardCardShape` was called with the new dimensions before `selectedSection` was cleared (from Bug Condition 2.5 in design). On unfixed code the shape is lost.
  - Test 1.4 — Edit-mode UI: long-press a card; assert no drawer icon or per-card remove/cross icon is present in the composition tree (from Bug Condition 2.2 in design). On unfixed code these icons are present.
  - Run all tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests FAIL (this is correct — it proves the bugs exist)
  - Document counterexamples found (e.g., `widthFor(SUMMARY_RING)` returns `1.0f`; `saveDashboardLayout` called 5 times; shape lost on Back)
  - Mark task complete when tests are written, run, and failures are documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Write preservation property tests (BEFORE implementing any fix)
  - **Property 2: Preservation** - Non-Resize/Reorder Behavior Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Create `DashboardPreservationTest` in `app/src/test/java/com/bruhascended/fitapp/ui/dashboard/`
  - Observe on UNFIXED code: tapping ACTIVE_ENERGY in normal mode launches `DashboardTrendActivity`
  - Observe on UNFIXED code: tapping the settings icon launches `SettingsActivity`
  - Observe on UNFIXED code: pressing Back in edit mode clears `selectedResizeSection` without navigating away
  - Observe on UNFIXED code: greeting card always spans full grid width regardless of edit mode
  - Observe on UNFIXED code: saving a custom order and reloading restores the same order
  - Write property-based test 2.1 — `reorderVisibleInFullOrder`: for all valid `(order, hiddenIds, fromVisible, toVisible)` tuples, the result contains the same elements as `order` and the visible subsequence reflects the move
  - Write property-based test 2.2 — `gridSpanForWidth` / `widthForGridSpan` round-trip: for all `span` in `1..gridColumns`, `gridSpanForWidth(widthForGridSpan(span, cols), cols) == span`
  - Write property-based test 2.3 — `heightForGridUnits` idempotency: for all already-snapped height values, applying `heightForGridUnits` again returns the same value
  - Write property-based test 2.4 — `DashboardUiConfig.clampWidthFraction` bounds: for all Float inputs, result is in `[MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION]`
  - Verify all tests PASS on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 3. Fix `DashboardUiConfig.widthFor()` default fallback in `DashboardSection.kt`

  - [x] 3.1 Add `DEFAULT_CARD_WIDTH_FRACTION = 0.5f` constant and update `widthFor()` fallback
    - In `DashboardUiConfig.Companion`, add `const val DEFAULT_CARD_WIDTH_FRACTION = 0.5f` alongside `MIN_CARD_WIDTH_FRACTION` and `MAX_CARD_WIDTH_FRACTION`
    - Change `widthFor()` body from `clampWidthFraction(widthFractions[section] ?: MAX_CARD_WIDTH_FRACTION)` to `clampWidthFraction(widthFractions[section] ?: DEFAULT_CARD_WIDTH_FRACTION)`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == FIRST_LOAD AND widthFor(section) == MAX_CARD_WIDTH_FRACTION` (Defect 2.3 in design)_
    - _Expected_Behavior: `widthFor(section)` returns `0.5f` when no saved width exists for the section_
    - _Preservation: `widthFor()` still returns the stored value when one exists; `clampWidthFraction` bounds are unchanged_
    - _Requirements: 1.3, 2.3_

- [x] 4. Replace screen-level plain Maps with `SnapshotStateMap` in `DashboardFragment.kt`

  - [x] 4.1 Replace `draftWidths` and `draftHeightScales` with `SnapshotStateMap`
    - Replace `var draftWidths by remember { mutableStateOf(dashConfig.widthFractions) }` with `val draftWidths: SnapshotStateMap<DashboardSection, Float> = remember { mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.widthFractions) } }`
    - Apply the same change for `draftHeightScales`
    - Add imports: `androidx.compose.runtime.snapshots.SnapshotStateMap`, `androidx.compose.runtime.mutableStateMapOf`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == RESIZE_DRAG AND stateOwner(draftWidth) == SCREEN_LEVEL` (Defect 2.1 in design)_
    - _Expected_Behavior: `span` lambda reads `draftWidths[section]` from `SnapshotStateMap`; only the affected cell recomposes on write_
    - _Preservation: `draftWidths` values are initialized from `dashConfig.widthFractions` as before_
    - _Requirements: 1.1, 2.1_

  - [x] 4.2 Update the `LaunchedEffect` to mutate `draftWidths` and `draftHeightScales` in-place and guard against clobbering in-progress drags
    - Change key from `LaunchedEffect(dashConfig, selectedResizeSection)` to `LaunchedEffect(dashConfig)`
    - Guard the reset with `if (selectedSection == null)` to avoid clobbering in-progress drags
    - Use `draftWidths.clear(); draftWidths.putAll(dashConfig.widthFractions)` instead of reassignment
    - Use `draftHeightScales.clear(); draftHeightScales.putAll(dashConfig.heightScales)` instead of reassignment
    - Update `draftOrder` reset similarly: `draftOrder.clear(); draftOrder.addAll(dashConfig.order)`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == RESIZE_DRAG_END AND saveDashboardCardShape NOT called` (Defect 2.5 in design)_
    - _Expected_Behavior: `LaunchedEffect` only fires when `selectedSection == null`; in-progress drags are never clobbered_
    - _Preservation: Config changes still propagate to draft state when not editing_
    - _Requirements: 1.5, 2.1, 2.5_

- [x] 5. Replace screen-level plain `List` with `SnapshotStateList` for `draftOrder` in `DashboardFragment.kt`

  - [x] 5.1 Replace `draftOrder` with `SnapshotStateList`
    - Replace `var draftOrder by remember { mutableStateOf(dashConfig.order) }` with `val draftOrder: SnapshotStateList<DashboardSection> = remember { mutableStateListOf<DashboardSection>().also { it.addAll(dashConfig.order) } }`
    - Add imports: `androidx.compose.runtime.snapshots.SnapshotStateList`, `androidx.compose.runtime.mutableStateListOf`
    - The `LaunchedEffect` reset (from task 4.2) already handles `draftOrder` in-place mutation
    - _Bug_Condition: `isBugCondition(input)` where `input.type == REORDER_DRAG AND commitTarget == VIEW_MODEL AND commitTiming == ON_MOVE` (Defect 2.4 in design)_
    - _Expected_Behavior: `SnapshotStateList` mutation in `onMove` triggers recomposition only at the two affected indices_
    - _Preservation: `draftOrder` is still initialized from `dashConfig.order` and reset on config change_
    - _Requirements: 1.4, 2.4_

- [x] 6. Replace `selectedResizeSection` with `selectedSection` + `derivedStateOf { isEditMode }` in `DashboardFragment.kt`

  - [x] 6.1 Rename `selectedResizeSection` to `selectedSection` and add `isEditMode` derived state
    - Replace `var selectedResizeSection by remember { mutableStateOf<DashboardSection?>(null) }` with `var selectedSection by remember { mutableStateOf<DashboardSection?>(null) }`
    - Add `val isEditMode by remember { derivedStateOf { selectedSection != null } }` immediately after
    - Add import: `androidx.compose.runtime.derivedStateOf`
    - Update all call sites: `selectedResizeSection` → `selectedSection`
    - Update `BackHandler(enabled = selectedSection != null)` and its body to set `selectedSection = null`
    - Pass `isEditMode = isEditMode` to `DashboardTopBar`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == LONG_PRESS AND editModeUI contains DRAWER_ICONS OR PER_CARD_REMOVE_ICON` (Defect 2.2 in design)_
    - _Expected_Behavior: `isEditMode` is a stable derived boolean; no extra allocation; downstream composables read `isEditMode` for the boolean flag_
    - _Preservation: Back press still clears `selectedSection` and exits edit mode without navigating away_
    - _Requirements: 1.2, 2.2_

- [x] 7. Refactor `DashboardWidgetCard` to hold local draft state and remove `onShapeChange` callback

  - [x] 7.1 Change `DashboardWidgetCard` signature and add local `draftWidth`/`draftHeightScale` state
    - Replace `widthFraction: Float` and `heightScale: Float` parameters with `committedWidthFraction: Float` and `committedHeightScale: Float`
    - Remove `onShapeChange: (Float, Float) -> Unit` parameter from the signature
    - Add inside the composable: `var draftWidth by remember(committedWidthFraction) { mutableStateOf(committedWidthFraction) }` and `var draftHeightScale by remember(committedHeightScale) { mutableStateOf(committedHeightScale) }`
    - Use `draftWidth`/`draftHeightScale` for all visual sizing (card height, container width calculation)
    - Pass a local `onShapeChange = { w, h -> draftWidth = w; draftHeightScale = h }` lambda to each `DashboardResizeHandle` — this lambda is NOT propagated to the screen level
    - Update the call site in `LazyVerticalGrid.items` to pass `committedWidthFraction = draftWidths[section] ?: dashConfig.widthFor(section)` and `committedHeightScale = draftHeightScales[section] ?: dashConfig.heightScaleFor(section)`
    - The `span` lambda at the `LazyVerticalGrid` level continues to read `draftWidths[section]` (the `SnapshotStateMap`), which is only written in `onShapeChangeFinished` — NOT during drag
    - _Bug_Condition: `isBugCondition(input)` where `input.type == RESIZE_DRAG AND stateOwner(draftWidth) == SCREEN_LEVEL` (Defect 2.1 in design)_
    - _Expected_Behavior: per-frame drag mutations are confined to `DashboardWidgetCard` local `remember` scope; `LazyVerticalGrid` span lambda is NOT re-evaluated during drag_
    - _Preservation: card visual size still tracks drag in real time; committed size still drives span after drag-end_
    - _Requirements: 1.1, 2.1_

- [x] 8. Update `onShapeChangeFinished` to write snapped values to `SnapshotStateMap` and call `saveDashboardCardShape()`

  - [x] 8.1 Ensure `onShapeChangeFinished` in the screen-level lambda writes to `SnapshotStateMap` and persists immediately
    - In the `onShapeChangeFinished` lambda passed from the screen to `DashboardWidgetCard`, compute `snappedWidth = widthForGridSpan(gridSpanForWidth(width, gridSize.columns), gridSize.columns)` and `snappedHeight = heightForGridUnits(height, gridSize.heightUnits)`
    - Write `draftWidths[section] = snappedWidth` and `draftHeightScales[section] = snappedHeight` (in-place mutation of `SnapshotStateMap`)
    - Call `viewModel.saveDashboardCardShape(section, snappedWidth, snappedHeight)`
    - Remove any duplicate persistence call that was previously in the screen-level `onShapeChange` lambda
    - _Bug_Condition: `isBugCondition(input)` where `input.type == RESIZE_DRAG_END AND saveDashboardCardShape NOT called` (Defect 2.5 in design)_
    - _Expected_Behavior: `saveDashboardCardShape` is called inside `onDragEnd` before any edit-mode state changes; new shape is always persisted regardless of subsequent Back presses_
    - _Preservation: card width and height saved via resize handles continue to be persisted and restored on next launch_
    - _Requirements: 1.5, 2.5_

- [x] 9. Update `reorderState` `onMove` to use `SnapshotStateList.move()` and commit only in `onDragEnd`

  - [x] 9.1 Replace `moveDashboardSection` call in `onMove` with `draftOrder.move(from.index, to.index)`
    - Replace the `onMove` lambda body: remove the `fromSection`/`toSection` lookup and `moveDashboardSection` call; replace with `draftOrder.move(from.index, to.index)`
    - Ensure `viewModel.saveDashboardLayout(draftOrder.toList(), dashConfig.hiddenIds)` is called only in `onDragEnd`, not in `onMove`
    - Remove the `onLongPress` reset of `draftOrder` (handled by the `LaunchedEffect` in task 4.2)
    - The `moveDashboardSection` helper function can be retained for unit testing purposes
    - _Bug_Condition: `isBugCondition(input)` where `input.type == REORDER_DRAG AND commitTarget == VIEW_MODEL AND commitTiming == ON_MOVE` (Defect 2.4 in design)_
    - _Expected_Behavior: `viewModel.saveDashboardLayout` is called exactly once per drag gesture (in `onDragEnd`); `SnapshotStateList.move()` triggers recomposition only at the two affected indices_
    - _Preservation: drag-to-reorder still persists the final order to `PreferencesRepository` and restores on next launch_
    - _Requirements: 1.4, 2.4_

- [x] 10. Strip `DashboardWidgetCard` edit-mode UI to border + 4 resize handles only

  - [x] 10.1 Remove all drawer icon rows and per-card remove/cross icons from the `if (selected)` block
    - The `if (selected)` block MUST contain only: a `Box` with `border(BorderStroke(2.dp, MaterialTheme.colors.primary), shape)` and the four `DashboardResizeHandle` composables (left, right, top, bottom)
    - Remove any `IconButton`, drawer icon `Row`, or cross/remove icon composable from `DashboardWidgetCard`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == LONG_PRESS AND editModeUI contains DRAWER_ICONS OR PER_CARD_REMOVE_ICON` (Defect 2.2 in design)_
    - _Expected_Behavior: after long-press, the selected card shows exactly 4 resize handles and a border; no other overlays_
    - _Preservation: resize handles and border are still shown when `selected == true`; in normal mode no handles are visible_
    - _Requirements: 1.2, 2.2_

- [x] 11. Update `DashboardTopBar` to accept `isEditMode: Boolean` and show "Editing" label

  - [x] 11.1 Add `isEditMode: Boolean` parameter and conditional "Editing" label to `DashboardTopBar`
    - Add `isEditMode: Boolean` as the first parameter of `DashboardTopBar`
    - Inside the `Row`, add `if (isEditMode) { Text(text = stringResource(R.string.dashboard_editing_label), style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary, modifier = Modifier.weight(1f).padding(start = 16.dp)) }` before the settings `IconButton`
    - Add `<string name="dashboard_editing_label">Editing</string>` to `app/src/main/res/values/strings.xml`
    - Update the `DashboardTopBar(...)` call site to pass `isEditMode = isEditMode`
    - _Bug_Condition: `isBugCondition(input)` where `input.type == LONG_PRESS AND editModeUI contains DRAWER_ICONS OR PER_CARD_REMOVE_ICON` (Defect 2.2 in design)_
    - _Expected_Behavior: top bar shows a subtle "Editing" label when `isEditMode == true`; label is absent in normal mode_
    - _Preservation: settings icon button is still present and functional in both modes; `SettingsActivity` still launches on tap_
    - _Requirements: 1.2, 2.2, 3.6_

- [x] 12. Write JVM unit tests for pure layout utility functions
  - **Property 1: Expected Behavior** — re-run the bug condition exploration tests from task 1 after the fix; they must now PASS
  - File: `app/src/test/java/com/bruhascended/fitapp/ui/dashboard/DashboardLayoutUtilsTest.kt`
  - Test `DashboardUiConfig.widthFor()` default: `DashboardUiConfig(order, emptySet(), emptyMap(), emptyMap()).widthFor(SUMMARY_RING)` must equal `0.5f` (validates fix for Defect 2.3)
  - Test `DashboardUiConfig.widthFor()` stored value: config with `widthFractions = mapOf(SUMMARY_RING to 0.75f)` must return `0.75f` for `SUMMARY_RING`
  - Test `gridSpanForWidth` on 4-column grid: `gridSpanForWidth(0.25f, 4) == 1`, `gridSpanForWidth(0.5f, 4) == 2`, `gridSpanForWidth(0.75f, 4) == 3`, `gridSpanForWidth(1.0f, 4) == 4`
  - Test `widthForGridSpan` round-trip: for each `span` in `1..4`, `gridSpanForWidth(widthForGridSpan(span, 4), 4) == span`
  - Test `heightForGridUnits` snapping: `heightForGridUnits(1.0f, 4) == 1.0f`, `heightForGridUnits(1.1f, 4) == 1.0f`, `heightForGridUnits(1.3f, 4) == 1.25f`
  - Test `reorderVisibleInFullOrder` identity: `fromVisible == toVisible` returns the same list unchanged
  - Test `reorderVisibleInFullOrder` with hidden sections: moving a visible section skips over hidden ones correctly
  - Test `reorderVisibleInFullOrder` out-of-bounds: indices outside `globals.indices` return the original list unchanged
  - Run with: `./gradlew test`
  - **EXPECTED OUTCOME**: All tests PASS after the fix is applied
  - _Requirements: 2.3, 2.4_

- [x] 13. Write Compose UI instrumented tests on the connected device
  - **Property 2: Preservation** — re-run the preservation property tests from task 2 after the fix; they must still PASS
  - File: `app/src/androidTest/java/com/bruhascended/fitapp/ui/dashboard/DashboardWidgetLayoutTest.kt`
  - Use `@get:Rule val composeTestRule = createComposeRule()`
  - **Test 13.1 — Default width**: set up a `DashboardUiConfig` with empty `widthFractions`; render a minimal `DashboardWidgetCard` with `committedWidthFraction = DashboardUiConfig.Default.widthFor(DashboardSection.SUMMARY_RING)`; assert the measured width is approximately `0.5f * containerWidth` (validates fix for Defect 2.3)
  - **Test 13.2 — Edit-mode entry**: render the dashboard screen composable; perform `longClick` on a card; assert `onNodeWithContentDescription("Resize left")` exists; assert no node with `contentDescription` matching `"Remove"` or `"Drawer"` exists (validates fix for Defect 2.2)
  - **Test 13.3 — Edit-mode exit via Back**: enter edit mode via long-press; press the system Back button; assert resize handle nodes no longer exist (validates Requirement 3.5)
  - **Test 13.4 — Resize persistence**: render `DashboardWidgetCard` with a mock `onShapeChangeFinished` callback; simulate a drag gesture on the right resize handle using `performTouchInput { swipeRight() }`; assert `onShapeChangeFinished` was called with snapped width and height values (validates fix for Defect 2.5)
  - **Test 13.5 — Reorder commit count**: render the dashboard with a mock `saveDashboardLayout`; simulate 3 `onMove` events via the reorder library's drag gesture; simulate `onDragEnd`; assert `saveDashboardLayout` was called exactly once (validates fix for Defect 2.4)
  - Run with: `./gradlew connectedDebugAndroidTest`
  - **EXPECTED OUTCOME**: All tests PASS after the fix is applied
  - _Requirements: 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 14. Checkpoint — Ensure all tests pass
  - Run JVM unit tests: `./gradlew test`
  - Run instrumented UI tests on the connected Android device: `./gradlew connectedDebugAndroidTest`
  - Confirm all bug condition exploration tests (task 1 / Property 1) now PASS — each defect is fixed
  - Confirm all preservation property tests (task 2 / Property 2) still PASS — no regressions
  - Confirm all JVM unit tests (task 12) PASS
  - Confirm all Compose UI instrumented tests (task 13) PASS
  - If any test fails, diagnose the root cause before patching
  - Ensure all tests pass; ask the user if questions arise
