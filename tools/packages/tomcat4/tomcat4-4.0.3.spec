%{!?http:%define http 8080}
%{!?httpproxy:%define httpproxy 8081}
%{!?httptest:%define httptest 8082}
%{!?httpssl:%define httpssl 8443}
%{!?ajp:%define ajp 8009}
%define	serialnum	2
%define	releasenum	0.onms.9

%define	manualtc 	tomcat-4.0.3
%define	manualapi 	servletapi-2.3
%define	homedir		/var/tomcat4
%define	javalibdir	/usr/share/java
%define	jname		jasper4
%define	tcuid		91

%define packname 	jakarta-tomcat-4.0.3
%define spackname 	jakarta-servletapi-4
%define packdname 	jakarta-tomcat-4.0.3-src
%define spackdname 	jakarta-servletapi-4-src

%define sconfdir	/etc/%{name}/conf
%define bindir		%{homedir}/bin
%define commondir	%{homedir}/common
%define confdir	 	%{homedir}/conf
%define libdir		%{homedir}/lib
%define logdir		%{homedir}/logs
%define servdir		%{homedir}/server
%define workdir	 	%{homedir}/work
%define appdir		%{homedir}/webapps
%define	manualdir	%{appdir}/tomcat-docs

Summary: 		Apache's servlet engine
Name: 			tomcat4
Version: 		4.0.3
Serial:			%{serialnum}
Release: 		%releasenum
License:		Apache Software License
Vendor:		 	Apache Software Foundation
Group: 			Networking/Daemons
Url: 			http://jakarta.apache.org/tomcat/
BuildArch: 		noarch
Source: 		http://jakarta.apache.org/builds/tomcat/release/v%{version}/src/%{packname}-src.tar.gz
Source1: 		http://jakarta.apache.org/builds/tomcat/release/v%{version}/src/%{spackname}-src.tar.gz
Source2:		http://jakarta.apache.org/builds/tomcat/release/v%{version}/bin/%{packname}-LE-jdk14.tar.gz
Source3: 		TOMCAT4.README.RPM
Source4: 		tomcat4.init
Source5:		tomcat4.conf
Source6:		tomcat4.logrotate
Source7:		tomcat4.wrapper
Source8:		jasper4.wrapper
Source9:		junit.jar
Source10:		ant141.jar
Source11:		ant141-optional.jar
Source12:		jta.jar
Source13:		tyrex.jar
Source14:		mx4j.jar
Source15:		mail.jar
Source16:		activation.jar
Source17:		xerces201.jar
Patch:			tomcat4-%{version}.patch
Patch1:			tomcat4-%{version}-security.patch
Packager: 		Henri Gomez <hgomez@slib.fr>, Benjamin Reed <ben@opennms.org>
BuildRoot: 		/var/tmp/%{name}-root
BuildRequires:		regexp >= 1.2, ant >= 1.4, tyrex >= 0.9.7.0
Requires:		j2sdk >= 1.4

%description
Develop Web applications in Java.

%package manual
Group: 		Documentation
Requires: 	tomcat4
Summary: 	Online manual for tomcat
Obsoletes:	tomcat4-doc

%description manual
Documentation for tomcat

%package webapps
Group:	System Environment/Applications
Requires:	tomcat4
Summary:	Web Applications for tomcat

%description webapps
Web Applications for tomcat

%prep
rm -rf $RPM_BUILD_DIR/%{name}

%setup -c -T -a 0
%setup -T -D -a 1
%setup -T -D -a 2
%setup -T -D -a 10

cd %{packdname}
%patch -p1
%patch1 -p1

%build
export TCRPMDIR=$RPM_BUILD_DIR/%{name}-%{version}
export TCBINDIR=$TCRPMDIR/%{packname}/common/lib/
[ -z "$JAVA_HOME" ] && export JAVA_HOME=/usr/java/j2sdk1.4.0
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=%{SOURCE9}:%{SOURCE10}:%{SOURCE11}:%{SOURCE12}:%{SOURCE13}:%{SOURCE14}:%{SOURCE15}:%{SOURCE16}

# build servlet-api 2.3
cd %{spackdname}
ant dist

# build tomcat 4
cd ../%{packdname}

cp build.properties.sample build.properties

ant $ANT_OPTS -Dant.home=%{javalibdir} \
	-Dservlet.home=$TCRPMDIR/%{spackdname}/dist/ \
	-Djava.home=$JAVA_HOME \
	-Dactivation.jar=%{SOURCE16} -Dcopy.activation.jar=%{SOURCE16} \
	-Dmail.jar=%{SOURCE15}       -Dcopy.mail.jar=%{SOURCE15} \
	-Djmxri.jar=%{SOURCE14}      -Dcopy.jmxri.jar=%{SOURCE14} \
	-Dtyrex.jar=%{SOURCE13}      -Dcopy.tyrex.jar=%{SOURCE13} \
	-Djunit.jar=%{SOURCE9}       -Dcopy.junit.jar=%{SOURCE9} \
	-Djta.jar=%{SOURCE12}        -Dcopy.jta.jar=%{SOURCE12}

%install
[ "$RPM_BUILD_ROOT" != "/" ] && rm -rf $RPM_BUILD_ROOT

export TCRPMDIR=$RPM_BUILD_DIR/%{name}-%{version}
export TCBINDIR=$TCRPMDIR/%{packname}/common/lib/
export PATH=$JAVA_HOME/bin:$PATH

mkdir -p $RPM_BUILD_ROOT%{homedir}
mkdir -p $RPM_BUILD_ROOT/etc/rc.d/init.d
mkdir -p $RPM_BUILD_ROOT/%{_bindir}
mkdir -p $RPM_BUILD_ROOT/%{sconfdir}
mkdir -p $RPM_BUILD_ROOT/etc/logrotate.d
mkdir -p $RPM_BUILD_ROOT/var/log/tomcat4

# sysv init and logging
install %{SOURCE3} .
install %{SOURCE4} $RPM_BUILD_ROOT/etc/rc.d/init.d/%{name}
install %{SOURCE5} $RPM_BUILD_ROOT/%{sconfdir}/%{name}.conf
install %{SOURCE6} $RPM_BUILD_ROOT/etc/logrotate.d/%{name}

# -Dcopy.crimson.jar=../lib/crimson.jar
# -Dcopy.jaxp.jar=../lib/jaxp.jar

cd %{packdname}
ant $ANT_OPTS -Dant.home=%{javalibdir} \
	-Dservlet.home=$TCRPMDIR/%{spackdname}/dist/ \
	-Djava.home=$JAVA_HOME \
	-Dactivation.jar=%{SOURCE16} -Dcopy.activation.jar=%{SOURCE16} \
	-Dmail.jar=%{SOURCE15}       -Dcopy.mail.jar=%{SOURCE15} \
	-Djmxri.jar=%{SOURCE14}      -Dcopy.jmxri.jar=%{SOURCE14} \
	-Dtyrex.jar=%{SOURCE13}      -Dcopy.tyrex.jar=%{SOURCE13} \
	-Djunit.jar=%{SOURCE9}       -Dcopy.junit.jar=%{SOURCE9} \
	-Djta.jar=%{SOURCE12}        -Dcopy.jta.jar=%{SOURCE12} dist

mv dist/* $RPM_BUILD_ROOT%{homedir}

install $RPM_BUILD_ROOT%{homedir}/bin/jspc.sh		$RPM_BUILD_ROOT%{_bindir}/jspc4

# rename catalina/jasper into dtomcat4/djasper4 to let wrapper take precedence
install $RPM_BUILD_ROOT%{homedir}/bin/jasper.sh		$RPM_BUILD_ROOT%{_bindir}/d%{jname}
install $RPM_BUILD_ROOT%{homedir}/bin/catalina.sh	$RPM_BUILD_ROOT%{_bindir}/d%{name}
install $RPM_BUILD_ROOT%{homedir}/bin/setclasspath.sh	$RPM_BUILD_ROOT%{_bindir}/setclasspath.sh

# install wrapper as tomcat4/jasper4
install %{SOURCE7}					$RPM_BUILD_ROOT%{_bindir}/%{name}
install %{SOURCE8}					$RPM_BUILD_ROOT%{_bindir}/%{jname}

# remove / reorder non-usefull stuff
rm -rf $RPM_BUILD_ROOT%{homedir}/src/ $RPM_BUILD_ROOT%{bindir}/*.sh $RPM_BUILD_ROOT%{bindir}/*.bat
 
# copy docs to manual dir
mkdir -p $RPM_BUILD_ROOT%{manualdir}
cp -rf $RPM_BUILD_ROOT%{homedir}/webapps/ROOT/* $RPM_BUILD_ROOT%{manualdir}

# FHS compliance patches, not easy to track them all boys :)

for i in $RPM_BUILD_ROOT/%{sconfdir}/%{name}.conf $RPM_BUILD_ROOT%{_bindir}/d%{name} \
	 $RPM_BUILD_ROOT%{_bindir}/%{name} $RPM_BUILD_ROOT/etc/rc.d/init.d/%{name}; do

	perl -pi -e "s|\@\@\@TCCONF\@\@\@|%{sconfdir}|g;" $i
	perl -pi -e "s|\@\@\@TCHOME\@\@\@|%{homedir}|g;" $i

done

%{!?version:%define version @opennms.version@}

perl -pi -e 's|8080|%{http}|g'		$RPM_BUILD_ROOT%{confdir}/server.xml
perl -pi -e 's|8081|%{httpproxy}|g'	$RPM_BUILD_ROOT%{confdir}/server.xml
perl -pi -e 's|8082|%{httptest}|g'	$RPM_BUILD_ROOT%{confdir}/server.xml
perl -pi -e 's|8443|%{httpssl}|g'	$RPM_BUILD_ROOT%{confdir}/server.xml

# added ajp13 listen mode from 8009 to 8109
perl -pi -e 's|8009|%{ajp}|g'		$RPM_BUILD_ROOT%{confdir}/server.xml

rm -rf $RPM_BUILD_ROOT%{homedir}/WEB-INF
cp %{SOURCE17} $RPM_BUILD_ROOT%{commondir}/lib
rm -rf $RPM_BUILD_ROOT%{logdir}
ln -sf /var/log/tomcat4 $RPM_BUILD_ROOT%{logdir}

%clean
[ "$RPM_BUILD_ROOT" != "/" ] && rm -rf $RPM_BUILD_ROOT

%pre
# Add the "tomcat" user
# we need a shell to be able to use su - later
/usr/sbin/useradd -c "Tomcat4" -u %{tcuid} \
	-s /bin/bash -r -d /var/tomcat tomcat4 2> /dev/null || :

# move the old logdir out of the way, it's now a symlink
if [ -d %{logdir} ] && [ ! -L %{logdir} ]; then
	mv %{logdir} %{logdir}.old
	if [ $? -ne 0 ]; then
		echo "error: unable to move old %{logdir}"
		exit 1
	fi
fi

%post
[ -x /sbin/chkconfig ] && /sbin/chkconfig tomcat4 off
[ -x /sbin/chkconfig ] && /sbin/chkconfig tomcat4 on

%preun
if [ $1 = 0 ]; then

	if [ -f /var/lock/subsys/%{name} ]; then
	/etc/rc.d/init.d/%{name} stop
	fi
	if [ -f /etc/rc.d/init.d/%{name} ]; then
	/sbin/chkconfig --del %{name}
	fi

	/usr/sbin/userdel tomcat4
fi

%files
%defattr(644 root root 755)
%attr(755,root,root)	%dir			%{appdir}
%attr(755,root,root)	%dir			%{bindir}
%attr(755,root,root)	%dir			%{commondir}
%attr(755,root,root)	%dir			%{confdir}
%attr(755,root,root)	%dir			%{sconfdir}
%attr(755,root,root)	%dir			%{libdir}
%attr(755,root,root)	%dir			%{logdir}
%attr(755,root,root)	%dir			%{servdir}
%attr(755,root,root)	%dir			%{workdir}
%attr(644,root,root)	%config			%{sconfdir}/%{name}.conf
%attr(755,root,root)				%{_bindir}
%attr(644,root,root)				%{bindir}/*
%attr( - ,root,root)				%{commondir}/*
%attr(644,root,root)	%config(noreplace)	%{confdir}/*
%attr( - ,root,root)				%{libdir}/*
%attr( - ,root,root)				%{servdir}/*
%attr(755,root,root)	%config			/etc/rc.d/init.d/%{name}
%attr( - ,root,root)	%doc 			TOMCAT4.README.RPM %{packdname}/{LICENSE,README*,RELE*}
%attr(755,root,root)	%dir			/var/log/tomcat4

%files manual
%defattr(644 root root 755)
%attr( - ,root,root)				%{manualdir}

%files webapps
%defattr(644 root root 755)
%attr( - ,root,root)				%{appdir}/ROOT
%attr( - ,root,root)				%{appdir}/examples
%attr( - ,root,root)				%{appdir}/manager
%attr( - ,root,root)				%{appdir}/webdav

%changelog
* Wed Mar 20 2002 Ben Reed <ben@opennms.org>
- tomcat 4.0.3

* Thu Nov 15 2001 Ben Reed <ben@opennms.org>
- tomcat's a web server, no point in making the docs RPM require "webserver"

* Fri Oct 24 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0.1
- ajp13 support is now included by default in Tomcat 4.0, so I've included
  stuff from jakarta-tomcat-connectors CVS (20011023) to rebuild ajp connector
  from source
- since you could have also a tomcat 3.x running, listen port in Tomcat 4.0
  rpm have been translated :
  HTTP 1.1 from 8080 to 8180, Proxied HTTP 1.1 from 8081 to 8181,
  HTTP 1.1/SSL from 8443 to 8543 and AJP13 from 8009 to 8109
  HTTP 1.0 from port 8082 to 8182
  if you want to use original values, edit file %{homedir}/conf/server.xml
- connectors activated by default are HTTP 1.1, WARP and AJP13
- rework RPM to have tomcat4 works under user tomcat4 (like tomcat 3.3 RPM)
- fix images corruptions

* Mon Oct 08 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0
- to avoid license problem both source and binary are included in the source rpm
  we use binary tarball to get required jar, jndi, mail, jta...
- Java Managment Extension, jmx is included in source rpm for build purpose but
  won't be installed in binary rpm again for license problem.
- many RPM enhancements from suggestion from Keith Irwin <keith_irwin@non.hp.com>,
  Nicolas Mailhot <nicolas.mailhot@one2team.com> and Jun Inamori. A big thanks
  to all of them
- tomcat run as nobody (finally) in init.d.
  BE CAREFULL NOW WHEN running tomcat as root (in interactive mode for example)
  since you could then make some files unreadable
- a config file is available for init.d script tuning, tomcat.conf, in %{confdir}
- no external parser is required since crimson (jaxp 1.1) is now included
- this RPM is still not FHS, need some reworks...
- no external parser is required since crimson (jaxp 1.1) is now included
- built under Redhat 6.2 with updates, jikes 1.15, ant 1.4, IBM JDK 1.3 (cx130-20010626), jsse 1.0.2

* Thu Sep 13 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0-rc1
- built under Redhat 6.2 with updates, jikes 1.14, IBM JDK 1.3 (cx130-20010626), jsse 1.0.2

* Tue Jun 05 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0b5
- built under Redhat 6.2 with updates, jikes 1.14, IBM JDK 1.3 (cx130-20010502), jsse 1.0.2
- included xerces 1.4.0 

* Fri May 11 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0b4

* Wed Apr 18 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0b3

* Mon Apr 02 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0b2
- second beta release of tomcat 4
- big cleanup of the SPEC which was just to complex.
- Need now a jaxp 1.1 conforming parser, => require xerces-j 1.3.1
- removed jaxp 1.0 / 1.1 from SPEC since we're using xerces-j 1.3.1
  (jaxp 1.1 / crimson is include in tomcat sources)
- YOU MUST HAVE the JSSE 1.0.2 jars in /usr/share/java (I've built a
  jsse RPM from Sun Jars but I COULD'T MAKE IT AVAILABLE...)

* Mon Jan 08 2001 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0b1
- first beta release of tomcat 4

* Thu Dec 14 2000 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0m5

* Thu Nov 02 2000 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0m4

* Tue Oct 24 2000 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0m3 RPM Release 2
- corrected jars included 
- we assume you've installed jsse jars in /usr/share/java
- renamed tomcat4-doc to tomcat4-manual (follow apache RPM naming)
- RH 7.0 changes location of docdir and DocumentRoot. The spec file is modified
  to place manual in right place when rebuilded under RH 6.x or RH 7.0
- compiled on Redhat 6.1 box plus updates with rpm-3.0.5
- build CLASSPATH=

* Thu Oct 19 2000 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0m3

* Tue Oct 17 2000 Henri Gomez <hgomez@slib.fr>
- tomcat-v4.0m2 RPM Release 2
- follow Debian policy about java stuff, libs in /usr/share/java,
  executable in /usr/bin, apps datas in %{homedir}
- prepare transition to RH 7.0 new document root (/var/www)
- build CLASSPATH=/usr/share/java/jsse.jar:/usr/share/java/jcert.jar:/usr/share/java/jnet.jar

* Thu Oct 12 2000 Henri Gomez <hgomez@slib.fr>
- v4.0-m2 
- to allow tomcat 3.x and tomcat 4.x coexistence, the RPM is named tomcat4.
  It start at version 4.0 and all datas will goes in /home/tomcat4.
- tomcat 4.0 need ant post 1.1, xerces 1.2.0, jsse 1.0.1 (min) and jmx.
  The RPM include ant post 1.1 (for build) and jmx (jmxri.jar for build/run) .
  I couldn't include JSSE stuff so you must download JSSE from java.sun.com.
  Next install jcert.jar, jnet.jar and jsse.jar in BOTH
  /usr/lib/java AND JAVA_HOME/jre/lib/ext
- TC 4.0 HTTP 1.1 connector moved from port 8080 to 8180 and
  from port 8081 to 8181 for HTTP 1.0 connector 
  (TC 3.x allready listen on 8080/8081)
 
* Sat Oct 07 2000 Henri Gomez <hgomez@slib.fr>
- v4.0-m1
- RPM never released

* Fri Oct 06 2000 Henri Gomez <hgomez@slib.fr>
- RPM release 2
- tomcat is now installed on /home/tomcat instead of
  /opt/tomcat since /opt is mounted read-only on
  many systems <summer@os2.ami.com.au>
- build CLASSPATH=/usr/lib/java/jsse.jar:/usr/lib/java/jcert.jar:/usr/lib/java/jnet.jar
- correct stuff in spec file (copyright, group...)

* Mon Sep 25 2000 Henri Gomez <hgomez@slib.fr>
- v3.2-beta-5 RPM
- detect jikes and use it to rebuild (yep 10 times faster).
- patched incorrect version number
- rebuilded with jikes 1.12 (and jsse 1.0.2).
