# Development Notes for OpenNMS Vue UI

## .env file

The `.env` file (or `.env.development`) contains some compile-time environment-specific configuration parameters.

- `VITE_BASE_V2_URL`: URL to the OpenNMS Rest Service v2 endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/api/v2`

- `VITE_BASE_REST_URL`: URL to the OpenNMS Rest Service endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/rest`

- `VITE_BASE_URL`: URL to the OpenNMS Rest Service endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/rest`

- `VITE_BASE_URL`: Absolute base URL of the web app. Default: `http://localhost:8980`

- `VITE_MENU_APP_MOUNT_ID`: Id of the `div` element hosting the Vue menu application. Default: `opennms-sidemenu-container`

- `VITE_APP_LOGO_NAME`: Base logo name for the main logo at the top of the application.
  Default: `LogoHorizon` for Horizon. May be different for different products.
  The file should be a Vue file in the `src/assets` directory which contains `svg` code for the logo.

## featherds and vue-tsc

Note there are some issues with `@featherds` and `vue-tsc`. `feather` may not compile with `vue-tsc` `1.x` versions.

This was resolved by updating `vue-tsc` to `^2.12.2`.

`vue-tsc 2.x` is a full rewrite based on `@volar/typescript` (Volar 2 engine). The reason it resolves the issue:                                                                       

```
  - vue-tsc 1.x: When processing typeof import("./components/FeatherCheckboxGroup.vue") in @featherds' src/index.d.ts, it fully type-checked the .vue template — including the v-bind="inherittedAttrs" binding. Vue 3.5.32 tightened HTMLAttributes types, so class: unknown in that binding started failing.                                                                                              
  - vue-tsc 2.x (Volar 2): Rewrote how it extracts prop types from .vue files referenced via typeof import(...). It only needs the component's props/emits definitions — it doesn't have to type-check the full template of those node_modules .vue files, so the stricter v-bind constraint on the <div> never gets evaluated.                                                                             
                                                                                                                                                                                                       
  This is also the better long-term fix: vue-tsc 1.x is effectively end-of-life (last release was 1.8.27 in late 2023), while 2.x is the actively maintained branch. No concerns with the upgrade — the TypeScript ~5.4.5 satisfies the >=5.0.0 peer dep requirement, and @vitejs/plugin-vue ^5.x is fully compatible. 
```
