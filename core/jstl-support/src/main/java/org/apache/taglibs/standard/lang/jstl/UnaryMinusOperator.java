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

/**
 *
 * <p>The implementation of the unary minus operator
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class UnaryMinusOperator
  extends UnaryOperator
{
  //-------------------------------------
  // Singleton
  //-------------------------------------

  public static final UnaryMinusOperator SINGLETON =
    new UnaryMinusOperator ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public UnaryMinusOperator ()
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
    return "-";
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
    if (pValue == null) {
      /*
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.ARITH_OP_NULL,
	   getOperatorSymbol ());
      }
      */
      return PrimitiveObjects.getInteger (0);
    }

    else if (pValue instanceof String) {
      if (Coercions.isFloatingPointString (pValue)) {
	double dval =
	  ((Number) 
	   (Coercions.coerceToPrimitiveNumber 
	    (pValue, Double.class, pLogger))).
	  doubleValue ();
	return PrimitiveObjects.getDouble (-dval);
      }
      else {
	long lval =
	  ((Number) 
	   (Coercions.coerceToPrimitiveNumber 
	    (pValue, Long.class, pLogger))).
	  longValue ();
	return PrimitiveObjects.getLong (-lval);
      }
    }

    else if (pValue instanceof Byte) {
      return PrimitiveObjects.getByte 
	((byte) -(((Byte) pValue).byteValue ()));
    }
    else if (pValue instanceof Short) {
      return PrimitiveObjects.getShort 
	((short) -(((Short) pValue).shortValue ()));
    }
    else if (pValue instanceof Integer) {
      return PrimitiveObjects.getInteger 
	((int) -(((Integer) pValue).intValue ()));
    }
    else if (pValue instanceof Long) {
      return PrimitiveObjects.getLong 
	((long) -(((Long) pValue).longValue ()));
    }
    else if (pValue instanceof Float) {
      return PrimitiveObjects.getFloat 
	((float) -(((Float) pValue).floatValue ()));
    }
    else if (pValue instanceof Double) {
      return PrimitiveObjects.getDouble 
	((double) -(((Double) pValue).doubleValue ()));
    }

    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.UNARY_OP_BAD_TYPE,
	   getOperatorSymbol (),
	   pValue.getClass ().getName ());
      }
      return PrimitiveObjects.getInteger (0);
    }
  }

  //-------------------------------------
}
