
# Welcome to OpenNMS

This short document give you hints how to contribute and help us to build THE network management platform developed under the open source model.
To help improve the software we use the [GitHub Fork & Pull](https://help.github.com/articles/using-pull-requests/) workflow.
It is required to sign an [OpenNMS Contributor Agreement (OCA)](https://www.opennms.org/wiki/Contributor_Agreement) to get your code into the release.

The software itself managed in [JIRA](http://issues.opennms.org) and we use [Bamboo](http://bamboo.internal.opennms.com:8085) as continuous integration system.
Please add the JIRA reference number as a reference to your pull request so we can track an manage your contribution in our software release management.
Your Pull Requests have to be reviewed and has to pass tests before they can get merged from an OGP or OpenNMS Group member.

To work with the source code we have more detailed resources:

* [OpenNMS](http://www.opennms.org/)
* [How to contribute](http://www.opennms.org/wiki/How_to_contribute)
* [Developing with Git](https://www.opennms.org/wiki/Developing_with_Git)
* [OCA](http://www.opennms.org/wiki/OCA)
* [Building OpenNMS](http://www.opennms.org/wiki/Building_OpenNMS)
* [Eclipse](http://www.opennms.org/wiki/Eclipse)
* [IntelliJ IDEA](https://www.opennms.org/wiki/IDEA_and_OpenNMS)

## OpenNMS Repository Branch Workflow

In your local git repository, the branches described below are accessible as "remote" branches, i.e., branches that reference a location on a remote repository (in this case, the OpenNMS git repository).

Here are the currently active branches, and their uses:

### foundation

_Foundation_ is a branch based on OpenNMS Horizon 14, plus a number of additional bug fixes as well as the GUI rework that went into Horizon 15.
It is the basis of OpenNMS Meridian 2015.x.

### foundation-2016

Foundation 2016 is a branch based on OpenNMS Horizon 17.
It will be the basis of OpenNMS Meridian 2016.x.

### release-*

If there is an active release pending (ie, Horizon 17.0.1, for example) there will be a "release-XX.X.X" branch created.
This branch should only receive bugfixes, and then be removed after release.

### develop

Future development of features that will go into OpenNMS 18.

## Merge Schedule

Bamboo is configured to auto-merge code from branch to branch so you should only have to make changes in the "oldest" relevant branch.

    foundation -> foundation-2016

Only critical security or bug fixes should go into Foundation.
These will auto-merge forward to the 2016 Foundation branch, for inclusion in future stable Horizon and Meridian 2016 releases.

    foundation-2016 -> release-XX.X.X

Foundation 2016 will auto-merge-forward to a release branch, if one is active, so that security and bug fixes from Foundation and Foundation 2016 automatically make it into the next release.

    release-XX.X.X -> develop

If a release branch is active, it will be automatically merged forward to develop, so that all bug fixes and changes make it into future releases.

    foundation-2016 -> develop

If a release branch is not active, Foundation 2016 is automatically merged forward to develop, so that bug and security fixes make it into future releases.

    foundation-2016 -> Meridian develop

Foundation 2016 is also merged automatically into the Meridian development codebase, to become part of a future Meridian release. Meridian develop is a very thin set of changes on top of foundation-2016, mostly for branding and a few default configuration changes.

## Where to Merge Code

In short, here is what you should branch from if you are writing new code:

*Critical Security or Bug Fixes:*

    foundation

*Non-Blocker Bug Fixes:*

    foundation-2016

*New Features:*

    develop

# Contributor Code of Conduct

As contributors and maintainers of this project, and in the interest of fostering an open and welcoming community, we pledge to respect all people who contribute through reporting issues, posting feature requests, updating documentation, submitting pull requests or patches, and other activities.

We are committed to making participation in this project a harassment-free experience for everyone, regardless of level of experience, gender, gender identity and expression, sexual orientation, disability, personal appearance, body size, race, ethnicity, age, religion, or nationality.

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery
* Personal attacks
* Trolling or insulting/derogatory comments
* Public or private harassment
* Publishing other's private information, such as physical or electronic addresses, without explicit permission
* Other unethical or unprofessional conduct
* Project maintainers have the right and responsibility to remove, edit, or reject comments, commits, code, wiki edits, issues, and other contributions * that are not aligned to this Code of Conduct, or to ban temporarily or permanently any contributor for other behaviors that they deem inappropriate, threatening, offensive, or harmful.

By adopting this Code of Conduct, project maintainers commit themselves to fairly and consistently applying these principles to every aspect of managing this project. Project maintainers who do not follow or enforce the Code of Conduct may be permanently removed from the project team.

This Code of Conduct applies both within project spaces and in public spaces when an individual is representing the project or its community.

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting a project maintainer at abuse@opennms.org. All complaints will be reviewed and investigated and will result in a response that is deemed necessary and appropriate to the circumstances. Maintainers are obligated to maintain confidentiality with regard to the reporter of an incident.

This Code of Conduct is adapted from the Contributor Covenant, version 1.3.0, available from http://contributor-covenant.org/version/1/3/0/
