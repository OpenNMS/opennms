#
#  $Id$
#
# The version used to be passed from build.xml. It's hardcoded here
# the build system generally passes --define "version X" to rpmbuild.
%{!?version:%define version 1.3.10}
# The release number is set to 0 unless overridden
%{!?releasenumber:%define releasenumber 0}
# The install prefix becomes $OPENMS_HOME in the finished package
%{!?instprefix:%define instprefix /opt/opennms}
# I think this is the directory where the package will be built
%{!?packagedir:%define packagedir opennms-%version-%{releasenumber}}
# This is where the OPENNMS_HOME variable will be set on the remote 
# operating system. Not sure this is needed anymore.
%{!?profiledir:%define profiledir /etc/profile.d}
# This is where the "share" directory will link on RPM-based systems
%{!?sharedir:%define sharedir /var/opennms}
# This is where the "logs" directory will link on RPM-based systems
%{!?logdir:%define logdir /var/log/opennms}
# Where the OpenNMS webapp lives
%{!?webappsdir:%define webappsdir %instprefix/webapps}
# Where the OpenNMS Jetty webapp lives
%{!?jettydir:%define jettydir %instprefix/jetty-webapps}
# The directory for the OpenNMS webapp
%{!?servletdir:%define servletdir opennms}
# Where OpenNMS binaries live
%{!?bindir:%define bindir %instprefix/bin}

%{!?jdk:%define jdk jdk >= 1:1.5}

# keep RPM from making an empty debug package
%define debug_package %{nil}
# don't do a bunch of weird redhat post-stuff  :)
%define __os_install_post %{nil}
%define __find_requires %{nil}
%define __perl_requires %{nil}

%define with_tests	0%{nil}
%define with_docs	1%{nil}

Name:			opennms
Summary:		Enterprise-grade Network Management Platform (Easy Install)
Release:		%releasenumber
Version:		%version
License:		LGPL/GPL
Group:			Applications/System
BuildArch:		noarch

Source:			%{name}-source-%{version}-%{releasenumber}.tar.gz
URL:			http://www.opennms.org/
BuildRoot:		%{_tmppath}/%{name}-%{version}-root

Requires:		opennms-webui      >= %{version}-%{release}
Requires:		opennms-core        = %{version}-%{release}
Requires:		postgresql-server  >= 7.4

# don't worry about buildrequires, the shell script will bomb quick  =)
BuildRequires:		%{jdk}

Prefix: %{instprefix}
Prefix: %{sharedir}
Prefix: %{logdir}

%description
OpenNMS is an enterprise-grade network management platform.

This package used to contain what is now in the "opennms-core" package.
It now exists to give a reasonable default installation of OpenNMS.

When you install this package, you will also need to install one of the
opennms-webapp packages.  OpenNMS now provides 2 ways to install the
web UI:

* standalone

  A standalone version of the web UI for OpenNMS, suitable for embedding inside
  tomcat or another servlet container, or on a server separate from the OpenNMS
  core server.

* jetty

  A version of the web UI for OpenNMS which uses a built-in, embedded version
  of Jetty, which runs in the same JVM as OpenNMS.  This is the recommended
  version unless you have specific needs otherwise.

%package core
Summary:	The core OpenNMS backend.
Group:		Applications/System
Requires:	jicmp
Requires:	%{jdk}
Obsoletes:	opennms < 1.3.11

%description core
The core OpenNMS backend.  This package contains the main OpenNMS
daemon responsible for discovery, polling, data collection, and
notifications (ie, anything that is not part of the web UI).

If you want to be able to view your data, you will need to install
one of the opennms-webapp packages.

The logs and data directories are relocatable.  By default, they are:

  logs: %{logdir}
  data: %{sharedir}

If you wish to install them to an alternate location, use the --relocate rpm
option, like so:

  rpm -i --relocate %{logdir}=/mnt/netapp/opennms-logs opennms-core.rpm

%if %{with_docs}
%package docs
Summary:	Documentation for the OpenNMS network management platform
Group:		Applications/System

%description docs
This package contains the API and user documentation
for OpenNMS.
%endif

%package remote-poller
Summary:	Remote (Distributed) Poller for OpenNMS
Group:		Applications/System
Requires:	%{jdk}

%description remote-poller
The OpenNMS distributed monitor.  For details, see:
  http://www.opennms.org/index.php/Distributed_Monitoring

%package webapp-jetty
Summary:	Embedded web interface for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}
Provides:	opennms-webui = %{version}-%{release}
Obsoletes:	opennms-webapp < 1.3.11

%description webapp-jetty
The web UI for OpenNMS.  This is the Jetty version, which runs
embedded in the main OpenNMS core process.

%package webapp-standalone
Summary:	Standalone web interface for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}
Provides:	opennms-webui = %{version}-%{release}
Obsoletes:	opennms-webapp < 1.3.11

%description webapp-standalone
The web UI for OpenNMS.  This is the standalone version, suitable for
use with Tomcat or another servlet container.

%package plugins
Summary:	All Plugins for OpenNMS
Group:		Applications/System
Requires:	opennms-plugin-provisioning-dns
Requires:	opennms-plugin-provisioning-link
Requires:	opennms-plugin-provisioning-map
Requires:	opennms-plugin-provisioning-rancid
Requires:	opennms-plugin-ticketer-centric

%description plugins
This installs all optional plugins for OpenNMS.

%package plugin-provisioning-dns
Summary:	DNS Provisioning Adapter for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}

%description plugin-provisioning-dns
The DNS provisioning adapter allows for updating dynamic DNS records based on
provisioned nodes.

%package plugin-provisioning-link
Summary:	Link Provisioning Adapter for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}

%description plugin-provisioning-link
The link provisioning adapter creates links between provisioned nodes based on naming
conventions defined in the link-adapter-configuration.xml file.  It also updates the
status of the map links based on data link events.

%package plugin-provisioning-map
Summary:	Map Provisioning Adapter for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}

%description plugin-provisioning-map
The map provisioning adapter will automatically create maps when nodes are provisioned
in OpenNMS.

%package plugin-provisioning-rancid
Summary:	RANCID Provisioning Adapter for OpenNMS
Group:		Applications/System
Requires:	opennms-core = %{version}-%{release}

%description plugin-provisioning-rancid
The RANCID provisioning adapter coordinates with the RANCID Web Service by updating
RANCID's device database when OpenNMS provisions nodes.

%prep

tar -xvzf $RPM_SOURCE_DIR/%{name}-source-%{version}-%{release}.tar.gz -C $RPM_BUILD_DIR
%define setupdir %{packagedir}/source

%setup -D -T -n %setupdir

##############################################################################
# building
##############################################################################

%build
rm -rf $RPM_BUILD_ROOT

# nothing necessary

##############################################################################
# installation
##############################################################################

%install
#
# This next bit is to keep gprintify.py on Mandriva/Mandrake from screwing
# up the "echo" statements in the opennms init script.  See:
#	http://qa.mandriva.com/twiki/bin/view/Main/InitscriptHowto?skin=print
#
DONT_GPRINTIFY="yes, please do not"
export DONT_GPRINTIFY

%if %{with_tests}
EXTRA_TARGETS="$EXTRA_TARGETS test"
%endif

%if %{with_docs}
EXTRA_TARGETS="$EXTRA_TARGETS docs"
%endif

if [ -e "settings.xml" ]; then
	SETTINGS_XML="-s `pwd`/settings.xml"
fi

echo "=== RUNNING INSTALL ==="

sh ./build.sh $SETTINGS_XML -Dbuild=all -Dinstall.version="%{version}-%{release}" -Ddist.name=$RPM_BUILD_ROOT \
    -Dopennms.home=%{instprefix} install assembly:attached

pushd opennms-tools
    sh ../build.sh $SETTINGS_XML -N -Dinstall.version="%{version}-%{release}" -Ddist.name=$RPM_BUILD_ROOT \
        -Dopennms.home=%{instprefix} install
popd

pushd features/remote-poller
    sh ../../build.sh $SETTINGS_XML -Dinstall.version="%{version}-%{release}" -Ddist.name=$RPM_BUILD_ROOT \
        -Dopennms.home=%{instprefix} package
popd

echo "=== INSTALL COMPLETED ==="

echo "=== UNTAR BUILD ==="

mkdir -p $RPM_BUILD_ROOT%{instprefix}

tar zxvf $RPM_BUILD_DIR/%{name}-%{version}-%{release}/source/target$RPM_BUILD_ROOT.tar.gz -C $RPM_BUILD_ROOT%{instprefix}

echo "=== UNTAR BUILD COMPLETED ==="

### XXX is this needed?  (Most of) the current scripts don't use OPENNMS_HOME.
### /etc/profile.d

mkdir -p $RPM_BUILD_ROOT%{profiledir}
cat > $RPM_BUILD_ROOT%{profiledir}/%{name}.sh << END
#!/bin/bash

OPENNMS_HOME=%{instprefix}
if ! echo "\$PATH" | grep "\$OPENNMS_HOME/bin" >/dev/null 2>&1; then
	PATH="\$PATH:\$OPENNMS_HOME/bin"
fi

export OPENNMS_HOME PATH

END

### install the remote poller jar

install -c -m 644 features/remote-poller/target/*-jar-with-dependencies.jar $RPM_BUILD_ROOT%{instprefix}/bin/remote-poller.jar

%if %{with_docs}

mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
cp -pr $RPM_BUILD_DIR/%{name}-%{version}-%{release}/source/opennms-doc/target/docbkx/html/* $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}/
rm -rf $RPM_BUILD_ROOT%{instprefix}/docs
cp README* $RPM_BUILD_ROOT%{instprefix}/etc/
rm -rf $RPM_BUILD_ROOT%{instprefix}/etc/README
rm -rf $RPM_BUILD_ROOT%{instprefix}/etc/README.build
%endif

install -d -m 755 $RPM_BUILD_ROOT%{logdir}
mv $RPM_BUILD_ROOT%{instprefix}/logs/* $RPM_BUILD_ROOT%{logdir}/
rm -rf $RPM_BUILD_ROOT%{instprefix}/logs
install -d -m 755 $RPM_BUILD_ROOT%{logdir}/{controller,daemon,webapp}

install -d -m 755 $RPM_BUILD_ROOT%{sharedir}
mv $RPM_BUILD_ROOT%{instprefix}/share/* $RPM_BUILD_ROOT%{sharedir}/
rm -rf $RPM_BUILD_ROOT%{instprefix}/share

pushd $RPM_BUILD_ROOT

# core package files
find $RPM_BUILD_ROOT%{instprefix}/etc ! -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,%config(noreplace) ," | \
    sort > %{_tmppath}/files.main
find $RPM_BUILD_ROOT%{instprefix}/bin ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(755,root,root) |" | \
    grep -v '/remote-poller.sh' | \
    grep -v '/remote-poller.jar' | \
    sort >> %{_tmppath}/files.main
find $RPM_BUILD_ROOT%{instprefix}/lib ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(755,root,root) |" | \
    sort >> %{_tmppath}/files.main
find $RPM_BUILD_ROOT%{instprefix}/etc -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,%dir ," | \
    sort >> %{_tmppath}/files.main

# jetty
find $RPM_BUILD_ROOT%{jettydir} ! -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,," | \
    grep -v '/WEB-INF/[^/]*\.xml$' | \
    grep -v '/WEB-INF/[^/]*\.properties$' | \
    sort >> %{_tmppath}/files.jetty
find $RPM_BUILD_ROOT%{jettydir} -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,%dir ," | \
    sort >> %{_tmppath}/files.jetty

# webapps
find $RPM_BUILD_ROOT%{webappsdir} ! -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,," | \
    grep -v '/WEB-INF/[^/]*\.xml$' | \
    grep -v '/WEB-INF/[^/]*\.properties$' | \
    sort > %{_tmppath}/files.webapp
find $RPM_BUILD_ROOT%{webappsdir} -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,%dir ," | \
    sort >> %{_tmppath}/files.webapp

popd

# provisioning adapters

cp integrations/opennms-dns-provisioning-adapter/target/*.jar  $RPM_BUILD_ROOT%{instprefix}/lib/
cp integrations/opennms-link-provisioning-adapter/target/*.jar $RPM_BUILD_ROOT%{instprefix}/lib/
cp integrations/opennms-map-provisioning-adapter/target/*.jar  $RPM_BUILD_ROOT%{instprefix}/lib/
cp integrations/opennms-rancid/target/*.jar                    $RPM_BUILD_ROOT%{instprefix}/lib/
rm -rf $RPM_BUILD_ROOT%{instprefix}/lib/*-sources.jar
rm -rf $RPM_BUILD_ROOT%{instprefix}/lib/*-tests.jar
rm -rf $RPM_BUILD_ROOT%{instprefix}/lib/*-xsds.jar

# config files, this should be more automated  :P
cp integrations/opennms-link-provisioning-adapter/src/main/resources/link-adapter-configuration.xml $RPM_BUILD_ROOT%{instprefix}/etc/
cp integrations/opennms-link-provisioning-adapter/src/main/resources/endpoint-configuration.xml $RPM_BUILD_ROOT%{instprefix}/etc/
cp integrations/opennms-map-provisioning-adapter/src/main/resources/mapsadapter-configuration.xml   $RPM_BUILD_ROOT%{instprefix}/etc/

%clean
rm -rf $RPM_BUILD_ROOT

##############################################################################
# file setup
##############################################################################

%files
%defattr(664 root root 775)

%files core -f %{_tmppath}/files.main
%defattr(664 root root 775)
%attr(755,root,root)	%{profiledir}/%{name}.sh
%attr(755,root,root)	%{instprefix}/contrib
			%{sharedir}
			%{logdir}
			%{logdir}/controller
			%{logdir}/daemon
			%{logdir}/webapp

%if %{with_docs}
%files docs
%defattr(644 root root 755)
%{_docdir}/%{name}-%{version}
%endif

%files remote-poller
%attr(755,root,root) %{bindir}/remote-poller.sh
%{instprefix}/bin/remote-poller.jar

%files webapp-jetty -f %{_tmppath}/files.jetty
%defattr(644 root root 755)
%{instprefix}/jetty-webapps
%config %{jettydir}/%{servletdir}/WEB-INF/*.xml
%config %{jettydir}/%{servletdir}/WEB-INF/*.properties

%files webapp-standalone -f %{_tmppath}/files.webapp
%defattr(644 root root 755)
%config %{webappsdir}/%{servletdir}/WEB-INF/*.xml
%config %{webappsdir}/%{servletdir}/WEB-INF/*.properties

%files plugins

%files plugin-provisioning-dns
%attr(664,root,root) %{instprefix}/lib/opennms-dns-provisioning-adapter*.jar

%files plugin-provisioning-link
%attr(664,root,root) %{instprefix}/lib/opennms-link-provisioning-adapter*.jar
%attr(664,root,root) %{instprefix}/etc/link-adapter-configuration.xml
%attr(664,root,root) %{instprefix}/etc/endpoint-configuration.xml

%files plugin-provisioning-map
%attr(664,root,root) %{instprefix}/lib/opennms-map-provisioning-adapter*.jar
%attr(664,root,root) %{instprefix}/etc/mapsadapter-configuration.xml

%files plugin-provisioning-rancid
%attr(664,root,root) %{instprefix}/lib/opennms-rancid*.jar

%post docs
printf -- "- making symlink for $RPM_INSTALL_PREFIX0/docs... "
if [ -e "$RPM_INSTALL_PREFIX0/docs" ] && [ ! -L "$RPM_INSTALL_PREFIX0/docs" ]; then
	echo "failed: $RPM_INSTALL_PREFIX0/docs is a real directory, but it should be a symlink to %{_docdir}/%{name}-%{version}."
else
	rm -rf "$RPM_INSTALL_PREFIX0/docs"
	ln -sf "%{_docdir}/%{name}-%{version}" "$RPM_INSTALL_PREFIX0/docs"
	echo "done"
fi

%postun docs
if [ "$1" = 0 ]; then
	if [ -L "$RPM_INSTALL_PREFIX0/docs" ]; then
		rm -f "$RPM_INSTALL_PREFIX0/docs"
	fi
fi

%post core

if [ -n "$DEBUG" ]; then
	env | grep RPM_INSTALL_PREFIX | sort -u
fi

if [ "$RPM_INSTALL_PREFIX0/logs" != "$RPM_INSTALL_PREFIX2" ]; then
	printf -- "- making symlink for $RPM_INSTALL_PREFIX0/logs... "
	if [ -e "$RPM_INSTALL_PREFIX0/logs" ] && [ ! -L "$RPM_INSTALL_PREFIX0/logs" ]; then
		echo "failed: $RPM_INSTALL_PREFIX0/logs is a real directory or file, but it should be a symlink to $RPM_INSTALL_PREFIX2."
		echo "Your OpenNMS install may not function properly."
	else
		rm -rf "$RPM_INSTALL_PREFIX0/logs"
		ln -sf "$RPM_INSTALL_PREFIX2" "$RPM_INSTALL_PREFIX0/logs"
		echo "done"
	fi
fi

for dir in controller daemon webapp; do
	if [ -f "$RPM_INSTALL_PREFIX2/$dir" ]; then
		printf -- "ERROR: not sure what to do... $RPM_INSTALL_PREFIX2/$dir is a file, but it should be a directory or symlink.  Expect problems.  :)"
	else
		if [ ! -d "$RPM_INSTALL_PREFIX2/$dir" ]; then
			mkdir -p "$RPM_INSTALL_PREFIX2/$dir"
		fi
	fi
done

if [ "$RPM_INSTALL_PREFIX0/share" != "$RPM_INSTALL_PREFIX1" ]; then
	printf -- "- making symlink for $RPM_INSTALL_PREFIX0/share... "
	if [ -e "$RPM_INSTALL_PREFIX0/share" ] && [ ! -L "$RPM_INSTALL_PREFIX0/share" ]; then
		echo "failed: $RPM_INSTALL_PREFIX0/share is a real directory, but it should be a symlink to $RPM_INSTALL_PREFIX1."
		echo "Your OpenNMS install may not function properly."
	else
		rm -rf "$RPM_INSTALL_PREFIX0/share"
		ln -sf "$RPM_INSTALL_PREFIX1" "$RPM_INSTALL_PREFIX0/share"
		echo "done"
	fi
fi

printf -- "- moving *.sql.rpmnew files (if any)... "
if [ `ls $RPM_INSTALL_PREFIX0/etc/*.sql.rpmnew 2>/dev/null | wc -l` -gt 0 ]; then
	for i in $RPM_INSTALL_PREFIX0/etc/*.sql.rpmnew; do
		mv $i ${i%%%%.rpmnew}
	done
fi
echo "done"

printf -- "- checking for old update files... "

JAR_UPDATES=`find $RPM_INSTALL_PREFIX0/lib/updates -name \*.jar   -exec rm -rf {} \; -print 2>/dev/null | wc -l`
CLASS_UPDATES=`find $RPM_INSTALL_PREFIX0/lib/updates -name \*.class -exec rm -rf {} \; -print 2>/dev/null | wc -l`
let TOTAL_UPDATES=`expr $JAR_UPDATES + $CLASS_UPDATES`
if [ "$TOTAL_UPDATES" -gt 0 ]; then
	echo "FOUND"
	echo ""
	echo "WARNING: $TOTAL_UPDATES old update files were found in your"
	echo "$RPM_INSTALL_PREFIX0/lib/updates directory.  They have been deleted"
	echo "because they should now be out of date."
	echo ""
else
	echo "done"
fi

rm -f $RPM_INSTALL_PREFIX0/etc/configured
for dir in /etc /etc/rc.d; do
	if [ -d "$dir" ]; then
		ln -sf $RPM_INSTALL_PREFIX0/bin/opennms $dir/init.d/opennms
		break
	fi
done

for LIBNAME in jicmp jrrd; do
	if [ `grep "opennms.library.${LIBNAME}" "$RPM_INSTALL_PREFIX0/etc/libraries.properties" 2>/dev/null | wc -l` -eq 0 ]; then
		LIBRARY_PATH=`rpm -ql "${LIBNAME}" 2>/dev/null | grep "/lib${LIBNAME}.so\$" | head -n 1`
		if [ -n "$LIBRARY_PATH" ]; then
			echo "opennms.library.${LIBNAME}=${LIBRARY_PATH}" >> "$RPM_INSTALL_PREFIX0/etc/libraries.properties"
		fi
	fi
done

echo ""
echo " *** Installation complete.  You must still run the installer and"
echo " *** make a few other changes before you start OpenNMS.  See the"
echo " *** install guide and release notes for details."

%postun core

if [ "$1" = 0 ]; then
	for dir in logs share; do
		if [ -L "$RPM_INSTALL_PREFIX0/$dir" ]; then
			rm -f "$RPM_INSTALL_PREFIX0/$dir"
		fi
	done
fi

%changelog
* Tue Sep 29 2009 Benjamin Reed <ranger@opennms.org>
- Add provisioning adapters as optional packages.

* Mon Oct 29 2007 Benjamin Reed <ranger@opennms.org>
- Make sure the postinstall happens in opennms-core, not opennms.

* Tue Sep 18 2007 Benjamin Reed <ranger@opennms.org>
- Look for existing libraries and auto-populate libraries.properties,
  if possible.

* Tue Sep 04 2007 Benjamin Reed <ranger@opennms.org>
- Split out -core and the webapps for easier balance of custom-installs
  across systems and easy install through package management.

* Thu Aug 16 2007 Benjamin Reed <ranger@opennms.org>
- Added handling of Jetty stuff.

* Wed Jul 25 2007 Benjamin Reed <ranger@opennms.org>
- Updated to be noarch, now that all native code has been moved to
  separate packages

* Fri Jan 05 2007 DJ Gregor <dj@opennms.org>
- Change the postgresql versions to 7.4.  The installer will not work
  with anything earlier than 7.4.
- Add WEB-INF/configuration.properties in the webapp as a configuration
  file.
- Fix up a comment to reflect the current name of the opennms startup script.

* Thu Jan 20 2005 DJ Gregor <dj@opennms.org>
- Remove much of the automatic bits that were performed at
  install time.

* Tue Sep 28 2004 Matt Brozowski <brozow@opennms.org>
- Modified to use opennms.xml rather than changes to server.xml

* Fri Nov 16 2001 Ben Reed <ben@opennms.org>
- Updated symlinks to point to tomcat 4.0.1 dirs

* Wed Oct 31 2001 Brian Weaver <weave@oculan.com>
- Added more dependencies since the webapp package was being
  installed BEFORE tomcat which just doesn't work.

* Tue Oct 30 2001 Brian Weaver <weave@oculan.com>
- Changed support from OpenJMS to JBossMQ

* Wed Oct 24 2001 Ben Reed <ben@opennms.org>
- changed things around so server.xml clobbers tomcat's
  server.xml, if one exists in
  webapps/%{name}/WEB-INF/server.xml

* Tue Oct 09 2001 Brian Weaver <weave@oculan.com>
- Removed the OpenJMS jar code after build so that
  it is not installed. This will force the client
  to use the OpenJMS jar files from /usr/share/java
  to ensure that the client/server are communicating
  correctly.

* Tue Oct 02 2001 Benjamin Reed <ben@opennms.org>
- added /var/tomcat4 to add some opennms jars

* Tue Oct 02 2001 Benjamin Reed <ben@opennms.org>
- init in temporarily gone until the new startup stuff is
  worked out

* Tue Oct 02 2001 Benjamin Reed <ben@opennms.org>
- postun gone, move much of pre into build-time, fix
  for doc and log link errors

* Sun Sep 30 2001 Benjamin Reed <ben@opennms.org>
- changed to handle generating a proper .src.rpm that can
  be built standalone

* Tue Sep 11 2001 Benjamin Reed <ben@opennms.org>
- Ported the RPM to the experimental build.

* Mon Apr 16 2001 Benjamin Reed <ben@opennms.org>
- Added a /usr/bin/opennms that calls the opennms script
  in %{instprefix} for convenience

* Tue Apr 04 2001 Benjamin Reed <ben@opennms.org>
- Rearranged to work with the new build system.

* Mon Nov 27 2000 Benjamin Reed <ben@opennms.org>
- Work on actually doing something other than copying files
- Addition of distribution-specific conditionals

* Wed Nov 8 2000 Benjamin Reed <ben@opennms.org>
- Initial RPM

