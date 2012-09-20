%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-a
Summary: Feature A
Version: 0.9
Release: 1
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

%description
This is a test feature.

%install
install -d "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"
echo -e "%{name}-%{version}-%{release}\n\n" > "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/testfile.conf"
touch "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/postinstall-%{version}-%{release}"

rsync -avr "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"

%clean
rm -rf "$RPM_BUILD_ROOT"

%files
%config(noreplace) %{OPENNMS_HOME}/etc/*
%{OPENNMS_HOME}/share/etc-pristine/*