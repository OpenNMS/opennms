#
#  $Id$
#
# The version used to be passed from build.xml. It's hardcoded here
# the build system generally passes --define "version X" to rpmbuild.
%{!?version:%define version 1.3.10}
# The release number is set to 0 unless overridden
%{!?releasenumber:%define releasenumber 0}
# The install prefix becomes $MINION_HOME in the finished package
%{!?minioninstprefix:%define minioninstprefix /opt/minion}
# The path where the repositories will live
%{!?minionrepoprefix:%define minionrepoprefix /opt/minion/repositories}
# Where Systemd files live
%{!?_unitdir:%define _unitdir /lib/systemd/system}

# Description
%{!?_name:%define _name opennms}
%{!?_descr:%define _descr OpenNMS}
%{!?packagedir:%define packagedir %{_name}-%version-%{releasenumber}}

%{!?_java:%define _java java-1.8.0-openjdk-devel}

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

Name:          %{_name}-minion
Summary:       OpenNMS Minion
Release:       %releasenumber
Version:       %version
License:       LGPL/AGPL
Group:         Applications/System
BuildArch:     noarch

Source:        %{_name}-source-%{version}-%{releasenumber}.tar.gz
URL:           http://www.opennms.org/wiki/Minion
BuildRoot:     %{_tmppath}/%{name}-%{version}-root

BuildRequires:	%{_java}
BuildRequires:	libxslt

Requires:       openssh
Requires(pre):  /usr/bin/getent
Requires(pre):  /usr/sbin/groupadd
Requires(pre):  /usr/sbin/useradd
Requires(pre):  /sbin/nologin
Requires:       /sbin/nologin
Requires:       /usr/bin/id
Requires:       /usr/bin/sudo
Requires(post): util-linux
Requires:       util-linux
Requires:       jicmp >= 2.0.0
Requires(pre):  jicmp >= 2.0.0
Requires:       jicmp6 >= 2.0.0
Requires(pre):  jicmp6 >= 2.0.0

Conflicts:      %{name}-container        < %{version}-%{release}
Conflicts:      %{name}-features-core    < %{version}-%{release}
Conflicts:      %{name}-features-default < %{version}-%{release}

Prefix:         %{minioninstprefix}

%description
OpenNMS Minion is a container infrastructure for distributed, scalable network
management and monitoring.

http://www.opennms.org/wiki/Minion

%{extrainfo}
%{extrainfo2}

%package container
Summary:   Obsolete: Provided for Upgrade Compatibility
Group:     Applications/System
Requires:  %{name} >= %{version}-%{release}

%description container
This package is obsolete, it only exists to ease upgrades.

%package features-core
Summary:   Obsolete: Provided for Upgrade Compatibility
Group:     Applications/System
Requires:  %{name} >= %{version}-%{release}

%description features-core
This package is obsolete, it only exists to ease upgrades.

%package features-default
Summary:   Obsolete: Provided for Upgrade Compatibility
Group:     Applications/System
Requires:  %{name} >= %{version}-%{release}

%description features-default
This package is obsolete, it only exists to ease upgrades.

%prep

TAR="$(command -v gtar || which gtar || command -v tar || which tar)"
if "$TAR" --uid=0 --gid=0 -cf /dev/null "$TAR" 2>/dev/null; then
  TAR="$TAR --uid=0 --gid=0"
fi
$TAR -xzf %{_sourcedir}/%{_name}-source-%{version}-%{release}.tar.gz -C "%{_builddir}"
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

tools/packages/minion/create-minion-assembly.sh $EXTRA_ARGS

TAR="$(command -v gtar || which gtar || command -v tar || which tar)"
if "$TAR" --uid=0 --gid=0 -cf /dev/null "$TAR" 2>/dev/null; then
  TAR="$TAR --uid=0 --gid=0"
fi

# Extract the minion assembly
mkdir -p %{buildroot}%{minioninstprefix}
$TAR -xzf %{_builddir}/%{_name}-%{version}-%{release}/opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz -C %{buildroot}%{minioninstprefix} --strip-components=1

# Remove extraneous directories that start with "d"
rm -rf %{buildroot}%{minioninstprefix}/{data,debian,demos}

# Create a default org.opennms.minion.controller.cfg file
echo "location = MINION" > %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg
echo "id = 00000000-0000-0000-0000-000000ddba11" >> %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg

# fix the init script for RedHat/CentOS layout
mkdir -p "%{buildroot}%{_initrddir}"
sed -e "s,^SYSCONFDIR[ \t]*=.*$,SYSCONFDIR=%{_sysconfdir}/sysconfig,g" \
	-e 's,^PING_REQUIRED=FALSE,PING_REQUIRED=TRUE,g' \
	-e "s,^MINION_HOME[ \t]*=.*$,MINION_HOME=%{minioninstprefix},g" \
	"%{buildroot}%{minioninstprefix}/etc/minion.init" \
	> "%{buildroot}%{_initrddir}"/minion
chmod 755 "%{buildroot}%{_initrddir}"/minion
rm -f '%{buildroot}%{minioninstprefix}/etc/minion.init'

mkdir -p "%{buildroot}%{_unitdir}"
install -c -m 644 "%{buildroot}%{minioninstprefix}/etc/minion.service" "%{buildroot}%{_unitdir}/minion.service"

# move minion.conf to the sysconfig dir
install -d -m 755 %{buildroot}%{_sysconfdir}/sysconfig
mv "%{buildroot}%{minioninstprefix}/etc/minion.conf" "%{buildroot}%{_sysconfdir}/sysconfig/minion"

# delete the debian files
rm -rf "%{buildroot}%{minioninstprefix}/debian"

### FILE LISTS FOR %files ###

# minion package files
find %{buildroot}%{minioninstprefix} ! -type d | \
    grep -v %{minioninstprefix}/bin | \
    grep -v %{minioninstprefix}/etc | \
    sed -e "s|^%{buildroot}|%attr(644,minion,minion) |" | \
    sort > %{_tmppath}/files.minion

# org.opennms.*, org.apache.karaf.features.cfg, and org.ops4j.pax.logging.cfg should
# be special-cased to not be replaced by default (and create .rpmnew files)
find %{buildroot}%{minioninstprefix}/etc ! -type d | \
    grep -E 'etc/(org.opennms.*|org.apache.karaf.features.cfg|org.ops4j.pax.logging.cfg)$' | \
    sed -e "s|^%{buildroot}|%attr(644,minion,minion) %config(noreplace) |" | \
    sort >> %{_tmppath}/files.minion

# all other etc files should replace by default (and create .rpmsave files)
find %{buildroot}%{minioninstprefix}/etc ! -type d | \
    grep -v etc/org.opennms. | \
    grep -v etc/org.apache.karaf.features.cfg | \
    grep -v etc/org.ops4j.pax.logging.cfg | \
    grep -v etc/featuresBoot.d | \
    sed -e "s|^%{buildroot}|%attr(644,minion,minion) %config |" | \
    sort >> %{_tmppath}/files.minion

# binary files
find %{buildroot}%{minioninstprefix}/bin ! -type d | \
    sed -e "s|^%{buildroot}|%attr(755,minion,minion) |" | \
    sort >> %{_tmppath}/files.minion

# directories
find %{buildroot}%{minioninstprefix} -type d | \
    sed -e "s,^%{buildroot},%dir ," | \
    sort >> %{_tmppath}/files.minion

%clean
rm -rf %{buildroot}

%files -f %{_tmppath}/files.minion
%defattr(664 minion minion 775)
%attr(755,minion,minion) %{_initrddir}/minion
%attr(644,minion,minion) %{_unitdir}/minion.service
%attr(644,minion,minion) %config(noreplace) %{_sysconfdir}/sysconfig/minion
%attr(644,minion,minion) %{minioninstprefix}/etc/featuresBoot.d/.readme

%files container

%files features-core

%files features-default

### PREINSTALL ###

%pre
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

getent group minion >/dev/null || groupadd -r minion
getent passwd minion >/dev/null || \
	useradd -r -g minion -d "${ROOT_INST}" -s /sbin/nologin \
	-c "OpenNMS Minion" minion
exit 0


### POSTINSTALL ###

%post
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

# Clean out the data directory
if [ -d "${ROOT_INST}/data" ]; then
    find "$ROOT_INST/data/"* -maxdepth 0 -name tmp -prune -o -print0 | xargs -0 rm -rf
    if [ -d "${ROOT_INST}/data/tmp"  ]; then
        find "$ROOT_INST/data/tmp/"* -maxdepth 0 -name README -prune -o -print0 | xargs -0 rm -rf
    fi
fi

# Clean out .m2 directory
if [ -d "${ROOT_INST}/.m2" ]; then
   rm -rf "${ROOT_INST}/.m2"
fi

# Generate an SSH key if necessary
if [ ! -f "${ROOT_INST}/etc/host.key" ]; then
    /usr/bin/ssh-keygen -m PEM -t rsa -N "" -b 4096 -f "${ROOT_INST}/etc/host.key"
    chown minion:minion "${ROOT_INST}/etc/"host.key*
fi

# Set up ICMP for non-root users
"${ROOT_INST}/bin/ensure-user-ping.sh" "minion" >/dev/null 2>&1 || echo "WARNING: Unable to enable ping by the 'minion' user. Try running ${ROOT_INST}/bin/ensure-user-ping.sh manually or run the minion as root."

# Generate a new UUID to replace the default UUID if it is still present
UUID=$(/usr/bin/uuidgen -t)
sed -i "s|id = 00000000-0000-0000-0000-000000ddba11|id = $UUID|g" "${ROOT_INST}/etc/org.opennms.minion.controller.cfg"

# Remove the directory used as the local Maven repo cache
rm -rf "${ROOT_INST}/repositories/.local"


### PRE-UN-INSTALLATION ###

%preun -p /bin/bash
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

if [ "$1" = 0 ] && [ -x "%{_initrddir}/minion" ]; then
	%{_initrddir}/minion stop || :
fi

