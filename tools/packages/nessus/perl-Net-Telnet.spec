%define version	3.02
%define release 4onms
%define name 	perl-Net-Telnet
%define realname	Net-Telnet

Summary:        Net::Telnet (module for perl) Jay Rogers <jay@rgrs.com>
Name: 		%{name}
version: 	%{version}
Release: 	%{release}
License: 	GPL
Group: 		Development/Perl
Source: 	%{realname}-%{version}.tar.bz2
Patch0:		Net-Telnet-test3.patch
URL: 		http://www.bacus.pt/Net_SSLeay/index.html
BuildRoot: 	%{_tmppath}/%{name}-%{version}-%{release}-root/
Prefix:		%{_prefix}
Requires: 	perl

%description
    Net::Telnet allows you to make client connections to a TCP port
    and do network I/O, especially to a port using the TELNET
    protocol.  Simple I/O methods such as print, get, and getline are
    provided.  More sophisticated interactive features are provided
    because connecting to a TELNET port ultimately means communicating
    with a program designed for human interaction.  These interactive
    features include the ability to specify a timeout and to wait for
    patterns to appear in the input stream, such as the prompt from a
    shell.

%prep
rm -rf $RPM_BUILD_ROOT

%setup -q -n %{realname}-%{version}
%patch -p1

%build
rm -rf $RPM_BUILD_ROOT
perl Makefile.PL PREFIX=%{prefix}
make
make test

%install
eval `perl '-V:installarchlib'`
mkdir -p $RPM_BUILD_ROOT/$installarchlib
%makeinstall PREFIX=$RPM_BUILD_ROOT/%{prefix}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%{_libdir}/perl5/site_perl/*
%{_mandir}/*/*
%doc MANIFEST README ChangeLog

%changelog
* Wed Mar 27 2002 Mike Johnson <mikej@oculan.com> 3.02-4onms
- Add patch from CPAN for erroneous test failures
- RedHatify man page location

* Sun Jun 17 2001 Geoffrey Lee <snailtalk@mandrakesoft.com> 3.02-3mdk
- Rebuild for the latest perl.
- Rename spec.

* Thu Aug 31 2000 Philippe Libat <philippe@mandrakesoft.com> 3.02-2mdk
- description.

* Thu Aug 31 2000 Philippe Libat <philippe@mandrakesoft.com> 3.02-1mdk
- doc
- macroszifications.

* Fri Jun 30 2000 Nicolas Planel <nicolas@mandrakesoft.com>
- Spec file was generated for MandrakeSoft

