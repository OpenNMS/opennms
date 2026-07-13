# Design: Migrate Feather `themes/variables` color tokens → owned `onms-` layer

**Date:** 2026-07-13
**Branch context:** FeatherDS → PrimeVue migration, Phase 6 (`jira/NMS-19981-...`)
**Status:** Approved design. Prerequisite work (Feather *component* removal) reordered to happen first — see "Ordering" below.

## Ordering (prerequisite)

This token migration is cleaner if **no Feather components remain**. As long as Feather
UI components (drawer, inputs, etc.) are in use they need `--feather-*` custom properties,
which forces `themes.scss` to keep applying the Feather theme (`open-mixins` / `open-light`).

Decision: remove the remaining Feather components **first**. Once none remain, this token
migration can *fully* drop the Feather theme (`open-mixins`, `open-light.css`) rather than
deferring it. The remaining Feather component footprint (small) at time of writing:

- `FeatherAppLayout` in `src/main/App.vue` — replace with an owned/PrimeVue layout.
- `PrimeVueTest.vue` — delete (removes `@featherds/input`, `@featherds/button` usage).
- `FeatherMenuList, MenuListEntry` type import in `src/components/Menu/utils.ts` — vendor the types.
- `useOutsideClick` from `@featherds/composables` in `Menubar.vue` — vendor the composable.
- `@import "@featherds/dropdown/scss/mixins"` in `Menubar.vue` — vendor the mixin(s) used.

(The `@featherds/table`, `/select`, `/icon`, `/autocomplete` hits in `src` are origin-comments,
not imports — already vendored.)

## Token strategy (chosen: Hybrid onms- layer)

Create an owned `--onms-*` token layer:

- **Generic tokens** that PrimeVue already provides (primary, secondary-text-on-surface,
  primary-text-on-color, clickable-normal, background, border-on-surface, shade-3) → defined
  as **aliases to the closest `--p-*` token** on `:root`, e.g.
  `--onms-primary: var(--p-primary-color)`. Light/dark switching is inherited from PrimeVue
  automatically. Each alias is verified against the Feather literal during implementation;
  if a `--p-*` isn't a faithful match, that token falls back to a copied literal.
- **OpenNMS-domain severity colors** (success, error, warning, major, minor, cleared,
  indeterminate, `state-text-color-on-surface-dark`) → **copied literals**. They differ
  between light and dark, so defined under both `.open-light` and `.open-dark`. Values copied
  verbatim from Feather so colors stay pixel-identical:
  - light: success `#0b720c`, error `#a5021f`, major `#e35302`, minor `#ffac26`,
    warning `#fbe947`, indeterminate `#0092c7`, cleared `#757575`
  - dark: success `#83ee7d`, error `#ffa3b5`, major `#e35302`, minor `#ffac26`,
    warning `#fbe947`, indeterminate `#20b9f0`, cleared `#b7b7b7`
- **Plain-value tokens** (spacing-xxs/m/xl, zindex-dropdown, font-semibold) → copied literals
  as `--onms-*`.

Rationale: lowest churn (usage sites keep `var($token)`, only import path changes), owns the
domain colors, and avoids duplicating PrimeVue's full light/dark color tables.

## Scope

- **In:** remove every `@featherds/styles/themes/variables` and `@featherds/styles/themes/utils`
  import from source (~34 files + `_severities.scss`, `opennms-feather-styles.scss`, `App.vue`,
  `themes.scss`).
- **After component removal (see Ordering):** also drop `open-mixins` / `open-light.css` and the
  `.open-light { @include open-light }` / `.open-dark { @include open-dark }` includes in
  `themes.scss`, since no Feather component needs `--feather-*` anymore.

## New files

1. **`src/styles/_onms-tokens.scss`** — SCSS-var → custom-prop-name aliases mirroring Feather's
   `prefix()` indirection, for exactly the tokens we use (`$success: --onms-success;` …). Usage
   sites keep `var($token)` untouched; only the import path changes.
2. **`src/styles/_onms-theme.scss`** — defines the `--onms-*` custom properties (generic aliases
   on `:root`; severity literals under `.open-light`/`.open-dark`; plain-value literals).
   Imported once globally.
3. **`src/styles/_onms-color-utils.scss`** — an `alpha($cssVarName, $amount)` function returning
   `color-mix(in srgb, var(#{$name}) $amount, transparent)`, replacing Feather's `utils.alpha`
   (which relied on generated `--feather-*-r/g/b` channel props). Same result, no channel tokens.

## Usage-site changes (~34 files)

- Swap each file's `@use/@import "@featherds/styles/themes/variables"` → the `onms-tokens`
  equivalent, **matching each file's existing import style** (`@use … as *`, namespaced `@use`,
  or `@import`). `var($token)` bodies unchanged.
- `_severities.scss`: swap to `onms-tokens` + `onms-color-utils`.
- `themes.scss`: change `body { background: var($background) }` to the onms/`--p-*` background;
  import `_onms-theme.scss`. (Drop Feather theme includes once components are gone — see Ordering.)

## Verification

- `grep "@featherds/styles/themes/variables|/utils"` in `src` → **0**.
- Build + `vue-tsc` + eslint clean.
- Visual harness (vite + Playwright), light **and** dark, on severity-heavy views (Map
  markers/popups, alarm/severity tables, `_severities` classes) — before/after screenshots;
  expected pixel-identical (severity values are copied literals; generic aliases value-verified).

## Risks & mitigations

- **Generic `--p-*` drift vs Feather** → verify each generic token against the Feather literal;
  literal fallback if not faithful.
- **`color-mix` browser support** → supported in all modern browsers (Chrome 111+/Safari 16.2+/
  FF 113+); acceptable here. Fallback: vendor the `-r/g/b` channel approach for older support.
- **Import-style heterogeneity** across the files → the codemod respects each file's
  `@use`/`@import` form.
- **Non-Feather lookalikes** (`$border-radius`, breakpoints, `$col-limit`) come from our own
  `src/styles/vars.scss` — out of scope, do not touch.
