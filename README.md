OpenNMS Prime
=============

OpenNMS Prime is a stable release of OpenNMS based on the open-source OpenNMS release.

Conventions
===========

Since we will be working in 2 separate git repositories, this document will specify which repository is being referred to by prefacing it with either `OpenNMS:` for the public upstream OpenNMS git repo, or `Prime:` for the private OpenNMS Prime git repo.  For example, when referring to OpenNMS upstream's `develop` branch, it will always be referred to as `OpenNMS:develop`, whereas the OpenNMS Prime `develop` branch will be referred to as `Prime:develop`.

Branch Management
=================

It wouldn't be a README without discussion of branch management.... ;)

OpenNMS Prime development will generally be one of three kinds of commits:

1. Bug fixes
2. New features that should be shared with OpenNMS upstream
3. New features that will be unique to OpenNMS Prime

Bug Fixes
---------

Bug fixes should be made by making the appropriate fix and applying it to the `OpenNMS:develop` branch in OpenNMS upstream, as normal.  If a bug is deemed important enough to be merged to OpenNMS Prime, it should be cherry-picked back to the `OpenNMS:foundation` branch, which will merge into Prime's `Prime:develop` branch through Bamboo.

New Upstream+Prime Shared Features
----------------------------------

If there is a feature being developed that can't wait for a major Prime revision and is deemed low-impact (ie, a new Detector), it should be made in a feature branch based on `OpenNMS:foundation`.  When the feature is complete and tested, it should be merged to `OpenNMS:foundation`, which will merge inte OpenNMS upstream's `OpenNMS:develop` branch as well as Prime's `Prime:develop` branch.

New Prime-Only Features
-----------------------

If there is a feature being developed specifically for Prime (ie, a theme editor for creating OpenNMS Prime color schemes), it should be made in a feature branch based on `Prime:develop`.  When the feature is complete and tested, it should be merged back to `Prime:develop`.
