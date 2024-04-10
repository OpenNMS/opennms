# Developer Tips and Tricks

Here is a not-entirely-organized collection of things that can make your life a little easier when working with the OpenNMS codebase.

## Git

### General Branch Layout

Horizon releases always come from the latest `release-XX.x` branch.
When a new Meridian release is imminent, we will branch from the latest Horizon to create it, based on the year.
We usually create this branch after a major Horizon version has had a few releases to stabilize.

For example, Meridian 2023 is based on a branch of `release-31.x` a bit before
Horizon 31.0.3 came out.
Meridian 2024 is based on `release-33.x` just before Horizon 33.0.3 came out.

Our continuous integration (using CircleCI) is configured to automatically merge "forward" across foundation, horizon, and develop branches.
At the time of this writing, it's currently:

(earlier foundations) -> `foundation-2024` -> `release-33.x` -> `develop`

### Useful Tags

#### Horizon

Horizon releases always get tagged `opennms-XX.X.X-1` so you can compare the changes between releases with, eg:

```bash
git diff opennms-32.0.1-1..opennms-33.0.2-1
```

#### Meridian

Meridian releases happen in a private repo that is forked from the primary OpenNMS (Horizon) one.
It contains mostly branding and build-related changes and is rarely written to directly.

When a new Meridian release is tagged, a tag is _also_ created in the Horizon repository to give an indicator as to what changes to the foundation branches made it into their corresponding meridian releases, in the format `meridian-foundation-XXXX.XX.X`.
You can compare the core code that goes into Meridian releases like so:

```bash
git diff meridian-foundation-2023.1.0..meridian-foundation-2023.1.1
```

### Useful Tools and Tricks

TODO: `git gone`, `git rebase -i`, ???

## Maven

### Partial Builds

OpenNMS's source code is massive and complicated.
You can use some of these techniques to avoid spending a long amount of time waiting for compiles while just working on a small part of the code.

#### Only Build the Bits You Need

While doing development, it's often convenient to (re)build only part of the tree.
To do so, you need a combination of the `--projects` (`-pl`) argument, to tell it what project(s) you care about, and then `--also-make` (`-am`) or `--also-make-dependents` (`-amd`).

For example, if you just did a clean checkout, and want to work on the code in the `opennms-dao` project, make a note of the artifact ID and then use `-am` like so:

```bash
./compile.pl -DskipTests=true -pl :opennms-dao -am install
```

The full name of the `opennms-dao` project is `org.opennms:opennms-dao` but as long as the artifact ID is unique, you don't have to bother typing the first part.

#### Build the Bits That Depend on the Bits You Changed

Alternately, if you've made some changes, and only want to build and/or run tests on the subset of things that depend on your changed project, you can use `-amd` to build anything that depends (even indirectly) on the project you specify:

```bash
./compile.pl -t -pl :opennms-dao -amd install
```

#### Build the Bits that Match a Search

There is a tool called `tools/development/pom-artifact.sh` that can turn a `pom.xml` file into a `groupId:artifactId` tuple, which you can then use when passing things to `--projects`/`-pl`.

A separate tool wraps it called `tools/development/grep-pom-artifact.sh` which performs a `git grep`, determines the related `pom.xml` files associated with any code that the grep matches, and then passes those poms to `pom-artifact.sh`.
This allows you to do powerful things like, "I just updated the dependency for jdom, rebuild anything that uses jdom to make sure it still passes."

```bash
./compile.pl -DskipTests=true -pl `tools/development/grep-pom-artifact.sh -i jdom` install
```

### Dependency Management

OpenNMS is a huge codebase developed over many years, and there are a lot of weird corners.
In an effort to make it easier to normalize things across a wide range of 3rd-party dependencies, we have some tools enabled to help make sure things don't go awry (as much as possible).

#### Maven Enforcer Plugin

This plugin enforces that certain unwanted dependencies _don't_ accidentally end up in the dependency tree.
Most are reasonably obvious, old versions got replaced with new versions with a different `groupId:artifactId``, or we're using a version that Apache Servicemix has bundled rather than the "original" jars.
(Servicemix takes other projects' code and wraps them up in OSGi-friendly bundles.)

If you use one of these banned jars, the enforcer plugin will throw an error and tell you to go use `mvn dependency:tree` in the affected project to determine how the bad dependency got pulled in.
Usually all you have to do is add an `<exclusions>` section to the dependency in the POM that brings in the bad dependency transiently.
You may also need to add the "approved" version of that thing as an explicit dependency as well.

For example, in the Selenium monitor, we exclude `commons-logging` from its dependencies:

```xml
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>${seleniumVersion}</version>
      <exclusions>
        <exclusion>
          <groupId>commons-logging</groupId>
          <artifactId>commons-logging</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

In this case, we **don't** bother adding anything other than `slf4j-api` as a dependency, since our runtime already pulls the commons-logging adapter in.
But in some cases if you are actually compiling against a transient dependency, you should add the replacement (`jcl-over-slf4j`) as a dependency to the Selenium monitor in its place.

### License Maven Plugin

The `license-maven-plugin` is used to create a report of all of the 3rd-party licensed code used in OpenNMS.
The build is currently configured to error out if there are dependencies with unknown licenses during tarball assembly in CircleCI (or locally if you pass `-Denable.license=true`).

If the build fails, the easiest thing to do to fix validation is to run:

```bash
./compile.pl -DskipTests=true -Denable.license=true -Passemblies -Psmoke install
```

It will die with an error in the project that needs license validation, and point you to the path of the `THIRD-PARTY.properties` that you need to update to fix it.
Update that file with the missing licenses, and then re-run your `./compile.pl` command, adding the `-rf <project>` to the end that the failure prompts for continuing your build.

It may take a few runs before you catch everything.

### Custom Forks

We have made custom forks of Spring and Spring Security to be able to backport security fixes without moving past 4.2.x, since we have had issues trying to move to 5.x (or even 4.3.x!)

These get deployed to our maven "3rdparty" repo.

#### Spring

Our modifications to Spring are [here](https://github.com/OpenNMS/spring-framework).

It contains a few cherry-picked backports from [the upstream Spring 5.x](https://github.com/spring-projects/spring-framework/tree/5.3.x).

#### Spring Security

Our modifications to Spring Security 4.x are [here](https://github.com/OpenNMS/spring-security/tree/4.2.x)

It contains a few cherry-picked backports from [the upstream Spring Security 5.x](https://github.com/spring-projects/spring-security/tree/5.8.x).

#### Servicemix Versions

We don't actually use the Spring and Spring Security jars directly, we use the Apache Servicemix versions. So instead of drastically changing the upstream builds, we have instead make forks of them as well.

For Spring Security, we just [updated the upstream Servicemix releases to point to our new versions of the jars]().

For Spring, it was simpler to make [a small project that generates new OSGi-friendly jars](https://github.com/OpenNMS/opennms-spring-patched) overlaying the patched files from our Spring fork on top of the original jars.

## Karaf

### Design (Now)

### Design (Future)

### Features Files

### Understanding Runtime/Smoke Test Failures

which logs matter
how to read the logs
how to reproduce locally (if you can)

### TODO: Spring, ActiveMQ, Camel, Oh My!

## Docker

where the different bits live
how to update the core/base OS
etc.

DCT !!!


Dependabot: team?

--------------------------------------------------------------------------------
