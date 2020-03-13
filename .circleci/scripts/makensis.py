#!/usr/bin/env python2
import sys
import re

m = re.search('-XOutFile (.*?) ', ' '.join(sys.argv))
open(m.group(1), "w").close()
