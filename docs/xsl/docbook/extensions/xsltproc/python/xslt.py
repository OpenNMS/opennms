#!/usr/bin/python -u

# THIS IS ALPHA CODE AND MAY NOT WORK CORRECTLY!

import sys
import string
import libxml2
import libxslt
from docbook import adjustColumnWidths

# Check the arguments

usage = "Usage: %s xmlfile.xml xslfile.xsl [outputfile] [param1=val [param2=val]...]" % sys.argv[0]

xmlfile = None
xslfile = None
outfile = "-"
params  = {}

try:
    xmlfile = sys.argv[1]
    xslfile = sys.argv[2]
except IndexError:
    print usage;
    sys.exit(1)

try:
    outfile = sys.argv[3]
    if string.find(outfile, "=") > 0:
        name, value = string.split(outfile, "=", 2);
        params[name] = value

    count = 4;
    while (sys.argv[count]):
        try:
            name, value = string.split(sys.argv[count], "=", 2);
            if params.has_key(name):
                print "Warning: '%s' re-specified; replacing value" % name
            params[name] = value
        except ValueError:
            print "Invalid parameter specification: '" + sys.argv[count] + "'"
            print usage
            sys.exit(1);
        count = count+1;
except IndexError:
    pass

# ======================================================================
# Memory debug specific
# libxml2.debugMemory(1)

# Setup environment
libxml2.lineNumbersDefault(1)
libxml2.substituteEntitiesDefault(1)
libxslt.registerExtModuleFunction("adjustColumnWidths",
                                  "http://nwalsh.com/xslt/ext/xsltproc/python/Table",
                                  adjustColumnWidths)

# Initialize and run
styledoc = libxml2.parseFile(xslfile)
style = libxslt.parseStylesheetDoc(styledoc)
doc = libxml2.parseFile(xmlfile)
result = style.applyStylesheet(doc, params)

# Save the result
style.saveResultToFilename(outfile, result, 0)

# Free things up
style.freeStylesheet()
doc.freeDoc()
result.freeDoc()

# Memory debug specific
#libxslt.cleanup()
#if libxml2.debugMemory(1) != 0:
#    print "Memory leak %d bytes" % (libxml2.debugMemory(1))
#    libxml2.dumpMemory()
