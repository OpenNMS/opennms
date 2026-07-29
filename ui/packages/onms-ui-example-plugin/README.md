# @opennms/onms-ui-example-plugin

A reference OpenNMS UI plugin. It is dev-only tooling — private, never part of
the core bundle — that exists for two reasons:

1. **Proves the seam works.** It builds a real, externalized `.es.js` module
   using the exact toolchain a third-party plugin author would use, and
   exercises a representative slice of `@opennms/onms-ui`: a themed form
   (input, select, buttons), a modal dialog, a host toast, and a tabbed
   data table. If this package builds clean and the checks below pass, the
   `window.OnmsUI` seam is safe for external consumers.
2. **Is the first plugin-developer document.** Everything below — the
   externals contract, the module contract, the version handshake, the build
   commands — is written for someone building a *different* OpenNMS UI plugin,
   not just for maintainers of this repo.

## The externals contract

The host page (`ui/src/main/main.ts`) puts the framework and the shared
component library on `window` before it mounts anything:

| Import a plugin writes  | Resolves from (at runtime) |
| ------------------------ | --------------------------- |
| `vue`                    | `window.Vue`                |
| `pinia`                  | `window.Pinia`               |
| `vue-router`              | `window.VueRouter`           |
| `@opennms/onms-ui`        | `window.OnmsUI`               |

A plugin build must **not** bundle any of these — it externalizes them so the
plugin's JS module stays tiny and always uses the exact framework/component
instances the host page is already running (single Vue runtime, single Pinia
store registry, single component library — no duplicate frameworks, no
prop/type drift).

This package's `vite.config.ts` implements that contract with
[`vite-plugin-externals`](https://www.npmjs.com/package/vite-plugin-externals):

```ts
viteExternalsPlugin({
  vue: 'Vue',
  pinia: 'Pinia',
  'vue-router': 'VueRouter',
  '@opennms/onms-ui': 'OnmsUI'
})
```

Window-global resolution (`import X from 'vue'` → `const X = window.Vue`) is
the plugin's **default** mode — no extra option is required to get it. (Its
`useWindow: false` option exists for a different scenario — module-path
resolution without the `window` prefix — and is not used here.)

## The module contract

The build output is a single ES module: `dist/exampleUiExtension.es.js`. When
loaded, it sets `window.exampleUiExtension` to its root component
(`src/main.ts`):

```ts
;(window as unknown as Record<string, unknown>).exampleUiExtension = ExampleApp
```

This is the host's `externalComponent()` contract
(`ui/src/components/Plugin/utils.ts`): the host injects the module via a
`<script type="module">` tag, then looks up `window[extensionId]` and mounts
it as the plugin's root component. `extensionId` is derived from the
**second-to-last path segment of the module's URL** — for a real plugin
served at:

```
GET .../rest/plugins/ui-extension/module/exampleUiExtension?path=dist/exampleUiExtension.es.js
```

the second-to-last segment is `exampleUiExtension`, which is why this package's
global name, file name, and the REST path segment for its extension ID all
have to agree. Get any of them out of sync and the host loads the module but
never finds the component on `window`.

CSS is a separate concern: Vite's library mode extracts any `<style>` block
into its own CSS file, and this example's dev harness (Task 5) does not serve
it — real plugins ship their CSS via `GET
/rest/plugins/ui-extension/css/{extensionId}` (`getCSSPath` in
`Plugin/utils.ts`), loaded by the host as a `<link>` tag. That is why
`ExampleApp.vue` has no `<style>` block and instead styles itself inline using
the host's PrimeVue CSS custom properties (`var(--p-content-background)`,
etc.) — the supported way for a plugin to look right in both the light and
dark host theme without shipping any CSS of its own.

## Version handshake

`@opennms/onms-ui` exports `ONMS_UI_VERSION`, mirrored onto
`window.OnmsUI.ONMS_UI_VERSION` at runtime. A plugin should read it (as this
example does, rendering it in its header) to confirm it's running against the
component-library version it was built and tested against, and to fail loudly
or degrade gracefully if the host is running something unexpectedly different.

Pin your plugin's own `package.json` dependency on `@opennms/onms-ui` to the
version matching your target OpenNMS release line — it only affects
TypeScript types and editor support at build time (the real component
implementations always come from the host's `window.OnmsUI` at runtime), but
keeping it aligned means the types you build against match the host you'll
actually run on.

## Build

```bash
pnpm install
pnpm --filter @opennms/onms-ui-example-plugin build
```

This produces `dist/exampleUiExtension.es.js`.

Type-check the package on its own strict `tsconfig.json` (nothing in the
normal `pnpm build`/`pnpm test` gates at the repo root reaches into this
package):

```bash
pnpm --filter @opennms/onms-ui-example-plugin typecheck
```

## Run it in the dev harness

```bash
VITE_EXAMPLE_PLUGIN=true pnpm dev
```

Then open `/example-plugin` in the running dev server. (Task 5 wires the dev
harness route and the flag that serves this package's `dist/` output plus a
mount point for it.)

## Manual verification checklist

After building, confirm:

- [ ] **Theme + dark mode**: the example follows the host's light/dark theme
      toggle with no flash or mismatched colors — it uses no CSS of its own.
- [ ] **Dialog z-index**: clicking "Open dialog (z-index)" overlays the host
      chrome correctly (nothing from the host renders on top of the modal or
      its backdrop).
- [ ] **Toast via host outlet**: clicking "Toast (host outlet)" shows a toast
      using the *host's* toast outlet (`OnmsToastHost`), not one the plugin
      renders itself.
- [ ] **Table/tabs render**: switching between the Form and Table tabs works,
      and the table renders three rows with an `up`/`down` status tag per row.
- [ ] **Zero framework code in dist** — externalization actually happened:

  ```bash
  grep -c "createElementBlock\|defineComponent" dist/exampleUiExtension.es.js
  # small (this component's own compiled render code only — not Vue's runtime)

  grep "window.OnmsUI\|window\['OnmsUI'\]" dist/exampleUiExtension.es.js
  # present — confirms components resolve from the host at runtime

  grep -c "p-datatable\|BaseComponent" dist/exampleUiExtension.es.js
  # 0 — confirms no PrimeVue implementation code was bundled
  ```

  The built file should be a few KB (not hundreds) — this package's own build
  produces a ~5 KB module.
