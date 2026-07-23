#!/usr/bin/env bash
# Shared helpers for makedeb.sh and makerpm.sh.
# Expects TOPDIR to already be set by the caller, and BRANCH/COMMIT/RELEASE_MAJOR
# to be set (even if empty) before extraInfo()/extraInfo2() are called.

function exists() {
    which "$1" >/dev/null 2>&1
}

function use_git() {
    exists git && test -d "${TOPDIR}/.git"
}

function run()
{
    if exists "$1"; then
        "$@"
    else
        die "Command not found: $1"
    fi
}

function die()
{
    echo "$@" 1>&2
    exit 1
}

function tell()
{
    echo -e "$@" 1>&2
}

function calcMinor()
{
    if use_git; then
        git log --pretty='format:%cd' --date=short | sort -u -r | head -n 1 | sed -e 's,^Date: *,,' -e 's,-,,g'
    else
        date '+%Y%m%d'
    fi
}

function branch()
{
    if [ -n "${BRANCH}" ]; then
        echo "${BRANCH}"
    elif [ -n "${BAMBOO_OPENNMS_BRANCH_NAME}" ]; then
        echo "${BAMBOO_OPENNMS_BRANCH_NAME}"
    elif [ -n "${bamboo_planRepository_branch}" ]; then
        echo "${bamboo_planRepository_branch}"
    elif use_git; then
        run git branch | grep -E '^\*' | awk '{ print $2 }'
    else
        echo "source"
    fi
}

function commit()
{
    if [ -n "${COMMIT}" ]; then
        echo "${COMMIT}"
    elif [ -n "${bamboo_repository_revision_number}" ]; then
        echo "${bamboo_repository_revision_number}"
    elif use_git; then
        run git log -1 | grep -E '^commit' | cut -d' ' -f2
    else
        echo ""
    fi
}

function extraInfo()
{
    branchname="$(branch)"
    if [ -n "${branchname}" ]; then
        if [ "$RELEASE_MAJOR" = "0" ] ; then
            echo "This is an OpenNMS build from the ${branchname} branch.  For a complete log, see:"
        else
            echo "This is an OpenNMS build from Git.  For a complete log, see:"
        fi
    else
        echo "This is an OpenNMS build from source."
    fi
}

function extraInfo2()
{
    commithash="$(commit)"
    if [ -n "${commithash}" ]; then
        echo "  https://github.com/OpenNMS/opennms/commit/${commithash}"
    else
        echo "  (unknown commit)"
    fi
}

function version()
{
    grep '<version>' pom.xml | \
        sed -e 's,^[^>]*>,,' -e 's,<.*$,,' -e 's,-[^-]*-SNAPSHOT$,,' -e 's,-SNAPSHOT$,,' -e 's,-testing$,,' -e 's,-,.,g' | \
        head -n 1
}

function opa_version()
{
    grep '<opennmsApiVersion>' pom.xml | \
    sed -e 's,^[^>]*>,,' -e 's,<.*$,,' -e 's,-[^-]*-SNAPSHOT$,,' -e 's,-SNAPSHOT$,,' -e 's,-testing$,,' -e 's,-,.,g' | \
    head -n 1
}

function skipCompile()
{
    if $ASSEMBLY_ONLY; then echo 1; else echo 0; fi
}

function enableSnapshots()
{
    if $ENABLE_SNAPSHOTS; then echo 1; else echo 0; fi
}
