/*
 This file is part of the OpenNMS(R) Application.

 OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 OpenNMS(R) is a derivative work, containing both original code, included code and modified
 code that was published under the GNU General Public License. Copyrights for modified 
 and included code are below.

 OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

 For more information contact: 
      OpenNMS Licensing       <license@opennms.org>
      http://www.opennms.org/
      http://www.opennms.com/
*/

/*****************************************************************************
   jRRD - Java Interface to Tobias Oetiker's RRDtool

   RRDtool 1.0.28  Copyright Tobias Oetiker, 1997 - 2000
 
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 *****************************************************************************
 * rrd_jinterface.c  Implementation of Java native method for calling
 *                   RRD functions (rrd_create(), rrd_update(), etc...)
 *                   from within a Java application.
 * 
 *                   Base code taken and modified from rrd_tool.c from 
 *                   RRDtool 1.0.28 distribution.
 *
 * rrd_jinterface.c,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
 *****************************************************************************/
#include <jni.h>
#include <unistd.h>
#if defined(__APPLE_CC__) || defined(__bsdi__) || defined(__FreeBSD__)
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include <string.h>
#include <stdlib.h>

#include "rrd.h"
#if 0
#pragma export on
#endif
#include "rrd_jinterface.h"
#if 0
#pragma export reset
#endif
#if !defined(__SOLARIS__) && !defined(__APPLE_CC__) && !defined(__bsdi__) && !defined(__FreeBSD__)
#include "getopt.h"
#endif

#define FALSE 0
#define TRUE 1

#undef DEBUG /* Enable/Disable debugging */

/*---------------------------------------------------------------------------
  *
  * Description:
  *	Parses provided command string and determines total number of
  *     arguments.  Assumes that argument is space-delimited.  Exception
  *     to this rule is made for double quotes.  When a double quote is
  *     found all chars up to the next double quote will be kept together
  *     as a single argument.  This was done to allow for filenames with
  *     spaces.
  *
  * Args:
  *		command		Command to parse.
  * 
  * Return:
  *		int	    	Argument count
  * 
  ---------------------------------------------------------------------------*/
static
int getArgumentCount(char* command) {
	int dquoted = FALSE;
	int count = 0;
	int ii = 0;
	
	for (ii=0; ii<strlen(command)+1; ii++) {
		if (command[ii] == '\"') {
			dquoted = dquoted ? FALSE : TRUE;
		}
		else if(dquoted)
		{
			continue;
		}
		else if(command[ii] == ' ')
		{
			count++;
			
			// trim off the remaining white space
			//
			while(command[ii+1] == ' ')
				ii++;
		}
		else if (command[ii] == '\0')
		{
			count++;
		}
	}
	
	return count;
}

/*---------------------------------------------------------------------------
  *
  * Description:
  *	Parses provided command string and builds an array of strings
  *     consisting of each of the individual arguments which make up the 
  *     command.  Assumes that each argument is space-delimited.  Exception
  *     to this rule is made for double quotes.  When a double quote is
  *     found all chars up to the next double quote will be kept together
  *     as a single argument.  This was done to allow for filenames with
  *     spaces.
  *
  * Args:
  *		char *command		Command to parse.
  * 		char **argv		Array of character pointers which
  *                                       will point to each parsed argument.
  * 
  * Return:
  *		int	    	Argument count
  * 
  ---------------------------------------------------------------------------*/
static
int buildArgList(char* command, char **argv) {
	int dquoted = FALSE;
	int count = 0;
	char *token = NULL;
	int  ii = 0;
	int  x = 0;
	char *buffer = (char *)malloc(strlen(command)+1);
	
	// get working copy of the command string
	strcpy(buffer, command);

#ifdef DEBUG
	printf("buildArgList(): command: '%s'\n", command);
#endif

	// iterate over the command string and parse out each
	// argument/token
	for (ii=0; ii<strlen(command)+1; ii++) {
#ifdef DEBUG
		printf("processing char: '%c'\n", command[ii]);
#endif
		if (command[ii] == '\"') {
#ifdef DEBUG
			printf("hit double quote.\n");
#endif
			dquoted = dquoted ? FALSE : TRUE;
		}
		else if(dquoted)
		{
#ifdef DEBUG
			printf("double quote is true.\n");
#endif
			buffer[x++] = command[ii];
		}
		else if(command[ii] == ' ')
		{
#ifdef DEBUG
			printf("hit space, creating token.\n");
#endif
			
			// null terminate string
			buffer[x] = '\0';
#ifdef DEBUG
			printf("buffer=%s\n", buffer);
#endif
			
			// allocate storage for new token string
			token = (char *)malloc(strlen(buffer)+1);
	
			strcpy(token, buffer);
#ifdef DEBUG
			printf("token: %s\n", token);
#endif
			
			// Assign next argv pointer to the token
			argv[count] = token;
			
			// increment arg count
			count++;
			
			// trim off the remaining white space
			//
			while(command[ii+1] == ' ')
				ii++;
				
			// reset buffer index
			x = 0;
		}
		else if (command[ii] == '\0')
		{
#ifdef DEBUG
			printf("hit null, creating token.\n");
#endif
			// terminate buffer
			buffer[x] = '\0';
			
			/// allocate storage for new token string
			token = (char *)malloc(strlen(buffer)+1);
	
			strcpy(token, buffer);
#ifdef DEBUG
			printf("token: %s\n", token);
#endif
			
			// Assign next argv pointer to the token
			argv[count] = token;
			
			// increment arg count
			count++;
		}
		else
		{
#ifdef DEBUG
			printf("normal char.\n");
#endif
			buffer[x++] = command[ii];
		}
	}
	
	free(buffer);
	return count;
}

 /*---------------------------------------------------------------------------
  * JNI Function (as defined in machine-generated file 'rrd_jinterface.h')
  *		Java_org_opennms_netmgt_rrd_rrdtool_Interface_launch() 
  * 
  * Description:
  *	JNI method implementation which takes a pre-formatted RRD command
  *     string, builds an argument list array of strings and calls the 
  *     appropriate RRD function with the arg list.  Does exactly what
  *     RRDtool.exe does only from within a Java application as opposed
  *     to the command line.  
  * 
  *		Sample command string:
  *			"fetch test.rrd AVERAGE --start N --end N"
  * 
  *     The above command would cause rrd_fetch() to be called with an argument
  *     list consisting of the tokenized elements of the command string.
  *     
  *     Please refer to the appropriate RRDtool documentation for additional
  *     information on the various RRDtool commands and their arguments.
  *
  * Supported RRDtool Functions:
  *    rrd_create()
  *    rrd_update()
  *    rrd_fetch()
  *
  * Args:
  *		*env	Java execution environment reference
  *		obj		Reference to the org.opennms.netmgt.rrd.rrdtool.Interface class
  *		javaCmdStr	RRDtool style command string
  * 
  * Return:
  *		jobjectArray    Array of Java String objects where:
  * 				  array[0] - NULL if successful or error text if failure.
  * 				  array[1] - Space delimited list of data source names
  * 						contained in the RRD file.
  * 				  array[2]..array[n] - The "fetched" data.  Refer to 
  * 						the appropriate RRDtool documentation for 
  * 						the format of the output from the RRDtool 
  * 						fetch command.
  * 				Will return NULL if memory allocation fails
  * 
  ---------------------------------------------------------------------------*/
JNIEXPORT jobjectArray JNICALL
Java_org_opennms_netmgt_rrd_rrdtool_Interface_launch(JNIEnv *env, jobject obj, jstring javaCmdStr) {

	/* Local vars */
	char *nativeCmdStr;
	char **argv;
	int  argc=0;
	int ii,jj;
	int len;

	jstring utf_str;
	jclass  clazz;
	jobjectArray resultsArray;
	int bytesWritten;
	jclass	clazzOutOfMem;

	/* initialize args */
	argv = NULL;

	/* find out of memory error just in case */
	clazzOutOfMem = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
	if(clazzOutOfMem == NULL || (*env)->ExceptionOccurred(env) != NULL)
		return NULL;

	/* get length of command string */
	len = (*env)->GetStringUTFLength(env, javaCmdStr);
	if((*env)->ExceptionOccurred(env) != NULL)
		return NULL;

	/* allocate memory to hold native command string */
	nativeCmdStr = (char *)malloc((len+1) * sizeof(char));
	if (nativeCmdStr == NULL)
	{
		(*env)->ThrowNew(env, clazzOutOfMem, "Failed to allocate native command string");
		return NULL;
	}

	/* retrieve command from jstring argument 'javaCmdStr' */
	(*env)->GetStringUTFRegion(env, javaCmdStr, 0, len, nativeCmdStr);
	if((*env)->ExceptionOccurred(env) != NULL)
	{
		free(nativeCmdStr);
		return NULL;
	}

#ifdef DEBUG
	printf("Interface_launch(): Command Text: %s\n", nativeCmdStr);
#endif

	/*---------------------------------------------------------*/
	/* Get argument count...this number will be used to        */
	/* allocate memory to point to each token.                 */
	/*---------------------------------------------------------*/
	argc = getArgumentCount(nativeCmdStr);
#ifdef DEBUG
	printf("Interface_launch(): argument count: %i\n", argc);
#endif
	
	/* Now we have argument count so lets allocate the array of */
	/* character pointers which will point to each arg token */
	argv = (char **)calloc(argc, sizeof(char *));
	if (argv == NULL)
	{
		free(nativeCmdStr);
		(*env)->ThrowNew(env, clazzOutOfMem, "Failed to allocate argv array");
		return NULL;
	}
    
	/* Reset nativeCmdStr with the original command string */
	(*env)->GetStringUTFRegion(env, javaCmdStr, 0, len, nativeCmdStr);
	if((*env)->ExceptionOccurred(env) != NULL)
	{
		free(nativeCmdStr);
		free(argv);
		return NULL;
	}

#ifdef DEBUG
	printf("Interface_launch(): Command Text: %s\n", nativeCmdStr);
#endif

	/*-----------------------------------------------------------*/
	/* Call buildArgList() to parse command string into an array */
	/* of character string args which can then be passed on to   */
	/* the appropriate rrd function.                             */
	/*-----------------------------------------------------------*/
	buildArgList(nativeCmdStr, argv);

	/*-------------------------------------------------------------*/
	/* Reset Getopt() 'optind' variable to 0.                      */
    	/*                                                             */
	/* RRD functions use getopt to parse arguments. Getopt uses a  */
	/* static int named "optind" to keep track which argument is   */
	/* checking as an option. We must RESET "optind" by setting it */
	/* to 0 before calling any of the rrd_* functions.             */
	/*-------------------------------------------------------------*/
	optind = 0;

	/* Get reference to the java String class */
	clazz = (*env)->FindClass(env, "java/lang/String");
	if((*env)->ExceptionOccurred(env) != NULL)
	{
		free(nativeCmdStr);
		for (ii=0; ii<argc; ii++)
			free(argv[ii]);
		free(argv);
		return NULL;
	}
	
	/* Call appropriate rrd_* function based on command */
	if (strcmp("create", argv[0]) == 0) 
	{
		if (rrd_create(argc, &argv[0]) != -1) {
			/* Command succeeded, iniitalize string array */
			resultsArray = (*env)->NewObjectArray(env, 1, clazz, NULL);
			if (resultsArray == NULL || (*env)->ExceptionOccurred(env) != NULL)
			{
				free(nativeCmdStr);
				for (ii=0; ii<argc; ii++)
					free(argv[ii]);
				free(argv);
				return NULL;
			}
		}
	}
    	else if (strcmp("update", argv[0]) == 0) 
	{
		/*rrd_ts_update(argc, &argv[0]);*/    /* Thread-safe version...doesn't use getopt() */
		if (rrd_update(argc, &argv[0]) != -1) {
			/* Command succeeded, iniitalize string array */
			resultsArray = (*env)->NewObjectArray(env, 1, clazz, NULL);
			if (resultsArray == NULL || (*env)->ExceptionOccurred(env) != NULL)
			{
				free(nativeCmdStr);
				for (ii=0; ii<argc; ii++)
					free(argv[ii]);
				free(argv);
				return NULL;
			}
		}
	}
	else if (strcmp("fetch", argv[0]) == 0) 
	{
		time_t        start,end;
		unsigned long step, ds_cnt;
		rrd_value_t   *data,*datai;
		char          **ds_namv;
		
  		int index;
		char buf[2000];
		int rowCount;
		int totalElements;
		
		if (rrd_fetch(argc, &argv[0],&start,&end,&step,&ds_cnt,&ds_namv,&data) != -1)
		{
			/* success! */
			datai = data;

#ifdef DEBUG
			printf("Interface_launch(): start: %ld end: %ld step: %ld ds_cnt: %ld\n", 
				start, end, step, ds_cnt);
#endif

			/*-------------------------------------------------------------*/
			/* Allocate array of jstring objects for storing fetched data  */
			/* First jstring in array will hold any error information      */
			/* or NULL if there was no error          	               */
			/* Second jstring in array will hold all the data source names */
			/* All remaining jstring in array will hold the fetched data   */
			/*							       */
			/*   ex. array[0] = NULL	 		  	       */
			/*     . array[1] = "ifOctetsIn ifOctetsOut"   		       */
			/*       array[1] = "8457488000 5000.0 2500.0" 		       */
			/*       array[2] = "8457488500 6000.0 3000.0" 		       */
			/*       etc...				 		       */
			/*-------------------------------------------------------------*/
		
			/* Determine total number of array elements required to hold the 
			   fetched data */
			for (ii=start, rowCount=0; ii<=end; ii+=step, rowCount++);
				
#ifdef DEBUG
			printf("Interface_launch(): rowCount: %d\n", rowCount);
#endif

			totalElements = rowCount + 2;
			
			resultsArray = (*env)->NewObjectArray(env, totalElements, clazz, NULL);
			if (resultsArray == NULL || (*env)->ExceptionOccurred(env) != NULL)
			{
				/* Free up the stuff we allocated */
				free(nativeCmdStr);
				for (ii=0; ii<argc; ii++)
					free(argv[ii]);
				free(argv);

				/* free up rrd allocated memory */
				for (ii=0;ii<ds_cnt;ii++)
					free(ds_namv[ii]);
				
		   		free(ds_namv);
		    		free (data);

				return NULL;
			}
			
			/* Add Data Source Names string */
			index = 0;
			for (ii=0; ii<ds_cnt; ii++)
			{
				sprintf(&buf[index], "%s ",ds_namv[ii]);
				index = index + strlen(ds_namv[ii]) + 1;
			}
#ifdef DEBUG
			printf("Interface_launch(): data source names string: %s\n", buf);
#endif
				
			utf_str = (*env)->NewStringUTF(env, buf);
			(*env)->SetObjectArrayElement(env, resultsArray, 1, utf_str);
			(*env)->DeleteLocalRef(env, utf_str);
			if((*env)->ExceptionOccurred(env) != NULL)
			{
				/* Free up the stuff we allocated */
				free(nativeCmdStr);
				for (ii=0; ii<argc; ii++)
					free(argv[ii]);
				free(argv);

				/* free up rrd allocated memory */
				for (ii=0;ii<ds_cnt;ii++)
					free(ds_namv[ii]);
				
		   		free(ds_namv);
		    		free (data);

				return NULL;
			}
			
			/* Add Fetch rows */
			for (ii = start; ii <= end; ii += step) 
			{
				jstring utf_str;
		        	sprintf(buf, "%10u:", ii);
				for (bytesWritten=0, jj = 0; jj < ds_cnt; jj++) 
				{
				    	bytesWritten += sprintf(&buf[bytesWritten], "%0.10e ", *(datai++));
				}

#ifdef DEBUG
				printf("Interface_launch(): fetch row(%d): -%s-\n", ii, buf);
#endif

				utf_str = (*env)->NewStringUTF(env, buf);
				(*env)->SetObjectArrayElement(env, resultsArray, ((ii-start)/step) + 2, utf_str);
				(*env)->DeleteLocalRef(env, utf_str);
				if((*env)->ExceptionOccurred(env) != NULL)
				{
					/* Free up the stuff we allocated */
					free(nativeCmdStr);
					for (ii=0; ii<argc; ii++)
						free(argv[ii]);
					free(argv);

					/* free up rrd allocated memory */
					for (ii=0;ii<ds_cnt;ii++)
						free(ds_namv[ii]);
				
			   		free(ds_namv);
			    		free (data);

					return NULL;
				}
			}
			
			/* free up RRD allocations */
			for (ii=0;ii<ds_cnt;ii++)
				free(ds_namv[ii]);
				
		   	free(ds_namv);
		    	free (data);
		}
    	} 
	else 
	{
		/* Unsupported/unknown functon error */
		rrd_set_error("unknown function '%s'",argv[0]);
    	}

	/* Free allocated memory */
	free(nativeCmdStr);
	for (ii=0; ii<argc; ii++)
		free(argv[ii]);
	free(argv);

	/* Check for RRD error */
    	if (rrd_test_error()) 
	{
		resultsArray = (*env)->NewObjectArray(env, 1, clazz, NULL);
		if (resultsArray == NULL || (*env)->ExceptionOccurred(env) != NULL)
			return NULL;
		
		/* Add error string */
		utf_str = (*env)->NewStringUTF(env, rrd_get_error());
		(*env)->SetObjectArrayElement(env, resultsArray, 0, utf_str);
		(*env)->DeleteLocalRef(env, utf_str);

		rrd_clear_error();

		if((*env)->ExceptionOccurred(env) != NULL)
			return NULL;
    	}
#ifdef DEBUG
	else
	{
		printf("Interface_launch(): success!!!\n");
	
		// Dump content of jstring array
		len = (*env)->GetArrayLength(env, resultsArray);
		printf("resultsArray length: %d\n", len);

		for (ii=0; ii<len; ii++) 
		{
			jboolean isCopy;
			jstring	j_str;
			const char *c_str;
	 		j_str = (jstring)(*env)->GetObjectArrayElement(env, resultsArray, ii);
			if (j_str == NULL) 
			{
				printf("elem[%d] =  NULL\n", ii);
				continue;
			}
			c_str = (*env)->GetStringUTFChars(env, j_str, &isCopy);
			printf("elem[%d] = %s\n", ii, c_str);
			if (isCopy == JNI_TRUE)
				(*env)->ReleaseStringUTFChars(env, j_str, c_str);
			(*env)->DeleteLocalRef(env, j_str);
	     	}
	}
#endif
	
	return resultsArray;
}
