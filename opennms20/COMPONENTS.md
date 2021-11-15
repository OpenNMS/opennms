
# Components

## Motivation

OpenNMS currently has **808 Maven modules** defined in .pom files within the source tree.

The Maven modules contain the core services, business logic, third-party dependency management, test frameworks, packaging, configuration, etc...
With the current structure it is often difficult to understand how the modules relate to one another and which features they are used for.

In order to work on improving the structure, we first want to better understand the modules we do have.
Indexing and grouping modules into higher level **components** seems like a logical start.

## Indexing

Let's continue to use the modules as the source of truth, and augment them with the following tags/properties:

```
  <properties>
    <opennms.doc.component>alarmd</opennms.doc.component>
    <opennms.doc.subcomponent>api</opennms.doc.subcomponent>
    <opennms.doc.stability>sustained</opennms.doc.stability>
  </properties>
```

### Components / Subcomponents

We'll try to group the modules logically under different components and iterate until we find a reasonable structure.

### Stability

Here's a model we can consider: https://raw.githubusercontent.com/Masterminds/stability/gh-pages/README.md

It has five stability markers:

- [Experimental](https://masterminds.github.io/stability/experimental.html): Not considered ready for prime-time usage
- [Active](https://masterminds.github.io/stability/active.html): At least one stable release and planned feature releases.
- [Sustained](https://masterminds.github.io/stability/sustained.html): One or more stable releases, mature, and "feature complete".
- [Maintenance](https://masterminds.github.io/stability/maintenance.html): Stable releases, but only infrequent bug fixing releases.
- [Unsupported](https://masterminds.github.io/stability/unsupported.html): No longer developed.

We can use identify modules with one of these

## CI

The top-level `build-module-index` job produces a `.csv` that looks like:
```
path,groupid,artifactid,component,subcomponent,stability
opennms-alarms/daemon/pom.xml,org.opennms,opennms-alarmd,alarmd,daemon,sustained
```

and a `.html` report that contains the component tree:
```
component
 * subcomponent 1
    ** module 1
 * subcomponent 2
    ** module 2
 * (other)
    ** module 3 
...
```

The job fails if the tree is not entirely indexed, giving developers this message:
```
The following Maven modules (6 out of 820) are missing tags:
 opennms-alarms/daemon/pom.xml org.opennms:opennms-alarmd

See https://github.com/OpenNMS/opennms/blob/features/opennms20/opennms20/COMPONENTS.md
```
