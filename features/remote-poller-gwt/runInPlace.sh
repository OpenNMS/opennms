#!/bin/bash
#*******************************************************************************
# This file is part of the OpenNMS(R) Application.
#
# OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
#
# OpenNMS(R) is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# OpenNMS(R) is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
#     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
#
# For more information contact: 
#     OpenNMS(R) Licensing <license@opennms.org>
#     http://www.opennms.org/
#     http://www.opennms.com/
#*******************************************************************************

if [ -z "$OPENNMS_ROOT" ]; then
	OPENNMS_ROOT=`ls -d ../../target/opennms-*-SNAPSHOT 2>/dev/null | sort -u | tail -n 1`
	if [ -z "$OPENNMS_ROOT" ]; then
		echo "You must run './assemble.pl -Dbuild.profile=dir' at least once before doing runInPlace.sh!"
		exit 1
	fi
	OPENNMS_ROOT=`cd $OPENNMS_ROOT; pwd`
fi

function err() {
    echo "$@" 1>&2
}

function warInPlace() {
    if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
    ../../compile.pl $OFFLINE_ARGS $DEFINES compile war:inplace
}

function runInPlace() {
    local PORT=$1
    if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
    ../../compile.pl $OFFLINE_ARGS $DEFINES -Dweb.port=$PORT -Dopennms.home=$OPENNMS_ROOT jetty:run-exploded
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
    removeGwtModuleFiles RemotePollerMap

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

    if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
    ../../compile.pl $OFFLINE_ARGS clean
}

function usage() {
    err "usage: runInPlace.sh [-hbcCgno] "
    err "\t-h : print this help"
    err "\t-C : clean up completely"
    err "\t-b : build in place before running"
    err "\t-c : remove WEB-INF/lib, WEB-INF/classes and META_INF dirs"
    err "\t-g : remove gwt generated files"
    err "\t-n : no-run: this is useful if you want to only build in place"
    err "\t-o : offline: run in offline mode"
    err "\t-p portnum : the port to run the webapp at"
    exit 1
}

WAR_INPLACE=false
RUN_INPLACE=true
REMOVE_GWT_FILES=false
REMOVE_CODE=false
OFFLINE=false
CLEAN=false
WEB_PORT=8080
DEFINES=""

while getopts obcChgnp:D: OPT; do
    case $OPT in
	b)  WAR_INPLACE=true
	    ;;
	c)  REMOVE_CODE=true
	    ;;
	C)  CLEAN=true
	    ;;
	D)  DEFINES="$DEFINES -D$OPTARG"
	    ;;
	g)  REMOVE_GWT_FILES=true
	    ;;
	n)  RUN_INPLACE=false
	    ;;
	o)  OFFLINE=true
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


