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

## Git

### `git gone`

If you, like me, end up having to bounce around a bunch of branches, including ones that go away once issues get closed.
I found a handy little git commandlet that someone wrote long ago, and I've used the heck out of it ever since.
You can download it [at this gist](https://gist.github.com/RangerRick/ef3626a6682b09fd298691fa586d256c) and stick it somewhere in your `$PATH`.

Running `git gone -d` will delete any local branches that have been removed remotely and don't have any changes that didn't end up in mainline.
Running `git gone -D` will delete them, even if those changes never got merged (be careful!).

### `git rebase -i`

Until I turn my branch into a pull-request (at which point I consider a branch write-only), I use `git rebase -i` judiciously to clean up my branches and try make my commits a series of logical steps.

I will often make small changes trying to figure out a build failure, committing little wip bits until the build goes green.
Once that happens (or sometimes before), I run `git rebase -i` and reorder and clean up all those WIP commits into something easier to review.
For example, see [this PR](https://github.com/OpenNMS/opennms/pull/5900/commits) that updates CXF.
I did a whole lot of fiddling as I got it working, but in the end I reordered and combined commits until they turned into 4 useful self-contained changes.

Start by running `git rebase -i <parent>` where the "parent" is usually the foundation or other branch you branched from.
In the case of the CXF PR above, that parent was `foundation-2023`.
So when I was working on that branch, I would `git rebase -i origin/foundation-2023` and git would open up an editor that looks like this:

```
pick bf442061eae fix some karaf bundle start-level issues
pick 0720310e9cc NMS-15065: fix swagger parsing of old Castor-style classes
pick cb8cdd2043e NMS-15065: clean up jackson2 dependencies
pick 859b5c2a5f9 NMS-15065: bump CXF to 3.3.5

# Rebase f036c5b5848..859b5c2a5f9 onto f036c5b5848 (4 commands)
#
# Commands:
# p, pick <commit> = use commit
# r, reword <commit> = use commit, but edit the commit message
# e, edit <commit> = use commit, but stop for amending
# s, squash <commit> = use commit, but meld into previous commit
# f, fixup [-C | -c] <commit> = like "squash" but keep only the previous
#                    commit's log message, unless -C is used, in which case
#                    keep only this commit's message; -c is same as -C but
#                    opens the editor
# x, exec <command> = run command (the rest of the line) using shell
# b, break = stop here (continue rebase later with 'git rebase --continue')
# d, drop <commit> = remove commit
# l, label <label> = label current HEAD with a name
# t, reset <label> = reset HEAD to a label
# m, merge [-C <commit> | -c <commit>] <label> [# <oneline>]
#         create a merge commit using the original merge commit's
#         message (or the oneline, if no original merge commit was
#         specified); use -c <commit> to reword the commit message
# u, update-ref <ref> = track a placeholder for the <ref> to be updated
#                       to this position in the new commits. The <ref> is
#                       updated at the end of the rebase
#
# These lines can be re-ordered; they are executed from top to bottom.
#
# If you remove a line here THAT COMMIT WILL BE LOST.
#
# However, if you remove everything, the rebase will be aborted.
#
```

If I decide that karaf bundle cleanup didn't need to happen, I could just delete the line entirely.
That change would disappear into the aether, and when you save from your editor the history would look like the "fix swagger parsing" commit comes right after the newest `foundation-2023` commit.

Conversely, if I decided that change was actually directly related to the `NMS-15065` work, rather than just a small fix I did before working on the bulk of my changes, I might change the work `pick` to the word `reword` to be able to add "`NMS-15065: `" to the front of the commit's description.

But _mostly_ what I do, is use the `fixup` command.
Every line where you change `pick` to `fixup` squashes that commit with the one above it.
If I wanted to, I could turn this all into one mega-commit with a good description by changing the editor to say the following:

```
reword bf442061eae fix some karaf bundle start-level issues
fixup 0720310e9cc NMS-15065: fix swagger parsing of old Castor-style classes
fixup cb8cdd2043e NMS-15065: clean up jackson2 dependencies
fixup 859b5c2a5f9 NMS-15065: bump CXF to 3.3.5
```

When you save from your editor, it will ask for the new commit message for the first commit (to, say `NMS-15065: bump CXF to 3.3.5 and fix some dependencies`), and the smush the rest together with it.
I do this a _ton_ with my little work-in-progress commits, combining them into something that's way more readable.

This goes a long way to making the code easier to read.

NOTE: If you rewrite the history in this way, you will _have_ to do `git push --force` rather than a `git push` to see your branch changes in the OpenNMS repository.
This works when you're off on a feature branch, but force-pushing is (rightly) disabled in our main `foundation` and `release` branches.

### `git log --no-merges`

Our branch-management is very handy for merging code around to the places it needs to be, but it also adds a lot of noise to the git logs.

You can add `--no-merges` to your `git log` command to skip the noise and only show "real" commits.

## Maven

### Partial Builds

OpenNMS's source code is massive and complicated.
You can use some of these techniques to avoid spending a long amount of time waiting for compiles while just working on a small part of the code.

#### Only Build the Bits You Need

While doing development, it's often convenient to (re)build only part of the tree.
To do so, you need a combination of the `--projects` argument, to tell it what project(s) you care about, and then `--also-make` (`-am`) or `--also-make-dependents` (`-amd`).

For example, if you just did a clean checkout, and want to work on the code in the `opennms-dao` project, make a note of the artifact ID and then use `-am` like so:

```bash
./compile.pl -DskipTests=true --projects :opennms-dao -am install
```

The full name of the `opennms-dao` project is `org.opennms:opennms-dao` but as long as the artifact ID is unique, you don't have to bother typing the first part.

#### Build the Bits That Depend on the Bits You Changed

Alternately, if you've made some changes, and only want to build and/or run tests on the subset of things that depend on your changed project, you can use `-amd` to build anything that depends (even indirectly) on the project you specify:

```bash
./compile.pl -t --projects :opennms-dao -amd install
```

#### Build the Bits that Match a Search

There is a tool called `tools/development/pom-artifact.sh` that can turn a `pom.xml` file into a `groupId:artifactId` tuple, which you can then use when passing things to `--projects`.

A separate tool wraps it called `tools/development/grep-pom-artifact.sh` which performs a `git grep`, determines the related `pom.xml` files associated with any code that the grep matches, and then passes those poms to `pom-artifact.sh`.
This allows you to do powerful things like, "I just updated the dependency for jdom, rebuild anything that uses jdom to make sure it still passes."

```bash
./compile.pl -DskipTests=true --projects `tools/development/grep-pom-artifact.sh -i jdom` install
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

Our modifications to Spring Security 4.x are [here](https://github.com/OpenNMS/spring-security/tree/4.2.x).

It contains a few cherry-picked backports from [the upstream Spring Security 5.x](https://github.com/spring-projects/spring-security/tree/5.8.x).

#### Servicemix Versions

We don't actually use the Spring and Spring Security jars directly, we use the Apache Servicemix versions. So instead of drastically changing the upstream builds, we have instead make forks of them as well.

For Spring Security, we just [updated the upstream Servicemix releases to point to our new versions of the jars](https://github.com/OpenNMS/servicemix-bundles/tree/spring-security-4.2.21).

For Spring, it was simpler to make [a small project that generates new OSGi-friendly jars](https://github.com/OpenNMS/opennms-spring-patched) overlaying the patched files from our Spring fork on top of the original jars.

## Karaf

OpenNMS's architecture is a bit odd, as it was first developed before Karaf (or even Spring!) existed.
Over time we have adopted newer technologies, but there is still a lot of older code that doesn't fit into the newer design, most notably our web UI infrastructure.

Since the web UI code is large and tied closely to a large amount of Spring Security configuration, rather than attempting to pull the entirety of the OpenNMS codebase inside Karaf, we decided to embed Karaf at a level _higher_ than the webapp as far as Spring contexts go so that the core of OpenNMS can be "pre-initialized" before extending it within Karaf.

### Design (Now)

Our Karaf infrastructure is built in a series of steps.

#### Features

All Karaf features files (collections of OSGi bundles and config) are defined in the [Karaf Features project](container/features/pom.xml).

* Third-party dependencies and other base features are defined in the [features-core.xml](container/features/src/main/resources/features-core.xml) (starting in `foundation-2022` and higher)
* Main code features are defined in the [features.xml](container/features/src/main/resources/features.xml)
* Minion features are defined in the [features-minion.xml](container/features/src/main/resources/features-minion.xml)
* Sentinel features are defined in the [features-sentinel.xml](container/features/src/main/resources/features-sentinel.xml)

#### Core

The Horizon core Karaf container is built in the ["karaf" container project](container/karaf/pom.xml), which uses the `karaf-maven-plugin` to create a Karaf assembly.
This builds only an "opennms-flavored" Karaf tarball, which is later augmented with additional features getting pulled into the `system/` directory.
The final assembly is built in the [full assembly](opennms-full-assembly/pom.xml).

#### Minion and Sentinel

Minion and Sentinel share a basic Karaf assembly built in the ["shared" container project](container/shared/pom.xml), which uses the `karaf-maven-plugin` to create a Karaf assembly.

They each add additional features to the `system/` directory with a series of projects, culminating in a final assembly in the ["minion"](opennms-assemblies/minion/pom.xml) and ["sentinel](opennms-assemblies/sentinel/pom.xml) projects.

### Design (Future)

Going forward, _any_ future new features should ideally be written as OSGi bundles and loaded into Karaf from features files.

The existing infrastructure that builds the core, Minion, and Sentinel assemblies is overly complicated. It involves making base Karaf assemblies, and then overlaying a bunch of extra `system/` files and a few configs on top of those assemblies.

I have been working to convert our build to just 2 assemblies: one to make a full Karaf install with all necessary dependencies inside the `system/` directory (in `features/containers/<blah>`), and then one very small one that turns that into an install tarball (with init/systemd files, etc.) (in `opennms-assemblies/<blah>`).

### Working with Features Files

Features files themselves are actually [pretty well-documented in Karaf's manual](https://karaf.apache.org/manual/latest/provisioning).

A few things to note that are useful, you will find them sprinkled around our features files:

* `<bundle dependency="true">` does not do what you might think it does based on the name.
  What it means is: "If something is asking for an export that this bundle provides, check if it's already provided somewhere else. If it is, don't load this bundle."
  The place we use this the most is in our hacked up OpenNMS core runtime, where we share a bunch of code from the main JVM's classloader into Karaf, through [custom.properties](container/karaf/src/main/filtered-resources/etc/custom.properties).
* By default, a feature is loaded by parsing the feature and any features it depends on, and just dumping all of the relevant bundles into the loader.
  Some bundles will "load" before things they depend on, and will stay in GracePeriod status until everything is resolved.
  This usually works fine, but sometimes things don't load/register correctly (or time out).
  In those cases, you can add `prerequisite="true"` to a `<feature>` include, to make sure that parent feature is loaded completely _before_ it tries loading things specified in the current feature.
* There are a few common start levels defined in [the `container/features/pom.xml` file](container/features/pom.xml) to make it easier to specify things that should come up early vs. late.
  In most cases, features resolve themselves eventually, but you can use these to give a hint to Karaf to give preference to things you know should be initialized early.

### Understanding Runtime/Smoke Test Failures

Most Karaf-related failures show up as a smoke test timing out.
To debug these, the main smoke-test output is rarely useful.
Instead, you will want to switch over to the "Artifacts" tab in the CircleCI results, and look for the `karaf.log` file in the test that's failing.
If it's a Sentinel or Minion test, there might be multiple `karaf.log` files to look at.
You can often just search for the word "exception" in those logs and it'll get you to the _real_ failure.

A Karaf error almost always looks something like this:

```
2024-04-11T17:11:05,873 | ERROR | Karaf-Extender-Feature-Install | KarafExtender                    | 129 - org.opennms.container.extender - 31.0.3.SNAPSHOT | Failed to install one or more features.
org.apache.felix.resolver.reason.ReasonException: Unable to resolve root: missing requirement [root] osgi.identity; osgi.identity=sentinel-flows; type=karaf.feature; version="[31.0.3.SNAPSHOT,31.0.3.SNAPSHOT]"; filter:="(&(osgi.identity=sentinel-flows)(type=karaf.feature)(version>=31.0.3.SNAPSHOT)(version<=31.0.3.SNAPSHOT))" [caused by: Unable to resolve sentinel-flows/31.0.3.SNAPSHOT: missing requirement [sentinel-flows/31.0.3.SNAPSHOT] osgi.identity; osgi.identity=opennms-dnsresolver-netty; type=karaf.feature [caused by: Unable to resolve opennms-dnsresolver-netty/31.0.3.SNAPSHOT: missing requirement [opennms-dnsresolver-netty/31.0.3.SNAPSHOT] osgi.identity; osgi.identity=org.opennms.features.dnsresolver.netty; type=osgi.bundle; version="[31.0.3.SNAPSHOT,31.0.3.SNAPSHOT]"; resolution:=mandatory [caused by: Unable to resolve org.opennms.features.dnsresolver.netty/31.0.3.SNAPSHOT: missing requirement [org.opennms.features.dnsresolver.netty/31.0.3.SNAPSHOT] osgi.wiring.package; filter:="(&(osgi.wiring.package=org.xbill.DNS)(version>=2.1.0)(!(version>=3.0.0)))"]]]
	at org.apache.felix.resolver.Candidates$MissingRequirementError.toException(Candidates.java:1341) ~[?:?]
...
```

...you generally need to read these backwards, from the end of the "Unable to resolve root" line back.

In this example, something expects to find `org.xbill.DNS` (ie, a jar bundle that exports classes that use `package org.xbill.DNS` in their code), with the restriction that it should be versioned greater-than-or-equal-to `2.1.0`, and _not_ greater-than-or-equal-to `3.0.0`.

If you read back one step, `org.opennms.features.dnsresolver.netty/31.0.3.SNAPSHOT` is the thing that was looking for it.
If you keep reading back, _that_ bundle was pulled in by a feature called `opennms-dnsresolver-netty` versioned `31.0.3.SNAPSHOT`.
Now you can look for the `opennms-dnsresolver-netty` feature in [the `features/container/src/main/resources/` directory](container/src/main/resources/) and fix its dependencies.
In this particular bit of code, I had some things that wanted version 3+, and some that wanted 2.x, so I reverted our updates to `dnsjava` to version `2.1.9` for our build.

### Future Refactoring: Spring, ActiveMQ, Camel, Oh My!

There are a bunch of long-term refactoring goals that still need work, to address potential security and other issues.

Our worst Spring and other security issues are handled by us having special backports of security fixes to OpenNMS-specific forks of those jars.
This helps us in the short-term, but ultimately we need to uplift the core to be using the latest Spring, Hibernate, Karaf, ActiveMQ, and Camel.

The unfortunate thing is that a lot of these are cross-dependent, so you can only update them in increments.
For example, Camel 2.x is the last version that supports Spring 4, if we wanted to move forward, we'd also need to update Spring.
ActiveMQ is similarly tied to both Spring and Camel.

We have taken a number of shots at updating these incrementally, and that work is partially complete, most notably the recent update to Camel.
We aren't at the _latest_ Camel 2.x, but the only remaining known CVEs exercise Camel code we don't use.

We will need to continue to work on these uplifts, a little at a time.
First, moving from Spring 4.2.x to 4.3.x (the last attempt had some strange failures, but might be better with some of the recent Karaf work).
Then, updating ActiveMQ and Camel.

Another large thing that will eventually hit us is when it's time to move to the latest servlet and related APIs.
Whole packages have been moved around (to `jakarta.*`) and a lot of things need to change at once.
It might be easier to work to eliminate web bits other than CXF/ReST and move to a more dynamic HTML/JS frontend than to try to move all of our Servlet/JSP stuff forward.

## Docker

Docker containers as of `foundation-2023` (Meridian 2023) are all based on images built in [the deploy-base project](https://app.circleci.com/pipelines/github/OpenNMS/deploy-base).

### The Variants

There are 2 branches:
* `master`: Ubuntu deploy base, used by `foundation-2023`
* `variant/ubi`: RedHat UBI deploy base, used by `foundation-2024` and up (including Horizon 33 and up)

They basically perform the same function, just with a different core OS.
The move to UBI was in the hopes of making it easier to eventually get OpenShift certification of our Helm charts.

### Updating the Images

To update the images, first update the deploy-base.

You can do this by checking out the project's default `master` branch, bumping the version in the `version.txt` file, and committing and pushing.
Then, merge the `master` branch into `variant/ubi` and push the changes.
If you're only bumping the version, it should merge cleanly.
This will create new builds for both variants and push them to dockerhub.
You can find the latest build numbers by checking the [ubuntu](https://hub.docker.com/r/opennms/deploy-base/tags?name=ubuntu) and [ubi9](https://hub.docker.com/r/opennms/deploy-base/tags?name=ubi9) tags.

Then, for each relevant branch (`foundation-2023`, `foundation-2024`, etc.), update the deploy-base reference in the following files to match the appropriate build number:

* [common.mk](opennms-container/common.mk)
* [core Dockerfile](opennms-container/core/Dockerfile)
* [minion Dockerfile](opennms-container/minion/Dockerfile)
* [sentinel Dockerfile](opennms-container/sentinel/Dockerfile)
* [smoke test](smoke-test/src/main/java/org/opennms/smoketest/containers/MockCloudContainer.java)

Someday we'll get around to parameterizing this so it's defined in a single file. 😅