## README

See https://github.com/gallenc/pgjdbc-ng/tree/pgjdbc-ng-0.6-osgi

pgjdbc-ng version: 0.6.osgi-wrap

Repackaging of pgjdbc-ng 0.6 with correct manifest.

THIS MODULE CAN BE REMOVED WHEN pgjdbc-ng 0.7 release IS RELEASED

### Background

OSGI Manifest Corrections for pgjdbc-ng 0.6

The standard pgjdbc-ng 0.6 release has OSGi metadata using the very 
old styleManifest-Version = 1.0 and does not import javax.sql and org.xml
dependencies correctly and does not work correctly with Apache Karaf.

This issue is reported in issue 250
 https://github.com/impossibl/pgjdbc-ng/issues/250

And patched against release 0.7-SNAPSHOT committed on Mar 16 2016
ttps://github.com/impossibl/pgjdbc-ng/commit/e8e188d98bb1a3cbccfb3d1515b5547f2317c220 

## Solution

This module repacks the standard pgjdbc-ng 0.6 jar with a new manifest created using the 
release 0.7-SNAPSHOT maven modifications.

This module will no longer be required when pgjdbc-ng 0.7 is released.
