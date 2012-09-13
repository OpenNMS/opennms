%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-a
Summary: Feature A
Version: 1.0
Release: 3
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires(pretrans): o-test-feature-init >= 1.0-1, rpm

%description
This is a test feature.

%install
install -d $RPM_BUILD_ROOT/opt/opennms/etc
echo -e "%{name}-%{version}-%{release}\n\n" > $RPM_BUILD_ROOT/opt/opennms/etc/testfile.conf
#touch "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/postinstall-%{version}-%{release}.txt"

%clean
rm -rf $RPM_BUILD_ROOT

%pretrans
echo "%{OPENNMS_HOME}/bin/config-tools/opennms-preinstall.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-preinstall.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
ls -la --full-time "%{OPENNMS_HOME}/etc"/*
echo rpm -v --verify "%{name}" 1>&2
rpm -v --verify "%{name}" 1>&2
exit 0

%posttrans
if [ -e "%{OPENNMS_HOME}/etc/postinstall-%{version}-%{release}.txt" ]; then
	echo "%{version}-%{release} exists"
else
	echo "%{version}-%{release} does not exist"
fi
ls -la --full-time "%{OPENNMS_HOME}/etc"/*

echo "%{OPENNMS_HOME}/bin/config-tools/opennms-postinstall.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-postinstall.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2

%files
%config(noreplace) /opt/opennms/etc/*