OpenNMS WebApp
==============

The JavaScript dependencies has been centralized and controlled through `bower`.

Currently when the `opennms-webapp` is built through `maven`, it is going to perform the following tasks, in terms of JavaScript:

* Install `NodeJS` in case it has not been installed yet.

* Execute `npm install` to install all the `NodeJS` dependencies. This will put the dependencies into `opennms-webapp/node_modules`, and this directory will be ignored by `GIT`.

* Execute `bower install` to install all the JavaScript dependencies. This will put the dependencies into `opennms-webapp/bower_components`, and this directory will be ignored by `GIT`.

* Execute `glup vendor` to install the actual JavaScript dependencies into `opennms-webapp/src/main/webapp/lib`. All the JSP, HTML and pages should reference a third-party dependency only from this directory. Also, projects outside `opennms-webapp` like Vaadin applications, should reference JS dependencies from this directory as well (as it has been currently implemented).

* Execute `karma start` to run all the karma/jasmine based tests.


Adding a new dependency
=======================

Adding a dependency is as simple as just modifying the `bower.json` file with the desired dependency, for example:

```
bower install --save angular
```

Then, execute the `gulp vendor` command to update the `lib` directory.

> _IMPORTANT: only the exposed files will be copied into the `lib` directory by default. In case additional files are required, the overrides section of the `bower.json` has to be updated._

For example, the `jquery-treegrid` extension, require some CSS and Images besides the main JS file, for this reason, the `overrides` section has to be modified like this:

```
"jquery-treegrid": {
  "main": [
    "js/jquery.treegrid.js",
    "css/**",
    "img/**"
  ]
},
```

Once the third-party library already exist on `src/main/webapp/lib`, it can be used on the JSP/HTML files.

> _IMPORTANT: anything on that directory has to be commited on the GIT repository._

Adding new karma tests
======================

Any set of tests should live on their own directory inside the `src/test/javascript` directory.

Then, karma.conf.js has to be modified to include any additional third-party dependency, as well as any source dependency.

_IMPORTANT: the source JavaScript code should be placed on `src/main/webapp/js/`. Ideally, each component should have its own directory inside the `js` directory like the Requisitions UI, which uses `onms-requisitions`._

For details on building OpenNMS, please see the wiki page: [Building OpenNMS][].

[Building OpenNMS]:  https://www.opennms.org/wiki/Installation:Source
