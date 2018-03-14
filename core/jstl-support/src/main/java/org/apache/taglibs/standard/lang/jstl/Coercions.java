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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 *
 * <p>This class contains the logic for coercing data types before
 * operators are applied to them.
 *
 * <p>The following is the list of rules applied for various type
 * conversions.
 *
 * <ul><pre>
 * Applying arithmetic operator
 *   Binary operator - A {+,-,*} B
 *     if A and B are null
 *       return 0
 *     if A or B is Float, Double, or String containing ".", "e", or "E"
 *       coerce both A and B to Double
 *       apply operator
 *     otherwise
 *       coerce both A and B to Long
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 * 
 *   Binary operator - A {/,div} B
 *     if A and B are null
 *       return 0
 *     otherwise
 *       coerce both A and B to Double
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 * 
 *   Binary operator - A {%,mod} B
 *     if A and B are null
 *       return 0
 *     if A or B is Float, Double, or String containing ".", "e" or "E"
 *       coerce both to Double
 *       apply operator
 *     otherwise
 *       coerce both A and B to Long
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 * 
 *   Unary minus operator - -A
 *     if A is null
 *       return 0
 *     if A is String
 *       if A contains ".", "e", or "E"
 *         coerce to Double, apply operator
 *       otherwise
 *         coerce to a Long and apply operator
 *     if A is Byte,Short,Integer,Long,Float,Double
 *       retain type, apply operator
 *     if operator results in exception, error
 *     otherwise
 *       error
 *
 * Applying "empty" operator - empty A
 *   if A is null
 *     return true
 *   if A is zero-length String
 *     return true
 *   if A is zero-length array
 *     return true
 *   if A is List and ((List) A).isEmpty()
 *     return true
 *   if A is Map and ((Map) A).isEmpty()
 *     return true
 *   otherwise
 *     return false
 * 
 * Applying logical operators
 *   Binary operator - A {and,or} B
 *     coerce both A and B to Boolean, apply operator
 *   NOTE - operator stops as soon as expression can be determined, i.e.,
 *     A and B and C and D - if B is false, then only A and B is evaluated
 *   Unary not operator - not A
 *     coerce A to Boolean, apply operator
 * 
 * Applying relational operator
 *   A {<,>,<=,>=,lt,gt,lte,gte} B
 *     if A==B
 *       if operator is >= or <=
 *         return true
 *       otherwise
 *         return false
 *     if A or B is null
 *       return false
 *     if A or B is Float or Double
 *       coerce both A and B to Double
 *       apply operator
 *     if A or B is Byte,Short,Character,Integer,Long
 *       coerce both A and B to Long
 *       apply operator
 *     if A or B is String
 *       coerce both A and B to String, compare lexically
 *     if A is Comparable
 *       if A.compareTo (B) throws exception
 *         error
 *       otherwise
 *         use result of A.compareTo(B)
 *     if B is Comparable
 *       if B.compareTo (A) throws exception
 *         error
 *       otherwise
 *         use result of B.compareTo(A)
 *     otherwise
 *       error
 * 
 * Applying equality operator
 *   A {==,!=} B
 *     if A==B
 *       apply operator
 *     if A or B is null
 *       return false for ==, true for !=
 *     if A or B is Float or Double
 *       coerce both A and B to Double
 *       apply operator
 *     if A or B is Byte,Short,Character,Integer,Long
 *       coerce both A and B to Long
 *       apply operator
 *     if A or B is Boolean
 *       coerce both A and B to Boolean
 *       apply operator
 *     if A or B is String
 *       coerce both A and B to String, compare lexically
 *     otherwise
 *       if an error occurs while calling A.equals(B)
 *         error
 *       apply operator to result of A.equals(B)
 * 
 * coercions
 * 
 *   coerce A to String
 *     A is String
 *       return A
 *     A is null
 *       return ""
 *     A.toString throws exception
 *       error
 *     otherwise
 *       return A.toString
 * 
 *   coerce A to primitive Number type N
 *     A is null or ""
 *       return 0
 *     A is Character
 *       convert to short, apply following rules
 *     A is Boolean
 *       error
 *     A is Number type N
 *       return A
 *     A is Number with less precision than N
 *       coerce quietly
 *     A is Number with greater precision than N
 *       coerce quietly
 *     A is String
 *       new N.valueOf(A) throws exception
 *         error
 *       return N.valueOf(A)
 *     otherwise
 *       error
 * 
 *   coerce A to Character should be
 *     A is null or ""
 *       return (char) 0
 *     A is Character
 *       return A
 *     A is Boolean
 *       error
 *     A is Number with less precision than short
 *       coerce quietly - return (char) A
 *     A is Number with greater precision than short
 *       coerce quietly - return (char) A
 *     A is String
 *       return A.charAt (0)
 *     otherwise
 *       error
 * 
 *   coerce A to Boolean
 *     A is null or ""
 *       return false
 *     A is Boolean
 *       return A
 *     A is String
 *       Boolean.valueOf(A) throws exception
 *         error
 *       return Boolean.valueOf(A)
 *     otherwise
 *       error
 * 
 *   coerce A to any other type T
 *     A is null
 *       return null
 *     A is assignable to T
 *       coerce quietly
 *     A is String
 *       T has no PropertyEditor
 *         if A is "", return null
 *         otherwise error
 *       T's PropertyEditor throws exception
 *         if A is "", return null
 *         otherwise error
 *       otherwise
 *         apply T's PropertyEditor
 *     otherwise
 *       error
 * </pre></ul>
 *
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class Coercions
{
  //-------------------------------------
  /**
   *
   * Coerces the given value to the specified class.
   **/
  public static Object coerce (Object pValue,
			       Class pClass,
			       Logger pLogger)
    throws ELException
  {
    if (pClass == String.class) {
      return coerceToString (pValue, pLogger);
    }
    else if (isPrimitiveNumberClass (pClass)) {
      return coerceToPrimitiveNumber (pValue, pClass, pLogger);
    }
    else if (pClass == Character.class ||
	     pClass == Character.TYPE) {
      return coerceToCharacter (pValue, pLogger);
    }
    else if (pClass == Boolean.class ||
	     pClass == Boolean.TYPE) {
      return coerceToBoolean (pValue, pLogger);
    }
    else {
      return coerceToObject (pValue, pClass, pLogger);
    }
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given class is Byte, Short, Integer, Long,
   * Float, Double
   **/
  static boolean isPrimitiveNumberClass (Class pClass)
  {
    return
      pClass == Byte.class ||
      pClass == Byte.TYPE ||
      pClass == Short.class ||
      pClass == Short.TYPE ||
      pClass == Integer.class ||
      pClass == Integer.TYPE ||
      pClass == Long.class ||
      pClass == Long.TYPE ||
      pClass == Float.class ||
      pClass == Float.TYPE ||
      pClass == Double.class ||
      pClass == Double.TYPE;
  }

  //-------------------------------------
  /**
   *
   * Coerces the specified value to a String
   **/
  public static String coerceToString (Object pValue,
				       Logger pLogger)
    throws ELException
  {
    if (pValue == null) {
      return "";
    }
    else if (pValue instanceof String) {
      return (String) pValue;
    }
    else {
      try {
	return pValue.toString ();
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError (Constants.TOSTRING_EXCEPTION,
			    exc,
			    pValue.getClass ().getName ());
	}
	return "";
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a value to the given primitive number class
   **/
  public static Number coerceToPrimitiveNumber (Object pValue,
						Class pClass,
						Logger pLogger)
    throws ELException
  {
    if (pValue == null ||
	"".equals (pValue)) {
      return coerceToPrimitiveNumber (0, pClass);
    }
    else if (pValue instanceof Character) {
      char val = ((Character) pValue).charValue ();
      return coerceToPrimitiveNumber ((short) val, pClass);
    }
    else if (pValue instanceof Boolean) {
      if (pLogger.isLoggingError ()) {
	pLogger.logError (Constants.BOOLEAN_TO_NUMBER,
			  pValue,
			  pClass.getName ());
      }
      return coerceToPrimitiveNumber (0, pClass);
    }
    else if (pValue.getClass () == pClass) {
      return (Number) pValue;
    }
    else if (pValue instanceof Number) {
      return coerceToPrimitiveNumber ((Number) pValue, pClass);
    }
    else if (pValue instanceof String) {
      try {
	return coerceToPrimitiveNumber ((String) pValue, pClass);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.STRING_TO_NUMBER_EXCEPTION,
	     (String) pValue,
	     pClass.getName ());
	}
	return coerceToPrimitiveNumber (0, pClass);
      }
    }
    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.COERCE_TO_NUMBER,
	   pValue.getClass ().getName (),
	   pClass.getName ());
      }
      return coerceToPrimitiveNumber (0, pClass);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a value to an Integer, returning null if the coercion
   * isn't possible.
   **/
  public static Integer coerceToInteger (Object pValue,
					 Logger pLogger)
    throws ELException
  {
    if (pValue == null) {
      return null;
    }
    else if (pValue instanceof Character) {
      return PrimitiveObjects.getInteger 
	((int) (((Character) pValue).charValue ()));
    }
    else if (pValue instanceof Boolean) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning (Constants.BOOLEAN_TO_NUMBER,
			    pValue,
			    Integer.class.getName ());
      }
      return PrimitiveObjects.getInteger
	(((Boolean) pValue).booleanValue () ? 1 : 0);
    }
    else if (pValue instanceof Integer) {
      return (Integer) pValue;
    }
    else if (pValue instanceof Number) {
      return PrimitiveObjects.getInteger (((Number) pValue).intValue ());
    }
    else if (pValue instanceof String) {
      try {
	return Integer.valueOf ((String) pValue);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingWarning ()) {
	  pLogger.logWarning
	    (Constants.STRING_TO_NUMBER_EXCEPTION,
	     (String) pValue,
	     Integer.class.getName ());
	}
	return null;
      }
    }
    else {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.COERCE_TO_NUMBER,
	   pValue.getClass ().getName (),
	   Integer.class.getName ());
      }
      return null;
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a long to the given primitive number class
   **/
  static Number coerceToPrimitiveNumber (long pValue,
					 Class pClass)
    throws ELException
  {
    if (pClass == Byte.class || pClass == Byte.TYPE) {
      return PrimitiveObjects.getByte ((byte) pValue);
    }
    else if (pClass == Short.class || pClass == Short.TYPE) {
      return PrimitiveObjects.getShort ((short) pValue);
    }
    else if (pClass == Integer.class || pClass == Integer.TYPE) {
      return PrimitiveObjects.getInteger ((int) pValue);
    }
    else if (pClass == Long.class || pClass == Long.TYPE) {
      return PrimitiveObjects.getLong ((long) pValue);
    }
    else if (pClass == Float.class || pClass == Float.TYPE) {
      return PrimitiveObjects.getFloat ((float) pValue);
    }
    else if (pClass == Double.class || pClass == Double.TYPE) {
      return PrimitiveObjects.getDouble ((double) pValue);
    }
    else {
      return PrimitiveObjects.getInteger (0);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a double to the given primitive number class
   **/
  static Number coerceToPrimitiveNumber (double pValue,
					 Class pClass)
    throws ELException
  {
    if (pClass == Byte.class || pClass == Byte.TYPE) {
      return PrimitiveObjects.getByte ((byte) pValue);
    }
    else if (pClass == Short.class || pClass == Short.TYPE) {
      return PrimitiveObjects.getShort ((short) pValue);
    }
    else if (pClass == Integer.class || pClass == Integer.TYPE) {
      return PrimitiveObjects.getInteger ((int) pValue);
    }
    else if (pClass == Long.class || pClass == Long.TYPE) {
      return PrimitiveObjects.getLong ((long) pValue);
    }
    else if (pClass == Float.class || pClass == Float.TYPE) {
      return PrimitiveObjects.getFloat ((float) pValue);
    }
    else if (pClass == Double.class || pClass == Double.TYPE) {
      return PrimitiveObjects.getDouble ((double) pValue);
    }
    else {
      return PrimitiveObjects.getInteger (0);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a Number to the given primitive number class
   **/
  static Number coerceToPrimitiveNumber (Number pValue,
					 Class pClass)
    throws ELException
  {
    if (pClass == Byte.class || pClass == Byte.TYPE) {
      return PrimitiveObjects.getByte (pValue.byteValue ());
    }
    else if (pClass == Short.class || pClass == Short.TYPE) {
      return PrimitiveObjects.getShort (pValue.shortValue ());
    }
    else if (pClass == Integer.class || pClass == Integer.TYPE) {
      return PrimitiveObjects.getInteger (pValue.intValue ());
    }
    else if (pClass == Long.class || pClass == Long.TYPE) {
      return PrimitiveObjects.getLong (pValue.longValue ());
    }
    else if (pClass == Float.class || pClass == Float.TYPE) {
      return PrimitiveObjects.getFloat (pValue.floatValue ());
    }
    else if (pClass == Double.class || pClass == Double.TYPE) {
      return PrimitiveObjects.getDouble (pValue.doubleValue ());
    }
    else {
      return PrimitiveObjects.getInteger (0);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a String to the given primitive number class
   **/
  static Number coerceToPrimitiveNumber (String pValue,
					 Class pClass)
    throws ELException
  {
    if (pClass == Byte.class || pClass == Byte.TYPE) {
      return Byte.valueOf (pValue);
    }
    else if (pClass == Short.class || pClass == Short.TYPE) {
      return Short.valueOf (pValue);
    }
    else if (pClass == Integer.class || pClass == Integer.TYPE) {
      return Integer.valueOf (pValue);
    }
    else if (pClass == Long.class || pClass == Long.TYPE) {
      return Long.valueOf (pValue);
    }
    else if (pClass == Float.class || pClass == Float.TYPE) {
      return Float.valueOf (pValue);
    }
    else if (pClass == Double.class || pClass == Double.TYPE) {
      return Double.valueOf (pValue);
    }
    else {
      return PrimitiveObjects.getInteger (0);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a value to a Character
   **/
  public static Character coerceToCharacter (Object pValue,
					     Logger pLogger)
    throws ELException
  {
    if (pValue == null ||
	"".equals (pValue)) {
      return PrimitiveObjects.getCharacter ((char) 0);
    }
    else if (pValue instanceof Character) {
      return (Character) pValue;
    }
    else if (pValue instanceof Boolean) {
      if (pLogger.isLoggingError ()) {
	pLogger.logError (Constants.BOOLEAN_TO_CHARACTER, pValue);
      }
      return PrimitiveObjects.getCharacter ((char) 0);
    }
    else if (pValue instanceof Number) {
      return PrimitiveObjects.getCharacter 
	((char) ((Number) pValue).shortValue ());
    }
    else if (pValue instanceof String) {
      String str = (String) pValue;
      return PrimitiveObjects.getCharacter (str.charAt (0));
    }
    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.COERCE_TO_CHARACTER,
	   pValue.getClass ().getName ());
      }
      return PrimitiveObjects.getCharacter ((char) 0);
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a value to a Boolean
   **/
  public static Boolean coerceToBoolean (Object pValue,
					 Logger pLogger)
    throws ELException
  {
    if (pValue == null ||
	"".equals (pValue)) {
      return Boolean.FALSE;
    }
    else if (pValue instanceof Boolean) {
      return (Boolean) pValue;
    }
    else if (pValue instanceof String) {
      String str = (String) pValue;
      try {
	return Boolean.valueOf (str);
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.STRING_TO_BOOLEAN,
	     exc,
	     (String) pValue);
	}
	return Boolean.FALSE;
      }
    }
    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.COERCE_TO_BOOLEAN,
	   pValue.getClass ().getName ());
      }
      return Boolean.TRUE;
    }
  }

  //-------------------------------------
  /**
   *
   * Coerces a value to the specified Class that is not covered by any
   * of the above cases
   **/
  public static Object coerceToObject (Object pValue,
				       Class pClass,
				       Logger pLogger)
    throws ELException
  {
    if (pValue == null) {
      return null;
    }
    else if (pClass.isAssignableFrom (pValue.getClass ())) {
      return pValue;
    }
    else if (pValue instanceof String) {
      String str = (String) pValue;
      PropertyEditor pe = PropertyEditorManager.findEditor (pClass);
      if (pe == null) {
	if ("".equals (str)) {
	  return null;
	}
	else {
	  if (pLogger.isLoggingError ()) {
	    pLogger.logError
	      (Constants.NO_PROPERTY_EDITOR,
	       str,
	       pClass.getName ());
	  }
	  return null;
	}
      }
      try {
	pe.setAsText (str);
	return pe.getValue ();
      }
      catch (IllegalArgumentException exc) {
	if ("".equals (str)) {
	  return null;
	}
	else {
	  if (pLogger.isLoggingError ()) {
	    pLogger.logError
	      (Constants.PROPERTY_EDITOR_ERROR,
	       exc,
	       pValue,
	       pClass.getName ());
	  }
	  return null;
	}
      }
    }
    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.COERCE_TO_OBJECT,
	   pValue.getClass ().getName (),
	   pClass.getName ());
      }
      return null;
    }
  }

  //-------------------------------------
  // Applying operators
  //-------------------------------------
  /**
   *
   * Performs all of the necessary type conversions, then calls on the
   * appropriate operator.
   **/
  public static Object applyArithmeticOperator 
    (Object pLeft,
     Object pRight,
     ArithmeticOperator pOperator,
     Logger pLogger)
    throws ELException
  {
    if (pLeft == null &&
	pRight == null) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.ARITH_OP_NULL,
	   pOperator.getOperatorSymbol ());
      }
      return PrimitiveObjects.getInteger (0);
    }

    else if (isFloatingPointType (pLeft) ||
	     isFloatingPointType (pRight) ||
	     isFloatingPointString (pLeft) ||
	     isFloatingPointString (pRight)) {
      double left =
	coerceToPrimitiveNumber (pLeft, Double.class, pLogger).
	doubleValue ();
      double right =
	coerceToPrimitiveNumber (pRight, Double.class, pLogger).
	doubleValue ();
      return 
	PrimitiveObjects.getDouble (pOperator.apply (left, right, pLogger));
    }

    else {
      long left =
	coerceToPrimitiveNumber (pLeft, Long.class, pLogger).
	longValue ();
      long right =
	coerceToPrimitiveNumber (pRight, Long.class, pLogger).
	longValue ();
      return
	PrimitiveObjects.getLong (pOperator.apply (left, right, pLogger));
    }
  }

  //-------------------------------------
  /**
   *
   * Performs all of the necessary type conversions, then calls on the
   * appropriate operator.
   **/
  public static Object applyRelationalOperator 
    (Object pLeft,
     Object pRight,
     RelationalOperator pOperator,
     Logger pLogger)
    throws ELException
  {
    if (isFloatingPointType (pLeft) ||
	isFloatingPointType (pRight)) {
      double left =
	coerceToPrimitiveNumber (pLeft, Double.class, pLogger).
	doubleValue ();
      double right =
	coerceToPrimitiveNumber (pRight, Double.class, pLogger).
	doubleValue ();
      return 
	PrimitiveObjects.getBoolean (pOperator.apply (left, right, pLogger));
    }

    else if (isIntegerType (pLeft) ||
	     isIntegerType (pRight)) {
      long left =
	coerceToPrimitiveNumber (pLeft, Long.class, pLogger).
	longValue ();
      long right =
	coerceToPrimitiveNumber (pRight, Long.class, pLogger).
	longValue ();
      return
	PrimitiveObjects.getBoolean (pOperator.apply (left, right, pLogger));
    }

    else if (pLeft instanceof String ||
	     pRight instanceof String) {
      String left = coerceToString (pLeft, pLogger);
      String right = coerceToString (pRight, pLogger);
      return
	PrimitiveObjects.getBoolean (pOperator.apply (left, right, pLogger));
    }

    else if (pLeft instanceof Comparable) {
      try {
	int result = ((Comparable) pLeft).compareTo (pRight);
	return
	  PrimitiveObjects.getBoolean 
	  (pOperator.apply (result, -result, pLogger));
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.COMPARABLE_ERROR,
	     exc,
	     pLeft.getClass ().getName (),
	     (pRight == null) ? "null" : pRight.getClass ().getName (),
	     pOperator.getOperatorSymbol ());
	}
	return Boolean.FALSE;
      }
    }

    else if (pRight instanceof Comparable) {
      try {
	int result = ((Comparable) pRight).compareTo (pLeft);
	return
	  PrimitiveObjects.getBoolean 
	  (pOperator.apply (-result, result, pLogger));
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.COMPARABLE_ERROR,
	     exc,
	     pRight.getClass ().getName (),
	     (pLeft == null) ? "null" : pLeft.getClass ().getName (),
	     pOperator.getOperatorSymbol ());
	}
	return Boolean.FALSE;
      }
    }

    else {
      if (pLogger.isLoggingError ()) {
	pLogger.logError
	  (Constants.ARITH_OP_BAD_TYPE,
	   pOperator.getOperatorSymbol (),
	   pLeft.getClass ().getName (),
	   pRight.getClass ().getName ());
      }
      return Boolean.FALSE;
    }
  }

  //-------------------------------------
  /**
   *
   * Performs all of the necessary type conversions, then calls on the
   * appropriate operator.
   **/
  public static Object applyEqualityOperator 
    (Object pLeft,
     Object pRight,
     EqualityOperator pOperator,
     Logger pLogger)
    throws ELException
  {
    if (pLeft == pRight) {
      return PrimitiveObjects.getBoolean (pOperator.apply (true, pLogger));
    }

    else if (pLeft == null ||
	     pRight == null) {
      return PrimitiveObjects.getBoolean (pOperator.apply (false, pLogger));
    }

    else if (isFloatingPointType (pLeft) ||
	     isFloatingPointType (pRight)) {
      double left =
	coerceToPrimitiveNumber (pLeft, Double.class, pLogger).
	doubleValue ();
      double right =
	coerceToPrimitiveNumber (pRight, Double.class, pLogger).
	doubleValue ();
      return 
	PrimitiveObjects.getBoolean 
	(pOperator.apply (left == right, pLogger));
    }

    else if (isIntegerType (pLeft) ||
	     isIntegerType (pRight)) {
      long left =
	coerceToPrimitiveNumber (pLeft, Long.class, pLogger).
	longValue ();
      long right =
	coerceToPrimitiveNumber (pRight, Long.class, pLogger).
	longValue ();
      return
	PrimitiveObjects.getBoolean 
	(pOperator.apply (left == right, pLogger));
    }

    else if (pLeft instanceof Boolean ||
	     pRight instanceof Boolean) {
      boolean left = coerceToBoolean (pLeft, pLogger).booleanValue ();
      boolean right = coerceToBoolean (pRight, pLogger).booleanValue ();
      return
	PrimitiveObjects.getBoolean 
	(pOperator.apply (left == right, pLogger));
    }

    else if (pLeft instanceof String ||
	     pRight instanceof String) {
      String left = coerceToString (pLeft, pLogger);
      String right = coerceToString (pRight, pLogger);
      return
	PrimitiveObjects.getBoolean 
	(pOperator.apply (left.equals (right), pLogger));
    }

    else {
      try {
      return
	PrimitiveObjects.getBoolean
	(pOperator.apply (pLeft.equals (pRight), pLogger));
      }
      catch (Exception exc) {
	if (pLogger.isLoggingError ()) {
	  pLogger.logError
	    (Constants.ERROR_IN_EQUALS,
	     exc,
	     pLeft.getClass ().getName (),
	     pRight.getClass ().getName (),
	     pOperator.getOperatorSymbol ());
	}
	return Boolean.FALSE;
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given Object is of a floating point type
   **/
  public static boolean isFloatingPointType (Object pObject)
  {
    return 
      pObject != null &&
      isFloatingPointType (pObject.getClass ());
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given class is of a floating point type
   **/
  public static boolean isFloatingPointType (Class pClass)
  {
    return
      pClass == Float.class ||
      pClass == Float.TYPE ||
      pClass == Double.class ||
      pClass == Double.TYPE;
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given string might contain a floating point
   * number - i.e., it contains ".", "e", or "E"
   **/
  public static boolean isFloatingPointString (Object pObject)
  {
    if (pObject instanceof String) {
      String str = (String) pObject;
      int len = str.length ();
      for (int i = 0; i < len; i++) {
	char ch = str.charAt (i);
	if (ch == '.' ||
	    ch == 'e' ||
	    ch == 'E') {
	  return true;
	}
      }
      return false;
    }
    else {
      return false;
    }
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given Object is of an integer type
   **/
  public static boolean isIntegerType (Object pObject)
  {
    return 
      pObject != null &&
      isIntegerType (pObject.getClass ());
  }

  //-------------------------------------
  /**
   *
   * Returns true if the given class is of an integer type
   **/
  public static boolean isIntegerType (Class pClass)
  {
    return
      pClass == Byte.class ||
      pClass == Byte.TYPE ||
      pClass == Short.class ||
      pClass == Short.TYPE ||
      pClass == Character.class ||
      pClass == Character.TYPE ||
      pClass == Integer.class ||
      pClass == Integer.TYPE ||
      pClass == Long.class ||
      pClass == Long.TYPE;
  }

  //-------------------------------------

}
