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
Requires(pre): %{jdk}
Requires:      %{jdk}
Requires(pre): openssh
Requires:      openssh

%description container
Minion Container

%{extrainfo}
%{extrainfo2}

%package features-core
Summary:       Minion Core Features
Group:         Applications/System
Requires(pre): %{name}-container = %{version}-%{release}
Requires:      %{name}-container = %{version}-%{release}
Requires(pre): util-linux
Requires:      util-linux

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

tar -xvzf %{_sourcedir}/%{_name}-source-%{version}-%{release}.tar.gz -C "%{_builddir}"
%define setupdir %{packagedir}

%setup -D -T -n %setupdir

%build

rm -rf %{buildroot}

%install

export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export PROJECTS="org.opennms.features.minion.container:karaf,org.opennms.features.minion:core-repository,org.opennms.features.minion:repository"

if [ "%{skip_compile}" = 1 ]; then
	echo "=== SKIPPING FULL COMPILE ==="
	echo "Projects: ${PROJECTS}"
	if [ "%{enable_snapshots}" = 1 ]; then
		OPTS_ENABLE_SNAPSHOTS="-Denable.snapshots=true"
		OPTS_UPDATE_POLICY="-DupdatePolicy=always"
	fi
	./compile.pl -N $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" -Dopennms.home="%{instprefix}" install
	./compile.pl $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dbuild=all -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" \
		-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false \
		-Dopennms.home="%{instprefix}" -Prun-expensive-tasks \
		--projects "$PROJECTS" \
		install
else
	# get the full list of minion projects to build
	PROJECTS="${PROJECTS},org.opennms.features.minion:container-parent,org.opennms.features.minion:core-parent,org.opennms.features.minion:org.opennms.features.minion.heartbeat,org.opennms.features.minion:repository,org.opennms.features.minion:shell"
	echo "=== RUNNING COMPILE ==="
	echo "Projects: ${PROJECTS}"
	./compile.pl $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_SETTINGS_XML $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY -Dbuild=all -Dinstall.version="%{version}-%{release}" -Ddist.name="%{buildroot}" \
		-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false \
		-Dopennms.home="%{instprefix}" -Prun-expensive-tasks \
		--projects "$PROJECTS" --also-make \
		install
fi


# Extract the container
mkdir -p %{buildroot}%{minioninstprefix}
tar zxvf %{_builddir}/%{_name}-%{version}-%{release}/features/minion/container/karaf/target/karaf-*.tar.gz -C %{buildroot}%{minioninstprefix} --strip-components=1
# Remove the data directory
rm -rf %{buildroot}%{minioninstprefix}/data
# Remove the demos directory
rm -rf %{buildroot}%{minioninstprefix}/demos

# Copy over the find-java.sh script
install -d -m 755 %{buildroot}%{minioninstprefix}/bin
install -c -m 755 %{_builddir}/%{_name}-%{version}-%{release}/opennms-base-assembly/src/main/filtered/bin/find-java.sh %{buildroot}%{minioninstprefix}/bin/find-java.sh

# Copy over the run script
mkdir -p %{buildroot}%{_initrddir}
sed -e 's,@INSTPREFIX@,%{minioninstprefix},g' -e 's,@SYSCONFDIR@,%{_sysconfdir}/sysconfig,g'  %{_builddir}/%{_name}-%{version}-%{release}/tools/packages/minion/minion.init > "%{buildroot}%{_initrddir}"/minion
chmod 755 "%{buildroot}%{_initrddir}"/minion

install -d -m 755 %{buildroot}%{_sysconfdir}/sysconfig
install -m 644 "%{_builddir}/%{_name}-%{version}-%{release}/tools/packages/minion/minion.sysconfig" "%{buildroot}%{_sysconfdir}/sysconfig/minion"

# Extract the core repository
mkdir -p %{buildroot}%{minionrepoprefix}/core
tar zxvf %{_builddir}/%{_name}-%{version}-%{release}/features/minion/core/repository/target/core-repository-*-repo.tar.gz -C %{buildroot}%{minionrepoprefix}/core
# Create a default org.opennms.minion.controller.cfg file
echo "location = MINION" > %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg
echo "id = 00000000-0000-0000-0000-000000ddba11" >> %{buildroot}%{minioninstprefix}/etc/org.opennms.minion.controller.cfg

# Extract the default repository
mkdir -p %{buildroot}%{minionrepoprefix}/default
tar zxvf %{_builddir}/%{_name}-%{version}-%{release}/features/minion/repository/target/repository-*-repo.tar.gz -C %{buildroot}%{minionrepoprefix}/default

# container package files
find %{buildroot}%{minioninstprefix} ! -type d | \
    grep -v %{minioninstprefix}/bin | \
    grep -v %{minionrepoprefix} | \
    grep -v %{minioninstprefix}/etc/featuresBoot.d | \
    grep -v %{minioninstprefix}/etc/org.opennms.minion.controller.cfg | \
    sed -e "s|^%{buildroot}|%attr(644,root,root) |" | \
    sort > %{_tmppath}/files.container
find %{buildroot}%{minioninstprefix}/bin ! -type d | \
    sed -e "s|^%{buildroot}|%attr(755,root,root) |" | \
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
%defattr(664 root root 775)
%attr(755,root,root) %{_initrddir}/minion
%attr(644,root,root) %config(noreplace) %{_sysconfdir}/sysconfig/minion
%attr(644,root,root) %{minioninstprefix}/etc/featuresBoot.d/.readme

%post container
# Clean out the data directory
rm -rf %{minioninstprefix}/data
# Generate an SSH key if necessary
if [ ! -f %{minioninstprefix}/etc/host.key ]; then
    /usr/bin/ssh-keygen -t rsa -N "" -b 4096 -f %{minioninstprefix}/etc/host.key
fi

%files features-core
%defattr(644 root root 755)
%{minionrepoprefix}/core
%config(noreplace) %{minioninstprefix}/etc/org.opennms.minion.controller.cfg

%post features-core
# Generate a new UUID to replace the default UUID if it is still present
UUID=$(/usr/bin/uuidgen -t)
sed -i "s|id = 00000000-0000-0000-0000-000000ddba11|id = $UUID|g" "%{minioninstprefix}/etc/org.opennms.minion.controller.cfg"
# Remove the directory used as the local Maven repo cache
rm -rf %{minionrepoprefix}/.local

%files features-default
%defattr(644 root root 755)
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
