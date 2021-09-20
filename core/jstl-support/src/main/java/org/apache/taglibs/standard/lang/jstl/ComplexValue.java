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

import java.util.List;
import java.util.Map;

/**
 *
 * <p>Represents a dynamic value, which consists of a prefix and an
 * optional set of ValueSuffix elements.  A prefix is something like
 * an identifier, and a suffix is something like a "property of" or
 * "indexed element of" operator.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ComplexValue
  extends Expression
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property prefix

  Expression mPrefix;
  public Expression getPrefix ()
  { return mPrefix; }
  public void setPrefix (Expression pPrefix)
  { mPrefix = pPrefix; }

  //-------------------------------------
  // property suffixes

  List mSuffixes;
  public List getSuffixes ()
  { return mSuffixes; }
  public void setSuffixes (List pSuffixes)
  { mSuffixes = pSuffixes; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ComplexValue (Expression pPrefix,
		       List pSuffixes)
  {
    mPrefix = pPrefix;
    mSuffixes = pSuffixes;
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the expression in the expression language syntax
   **/
  public String getExpressionString ()
  {
    StringBuffer buf = new StringBuffer ();
    buf.append (mPrefix.getExpressionString ());

    for (int i = 0; mSuffixes != null && i < mSuffixes.size (); i++) {
      ValueSuffix suffix = (ValueSuffix) mSuffixes.get (i);
      buf.append (suffix.getExpressionString ());
    }

    return buf.toString ();
  }

  //-------------------------------------
  /**
   *
   * Evaluates by evaluating the prefix, then applying the suffixes
   **/
  public Object evaluate (Object pContext,
			  VariableResolver pResolver,
			  Map functions,
			  String defaultPrefix,
			  Logger pLogger)
    throws ELException
  {
    Object ret = mPrefix.evaluate (pContext, pResolver, functions,
				   defaultPrefix, pLogger);

    // Apply the suffixes
    for (int i = 0; mSuffixes != null && i < mSuffixes.size (); i++) {
      ValueSuffix suffix = (ValueSuffix) mSuffixes.get (i);
      ret = suffix.evaluate (ret, pContext, pResolver, functions,
			     defaultPrefix, pLogger);
    }

    return ret;
  }

  //-------------------------------------
}
