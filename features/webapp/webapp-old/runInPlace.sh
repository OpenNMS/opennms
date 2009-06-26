#!/bin/sh

function err() {
    echo "$@" 1>&2
}

function warInPlace() {
    ../build.sh compile war:inplace
}

function runInPlace() {
    local PORT=$1
    ../build.sh -Dweb.port=$PORT jetty:run
}

function removeGwtModuleFiles() {
    local MOD=$1
    local TOP=`pwd`
    local WEBDIR=$TOP/src/main/webapp
    (
    cd target/$MOD
    for i in *; do
	rm -f $WEBDIR/$i
    done
    )
}

function removeGwtFiles() {
    removeGwtModuleFiles org.opennms.dashboard.Dashboard

    rm -f src/main/webapp/*.cache.*
    rm -f src/main/webapp/*.nocache.*
    rm -f src/main/webapp/gwt.js
    rm -f src/main/webapp/hosted.html
    rm -f src/main/webapp/history.html

}

function removeCode() {
    rm -rf src/main/webapp/WEB-INF/lib
    rm -rf src/main/webapp/WEB-INF/classes
    rm -rf src/main/webapp/META-INF
}

function clean() {
    removeCode
    removeGwtFiles
    rm -f src/main/webapp/WEB-INF/version.properties
    rm -f src/main/webapp/WEB-INF/configuration.properties

    ../build.sh clean
}

function usage() {
    err "usage: runInPlace.sh [-hbcCgn] "
    err "\t-h : print this help"
    err "\t-C : clean up completely"
    err "\t-b : build in place before running"
    err "\t-c : remove WEB-INF/lib, WEB-INF/classes and META_INF dirs"
    err "\t-g : remove gwt generated files"
    err "\t-n : no-run: this is useful if you want to only build in place"
    err "\t-p portnum : the port to run the webapp at"
    exit 1
}

WAR_INPLACE=false
RUN_INPLACE=true
REMOVE_GWT_FILES=false
REMOVE_CODE=false
CLEAN=false
WEB_PORT=8080

while getopts bcChgnp: OPT; do
    case $OPT in
	b)  WAR_INPLACE=true
	    ;;
	c)  REMOVE_CODE=true
	    ;;
	C)  CLEAN=true
	    ;;
	g)  REMOVE_GWT_FILES=true
	    ;;
	n)  RUN_INPLACE=false
	    ;;
	p)  WEB_PORT=$OPTARG
	    ;;
	*)  usage
	    ;;
    esac
done

if $CLEAN; then
    clean
    exit 0
fi

if ! $WAR_INPLACE && [ ! -e src/main/webapp/WEB-INF/configuration.properties ]; then
    err "No configuration.properties found in place. You must  build in place first"
    err "Try running with the '-b' option.  This normally only needs to be done once."
    if $RUN_INPLACE; then
	exit 1
    fi
fi

if $WAR_INPLACE; then
    warInPlace
fi

if $REMOVE_GWT_FILES; then
    removeGwtFiles
fi

if $REMOVE_CODE; then
    removeCode
fi

if $RUN_INPLACE; then
    runInPlace $WEB_PORT
fi


