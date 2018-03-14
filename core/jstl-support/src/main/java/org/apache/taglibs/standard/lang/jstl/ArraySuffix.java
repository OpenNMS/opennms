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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 *
 * <p>Represents an operator that obtains a Map entry, an indexed
 * value, a property value, or an indexed property value of an object.
 * The following are the rules for evaluating this operator:
 *
 * <ul><pre>
 * Evaluating a[b] (assuming a.b == a["b"])
 *   a is null
 *     return null
 *   b is null
 *     return null
 *   a is Map
 *     !a.containsKey (b)
 *       return null
 *     a.get(b) == null
 *       return null
 *     otherwise
 *       return a.get(b)
 *   a is List or array
 *     coerce b to int (using coercion rules)
 *     coercion couldn't be performed
 *       error
 *     a.get(b) or Array.get(a, b) throws ArrayIndexOutOfBoundsException or IndexOutOfBoundsException
 *       return null
 *     a.get(b) or Array.get(a, b) throws other exception
 *       error
 *     return a.get(b) or Array.get(a, b)
 * 
 *   coerce b to String
 *   b is a readable property of a
 *     getter throws an exception
 *       error
 *     otherwise
 *       return result of getter call
 *
 *   otherwise
 *     error
 * </pre></ul>
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ArraySuffix
  extends ValueSuffix
{
  //-------------------------------------
  // Constants
  //-------------------------------------

  // Zero-argument array
  static Object [] sNoArgs = new Object [0];

  //-------------------------------------
  // Properties
  //-------------------------------------
  // property index

  Expression mIndex;
  public Expression getIndex ()
  { return mIndex; }
  public void setIndex (Expression pIndex)
  { mIndex = pIndex; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ArraySuffix (Expression pIndex)
  {
    mIndex = pIndex;
  }

  //-------------------------------------
  /**
   *
   * Gets the value of the index
   **/
  Object evaluateIndex (Object pContext,
			VariableResolver pResolver,
			Map functions,
			String defaultPrefix,
			Logger pLogger)
    throws ELException
  {
    return mIndex.evaluate (pContext, pResolver, functions, defaultPrefix,
			    pLogger);
  }

  //-------------------------------------
  /**
   *
   * Returns the operator symbol
   **/
  String getOperatorSymbol ()
  {
    return "[]";
  }

  //-------------------------------------
  // ValueSuffix methods
  //-------------------------------------
  /**
   *
   * Returns the expression in the expression language syntax
   **/
  public String getExpressionString ()
  {
    return "[" + mIndex.getExpressionString () + "]";
  }

  //-------------------------------------
  /**
   *
   * Evaluates the expression in the given context, operating on the
   * given value.
   **/
  public Object evaluate (Object pValue,
			  Object pContext,
			  VariableResolver pResolver,
			  Map functions,
			  String defaultPrefix,
			  Logger pLogger)
    throws ELException
  {
    Object indexVal;
    String indexStr;
    BeanInfoProperty property;
    BeanInfoIndexedProperty ixproperty;

    // Check for null value
    if (pValue == null) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning 
	  (Constants.CANT_GET_INDEXED_VALUE_OF_NULL,
	   getOperatorSymbol ());
      }
      return null;
    }

    // Evaluate the index
    else if ((indexVal = evaluateIndex (pContext, pResolver,
					functions, defaultPrefix, pLogger)) == 
	     null) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.CANT_GET_NULL_INDEX,
	   getOperatorSymbol ());
      }
      return null;
    }

    // See if it's a Map
    else if (pValue instanceof Map) {
      Map val = (Map) pValue;
      return val.get (indexVal);
    }

    // See if it's a List or array
    else if (pValue instanceof List ||
	     pValue.getClass ().isArray ()) {
      Integer indexObj = Coercions.coerceToInteger (indexVal, pLogger);
      if (indexObj == null) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.BAD_INDEX_VALUE,
	     getOperatorSymbol (),
	     indexVal.getClass ().getName ());
	}
	return null;
      }
      else if (pValue instanceof List) {
	try {
	  return ((List) pValue).get (indexObj.intValue ());
	}
	catch (ArrayIndexOutOfBoundsException exc) {
	  if (pLogger.isLoggingWarning ()) {
	    pLogger.logWarning
	      (Constants.EXCEPTION_ACCESSING_LIST,
	       exc,
	       indexObj);
	  }
	  return null;
	}
	catch (IndexOutOfBoundsException exc) {
	  if (pLogger.isLoggingWarning ()) {
	    pLogger.logWarning
	      (Constants.EXCEPTION_ACCESSING_LIST,
	       exc,
	       indexObj);
	  }
	  return null;
	}
	catch (Exception exc) {
	  if (pLogger.isLoggingError ()) {
	    pLogger.logError
	      (Constants.EXCEPTION_ACCESSING_LIST,
	       exc,
	       indexObj);
	  }
	  return null;
	}
      }
      else {
	try {
	  return Array.get (pValue, indexObj.intValue ());
	}
	catch (ArrayIndexOutOfBoundsException exc) {
	  if (pLogger.isLoggingWarning ()) {
	    pLogger.logWarning
	      (Constants.EXCEPTION_ACCESSING_ARRAY,
	       exc,
	       indexObj);
	  }
	  return null;
	}
	catch (IndexOutOfBoundsException exc) {
	  if (pLogger.isLoggingWarning ()) {
	    pLogger.logWarning
	      (Constants.EXCEPTION_ACCESSING_ARRAY,
	       exc,
	       indexObj);
	  }
	  return null;
	}
	catch (Exception exc) {
	  if (pLogger.isLoggingError ()) {
	    pLogger.logError
	      (Constants.EXCEPTION_ACCESSING_ARRAY,
	       exc,
	       indexObj);
	  }
	  return null;
	}
      }
    }

    // Coerce to a String for property access

    else if ((indexStr = Coercions.coerceToString (indexVal, pLogger)) == 
	     null) {
      return null;
    }

    // Look for a JavaBean property
    else if ((property = BeanInfoManager.getBeanInfoProperty
	      (pValue.getClass (),
	       indexStr,
	       pLogger)) != null &&
	     property.getReadMethod () != null) {
      try {
	return property.getReadMethod ().invoke (pValue, sNoArgs);
      }
      catch (InvocationTargetException exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ERROR_GETTING_PROPERTY,
	     exc.getTargetException (),
	     indexStr,
	     pValue.getClass ().getName ());
	}
	return null;
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ERROR_GETTING_PROPERTY,
	     exc,
	     indexStr,
	     pValue.getClass ().getName ());
	}
	return null;
      }
    }

    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.CANT_FIND_INDEX,
	   indexVal,
	   pValue.getClass ().getName (),
	   getOperatorSymbol ());
      }
      return null;
    }
  }

  //-------------------------------------
}
