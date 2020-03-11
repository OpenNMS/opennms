#!/usr/bin/env python
import sys
import re

m = re.search('-XOutFile (.*?) ', ' '.join(sys.argv))
open(m.group(1), "w").close()
