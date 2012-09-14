%define OPENNMS_HOME /opt/opennms

Name: o-test-foreign
Summary: OMG Foreign
Version: 1.0
Release: 1
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires: o-test-feature-a >= %{version}-%{release}

%description
This is not under our control!  It's craaaaaazy!

%install
install -d $RPM_BUILD_ROOT/opt/opennms/etc
echo -e "%{name}-%{version}-%{release}\n\n" > $RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/foreign.conf

%clean
rm -rf $RPM_BUILD_ROOT

%post
echo -e "\nPhase: post %{name}\n"
echo "Holy crap, monkeys are awesome." >> "%{OPENNMS_HOME}/etc/testfile.conf"

%files
%config(noreplace) /opt/opennms/etc/*