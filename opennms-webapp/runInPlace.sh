#!/bin/bash

if [ -z "$OPENNMS_ROOT" ]; then
	OPENNMS_ROOT=`ls -d ../target/opennms-*-SNAPSHOT 2>/dev/null | sort -u | tail -n 1`
	if [ -z "$OPENNMS_ROOT" ]; then
		echo "You must run './assemble.pl -Dbuild.profile=dir' at least once before doing runInPlace.sh!"
		exit 1
	fi
	OPENNMS_ROOT=`cd $OPENNMS_ROOT; pwd`
fi

function err() {
    echo -e "$@" 1>&2
}

function warInPlace() {
	if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
    ../compile.pl $OFFLINE_ARGS $DEFINES '-P!jspc,runInPlace' compile war:inplace
    mkdir -p src/main/webapp/{WEB-INF,META-INF}
    sed -e "s,\${install.dir},$OPENNMS_ROOT,g" src/web-inf/configuration.properties > src/main/webapp/WEB-INF/configuration.properties
    sed -e "s,\${install.servlet.dir},`pwd`/src/main/webapp,g" src/meta-inf/context.xml > src/main/webapp/META-INF/context.xml
}

function runInPlace() {
    local PORT=$1
	if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
	if $DEBUG; then
			MAVEN_OPTS="${MAVEN_OPTS} -XX:PermSize=512m -XX:MaxMetaspaceSize=1g -Xmx1g -XX:ReservedCodeCacheSize=512m"
			MAVEN_OPTS="${MAVEN_OPTS} -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8002,server=y,suspend=n"
			export MAVEN_OPTS;
	fi
	echo $MAVEN_OPTS;
  ../compile.pl $OFFLINE_ARGS $DEFINES -Dweb.port=$PORT -Dopennms.home=$OPENNMS_ROOT '-P!jspc,runInPlace' jetty:run-exploded
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
	
	if $OFFLINE; then
        OFFLINE_ARGS="-o"
    fi
    ../compile.pl $OFFLINE_ARGS clean
}

function usage() {
    err "usage: runInPlace.sh [-hbcCgn] "
    err "\t-h : print this help"
    err "\t-C : clean up completely"
    err "\t-b : build in place before running"
	err "\t-t : start jetty with debug port 8002 open"
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
DEBUG=false
DEFINES=""

while getopts bcChgntp: OPT; do
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
	t)  DEBUG=true
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


