#!/bin/sh

cat <<END


$0 is obsolete!

Because of the way Maven assemblies are supposed to work, and
the way we used to abuse them, build.sh had become a monstrocity
of special cases.  It has been replaced by two perl scripts
which should work on all supported platforms.

For details on using the new build scripts, see:

  http://www.opennms.org/wiki/Building_OpenNMS




In most cases, you should be able to just run:

  ./compile.pl
  ./assemble.pl

...if you want things to work the way build.sh was most commonly
used previously.

END

exit 1
