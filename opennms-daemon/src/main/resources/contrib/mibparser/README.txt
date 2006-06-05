John E. Rodriguez -- snmpfactotum@yahoo.com

The MIB parser is built with JavaCC, javac, java
The parser uses standard compiler design. The few unique things that I have
done in the compiler probably fall under the area of "syntax directed 
translation using synthesized and inherited attributes" from "Compilers, 
Priciples, Techniques and Tools" by Aho, Sethi and Ullman which is the 
landmark text for compiler design.

The building and running of the parser is done with Ant.

Set your ANT_HOME and JAVA_HOME appropriately.

You could run without Ant but why would you want to?

If you choose to run without Ant then shell scripts have to be written
for your target. 
Just look at what is done in build.xml (ant -debug test).
For example, the command to parse 2 MIBS would be something like:
java -classpath .:javacc.jar:mibparser.jar ParseMib RFC1213-MIB RFC1229-MIB


Directories:
------------
./ contains this README, build.xml
./build/ the class files are put here during build
./dist/ the mibparser.jar made from the class files is put here
./log/ output of building or tests are put here
./lib/ the javacc.jar is the parser generator from Sun Microsystems
./mibs/ MIBS to test with
./src/ the java source files and the grammar files (jjt, jj) are put here

Building:
---------
Optional, only do if you change the grammar.
  ant -DJJTREE_EXE="/javacc-3.2/bin/jjtree" runjjtree
  ant -DJAVACC_EXE="/javacc-3.2/bin/javacc" runjavacc
Compile, only if the grammar or the java source changed
  ant compile
Jar
  ant jar

Running tests:
--------------
This parser has successflly run all MIBS listed in all.sh
(See the exceptions noted below)

There is a set of MIBS for testing purposes supplied with the parser 
distribution.

Run a test from mibs in the ./mibs dir (hardcoded for RFC1213-MIB, RFC1253-MIB)
  ant test
  
Test any MIB you have by:
  ant -DMIBS="FIRST-MIB-NAME SECOND-MIB-NAME" testmibs
  
For lots of debug or to see the Abstract Syntax Tree structure use:
  ant -DFLAGS=-debug test
  ant -DFLAGS=-debug -DMIBS="FIRST-MIB-NAME SECOND-MIB-NAME" testmibs
  
If you are running tests of your own MIBS then the MIBS that are imported
must come first. This is because the OIDS must be built up from OIDS that
are inherited from.

However, sometimes you can get away with only including some of the imports.
For example, RFC1289-MIB imports RFC1155-SMI, RFC-1212 and RFC1213-MIB but 
needs only:
  ant -DFLAGS=-debug -DMIBS="RFC1213-MIB RFC1289-MIB" testmibs
In fact most MIBS in the mibs dir can be compiled just using RFC1213-MIB.

If the MIBS have followed the naming conventions described in "Understanding
SNMP MIBS" by Perkins and McGinnis, then you will usually not need to include
MIBS with names ending in "SMI". These MIBS may have constructs not handled
yet by the parser and further, are not usually necessary for the output XML.

The parser does not currently support MACRO. This is because the contents
of a MACRO does not supply important data that would go in the XML.

Note that the MIB parser does not write XML for uninteresting types like a 
String (can't graph a string).

Error Codes.
------------
1 means some unexpected exception.
2 means that you need to include a MIB that is in the imports.
3 means a file was not found on disk or is mis-spelled

Tools used.
-----------
javacc-3.2
jdk1.3.0_02
apache-ant-1.5.4

If you find problems, please let me know. Email me
    1) the MIBS
    2) parser version ("java ParseMib" with no arguments)
    3) short description of the problem
at:
snmpfactotum@yahoo.com    John Rodriguez 
