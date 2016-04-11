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
%{!?packagedir:%define packagedir %{_name}-minion-%version-%{releasenumber}}

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

%build

rm -rf $RPM_BUILD_ROOT

%install

# Extract the container
mkdir -p $RPM_BUILD_ROOT%{minioninstprefix}
tar zxvf $RPM_BUILD_DIR/%{_name}-%{version}-%{release}/features/minion/container/karaf/target/karaf-*.tar.gz -C $RPM_BUILD_ROOT%{minioninstprefix} --strip-components=1
# Remove the data directory
rm -rf $RPM_BUILD_ROOT%{minioninstprefix}/data

# Copy over the run script
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
sed -e 's,@INSTPREFIX@,%{minioninstprefix},g' $RPM_BUILD_DIR/%{_name}-%{version}-%{release}/tools/packages/minion/minion.init > "$RPM_BUILD_ROOT%{_initrddir}"/minion
chmod 755 "$RPM_BUILD_ROOT%{_initrddir}"/minion

# Extract the core repository
mkdir -p $RPM_BUILD_ROOT%{minionrepoprefix}/core
tar zxvf $RPM_BUILD_DIR/%{_name}-%{version}-%{release}/features/minion/core/repository/target/core-repository-*-repo.tar.gz -C $RPM_BUILD_ROOT%{minionrepoprefix}/core
echo "location = MINION" > $RPM_BUILD_ROOT%{minioninstprefix}/etc/org.opennms.minion.controller.cfg
echo "id = 00000000-0000-0000-0000-000000ddba11" >> $RPM_BUILD_ROOT%{minioninstprefix}/etc/org.opennms.minion.controller.cfg

# Extract the default repository
mkdir -p $RPM_BUILD_ROOT%{minionrepoprefix}/default
tar zxvf $RPM_BUILD_DIR/%{_name}-%{version}-%{release}/features/minion/repository/target/repository-*-repo.tar.gz -C $RPM_BUILD_ROOT%{minionrepoprefix}/default

# container package files
find $RPM_BUILD_ROOT%{minioninstprefix} ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(644,root,root) |" | \
    grep -v %{minioninstprefix}/bin | \
    grep -v %{minioninstprefix}/repositories | \
    grep -v %{minioninstprefix}/etc/featuresBoot.d | \
    grep -v %{minioninstprefix}/etc/org.opennms.minion.controller.cfg | \
    sort > %{_tmppath}/files.container
find $RPM_BUILD_ROOT%{minioninstprefix}/bin ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(755,root,root) |" | \
    sort >> %{_tmppath}/files.container
find $RPM_BUILD_ROOT%{minioninstprefix} -type d | \
    sed -e "s,^$RPM_BUILD_ROOT,%dir ," | \
    sort >> %{_tmppath}/files.container

# features-core package files
find $RPM_BUILD_ROOT%{minionrepoprefix}/core ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(644,root,root) |" | \
    sort > %{_tmppath}/files.core

# features-default package files
find $RPM_BUILD_ROOT%{minionrepoprefix}/default ! -type d | \
    sed -e "s|^$RPM_BUILD_ROOT|%attr(644,root,root) |" | \
    sort > %{_tmppath}/files.default

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(664 root root 775)

%files container -f %{_tmppath}/files.container
%defattr(664 root root 775)
%attr(755,root,root) %{_initrddir}/minion
%attr(644,root,root) %{minioninstprefix}/etc/featuresBoot.d/.readme

%post container
# Generate an SSH key
if [ ! -f %{minioninstprefix}/etc/host.key ]; then
    /usr/bin/ssh-keygen -t rsa -N "" -b 4096 -f %{minioninstprefix}/etc/host.key
fi

%files features-core -f %{_tmppath}/files.core
%defattr(664 root root 775)
%config(noreplace) %{minioninstprefix}/etc/org.opennms.minion.controller.cfg

%post features-core
# Generate a new UUID
UUID=$(/usr/bin/uuidgen -t)
sed -i "s|id =.*|id = $UUID|g" "%{minioninstprefix}/etc/org.opennms.minion.controller.cfg"

%files features-default -f %{_tmppath}/files.default
%defattr(664 root root 775)

