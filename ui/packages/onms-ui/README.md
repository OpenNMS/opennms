# @opennms/onms-ui

The OpenNMS UI **seam layer**: owned `Onms-` components that wrap the current
third-party UI framework (PrimeVue today) behind a stable, OpenNMS-controlled
API. Core UI code and (in a later phase) plugin UIs consume these components
instead of importing PrimeVue directly, so the underlying framework can be
replaced without rewriting consumers.

## Rules

1. **Never import `primevue/*` outside this package.** ESLint
   (`no-restricted-imports` in `ui/eslint.config.js`) enforces this per
   wrapped module.
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

## Sanctioned direct-PrimeVue exceptions

Outside this package, importing `primevue/*` is banned by ESLint
(`no-restricted-imports`, per module, in `ui/eslint.config.js`). A small,
explicitly-listed set of exceptions exist today and are expected to shrink
over time, not grow:

- `primevue/config` and `primevue/tooltip` in `ui/src/theme/primevue-setup.ts`
  — app bootstrap: installing the PrimeVue plugin and registering the
  `v-tooltip` directive globally are host concerns, not seam-wrapped
  components. `v-tooltip` itself is host-provided today; making it a
  seam-owned directive is out of scope for this phase.
- `primevue/tieredmenu` in `ui/src/components/Menu/SideMenu.vue` — the side
  navigation drives `TieredMenu` internals directly (dirty-flag tracking, DOM
  queries against `.p-tieredmenu-*` classes) that don't fit a thin prop/slot
  wrapper. The import carries an inline `eslint-disable-next-line
  no-restricted-imports` with a comment pointing back to NMS-20081; revisit
  when the side menu is redesigned.
- `primevue/datatable` and `primevue/column` — used at ~40 and ~29 call sites
  respectively across the app. Wrapping `DataTable` is its own design effort
  (`OnmsTable`), deferred to a future tranche.

## Planned next

OnmsTable (DataTable + Column) as its own effort. Theme tokens, the library
build/`.d.ts` output, npm publishing, and `window.OnmsUI` runtime exposure
arrive with the plugin-sharing phase (see NMS-20029).
