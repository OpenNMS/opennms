#!/bin/sh

M2E_SITE=`pwd`
SITE_MERGE=${M2E_SITE}/site_merge.rb
TARGET_PLATFORM=/home/j2ee-hudson/eclipse-tp-e33/eclipse
ECLIPSE=${TARGET_PLATFORM}/eclipse
ECLIPSE=/opt/eclipse-3.5/eclipse

# The update site which contains all releases of m2eclipse
UPDATE_SITE_MAIN=${M2E_SITE}/update

# The update site that was generated for the current release
UPDATE_SITE_RELEASE=${M2E_SITE}/update-dev

# Copy the features for the new release to the release update site
cp ${UPDATE_SITE_RELEASE}/features/* ${UPDATE_SITE_MAIN}/features

# Copy the plugins for the new release to the release update site
cp ${UPDATE_SITE_RELEASE}/plugins/* ${UPDATE_SITE_MAIN}/plugins

# Add new features to the site.xml
${SITE_MERGE} ${UPDATE_SITE_MAIN}/site.xml ${UPDATE_SITE_RELEASE}/site.xml > ${M2E_SITE}/site.xml
mv ${M2E_SITE}/site.xml ${UPDATE_SITE_MAIN}/site.xml

# Generate P2 Metadata
rm -f ${UPDATE_SITE_MAIN}/artifacts.xml ${UPDATE_SITE_MAIN}/content.xml
${ECLIPSE} \
  -nosplash \
  -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator \
  -updateSite ${UPDATE_SITE_MAIN} \
  -site file:${UPDATE_SITE_MAIN}/site.xml \
  -metadataRepository file:${UPDATE_SITE_MAIN} \
  -metadataRepositoryName "Maven Integration for Eclipse Update Site" \
  -artifactRepository file:${UPDATE_SITE_MAIN} \
  -artifactRepositoryName "Maven Integration for Eclipse Artifacts" \
  -noDefaultIUs -vmargs -Xmx1024m
