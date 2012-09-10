Name: o-test-feature-a
Summary: Feature A
Version: 1.0
Release: 2
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

%description
This is a test feature.

%install
install -d $RPM_BUILD_ROOT/opt/opennms/etc
echo "%{name}-%{version}-%{release}" > $RPM_BUILD_ROOT/opt/opennms/etc/testfile.conf

%clean
rm -rf $RPM_BUILD_ROOT

%files
%config(noreplace) /opt/opennms/etc/*.conf