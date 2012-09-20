%define OPENNMS_HOME /opt/opennms

Name: o-test-feature-b
Summary: Feature B
Version: 1.0
Release: 3
License: GPL
Group: Applications/System
BuildArch: noarch
BuildRoot: /var/tmp/%{name}-buildroot

Requires(pretrans): /bin/sh, /bin/mv
Requires(pre): o-test-feature-init >= 1.0-1
Requires: o-test-feature-init >= 1.0-1
Requires(pre): o-test-feature-a >= %{version}-%{release}
Requires: o-test-feature-a >= %{version}-%{release}

%description
This is a test feature.

%install
install -d "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"
echo -e "%{name}-%{version}-%{release}\n\n" > $RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/b.conf

rsync -avr "$RPM_BUILD_ROOT%{OPENNMS_HOME}/etc/" "$RPM_BUILD_ROOT%{OPENNMS_HOME}/share/etc-pristine/"

%clean
rm -rf $RPM_BUILD_ROOT

%pretrans
echo -e "\nPhase: pretrans %{name}\n"
if ! [ -x "%{OPENNMS_HOME}/bin/config-tools/opennms-pretrans.pl" ]; then
	# on a first install, if we're upgrading save etc/ for git-setup.pl
	# this avoids anything in etc/ as being marked as .rpmnew
	if [ -d "%{OPENNMS_HOME}/etc" ]; then
		[ -x /bin/mv ] && /bin/mv "%{OPENNMS_HOME}/etc" "%{OPENNMS_HOME}/.etc-pretrans"
		if [ -x /bin/cp ] && [ -x /bin/mkdir ]; then
			mkdir -p "%{OPENNMS_HOME}/etc"
			cp -pR "%{OPENNMS_HOME}/share/etc-pristine"/* "%{OPENNMS_HOME}/etc/"
		fi
	fi
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
%config(noreplace) %{OPENNMS_HOME}/etc/*
%{OPENNMS_HOME}/share/etc-pristine/*