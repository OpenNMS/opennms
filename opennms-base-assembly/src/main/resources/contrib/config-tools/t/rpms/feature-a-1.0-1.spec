%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-a
Summary: Feature A
Version: 1.0
Release: 1
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires(pre): o-test-feature-init >= 1.0-1

%description
This is a test feature.

%install
install -d "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc"
echo "%{name}-%{version}-%{release}" > "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/testfile.conf"

%clean
rm -rf "$RPM_BUILD_ROOT"

%pre
"%{OPENNMS_HOME}/bin/config-tools/opennms-preinstall.pl" "%{name}" "%{version}-%{release}"

%post
"%{OPENNMS_HOME}/bin/config-tools/opennms-postinstall.pl" "%{name}" "%{version}-%{release}"

%files
%config(noreplace) %{OPENNMS_HOME}/etc/*.conf