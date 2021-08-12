# Vue 3 + Typescript + Vite

This template should help get you started developing with Vue 3 and Typescript in Vite.

## Build instructions

This project was started with Node v14+
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

## Type Support For `.vue` Imports in TS

Since TypeScript cannot handle type information for `.vue` imports, they are shimmed to be a generic Vue component type by default. In most cases this is fine if you don't really care about component prop types outside of templates. However, if you wish to get actual prop types in `.vue` imports (for example to get props validation when using manual `h(...)` calls), you can use the following:
