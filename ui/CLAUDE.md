# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This file covers the `ui/` folder only — the Vue 3 SPA embedded within OpenNMS (a Java-based network monitoring platform). Run all commands below from the `ui/` directory.

## Hard Rules

- **Adhere to the linting and formatting rules in `ui/eslint.config.js`** (ESLint v10 flat config + `@stylistic`; there is no Prettier). Key conventions: single quotes, no semicolons, 2-space indent, no trailing commas, `1tbs` brace style, and `curly: all` — every control-flow block uses braces with line breaks, even single statements. Run `pnpm lint` before considering work done.
- **`.ts` files (but not `.vue` files) must start with the OpenNMS license header** — the `///`-style AGPLv3 comment block. Copy it verbatim from any existing `.ts` file (e.g. `src/services/axiosInstances.ts`).
- **Never import `primevue/*` in app code (`src/`) when an `Onms-` wrapper exists** — use `@opennms/onms-ui` instead. ESLint `no-restricted-imports` enforces the current list; only `packages/onms-ui/` itself and tests are exempt.

## Commands

Package manager is **pnpm** (enforced by a preinstall hook). Node 22.13+.

```bash
pnpm install              # after dependency changes
pnpm run build:all        # build BOTH apps (main SPA + menu); plain `build` is main-only
pnpm run build:dev:all    # both apps, non-minified for debugging
pnpm test                 # run all vitest unit tests
pnpm test tests/map.test.ts   # run a single test file
pnpm test:watch           # watch mode
pnpm lint                 # check lint/format
pnpm lint:fix             # auto-fix, but double-check the result
```

Type checking (`vue-tsc --noEmit`) runs as part of every build script; there is no separate typecheck script.

### Deploying to a local OpenNMS instance

There is no usable dev server (OpenNMS URLs point at port 8980). Build, then copy assets into the running instance (paths below are relative to the repo root; `XX.X.X` is the current snapshot version under `target/`):

```bash
# Main SPA
cd target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui
cp <repo-root>/ui/src/main/dist/assets/*.* assets
cp <repo-root>/ui/src/main/dist/index.html .

# Menu component
cd target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui-components
cp <repo-root>/ui/src/menu/dist-menu/assets/*.* assets
cp <repo-root>/ui/src/menu/dist-menu/index.html .
```

Occasionally `rm assets/*.*` in the target directories to clear stale hashed files.

## Architecture

### Two Vite apps, one codebase

- **Main SPA** (`src/main/`, `vite.config.ts`) — full application served at `/opennms/ui`, built to `src/main/dist/`
- **Menu app** (`src/menu/`, `vite.config.menu.ts`) — embeds into legacy JSP/Vaadin pages at `/opennms/ui-components`, built to `src/menu/dist-menu/`

Both share `src/components/`, `src/services/`, `src/stores/`, and `src/composables/` but have separate entry points and build configs. The Java `SpaRoutingFilter` serves `index.html` for non-asset URLs; routing uses `createWebHashHistory('/opennms/ui')`.

This is a pnpm workspace (`pnpm-workspace.yaml`): the app (`web`) plus `packages/onms-ui`.

### The seam layer: `@opennms/onms-ui` (`packages/onms-ui/`)

Owned `Onms-` components that wrap PrimeVue 4.x behind a stable, OpenNMS-controlled API so the underlying framework can be swapped without rewriting consumers. FeatherDS (`@featherds/*`) has been fully removed. Rules (see `packages/onms-ui/README.md`):

1. Direct `primevue/*` imports live only inside this package.
2. The public API is the declared props/slots/emits only. DOM attrs (`class`, `style`, `data-*`, `aria-*`) and native events fall through to the root element; anything else that falls through is unsupported.
3. `unsafePt` (maps to PrimeVue `pt`) is an escape hatch, not an API — every use should link a follow-up to promote it into a real prop.
4. No PrimeVue types or values in any public signature; exported types are defined in OpenNMS vocabulary in `packages/onms-ui/src/types.ts`.
5. Every new component ships with a contract test in `tests/onms-ui/` asserting prop mapping, emit forwarding, and slot forwarding.

The package covers most of the PrimeVue surface in use: buttons, form inputs (text, number, password, textarea, checkbox, radio, select, multiselect, autocomplete, datepicker, toggle switch, search input, listbox), overlays (dialog, confirmation/message dialogs, drawer, popover, menu), data display (table + column, card, panel, chip, tag, tabs family), plus `OnmsSpinner`, `OnmsToastHost` + `useOnmsToast`, and the `v-onms-tooltip` directive. The authoritative list is `packages/onms-ui/src/components/` and the ESLint `no-restricted-imports` entries in `eslint.config.js` — check there before importing anything from `primevue/*`. Exception: `primevue/tieredmenu` has no wrapper yet and is sanctioned only in `SideMenu.vue` (inline-disabled).

### UI, theming, layout

- Icons: `OnmsIcon` wrapper + vendored SVG components under `src/components/icons/`
- Layout: `OnmsAppLayout` (`src/components/Layout/OnmsAppLayout.vue`)
- Theming: owned `--onms-*` tokens (`src/styles/onms-theme.scss`, `_onms-tokens.scss`) layered over PrimeVue's `--p-*` tokens; light/dark via `.open-light`/`.open-dark` class on `<html>`/`<body>` (PrimeVue `darkModeSelector`)
- Typography: owned mixins in `src/styles/_onms-typography.scss` (CSS classes `.headline3`, `.headline4`, `.subtitle1`)
- Custom elements (e.g. `<rapi-doc>`) must be registered in `vite.config.ts` under `vue.template.compilerOptions.isCustomElement`

### Components: `<script setup>` only

All components use Composition API with `<script setup lang="ts">`, typed `defineProps<{...}>()` and `defineEmits<{...}>()`.

### Form fields: `FormField` wrapper

Form inputs are wrapped in `FormField` (`src/components/Common/FormField.vue`), which renders a top-aligned bold label plus error/hint text and wires up `aria-describedby`. Pass `label`, `for` (matching the control's `id`), and optional `required`/`error`/`hint`. Do not use PrimeVue `FloatLabel`/`IftaLabel` — the top-label pattern replaced them.

### State: Pinia setup stores

Stores in `src/stores/` use the setup-store pattern (a function returning refs and actions), not the Options API.

### Service layer

Services in `src/services/` use pre-configured axios instances from `services/axiosInstances.ts`:
- `v2` — OpenNMS REST API v2 (`/opennms/api/v2`)
- `rest` — legacy REST API (`/opennms/rest`)
- `restFile` — multipart file uploads

Each service exports individual functions; all are aggregated into the default export of `services/index.ts` — add new service methods there too.

### Auto-imports

Vue, Vue Router, and VueUse APIs are auto-imported via `unplugin-auto-import` (`ref`, `computed`, `watch`, `useRouter`, `whenever`, etc. need no imports). **Custom composables from `src/composables/` must be imported manually** (e.g. `import useSnackbar from '@/composables/useSnackbar'`). Composables share global state via module-level refs.

### Role-based access control

Routes and UI elements check roles via the `useRole` composable (`adminRole`, `filesystemEditorRole`, `rolesAreLoaded`, ...); route guards use `beforeEnter` and show a snackbar on denial. Debug role issues via `useAuthStore().whoAmI.roles`.

### Testing

Vitest + Happy-DOM + `@vue/test-utils` + `@pinia/testing`. Tests live in `tests/`, mirroring the `src/` structure. Mount components with `createTestingPinia({ stubActions: false })` and stub `router-link`; prefer `data-test` attributes for element selection.

### Key directories

- `src/components/` — reusable components (organized by feature)
- `src/containers/` — top-level page components (route targets)
- `src/composables/` — shared reactive logic
- `src/stores/` — Pinia stores
- `src/services/` — backend API layer
- `src/types/` — TypeScript type definitions
- `packages/onms-ui/` — the seam-layer package
- `tests/` — vitest unit tests

### Environment configuration

`.env` files resolve relative to `src/main/` (Vite `envDir`). Use `import.meta.env.VITE_*` in source — do not use `process.env`. Main variables: `VITE_BASE_V2_URL`, `VITE_BASE_REST_URL`, `VITE_BASE_URL` (default `http://localhost:8980`), `VITE_APP_LOGO_NAME` (product logo component aliased in `vite.config.ts`), `VITE_MENU_APP_MOUNT_ID`.
