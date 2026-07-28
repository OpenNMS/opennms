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

Planned next: Tabs family, Menu, Drawer, Chip, ToggleSwitch, InputNumber,
ConfirmationDialog/MessageDialog absorption, and OnmsTable (DataTable) as its
own effort. Theme tokens and npm publishing arrive with the plugin-sharing
phase (see NMS-20029).
