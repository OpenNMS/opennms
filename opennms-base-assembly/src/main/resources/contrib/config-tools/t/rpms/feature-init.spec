%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-init
Summary: Feature Initialization
Version: 1.0
Release: 1
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires(post): git >= 1.7

Source: git-setup.pl
Source1: opennms-preinstall.pl
Source2: opennms-postinstall.pl

%description
Tools for manipulating Git configuration directories.

Creates a Git environment in $OPENNMS_HOME/etc on first installation.

%install
echo "SOURCE0 = %{SOURCE0}"
install -d "$RPM_BUILD_ROOT%{OPENNMS_HOME}/bin/config-tools"
install -c -m 755 "%{SOURCE0}" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/bin/config-tools/"
install -c -m 755 "%{SOURCE1}" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/bin/config-tools/"
install -c -m 755 "%{SOURCE2}" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/bin/config-tools/"

%clean
rm -rf "$RPM_BUILD_ROOT"

%post
"%{OPENNMS_HOME}/bin/config-tools/git-setup.pl" "%{OPENNMS_HOME}"

%files
%attr(755,root,root) %{OPENNMS_HOME}/bin/config-tools/*.pl