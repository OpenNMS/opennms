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

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 *
 * <p>The implementation of the empty operator
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class EmptyOperator
  extends UnaryOperator
{
  //-------------------------------------
  // Singleton
  //-------------------------------------

  public static final EmptyOperator SINGLETON =
    new EmptyOperator ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public EmptyOperator ()
  {
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the symbol representing the operator
   **/
  public String getOperatorSymbol ()
  {
    return "empty";
  }

  //-------------------------------------
  /**
   *
   * Applies the operator to the given value
   **/
  public Object apply (Object pValue,
		       Object pContext,
		       Logger pLogger)
    throws ELException
  {
    // See if the value is null
    if (pValue == null) {
      return PrimitiveObjects.getBoolean (true);
    }

    // See if the value is a zero-length String
    else if ("".equals (pValue)) {
      return PrimitiveObjects.getBoolean (true);
    }

    // See if the value is a zero-length array
    else if (pValue.getClass ().isArray () &&
	     Array.getLength (pValue) == 0) {
      return PrimitiveObjects.getBoolean (true);
    }

    // See if the value is an empty List
    else if (pValue instanceof List &&
	     ((List) pValue).isEmpty ()) {
      return PrimitiveObjects.getBoolean (true);
    }

    // See if the value is an empty Map
    else if (pValue instanceof Map &&
	     ((Map) pValue).isEmpty ()) {
      return PrimitiveObjects.getBoolean (true);
    }

    // Otherwise, not empty
    else {
      return PrimitiveObjects.getBoolean (false);
    }
  }

  //-------------------------------------
}
