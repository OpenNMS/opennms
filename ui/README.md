# Vue 3 + Typescript + Vite

This template should help you start developing with Vue 3, Typescript 5, and Vite 4.

## Build instructions

This project requires Node 18+. Node 22+ is recommended. pnpm 10.24+ is required.

To install packages and build (see Developer workflow below for more):
```
pnpm install
pnpm run build:all
```

Run unit tests
```
pnpm test
```

## State management: pinia
This project uses [pinia](https://pinia.vuejs.org/).

pinia stores are under `stores`.

*NOTE:* this project used to use `vuex` but it has been replaced by `pinia`.

## Vue-router
Project routes make use of [vue-router](https://next.router.vuejs.org/guide/).

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/)

## Developer workflow

Developer workflow for development -> build -> fast deploy. There may be issues running the vite development server since it runs on a different port than OpenNMS; OpenNMS menu items and URLs will point to that port instead of the vite port.

- assuming your local OpenNMS Horizon code is in `~/projects/opennms`

- write your code

- any time you update dependencies, or on initial build, from your `~/projects/opennms/ui` directory, run `pnpm install`

- from your `~/projects/opennms/ui` directory, run `pnpm run build:all` (or `pnpm run build:all:dev` for non-minified mode)

- have a console window open in the target directory where the built/deployed files need to be, e.g., `~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui` and `~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui-components`

- after build completes, run the following to copy all the built JS and CSS asset files and new `index.html` files into your OpenNMS instance.

`ui` is for the Vue SPA UI app, `ui-components` is for the Vue menu app that is embedded in legacy JSP/Vaadin pages

```
cd ~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui
cp ~/projects/opennms/ui/src/main/dist/assets/*.* assets
cp ~/projects/opennms/ui/src/main/dist/index.html .

cd ~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui-components
cp ~/projects/opennms/ui/src/menu/dist-menu/assets/*.* assets
cp ~/projects/opennms/ui/src/menu/dist-menu/index.html .
```

- from `target/snapshot/jetty-webapps/opennms/ui` and `target/snapshot/jetty-webapps/opennms/ui-components`, occasionally run `rm assets/*.*` to clear out old files

- refresh your browser, which points at `http://localhost:8980/opennms` and choose a menu item to go to a Vue SPA page, or else see the Vue Menu on legacy pages

- test and debug code, use browser `F12 Developer Tools` to set breakpoints, view console output, inspect elements, etc.

- often `console.log` or `console.dir` statements in the code are more helpful for debugging than the browser debugger

- run `pnpm lint` to check for any linting/formatting errors. `pnpm lint --fix` may fix them, but you should always double check

## Prettier
Formatting should use the .prettierrc file. For VSCode, install the Prettier extension, go to the IDE Settings and set this formatter to take precedence.

### Use `<script setup>`

[`<script setup>`](https://github.com/vuejs/rfcs/pull/227). To get proper IDE support for the syntax, use [Volar](https://marketplace.visualstudio.com/items?itemName=johnsoncodehk.volar).

# On serving & routing

The SPA assets are currently hosted on Jetty via the /opennms application.

The [SpaRoutingFilter](opennms-web-api/src/main/java/org/opennms/web/servlet/SpaRoutingFilter.java) serve up the `index.html` page for URLs that do not refer to project assets.
