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

## OnmsTooltip

`OnmsTooltip` (`packages/onms-ui/src/directives/OnmsTooltip.ts`) is a seam
re-export of PrimeVue's `Tooltip` directive, exported from the package barrel
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
`@opennms/onms-ui` to `OnmsUI` (see
[`vite-plugin-externals`](https://www.npmjs.com/package/vite-plugin-externals)),
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
