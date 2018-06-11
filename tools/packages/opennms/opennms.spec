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
# This is where the OPENNMS_HOME variable will be set on the remote 
# operating system. Not sure this is needed anymore.
%{!?profiledir:%define profiledir /etc/profile.d}
# This is where the "share" directory will link on RPM-based systems
%{!?sharedir:%define sharedir /var/opennms}
# This is where the "logs" directory will link on RPM-based systems
%{!?logdir:%define logdir /var/log/opennms}
# Where the OpenNMS Jetty webapp lives
%{!?jettydir:%define jettydir %instprefix/jetty-webapps}
# The directory for the OpenNMS webapp
%{!?servletdir:%define servletdir opennms}
# Where OpenNMS binaries live
%{!?bindir:%define bindir %instprefix/bin}

# Description
%{!?_name:%define _name "opennms"}
%{!?_descr:%define _descr "OpenNMS"}
%{!?packagedir:%define packagedir %{_name}-%version-%{releasenumber}}

%{!?jdk:%define jdk java-1.8.0}

%{!?extrainfo:%define extrainfo }
%{!?extrainfo2:%define extrainfo2 }
%{!?skip_compile:%define skip_compile 0}
%{!?enable_snapshots:%define enable_snapshots 1}

# keep RPM from making an empty debug package
%define debug_package %{nil}
# don't do a bunch of weird redhat post-stuff  :)
%define _use_internal_dependency_generator 0
%define __os_install_post %{nil}
%define __find_requires %{nil}
%define __perl_requires %{nil}
%global _binaries_in_noarch_packages_terminate_build 0
AutoReq: no
AutoProv: no

%define with_tests	0%{nil}

Name:			%{_name}
Summary:		Enterprise-grade Network Management Platform (Easy Install)
Release:		%releasenumber
Version:		%version
License:		LGPL/AGPL
Group:			Applications/System
BuildArch:		noarch

Source:			%{name}-source-%{version}-%{releasenumber}.tar.gz
URL:			http://www.opennms.org/

Requires(pre):		%{name}-webui       = %{version}-%{release}
Requires:		%{name}-webui       = %{version}-%{release}
Requires(pre):		%{name}-core        = %{version}-%{release}
Requires:		%{name}-core        = %{version}-%{release}
Requires(pre):		postgresql-server  >= 9.1
Requires:		postgresql-server  >= 9.1

# don't worry about buildrequires, the shell script will bomb quick  =)
#BuildRequires:		%{jdk}

Prefix: %{instprefix}
Prefix: %{sharedir}
Prefix: %{logdir}

%description
%{_descr} is an enterprise-grade network management platform.

This package used to contain what is now in the "%{name}-core" package.
It now exists to give a reasonable default installation of OpenNMS.

When you install this package, you will likely also need to install the
webapp package.

%{extrainfo}
%{extrainfo2}


%package core
Summary:	The core OpenNMS backend.
Group:		Applications/System
Requires(pre):	jicmp >= 2.0.0
Requires:	jicmp >= 2.0.0
Requires(pre):	jicmp6 >= 2.0.0
Requires:	jicmp6 >= 2.0.0
Requires(pre):	%{jdk}
Requires:	%{jdk}
Obsoletes:	opennms < 1.3.11
Provides:	%{name}-plugin-protocol-xml = %{version}-%{release}
Obsoletes:	%{name}-plugin-protocol-xml < %{version}

%description core
The core backend.  This package contains the main daemon responsible
for discovery, polling, data collection, and notifications (ie,
anything that is not part of the web UI).

If you want to be able to view your data, you will need to install
the webapp package.

The logs and data directories are relocatable.  By default, they are:

  logs: %{logdir}
  data: %{sharedir}

If you wish to install them to an alternate location, use the --relocate rpm
option, like so:

  rpm -i --relocate %{logdir}=/mnt/netapp/%{name}-logs %{name}-core.rpm

%{extrainfo}
%{extrainfo2}


%package source
Summary:	Source for the %{_descr} network management platform
Group:		Applications/System

%description source
This package contains the source tarball for %{_descr}, for AGPL compliance.

%{extrainfo}
%{extrainfo2}


%package docs
Summary:	Documentation for the %{_descr} network management platform
Group:		Applications/System

%description docs
This package contains the API and user documentation.

%{extrainfo}
%{extrainfo2}


%package remote-poller
Summary:	Remote (Distributed) Poller for %{_descr}
Group:		Applications/System
Requires(pre):	%{jdk}
Requires:	%{jdk}

%description remote-poller
The distributed monitor.  For details, see:
  http://www.opennms.org/index.php/Distributed_Monitoring

%{extrainfo}
%{extrainfo2}


%package jmx-config-generator
Summary:	Generate JMX Configuration
Group:		Applications/System
Requires(pre):	%{jdk}
Requires:	%{jdk}
Requires:	%{name}-core = %{version}-%{release}

%description jmx-config-generator
Generates configuration files for monitoring/collecting from
the Java Management Extensions.

%{extrainfo}
%{extrainfo2}


%package webapp-jetty
Summary:	Embedded web interface
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}
Provides:	%{name}-webui = %{version}-%{release}
Obsoletes:	opennms-webapp < 1.3.11

%description webapp-jetty
The web UI.  This is the Jetty version, which runs
embedded in the main core process.

%{extrainfo}
%{extrainfo2}


%package webapp-remoting
Summary:	Remote Poller webapp
Group:		Applications/System
Requires:	%{name}-webapp-jetty = %{version}-%{release}
Conflicts:	%{name}-webapp-jetty < 19.0.0-0

%description webapp-remoting
The JNLP application that provides the Remote Poller.

%{extrainfo}
%{extrainfo2}


%package ncs
Summary:	Network Component Services
Group:		Applications/System
Requires:	%{name}-webapp-jetty = %{version}-%{release}

%description ncs
NCS provides a framework for doing correlation of service events across
disparate nodes.

%{extrainfo}
%{extrainfo2}


%package plugins
Summary:	All Plugins
Group:		Applications/System
Requires(pre):	%{name}-plugin-northbounder-jms
Requires:	%{name}-plugin-northbounder-jms
Requires(pre):	%{name}-plugin-provisioning-dns
Requires:	%{name}-plugin-provisioning-dns
Requires(pre):	%{name}-plugin-provisioning-rancid
Requires:	%{name}-plugin-provisioning-rancid
Requires(pre):	%{name}-plugin-provisioning-reverse-dns
Requires:	%{name}-plugin-provisioning-reverse-dns
Requires(pre):	%{name}-plugin-provisioning-snmp-asset
Requires:	%{name}-plugin-provisioning-snmp-asset
Requires(pre):	%{name}-plugin-provisioning-snmp-hardware-inventory
Requires:	%{name}-plugin-provisioning-snmp-hardware-inventory
Requires(pre):	%{name}-plugin-ticketer-jira
Requires:	%{name}-plugin-ticketer-jira
Requires(pre):	%{name}-plugin-ticketer-otrs
Requires:	%{name}-plugin-ticketer-otrs
Requires(pre):	%{name}-plugin-ticketer-rt
Requires:	%{name}-plugin-ticketer-rt
Requires(pre):	%{name}-plugin-protocol-cifs
Requires:	%{name}-plugin-protocol-cifs
Requires(pre):	%{name}-plugin-protocol-dhcp
Requires:	%{name}-plugin-protocol-dhcp
Requires(pre):	%{name}-plugin-protocol-nsclient
Requires:	%{name}-plugin-protocol-nsclient
Requires(pre):	%{name}-plugin-protocol-radius
Requires:	%{name}-plugin-protocol-radius
Requires(pre):	%{name}-plugin-protocol-xmp
Requires:	%{name}-plugin-protocol-xmp
Requires(pre):	%{name}-plugin-collector-vtdxml-handler
Requires:	%{name}-plugin-collector-vtdxml-handler

%description plugins
This installs all optional plugins.

%{extrainfo}
%{extrainfo2}


%package plugin-northbounder-jms
Summary:	JMS Alarm Northbounder
Group:		Applications/System
Requires:	%{name}-core = %{version}-%{release}

%description plugin-northbounder-jms
This northbounder allows you to send OpenNMS alarms to an 
external JMS listener.

%{extrainfo}
%{extrainfo2}


%package plugin-provisioning-map
Summary:	Obsolete: Map Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-map
The map provisioning adapter is no longer part of OpenNMS.


%package plugin-provisioning-link
Summary:	Obsolete: Link Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-link
The map provisioning adapter is no longer part of OpenNMS.


%package plugin-provisioning-dns
Summary:	DNS Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-dns
The DNS provisioning adapter allows for updating dynamic DNS records based on
provisioned nodes.

%{extrainfo}
%{extrainfo2}


%package plugin-provisioning-reverse-dns
Summary:	Reverse DNS Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-reverse-dns
The Reverse DNS provisioning adapter allows for updating the hostname on an
interface based on its reverse DNS lookup.

%{extrainfo}
%{extrainfo2}


%package plugin-provisioning-rancid
Summary:	RANCID Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-rancid
The RANCID provisioning adapter coordinates with the RANCID Web Service by updating
RANCID's device database when %{_descr} provisions nodes.

%{extrainfo}
%{extrainfo2}


%package plugin-provisioning-snmp-asset
Summary:	SNMP Asset Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-snmp-asset
The SNMP asset provisioning adapter responds to provisioning events by updating asset
fields with data fetched from SNMP GET requests.

%{extrainfo}
%{extrainfo2}


%package plugin-provisioning-snmp-hardware-inventory
Summary:	SNMP Hardware Inventory Provisioning Adapter
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-provisioning-snmp-hardware-inventory
The SNMP Hardware Inventory provisioning adapter responds to provisioning events by updating 
hardware fields with data fetched from the ENTITY-MIB and vendor extensions of this MIB.

%{extrainfo}
%{extrainfo2}


%package plugin-ticketer-jira
Summary:	JIRA Ticketer Plugin
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-ticketer-jira
The JIRA ticketer plugin provides the ability to automatically create JIRA
issues from %{_descr} alarms.

%{extrainfo}
%{extrainfo2}


%package plugin-ticketer-otrs
Summary:	OTRS Ticketer Plugin
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-ticketer-otrs
The OTRS ticketer plugin provides the ability to automatically create OTRS
issues from %{_descr} alarms.

%{extrainfo}
%{extrainfo2}


%package plugin-ticketer-rt
Summary:	RT Ticketer Plugin
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-ticketer-rt
The RT ticketer plugin provides the ability to automatically create RT
tickets from %{_descr} alarms.

%{extrainfo}
%{extrainfo2}


%package plugin-protocol-cifs
Summary:	CIFS Poller Plugin
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-protocol-cifs
The CIFS protocol plugin provides a poller monitor for CIFS network shares.

%{extrainfo}
%{extrainfo2}


%package plugin-protocol-dhcp
Summary:	DHCP Poller and Detector Plugin
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-protocol-dhcp
The DHCP protocol plugin provides a daemon, provisioning detector, capsd plugin, and
poller monitor for DHCP.

%{extrainfo}
%{extrainfo2}


%package plugin-protocol-nsclient
Summary:	NSCLIENT Plugin Support
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-protocol-nsclient
The NSClient protocol plugin provides a capsd plugin and poller monitor for NSClient
and NSClient++.

%{extrainfo}
%{extrainfo2}


%package plugin-protocol-radius
Summary:	RADIUS Plugin Support
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-protocol-radius
The RADIUS protocol plugin provides a provisioning detector, capsd plugin, poller
monitor, and Spring Security authorization mechanism for RADIUS.

%{extrainfo}
%{extrainfo2}


%package plugin-protocol-xmp
Summary:	XMP Poller
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-protocol-xmp
The XMP protocol plugin provides a capsd plugin and poller monitor for XMP.

%{extrainfo}
%{extrainfo2}


%package plugin-collector-juniper-tca
Summary:	Juniper TCA Collector
Group:		Applications/System
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-collector-juniper-tca
The Juniper JCA collector provides a collector plugin for Collectd to collect data from TCA devices.

%{extrainfo}
%{extrainfo2}


%package plugin-collector-vtdxml-handler
Summary:	VTD-XML Collection Handler
Group:		Applications/System
License:	GPL
Requires(pre):	%{name}-core = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}

%description plugin-collector-vtdxml-handler
The XML Collection Handler for Standard and 3GPP XMLs based on VTD-XML.
VTD-XML is very fast GPL library for parsing XMLs with XPath Support.

%{extrainfo}
%{extrainfo2}

%prep

tar -xvzf %{_sourcedir}/%{name}-source-%{version}-%{release}.tar.gz -C "%{_builddir}"
%define setupdir %{packagedir}

%setup -D -T -n %setupdir

##############################################################################
# building
##############################################################################

%build
rm -rf %{buildroot}

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

export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true"

if [ -e "settings.xml" ]; then
	export OPTS_SETTINGS_XML="-s `pwd`/settings.xml"
fi

if [ "%{skip_compile}" = 1 ]; then
	echo "=== SKIPPING COMPILE ==="
	if [ "%{enable_snapshots}" = 1 ]; then
		OPTS_ENABLE_SNAPSHOTS="-Denable.snapshots=true"
		OPTS_UPDATE_POLICY="-DupdatePolicy=always"
	fi
	TOPDIR=`pwd`
	for dir in . opennms-tools; do
		cd $dir
			"$TOPDIR"/compile.pl -N $OPTS_SKIP_TESTS $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dinstall.version="%{version}-%{release}" -Ddist.name="%{name}-%{version}-%{release}.%{_arch}" -Dopennms.home="%{instprefix}" install
		cd -
	done
else
	echo "=== RUNNING COMPILE ==="
	./compile.pl $OPTS_SKIP_TESTS $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dbuild=all -Dinstall.version="%{version}-%{release}" -Ddist.name="%{name}-%{version}-%{release}.%{_arch}" \
		-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false \
		-Dopennms.home="%{instprefix}" -Prun-expensive-tasks install
fi

echo "=== BUILDING ASSEMBLIES ==="
./assemble.pl $OPTS_SKIP_TESTS $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dbuild=all -Dinstall.version="%{version}-%{release}" -Ddist.name="%{name}-%{version}-%{release}.%{_arch}" \
	-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false \
	-Dopennms.home="%{instprefix}" -Prun-expensive-tasks -Dbuild.profile=full install

cd opennms-tools
	../compile.pl $OPTS_SKIP_TESTS $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -N -Dinstall.version="%{version}-%{release}" -Ddist.name="%{name}-%{version}-%{release}.%{_arch}" \
	-Dopennms.home="%{instprefix}" install
cd -

echo "=== INSTALL COMPLETED ==="

echo "=== UNTAR BUILD ==="

mkdir -p %{buildroot}%{instprefix}

# Untar the tar.gz created by opennms-full-assembly
tar zxvf %{_builddir}/%{name}-%{version}-%{release}/target/%{name}-%{version}-%{release}.%{_arch}.tar.gz -C %{buildroot}%{instprefix}

echo "=== UNTAR BUILD COMPLETED ==="

### Set this so users can refer to $OPENNMS_HOME easily.
### /etc/profile.d

mkdir -p %{buildroot}%{profiledir}
cat > %{buildroot}%{profiledir}/%{name}.sh << END
#!/bin/bash

OPENNMS_HOME=%{instprefix}
if ! echo "\$PATH" | grep "\$OPENNMS_HOME/bin" >/dev/null 2>&1; then
	PATH="\$PATH:\$OPENNMS_HOME/bin"
fi

export OPENNMS_HOME PATH

END

# Move the docs into %{_docdir}
rm -rf %{buildroot}%{_docdir}/%{name}-%{version}
mkdir -p %{buildroot}%{_docdir}
find %{buildroot}%{instprefix}/docs -xdev -depth -type d -print0 | xargs -0 -r rmdir 2>/dev/null || true
mv %{buildroot}%{instprefix}/docs %{buildroot}%{_docdir}/%{name}-%{version}
cp README* %{buildroot}%{instprefix}/etc/
rm -rf %{buildroot}%{instprefix}/etc/README
rm -rf %{buildroot}%{instprefix}/etc/README.build

install -d -m 755 %{buildroot}%{logdir}
mv %{buildroot}%{instprefix}/logs/.readme %{buildroot}%{logdir}/
rm -rf %{buildroot}%{instprefix}/logs

install -d -m 755 %{buildroot}%{sharedir}
mv %{buildroot}%{instprefix}/share/* %{buildroot}%{sharedir}/
rm -rf %{buildroot}%{instprefix}/share

# Copy the /etc directory into /etc/pristine
rsync -avr --exclude=examples %{buildroot}%{instprefix}/etc/ %{buildroot}%{sharedir}/etc-pristine/
chmod -R go-w %{buildroot}%{sharedir}/etc-pristine/

install -d -m 755 %{buildroot}%{_initrddir} %{buildroot}%{_sysconfdir}/sysconfig
install -m 755 %{buildroot}%{instprefix}/contrib/remote-poller/remote-poller.init      %{buildroot}%{_initrddir}/opennms-remote-poller
install -m 640 %{buildroot}%{instprefix}/contrib/remote-poller/remote-poller.sysconfig %{buildroot}%{_sysconfdir}/sysconfig/opennms-remote-poller
rm -rf %{buildroot}%{instprefix}/contrib/remote-poller

rm -rf %{buildroot}%{instprefix}/lib/*.tar.gz

# Remove all duplicate JARs from /system and symlink them to the JARs in /lib to save disk space
for FILE in %{buildroot}%{instprefix}/lib/*.jar; do BASENAME=`basename $FILE`; for SYSFILE in `find %{buildroot}%{instprefix}/system -name $BASENAME`; do rm -f $SYSFILE; ln -s /opt/opennms/lib/$BASENAME $SYSFILE; done; done
# Remove all duplicate JARs from /jetty-webapps/opennms-remoting/webstart and symlink them to the JARs in /lib to save disk space
# NOTE: We can't do this because the JARs in webstart are signed
#for FILE in %{buildroot}%{instprefix}/lib/*.jar; do BASENAME=`basename $FILE`; for SYSFILE in `find %{buildroot}%{instprefix}/jetty-webapps/opennms-remoting/webstart -name $BASENAME`; do rm -f $SYSFILE; ln -s %{instprefix}/lib/$BASENAME $SYSFILE; done; done

cd %{buildroot}

# core package files
find %{buildroot}%{instprefix}/etc ! -type d | \
	sed -e "s,^%{buildroot},%config(noreplace) ," | \
	grep -v '%{_initrddir}/opennms-remote-poller' | \
	grep -v '%{_sysconfdir}/sysconfig/opennms-remote-poller' | \
	grep -v 'ncs-northbounder-configuration.xml' | \
	grep -v 'drools-engine.d/ncs' | \
	grep -v 'dhcpd-configuration.xml' | \
	grep -v 'jira.properties' | \
	grep -v 'jms-northbounder-configuration.xml' | \
	grep -v 'juniper-tca' | \
	grep -v 'mapsadapter-configuration.xml' | \
	grep -v 'nsclient-config.xml' | \
	grep -v 'nsclient-datacollection-config.xml' | \
	grep -v 'otrs.properties' | \
	grep -v '/rt.properties' | \
	grep -v 'snmp-asset-adapter-configuration.xml' | \
	grep -v 'snmp-hardware-inventory-adapter-configuration.xml' | \
	grep -v '/users.xml' | \
	grep -v 'xmp-config.xml' | \
	grep -v 'xmp-datacollection-config.xml' | \
	grep -v 'tca-datacollection-config.xml' | \
	sort > %{_tmppath}/files.main
find %{buildroot}%{sharedir}/etc-pristine ! -type d | \
	sed -e "s,^%{buildroot},," | \
	grep -v '%{_initrddir}/opennms-remote-poller' | \
	grep -v '%{_sysconfdir}/sysconfig/opennms-remote-poller' | \
	grep -v 'ncs-northbounder-configuration.xml' | \
	grep -v 'ncs.xml' | \
	grep -v 'drools-engine.d/ncs' | \
	grep -v 'dhcpd-configuration.xml' | \
	grep -v 'jira.properties' | \
	grep -v 'jms-northbounder-configuration.xml' | \
	grep -v 'juniper-tca' | \
	grep -v 'mapsadapter-configuration.xml' | \
	grep -v 'nsclient-config.xml' | \
	grep -v 'nsclient-datacollection-config.xml' | \
	grep -v 'otrs.properties' | \
	grep -v '/rt.properties' | \
	grep -v 'snmp-asset-adapter-configuration.xml' | \
	grep -v 'snmp-hardware-inventory-adapter-configuration.xml' | \
	grep -v 'xmp-config.xml' | \
	grep -v 'xmp-datacollection-config.xml' | \
	grep -v 'tca-datacollection-config.xml' | \
	sort >> %{_tmppath}/files.main
find %{buildroot}%{instprefix}/bin ! -type d | \
	sed -e "s|^%{buildroot}|%attr(755,root,root) |" | \
	grep -v '/jmx-config-generator' | \
	grep -v '/remote-poller.sh' | \
	grep -v '/remote-poller.jar' | \
	sort >> %{_tmppath}/files.main
find %{buildroot}%{sharedir} ! -type d | \
	sed -e "s,^%{buildroot},," | \
	grep -v 'etc-pristine' | \
	grep -v 'ncs-' | \
	grep -v 'nsclient-config.xsd' | \
	grep -v 'nsclient-datacollection.xsd' | \
	grep -v 'xmp-config.xsd' | \
	grep -v 'xmp-datacollection-config.xsd' | \
	grep -v 'tca-datacollection-config.xml' | \
	grep -v 'juniper-tca' | \
	sort >> %{_tmppath}/files.main
find %{buildroot}%{instprefix}/contrib ! -type d | \
	sed -e "s|^%{buildroot}|%attr(755,root,root) |" | \
	sort >> %{_tmppath}/files.main
find %{buildroot}%{instprefix}/lib ! -type d | \
	sed -e "s|^%{buildroot}|%attr(755,root,root) |" | \
	grep -v 'jdhcp' | \
	grep -v 'jradius' | \
	grep -v 'org.opennms.features.ncs.ncs-' | \
	grep -v 'opennms-alarm-northbounder-jms' | \
	grep -v 'opennms-integration-otrs' | \
	grep -v 'opennms-integration-rt' | \
	grep -v 'opennms_jmx_config_generator' | \
	grep -v 'org.opennms.features.juniper-tca-collector' | \
	grep -v 'org.opennms.protocols.cifs' | \
	grep -v 'org.opennms.protocols.dhcp' | \
	grep -v 'org.opennms.protocols.nsclient' | \
	grep -v 'org.opennms.protocols.radius' | \
	grep -v 'org.opennms.protocols.xmp' | \
	grep -v 'opennms-vtdxml-collector-handler' | \
	grep -v 'provisioning-adapter' | \
	grep -v 'vtd-xml' | \
	grep -v 'xmp' | \
	sort >> %{_tmppath}/files.main
find %{buildroot}%{instprefix}/system ! -type d | \
    sed -e "s|^%{buildroot}|%attr(755,root,root) |" | \
	grep -v 'jira-' | \
    sort >> %{_tmppath}/files.main
# Put the etc, lib, and system subdirectories into the package
find %{buildroot}%{instprefix}/etc %{buildroot}%{instprefix}/lib %{buildroot}%{instprefix}/system -type d | \
	sed -e "s,^%{buildroot},%dir ," | \
	sort >> %{_tmppath}/files.main

# jetty
find %{buildroot}%{jettydir} ! -type d | \
	sed -e "s,^%{buildroot},," | \
	grep -v '/opennms-remoting' | \
	grep -v '/opennms/source/' | \
	grep -v '/WEB-INF/[^/]*\.xml$' | \
	grep -v '/WEB-INF/[^/]*\.properties$' | \
	grep -v '/WEB-INF/jsp/alarm/ncs' | \
	grep -v '/WEB-INF/jsp/ncs/' | \
	grep -v '/WEB-INF/lib/org.opennms.features.ncs.ncs' | \
	sort >> %{_tmppath}/files.jetty
find %{buildroot}%{jettydir}/*/WEB-INF/*.xml | \
	sed -e "s,^%{buildroot},%config ," | \
	grep -v '/opennms-remoting' | \
	grep -v '/WEB-INF/ncs' | \
	sort >> %{_tmppath}/files.jetty
find %{buildroot}%{jettydir} -type d | \
	sed -e "s,^%{buildroot},%dir ," | \
	grep -v '/opennms-remoting' | \
	sort >> %{_tmppath}/files.jetty

cd -

%clean
rm -rf %{buildroot}

##############################################################################
# file setup
##############################################################################

%files
%defattr(664 root root 775)

%files core -f %{_tmppath}/files.main
%defattr(664 root root 775)
%exclude %dir %{instprefix}/etc/drools-engine.d/ncs
%attr(755,root,root)	%{profiledir}/%{name}.sh
%attr(755,root,root)	%{logdir}
%attr(640,root,root)	%config(noreplace) %{instprefix}/etc/users.xml
			%{instprefix}/data
			%{instprefix}/deploy

%files docs
%defattr(644 root root 755)
%{_docdir}/%{name}-%{version}

%files remote-poller
%attr(755,root,root) %{_initrddir}/opennms-remote-poller
%attr(755,root,root) %config(noreplace) %{_sysconfdir}/sysconfig/opennms-remote-poller
%attr(755,root,root) %{bindir}/remote-poller.sh
%{instprefix}/bin/remote-poller.jar

%files jmx-config-generator
%attr(755,root,root) %{bindir}/jmx-config-generator
%{instprefix}/lib/opennms_jmx_config_generator.jar

%files ncs
%defattr(644 root root 755)
%{instprefix}/lib/org.opennms.features.ncs.ncs-*.jar
%{jettydir}/%{servletdir}/WEB-INF/lib/org.opennms.features.ncs.ncs-*.jar
%dir %{instprefix}/etc/drools-engine.d/ncs
%config(noreplace) %{instprefix}/etc/drools-engine.d/ncs/*
%config(noreplace) %{instprefix}/etc/ncs-northbounder-configuration.xml
%{sharedir}/xsds/ncs-*.xsd
%config %{jettydir}/%{servletdir}/WEB-INF/ncs*.xml
%config %{jettydir}/%{servletdir}/WEB-INF/jsp/alarm/ncs-*
%config %{jettydir}/%{servletdir}/WEB-INF/jsp/ncs
%dir %{sharedir}/etc-pristine/drools-engine.d/ncs
%{sharedir}/etc-pristine/drools-engine.d/ncs/*
%{sharedir}/etc-pristine/ncs-northbounder-configuration.xml

%files source
%defattr(644 root root 755)
%{jettydir}/opennms/source/*

%files webapp-jetty -f %{_tmppath}/files.jetty
%defattr(644 root root 755)
%config %{jettydir}/%{servletdir}/WEB-INF/*.properties

%files webapp-remoting
%defattr(644 root root 755)
%config %{jettydir}/opennms-remoting/WEB-INF/*.xml
%{jettydir}/opennms-remoting

%files plugins

%files plugin-northbounder-jms
%defattr(644 root root 755)
%{instprefix}/lib/opennms-alarm-northbounder-jms-*.jar
%config(noreplace) %{instprefix}/etc/jms-northbounder-configuration.xml
%{sharedir}/etc-pristine/jms-northbounder-configuration.xml

%files plugin-provisioning-dns
%defattr(664 root root 775)
%{instprefix}/lib/opennms-dns-provisioning-adapter*.jar

%files plugin-provisioning-reverse-dns
%defattr(664 root root 775)
%{instprefix}/lib/opennms-reverse-dns-provisioning-adapter*.jar

%files plugin-provisioning-rancid
%defattr(664 root root 775)
%{instprefix}/lib/opennms-rancid-provisioning-adapter*.jar

%files plugin-provisioning-snmp-asset
%defattr(664 root root 775)
%{instprefix}/lib/opennms-snmp-asset-provisioning-adapter*.jar
%config(noreplace) %{instprefix}/etc/snmp-asset-adapter-configuration.xml
%{sharedir}/etc-pristine/snmp-asset-adapter-configuration.xml

%files plugin-provisioning-snmp-hardware-inventory
%defattr(664 root root 775)
%{instprefix}/lib/opennms-snmp-hardware-inventory-provisioning-adapter*.jar
%config(noreplace) %{instprefix}/etc/snmp-hardware-inventory-adapter-configuration.xml
%{sharedir}/etc-pristine/snmp-hardware-inventory-adapter-configuration.xml

%files plugin-protocol-cifs
%defattr(664 root root 775)
%{instprefix}/lib/org.opennms.protocols.cifs*.jar

%files plugin-ticketer-jira
%defattr(664 root root 775)
%{instprefix}/system/org/opennms/features/jira-troubleticketer/*/jira-*.jar
%{instprefix}/system/org/opennms/features/jira-troubleticketer/*/jira-*.jar.sha1
%{instprefix}/system/org/opennms/features/jira-client/*/jira-*.jar
%{instprefix}/system/org/opennms/features/jira-client/*/jira-*.jar.sha1
%config(noreplace) %{instprefix}/etc/jira.properties
%{sharedir}/etc-pristine/jira.properties

%files plugin-ticketer-otrs
%defattr(664 root root 775)
%{instprefix}/lib/opennms-integration-otrs-*.jar
%config(noreplace) %{instprefix}/etc/otrs.properties
%{sharedir}/etc-pristine/otrs.properties

%files plugin-ticketer-rt
%defattr(664 root root 775)
%{instprefix}/lib/opennms-integration-rt-*.jar
%config(noreplace) %{instprefix}/etc/rt.properties
%{sharedir}/etc-pristine/rt.properties

%files plugin-protocol-dhcp
%defattr(664 root root 775)
%config(noreplace) %{instprefix}/etc/dhcp*.xml
%{instprefix}/lib/jdhcp-*.jar
%{instprefix}/lib/org.opennms.protocols.dhcp*.jar
%{sharedir}/etc-pristine/dhcp*.xml
%{sharedir}/xsds/dhcp*.xsd

%files plugin-protocol-nsclient
%defattr(664 root root 775)
%config(noreplace) %{instprefix}/etc/nsclient*.xml
%{instprefix}/etc/examples/nsclient*.xml
%{instprefix}/lib/org.opennms.protocols.nsclient*.jar
%{sharedir}/etc-pristine/nsclient*.xml
%{sharedir}/xsds/nsclient*.xsd

%files plugin-protocol-radius
%defattr(664 root root 775)
%{instprefix}/lib/*jradius-*.jar
%{instprefix}/lib/org.opennms.protocols.radius*.jar

%files plugin-protocol-xmp
%defattr(664 root root 775)
%config(noreplace) %{instprefix}/etc/xmp*.xml
%{instprefix}/lib/org.opennms.protocols.xmp-*.jar
%{instprefix}/lib/xmp-*.jar
%{sharedir}/etc-pristine/xmp*.xml
%{sharedir}/xsds/xmp*.xsd

%files plugin-collector-juniper-tca
%defattr(664 root root 775)
%config(noreplace) %{instprefix}/etc/tca*.xml
%config(noreplace) %{instprefix}/etc/datacollection/juniper-tca*
%config(noreplace) %{instprefix}/etc/snmp-graph.properties.d/juniper-tca*
%{instprefix}/lib/org.opennms.features.juniper-tca-collector-*.jar
%{sharedir}/etc-pristine/tca*.xml
%{sharedir}/etc-pristine/datacollection/juniper-tca*
%{sharedir}/etc-pristine/snmp-graph.properties.d/juniper-tca*

%files plugin-collector-vtdxml-handler
%defattr(664 root root 775)
%{instprefix}/lib/opennms-vtdxml-collector-handler-*.jar
%{instprefix}/lib/vtd-xml-*.jar

%post -p /bin/bash docs
ROOT_INST="$RPM_INSTALL_PREFIX0"
SHARE_INST="$RPM_INSTALL_PREFIX1"
LOG_INST="$RPM_INSTALL_PREFIX2"
[ -z "$ROOT_INST"  ] && ROOT_INST="%{instprefix}"
[ -z "$SHARE_INST" ] && SHARE_INST="%{sharedir}"
[ -z "$LOG_INST"   ] && LOG_INST="%{logdir}"

printf -- "- making symlink for $ROOT_INST/docs... "
if [ -e "$ROOT_INST/docs" ] && [ ! -L "$ROOT_INST/docs" ]; then
	echo "failed: $ROOT_INST/docs is a real directory, but it should be a symlink to %{_docdir}/%{name}-%{version}."
else
	rm -rf "$ROOT_INST/docs"
	ln -sf "%{_docdir}/%{name}-%{version}" "$ROOT_INST/docs"
	echo "done"
fi

printf -- "- making symlink for $ROOT_INST/jetty-webapps/%{servletdir}/docs... "
if [ -e "$ROOT_INST/jetty-webapps/%{servletdir}/docs" ] && [ ! -L "$ROOT_INST/jetty-webapps/%{servletdir}/docs" ]; then
  echo "failed: $ROOT_INST/jetty-webapps/%{servletdir}/docs is a real directory, but it should be a symlink to %{_docdir}/%{name}-%{version}."
else
  rm -rf "$ROOT_INST/jetty-webapps/%{servletdir}/docs"
  ln -sf "%{_docdir}/%{name}-%{version}" "$ROOT_INST/jetty-webapps/%{servletdir}/docs"
  echo "done"
fi

%postun -p /bin/bash docs
ROOT_INST="$RPM_INSTALL_PREFIX0"
SHARE_INST="$RPM_INSTALL_PREFIX1"
LOG_INST="$RPM_INSTALL_PREFIX2"
[ -z "$ROOT_INST"  ] && ROOT_INST="%{instprefix}"
[ -z "$SHARE_INST" ] && SHARE_INST="%{sharedir}"
[ -z "$LOG_INST"   ] && LOG_INST="%{logdir}"

if [ "$1" = 0 ]; then
	if [ -L "$ROOT_INST/docs" ]; then
		rm -f "$ROOT_INST/docs"
	fi
fi

if [ "$1" = 0 ]; then
  if [ -L "$ROOT_INST/jetty-webapps/%{servletdir}/docs" ]; then
    rm -f "$ROOT_INST/jetty-webapps/%{servletdir}/docs"
  fi
fi

%pre -p /bin/bash core
ROOT_INST="$RPM_INSTALL_PREFIX0"
[ -z "$ROOT_INST"  ] && ROOT_INST="%{instprefix}"
if [ -e "${ROOT_INST}/etc/users.xml" ]; then
	chmod 640 "${ROOT_INST}/etc/users.xml"
fi

%post -p /bin/bash core
ROOT_INST="$RPM_INSTALL_PREFIX0"
SHARE_INST="$RPM_INSTALL_PREFIX1"
LOG_INST="$RPM_INSTALL_PREFIX2"
[ -z "$ROOT_INST"  ] && ROOT_INST="%{instprefix}"
[ -z "$SHARE_INST" ] && SHARE_INST="%{sharedir}"
[ -z "$LOG_INST"   ] && LOG_INST="%{logdir}"

if [ -n "$DEBUG" ]; then
	env | grep RPM_INSTALL_PREFIX | sort -u
fi

for prefix in lib lib64; do
	if [ -d "/usr/$prefix/systemd" ]; then
		SYSTEMDDIR="/usr/$prefix/systemd/system"
		printf -- "- installing service into $SYSTEMDDIR... "
		install -d -m 755 "$SYSTEMDDIR"
		install -m 644 "${ROOT_INST}/etc/opennms.service" "${SYSTEMDDIR}"/
		echo "done"
	fi
done

if [ "$ROOT_INST/logs" != "$LOG_INST" ]; then
	printf -- "- making symlink for $ROOT_INST/logs... "
	if [ -e "$ROOT_INST/logs" ] && [ ! -L "$ROOT_INST/logs" ]; then
		echo "failed: $ROOT_INST/logs is a real directory or file, but it should be a symlink to $LOG_INST."
		echo "Your %{_descr} install may not function properly."
	else
		rm -rf "$ROOT_INST/logs"
		ln -sf "$LOG_INST" "$ROOT_INST/logs"
		echo "done"
	fi
fi

if [ "$ROOT_INST/share" != "$SHARE_INST" ]; then
	printf -- "- making symlink for $ROOT_INST/share... "
	if [ -e "$ROOT_INST/share" ] && [ ! -L "$ROOT_INST/share" ]; then
		echo "failed: $ROOT_INST/share is a real directory, but it should be a symlink to $SHARE_INST."
		echo "Your %{_descr} install may not function properly."
	else
		rm -rf "$ROOT_INST/share"
		ln -sf "$SHARE_INST" "$ROOT_INST/share"
		echo "done"
	fi
fi

printf -- "- moving *.sql.rpmnew files (if any)... "
if [ `ls $ROOT_INST/etc/*.sql.rpmnew 2>/dev/null | wc -l` -gt 0 ]; then
	for i in $ROOT_INST/etc/*.sql.rpmnew; do
		mv $i ${i%%%%.rpmnew}
	done
fi
echo "done"

printf -- "- checking for old update files... "

JAR_UPDATES=`find $ROOT_INST/lib/updates -name \*.jar   -exec rm -rf {} \; -print 2>/dev/null | wc -l`
CLASS_UPDATES=`find $ROOT_INST/lib/updates -name \*.class -exec rm -rf {} \; -print 2>/dev/null | wc -l`
TOTAL_UPDATES=`expr $JAR_UPDATES + $CLASS_UPDATES`
if [ "$TOTAL_UPDATES" -gt 0 ]; then
	echo "FOUND"
	echo ""
	echo "WARNING: $TOTAL_UPDATES old update files were found in your"
	echo "$ROOT_INST/lib/updates directory.  They have been deleted"
	echo "because they should now be out of date."
	echo ""
else
	echo "done"
fi

rm -f $ROOT_INST/etc/configured
for dir in /etc /etc/rc.d; do
	if [ -d "$dir" ]; then
		ln -sf $ROOT_INST/bin/opennms $dir/init.d/opennms
		break
	fi
done

for LIBNAME in jicmp jicmp6 jrrd jrrd2; do
	if [ `grep "opennms.library.${LIBNAME}" "$ROOT_INST/etc/libraries.properties" 2>/dev/null | wc -l` -eq 0 ]; then
		LIBRARY_PATH=`rpm -ql "${LIBNAME}" 2>/dev/null | grep "/lib${LIBNAME}.so\$" | head -n 1`
		if [ -n "$LIBRARY_PATH" ]; then
			echo "opennms.library.${LIBNAME}=${LIBRARY_PATH}" >> "$ROOT_INST/etc/libraries.properties"
		fi
	fi
done

printf -- "- cleaning up \$OPENNMS_HOME/data... "
if [ -d "$ROOT_INST/data" ]; then
	find "$ROOT_INST/data/"* -maxdepth 0 -name tmp -prune -o -print0 | xargs -0 rm -rf
	find "$ROOT_INST/data/tmp/"* -maxdepth 0 -name README -prune -o -print0 | xargs -0 rm -rf
fi
echo "done"

if [ ! -e "$ROOT_INST/etc/java.conf" ]; then
	"$ROOT_INST/bin/runjava" "-s"
fi

echo ""
echo " *** Installation complete.  You must still run the installer at"
echo " *** \$OPENNMS_HOME/bin/install -dis to be sure your database is up"
echo " *** to date before you start %{_descr}.  See the install guide at"
echo " *** http://www.opennms.org/wiki/Installation:RPM and the"
echo " *** release notes for details."
echo ""

%postun -p /bin/bash core
ROOT_INST="$RPM_INSTALL_PREFIX0"
SHARE_INST="$RPM_INSTALL_PREFIX1"
LOG_INST="$RPM_INSTALL_PREFIX2"
[ -z "$ROOT_INST"  ] && ROOT_INST="%{instprefix}"
[ -z "$SHARE_INST" ] && SHARE_INST="%{sharedir}"
[ -z "$LOG_INST"   ] && LOG_INST="%{logdir}"

if [ "$1" = 0 ]; then
	for dir in logs share; do
		if [ -L "$ROOT_INST/$dir" ]; then
			rm -f "$ROOT_INST/$dir"
		fi
	done
fi

%changelog
* Thu Feb 10 2011 Benjamin Reed <ranger@opennms.org>
- Initial RPM package: see https://github.com/OpenNMS/opennms/commits/develop/tools/packages/opennms/opennms.spec for the full commit log.
