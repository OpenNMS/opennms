#
#  $Id$
#
# The version used to be passed from build.xml. It's hardcoded here
# the build system generally passes --define "version X" to rpmbuild.
%{!?version:%define version 1.3.10}
# The release number is set to 0 unless overridden
%{!?releasenumber:%define releasenumber 0}
# The install prefix becomes $SENTINEL_HOME in the finished package
%{!?sentinelinstprefix:%define sentinelinstprefix /opt/sentinel}
# The path where the repositories will live 
%{!?sentinelrepoprefix:%define sentinelrepoprefix /opt/sentinel/repositories}

# Description
%{!?_name:%define _name opennms}
%{!?_descr:%define _descr OpenNMS}
%{!?packagedir:%define packagedir %{_name}-%version-%{releasenumber}}

%{!?_java:%define _java jre-1.8.0}

%{!?extrainfo:%define extrainfo %{nil}}
%{!?extrainfo2:%define extrainfo2 %{nil}}
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

%define with_tests  0%{nil}

Name:          %{_name}-sentinel
Summary:       OpenNMS Sentinel
Release:       %releasenumber
Version:       %version
License:       LGPL/AGPL
Group:         Applications/System
BuildArch:     noarch

Source:        %{_name}-source-%{version}-%{releasenumber}.tar.gz
URL:           http://www.opennms.org/wiki/Sentinel
BuildRoot:     %{_tmppath}/%{name}-%{version}-root

BuildRequires:	%{_java}
BuildRequires:	libxslt

#Requires:       jicmp >= 2.0.0
#Requires(pre):  jicmp >= 2.0.0
#Requires:       jicmp6 >= 2.0.0
#Requires(pre):  jicmp6 >= 2.0.0
Requires:       openssh
Requires(post): util-linux
Requires:       util-linux
Requires(pre):  %{_java}
Requires:       %{_java}
Requires(pre):  /usr/bin/getent
Requires(pre):  /usr/sbin/groupadd
Requires(pre):  /usr/sbin/useradd
Requires(pre):  /sbin/nologin
Requires:       /sbin/nologin
Requires:       /usr/bin/id
Requires:       /usr/bin/sudo

Prefix:        %{sentinelinstprefix}

%description
OpenNMS Sentinel is a container for running a subset of OpenNMS
services in a standalone container, suitable for horizontally
scaling some subsystems, like flow telemetry processing.

http://www.opennms.org/wiki/Sentinel

%{extrainfo}
%{extrainfo2}


%prep

tar zxf %{_sourcedir}/%{_name}-source-%{version}-%{release}.tar.gz -C "%{_builddir}"
%define setupdir %{packagedir}

%setup -D -T -n %setupdir

%build

rm -rf %{buildroot}

%install

export EXTRA_ARGS=""
if [ "%{enable_snapshots}" = 1 ]; then
	EXTRA_ARGS="-s"
fi

if [ "%{skip_compile}" = 1 ]; then
	EXTRA_ARGS="$EXTRA_ARGS -c"
fi

tools/packages/sentinel/create-sentinel-assembly.sh $EXTRA_ARGS

# Extract the sentinel assembly
mkdir -p %{buildroot}%{sentinelinstprefix}
tar zxf %{_builddir}/%{_name}-%{version}-%{release}/opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz -C %{buildroot}%{sentinelinstprefix} --strip-components=1

# Remove extraneous directories that start with "d"
rm -rf %{buildroot}%{sentinelinstprefix}/{data,debian,demos}

# fix the init script for RedHat/CentOS layout
mkdir -p "%{buildroot}%{_initrddir}"
sed -e "s,^SYSCONFDIR[ \t]*=.*$,SYSCONFDIR=%{_sysconfdir}/sysconfig,g" -e "s,^SENTINEL_HOME[ \t]*=.*$,SENTINEL_HOME=%{sentinelinstprefix},g" "%{buildroot}%{sentinelinstprefix}/etc/sentinel.init" > "%{buildroot}%{_initrddir}"/sentinel
chmod 755 "%{buildroot}%{_initrddir}"/sentinel
rm -f '%{buildroot}%{sentinelinstprefix}/etc/sentinel.init'

# move sentinel.conf to the sysconfig dir
install -d -m 755 %{buildroot}%{_sysconfdir}/sysconfig
mv "%{buildroot}%{sentinelinstprefix}/etc/sentinel.conf" "%{buildroot}%{_sysconfdir}/sysconfig/sentinel"

# sentinel package files
find %{buildroot}%{sentinelinstprefix} ! -type d | \
    grep -v %{sentinelinstprefix}/bin | \
    grep -v %{sentinelrepoprefix} | \
    grep -v %{sentinelinstprefix}/etc/featuresBoot.d | \
    sed -e "s|^%{buildroot}|%attr(644,sentinel,sentinel) |" | \
    sort > %{_tmppath}/files.sentinel
find %{buildroot}%{sentinelinstprefix}/bin ! -type d | \
    sed -e "s|^%{buildroot}|%attr(755,sentinel,sentinel) |" | \
    sort >> %{_tmppath}/files.sentinel
# Exclude subdirs of the repository directory
find %{buildroot}%{sentinelinstprefix} -type d | \
    grep -v %{sentinelrepoprefix}/ | \
    sed -e "s,^%{buildroot},%dir ," | \
    sort >> %{_tmppath}/files.sentinel

%clean
rm -rf %{buildroot}

%files -f %{_tmppath}/files.sentinel
%defattr(664 sentinel sentinel 775)
%attr(755,sentinel,sentinel) %{_initrddir}/sentinel
%attr(644,sentinel,sentinel) %config(noreplace) %{_sysconfdir}/sysconfig/sentinel
%attr(644,sentinel,sentinel) %{sentinelinstprefix}/etc/featuresBoot.d/.readme

%pre
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{sentinelinstprefix}"

getent group sentinel >/dev/null || groupadd -r sentinel
getent passwd sentinel >/dev/null || \
	useradd -r -g sentinel -d "${ROOT_INST}" -s /sbin/nologin \
	-c "OpenNMS Sentinel" sentinel
exit 0

%post
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{sentinelinstprefix}"

# Clean out the data directory
if [ -d "${ROOT_INST}/data" ]; then
    find "$ROOT_INST/data/"* -maxdepth 0 -name tmp -prune -o -print0 | xargs -0 rm -rf
    if [ -d "${ROOT_INST}/data/tmp"  ]; then
        find "$ROOT_INST/data/tmp/"* -maxdepth 0 -name README -prune -o -print0 | xargs -0 rm -rf
    fi
fi

# Remove the directory used as the local Maven repo cache
rm -rf "${ROOT_INST}/repositories/.local"

# Generate an SSH key if necessary
if [ ! -f "${ROOT_INST}/etc/host.key" ]; then
    /usr/bin/ssh-keygen -t rsa -N "" -b 4096 -f "${ROOT_INST}/etc/host.key"
    chown sentinel:sentinel "${ROOT_INST}/etc/"host.key*
fi

# Set up ICMP for non-root users
"${ROOT_INST}/bin/ensure-user-ping.sh" "sentinel" >/dev/null 2>&1 || echo "WARNING: Unable to enable ping by the 'sentinel' user. If you intend to run ping-related commands from the Sentinel container without running as root, try running ${ROOT_INST}/bin/ensure-user-ping.sh manually."

%preun -p /bin/bash
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{sentinelinstprefix}"

if [ "$1" = 0 ] && [ -x "%{_initrddir}/sentinel" ]; then
	%{_initrddir}/sentinel stop || :
fi

%postun -p /bin/bash
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{sentinelinstprefix}"

if [ "$1" = 0 ] && [ -n "${ROOT_INST}" ] && [ -d "${ROOT_INST}" ]; then
	rm -rf "${ROOT_INST}" || echo "WARNING: failed to delete ${ROOT_INST}. You may have to clean it up yourself."
fi
