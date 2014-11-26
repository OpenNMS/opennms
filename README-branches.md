OpenNMS Branch Management with GitFlow
======================================

As of OpenNMS 14, we moved to using "GitFlow" for managing braches.  Please read
the [GitFlow] page for an overview of how GitFlow works.  It is largely the same
as the way we have already dealt with our "unstable" release tree, but instead
uses a so-called "guarded master".

Development
-----------

Development occurs on the `develop` branch.  This branch behaves very much like
`master` had traditionally in OpenNMS development, although we ask for more
discipline when committing to the `develop` branch.  (Creating a feature or
bugfix branch first, and then merging it back into `develop` rather than
making a commit directly.)

The `develop` branch should always be _releasable_, ie, it should always be in
a state where no feature or fix is merged to it until it is expected to run and
pass tests.  Snapshot builds are made from the `develop` branch.

Master
------

The `master` branch should never be committed to directly.  Instead, when it
is time to do a release, a release branch is created off of the `develop`
branch.  When this release branch has been vetted and tested, it gets merged
to `master` and then tagged.  That way, `master` will always contain a
known-good, released set of code.

Features and Bugfixes
---------------------

Any new features or bugfixes should be done in a branch made off of the
`develop` branch.  When tests pass, you then merge your feature branch back
into the main `develop` branch.

For example:

```
git fetch
git checkout -b features/cool-new-thing origin/develop
# *make changes*
git commit -a -m 'I made a cool new thing!'
# *fix bug*
git commit -a -m 'Whups, test ABC did not pass because I forgot something'
# unit tests pass
git checkout develop
git pull
git merge --no-ff features/cool-new-thing
git branch -d features/cool-new-thing
git push
```

Critical Bugs/Hotfix Releases
=============================

If you find a critical bug that should go out before the next release cycle
(ie, you've found a 14.0.0 bug that deserves fixing immediately, rather than
waiting for 15.0.0 to come out), then submit a patch or make a pull request
based on `master`. Once it passes all unit tests and is code-reviewed and
tested, a new `hotfix` release branch will be created from master, the
critical fix will be applied, and then a new version (eg, 14.0.1) will be
released.

Submitting Code (for Non-Committers)
====================================

If you don't have commit access to OpenNMS and wish to submit a patch, the
best way to do so is through GitHub's pull request mechanism.

If you haven't done so already, fork the OpenNMS repository and create a
branch based on the OpenNMS `develop` branch.  Then make your changes and
submit a pull request back to the OpenNMS `develop` branch.  We'll review
the code, and if you already have filled out an [OCA], merge it.

OpenNMS Packages (RPM & Debian)
===============================

Described below is where the various packages built from the GitFlow
branches are located:

* stable: latest `master` release (`14.0.0`)
* testing: latest `master` branch snapshot (`14.0.0`)
* snapshot: latest `develop` branch snapshot (`15.0.0-SNAPSHOT`)

Future Release Cycle
--------------------

When it is time to make a `15.0.0` release, we will branch `develop` to a
new `release-15.0.0` branch.  At that point, the repositories will change
slightly to include the release branch in addition to the normal `develop`
snapshots:

* stable: latest `master` release (`14.0.0`)
* testing: latest `release-15.0.0` branch snapshot (`15.0.0-SNAPSHOT`)
* snapshot: latest `develop` branch snapshot (`16.0.0-SNAPSHOT`)

[GitFlow]: http://nvie.com/posts/a-successful-git-branching-model/ "GitFlow"
[OCA]: http://www.opennms.org/wiki/OCA "OpenNMS Contributor Agreement"
