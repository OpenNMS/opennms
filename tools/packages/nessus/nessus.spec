%define mainname	nessus
%define name		%{mainname}
%define version		1.1.14
%define release		0.onms.4
%define prefix		%{_prefix}

Name:		%{name}
Summary:	Nessus security scanner
Version:	%{version}
Release:	%{release}
License:	GPL
Group:		System/Servers
URL:		http://www.nessus.org
Source1:	nessus-libraries-%{version}.tar.gz
Source2:	libnasl-%{version}.tar.gz
Source3:	nessus-core-%{version}.tar.gz
Source4:	nessus-plugins-%{version}.tar.gz
Source5:	nessusd.init.bz2
Source6:	nessusd.users.bz2
Source7:	nessusd.rules.bz2
Source8:	nessusd.conf.bz2
Source9:	plugins_api.txt.bz2
Source10:       nessus-16.png.bz2
Source11:       nessus-32.png.bz2
Source12:       nessus-48.png.bz2
Source13:	nessus-users.tar.gz
Patch0:		nessus-1.1.7-mkcert.patch.bz2
Patch1:		nessus-core-unlimited.patch.bz2
Prefix:         %{prefix}
Requires:	nmap lynx tar gzip %{name}-libs
BuildRequires:	perl
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-buildroot

%description
Nessus is a free, up-to-date, and full featured remote security scanner for
Linux. It is multithreaded, plugin-based, has a nice GTK interface, and
currently performs 410 remote security checks. It has powerful reporting
capabilities (HTML, LaTeX, ASCII text) and not only points out problems,
but suggests a solution for each of them.

%package libs
Group:		System/Servers
Summary:	Libraries needed by nessus
%description libs
Libraries needed by nessus


%package devel
Group:		System/Libraries
Summary:	Development libraries and headers for Nessus.
%description devel
Development libraries and headers for Nessus.


%package client
Group:		Monitoring
Summary:	Nessus GTK+ client.
Requires:	%{name}-libs
%description client
Nessus Gtk+ client.


%package plugins
Group:		System/Servers
Summary:	Nessus plugins.
Requires:	%{name} %{name}-libs
%description plugins
Nessus plugins.


%prep
rm -rf $RPM_BUILD_ROOT

%setup -q -c -T
%setup -q -T -D -a 1
%setup -q -T -D -a 2
%setup -q -T -D -a 3
%setup -q -T -D -a 4

%patch0 -p1 -b .mkcert
%patch1 -p0 -b .unlconn

perl -pi -e 's/^installuser=.*/installuser='`whoami`'/' nessus-core/nessus.tmpl.in
bzip2 -cd %{SOURCE9} > ./`basename %{SOURCE9} .bz2`

%build
if [ -d %buildroot ]; then rm -rf %buildroot; fi
mkdir -p %buildroot{%{_bindir},%{_libdir}/%{name}/reports,%{_initrddir},%{_var}/{log,lib}/nessus}

# Build and install nessus libraries
cd nessus-libraries
 CFLAGS="%optflags" ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no \
 %configure --disable-cipher
 perl -pi -e 's/-o root / /g; s/-o \$\(installuser\) / /g; y/{}/()/' Makefile
 make
 %makeinstall
cd ..
perl -pi -e 's|^PREFIX=.*|PREFIX='%buildroot%{_prefix}'|;
             s|-I/usr/include/peks|-I\$\INCLUDEDIR/peks|;' %buildroot%{_bindir}/nessus-config

# Build and install nasl
cd libnasl;
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot%{_libdir}:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no %configure
 perl -pi -e 's/-o root / /g; s/-o \$\(installuser\) / /g; y/{}/()/' Makefile
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot%{_libdir}:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no make NESSUSCONFIG=%buildroot%{_bindir}/nessus-config
 %makeinstall
cd ..
perl -pi -e 's|^PREFIX=.*|PREFIX='%buildroot%{_prefix}'|' %buildroot%{_bindir}/nasl-config

# Build and install nessus-core
cd nessus-core
 perl -pi -e 's|^NESSUSD_CONFDIR=.*|NESSUSD_CONFDIR=%{_sysconfdir}/|;
              s|^NESSUSD_STATEDIR=.*|NESSUSD_STATEDIR=%{_var}/lib/nessus|;
              s|^NESSUSD_DATADIR=.*|NESSUSD_DATADIR=%{_sysconfdir}/nessus|;' ./configure
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot%{prefix}/lib:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no \
 %configure \
   --datadir=%{_sysconfdir} --sharedstatedir=%{_var}/lib \
   --enable-release --disable-cipher \
   --localstatedir=%{_var}/lib \
   --includedir=%buildroot%{_prefix}/include; \
 perl -pi -e 's/-o root / /g; s/-o \$\(installuser\) / /g; y/{}/()/' Makefile
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot%{prefix}/lib:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no make \
 NESSUSCONFIG=%buildroot%{_bindir}/nessus-config \
 NASLCONFIG=%buildroot%{_bindir}/nasl-config
 %makeinstall localstatedir=%buildroot%{_var}/lib \
   datadir=%buildroot%{_sysconfdir} \
   NESSUSD_CONFDIR=%buildroot%{_sysconfdir}/ \
   NESSUSD_STATEDIR=%buildroot%{_var}/lib/nessus \
   NESSUSD_DATADIR=%buildroot%{_sysconfdir}/nessus
cd ..

# Build and install nessus-plugins
cd nessus-plugins
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot/%{_libdir}:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no \
 %configure
 perl -pi -e 's/-o root / /g; s/-o \$\(installuser\) / /g; y/{}/()/' Makefile
 PATH="%buildroot%{_bindir}:$PATH" \
 CFLAGS="%optflags" \
 LD_LIBRARY_PATH="%buildroot/%{_libdir}:$LD_LIBRARY_PATH" \
 ac_cv_prog_cc_g=no ac_cv_prog_cxx_g=no \
 make NESSUSCONFIG=%buildroot%{_bindir}/nessus-config \
 NASLCONFIG=%buildroot%{_bindir}/nasl-config
 %makeinstall
cd ..

%install
mkdir -p %buildroot{%{_bindir},%{_libdir}/%{name}/reports,%{_sysconfdir}/nessus,%{_initrddir},%{_var}/log/nessus}
# Main Install already done in build
# for dir in nessus-libraries libnasl nessus-core nessus-plugins; do
# (cd $dir; make install prefix=%buildroot/%{_prefix} \
#  sysconfdir=%buildroot%{_sysconfdir}/nessus localstatedir=%buildroot%{_var} \
#  datadir=%buildroot%{_sysconfdir})
# done
bzip2 -cd %{SOURCE5} > %buildroot%{_initrddir}/nessusd
for file in %{SOURCE6} %{SOURCE7} %{SOURCE8} ; do
    bzip2 -cd $file > %buildroot%{_sysconfdir}/nessus/$(basename $file .bz2)
done

# clean-up man files
for i in %{_mandir}/man8/nessusd.8; do
        perl -pi -e 's|'%buildroot%{_sysconfdir}/nessusd.conf'|%{_sysconfdir}/nessus/nessusd.conf|g' %buildroot/$i
done
 
# Correct paths in devel stuff
perl -pi -e 's|^PREFIX=.*|PREFIX='%{_prefix}'|' \
 %buildroot%{_bindir}/*-config \

# manually install docs
mkdir -p %buildroot%{_docdir}/%{name}-%{version}
cp -av $RPM_BUILD_DIR/%{name}-%{version}/%{name}-core/{CHANGES,INSTALL,README_LINUX,TODO,UPGRADE_README} %buildroot%{_docdir}/%{name}-%{version}

mkdir -p $RPM_BUILD_ROOT%{_var}/lib/nessus
tar -xvzf %{SOURCE13} -C $RPM_BUILD_ROOT%{_var}/lib/nessus

%clean
if [ -d %buildroot ]; then rm -rf %buildroot; fi

%post
#set -x
perl -pi -e 's|\@RPM_INSTALL_PREFIX\@|'${RPM_INSTALL_PREFIX}'|' \
 %{_initrddir}/nessusd %{_sysconfdir}/nessus/nessusd.conf

#set +x
# Done in libs package now - main package doesn't contain libs anymore:
#/sbin/ldconfig

%preun

%post libs -p /sbin/ldconfig

%postun libs -p /sbin/ldconfig

%files
%defattr(0644,root,root,0755)
%doc %{_docdir}/%{name}-%{version}/*
%attr(0755,root,root) %{_sbindir}/nessusd
%attr(0755,root,root) %{_sbindir}/nessus-adduser
%attr(0755,root,root) %{_sbindir}/nessus-rmuser
%attr(0755,root,root) %{_sbindir}/nessus-update-plugins
# Included in libs package
#%attr(0755,root,root) %{_libdir}/*.so*
%dir %{_sysconfdir}/nessus
%attr(0644,root,root) %config(noreplace) %{_sysconfdir}/nessus/accounts.txt
%attr(0644,root,root) %config(noreplace) %{_sysconfdir}/nessus/nessusd.conf
%attr(0644,root,root) %config(noreplace) %{_sysconfdir}/nessus/nessusd.rules
%attr(0644,root,root) %config(noreplace) %{_sysconfdir}/nessus/nessusd.users
%attr(0644,root,root) %config(noreplace) %{_sysconfdir}/nessus/queso.conf
%dir %{_var}/log/nessus
%dir %{_var}/lib/nessus
#%dir %{_var}/nessus
#%{_var}/nessus/users
%{_var}/lib/nessus/users
%attr(0755,root,root) %config(noreplace) %{_initrddir}/nessusd
%{_mandir}/man8/*

%files libs
%defattr(0644,root,root,755)
# Main package:
%attr(0755,root,root) %{_libdir}/*.so*
# Client package:
# Included through the *.so*
#%attr(0755,root,root) %{_libdir}/libnessus.so*

%files devel
%defattr(0644,root,root,755)
%doc plugins_api.txt
%{_prefix}/include/*
%{_libdir}/*.*a
%attr(0755,root,root) %{_bindir}/nessus-config
%attr(0755,root,root) %{_bindir}/nasl-config

%files client
%defattr(0644,root,root,755)
%attr(0755,root,root) %{_bindir}/nessus
# Included in libs package:
#%attr(0755,root,root) %{_libdir}/libnessus.so*
%doc %{_mandir}/man1/*
%dir %{_libdir}/nessus/reports

%files plugins
%defattr(0644,root,root,0755)
%{_libdir}/nessus/plugins/*

%changelog
* Wed Mar 27 2002 Mike Johnson <mikej@opennms.org> 1.1.14-0.onms.4
- Fix configure directives so nessusd and nessusd-adduser agree
- Alter init script so that nessusd only listens to localhost

* Fri Mar 15 2002 Benjamin Reed <ben@opennms.org> 1.1.14-0.onms.1
- rebuild

* Fri Mar 15 2002 Benjamin Reed <ben@opennms.org> 1.1.13-0.onms.1
- modified to allow unlimited connections

* Thu Feb 28 2002 Lenny Cartier <lenny@mandrakesoft.com> 1.1.13-1mdk
- 1.1.13
- xpm2png

* Tue Jan 22 2002 Laurent Culioli <laurent@mandrakesoft.com> 1.1.11-1mdk
- 1.1.11

* Sat Jan 19 2002 Lenny Cartier <lenny@mandrakesoft.com> 1.1.9-3mdk
- rebuild

* Thu Nov 22 2001 Alexander Skwar <ASkwar@Linux-Mandrake.com> 1.1.9-2mdk
- Make rpmlint a little happier

* Wed Nov 21 2001 Alexander Skwar <ASkwar@Linux-Mandrake.com> 1.1.9-1mdk
- 1.1.9
- Actually really set localstatedir to /var/lib instead of to /var/log

* Thu Nov  8 2001 Vincent Danen <vdanen@mandrakesoft.com> 1.1.8-1mdk
- 1.1.8
- call nessus-mkcert at install if certs do not exist
- patch nessus-mkcert to use more sensible locations to store certs/keys (P1)
- make localstatedir /var/lib and not /var/log (???) so that user accounts
  and info go into /var/lib/users and not /var/log/users

* Wed Nov  7 2001 Frederic Lepied <flepied@mandrakesoft.com> 1.1.6-2mdk
- don't use sub shell in %build
- use %%make
- use service macros
- added the missing nessus-mkcert, nessus-rmuser and nessus-update-plugins
- add a dependency on tar, gzip and lynx for nessus-update-plugins

* Wed Oct 17 2001 Lenny Cartier <lenny@mandrakesoft.com> 1.1.6-1mdk
- 1.1.6

* Mon Sep 17 2001 Lenny Cartier <lenny@mandrakesoft.com> 1.1.4-1mdk
- added by Oden Eriksson <oden.eriksson@kvikkjokk.net> :
	- updated to 1.1.4

* Tue Aug 21 2001 Lenny Cartier <lenny@mandrakesoft.com> 1.0.9-1mdk
- updated to 1.0.9

* Wed Jun  6 2001 Laurent Culioli <laurent@mandrakesoft.com> 1.0.8-1mdk
- updated to 1.0.8

* Wed Mar 07 2001  Lenny Cartier <lenny@mandrakesoft.com> 1.0.7a-1mdk
- upgraded to 1.0.7a

* Tue Jan 30 2001 Lenny Cartier <lenny@mandrakesoft.com> 1.0.7-1mdk
- used srpm from Guillaume Rousse <g.rousse@mandrake-linux.com> :
	- updated to 1.0.7

* Mon Nov 06 2000 Lenny Cartier <lenny@mandrakesoft.com> 1.0.5-1mdk
- used srpm from Alexander Skwar <ASkwar@Linux-Mandrake.com> :
	New version
	Added menu for the client with icons
	Quiet the unpacking of the files

* Wed Jul 26 2000 John Johnson <jjohnson@linux-mandrake.com> 1.0.3-1mdk
- Fixed an error in my spec file that caused the nessusd script in
  /etc/rc.d/init.d to not work properly.

* Wed Jul 12 2000 John Johnsin <jjohnson@linux-mandrake.com> 1.0.3-1mdk
- Updated rpm for version 1.0.3 
- made a few small changes to spec file

* Sat Jun 10 2000 John Johnson <jjohnson@linux-mandrake.com> 1.0.1-1mdk
- updated sources to the new version.

* Mon May 29 2000 Vincent Danen <vdanen@linux-mandrake.com> 1.0.0-2mdk
- bzip sources
- fix group
- various specfile cleanups
- made unrelocatable
- added call to ldconfig in post and postun

* Thu May 18 2000 John Johnson <jjohnson@linux-mandrake.com>
- Made Mandrake rpm
