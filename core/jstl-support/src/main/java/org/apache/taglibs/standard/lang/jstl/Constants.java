/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Portions Copyright Apache Software Foundation.
 */ 

package org.apache.taglibs.standard.lang.jstl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * <p>This contains all of the non-public constants, including
 * messsage strings read from the resource file.
 *
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 *
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class Constants
{
  //-------------------------------------
  // Resources

  static ResourceBundle sResources =
  ResourceBundle.getBundle ("org.apache.taglibs.standard.lang.jstl.Resources");

  //-------------------------------------
  // Messages from the resource bundle
  //-------------------------------------

  public static final String EXCEPTION_GETTING_BEANINFO =
    getStringResource ("EXCEPTION_GETTING_BEANINFO");

  public static final String NULL_EXPRESSION_STRING =
    getStringResource ("NULL_EXPRESSION_STRING");

  public static final String PARSE_EXCEPTION =
    getStringResource ("PARSE_EXCEPTION");

  public static final String CANT_GET_PROPERTY_OF_NULL =
    getStringResource ("CANT_GET_PROPERTY_OF_NULL");

  public static final String NO_SUCH_PROPERTY =
    getStringResource ("NO_SUCH_PROPERTY");

  public static final String NO_GETTER_METHOD =
    getStringResource ("NO_GETTER_METHOD");

  public static final String ERROR_GETTING_PROPERTY =
    getStringResource ("ERROR_GETTING_PROPERTY");

  public static final String CANT_GET_INDEXED_VALUE_OF_NULL =
    getStringResource ("CANT_GET_INDEXED_VALUE_OF_NULL");

  public static final String CANT_GET_NULL_INDEX =
    getStringResource ("CANT_GET_NULL_INDEX");

  public static final String NULL_INDEX =
    getStringResource ("NULL_INDEX");

  public static final String BAD_INDEX_VALUE =
    getStringResource ("BAD_INDEX_VALUE");

  public static final String EXCEPTION_ACCESSING_LIST =
    getStringResource ("EXCEPTION_ACCESSING_LIST");

  public static final String EXCEPTION_ACCESSING_ARRAY =
    getStringResource ("EXCEPTION_ACCESSING_ARRAY");

  public static final String CANT_FIND_INDEX =
    getStringResource ("CANT_FIND_INDEX");

  public static final String TOSTRING_EXCEPTION =
    getStringResource ("TOSTRING_EXCEPTION");

  public static final String BOOLEAN_TO_NUMBER =
    getStringResource ("BOOLEAN_TO_NUMBER");

  public static final String STRING_TO_NUMBER_EXCEPTION =
    getStringResource ("STRING_TO_NUMBER_EXCEPTION");

  public static final String COERCE_TO_NUMBER =
    getStringResource ("COERCE_TO_NUMBER");

  public static final String BOOLEAN_TO_CHARACTER =
    getStringResource ("BOOLEAN_TO_CHARACTER");

  public static final String EMPTY_STRING_TO_CHARACTER =
    getStringResource ("EMPTY_STRING_TO_CHARACTER");

  public static final String COERCE_TO_CHARACTER =
    getStringResource ("COERCE_TO_CHARACTER");

  public static final String NULL_TO_BOOLEAN =
    getStringResource ("NULL_TO_BOOLEAN");

  public static final String STRING_TO_BOOLEAN =
    getStringResource ("STRING_TO_BOOLEAN");

  public static final String COERCE_TO_BOOLEAN =
    getStringResource ("COERCE_TO_BOOLEAN");

  public static final String COERCE_TO_OBJECT =
    getStringResource ("COERCE_TO_OBJECT");

  public static final String NO_PROPERTY_EDITOR =
    getStringResource ("NO_PROPERTY_EDITOR");

  public static final String PROPERTY_EDITOR_ERROR =
    getStringResource ("PROPERTY_EDITOR_ERROR");

  public static final String ARITH_OP_NULL =
    getStringResource ("ARITH_OP_NULL");

  public static final String ARITH_OP_BAD_TYPE =
    getStringResource ("ARITH_OP_BAD_TYPE");

  public static final String ARITH_ERROR =
    getStringResource ("ARITH_ERROR");

  public static final String ERROR_IN_EQUALS =
    getStringResource ("ERROR_IN_EQUALS");

  public static final String UNARY_OP_BAD_TYPE =
    getStringResource ("UNARY_OP_BAD_TYPE");

  public static final String NAMED_VALUE_NOT_FOUND =
    getStringResource ("NAMED_VALUE_NOT_FOUND");

  public static final String CANT_GET_INDEXED_PROPERTY =
    getStringResource ("CANT_GET_INDEXED_PROPERTY");

  public static final String COMPARABLE_ERROR =
    getStringResource ("COMPARABLE_ERROR");

  public static final String BAD_IMPLICIT_OBJECT =
    getStringResource ("BAD_IMPLICIT_OBJECT");

  public static final String ATTRIBUTE_EVALUATION_EXCEPTION =
    getStringResource ("ATTRIBUTE_EVALUATION_EXCEPTION");

  public static final String ATTRIBUTE_PARSE_EXCEPTION =
    getStringResource ("ATTRIBUTE_PARSE_EXCEPTION");

  public static final String UNKNOWN_FUNCTION =
    getStringResource ("UNKNOWN_FUNCTION");

  public static final String INAPPROPRIATE_FUNCTION_ARG_COUNT =
    getStringResource ("INAPPROPRIATE_FUNCTION_ARG_COUNT");

  public static final String FUNCTION_INVOCATION_ERROR =
    getStringResource ("FUNCTION_INVOCATION_ERROR");


  //-------------------------------------
  // Getting resources
  //-------------------------------------
  /**
   *
   * 
   **/
  public static String getStringResource (String pResourceName)
    throws MissingResourceException
  {
    try {
      String ret = sResources.getString (pResourceName);
      if (ret == null) {
	String str = "ERROR: Unable to load resource " + pResourceName;
	System.err.println (str);
	throw new MissingResourceException 
	  (str, 
	   "org.apache.taglibs.standard.lang.jstl.Constants",
	   pResourceName);
      }
      else {
	return ret;
      }
    }
    catch (MissingResourceException exc) {
      System.err.println ("ERROR: Unable to load resource " +
			  pResourceName +
			  ": " +
			  exc);
      throw exc;
    }
  }

  //-------------------------------------
}
