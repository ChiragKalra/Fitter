# Bugfix Requirements Document

## Introduction

The home page dashboard in Fitter uses a `LazyVerticalGrid` with Jetpack Compose to display
fitness metric cards. The current implementation has three distinct defects:

1. **Lag during interactions** — card resize and drag-to-reorder operations are noticeably janky
   because state mutations (draft widths, draft heights, draft order) are hoisted too high in the
   composition tree, causing the entire grid to recompose on every pointer event.

2. **Cards are always full-width** — the default `widthFraction` is `1.0` (full grid span), so
   cards never appear side-by-side out of the box. The grid infrastructure exists but the UX
   never surfaces narrower cards to the user.

3. **Incorrect edit-mode UI** — when a card is long-pressed, the current code shows resize handles
   but also retains a drawer-style icon row and a per-card remove/cross icon that do not match the
   Android home screen widget paradigm. On a real Android launcher, long-pressing a widget enters a
   global edit mode for the whole screen; there are no per-card remove icons visible during normal
   use, and no drawer icons overlaid on the card during resize.

The goal of this fix is to rebuild the dashboard interaction flow so it exactly mirrors Android
home screen widget behaviour: smooth resize via edge handles, a clean global edit mode entered by
long-pressing any card, no extraneous icons during normal or edit mode, and cards that default to
a sensible non-full-width layout.

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the user drags a resize handle on a dashboard card THEN the system recomposes the entire
`LazyVerticalGrid` on every pointer-move event, producing visible frame drops and lag.

1.2 WHEN the user long-presses a card to enter edit mode THEN the system displays drawer-style
icons and a per-card cross/remove icon overlaid on the card, which does not match Android home
screen widget behaviour.

1.3 WHEN the dashboard is first loaded with no saved layout THEN the system renders every card at
full screen width (widthFraction = 1.0), leaving no cards side-by-side and wasting screen space.

1.4 WHEN the user is in edit mode and drags a card to reorder it THEN the system applies the
reorder to the full `draftOrder` list on every intermediate drag frame, causing unnecessary
recompositions across all visible cards.

1.5 WHEN the user exits edit mode by pressing Back or tapping the selected card THEN the system
discards unsaved draft shape changes without persisting them, so a resize that was visually
completed is silently lost.

### Expected Behavior (Correct)

2.1 WHEN the user drags a resize handle on a dashboard card THEN the system SHALL update only the
state local to that card's composable (not the full grid), so the frame rate during resize SHALL
remain at or above 60 fps with no perceptible jank.

2.2 WHEN the user long-presses any dashboard card THEN the system SHALL enter a global edit mode
for the whole dashboard screen — showing resize handles on the selected card and a subtle
"editing" indicator — without displaying any drawer icons or per-card remove/cross icons.

2.3 WHEN the dashboard is first loaded with no saved layout THEN the system SHALL render cards
using a default layout where at least some cards occupy half the grid width (widthFraction ≈ 0.5),
so two cards appear side-by-side on a standard phone screen.

2.4 WHEN the user drags a card to reorder it in edit mode THEN the system SHALL batch the
intermediate reorder into a local draft that is committed to the ViewModel only when the drag
gesture ends, not on every drag frame.

2.5 WHEN the user finishes dragging a resize handle and releases the pointer THEN the system SHALL
persist the new card shape (width fraction and height scale) to the ViewModel immediately, so the
size is retained after exiting edit mode.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the user taps a card that has an associated trend metric (Active Energy, Calorie Balance,
Weight Delta, Weight Projection) THEN the system SHALL CONTINUE TO launch `DashboardTrendActivity`
for that metric.

3.2 WHEN the user saves a custom card order via drag-to-reorder THEN the system SHALL CONTINUE TO
persist the order to `PreferencesRepository` and restore it on next app launch.

3.3 WHEN the user saves a custom card width or height via resize handles THEN the system SHALL
CONTINUE TO persist the shape to `PreferencesRepository` and restore it on next app launch.

3.4 WHEN the dashboard is displayed THEN the system SHALL CONTINUE TO show the greeting card at
the top spanning the full grid width, unaffected by edit mode.

3.5 WHEN the user presses the system Back button while in edit mode THEN the system SHALL CONTINUE
TO exit edit mode and return the dashboard to its normal (non-editing) state without navigating
away from the dashboard screen.

3.6 WHEN the user opens Settings from the dashboard top bar THEN the system SHALL CONTINUE TO
launch `SettingsActivity`.

3.7 WHEN the dashboard is in normal (non-edit) mode THEN the system SHALL CONTINUE TO display all
visible cards with their persisted widths and heights, with no resize handles or edit-mode
indicators visible.
