#!/bin/bash

VERSION_BUILD='1.5'

if [ -z "$3" ]; then
	echo "usage: $0 <java_output_dir> <class_output_dir> <input_dir> [input_dir]"
	echo "note that directories must be full paths!"
	exit 1;
fi

# minimum requirements
MINIMUM_JAVA=1.3.0-00 export MINIMUM_JAVA

# workaround for buggy libc's
ulimit -s 2048

PWD_CMD=`which pwd 2>&1 | grep -v "no pwd in" | grep -v "shell built-in command"`
[ -z "$PWD_COMMAND" ] && [ -x /bin/pwd ] && PWD_CMD="/bin/pwd"

if [ `expr "$0" : '\(.\)'` = "/" ]; then
	PREFIX=`dirname $0` export PREFIX
else
	if [ `expr "$0" : '\(..\)'` = ".." ]; then
		cd `dirname $0`
		PREFIX=`$PWD_CMD` export PREFIX
		cd -
	elif [ `expr "$0" : '\(.\)'` = "." ] || [ `expr "$0" : '\(.\)'` = "/" ]; then
		PREFIX=`$PWD_CMD` export PREFIX
	else
		PREFIX=`$PWD_CMD`"/"`dirname $0` export PREFIX
	fi
fi

# load libraries
for script in pid_process arg_process build_classpath check_tools \
	compiler_setup find_jarfile handle_properties java_lint version_compare; do
	source $PREFIX/tools/infrastructure/${script}.sh
done

# load platform-independent settings
for file in $PREFIX/tools/infrastructure/platform_*.sh; do
	source $file
done

TOOL_CLASSES="`PROPERTY_OVERRIDE="$PREFIX/tools/build.properties" get_property opennms.tools.classes`"
BUILD_CLASSES="`get_property 'root.build'`/opennms/classes"
SABLECC_CLASSES="`get_property 'root.build'`/sablecc"

# see tools/infrastructure/build_classpath.sh for syntax
CLASSPATH=`build_classpath cp:$CLASSPATH_OVERRIDE dir:$TOOL_CLASSES \
	dir:$BUILD_CLASSES dir:$SABLECC_CLASSES \
	dir:$PREFIX/lib jar:ant141 jar:ant141-optional jar:ldap \
	jar:xerces14 jar:xalan122 jar:regexp120 jar:jdhcp jar:castor-0.9.3.9 \
	jar:castor-0.9.3.9-xml jar:jcifs jar:jms102a jar:log4j jar:postgresql \
	jar:jdbc2_0-stdext jar:sablecc-2.17.2 jar:sablecc-anttask-1.1.0 \
	jar:servlet jar:catalina jar:fop0150 jar:bsf220 jar:batik100 \
	jar:jimi100 jar:ldap jar:jsse jar:jcert jar:jnet \
	jardir:/var/tomcat4/lib "cp:$CLASSPATH"` \
	export CLASSPATH
PATH="$JAVA_HOME/bin:$PATH" export PATH

JAVA_OUTPUT_DIR="$1"; shift
CLASS_OUTPUT_DIR="$1"; shift

IGNORE="^admin/eventconf"

echo "- creating java files:"
if [ -z "$COMPILEONLY" ]; then
	for dir in "$@"; do
		echo "[entering '$dir']"
		if pushd "$dir" >/dev/null 2>&1; then
			DIRECTORIES=". `find . -depth -name \*.jsp | sed -e 's#[^/]*$##' -e 's#^./##' -e 's#/$##' | sort | uniq`"
			for dir in $DIRECTORIES; do
				CONTINUE=1
				for ig in $IGNORE; do
					if perl -e "if ('$dir' =~ m#$ig#) { exit 0 } else { exit 1 }"; then
						CONTINUE=0
					fi
				done
				if [ "$CONTINUE" -eq "1" ]; then
					echo -e "  - generating java in '$dir': \c"
					if [ -f "`ls $dir/*.jsp | head -1`" ]; then
						rm -rf "$JAVA_OUTPUT_DIR/$dir/*"
						mkdir -p "$JAVA_OUTPUT_DIR/$dir"
						$JAVA_HOME/bin/java -Djasper.home=`pwd` org.apache.jasper.JspC -uriroot `pwd` -v -d "$JAVA_OUTPUT_DIR/$dir" $dir/*.jsp
						echo "done"
					else
						echo "  no JSP files in this directory!"
					fi
				else
					echo -e "  - generating java in '$dir': skipped"
				fi
			done
			popd >/dev/null 2>&1
		fi
	done
	echo ""
fi

IGNORE="boxwidget_0002dtop_0002dinclude.java"

rm -rf $CLASS_OUTPUT_DIR/*
FAILED_LOGS=""
COMPILE_ERRORS=0
echo "- compiling java files: "
echo "[entering '$JAVA_OUTPUT_DIR']"
pushd $JAVA_OUTPUT_DIR >/dev/null 2>&1
for dir in . `find . -name \*.java | sed -e 's#[^/]*$##' -e 's#^./##' -e 's#/$##' | sort | uniq`; do
	FILES=""
	echo -e "  - compiling in '$dir': \c"
	mkdir -p $CLASS_OUTPUT_DIR/$dir
	for file in $dir/*.java; do
		DOIG=0
		filename="`echo $file | sed -e 's#^.*/##'`"
		for ig in $IGNORE; do
			if [ "$ig" = "$filename" ]; then
				DOIG=1
			fi
		done
		if [ "$DOIG" = "0" ]; then
			FILES="$FILES $file"
		fi
	done
	javac -d $CLASS_OUTPUT_DIR/$dir $FILES > $CLASS_OUTPUT_DIR/$dir/compile.log 2>&1
	if [ "$?" -gt "0" ]; then
		let COMPILE_ERRORS="($COMPILE_ERRORS+1)"
		FAILED_LOGS="$FAILED_LOGS $CLASS_OUTPUT_DIR/$dir/compile.log"
		echo "failed"
	else
		echo "ok"
	fi
done
popd >/dev/null 2>&1
echo ""

if [ "$COMPILE_ERRORS" -gt 0 ]; then
	echo "There were $COMPILE_ERRORS errors compiling the JSPs."
	echo "Please see the following logs:"
	for log in $FAILED_LOGS; do
		echo "  $log"
	done
fi

exit $COMPILE_ERRORS
