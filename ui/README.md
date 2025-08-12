# Vue 3 + Typescript + Vite

This template should help you start developing with Vue 3, Typescript 4.9, and Vite 4.

## Build instructions

This project requires Node 18+.

You will also need [yarn](https://yarnpkg.com/getting-started/install)

To install packages and run dev server
```
yarn install
yarn dev
```

Build for prod
```
yarn build
```

Build/watch in dev mode
```
yarn watch:dev
```

Run unit tests
```
yarn test
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

- from your `~/projects/opennms/ui` directory, run `yarn dev` or `yarn watch:dev` to build in development mode (or `yarn build` for production / minified mode)

- have a console window open in the target directory where the built/deployed files need to be, e.g., `~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui`

- after build completes, run `cp ~/projects/opennms/ui/dist/assets/*.* assets` to copy all the built JS and other asset files, then run `cp ~/projects/opennms/ui/dist/index.html .` to copy the new `index.html` file

- from `target/snapshot/jetty-webapps/opennms/ui`, occasionally run `rm assets/*.*` to clear out old files

- refresh your browser, which points at `http://localhost:8980/opennms/ui/index.html#/nodes` or similar

- test and debug code, use browser `F12 Developer Tools` to set breakpoints, view console output, inspect elements, etc.

## Prettier
Formatting should use the .prettierrc file. For VSCode, install the Prettier extension, go to the IDE Settings and set this formatter to take precedence.

### Use `<script setup>`

[`<script setup>`](https://github.com/vuejs/rfcs/pull/227). To get proper IDE support for the syntax, use [Volar](https://marketplace.visualstudio.com/items?itemName=johnsoncodehk.volar).

# On serving & routing

The SPA assets are currently hosted on Jetty via the /opennms application.

The [SpaRoutingFilter](opennms-web-api/src/main/java/org/opennms/web/servlet/SpaRoutingFilter.java) serve up the `index.html` page for URLs that do not refer to project assets.

