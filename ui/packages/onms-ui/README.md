# @opennms/onms-ui

The OpenNMS UI **seam layer**: owned `Onms-` components that wrap the current
third-party UI framework (PrimeVue today) behind a stable, OpenNMS-controlled
API. Core UI code and (in a later phase) plugin UIs consume these components
instead of importing PrimeVue directly, so the underlying framework can be
replaced without rewriting consumers.

## Rules

1. **Never import `primevue/*` outside this package.** ESLint
   (`no-restricted-imports` in `ui/eslint.config.js`) enforces this per
   wrapped module in app code (`ui/src/`), and bans `primevue/*` entirely
   in plugin code (`packages/onms-ui-example-plugin/src/`). Tests and
   build tooling are exempt from the rule, not from the convention.
2. **The public API is the declared props/slots/emits only.** DOM attrs
   (`class`, `style`, `data-*`, `aria-*`) and native events fall through to
   the root element and are supported. Anything else that happens to fall
   through is unsupported.
3. **`unsafePt` is an escape hatch, not an API.** It maps to PrimeVue's `pt`
   option and will break without notice when the underlying framework changes.
   Every use should link a follow-up to promote the need into a real prop.
   Composite components that assemble more than one PrimeVue primitive
   internally (`OnmsSearchInput`, `OnmsConfirmationDialog`, `OnmsMessageDialog`,
   `OnmsToastHost`) do **not** accept `unsafePt` — there is no single
   underlying `pt` root to target, so the escape hatch is omitted rather than
   wired to one arbitrarily-chosen internal.
4. **No PrimeVue types or values in any public signature.** Exported types are
   defined here in OpenNMS vocabulary.
5. New components ship with a contract test in `ui/tests/onms-ui/` asserting
   prop mapping, emit forwarding, and slot forwarding.

## Components (tranche 1)

OnmsButton, OnmsIconButton, OnmsIcon, OnmsInputText, OnmsTextarea,
OnmsPassword, OnmsCheckbox, OnmsSelect, OnmsAutoComplete, OnmsTag, OnmsDialog,
OnmsSpinner, OnmsToastHost + useOnmsToast. Note: `OnmsButton`/`OnmsIconButton`
support a `ghost` variant, `OnmsTag` supports slot content, and `OnmsTextarea`
supports `autoResize`/`fluid` props.

## Components (tranche 2)

OnmsTabs / OnmsTabList / OnmsTab / OnmsTabPanels / OnmsTabPanel (compose
together to replace a PrimeVue `Tabs`), OnmsMenu (+ the `OnmsMenuItem` type),
OnmsDrawer, OnmsChip, OnmsToggleSwitch, OnmsRadioButton, OnmsInputNumber,
OnmsSearchInput, OnmsPopover, OnmsPanel, OnmsCard, OnmsListbox,
OnmsMultiSelect, OnmsDatePicker. This tranche also absorbed the
pre-existing `OnmsConfirmationDialog` and `OnmsMessageDialog` (built on
`OnmsDialog`) into the package.

Notable deviations from the underlying PrimeVue defaults — each is an
intentional seam default, not an oversight, and matches what every existing
OpenNMS call site already passed explicitly:

- **`OnmsDrawer`** defaults `position` to `'right'` (PrimeVue's own default is
  `'left'`) — every OpenNMS drawer opens from the right.
- **`OnmsMenu`** defaults `popup` to `true` (PrimeVue's own default is
  `false`) — every OpenNMS usage is a popup/dropdown menu; pass
  `:popup="false"` for an inline menu.
- **`OnmsSearchInput`** is a fixed three-part composite: a leading search icon,
  the input, and a trailing clear button that appears only when there is a value
  (clicking it emits `update:modelValue` with `''` plus a `clear` event, then
  returns focus to the input). Consumers do not supply the icons. It also
  exposes `focus()`/`blur()` for callers that need to drive the field.
- **`OnmsInputNumber`** defaults `useGrouping` to `false` (PrimeVue's own
  default is `true`) — OpenNMS numeric fields are ports/counts/intervals,
  not grouped quantities.
- **`OnmsTab`** bakes `text-transform: uppercase` on the tab label, absorbing
  a style rule every existing tab screen re-declared per-site as a
  `:deep(.p-tab)` override.

Other tranche-2 notes:

- **`OnmsMenu`**'s `#item` slot forwards PrimeVue's own slot props verbatim
  (`{ item, props }`); `props.action` is a render-binding object owned by
  PrimeVue, not OpenNMS vocabulary. This is accepted seam leakage (documented
  here rather than wrapped) because re-shaping it would mean re-implementing
  PrimeVue's internal item-binding logic for no behavioral gain.
- **`OnmsInputNumber`**'s `inputProps` (plain DOM attrs for the inner
  `<input>`, e.g. `data-test`/`aria-label`) has no matching prop on PrimeVue
  4.5.5's `InputNumber` — it doesn't exist there. Internally it's routed
  through `pt.pcInputText.root`, the pass-through PrimeVue uses to reach the
  nested `InputText`'s rendered `<input>`, and deep-merged with `unsafePt` (a
  caller-supplied `unsafePt.pcInputText` survives; `inputProps` wins on a
  root-key collision). Function-valued `pt` sections (e.g. a function
  `unsafePt.pcInputText` or `.root`) aren't supported in combination with
  `inputProps` — the object spread drops them; use one mechanism or the
  other.
- **`OnmsListbox`**'s `change` event emits the selected value directly, not
  PrimeVue's `{ originalEvent, value }` event object, matching the
  `OnmsAutoComplete` `optionSelect` precedent.
- **`OnmsAutoComplete`** exposes `clearInput()` and `focus()` for call sites that
  render their own clear affordance (`MapSearch`). `clearInput()` exists because
  in `multiple` mode PrimeVue's inner input is *uncontrolled* — it binds `value`
  only in single mode — so text typed but not yet turned into a selection lives
  in the DOM with no model to reset, and a caller cannot reach it through
  `modelValue`. Clearing the selection itself is still done via `modelValue`.

## Components (tranche 3)

OnmsTable (+ the `OnmsTablePageEvent`, `OnmsTableSortEvent`,
`OnmsTableRowEditSaveEvent` types) and OnmsColumn.

- **`OnmsColumn`** is a *compile-time re-export* of PrimeVue `Column`, not a
  component wrapper — the one component in this package where that
  distinction matters. `DataTable` discovers its columns by walking its
  default-slot vnode tree for `type.name === 'Column'`; a `Column` nested
  inside a real wrapper component is only discovered when the wrapper vnode
  carries an explicit `key`, and a forgotten `key` silently drops the column.
  So `OnmsColumn`'s runtime component *is* PrimeVue `Column` — identical
  discovery, ordering and slot behavior — and the seam narrowing lives
  entirely in a type cast (`OnmsColumnProps`/`OnmsColumnSlots`), enforced by
  `vue-tsc` in consumer templates. A future framework swap replaces
  `OnmsColumn.ts` with a real column-collection component under the same tag.
  Because it's a re-export, its passthrough escape hatch keeps PrimeVue's
  `pt` prop name rather than `unsafePt` — a cast cannot rename a runtime prop
  — the one deliberate naming exception to rule 3 above.
- **`OnmsTable`** bakes `scope="col"` onto every column's header cell
  (PrimeVue's own `DataTable`/`Column` never sets it), by defaulting
  `pt.column.headerCell.scope` on the underlying `DataTable`. That default is
  deep-merged with the consumer-supplied `unsafePt`, so a consumer's own
  `unsafePt.column.headerCell` keys — including an explicit `scope` — win on
  collision.
- **`virtualScrollItemSize`** is the narrowed virtual-scroll surface: it maps
  to PrimeVue's `virtualScrollerOptions.itemSize` only. The rest of
  `virtualScrollerOptions` isn't exposed.
- **`expandedRows`** accepts either an array of row instances or an object
  keyed by `dataKey`, matching PrimeVue's own `DataTableExpandedRows` shape
  for tables that expand by key rather than by row identity.
- Not exposed (never used anywhere in the app today): selection mode,
  filters, `loading`, removable sort, paginator templates, CSV export. Extend
  the seam (a new prop/emit on `OnmsTable`/`OnmsColumn`) before reaching for
  `unsafePt` if a real need for one of these appears.

## Icons

The icon set (262 template-only SVG SFCs, originally vendored from FeatherDS)
lives at `packages/onms-ui/src/icons/<category>/<Name>.vue`, alongside the
`OnmsIcon` wrapper that renders it. It moved here from `ui/src/components/icons/`
in NMS-20243: under the old `@/components/icons/...` path it was reachable only
through the core app's own Vite alias, so no plugin could import an icon at all.

Icons are reached through a **subpath export**, not the barrel:

```ts
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
// then: <OnmsIcon :icon="DeleteIcon" />
```

The 12 category directories (`account`, `action`, `communication`, `content`,
`datavis`, `file`, `hardware`, `medical`, `navigation`, `network`,
`notification`, `status`) are part of the path. They matter: 22 basenames
(`Server`, `Cloud`, `Security`, `Group`, `Build`, `Code`, `Cancel`, …) appear in
more than one category, so the category is what disambiguates them.

### Why icons are NOT in the barrel

Deliberate, and load-bearing. `ui/src/main/main.ts` does
`import * as OnmsUI` and assigns that namespace object to `window.OnmsUI`. A
namespace object cannot be tree-shaken — so **anything reachable from
`index.ts` ships to every user unconditionally**. Exporting 262 icons from the
barrel would force the entire set into the core bundle even though a typical
screen uses a handful.

Keeping them on a subpath preserves per-icon tree-shaking and code-splitting:
each call site imports one module, and Rollup includes only what is reached.
(Verified: an icon used only by the Adhoc Graphs screen lands in the
`AdhocGraphs.js` chunk, not `index.js`.) A corollary worth remembering —
`tests/onms-ui/exports.test.ts` needed no change when the icons moved, because
the runtime contract genuinely did not change.

### What this means for plugins

Two different mechanisms, depending on how a plugin gets at an icon:

| | Icon used *internally* by a seam component | Plugin imports an icon *directly* |
|---|---|---|
| Path | relative, inside this package | `@opennms/onms-ui/icons/action/Delete.vue` |
| At runtime | in the **host** bundle, shared | **bundled into the plugin's own dist** (~1 KB) |
| Externalized to `window.OnmsUI`? | n/a | **No.** `external: ['@opennms/onms-ui']` is an exact-string match, and `rollup-plugin-external-globals` maps exact ids — neither matches a subpath |
| Plugin rebuild to pick up a change? | No — the host owns it | Yes, like any vendored asset |
| Needs this package resolvable at plugin build time? | No | Yes |

The first column is why this move is **invisible to the plugin ABI**. A plugin
using `OnmsSearchInput` renders the *host's* compiled component, which carries
its search and clear icons with it; the plugin's dist contains no icon data at all. So an
already-built plugin keeps working across this change with no rebuild.

The last row is the live caveat: this package is `"private": true` and is not
published to a registry. In-repo consumers (the core app, the example plugin)
resolve it through the pnpm workspace and work today. An **external** plugin
repo compiles against the *bare* specifier and externalizes it, so it never
resolves the package — but a subpath icon import *does* require real
resolution. External plugins therefore can't use subpath icons until this
package is installable (published, tarball, or vendored). That is strictly
better than before, where icons were unreachable by any plugin.

If sharing icons off `window.OnmsUI` is ever genuinely wanted, it does not
require moving them again — widen the plugin externals contract to a
regex/function matching `@opennms/onms-ui/icons/*` and expose a registry. That
is a plugin-toolchain change, and it carries the bundle cost described above;
it is declined for now, not foreclosed.

## OnmsTooltip

`OnmsTooltip` (`packages/onms-ui/src/directives/OnmsTooltip.ts`) is a seam
wrapper around PrimeVue's `Tooltip` directive, exported from the package barrel
alongside the components above. Unlike the components, it isn't installed by
this package — directives are registered at the app level, the same way core
already installs the PrimeVue plugin itself
(`ui/src/theme/primevue-setup.ts`):

```ts
import { OnmsTooltip } from '@opennms/onms-ui'
app.directive('onms-tooltip', OnmsTooltip)
```

Templates use it as `v-onms-tooltip`, not PrimeVue's own `v-tooltip` —
ESLint's `no-restricted-imports` bans importing `primevue/tooltip` directly
in app and plugin code, pointing call sites at `OnmsTooltip` instead. The
rename is behavior-neutral: PrimeVue keys the directive's internals (the
`pt` name, `data-pc-name`, the tooltip z-index bucket) off
`BaseTooltip.extend('tooltip', ...)`, not off the name it's registered under,
so `v-onms-tooltip` behaves identically to `v-tooltip` (verified against
primevue@4.5.5) — only the vocabulary in the template changes.

It is a thin wrapper rather than a bare re-export because of one upstream bug
(NMS-20162): PrimeVue stamps the configured tooltip z-index onto the host element
as `$_ptooltipZIndex`, and does it in `beforeMount` only, reading it from
`binding.instance.$primevue`. That misses two cases.

1. Vue fills `binding.instance` in with `getComponentPublicInstance()`, which
   hands over the host's *exposeProxy* whenever the component has called
   `expose()` — which `<script setup>` always compiles to. That proxy resolves
   Vue's own `$`-properties but not app `globalProperties`, so `$primevue` came
   back undefined for every tooltip in the app.
2. `beforeMount` returns early on an empty directive value, before the capture,
   and `updated` re-binds the events but never sets the property. So a tooltip
   whose text arrives with data — `v-onms-tooltip="row.label"` in a table cell,
   a label computed from a store — mounted empty and stayed uncaptured even once
   it had something to say.

Either way nothing was captured and the ZIndex util fell back to ~1000 — behind
the fixed menubar (1030) and the side-menu rail (2000), which reads as "the
tooltip never opens".

The wrapper fixes both by resolving the configured z-index off the vnode's app
context — the same fallback PrimeVue's own `BaseDirective._getConfig` uses for
the rest of its config, which is why everything except the z-index worked — and
stamping it on the host after both `beforeMount` and `updated`. `updated` is
enough for a late value because `tooltipActions` reads the property at show time
(`ZIndex.set('tooltip', tooltipElement, el.$_ptooltipZIndex)`), not at bind time,
so nothing has to be remounted for a tooltip to arrive late. See
`tests/onms-ui/OnmsTooltip.test.ts`.

### Tooltips on `OnmsIconButton`

Prefer the `tooltip` prop over putting `v-onms-tooltip` on the component:

```vue
<OnmsIconButton :icon="Delete" tooltip="Delete" />
```

The prop mounts the directive on the button itself, with no remount when the
text arrives late — the wrapper handles that (see above). When `tooltip` is set
the native `title` attribute is dropped, so the browser's own tooltip doesn't
duplicate the rich one; `title`, if given, still names the button for assistive
tech, and a `tooltip`-only button is named from the tooltip text. Positioning
modifiers (`v-onms-tooltip.top`) have no prop equivalent — a call site needing
one keeps using the directive.

## Runtime exposure for plugins

This package's barrel export (`packages/onms-ui/src/index.ts`) **is** the
`window.OnmsUI` runtime contract that externalized plugin bundles compile
against. The host (`ui/src/main/main.ts`) does:

```ts
import * as OnmsUI from '@opennms/onms-ui'
;(window as any).OnmsUI = OnmsUI
```

exactly as it already does for `vue`/`pinia`/`vue-router` on `window.Vue`/
`window.Pinia`/`window.VueRouter`. A plugin's build externalizes
`@opennms/onms-ui` to `window.OnmsUI` (see
[`rollup-plugin-external-globals`](https://www.npmjs.com/package/rollup-plugin-external-globals)
in the example plugin's `vite.config.ts`),
so `import { OnmsButton } from '@opennms/onms-ui'` in plugin source resolves
to `window.OnmsUI.OnmsButton` at runtime instead of being bundled — one Vue
runtime, one component library, shared between host and plugin.

Because the namespace object *is* the contract, `tests/onms-ui/exports.test.ts`
asserts the exact set of runtime exports (`EXPECTED_RUNTIME_EXPORTS`): a
name silently missing from the barrel breaks a plugin's import at load time
with no build-time warning on either side. Any addition or removal to the
barrel must update that list in the same commit. `ONMS_UI_VERSION` (mirrored
onto `window.OnmsUI.ONMS_UI_VERSION`) lets a plugin confirm which
component-library version the host is actually running.

For the full plugin-developer walkthrough — the externals contract, the
module contract (`window[extensionId]`, derived from the module URL's
second-to-last path segment), the version handshake, and the dev harness
(`VITE_EXAMPLE_PLUGIN=true pnpm dev`, then open `/example-plugin`) — see
[`packages/onms-ui-example-plugin`](../onms-ui-example-plugin/README.md), a
real externalized plugin built with the toolchain a third-party author would
use.

## Sanctioned direct-PrimeVue exceptions

Outside this package, importing `primevue/*` is banned by ESLint
(`no-restricted-imports`, per module, in `ui/eslint.config.js`). A small,
explicitly-listed set of exceptions exist today and are expected to shrink
over time, not grow:

- `primevue/config` in `ui/src/theme/primevue-setup.ts` — app bootstrap:
  installing the PrimeVue plugin itself is a host concern, not a
  seam-wrapped component. (Its tooltip directive is seam-wrapped — see
  `OnmsTooltip` below — `primevue/tooltip` is banned outside this package by
  the same ESLint rule.)
- `primevue/tieredmenu` in `ui/src/components/Menu/SideMenu.vue` — the side
  navigation drives `TieredMenu` internals directly (dirty-flag tracking, DOM
  queries against `.p-tieredmenu-*` classes) that don't fit a thin prop/slot
  wrapper. The import carries an inline `eslint-disable-next-line
  no-restricted-imports` with a comment pointing back to NMS-20081; revisit
  when the side menu is redesigned.

## Planned next

Theme tokens, the library build/`.d.ts` output, and npm publishing remain for
a later phase of the plugin-sharing work (see NMS-20029). `window.OnmsUI`
runtime exposure has landed — see "Runtime exposure for plugins" above.
