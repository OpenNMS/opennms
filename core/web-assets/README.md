# Web Assets

This project contains a monolithic build of all of OpenNMS's JavaScript and CSS web assets.
It also contains a Java service to look up resources by name. (Automatically resolving `.min.js` extensions and such.)

# Repository Layout

```
.
+-- pom.xml                   - Maven POM
+-- webpack.config.js         - webpack configuration
+-- project.json              - JavaScript project info (including possible build commands)
+-- src/
    +-- assembly/
    |   +-- dist.xml          - pack up the compiled assets into a convenient jar
    +-- main/
    |   +-- assets/           - all assets (fonts, images, styles, and javascript)
    |       +-- fonts/        - font files -- NOT copied unless referenced from CSS
    |       +-- images/       - image files -- NOT copied unless referenced from CSS
    |       +-- js/           - JavaScript resources that use CommonJS (require) for loading modules
    |       |   +-- apps/     - JavaScript applications (see the "Applications" section below)
    |       |   +-- lib/      - dumping ground for things that don't fit elsewhere
    |       |   +-- vendor/   - "roll-up" JavaScript files that make it easier to manage dependencies
    |       |   +-- vaadin/   - JavaScript resources that are used as Vaadin components
    |       +-- modules/      - JavaScript resources that use modern module loading (import/export)
    |       |   +-- apps/     - JavaScript applications (see the "Applications" section below)
    |       |   +-- lib/      - dumping ground for things that don't fit elsewhere
    |       |   +-- vendor/   - "roll-up" JavaScript files that make it easier to manage dependencies
    |       |   +-- vaadin/   - JavaScript resources that are used as Vaadin components
    |       +-- static/       - static files that are copied directly into the asset bundle
    |       +-- style/        - CSS and SASS (.scss) stylesheets
    |   +-- java/             - Java asset service
    |   +-- resources/        - Spring component context
    +-- test/
        +-- java/             - Java unit tests
        +-- javascript/       - JavaScript unit tests
        +-- resources/        - Java unit test resources
```

# Development

To build the project, you can just use Maven as normal, eg:

```
$ ../../compile.pl clean install
```

## Building by "Hand"

While developing new assets, it can be useful to run the node toolchain directly to build.
To do so, make sure you have [Node.js](https://nodejs.org/) and [Yarn](https://yarnpkg.com/) installed.
Note that your Node version (and associated binary plugins built in `node_modules/`) may not match the version auto-downloaded by the Maven build, so it is best to do a `../../compile.pl clean` before hand-building.

Once Node and Yarn are installed, download the dependencies with Yarn:

```
$ yarn
```

Then, there are a number of commands you can run while developing:

* `yarn build`: build all assets in "development" mode -- `.min.js` assets will not be generated (more on this below)
* `yarn release`: build all assets in "production" mode -- this generates both normal `.js` assets and prod `.min.js` assets
* `yarn watch`: build "development" assets continually, whenever files change
* `yarn release`: build "production" assets continually, whenever files change
* `yarn lint`: check for errors or warnings in your JavaScript code
* `yarn test`: run unit tests

## Adding a New Dependency

Bower is no longer used for JS runtime management.
Instead, this is a normal npm (well, Yarn) project, and you can add dependencies as you would any other similar project.

### Development Dependencies

If you are modifying `webpack.config.js` to do something new and you need a dependency added for that, should run:

```
$ yarn add -D <my-new-build-dependency>
```

### Runtime Dependencies

If you need a new dependency to be included into your JavaScript project, you skip the -D (development) flag:

```
$ yarn add <my-new-runtime-dependency>
```

## Adding New Tests

Tests are run using [Jest](https://facebook.github.io/jest/).
To add a new test, just put a file with the extension `.spec.js` or `.spec.ts` in the `src/test/javascript/` directory.

## Referencing Assets from JSPs

While you can link to the assets directly in the `/opennms/assets/` path in the webapp, it is recommended that you use JSP includes instead.  This allows for auto-handling of extensions (`.min.js` vs. `.js`) and automatically adds versioned hashes to resource URLs for browser cache purposes.

To include a resource in a JSP file, use the following syntax:

```
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <!-- required: the entry point's name -->
  <jsp:param name="asset" value="manifest" />

  <!-- optional type; if excluded, all resources generated from the specified asset will be included -->
  <jsp:param name="asset-type" value="js" />

  <!-- optional media attribute for css types -->
  <jsp:param name="asset-media" value="screen" />

  <!-- optional async attribute, defaults to false; sets async on the <script> tag -->
  <jsp:param name="asset-async" value="true" />

  <!-- optional defer attribute, defaults to false; sets defer on the <script> tag -->
  <jsp:param name="asset-defer" value="true" />
</jsp:include>
```

## Configuring OpenNMS for Development

While developing JSPs that load assets, it can be convenient to not have to keep copying resources into the `assets/` directory.
To do so, just delete the `$OPENNMS_HOME/jetty-webapps/opennms/assets/` directory and then symlink it to the `target/dist/assets` directory in this project.

You can also configure the JSP `AssetLocator` class to use minified or un-minified versions of the assets.
To do so, create an asset property file in `$OPENNMS_HOME/etc/opennms.properties.d/` and set the following property:

```
# true or false: whether to serve minified assets from JSPs
org.opennms.web.assets.minified=false
```


# About JavaScript Assets

All JavaScript assets are compiled to a .js file using a UMD loader with webpack.
The webpack bundler compiles assets based on "entry points", which are individual files that should be compiled and rolled up into resources.
This webpack configuration has a convention that will automatically create entry points based on filesystem layout.

The source tree for JavaScript files is split into 2 sections: `js` and `modules`.
`js` is for old-style (or node-style) `require()` calls to load dependencies.
`modules` is for ES6-style `import` calls.

The idea is to eventually refactor code out of the `js` directory into cleaner modules, importing only what is needed.
This lets webpack eventually automatically remove dead code from compiled JavaScript.

## Applications

In this build system, an "application" is any directory containing an `index.js` or `index.ts` file.
This is designed for builds like AngularJS apps with templates, or code-splitting into multiple files but with one "entry point".
The compiled entry point file will be named after the directory that the `index.js` file is in.
For example, if you create a directory in `apps` called `foo` with an `index.js` file in it, the entry point will be named "*foo*" and the final asset will be the file `foo.js`.

## Vendor Files

The `vendor/` directory is for the equivalent of the `dependencies/` directory in the OpenNMS Java build.
Files in `vendor/` should be a single JavaScript file that includes other dependencies (and maybe a little glue code) to make it easier to get common dependencies rolled up into one include.
The compiled entry point file will be named after the file in the `vendor/` directory.
For example, if your vendor file is called `foo.js`, the entry point will be named "*foo*" and the final asset will be the file `foo.js`.

## Vaadin Components

The `vaadin/` directory exists for files that will be included in `@JavaScript` tags in a Vaadin application.
Because of the version of Vaadin we use, it is [very picky about initialization order](https://github.com/vaadin/framework/issues/3631).
Anything under the `vaadin/` subdirectory will be compiled as a single monolithic (minified) script including dependencies with a `.vaadin.js` extension.

## Libraries

The `lib/` directory exists for things that don't fit under `apps/`, `vendor/`, or `vaadin/`.
It should contain common code and utilities.
It also can contain 3rd-party code that you wish to include into other assets.
Note that any directory named `3rdparty` will automatically be ignored by the webpack entry-point compilation code.
The naming convention is the same as for applications.

# About Stylesheet Assets

CSS files will be included directly into the finished bundle as-is.
SCSS files will be compiled to CSS before being included.

`@import` tags will be resolved at compile-time.
If you need to import CSS or SCSS that comes from the `node_modules/` directory, you can include it with a `~` and the module name as prefix.
For example, to include the `_bootstrap.scss` file from `node_modules/bootstrap-sass/assets/stylesheets/`, you would use: `@import "~bootstrap-sass/assets/stylesheets/_bootstrap.scss";`


# About Static Assets

The `static/` directory is the last resort for things that don't fit elsewhere.
If you need to put an asset (image, JavaScript, etc.) directly into the asset bundle without compilation or reference from other code, this is the place to put it.
