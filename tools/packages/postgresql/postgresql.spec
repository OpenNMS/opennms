%{!?perl:%define perl 1}
%{!?tcl:%define tcl 1}
%{!?tkpkg:%define tkpkg 1}
%{!?odbc:%define odbc 1}
%{!?jdbc:%define jdbc 1}
%{!?test:%define test 1}
%{!?python:%define python 1}
%{!?pltcl:%define pltcl 1}
%{?forceplperl:%define plperl %{expand:forceplperl}}
%{!?forceplperl:%define forceplperl 0}
%{!?plperl:%define plperl 0}
%{!?ssl:%define ssl 0}
%{!?kerberos:%define kerberos 1}

# Utility feature defines.
%{!?enable_mb:%define enable_mb 1}
%{!?pgaccess:%define pgaccess 1}
%{!?newintarray:%define newintarray 0}

# Python major version.
%{expand: %%define pyver %(python -c 'import sys;print(sys.version[0:3])')}

Summary: PostgreSQL client programs and libraries.
Name: postgresql
Version: 7.1.3

# Conventions for PostgreSQL Global Development Group RPM releases:

# Official PostgreSQL Development Group RPMS have a PGDG after the release number.
# Integer releases are stable -- 0.1.x releases are Pre-releases, and x.y are
# test releases.

# Pre-releases are those that are built from CVS snapshots or pre-release
# tarballs from postgresql.org.  Official beta releases are not 
# considered pre-releases, nor are release candidates, as their beta or
# release candidate status is reflected in the version of the tarball. Pre-
# releases' versions do not change -- the pre-release tarball of 7.0.3, for
# example, has the same tarball version as the final official release of 7.0.3:
# but the tarball is different.

# Test releases are where PostgreSQL itself is not in beta, but certain parts of
# the RPM packaging (such as the spec file, the initscript, etc) are in beta.

# Pre-release RPM's should not be put up on the public ftp.postgresql.org server
# -- only test releases or full releases should be.

Release: 0.onms.4
License: BSD
Group: Applications/Databases
Source0: ftp://ftp.postgresql.org/pub/source/v%{version}/postgresql-%{version}.tar.gz
Source3: postgresql.init
Source4: file-lists.tar.gz
Source5: ftp://ftp.postgresql.org/pub/source/v%{version}/postgresql-%{version}.tar.gz.md5
Source6: README.rpm-dist
Source7: migration-scripts.tar.gz
Source10: http://www.retep.org.uk/postgres/jdbc7.0-1.1.jar
Source11: jdbc7.1-1.2.jar
Source12: postgresql-dump.1.gz
Source13: jdbc7.1-1.3.jar
Source14: rh-pgdump.sh
Source15: postgresql-bashprofile
Source16: http://www.sai.msu.su/~megera/postgres/gist/code/7.1.2/contrib-intarray.tar.gz
Patch1: rpm-pgsql-7.1.patch
Patch2: %{name}-7.1.plperl.patch
Patch3: %{name}-7.1.s390x.patch
Patch4: %{name}-conf-update.patch.bz2
Buildrequires: perl glibc-devel
Prereq: /sbin/ldconfig initscripts
BuildPrereq: python-devel perl tcl /lib/cpp
%if %ssl
BuildPrereq: openssl-devel
%endif
%if %kerberos
BuildPrereq: krb5-devel
%endif
Url: http://www.postgresql.org/ 
Obsoletes: postgresql-clients
Buildroot: %{_tmppath}/%{name}-%{version}-root
# Obsolete the packages we are not building...
%if ! %{plperl}
Obsoletes: postgresql-plperl
%endif
%if ! %{tcl}
Obsoletes: postgresql-tcl
%endif
%if ! %{tkpkg}
Obsoletes: postgresql-tk
%endif
%if ! %{odbc}
Obsoletes: postgresql-odbc
%endif
%if ! %{perl}
Obsoletes: postgresql-perl
%endif
%if ! %{python}
Obsoletes: postgresql-python
%endif
%if ! %{jdbc}
Obsoletes: postgresql-jdbc
%endif
%if ! %{test}
Obsoletes: postgresql-test
%endif



# This is the PostgreSQL Global Development Group Official RPMset spec file,
# or a derivative thereof.
# Copyright 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
# and others listed.

# Major Contributors:
# ---------------
# Lamar Owen
# Trond Eivind Glomsrød <teg@redhat.com>
# Thomas Lockhart
# Reinhard Max
# Karl DeBisschop
# and others in the Changelog....

# This spec file and ancilliary files are licensed in accordance with 
# The PostgreSQL license.

# On top of this file you can find the default build package list macros.  These can be overridden by defining
# on the rpm command line:
# rpm --define 'packagename 1' .... to force the package to build.
# rpm --define 'packagename 0' .... to force the package NOT to build.
# The base package, the lib package, the devel package, and the server package always get built.


%description
PostgreSQL is an advanced Object-Relational database management system
(DBMS) that supports almost all SQL constructs (including
transactions, subselects and user-defined types and functions). The
postgresql package includes the client programs and libraries that
you'll need to access a PostgreSQL DBMS server.  These PostgreSQL
client programs are programs that directly manipulate the internal
structure of PostgreSQL databases on a PostgreSQL server. These client
programs can be located on the same machine with the PostgreSQL
server, or may be on a remote machine which accesses a PostgreSQL
server over a network connection. This package contains the docs
in HTML for the whole package, as well as command-line utilities for
managing PostgreSQL databases on a PostgreSQL server. 

If you want to manipulate a PostgreSQL database on a remote PostgreSQL
server, you need this package. You also need to install this package
if you're installing the postgresql-server package.

%package libs
Summary: The shared libraries required for any PostgreSQL clients.
Group: Applications/Databases
Provides: libpq.so.2.1 libpq.so.2.0 libpq.so

%description libs
The postgresql-libs package provides the essential shared libraries for any 
PostgreSQL client program or interface. You will need to install this package
to use any other PostgreSQL package or any clients that need to connect to a
PostgreSQL server.

%package server
Summary: The programs needed to create and run a PostgreSQL server.
Group: Applications/Databases
Prereq: /usr/sbin/useradd /sbin/chkconfig 
Requires: postgresql = %{version} libpq.so

%package docs
Summary: Extra documentation for PostgreSQL
Group: Applications/Databases
%description docs
The postgresql-docs package includes the SGML source for the documentation
as well as the documentation in other formats, and some extra documentation.
Install this package if you want to help with the PostgreSQL documentation
project, or if you want to generate printed documentation.

%package contrib
Summary: Contributed source and binaries distributed with PostgreSQL
Group: Applications/Databases
Requires: libpq.so postgresql = %{version}
%description contrib
The postgresql-contrib package includes the contrib tree distributed with
the PostgreSQL tarball.  Selected contrib modules are prebuilt.

%description server
The postgresql-server package includes the programs needed to create
and run a PostgreSQL server, which will in turn allow you to create
and maintain PostgreSQL databases.  PostgreSQL is an advanced
Object-Relational database management system (DBMS) that supports
almost all SQL constructs (including transactions, subselects and
user-defined types and functions). You should install
postgresql-server if you want to create and maintain your own
PostgreSQL databases and/or your own PostgreSQL server. You also need
to install the postgresql package.

%package devel
Summary: PostgreSQL development header files and libraries.
Group: Development/Libraries
Requires: postgresql-libs = %{version}

%description devel
The postgresql-devel package contains the header files and libraries
needed to compile C or C++ applications which will directly interact
with a PostgreSQL database management server and the ecpg Embedded C
Postgres preprocessor. You need to install this package if you want to
develop applications which will interact with a PostgreSQL server. If
you're installing postgresql-server, you need to install this
package.

#------------
%if %plperl
%package plperl
Summary: The PL/Perl procedural language for PostgreSQL.
Group: Applications/Databases
Requires: perl, postgresql = %{version}

%description plperl
PostgreSQL is an advanced Object-Relational database management
system.  The postgresql-plperl package contains the the PL/Perl
procedural language for the backend.
%endif

#------------
%if %tcl
%package tcl
Summary: A Tcl client library, and the PL/Tcl procedural language for PostgreSQL.
Group: Applications/Databases
Requires: tcl >= 8.0

%description tcl
PostgreSQL is an advanced Object-Relational database management
system.  The postgresql-tcl package contains the libpgtcl client library,
the pg-enhanced pgtclsh, and the PL/Tcl procedural language for the backend.
%endif

#------------
%if %tkpkg
%package tk
Summary: Tk shell and tk-based GUI for PostgreSQL.
Group: Applications/Databases
Requires: tcl >= 8.0, tk >= 8.0

%description tk
PostgreSQL is an advanced Object-Relational database management
system.  The postgresql-tk package contains the pgaccess
program. Pgaccess is a graphical front end, written in Tcl/Tk, for the
psql and related PostgreSQL client programs.
%endif

#------------
%if %odbc
%package odbc
Summary: The ODBC driver needed for accessing a PostgreSQL DB using ODBC.
Group: Applications/Databases

%description odbc
PostgreSQL is an advanced Object-Relational database management
system. The postgresql-odbc package includes the ODBC (Open DataBase
Connectivity) driver and sample configuration files needed for
applications to access a PostgreSQL database using ODBC.
%endif

#------------
%if %perl
%package perl
Summary: Development module needed for Perl code to access a PostgreSQL DB.
Group: Applications/Databases
Requires: perl >= 5.004-4

%description perl
PostgreSQL is an advanced Object-Relational database management
system. The postgresql-perl package includes a module for developers
to use when writing Perl code for accessing a PostgreSQL database.
%endif

#------------
%if %python
%package python
Summary: Development module for Python code to access a PostgreSQL DB.
Group: Applications/Databases
Requires: python >= 1.5
Conflicts: python >= 1.6


%description python
PostgreSQL is an advanced Object-Relational database management
system.  The postgresql-python package includes a module for
developers to use when writing Python code for accessing a PostgreSQL
database.
%endif

#----------
%if %jdbc
%package jdbc
Summary: Files needed for Java programs to access a PostgreSQL database.
Group: Applications/Databases

%description jdbc
PostgreSQL is an advanced Object-Relational database management
system. The postgresql-jdbc package includes the .jar file needed for
Java programs to access a PostgreSQL database.
%endif

#------------
%if %test
%package test
Summary: The test suite distributed with PostgreSQL.
Group: Applications/Databases
Requires: postgresql = %{version}

%description test
PostgreSQL is an advanced Object-Relational database management
system. The postgresql-test package includes the sources and pre-built
binaries of various tests for the PostgreSQL database management
system, including regression tests and benchmarks.
%endif

%prep
%setup -q 

%patch1 -p1

#PL/Perl stuff
%patch2 -p1
%patch3 -p1
%patch4 -p1

%if %forceplperl
FORCEPLPERL=--force
%else
FORCEPLPERL=
%endif

%if %plperl
        pushd src/pl/plperl
        EXTRA_INCLUDES=-I../../include plperl_installdir=$RPM_BUILD_ROOT/usr/lib perl Makefile.PL $FORCEPLPERL
        popd
%endif
#

# Newer, optional intarray in contrib.
%if %newintarray
	pushd contrib
	rm -fr intarray
	tar xvzf %{SOURCE16}
	popd
%endif

%build

# Get file lists....
tar xzf %{SOURCE4}

#Commented out for testing on other platforms for now.
# If libtool installed, copy some files....
#if [ -d /usr/share/libtool ]
#then
#	cp /usr/share/libtool/config.* .
#fi

# Strip out -ffast-math from CFLAGS....

export CFLAGS=`echo $RPM_OPT_FLAGS|xargs -n 1|grep -v ffast-math|xargs -n 100`
export CXXFLAGS="$CFLAGS"

./configure --enable-locale  --with-CXX --prefix=/usr\
%if %perl
	--with-perl \
%endif
%if %enable_mb
	--enable-multibyte \
%endif
%if %tcl
	--with-tcl \
%endif
%if %tkpkg
%else
	--without-tk \
%endif
%if %odbc
	--with-odbc \
%endif
	--enable-syslog\
%if %python
	--with-python \
%endif
%if %ssl
	--with-openssl \
%endif
%if %kerberos
	--with-krb5=/usr/kerberos \
%endif
	--sysconfdir=/etc/pgsql \
	--mandir=%{_mandir} \
	--docdir=%{_docdir} \
	--includedir=%{_includedir}/pgsql \
	--datadir=/usr/share/pgsql

if [ -x /usr/bin/getconf ]; then
	CPUS=`getconf _NPROCESSORS_ONLN`
fi
if test "x$CPUS" = "x" -o "x$CPUS" = "x0"; then
	CPUS=1
fi

make -j$CPUS COPT="$CFLAGS" all

%if %plperl
        pushd src/pl/plperl
  make INC=-I../../include
        popd
%endif

%if %python
	pushd src
	pushd interfaces/python
	cp /usr/lib/python%{pyver}/config/Makefile.pre.in .
	echo *shared* > Setup
	echo _pg pgmodule.c -I../../include -I../libpq -L../libpq -lpq -lcrypt >> Setup
	make -f Makefile.pre.in boot
	make
	popd
	popd
%endif

%if %test
	pushd src/test
	make all
	popd
%endif

%install
rm -rf $RPM_BUILD_ROOT

make DESTDIR=$RPM_BUILD_ROOT install

%if %plperl
make DESTDIR=$RPM_BUILD_ROOT -C src/pl/plperl install
#cp blib/arch/auto/plperl/plperl.so $RPM_BUILD_ROOT/usr/lib
%endif

%if %perl
	make PREFIX=$RPM_BUILD_ROOT/usr -C src/interfaces/perl5 -f Makefile install

	# Get rid of the packing list generated by the perl Makefile, and build my own...
	find $RPM_BUILD_ROOT/usr/lib/perl5 -name .packlist -exec rm -f {} \;
	find $RPM_BUILD_ROOT/usr/lib/perl5 -type f -print | \
		sed -e "s|$RPM_BUILD_ROOT/|/|g"  | \
		sed -e "s|.*/man/.*|&\*|" > perlfiles.list
	find $RPM_BUILD_ROOT/usr/lib/perl5 -type d -name Pg -print | \
		sed -e "s|$RPM_BUILD_ROOT/|%dir /|g" >> perlfiles.list
	
	# check and fixup Pg manpage location....
	if [ ! -e $RPM_BUILD_ROOT%{_mandir}/man3/Pg.* ]
	then
		mkdir -p $RPM_BUILD_ROOT%{_mandir}/man3
		cp `find $RPM_BUILD_ROOT -name 'Pg.3*' -print` $RPM_BUILD_ROOT%{_mandir}/man3
	fi
	
	pushd src/interfaces
	mkdir -p $RPM_BUILD_ROOT/usr/share/pgsql/perl5
	cp -a perl5/test.pl $RPM_BUILD_ROOT/usr/share/pgsql/perl5
	popd
	# remove perllocal.pod and Pg.bs from the file list - only occurs with 5.6

	perl -pi -e "s/^.*perllocal.pod$//" perlfiles.list
	perl -pi -e "s/^.*Pg.bs$//" perlfiles.list
	mkdir -p $RPM_BUILD_ROOT/usr/lib/perl5/site_perl/%{_arch}-linux/auto/Pg

%endif

make -C doc


# man pages....
pushd $RPM_BUILD_ROOT%{_mandir}
tar xzf $RPM_BUILD_DIR/postgresql-%{version}/doc/man.tar.gz

# the postgresql-dump manpage.....
cp %{SOURCE12} man1
popd

# install the dump script

install -m755 %SOURCE14 $RPM_BUILD_ROOT/usr/bin/

# install dev headers.

make DESTDIR=$RPM_BUILD_ROOT install-all-headers

#fixup directory permissions for SPI stuff...
#pushd $RPM_BUILD_ROOT/usr/include/pgsql
#chmod 755 access catalog executor nodes rewrite storage tcop utils
#popd

#popd

# copy over Makefile.global to the include dir....
install -m755 src/Makefile.global $RPM_BUILD_ROOT/usr/include/pgsql

%if %pgaccess
	# pgaccess installation
	pushd src/bin
	install -m 755 pgaccess/pgaccess $RPM_BUILD_ROOT/usr/bin
	mkdir -p $RPM_BUILD_ROOT/usr/share/pgsql/pgaccess
	install -m 644 pgaccess/main.tcl $RPM_BUILD_ROOT/usr/share/pgsql/pgaccess
	tar cf - pgaccess/lib pgaccess/images | tar xf - -C $RPM_BUILD_ROOT/usr/share/pgsql
	cp -a pgaccess/doc/html   ../../doc/pgaccess
	cp    pgaccess/demo/*.sql ../../doc/pgaccess
	popd
%endif

%if %python
	# Python
	pushd src/interfaces/python
	# Makefile.pre.in doesn't yet support .py files anyway, so we stick to a manual installation
	  mkdir -p $RPM_BUILD_ROOT/usr/lib/python%{pyver}/site-packages
	  install -m 755 _pgmodule.so *.py $RPM_BUILD_ROOT/usr/lib/python%{pyver}/site-packages/
	popd
%endif

%if %jdbc
	# Java/JDBC
	# The user will have to set a CLASSPATH to find it here, but not sure where else to put it...

	# Install 7.0 JDBC jars 
	install -m 755 %{SOURCE10} $RPM_BUILD_ROOT/usr/share/pgsql
	install -m 755 %{SOURCE11} $RPM_BUILD_ROOT/usr/share/pgsql
	install -m 755 %{SOURCE13} $RPM_BUILD_ROOT/usr/share/pgsql

%endif

# Fixup more permissions...
chmod 644 $RPM_BUILD_ROOT%{_mandir}/*/*
chmod +x $RPM_BUILD_ROOT/usr/lib/lib*.so.*

# The initscripts....
# Redhat-style....
if [ -d /etc/rc.d/init.d ]
then
	install -d $RPM_BUILD_ROOT/etc/rc.d/init.d
	install -m 755 %{SOURCE3} $RPM_BUILD_ROOT/etc/rc.d/init.d/postgresql
	mv redhat-style-files.lst files.lst
fi

# SuSE-style....
# NOTE: SuSE stuff not yet fully implemented -- this is likely to not work yet.
# Putting SuSE-style stuff here
if [ -d /sbin/init.d ]
then
	# install the SuSE stuff...
	mv suse-style-files.lst files.lst
fi


# PGDATA needs removal of group and world permissions due to pg_pwd hole.
install -d -m 700 $RPM_BUILD_ROOT/var/lib/pgsql/data

# backups of data go here...
install -d -m 700 $RPM_BUILD_ROOT/var/lib/pgsql/backups

# Move the PL's to the right place
mkdir -p $RPM_BUILD_ROOT/usr/lib/pgsql
mv $RPM_BUILD_ROOT/usr/lib/pl*.so $RPM_BUILD_ROOT/usr/lib/pgsql

# postgres' .bash_profile
install -m 644 %{SOURCE15} $RPM_BUILD_ROOT/var/lib/pgsql/.bash_profile

%if %test
	# tests. There are many files included here that are unnecessary, but include
	# them anyway for completeness.
	mkdir -p $RPM_BUILD_ROOT/usr/lib/pgsql/test
	cp -a src/test/regress $RPM_BUILD_ROOT/usr/lib/pgsql/test
	install -m 0755 config/config.guess $RPM_BUILD_ROOT/usr/lib/pgsql
	install -m 0755 contrib/spi/refint.so $RPM_BUILD_ROOT/usr/lib/pgsql/test/regress
	install -m 0755 contrib/spi/autoinc.so $RPM_BUILD_ROOT/usr/lib/pgsql/test/regress
	pushd  $RPM_BUILD_ROOT/usr/lib/pgsql/test/regress/
	strip *.so
	popd
%endif

# Upgrade scripts.
pushd $RPM_BUILD_ROOT
tar xzf %{SOURCE7}
popd

# logrotate script removed until future release
#logrotate script source (which needs WORK)
#mkdir -p $RPM_BUILD_ROOT/etc/logrotate.d
#cp %{SOURCE8} $RPM_BUILD_ROOT/etc/logrotate.d/postgres
#chmod 0644 $RPM_BUILD_ROOT/etc/logrotate.d/postgres

# Fix some more documentation
# no need for the OS2 client
rm -rf contrib/os2client
gzip doc/internals.ps
cp %{SOURCE6} README.rpm-dist
mv $RPM_BUILD_ROOT%{_docdir}/postgresql/html doc

# Build contrib stuff....
pushd contrib
make clean
make all
popd 
# move the contrib tree to the right place after building....
cp -r contrib $RPM_BUILD_ROOT/usr/lib/pgsql
# We'll do more prep work in a later release.....

#more massaging

pushd $RPM_BUILD_ROOT/usr/lib/pgsql/contrib

# Get rid of useless makefiles
rm -f Makefile */Makefile
# earthdistance
pushd earthdistance
perl -pi -e "s|/usr/share/pgsql/contrib|/usr/lib/pgsql/contrib/earthdistance|" *
popd

# array
pushd array
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/array|" *
popd

# cube
pushd cube
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/cube|" cube.sql
popd

# fulltext
pushd fulltextindex
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/fulltextindex|" *.sql
popd

# intarray
pushd intarray
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/intarray|" *.sql
popd

# isbn_issn
pushd isbn_issn
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/isbn_issn|" *.sql
popd

# lo
pushd lo
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/lo|" *.sql
popd

# miscutil
pushd miscutil
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/miscutil|" *.sql
popd

# noup
pushd noupdate
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/noupdate|" *.sql
popd

# pgcrypto
pushd pgcrypto
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/pgcrypto|" *.sql
popd


# rserv
pushd rserv
perl -pi -e "s|/usr/share/|/usr/lib/|" *
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/rserv|" *
perl -pi -e "s|/usr/bin|/usr/lib/pgsql/contrib/rserv|" *
perl -pi -e "s|/usr/lib/pgsql/contrib\"|/usr/lib/pgsql/contrib/rserv\"|" *
perl -pi -e "s|/usr/lib/pgsql/contrib$|/usr/lib/pgsql/contrib/rserv|" *
popd

# seg 
pushd seg
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/seg|" *.sql
popd

# soundex
pushd soundex
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/soundex|" *.sql
popd

# spi
pushd spi
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/spi|" *.sql
popd

# string
pushd string
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/string|" *.sql
popd

# userlock
pushd userlock
perl -pi -e "s|/usr/lib/contrib|/usr/lib/pgsql/contrib/userlock|" *.sql
popd

popd

# Fix a dangling symlink
mkdir -p $RPM_BUILD_ROOT/usr/include/pgsql/port
cp src/include/port/linux.h $RPM_BUILD_ROOT/usr/include/pgsql/port/
ln -sf port/linux.h $RPM_BUILD_ROOT/usr/include/pgsql/os.h

#more broken symlinks
rm -f $RPM_BUILD_ROOT/usr/lib/pgsql/contrib/pg_resetxlog/pg_crc.c $RPM_BUILD_ROOT/usr/lib/pgsql/contrib/pg_controldata/pg_crc.c
cp src/backend/utils/hash/pg_crc.c $RPM_BUILD_ROOT/usr/lib/pgsql/contrib/pg_resetxlog/pg_crc.c
ln $RPM_BUILD_ROOT/usr/lib/pgsql/contrib/pg_resetxlog/pg_crc.c $RPM_BUILD_ROOT/usr/lib/pgsql/contrib/pg_controldata/pg_crc.c

# Symlink libpq.so.2.0 to libpq.so.2.1 for backwards compatibility, until 
# -soname patches are the norm.
pushd $RPM_BUILD_ROOT/usr/lib
ln -s libpq.so.2.1 libpq.so.2.0
popd


%pre
# Need to make backups of some executables if an upgrade
# They will be needed to do a dump of the old version's database.
# All output redirected to /dev/null.

if [ $1 -gt 1 ]
then
   mkdir -p /usr/lib/pgsql/backup > /dev/null
   pushd /usr/bin > /dev/null
   cp -fp postmaster postgres pg_dump pg_dumpall psql /usr/share/pgsql/backup > /dev/null 2>&1  || :
   popd > /dev/null
   pushd /usr/lib > /dev/null
   cp -fp libpq.* /usr/share/pgsql/backup > /dev/null 2>&1 || :
   popd > /dev/null
fi

%post libs -p /sbin/ldconfig 
%postun libs -p /sbin/ldconfig 

%pre server
groupadd -g 26 -o -r postgres >/dev/null 2>&1 || :
useradd -M -n -g postgres -o -r -d /var/lib/pgsql -s /bin/bash \
	-c "PostgreSQL Server" -u 26 postgres >/dev/null 2>&1 || :
touch /var/log/pgsql
chown postgres.postgres /var/log/pgsql
chmod 0700 /var/log/pgsql


%post server
chkconfig --add postgresql
/sbin/ldconfig

%preun server
if [ $1 = 0 ] ; then
	chkconfig --del postgresql
fi

%postun server
/sbin/ldconfig 
if [ $1 -ge 1 ]; then
  /sbin/service postgresql condrestart >/dev/null 2>&1
fi
if [ $1 = 0 ] ; then
	userdel postgres >/dev/null 2>&1 || :
	groupdel postgres >/dev/null 2>&1 || : 
fi

%if %odbc
%post -p /sbin/ldconfig  odbc
%postun -p /sbin/ldconfig  odbc
%endif

%if %tcl
%post -p /sbin/ldconfig   tcl
%postun -p /sbin/ldconfig   tcl
%endif

%if %plperl
%post -p /sbin/ldconfig   plperl
%postun -p /sbin/ldconfig   plperl
%endif

%if %test
%post test
chown -R postgres.postgres /usr/share/pgsql/test >/dev/null 2>&1 || :
%endif

%clean
rm -rf $RPM_BUILD_ROOT
rm -f perlfiles.list

# Ok, we are dynamically generating some filelists.  These are by default
# under the BUILD/postgresql-x.y.z tree.

# Note that macros such as config are available in those lists.
# The lists differentiate between RedHat, SuSE, and others.

%files
%defattr(-,root,root)
%doc doc/FAQ doc/KNOWN_BUGS doc/MISSING_FEATURES doc/README* 
%doc COPYRIGHT README HISTORY doc/bug.template
%doc README.rpm-dist
%doc doc/html
/usr/bin/createdb
/usr/bin/createlang
/usr/bin/createuser
/usr/bin/dropdb
/usr/bin/droplang
/usr/bin/dropuser
/usr/bin/pg_dump
/usr/bin/pg_dumpall
/usr/bin/pg_restore
/usr/bin/psql
/usr/bin/vacuumdb
%{_mandir}/man1/createdb.1*
%{_mandir}/man1/createlang.1*
%{_mandir}/man1/createuser.1*
%{_mandir}/man1/dropdb.1*
%{_mandir}/man1/droplang.1*
%{_mandir}/man1/dropuser.1*
%{_mandir}/man1/pg_dump.1*
%{_mandir}/man1/pg_dumpall.1*
%{_mandir}/man1/psql.1*
%{_mandir}/manl/*
%dir %{_mandir}/manl/

%files docs
%defattr(-,root,root)
%doc doc/src/*

%files contrib
%defattr(-,root,root)
%dir /usr/lib/pgsql/contrib/
/usr/lib/pgsql/contrib/*

%files libs
%defattr(-,root,root)
/usr/lib/libpq.so.*
/usr/lib/libecpg.so.*
/usr/lib/libpq++.so.*
/usr/lib/libpgeasy.so.*

%files server -f files.lst
%defattr(-,root,root)
/usr/bin/initdb
/usr/bin/initlocation
/usr/bin/ipcclean
/usr/bin/pg_ctl
/usr/bin/pg_encoding
/usr/bin/pg_id
/usr/bin/pg_passwd
/usr/bin/postgres
/usr/bin/postgresql-dump
/usr/bin/postmaster
/usr/bin/rh-pgdump.sh
%{_mandir}/man1/initdb.1*
%{_mandir}/man1/initlocation.1*
%{_mandir}/man1/ipcclean.1*
%{_mandir}/man1/pg_ctl.1*
%{_mandir}/man1/pg_passwd.1*
%{_mandir}/man1/postgres.1*
%{_mandir}/man1/postmaster.1*
%{_mandir}/man1/vacuumdb.1*
%{_mandir}/man1/postgresql-dump.1*
/usr/share/pgsql/global.bki
/usr/share/pgsql/global.description
/usr/share/pgsql/template1.bki
/usr/share/pgsql/template1.description
/usr/share/pgsql/*.sample
/usr/lib/pgsql/plpgsql.so
%dir /usr/lib/pgsql
%dir /usr/share/pgsql
%attr(700,postgres,postgres) %dir /usr/share/pgsql/backup
/usr/share/pgsql/backup/pg_dumpall_new
%attr(700,postgres,postgres) %dir /var/lib/pgsql
%attr(700,postgres,postgres) %dir /var/lib/pgsql/data
%attr(700,postgres,postgres) %dir /var/lib/pgsql/backups
%attr(644,postgres,postgres) %config(noreplace) /var/lib/pgsql/.bash_profile

%files devel
%defattr(-,root,root)
/usr/include/pgsql/*
%dir /usr/include/pgsql
/usr/bin/ecpg
/usr/lib/lib*.a
/usr/bin/pg_config
/usr/lib/libpq.so
/usr/lib/libecpg.so
/usr/lib/libpq++.so
/usr/lib/libpgeasy.so
%{_mandir}/man1/ecpg.1*

%if %tcl
%files tcl
%defattr(-,root,root)
%attr(755,root,root) /usr/lib/libpgtcl.so*
/usr/bin/pgtclsh
%{_mandir}/man1/pgtclsh.1*
/usr/lib/pgsql/pltcl.so
%endif

%if %tkpkg
%files tk
%defattr(-,root,root)
/usr/bin/pgtksh
%{_mandir}/man1/pgtksh.1*
%endif
%if %pgaccess
%doc doc/pgaccess/*
/usr/share/pgsql/pgaccess
/usr/bin/pgaccess
%{_mandir}/man1/pgaccess.1*
%endif

%if %odbc
%files odbc
%defattr(-,root,root)
%attr(755,root,root) /usr/lib/libpsqlodbc.so*
%config(noreplace) /etc/pgsql/odbcinst.ini
/usr/share/pgsql/odbc.sql
%endif

%if %perl
%files -f perlfiles.list perl
%defattr (-,root,root)
%dir /usr/lib/perl5/site_perl/%{_arch}-linux/auto
/usr/share/pgsql/perl5
%{_mandir}/man3/Pg.*
%endif

%if %plperl
%files plperl
%defattr(-,root,root)
/usr/lib/pgsql/plperl.so
%endif

%if %python
%files python
%defattr(-,root,root)
%doc src/interfaces/python/README src/interfaces/python/tutorial
/usr/lib/python%{pyver}/site-packages/_pgmodule.so
/usr/lib/python%{pyver}/site-packages/*.py
%endif

%if %jdbc
%files jdbc
%defattr(-,root,root)
/usr/share/pgsql/jdbc7.0-1.1.jar
/usr/share/pgsql/jdbc7.1-1.2.jar
/usr/share/pgsql/jdbc7.1-1.3.jar
%endif

%if %test
%files test
%defattr(-,postgres,postgres)
%attr(755,postgres,postgres) /usr/lib/pgsql/config.guess
%attr(-,postgres,postgres) /usr/lib/pgsql/test/*
%attr(-,postgres,postgres) %dir /usr/lib/pgsql/test
%endif

%changelog
* Fri Aug 17 2001 Lamar Owen <lamar.owen@wgcr.org>
- 7.1.3-1PGDG
- Kerberos auth optional.
- Sync with latest Rawhide RPMset.
- Minor README.rpm-dist updates.
- Handle stop with stale pid file.
- Make packages own their directories.

* Tue Jul 18 2001 Lamar Owen <lamar.owen@wgcr.org>
- Sync with latest Red Hat RPMset.  Enable test package by default for PGDG releases.
- Minor updates to README.rpm-dist.

* Mon Jul  9 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Make the -devel subpackage depend on -libs, not the main package

* Tue Jun 19 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Make sure the python subpackage depends on mx - otherwise, you'd get an error when importing

* Mon Jun 18 2001 Florian La Roche <Florian.LaRoche@redhat.de>
- add changes for s390x

* Fri Jun 15 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Many path fixes for contrib packages
- Newer intarray
- Don't use nested ifs for tkpkg/pgaccess
- Turn off test package for Red Hat Linux - it makes most sense just to use 
  this during development
- One "pgacess" -> "pgaccess"

* Wed Jun 13 2001 Lamar Owen <lamar.owen@wgcr.org> <lamar@postgresql.org>
- -4PGDG
- patchset update for pg_regress.sh
- can the dot in the release -- confused too many people and some programs.
- -3.PGDG
- updated README.rpm-dist
- updated patch to 7.1.2, fixing some places where /usr/share/pgsql 
  was still being used where /usr/lib/pgsql was intended.
- PGLIB now set to /usr/lib/pgsql -- initdb doesn't use it to find the bki's.

* Tue Jun 12 2001 Trond Eivind Glomsrød <teg@redhat.com>
- sync
- Run chkconfig --add on server install
- Move the prereqs on useradd on chkconfig to the server package
- don't disable the %%clean section

* Sat Jun 09 2001 Lamar Owen <lamar.owen@wgcr.org>
- Sync up with Trond Eivind's set.

* Thu Jun  7 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Don't create postgres' .bashrc from the server post script: include it instead
- Move the test packages from /usr/share/pgsql to /usr/lib/pgsql
- Move the symlinks libpq.so, libecpg.so, libpq++.so and libpgeasy.so to the devel subpackage from 
  libs
- Source the i18n data from the .bashrc instead of in the initscript

* Mon May 28 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Handle i18n for database startup - backend needs to have the same locale everytime, 
  but this certainly can't be hardcoded to C either
- Fix stop, restart in initscript (#42217)
- Make database init _much_ less verbose
- other minor fixes to the initscript

* Fri May 25 2001 Trond Eivind Glomsrød <teg@redhat.com>
- 7.1.2

* Thu May 24 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- 7.1.2-0.1.1.PGDG PRERELEASE
- Changed versioning and release numbering a little for better flexibility,
-- particularly for distribution packagers.
- Release numbering comment at top of spec, where it's more useful.
- Trimmed changelog to 7.1.x. See the last 7.0.3's specfile for the 7.0
  changlog, and the last 6.5.3's specfile for the changelog prior to
  7.0.

* Sun May 20 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- 7.1.1-4.PGDG
- _really_ got Python version agnosticism working.

* Sat May 19 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- 7.1.1-3.PGDG Release
- Python version agnosticism.....

* Tue May 15 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Use openssl
- Make it obsolete subpackages if they aren't built

* Mon May 14 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- 7.1.1-2.PGDG Release.
- Appended .PGDG to release string to differentiate our RPM set from the others.
- /bin/sh fix in initscript
- README.rpm-dist updates.

* Mon May 14 2001 Bernhard Rosenkraenzer <bero@redhat.com> 7.1.1-0.7
- Rebuild with new readline

* Thu May 10 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Initial 7.1.1

* Mon May  7 2001 Trond Eivind Glomsrød <teg@redhat.com>
- Specify shell when running commands through su in the initscript, to avoid problems 
  when people switch the postgres user to use tcsh

* Thu Apr 19 2001 Trond Eivind Glomsrød <teg@redhat.com>
- JDBC driver for Postgresql 7.1

* Sun Apr 15 2001 Trond Eivind Glomsrød <teg@redhat.com>
- slightly different versioning scheming, will go back to the official
  later when I'm sure the package is ready to use

* Fri Apr 13 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- 7.1 RELEASE
- 7.1-1 RPM RELEASE
- Change to COPTS -- strip out -ffastmath -- Considered Harmful.
- Back to old versioning scheme, kept teg's other fixes.
- README.rpm-dist updated.
- PGVERSION updated all-around (hopefully!)
- Couple of fixes from Peter E.
- Rearrange dependencies -- only the -libs subpackage is required for most stuff
- Removed broken and confusing logrotate script.

* Mon Apr  9 2001 Trond Eivind Glomsrød <teg@redhat.com>
- chown considered harmful - removed
- fix dangling symlimks (pg_crc.c)
- libpq.so changes for maximum compatiblity
- different versioning scheme, to avoid trouble later
- remove temporary perl file from the file list
- fix spelling error in tcl description
- mark odbcinst.ini as a config file
- use %%defattr on packages which didn't have it

* Sat Apr 07 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- Integrated the PL/Perl stuff from Karl DeBisschop --conditional.
- Packaging reorg: added contrib and docs subpackages.
- Removed sgml source docs from main package --> docs subpackage.
- Removed contrib tree from main package --> contrib subpackage.
- Contrib tree is now prebuilt -- HOWEVER, very little install work is
  currently done with this. 

* Fri Apr 06 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- Quickie RC3.  There will be a 7.1RC3-2 shortly with other stuff.

* Tue Apr 03 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- RC2
- eliminate versioning information for ancilliary files in prep for CVS.
- Fix docs mixup.

* Tue Mar 27 2001 Lamar Owen <lamar@postgresql.org> <lamar.owen@wgcr.org>
- RC1 quickie.

* Tue Mar 20 2001 Lamar Owen <lamar@postgresql.org>
- Beta 6 initial build.
- Use make install-all-headers instead of cpio hack for devel headers.
- Split out the libs into the libs subpackage.
- Updated initscript to use pg_ctl to stop
- Updated initscript to initdb and start postmaster with LC_ALL=C to 
  prevent index corruption.


* Sun Jan 28 2001 Lamar Owen <lamar@postgresql.org>
- Beta4
- Return to 'pgsql' directories instead of 'postgresql'
- Better perl patches.

* Mon Jan 15 2001 Lamar Owen <lamar@postgresql.org>
- Edit patches to get rid of some cruft.
- Eliminate some more pre-7.1 specfile baggage that is no longer necessary.
- Moved pg_id to server
- added Makefile.global and pg_config to devel
- Corrected /usr/include/pgsql to /usr/include/postgresql
- Fixed some configure options.  Will be trying the configure macro next release
- Since it is terminally ill in this version pg_upgrade is _gone_.
- Thanks to Peter E for a good review.

* Sun Jan 14 2001 Lamar Owen <lamar@postgresql.org>
- Running regression. 1 on the release-o-meter.
- Minor patches to get regression running right.  
- Initscript tweaking -- the old test for a database structure fails with 7.1's new structure.

* Sat Jan 13 2001 Lamar Owen <lamar@postgresql.org>
- Perl 5 needs to be built with PREFIX set on the Makefile, not GNUmakefile....
- The 7.1 build is different from the 7.0 build -- see the configure line.
- NOTE: many files that used to be in /usr/share/postgresql are now in /usr/share/postgresql!
- by request, conditional packages are now supported. See the top of the spec.
- Fixed the server postinstall problems.

* Mon Jan 08 2001 Lamar Owen <lamar@postgresql.org>
- First 7.1 beta test-build







