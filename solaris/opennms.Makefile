#
#   $Id$
#
# Copyright (c) 1999, 2008 Daniel J. Gregor, Jr., All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY DANIEL J. GREGOR, JR. ``AS IS'' AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED.  IN NO EVENT SHALL DANIEL J. GREGOR, JR. BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
# OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# Homepage for OpenNMS:
#	http://www.opennms.org/
#
# You can get the source at:
#	XXX fill this in XXX
#

TOPONMSDIR:sh= cd .. ; pwd
OPENNMSTGZSH = ls ${TOPONMSDIR}/target/opennms-*.tar.gz
OPENNMSTGZ   = ${OPENNMSTGZSH:sh}

#SRCVERSIONSH= echo ${OPENNMSTGZ} | sed -e 's/.*opennms-//' -e 's/\.tar\.gz//'
SRCVERSIONSH= echo ${OPENNMSTGZ} | sed -e 's/.*\///' -e 's/\.tar\.gz//'
SRCVERSIONSHORT  = ${SRCVERSIONSH:sh}
REVISIONSH  = svn info ${TOPONMSDIR} 2> /dev/null | grep Revision: | awk '{ print $$2 }'
REVISION    = ${REVISIONSH:sh}
REVISIONDASH= if [ x"${REVISION}" = x"" -o x"`echo ${SRCVERSIONSHORT} | grep SNAPSHOT`" = x"" ]; then echo ""; else echo "-${REVISION}"; fi
SRCVERSION  = ${SRCVERSIONSHORT}${REVISIONDASH:sh}
BUILDDIR    = untar

DESTDIR     = ${TOPDIR}/pkg 

BASEDIR     = /opt/opennms
#ETCDIR      = /etc/opt/opennms
BASEDIREXISTS=/opt

CONFOPTS    = 

PKGNAME     = opennms
NAME        = OpenNMS
DESC        = Enterprise-grade open source network management system
ARCH        = all
VERSIONSH1  = echo ${SRCVERSION} | sed -e 's/^[^-]*-//' -e 's/^.*\.v//'
VERSION     = ${VERSIONSH1:sh}
CATEGORY    = application
MAXINST     = 1000
VENDOR      = The OpenNMS Group
EMAIL       = dj@opennms.org
CLASSES     = none

PKGPROTO    = /usr/bin/pkgproto
PKGMK       = /usr/bin/pkgmk
PKGTRANS    = /usr/bin/pkgtrans

INSTUSER    = opennms
INSTGROUP   = opennms

TOPDIR:sh   = pwd

SPOOLDIR    = /var/spool/pkg

OTHERFILES  = ${DESTDIR}${BASEDIR}/docs/COPYING \
		${DESTDIR}${BASEDIR}/contrib/svc-opennms \
		${DESTDIR}${BASEDIR}/contrib/smf-manifest.xml
INSTALLFILES= ${DESTDIR}/install/pkginfo \
		${DESTDIR}/install/copyright \
		${DESTDIR}/install/depend \
		${DESTDIR}/install/preinstall \
		${DESTDIR}/install/postinstall \
		${DESTDIR}/install/preremove \
		${DESTDIR}/install/checkinstall

all: package

package: check pkg

check::
	@echo "Source version: ${SRCVERSION}"
	test -f ${OPENNMSTGZ}

pkg: ${SRCVERSION}.pkg

${SRCVERSION}.pkg:  ${DESTDIR}/prototype
	( cd ${DESTDIR} && ${PKGMK} -d ${SPOOLDIR} -or . )
## This doesn't seem to work reliably, anymore. :-(
#	( cd ${SPOOLDIR}/${PKGNAME}/reloc && find . -depth -print | \
#		grep -v '^\.$$' | cpio -odm | compress -f > ../reloc.cpio.Z )
#	rm -rf ${SPOOLDIR}/${PKGNAME}/reloc
	$(PKGTRANS) ${SPOOLDIR} ${TOPDIR}/${SRCVERSION}.pkg ${PKGNAME}

${DESTDIR}/prototype: .package-installed.${SRCVERSION} ${INSTALLFILES}
	@( cd ${DESTDIR} ; \
	 find . -print | \
		$(PKGPROTO) | \
		nawk -v instuser="${INSTUSER}" \
		     -v instgroup="${INSTGROUP}" \
		     -v basedirexists="${BASEDIREXISTS}" \
		'BEGIN { \
			b = split(basedirexists, basedirs); \
		} \
		{ \
			if ( match($$3, "^prototype$$") ) { \
				next; \
 			} \
			if ( match($$3, "^install$$") ) { \
				next; \
 			} \
			if ( match($$3, "^install/") ) { \
			 	base = $$3; \
			 	sub("^.*/", "", base); \
			 	print "i", base "=" $$3; \
				next; \
 			} \
			for ( i = 1; i <= b; i++ ) { \
				checkbasedir = basedirs[i]; \
				sub("^/", "", checkbasedir); \
				while ( checkbasedir != "" ) { \
					if ( match($$3, "^"checkbasedir"$$") ) { \
						sub("[^/]*$$", "", checkbasedir); \
						sub("/$$", "", checkbasedir); \
						print $$1, $$2, $$3, "?", "?", "?"; \
						next; \
	 				} \
					sub("[^/]*$$", "", checkbasedir); \
					sub("/$$", "", checkbasedir); \
				} \
			} \
			\
			print $$1, $$2, $$3, $$4, instuser, instgroup; \
		}' \
	 ) | \
	sed '/ opt /d;/ etc /d;/ etc\/opt /d' | \
	sed 's!\(opt/FSFsudo/bin/sudo\) [0-9][0-9]*!\1 4111!' | \
	sed 's!\(opt/FSFsudo/sbin/visudo\) [0-9][0-9]*!\1 4111!' \
	> ${DESTDIR}/prototype

${DESTDIR}/install: ${DESTDIR}
	mkdir $@

${DESTDIR}/install/pkginfo: ${DESTDIR}/install 
	rm -f $@
	@echo "PKG=\"${PKGNAME}\"" >> $@
	@echo "NAME=\"${NAME}\"" >> $@
	@echo "DESC=\"${DESC}\"" >> $@
	@echo "ARCH=\"${ARCH}\"" >> $@
	@echo "MAXINST=\"${MAXINST}\"" >> $@
	@echo "VERSION=\"${VERSION}\"" >> $@
	@echo "CATEGORY=\"${CATEGORY}\"" >> $@
	@echo "VENDOR=\"${VENDOR}\"" >> $@
	@echo "EMAIL=\"${EMAIL}\"" >> $@
	@echo "BASEDIR=\"/\"" >> $@
	@echo "CLASSES=\"${CLASSES}\"" >> $@

${DESTDIR}/install/depend: ${DESTDIR}/install install/depend
	cp install/depend $@

${DESTDIR}/install/copyright: ${DESTDIR}/install install/copyright
	cp install/copyright $@

${DESTDIR}/install/checkinstall: ${DESTDIR}/install install/checkinstall
	cp install/checkinstall $@

${DESTDIR}/install/preinstall: ${DESTDIR}/install install/preinstall
	cp install/preinstall $@

${DESTDIR}/install/postinstall: ${DESTDIR}/install install/postinstall
	cp install/postinstall $@

${DESTDIR}/install/preremove: ${DESTDIR}/install install/preremove
	cp install/preremove $@

${DESTDIR}/install/postremove: ${DESTDIR}/install install/postremove
	cp install/postremove $@

.package-installed.${SRCVERSION}: build clean.${DESTDIR} ${DESTDIR}${BASEDIR} \
		${OTHERFILES} ${OPENNMSTGZ}
#	cd ${BUILDDIR} ; ${MAKE} prefix=${DESTDIR}${BASEDIR} \
#		sysconfdir=${DESTDIR}${ETCDIR} \
#		sbindir=${DESTDIR}${BASEDIR}/sbin \
#		INSTALL=${TOPDIR}/deejinstall \
#		install_uid=root install_gid=root \
#		install
#	-( cd ${DESTDIR}/${BASEDIR}/bin ; strip * )
	cd ${DESTDIR}${BASEDIR} && gzip -cd ${OPENNMSTGZ} | gtar xf -
	chmod 755 ${DESTDIR}${BASEDIR}/bin/*
	find ${DESTDIR}${BASEDIR}/logs -name .readme | xargs rm
	find ${DESTDIR}${BASEDIR}/share -name .readme | xargs rm
	rm -rf ${DESTDIR}${BASEDIR}/webapps
	/usr/bin/echo "/^RUNAS=/\\ns/.*/RUNAS=\\\"${INSTUSER}\\\"/\\nw\\nq" | \
		ed ${DESTDIR}${BASEDIR}/bin/opennms
	mkdir ${DESTDIR}${BASEDIR}/etc/.dist
	mv `find ${DESTDIR}${BASEDIR}/etc/* -prune -type f` ${DESTDIR}${BASEDIR}/etc/.dist/.
	touch $@

# XXX it should be under BUILDDIR somewhere
${DESTDIR}${BASEDIR}/docs/COPYING: ${DESTDIR}${BASEDIR}/docs
	cp ${TOPONMSDIR}/COYPING $@

${DESTDIR}${BASEDIR}/contrib/svc-opennms: ${DESTDIR}${BASEDIR}/contrib svc-opennms
	cp svc-opennms $@

${DESTDIR}${BASEDIR}/contrib/smf-manifest.xml: ${DESTDIR}${BASEDIR}/contrib smf-manifest.xml
	cp smf-manifest.xml $@

${DESTDIR}${BASEDIR}/contrib: ${DESTDIR}${BASEDIR}
	mkdir $@

clean.${DESTDIR}:
	rm -rf ${DESTDIR}

${DESTDIR}${BASEDIR}:
	mkdir -p ${DESTDIR}${BASEDIR}

build: .configured.${SRCVERSION}
	true

clean:
	rm -rf ${BUILDDIR} ${DESTDIR} .configured.${SRCVERSION} \
		.untarred.${SRCVERSION} .package-installed.${SRCVERSION} \
		${SPOOLDIR}/${PKGNAME} ${SRCVERSION}.pkg

.untarred.${SRCVERSION}: ${OPENNMSTGZ}
#	rm -rf ${BUILDDIR}
#	mkdir -p ${BUILDDIR}
#	cd ${BUILDDIR} && gzip -cd $? | gtar xvf -
	touch $@

.configured.${SRCVERSION}: .untarred.${SRCVERSION}
#	cd ${BUILDDIR} ; ./configure --prefix=${BASEDIR} \
#		--sysconfdir=${ETCDIR} --sbindir=${BASEDIR}/sbin
#		${CONFOPTS} 
	touch $@

veryclean:
	rm -rf ${BUILDDIR} ${DESTDIR} .configured.${SRCVERSION} \
		.untarred.${SRCVERSION} .package-installed.${SRCVERSION} \
		.revision ${SPOOLDIR}/${PKGNAME} ${SRCVERSION}.pkg

${DESTDIR}${BASEDIR}/docs: ${DESTDIR}${BASEDIR}
	mkdir -p ${DESTDIR}${BASEDIR}/docs 

