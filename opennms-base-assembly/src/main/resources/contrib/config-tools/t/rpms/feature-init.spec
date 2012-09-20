%define OPENNMS_HOME /opt/opennms
%define TOOLDIR %{OPENNMS_HOME}/bin/config-tools

Name: o-test-feature-init
Summary: Feature Initialization
Version: 1.0
Release: 1
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires: git >= 1.7, rpm
Requires(post): git >= 1.7, rpm

Source: perlfiles.tar.gz

%description
Tools for manipulating Git configuration directories.

Creates a Git environment in $OPENNMS_HOME/etc on first installation.

%prep
%setup -c

%build
perl Makefile.PL PREFIX="%{_prefix}" INSTALLDIRS="perl"
make

%install
make install PREFIX="$RPM_BUILD_ROOT%{_prefix}" INSTALLDIRS="perl"
install -d -m 755 "$RPM_BUILD_ROOT%{TOOLDIR}"
mv "$RPM_BUILD_ROOT%{_bindir}"/*.pl "$RPM_BUILD_ROOT%{TOOLDIR}/"
cd "$RPM_BUILD_ROOT%{TOOLDIR}"
ln -s opennms-pretrans.pl opennms-pre.pl

%clean
rm -rf "$RPM_BUILD_ROOT"

%pre
rpm -q --queryformat='\%{version}-\%{release}' $package 2>/dev/null | grep -v 'is not installed' | sort -u | head -n 1 > /tmp/git-setup.$package
exit 0

%post
#"%{TOOLDIR}/git-setup.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{TOOLDIR}/git-setup.pl" "%{OPENNMS_HOME}" "o-test-feature-a" "%{version}-%{release}" 1>&2

%files
%{_prefix}
%attr(755,root,root) %{TOOLDIR}/*.pl