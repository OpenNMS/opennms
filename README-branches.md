OpenNMS Branch Management with GitFlow
======================================

As of OpenNMS 1.14, we are moving to using "GitFlow" for managing braches.
Please read the [GitFlow] page for an overview of how GitFlow works.  It is
largely the same as the way we have already dealt with our "unstable" release
tree, but instead uses a so-called "guarded master".

Development
-----------

Development will occur on the `develop` branch.  This branch will behave very
much like `master` had traditionally in OpenNMS development, although we ask
for more discipline when committing to the `develop` branch.  (Creating a feature
or bugfix branch first, and then merging it back into `develop` rather than
making a commit directly.)

The `develop` branch should always be _releasable_, ie, it should always be in
a state where no feature or fix is merged to it until it is expected to run and
pass tests.  Snapshot builds will be made from the `develop` branch.

Master
------

The `master` branch will never be committed to directly.  Instead, when it
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
git checkout develop
git pull
git merge --no-ff features/cool-new-thing
git branch -d features/cool-new-thing
git push
```

Legacy Branch Management During the Transition to GitFlow
=========================================================

The goal is to move to a more fluid release schedule, where we make "stable"
releases early and often, rather than having 1- or 2-year cycles between releases.
(Especially since our "unstable" `master` has traditionally been generally pretty
stable.)

However, for now we still have the `1.12` branch to manage until 1.14 stabilizes.
For this period of time while 1.12.x is still supported, we will treat the `1.12`
branch as a sort of mini-`develop`.  Critical bug fixes should be done in the
`1.12` branch and then merged to the `rc/stable/1.14.0` branch.

Active Branches
---------------

* `1.12`: contains version `1.12.10-SNAPSHOT`, ie, future `1.12.10` release
* `rc/stable/1.14.0`: contains version `1.14.0-SNAPSHOT`, ie, future `1.14.0` release
* `develop`: contains version `1.15.0-SNAPSHOT`, ie, the place where new features and
  non-blocker bugfixes should go
* `master`: Currently still has a snapshot version in it, but going forward **only
  tagged releases merge here**

Where to Put Your Code
----------------------

* Critical Bug Fixes: `1.12` (merged to `rc/stable/1.14.0`)
* 1.14 Blocker Bug Fixes: `rc/stable/1.14.0` (merged to `develop`)
* Anything Else: `feature/XXXX` or `jira/XXXX` branch, then merge --no-ff to `develop` when complete

OpenNMS Packages (RPM & Debian)
===============================

Described below is where the various packages built from the GitFlow branches will be located.

Legacy Transition
-----------------

During the legacy transition, the OpenNMS package repositories will be set up as follows:

* stable: latest stable release (`1.12.9`)
* testing: latest `1.12` branch snapshot (`1.12.10-SNAPSHOT`)
* unstable: latest unstable release (`1.13.4`)
* snapshot: latest `rc/stable/1.14.0` branch snapshot (`1.14.0-SNAPSHOT`)
* bleeding: latest `develop` branch snapshot (`1.15.0-SNAPSHOT`)

Future
------

When the legacy transition is finished and `1.14.0` is released, the repositories will be as follows:

* stable: latest `master` release (`1.14.0`)
* testing: latest `develop` branch snapshot (`1.15.0-SNAPSHOT`)
* unstable: obsolete, redirect to `stable` (`1.14.0`)
* snapshot: latest `develop` branch snapshot (`1.15.0-SNAPSHOT`)
* bleeding: obsolete, redirect to `snapshot` (`1.15.0-SNAPSHOT`)

Future Release Cycle
--------------------

When it is time to make a `1.15.0` release, we will branch `develop` to a new `rc/stable/1.15.0` branch.  At that point, the repositories will change slightly to include the rc branch in addition to the normal `develop` snapshots:

* stable: latest `master` release (`1.14.0`)
* testing: latest `rc/stable/1.15.0` branch snapshot (`1.15.0-SNAPSHOT`)
* unstable: obsolete, redirect to `stable` (`1.14.0`)
* snapshot: latest `develop` branch snapshot (`1.16.0-SNAPSHOT`)
* bleeding: obsolete, redirect to `snapshot` (`1.16.0-SNAPSHOT`)


[GitFlow]: http://nvie.com/posts/a-successful-git-branching-model/ "GitFlow"
