# ui-components - Vue 3 + Typescript + Vite

The `ui-components` project is a Vue mini application which contains top and side menu bars that are basically the same (as much as possible) as the menu components in the main `ui` project.

**NOTE:** Any changes made here - or in the `ui` project which affects the Menubar or Sidebar components - must also be made in the other code.
At some point we may factor this out into a separate component package, but currently it is up to the developer to keep the menus in sync.

This app is meant to be embedded in the "legacy" JSP-based OpenNMS UI application to provide the same menus as in the Vue app.
This project will replace the menubar / navbar that was used in the JSP `opennms-webapp` application.

## Build instructions

This project requires Node 18+ and `yarn` 1.22.18+ to build. Currently using Node 22.4.0 and yarn 1.22.22 in development.

You will need [yarn](https://yarnpkg.com/getting-started/install)

To install packages and build:

```
yarn install
yarn build
```

Build for production - files will be put in `dist`.
```
yarn build
```

Run unit tests
```
yarn test
```

## Build options

To disable minification, in order to be able to view and debug code more easily, you can add this under the `build` section in `vite.config.ts`:

```
...
  build: {
    ...
    minify: false
  }
```

Do **not** check this in as it should not be used for production code.

## State management: pinia
This project uses [pinia](https://pinia.vuejs.org/).

pinia stores are under `stores`.

## Vue-router

Unlike the Vue SPA (Single Page Application) app in `ui`, this is not a SPA but rather embedded into the JSP application.
Currently it does not use `vue-router`.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/)

## Developer workflow

Developer workflow for development -> build -> fast deploy.

- assuming your local OpenNMS code is in `~/projects/opennms`

- write your code

- from your `~/projects/opennms/ui-components` directory, run `yarn build`

- have a console window open in the target directory where the built/deployed files need to be, e.g., `~/projects/opennms/target/opennms-XX.X.X-SNAPSHOT/jetty-webapps/opennms/ui-components`

- after build completes, run `cp ~/projects/opennms/ui-components/dist/assets/*.* assets` to copy all the built JS and other asset files, then run `cp ~/projects/opennms/ui/dist/index.html .` to copy the new `index.html` file

```
# to copy all the built JS and other asset files:
cp ~/projects/opennms/ui-components/dist/assets/*.* assets

# to copy the new 'index.html' file
cp ~/projects/opennms/ui/dist/index.html .
```

- from `target/snapshot/jetty-webapps/opennms/ui-components`, occasionally run `rm assets/*.*` to clear out old files

- refresh your browser while on a "legacy" page, for example the main home page, the old node list page, etc.

- test and debug code, use browser `F12 Developer Tools` to set breakpoints, view console output, inspect elements, etc. See the note above re disabling minification.

### Use `<script setup>`

[`<script setup>`](https://github.com/vuejs/rfcs/pull/227). To get proper IDE support for the syntax, use [Volar](https://marketplace.visualstudio.com/items?itemName=johnsoncodehk.volar).

# On serving & routing

The Vue assets are currently hosted on Jetty via the /opennms application.

The `opennms-webapp` `webapp/includes/bootstrap.jsp` file loads this Vue application.

It is mounted in a `div` with the `id` of `opennms-sidemenu-container`. Note that this is expected in `main.ts` as the mounting element.

If for some reason this changes in the hosting page, you can use the `.env` variable `VITE_APP_MOUNT_ID` to change it.

The `opennms-webapp` `pom.xml` file has a step to copy the `ui-components/dist` output into the `opennms-webapp` artifact, which eventually gets copied to `opennms/jetty-webapps/opennms/ui-components` on installation.

`bootstrap.jsp` will emit something like this:

```
<div id="opennms-sidemenu-container"></div>
<script type="module" src="/opennms/ui-components/assets/index.js"></script>
 ```

## Reenable old menu

To reenable the old/legacy JSP menu for a page, add `?oldmenu=true` to the URL. This will prevent the new Vue menus from being displayed and will display the legacy menu.

Once the new menus are complete, this option will be removed.
