# Vue 3 + Typescript + Vite

This template should help get you started developing with Vue 3 and Typescript in Vite.

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

## Vuex state management
This project uses [Vuex](https://next.vuex.vuejs.org/) with the modules pattern.
Each store module has separate files for state, actions, and mutations.
Current convention is to only call actions from components, (no mutations).

## Vue-router
Project routes make use of [vue-router](https://next.router.vuejs.org/guide/)

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/)

## Prettier
Formatting should use the .prettierrc file. For VSCode, install the Prettier extension, go to the IDE Settings and set this formatter to take precedence.

### Use `<script setup>`

[`<script setup>`](https://github.com/vuejs/rfcs/pull/227). To get proper IDE support for the syntax, use [Volar](https://marketplace.visualstudio.com/items?itemName=johnsoncodehk.volar).

# On serving & routing

The SPA assets are currently hosted on Jetty via the /opennms application.

The [SpaRoutingFilter](opennms-web-api/src/main/java/org/opennms/web/servlet/SpaRoutingFilter.java) serve up the `index.html` page for URLs that do not refer to project assets.

