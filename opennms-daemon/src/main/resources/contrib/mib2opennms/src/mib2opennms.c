/*
 * mib2opennms.c
 *
 * Convert SNMP MIB trap descriptons into OpenNMS XML format.
 * 
 * Copyright (c) 2002 Tomas Carlsson <tc@tompa.nu>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id$
 */
#include <stdio.h>
#include <smi.h>
#include <unistd.h> 
#include <string.h>
#include "config.h"

typedef struct EventDefaults {
	char* ueiPrefix;
	char* severity;
} EventDefaults;

int verbosity = 0;
int generic6 = 0;
int wrapevents = 0;

#define verbose(level, ...) \
	if( (level) <= verbosity ) { \
		fprintf(stdout, __VA_ARGS__);}

void dumpOid(SmiNode* node, FILE* file) {
	int j;
	int len = node->oidlen - 2;

	fprintf(file, "\t\t<maskelement>\n");
	fprintf(file, "\t\t\t<mename>id</mename>\n");
	fprintf(file, "\t\t\t<mevalue>");
	for ( j = 0; j < len; j++ ) {
		fprintf(file, ".%d", node->oid[j]);
	}
	fprintf(file, "</mevalue>\n");
	fprintf(file, "\t\t</maskelement>\n");
	fprintf(file, "\t\t<maskelement>\n");
	fprintf(file, "\t\t\t<mename>generic</mename>\n");

	if (generic6) {
		fprintf(file, "\t\t\t<mevalue>6</mevalue>\n");
		node->oid[j++];
	}
	else
		fprintf(file, "\t\t\t<mevalue>%d</mevalue>\n", node->oid[j++]);

	fprintf(file, "\t\t</maskelement>\n");
	fprintf(file, "\t\t<maskelement>\n");
	fprintf(file, "\t\t\t<mename>specific</mename>\n");
	fprintf(file, "\t\t\t<mevalue>%d</mevalue>\n", node->oid[j]);
	fprintf(file, "\t\t</maskelement>\n");
}

int dumpNamed(SmiNode* node, FILE *file) {
	SmiNamedNumber*    smiNamedNumber;
	SmiType*           smiType;
	int	output = 0;

	smiType = smiGetNodeType(node);

	for ( smiNamedNumber = smiGetFirstNamedNumber(smiType);
		smiNamedNumber;
		smiNamedNumber = smiGetNextNamedNumber(smiNamedNumber) )
	{
		fprintf(file, "\n\t\t%s(%d)",
			smiNamedNumber->name,
			smiNamedNumber->value.value.integer32);
		output++;
	}

	if (output)
		fprintf(file, "\n");

	return output;
}

void dumpXml(SmiModule* smiModule, FILE* file, EventDefaults* defs) {
	SmiNode*    smiNode;
	SmiNode*    tmpNode;
	SmiElement* smiElem;
	int         i;
	char*       logmsg;

	smiNode = smiGetFirstNode(smiModule, SMI_NODEKIND_NOTIFICATION);

	fprintf(file, "<!-- Start of auto generated data from MIB: %s -->\n", smiModule->name);
	if (wrapevents) {
		fprintf(file, "<events>\n");
	}

	for(; smiNode; smiNode = smiGetNextNode(smiNode, SMI_NODEKIND_NOTIFICATION) ) 
	{
		fprintf(file, "<event>\n");
		fprintf(file, "\t<mask>\n");

		/*
		* set the OID as mask element
		*/
		dumpOid(smiNode, file);
		fprintf(file, "\t</mask>\n");

		/*
		* Use node name as part of UEI and label
		*/
		fprintf(file, "\t<uei>%s%s</uei>\n", defs->ueiPrefix, smiNode->name);
		fprintf(file, "\t<event-label>%s defined trap event: %s</event-label>\n", 
			smiModule->name, smiNode->name);

		/*
		* The OpenNMS description will contain the MIB description
		* and a list of the trap parameters with values.
		* The params are listed in a html table, hence the pretty '&lt;&gt;':s
		*/
		fprintf(file, "\t<descr>\n&lt;p&gt;%s&lt;/p&gt;", smiNode->description);
		fprintf(file, "&lt;table&gt;");

		logmsg = (char *) malloc( 2000 * sizeof (char));
		logmsg[0]='\0';
		sprintf(logmsg, "\t\t<logmsg dest='logndisplay'>&lt;p&gt;\n\t\t\t%s trap received ", 
			smiNode->name);

		for (smiElem = smiGetFirstElement(smiNode), i=1;
			smiElem;
			smiElem = smiGetNextElement(smiElem), i++)
		{
			tmpNode = smiGetElementNode(smiElem);
			fprintf(file, "\n\t&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n\n\t%s&lt;/b&gt;&lt;/td&gt;", 
				tmpNode->name);
			fprintf(file, "&lt;td&gt;\n\t%%parm[#%d]%%;", i);
			fprintf(file, "&lt;/td&gt;&lt;td&gt;&lt;p&gt;");
			if (dumpNamed(tmpNode, file))		// Values actually printed ??
				fprintf(file, "\t");				// yes - align text
			fprintf(file, "&lt;/p&gt;&lt;/td&gt;");
			fprintf(file, "&lt;/tr&gt;");
			sprintf(logmsg + strlen(logmsg), "\n\t\t\t%s=%%parm[#%d]%% ", 
				tmpNode->name, i);
		}
		sprintf(logmsg + strlen(logmsg) - 1, "&lt;/p&gt;\n\t\t</logmsg>");
		fprintf(file, "&lt;/table&gt;\n");
		fprintf(file, "\t</descr>\n");

		/*
		* The log message will include the trap name
		*/
		fprintf(file, "%s\n", logmsg);
		free(logmsg);

		/*
		* In order to have dynamic severity there must 
		* be support in OpenNMS
		*/
		fprintf(file, "\t<severity>%s</severity>\n", defs->severity);
		fprintf(file, "</event>\n");
	}
  
	if (wrapevents) {
		fprintf(file, "</events>\n");
	}
	fprintf(file, "<!-- End of auto generated data from MIB: %s -->\n", 
		smiModule->name);
}

void usage() {
	fprintf(stderr, 
		"Usage: mib2opennms [-v] [-f file] [-m MIBPATH] [-6] [-w] MIB1 [MIB2 [...]]\n"\
		"       -6 - hardcode generic to 6\n"\
		"       -w - wrap event in <events> tag\n"
);
	exit(1);
}

int main(int argc, char *argv[])
{
	int i;
	int c;
	int moduleCount = 0;

	char* modulename;
	char* filename = NULL;

	char* STANDARD_PATH = ".:/usr/share/snmp/mibs";
	char* mibpath = NULL;
	int pathlen = 0;

	FILE* file = stdout;

	SmiModule* smiModule;
	SmiModule** modules = NULL;

	EventDefaults* defaults = NULL;

	fprintf(stderr, "mib2opennms version %s\n", VERSION);

	while ( (c = getopt(argc, argv, "m:f:v:6w")) != -1 ) {
		switch (c) {
			case 'm' :
				mibpath = optarg;
				break;
			case 'f' :
				filename = optarg;
				break;
			case 'v' :
				verbosity++;
				break;
			case '6' :
				generic6++;
				break;
			case 'w' :
				wrapevents++;
				break;
			default :
				usage();
		}
	}

	if (optind == argc) {
		usage();
	}
    
	smiInit(NULL);

	pathlen = strlen(STANDARD_PATH) + (mibpath == NULL ? 0 : strlen(mibpath)+1 /* for the colon */);

	char * newpath = (char *) malloc( pathlen * sizeof(char) );
	newpath[0] = '\0';

	if (mibpath != NULL) {
	  strcat(newpath, mibpath);
	  strcat(newpath, ":");
	}
  
	strcat(newpath, STANDARD_PATH);

	smiSetPath(newpath);

	modules = (SmiModule **) malloc( argc * sizeof(SmiModule *));
	moduleCount = 0;

	while( optind < argc ) {
		i = optind++;
		verbose(4, "Loading MIB: %s\n", argv[i]);
		modulename = smiLoadModule(argv[i]);
		smiModule = modulename ? smiGetModule(modulename) : NULL;
		if ( ! smiModule ) {
			fprintf(stderr, "mib2opennms: cannot locate module `%s'\n",
			argv[i]);
		} 
		else
		{
			if ((smiModule->conformance) && (smiModule->conformance < 3)) {
				if (verbosity > 0) {
					fprintf(stderr,
						"mib2opennms: '%s' contains errors, output may be flawed\n",
						argv[i]);
				}
			}
			modules[moduleCount++] = smiModule;
			verbose(3, "MIB loaded: %s\n", modulename);
		}
	}      

	if ( filename != NULL ) {
		file = fopen(filename, "w");
		if ( file == NULL ) {
			perror("Could not open file for writing");
			exit(1);
		}
	}

	defaults = (EventDefaults*) malloc(sizeof(struct EventDefaults));
	defaults->ueiPrefix = "uei.opennms.org/mib2opennms/";
	defaults->severity  = "Indeterminate";

	for ( i = 0; i < moduleCount; i++ ) {
		smiModule = modules[i];
		verbose(3, "Dumping %s to file\n", smiModule->name);
		dumpXml(smiModule, file, defaults);
	}
    
	if ( filename != NULL ) {
		fflush(file);
		fclose(file);
	}

	free(defaults);

	exit(0);
}
