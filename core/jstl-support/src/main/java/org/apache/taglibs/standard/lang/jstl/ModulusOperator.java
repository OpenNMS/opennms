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
 * <p>The implementation of the modulus operator
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ModulusOperator
  extends BinaryOperator
{
  //-------------------------------------
  // Singleton
  //-------------------------------------

  public static final ModulusOperator SINGLETON =
    new ModulusOperator ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ModulusOperator ()
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
    return "%";
  }

  //-------------------------------------
  /**
   *
   * Applies the operator to the given value
   **/
  public Object apply (Object pLeft,
		       Object pRight,
		       Object pContext,
		       Logger pLogger)
    throws ELException
  {
    if (pLeft == null &&
	pRight == null) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.ARITH_OP_NULL,
	   getOperatorSymbol ());
      }
      return PrimitiveObjects.getInteger (0);
    }

    if ((pLeft != null &&
	 (Coercions.isFloatingPointType (pLeft) ||
	  Coercions.isFloatingPointString (pLeft))) ||
	(pRight != null &&
	 (Coercions.isFloatingPointType (pRight) ||
	  Coercions.isFloatingPointString (pRight)))) {
      double left =
	Coercions.coerceToPrimitiveNumber (pLeft, Double.class, pLogger).
	doubleValue ();
      double right =
	Coercions.coerceToPrimitiveNumber (pRight, Double.class, pLogger).
	doubleValue ();

      try {
	return PrimitiveObjects.getDouble (left % right);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ARITH_ERROR,
	     getOperatorSymbol (),
	     "" + left,
	     "" + right);
	}
	return PrimitiveObjects.getInteger (0);
      }
    }
    else {
      long left =
	Coercions.coerceToPrimitiveNumber (pLeft, Long.class, pLogger).
	longValue ();
      long right =
	Coercions.coerceToPrimitiveNumber (pRight, Long.class, pLogger).
	longValue ();

      try {
	return PrimitiveObjects.getLong (left % right);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ARITH_ERROR,
	     getOperatorSymbol (),
	     "" + left,
	     "" + right);
	}
	return PrimitiveObjects.getInteger (0);
      }
    }
  }

  //-------------------------------------
}
