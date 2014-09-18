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
# _make changes_
git commit -a -m 'I made a cool new thing!'
# _fix bug_
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

Where to Put Your Code
----------------------

* Critical Bug Fixes: `1.12` (merged to `rc/stable/1.14.0`)
* 1.14 Blocker Bug Fixes: `rc/stable/1.14.0` (merged to `develop`)
* Anything Else: `feature/XXXX` or `jira/XXXX` branch, then merge --no-ff to `develop` when complete

[GitFlow]: http://nvie.com/posts/a-successful-git-branching-model/ "GitFlow"
