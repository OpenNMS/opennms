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

# Description
%{!?_name:%define _name "opennms"}
%{!?_descr:%define _descr "OpenNMS"}
%{!?packagedir:%define packagedir %{_name}-%version-%{releasenumber}}

%{!?_java:%define _java jre-1.8.0}

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

Requires(pre): %{name}-features-default = %{version}-%{release}
Requires:      %{name}-features-default = %{version}-%{release}

Prefix:        %{minioninstprefix}

%description
OpenNMS Minion is a container infrastructure for distributed, scalable network
management and monitoring.

http://www.opennms.org/wiki/Minion

%{extrainfo}
%{extrainfo2}

%package container
Summary:       Minion Container
Group:         Applications/System
Requires:      openssh
Requires(pre): %{_java}
Requires:      %{_java}
Requires(pre): /usr/bin/getent
Requires(pre): /usr/sbin/groupadd
Requires(pre): /usr/sbin/useradd
Requires(pre): /sbin/nologin
Requires:      /sbin/nologin
Requires:      /usr/bin/id
Requires:      /usr/bin/sudo

%description container
Minion Container

%{extrainfo}
%{extrainfo2}

%package features-core
Summary:        Minion Core Features
Group:          Applications/System
Requires(pre):  %{name}-container = %{version}-%{release}
Requires:       %{name}-container = %{version}-%{release}
Requires(post): util-linux
Requires:       util-linux

%description features-core
Minion Core Features

%{extrainfo}
%{extrainfo2}

%package features-default
Summary:       Minion Default Features
Group:         Applications/System
Requires(pre): %{name}-features-core = %{version}-%{release}
Requires:      %{name}-features-core = %{version}-%{release}

%description features-default
Minion Default Features

%{extrainfo}
%{extrainfo2}

%prep

tar zxf %{_sourcedir}/%{_name}-source-%{version}-%{release}.tar.gz -C "%{_builddir}"
%define setupdir %{packagedir}

%setup -D -T -n %setupdir

%build

rm -rf %{buildroot}

%install

export OPTS_MAVEN="-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false"
export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export OPTS_ASSEMBLIES="-Passemblies"
export OPTS_PROFILES="-Prun-expensive-tasks"
export COMPILE_PROJECTS="org.opennms.features.minion.container:karaf,org.opennms.features.minion:core-repository,org.opennms.features.minion:repository,org.opennms.features.minion:container-parent,org.opennms.features.minion:core-parent,org.opennms.features.minion:org.opennms.features.minion.heartbeat,org.opennms.features.minion:repository,org.opennms.features.minion:shell"
export ASSEMBLY_PROJECTS=":org.opennms.assemblies.minion"

if [ "%{enable_snapshots}" = 1 ]; then
	OPTS_ENABLE_SNAPSHOTS="-Denable.snapshots=true"
	OPTS_UPDATE_POLICY="-DupdatePolicy=always"
fi

# always build the root POM, just to be sure inherited properties/plugin/dependencies are right
./compile.pl -N $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" -Dopennms.home="%{instprefix}" install
if [ "%{skip_compile}" = 1 ]; then
	echo "=== SKIPPING FULL COMPILE ==="
	echo "Projects: ${ASSEMBLY_PROJECTS}"
	./compile.pl $OPTS_MAVEN $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY $OPTS_PROFILES $OPTS_ASSEMBLIES -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" \
		-Dopennms.home="%{instprefix}" \
		--projects "${ASSEMBLY_PROJECTS}" \
		install
else
	# get the full list of minion projects to build
	echo "=== RUNNING COMPILE ==="
	echo "Projects: ${COMPILE_PROJECTS},${ASSEMBLY_PROJECTS}"
	./compile.pl $OPTS_MAVEN $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY $OPTS_PROFILES $OPTS_ASSEMBLIES -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" \
		-Dopennms.home="%{instprefix}" \
		--projects "${COMPILE_PROJECTS},${ASSEMBLY_PROJECTS}" --also-make \
		install
fi

# Extract the minion assembly
mkdir -p %{buildroot}%{minioninstprefix}
tar zxf %{_builddir}/%{_name}-%{version}-%{release}/opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz -C %{buildroot}%{minioninstprefix} --strip-components=1

# Remove the data directory
rm -rf %{buildroot}%{minioninstprefix}/data
# Remove the demos directory
rm -rf %{buildroot}%{minioninstprefix}/demos

# Generate SHA-1 checksums for the JAR and XML artifacts in the feature repositories
for FILE in `find %{buildroot}%{minioninstprefix}/{repositories,system} ! -type d -name "*.xml" -or -name "*.jar"`; do
  sha1sum $FILE | cut -d ' ' -f 1 > $FILE.sha1
done

# Create a default org.opennms.minion.controller.cfg file
echo "location = MINION" > %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg
echo "id = 00000000-0000-0000-0000-000000ddba11" >> %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg

# fix the init script for RedHat/CentOS layout
mkdir -p "%{buildroot}%{_initrddir}"
sed -e "s,^SYSCONFDIR[ \t]*=.*$,SYSCONFDIR=%{_sysconfdir}/sysconfig,g" -e "s,^MINION_HOME[ \t]*=.*$,MINION_HOME=%{minioninstprefix},g" "%{buildroot}%{minioninstprefix}/etc/minion.init" > "%{buildroot}%{_initrddir}"/minion
chmod 755 "%{buildroot}%{_initrddir}"/minion
rm -f '%{buildroot}%{minioninstprefix}/etc/minion.init'

# move minion.conf to the sysconfig dir
install -d -m 755 %{buildroot}%{_sysconfdir}/sysconfig
mv "%{buildroot}%{minioninstprefix}/etc/minion.conf" "%{buildroot}%{_sysconfdir}/sysconfig/minion"

# container package files
find %{buildroot}%{minioninstprefix} ! -type d | \
    grep -v %{minioninstprefix}/bin | \
    grep -v %{minionrepoprefix} | \
    grep -v %{minioninstprefix}/etc/featuresBoot.d | \
    grep -v %{minioninstprefix}/etc/org.opennms.minion.controller.cfg | \
    sed -e "s|^%{buildroot}|%attr(644,minion,minion) |" | \
    sort > %{_tmppath}/files.container
find %{buildroot}%{minioninstprefix}/bin ! -type d | \
    sed -e "s|^%{buildroot}|%attr(755,minion,minion) |" | \
    sort >> %{_tmppath}/files.container
# Exclude subdirs of the repository directory
find %{buildroot}%{minioninstprefix} -type d | \
    grep -v %{minionrepoprefix}/ | \
    sed -e "s,^%{buildroot},%dir ," | \
    sort >> %{_tmppath}/files.container

%clean
rm -rf %{buildroot}

%files
%defattr(664 root root 775)

%files container -f %{_tmppath}/files.container
%defattr(664 minion minion 775)
%attr(755,minion,minion) %{_initrddir}/minion
%attr(644,minion,minion) %config(noreplace) %{_sysconfdir}/sysconfig/minion
%attr(644,minion,minion) %{minioninstprefix}/etc/featuresBoot.d/.readme

%pre container
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

getent group minion >/dev/null || groupadd -r minion
getent passwd minion >/dev/null || \
	useradd -r -g minion -d "${ROOT_INST}" -s /sbin/nologin \
	-c "OpenNMS Minion" minion
exit 0

%post container
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

# Clean out the data directory
rm -rf "${ROOT_INST}/data"
# Generate an SSH key if necessary
if [ ! -f "${ROOT_INST}/etc/host.key" ]; then
    /usr/bin/ssh-keygen -t rsa -N "" -b 4096 -f "${ROOT_INST}/etc/host.key"
    chown minion:minion "${ROOT_INST}/etc/"host.key*
fi

%files features-core
%defattr(644 minion minion 755)
%{minionrepoprefix}/core
%config(noreplace) %{minioninstprefix}/etc/org.opennms.minion.controller.cfg

%post features-core
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

# Generate a new UUID to replace the default UUID if it is still present
UUID=$(/usr/bin/uuidgen -t)
sed -i "s|id = 00000000-0000-0000-0000-000000ddba11|id = $UUID|g" "${ROOT_INST}/etc/org.opennms.minion.controller.cfg"
# Remove the directory used as the local Maven repo cache
rm -rf "${ROOT_INST}/repositories/.local"

%files features-default
%defattr(644 minion minion 755)
%{minionrepoprefix}/default

%post features-default
# Remove the directory used as the local Maven repo cache
rm -rf %{minionrepoprefix}/.local

%preun -p /bin/bash container
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

if [ "$1" = 0 ] && [ -x "%{_initrddir}/minion" ]; then
	%{_initrddir}/minion stop || :
fi

%postun -p /bin/bash container
ROOT_INST="${RPM_INSTALL_PREFIX0}"
[ -z "${ROOT_INST}" ] && ROOT_INST="%{minioninstprefix}"

if [ "$1" = 0 ] && [ -n "${ROOT_INST}" ] && [ -d "${ROOT_INST}" ]; then
	rm -rf "${ROOT_INST}" || echo "WARNING: failed to delete ${ROOT_INST}. You may have to clean it up yourself."
fi
