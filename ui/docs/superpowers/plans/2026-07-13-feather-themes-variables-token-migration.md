# Feather themes/variables Token Migration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove all `@featherds/styles` usage from the UI by replacing every `--feather-*` token reference with an owned `--onms-*` token layer, then drop the Feather theme entirely (including `open-mixins`/`open-light`) and the `@featherds/styles` package.

**Architecture:** An owned token layer defines `--onms-*` CSS custom properties. Generic tokens that exactly match a PrimeVue token are declared as aliases (`--onms-primary: var(--p-primary-color)`) so they inherit PrimeVue's light/dark switching; OpenNMS-domain and non-matching tokens are copied literals under `.open-light`/`.open-dark`. An SCSS-var indirection file (`_onms-tokens.scss`) maps `$token → --onms-token` so consuming files keep `var($token)` and only change their import. A `color-mix()`-based `alpha()` replaces Feather's `utils.alpha`.

**Tech Stack:** Vue 3 + TypeScript + Vite, Dart Sass (`@use`/`@import`), PrimeVue 4 (`--p-*` tokens), pnpm.

## Global Constraints

- Colors must remain **pixel-identical** in both light and dark themes — verified against the Feather literal values in the token table below.
- Braces with line breaks for all control-flow blocks (ESLint enforced).
- Do NOT touch `src/styles/vars.scss` tokens (`$border-radius*`, breakpoints, `$col-limit`) — these are ours, not Feather.
- The theme class `open-light` / `open-dark` is on both `<html>` and `<body>`; exactly one is present at a time. PrimeVue `--p-*` switch via `.open-dark` (darkModeSelector).
- Every verification gate: `pnpm run build:all`, `pnpm exec vue-tsc --noEmit`, `pnpm run lint`, `pnpm run test` must pass.

---

## Token value table (source of truth)

Feather values extracted from `@featherds/styles/themes/open-light.css` / `open-dark.css`.

| token | light | dark | treatment |
|---|---|---|---|
| primary | #273180 | #00BFCB | **alias** `var(--p-primary-color)` |
| secondary-text-on-surface | rgba(10,12,27,0.7) | rgba(255,255,255,0.78) | **alias** `var(--p-text-muted-color)` |
| surface | #ffffff | #15182B | **alias** `var(--p-content-background)` |
| primary-text-on-color | rgb(255,255,255) | rgb(10,12,27) | literal (light/dark) |
| background | #f4f7fc | #0a0c1b | literal (light/dark) |
| border-on-surface | rgba(10,12,27,0.12) | rgba(255,255,255,0.24) | literal (light/dark) |
| clickable-normal | #0402a9 | #b3e7ff | literal (light/dark) |
| shade-3 | rgba(10,12,27,0.24) | rgba(255,255,255,0.24) | literal (light/dark) |
| success | #0b720c | #83ee7d | literal (light/dark) |
| error | #a5021f | #ffa3b5 | literal (light/dark) |
| warning | #fbe947 | #fbe947 | literal (light/dark) |
| major | #e35302 | #e35302 | literal (light/dark) |
| minor | #ffac26 | #ffac26 | literal (light/dark) |
| cleared | #757575 | #b7b7b7 | literal (light/dark) |
| indeterminate | #0092c7 | #20b9f0 | literal (light/dark) |
| surface-dark | #131736 | #131736 | literal (:root, theme-independent) |
| state-text-color-on-surface-dark | rgba(255,255,255,0.78) | rgba(255,255,255,0.78) | literal (:root) |
| font-family | OpenSans, Helvetica, Arial, sans-serif | (same) | literal (:root) |
| header-font-family | Inter, Helvetica, Arial, sans-serif | (same) | literal (:root) |
| font-semibold | 600 | (same) | literal (:root) |
| body-small-line-height | 1.5rem | (same) | literal (:root) |
| body-small-letter-spacing | 0.01786em | (same) | literal (:root) |
| spacing-xxs | 0.25rem | (same) | literal (:root) |
| spacing-m | 1rem | (same) | literal (:root) |
| spacing-xl | 1.5rem | (same) | literal (:root) |
| zindex-dropdown | 1000 | (same) | literal (:root) |
| zindex-fixed | 1030 | (same) | literal (:root) |

`primary-text-on-surface` is intentionally omitted (unused in src).

---

## File structure

- **Create** `src/styles/_onms-tokens.scss` — `$token → --onms-token` SCSS-var indirection (usable via `@use` and `@import`).
- **Create** `src/styles/_onms-theme.scss` — defines the `--onms-*` custom properties (aliases on `:root`; literals on `:root` / `.open-light` / `.open-dark`).
- **Create** `src/styles/_onms-color-utils.scss` — `alpha($cssVarName, $amount)` via `color-mix`.
- **Modify** `src/styles/themes.scss` — import the onms theme; drop Feather `open-mixins`/`variables`; body background → `--onms-background`.
- **Modify** `src/styles/_severities.scss` — use onms-tokens + onms-color-utils instead of Feather variables/utils.
- **Modify** `src/styles/opennms-feather-styles.scss` — swap the Feather variables `@use`.
- **Modify** `src/styles/_onms-typography.scss` — `var(--feather-*)` → `var(--onms-*)`.
- **Modify** `src/main/App.vue` — drop Feather `open-mixins`/`variables` imports; `var($clickable-normal)` keeps working via onms-tokens import.
- **Modify** the ~30 `@featherds/styles/themes/variables` consumer files (import swap only).
- **Modify** the raw-`var(--feather-*)` consumer files (literal swap).
- **Modify** `package.json` / lockfile — remove `@featherds/styles`.

---

## Task 1: Create the owned token layer (3 new files)

**Files:**
- Create: `src/styles/_onms-tokens.scss`
- Create: `src/styles/_onms-theme.scss`
- Create: `src/styles/_onms-color-utils.scss`

**Interfaces (Produces):**
- `_onms-tokens.scss` exports SCSS vars `$primary, $secondary-text-on-surface, $surface, $primary-text-on-color, $background, $border-on-surface, $clickable-normal, $shade-3, $success, $error, $warning, $major, $minor, $cleared, $indeterminate, $surface-dark, $state-text-color-on-surface-dark, $font-family, $header-font-family, $font-semibold, $body-small-line-height, $body-small-letter-spacing, $spacing-xxs, $spacing-m, $spacing-xl, $zindex-dropdown, $zindex-fixed` — each holding the string `--onms-<name>`.
- `_onms-color-utils.scss` exports function `alpha($cssVarName, $amount)` returning a `color-mix()` expression.

- [ ] **Step 1: Write `_onms-tokens.scss`** (each file gets the standard AGPL license header used across `src/styles/*.scss`)

```scss
// (AGPL license header — copy from src/styles/_severities.scss)

// SCSS-var -> CSS-custom-property-name indirection for the owned OpenNMS token
// layer (replaces @featherds/styles/themes/variables). Usage sites keep
// `var($token)`, which resolves to `var(--onms-token)`. The `--onms-*` values
// are defined in _onms-theme.scss. Usable via both `@use ... as variables` and
// `@import`.
@function prefix($name) {
  @return --onms-#{$name};
}

$primary: prefix(primary);
$secondary-text-on-surface: prefix(secondary-text-on-surface);
$surface: prefix(surface);
$surface-dark: prefix(surface-dark);
$primary-text-on-color: prefix(primary-text-on-color);
$background: prefix(background);
$border-on-surface: prefix(border-on-surface);
$clickable-normal: prefix(clickable-normal);
$shade-3: prefix(shade-3);
$state-text-color-on-surface-dark: prefix(state-text-color-on-surface-dark);

// status / severity
$success: prefix(success);
$error: prefix(error);
$warning: prefix(warning);
$major: prefix(major);
$minor: prefix(minor);
$cleared: prefix(cleared);
$indeterminate: prefix(indeterminate);

// typography
$font-family: prefix(font-family);
$header-font-family: prefix(header-font-family);
$font-semibold: prefix(font-semibold);
$body-small-line-height: prefix(body-small-line-height);
$body-small-letter-spacing: prefix(body-small-letter-spacing);

// spacing / layout
$spacing-xxs: prefix(spacing-xxs);
$spacing-m: prefix(spacing-m);
$spacing-xl: prefix(spacing-xl);
$zindex-dropdown: prefix(zindex-dropdown);
$zindex-fixed: prefix(zindex-fixed);
```

- [ ] **Step 2: Write `_onms-theme.scss`**

```scss
// (AGPL license header)

// Owned OpenNMS token definitions (replaces the FeatherDS theme). Generic
// tokens that exactly match a PrimeVue token are aliased so they inherit
// PrimeVue light/dark switching; OpenNMS-domain and non-matching tokens are
// copied literals (values verbatim from FeatherDS open-light/open-dark).
:root {
  // aliases (verified equal to the FeatherDS values in both themes)
  --onms-primary: var(--p-primary-color);
  --onms-secondary-text-on-surface: var(--p-text-muted-color);
  --onms-surface: var(--p-content-background);

  // theme-independent literals
  --onms-surface-dark: #131736;
  --onms-state-text-color-on-surface-dark: rgba(255, 255, 255, 0.78);
  --onms-font-family: OpenSans, Helvetica, Arial, sans-serif;
  --onms-header-font-family: Inter, Helvetica, Arial, sans-serif;
  --onms-font-semibold: 600;
  --onms-body-small-line-height: 1.5rem;
  --onms-body-small-letter-spacing: 0.01786em;
  --onms-spacing-xxs: 0.25rem;
  --onms-spacing-m: 1rem;
  --onms-spacing-xl: 1.5rem;
  --onms-zindex-dropdown: 1000;
  --onms-zindex-fixed: 1030;
}

.open-light {
  --onms-primary-text-on-color: rgb(255, 255, 255);
  --onms-background: #f4f7fc;
  --onms-border-on-surface: rgba(10, 12, 27, 0.12);
  --onms-clickable-normal: #0402a9;
  --onms-shade-3: rgba(10, 12, 27, 0.24);
  --onms-success: #0b720c;
  --onms-error: #a5021f;
  --onms-warning: #fbe947;
  --onms-major: #e35302;
  --onms-minor: #ffac26;
  --onms-cleared: #757575;
  --onms-indeterminate: #0092c7;
}

.open-dark {
  --onms-primary-text-on-color: rgb(10, 12, 27);
  --onms-background: #0a0c1b;
  --onms-border-on-surface: rgba(255, 255, 255, 0.24);
  --onms-clickable-normal: #b3e7ff;
  --onms-shade-3: rgba(255, 255, 255, 0.24);
  --onms-success: #83ee7d;
  --onms-error: #ffa3b5;
  --onms-warning: #fbe947;
  --onms-major: #e35302;
  --onms-minor: #ffac26;
  --onms-cleared: #b7b7b7;
  --onms-indeterminate: #20b9f0;
}
```

- [ ] **Step 3: Write `_onms-color-utils.scss`**

```scss
// (AGPL license header)

@use "sass:math";

// Replaces @featherds/styles/themes/utils `alpha()`. Builds a translucent color
// from an --onms-* custom property. $amount is a 0-1 fraction (matching the old
// Feather call sites: $solid: 1, $opacity: 0.2). color-mix with `transparent`
// yields the same result as rgba(r, g, b, $amount) in the srgb space.
@function alpha($cssVarName, $amount) {
  @return color-mix(in srgb, var(#{$cssVarName}) percentage($amount), transparent);
}
```

- [ ] **Step 4: Verify the files compile** by importing them from a scratch build (covered when Task 3 wires them in). No standalone test.

- [ ] **Step 5: Commit**

```bash
git add src/styles/_onms-tokens.scss src/styles/_onms-theme.scss src/styles/_onms-color-utils.scss
git commit -m "NMS-19981: Add owned onms- token layer (tokens, theme, color-utils)"
```

---

## Task 2: Wire the theme in + migrate the global/style files

**Files:**
- Modify: `src/styles/themes.scss`
- Modify: `src/styles/_severities.scss`
- Modify: `src/styles/opennms-feather-styles.scss`
- Modify: `src/styles/_onms-typography.scss`
- Modify: `src/main/App.vue`

- [ ] **Step 1: `themes.scss`** — replace the Feather theme block:

Replace:
```scss
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/styles/themes/variables";

.open-light {
  @include open-light;
}

.open-dark {
  @include open-dark;
}

body {
  background: var($background);
}
```
with:
```scss
@import "@/styles/onms-theme";
@import "@/styles/onms-tokens";

body {
  background: var($background);
}
```
(`var($background)` now resolves to `var(--onms-background)`.)

- [ ] **Step 2: `_severities.scss`** — replace the imports and `utils.alpha`/`variables.$*`:

Replace `@use '@featherds/styles/themes/utils';` and `@use '@featherds/styles/themes/variables';` with:
```scss
@use '@/styles/onms-color-utils' as utils;
@use '@/styles/onms-tokens' as variables;
```
No other change — `utils.alpha(variables.$error, $solid)` etc. keep working (`$solid: 1`, `$opacity: 0.2`).

- [ ] **Step 3: `opennms-feather-styles.scss`** — replace `@use "@featherds/styles/themes/variables";` with `@use "@/styles/onms-tokens" as variables;` (verify the file references `variables.$*`; if it uses a different namespace, match it).

- [ ] **Step 4: `_onms-typography.scss`** — replace the raw Feather refs:
  - `var(--feather-font-family)` → `var(--onms-font-family)`
  - `var(--feather-header-font-family)` → `var(--onms-header-font-family)`
  - `var(--feather-body-small-line-height)` → `var(--onms-body-small-line-height)`
  - `var(--feather-body-small-letter-spacing)` → `var(--onms-body-small-letter-spacing)`

- [ ] **Step 5: `App.vue`** — remove `@import "@featherds/styles/themes/open-mixins";` and `@import "@featherds/styles/themes/variables";`; add `@import "@/styles/onms-tokens";` (so `var($clickable-normal)` resolves). Keep `@import '@/styles/onms-typography';`.

- [ ] **Step 6: Build + verify no regression**

Run: `pnpm run build:all` — Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/styles/themes.scss src/styles/_severities.scss src/styles/opennms-feather-styles.scss src/styles/_onms-typography.scss src/main/App.vue
git commit -m "NMS-19981: Wire onms- theme; migrate global style files off Feather tokens"
```

---

## Task 3: Migrate the `var($token)` consumer files (import swap)

**Files:** the ~30 files importing `@featherds/styles/themes/variables`. Two import styles exist:
- `@import "@featherds/styles/themes/variables";` → `@import "@/styles/onms-tokens";`
- `@use '@featherds/styles/themes/variables';` (default namespace `variables`) → `@use "@/styles/onms-tokens" as variables;`
- `@use "@featherds/styles/themes/variables" as variables;` → `@use "@/styles/onms-tokens" as variables;`

- [ ] **Step 1: Enumerate the exact list**

Run: `grep -rlE "@featherds/styles/themes/variables" src --include='*.vue' --include='*.scss' | grep -vE "themes.scss|_severities|opennms-feather-styles"`

- [ ] **Step 2: Apply the import swap** per the mapping above, preserving each file's quoting and namespace. (A `sed`-style codemod is acceptable, but must handle both `@import` and `@use ... as variables` forms; verify each file afterward.)

- [ ] **Step 3: Verify zero remaining themes/variables imports**

Run: `grep -rn "@featherds/styles/themes/variables" src` — Expected: no output.

- [ ] **Step 4: Build**

Run: `pnpm run build:all` — Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -A src
git commit -m "NMS-19981: Repoint var(\$token) consumers to onms-tokens"
```

---

## Task 4: Migrate the raw `var(--feather-*)` literal consumers

**Files:** the raw-literal files (excluding `_onms-typography.scss` + `opennms-preset.ts`, handled elsewhere):
`EventConfiguration/EventConfigUploadFilesTab.vue`, `Nodes/AssetFilterPanel.vue`, `Nodes/ExtendedSearchPanel.vue`, `SnmpDataCollection/SnmpDataCollectionSourceImport.vue`, `SnmpDataCollection/.../SnmpDataCollectionSourceProfilesDrawer.vue`, `SnmpDataCollection/.../ProfileRrdSettingsTab.vue`, `SnmpDataCollection/.../ProfileDetailsTab.vue`, `SnmpDataCollection/.../SnmpDataCollectionProfileDetails.vue`, `Layout/BreadCrumbs.vue`, `Menu/SideMenu.vue`, `Menu/Menubar.vue`, `containers/SnmpDataCollectionSourceDetail.vue`.

- [ ] **Step 1: Replace every `var(--feather-X)` → `var(--onms-X)`** (same token name), including fallback forms like `var(--feather-zindex-fixed, 1030)` → `var(--onms-zindex-fixed, 1030)`. Only the tokens in the value table exist as `--onms-*`; if a file references a `--feather-*` NOT in the table, STOP and add that token to `_onms-tokens.scss`/`_onms-theme.scss` first (with its verbatim light/dark value from open-light.css/open-dark.css).

- [ ] **Step 2: Verify**

Run: `grep -rn "var(--feather-" src | grep -vE "opennms-preset"` — Expected: no output.

- [ ] **Step 3: Build** — Run: `pnpm run build:all` — Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add -A src
git commit -m "NMS-19981: Repoint raw var(--feather-*) literals to var(--onms-*)"
```

---

## Task 5: Handle `opennms-preset.ts`

**Files:** Modify: `src/theme/opennms-preset.ts`

- [ ] **Step 1: Inspect the `var(--feather-*)` reference(s)**

Run: `grep -n "feather" src/theme/opennms-preset.ts`

- [ ] **Step 2:** If a `var(--feather-*)` is used, replace it with the verbatim literal value from the token table (the preset already uses literals elsewhere per its own doc comment, so this keeps it self-contained). Update the file's header comment that references `--feather-*`.

- [ ] **Step 3: Build** — Run: `pnpm run build:all` — Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/theme/opennms-preset.ts
git commit -m "NMS-19981: Remove --feather-* reference from PrimeVue preset"
```

---

## Task 6: Drop the `@featherds/styles` package + final verification

**Files:** Modify: `package.json`, `pnpm-lock.yaml`

- [ ] **Step 1: Confirm zero Feather references remain**

Run: `grep -rn "@featherds\|--feather-" src --include='*.vue' --include='*.scss' --include='*.ts' | grep -vE "/dist/|Vendored|origin|was @featherds|replaces"` — Expected: no output.

- [ ] **Step 2: Remove the package**

Run: `pnpm remove @featherds/styles`

- [ ] **Step 3: Full verification suite**

```bash
pnpm run build:all
pnpm exec vue-tsc --noEmit
pnpm run lint
pnpm run test
```
Expected: all PASS.

- [ ] **Step 4: Visual parity check** (harness: vite + Playwright, light AND dark) on severity-heavy views — Map markers/popups, alarm/severity tables (`_severities` classes), Nodes table, breadcrumbs, menubar/side menu. Compare before/after; expected pixel-identical.

- [ ] **Step 5: Commit**

```bash
git add package.json pnpm-lock.yaml
git commit -m "NMS-19981: Remove @featherds/styles; FeatherDS fully removed from UI"
```

---

## Self-review notes

- **Spec coverage:** token strategy (hybrid: 3 aliases + literals) ✓; new files ✓; `_severities` alpha via color-mix ✓; both import styles handled (Task 3) ✓; raw literals (Task 4) ✓; typography refs (Task 2) ✓; preset (Task 5) ✓; drop theme + package (Tasks 2, 6) ✓; verification incl. visual parity ✓.
- **Ambiguity:** any `--feather-*` encountered that is not in the token table (Task 4 Step 1) → add it verbatim before proceeding, rather than guessing.
- **Type consistency:** SCSS var names in `_onms-tokens.scss` match the `--onms-*` names in `_onms-theme.scss` and the raw-literal replacements in Task 4.
