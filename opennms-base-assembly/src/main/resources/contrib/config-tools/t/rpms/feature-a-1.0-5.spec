%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-a
Summary: Feature A
Version: 1.0
Release: 5
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires(pretrans): /bin/sh
Requires(pre): o-test-feature-init >= 1.0-1
Requires: o-test-feature-init >= 1.0-1

%description
This is a test feature.

%install
install -d $RPM_BUILD_ROOT/opt/opennms/etc "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"
echo -e "%{name}-%{version}-%{release}\n\n" > $RPM_BUILD_ROOT/opt/opennms/etc/testfile.conf
touch "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/postinstall-%{version}-%{release}"

rsync -avr "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"

%clean
rm -rf $RPM_BUILD_ROOT

%pretrans
echo -e "\nPhase: pretrans %{name}\n"
if ! [ -x "%{OPENNMS_HOME}/bin/config-tools/opennms-pretrans.pl" ]; then
	# on a first install, it doesn't matter if it runs, because everything is pristine
	exit 0;
fi
echo "pretrans %{name}: %{OPENNMS_HOME}/bin/config-tools/opennms-pretrans.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-pretrans.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2

%pre
echo -e "\nPhase: pre %{name}\n"
echo "pre %{name}: %{OPENNMS_HOME}/bin/config-tools/opennms-pre.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-pre.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2

%post
echo -e "\nPhase: post %{name}\n"
echo "post %{name}: %{OPENNMS_HOME}/bin/config-tools/opennms-post.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-post.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2

%posttrans
echo -e "\nPhase: posttrans %{name}\n"
echo "posttrans %{name}: %{OPENNMS_HOME}/bin/config-tools/opennms-posttrans.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2
"%{OPENNMS_HOME}/bin/config-tools/opennms-posttrans.pl" "%{OPENNMS_HOME}" "%{name}" "%{version}-%{release}" 1>&2

%files
%config(noreplace) /opt/opennms/etc/*
%{OPENNMS_HOME}/share/etc-pristine/*