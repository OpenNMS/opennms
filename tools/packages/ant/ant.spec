%define	packname 	jakarta-ant
%define	javalibdir  	/usr/share/java
%define	applibdir	/usr/share/ant

%if "%{rh6}" == "1"
%define manualdir	/home/httpd/html/manual/%{name}
%else
%define manualdir	/var/www/html/manual/%{name}
%endif

Summary: 		ant build tool for java
Name: 			ant
Version: 		1.3
Release: 		2
Group: 			Development/Tools
Copyright: 		Apache
Url: 			http://jakarta.apache.org/ant
BuildArch:		noarch
Source:			http://jakarta.apache.org/builds/jakarta-ant/release/v%{version}/src/%{packname}-%{version}-src.tar.gz
Source1: 		ant.sh
Source2:		%{packname}-%{version}-optional.jar
Source3:		ant.profile
BuildRoot: 		/var/tmp/ant-root
Requires:		regexp > 1.2-3
BuildRequires:		regexp > 1.2-3

%description
Platform-independent build tool for java.
Ant is a Java based build system
Ant is used by apache jakarta&xml projects.

%package manual
Group: 			Documentation
Summary: 		Online manual for ant
Obsoletes:		ant-doc

%description manual
Documentation for ant, Platform-independent build tool for java.
Used by Apache Group for jakarta and xml projects.

%prep
rm -rf $RPM_BUILD_ROOT

%setup -n %{packname}-%{version}

%build
export TCMPDIR=$RPM_BUILD_DIR/%{packname}-%{version}
cp %{SOURCE2} .
export CLASSPATH=${TCMPDIR}/%{packname}-%{version}-optional.jar
if [ -f /usr/bin/jikes ]; then
export CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/rt.jar
export JAVAC=jikes
export ANT_OPTS="-Dbuild.compiler=jikes"
fi

if [ -f /usr/share/java/regexp.jar ]; then
  export CLASSPATH=$CLASSPATH:/usr/share/java/regexp.jar
fi

sh bootstrap.sh 

%install
export TCMPDIR=$RPM_BUILD_DIR/%{packname}-%{version}
mkdir -p $RPM_BUILD_ROOT/dist 
export CLASSPATH=$CLASSPATH:${TCMPDIR}/%{packname}-%{version}-optional.jar

if [ -f /usr/bin/jikes ]; then
export CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/rt.jar
export JAVAC=jikes
export ANT_OPTS="-Dbuild.compiler=jikes"
fi

if [ -f /usr/share/java/regexp.jar ]; then
  export CLASSPATH=$CLASSPATH:/usr/share/java/regexp.jar
fi

sh build.sh dist 

mkdir -p $RPM_BUILD_ROOT/usr/bin
mkdir -p $RPM_BUILD_ROOT%{javalibdir}

# this damn't tomcat build failed in copydir ;-( 
mkdir -p $RPM_BUILD_ROOT/%{applibdir}/bin

install -m 644 dist/lib/ant.jar $RPM_BUILD_ROOT%{javalibdir}
install -m 644 dist/lib/jaxp.jar $RPM_BUILD_ROOT%{javalibdir}
install -m 644 dist/lib/parser.jar $RPM_BUILD_ROOT%{javalibdir}
install -m 644 dist/lib/optional.jar $RPM_BUILD_ROOT%{javalibdir}

# auto-add to the classpath on startup
mkdir -p $RPM_BUILD_ROOT/etc/profile.d
install -m 755 %{SOURCE3} $RPM_BUILD_ROOT/etc/profile.d/%{name}.sh


# remove crazy DOS
for i in dist/bin/antRun dist/bin/runant.pl ; do
	perl -pi -e "s|\015||g;" $i
	install -m 755 $i $RPM_BUILD_ROOT%{applibdir}/bin
done

# fix perl 
perl -pi -e "s|/usr/local/bin/perl|/usr/bin/perl|g;"  $RPM_BUILD_ROOT%{applibdir}/bin/runant.pl

install -m 755 %{SOURCE1} $RPM_BUILD_ROOT/usr/bin/ant

# manual
mkdir -p $RPM_BUILD_ROOT%{manualdir}
cp -prf dist/docs/* $RPM_BUILD_ROOT%{manualdir}

%clean
rm -rf $RPM_BUILD_ROOT

%post

%files
%defattr(-,root,root)
%doc LICENSE README TODO WHATSNEW
%dir       %{javalibdir}
%{_bindir}/ant
%{applibdir}/bin/antRun
%{applibdir}/bin/runant.pl
%{javalibdir}/*.jar

%files manual
%defattr(644 root root 755)
%attr( - ,root,root)  %{manualdir}

%changelog
* Tue Mar 06 2001 Ben Reed <ben@opennms.org>
- removed requires -- manuals DO NOT require a webserver,
  just lynx
- changed the RH62/FHS directory to a command-line define
  instead of having to edit the spec for a compile on
  different distros
- added regexp.jar to the classpath during build
- added the profile.d file to add to the classpath on
  startup

* Mon Mar 05 2001 Henri Gomez <hgomez@slib.fr>
- ant 1.3
- build CLASSPATH=/usr/share/java/bsf.jar:/usr/share/java/jakarta-regexp.jar:/usr/share/java/xalan.jar:/usr/share/java/xerces.jar

* Mon Feb 27 2001 Henri Gomez <hgomez@slib.fr>
- ant-1.3b2 release 3
- fix manual permissions

* Mon Feb 26 2001 Henri Gomez <hgomez@slib.fr>
- ant-1.3b2 release 2
- fix /usr/local/bin/perl references

* Fri Feb 09 2001 Henri Gomez <hgomez@slib.fr>
- ant-1.3b1 
- build CLASSPATH=/usr/share/java/bsf.jar:/usr/share/java/jakarta-regexp.jar:/usr/share/java/xalan.jar:/usr/share/java/xerces.jar

* Wed Oct 25 2000 Henri Gomez <hgomez@slib.fr>
- ant-1.2 RPM Release 2
- renamed ant-doc to ant-manual (follow apache RPM naming)
- RH 7.0 changes location of docdir and DocumentRoot. The spec file is modified
  to place manual in right place when rebuilded under RH 6.x or RH 7.0
- compiled on Redhat 6.1 box plus updates with rpm-3.0.5

* Tue Oct 24 2000 Henri Gomez <hgomez@slib.fr>
- ant-1.2
- added optional.jar 
- read WHATSNEW for changes in ant 1.2

* Fri Oct 20 2000 Henri Gomez <hgomez@slib.fr>
- ant-1.2rc 
- added optional.jar (from jarkata site)
- source file is renamed from jakarta-ant-src.tar.gz to 
  jakarta-ant-src-v1.2rc.tar.gz to allow multiple source file
  in my RPM source dir
- build CLASSPATH=

* Mon Oct 16 2000 Henri Gomez <hgomez@slib.fr>
- ant-1.1 RPM Release 5
- follow Debian policy about java stuff, libs in /usr/share/java,
  executable in /usr/bin
- prepare transition to RH 7.0 new document root (/var/www)
- build CLASSPATH=/usr/share/java/bsf.jar:/usr/share/java/xalan.jar

* Fri Oct 06 2000 Henri Gomez <hgomez@slib.fr>
- v1.1-4
- tomcat build failed if ANT_HOME is set to /usr.
  ant shell script set ANT_HOME to /usr/share/ant 
  to fix 'antRun not found' and allow tomcat build ;-?!
- reorganized group packages for javas RPM :
  Development/Tools (ant)
  Developement/Libraries(xalan, xerces)
  Documentation (all ;-)
- build CLASSPATH=/usr/lib/java/bsf.jar:/usr/lib/java/bsfengines.jar:/usr/lib/java/xalan.jar

* Fri Sep 29 2000 Henri Gomez <hgomez@slib.fr>
- v1.1-3- jars are now installed on /usr/lib/java since /opt is
  mounted read-only on many systems <summer@os2.ami.com.au>
- correct bad URL in apidocs
- try to use jikes if present
- rebuilded with IBM JDK 1.3.0 (cx130-20000815)

* Fri Jul 21 2000 Henri Gomez <hgomez@slib.fr>
- v1.1-2 
- Rebuild rpm with IBM JDK 1.3 (cx130-20000623) to allow ant  
  work under both JDK 1.1.8 and JDK 1.3.
- minor spec file correction (patch ant after install)

* Thu Jul 20 2000 Henri Gomez <hgomez@slib.fr>
- v1.1 final release
- ant need now a JAXP compatible parser. Sun's jaxp 1.0.1 are
  allready included in .tar.gz and so we will use and export
  these jar (jaxp.jar & parser.jar).
  You could also use Apache xerces 1.1.2 or later.
- removed export CLASSPATH= at build time, but you'll have to
  ensure now you have a minimal CLASSPATH (ie: xml parser jars)
- Try to use now the Linux Software Map and Redhat Map.
  exec goes /usr/bin and classes in /usr/lib/ant.
  documentation stay in /home/httpd/html/manual/ant
- Compiled on Redhat 6.1 with latest IBM JDK 1.1.8 (l118-20000515)
 
* Tue May 02 2000 Henri Gomez <hgomez@slib.fr>
- v0.3.1
- From jakarta/tomcat 3.1 final release. Need now to
  have a consistent version number ;-)
- Fixed classpath problem at compile time by cleaning CLASSPATH before
  build/install stages.
- Compiled on Redhat 6.1 with IBM JDK 1.1.8 (20000328)
 
* Thu Apr 13 2000 Henri Gomez <hgomez@slib.fr>
- v0.3.1_rc1
- Version renamed to 0.3.1_rc1 to follow Sam Ruby (rubys@us.ibm.com)
  recommandation since the next major release will be 1.0
 
* Wed Mar 08 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1
- removed moo from ant RPM. Will be now in watchdog RPM.

* Tue Feb 29 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc2

* Fri Feb 25 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc1
- moo is no more in the tar packages, will be released
  in another RPM
- added doc package

* Fri Jan 28 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m1

* Tue Jan 18 2000 Henri Gomez <gomez@slib.fr>
- first RPM of v3.1_m1_rc1 

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- moved from /opt/jakarta/jakarta-tools to /opt/ant

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- CVS 4 Jan 2000 
- added servlet.jar from tomcat in SRPM
   to allow first build of moo.

* Thu Dec 30 1999 Henri Gomez <gomez@slib.fr>
- Initial release for jakarta-tools cvs


