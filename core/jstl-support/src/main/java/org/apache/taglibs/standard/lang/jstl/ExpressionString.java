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

import java.util.Map;

/**
 *
 * <p>Represents an expression String consisting of a mixture of
 * Strings and Expressions.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ExpressionString
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property elements

  Object [] mElements;
  public Object [] getElements ()
  { return mElements; }
  public void setElements (Object [] pElements)
  { mElements = pElements; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ExpressionString (Object [] pElements)
  {
    mElements = pElements;
  }

  //-------------------------------------
  /**
   *
   * Evaluates the expression string by evaluating each element,
   * converting it to a String (using toString, or "" for null values)
   * and concatenating the results into a single String.
   **/
  public String evaluate (Object pContext,
			  VariableResolver pResolver,
			  Map functions,
			  String defaultPrefix,
			  Logger pLogger)
    throws ELException
  {
    StringBuffer buf = new StringBuffer ();
    for (int i = 0; i < mElements.length; i++) {
      Object elem = mElements [i];
      if (elem instanceof String) {
	buf.append ((String) elem);
      }
      else if (elem instanceof Expression) {
	Object val = 
	  ((Expression) elem).evaluate (pContext,
					pResolver,
					functions,
					defaultPrefix,
					pLogger);
	if (val != null) {
	  buf.append (val.toString ());
	}
      }
    }
    return buf.toString ();
  }

  //-------------------------------------
  /**
   *
   * Returns the expression in the expression language syntax
   **/
  public String getExpressionString ()
  {
    StringBuffer buf = new StringBuffer ();
    for (int i = 0; i < mElements.length; i++) {
      Object elem = mElements [i];
      if (elem instanceof String) {
	buf.append ((String) elem);
      }
      else if (elem instanceof Expression) {
	buf.append ("${");
	buf.append (((Expression) elem).getExpressionString ());
	buf.append ("}");
      }
    }
    return buf.toString ();
  }

  //-------------------------------------
}
