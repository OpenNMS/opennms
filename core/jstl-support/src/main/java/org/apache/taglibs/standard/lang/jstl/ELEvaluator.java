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

import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.taglibs.standard.lang.jstl.parser.ELParser;
import org.apache.taglibs.standard.lang.jstl.parser.ParseException;
import org.apache.taglibs.standard.lang.jstl.parser.Token;
import org.apache.taglibs.standard.lang.jstl.parser.TokenMgrError;

/**
 *
 * <p>This is the main class for evaluating expression Strings.  An
 * expression String is a String that may contain expressions of the
 * form ${...}.  Multiple expressions may appear in the same
 * expression String.  In such a case, the expression String's value
 * is computed by concatenating the String values of those evaluated
 * expressions and any intervening non-expression text, then
 * converting the resulting String to the expected type using the
 * PropertyEditor mechanism.
 *
 * <p>In the special case where the expression String is a single
 * expression, the value of the expression String is determined by
 * evaluating the expression, without any intervening conversion to a
 * String.
 *
 * <p>The evaluator maintains a cache mapping expression Strings to
 * their parsed results.  For expression Strings containing no
 * expression elements, it maintains a cache mapping
 * ExpectedType/ExpressionString to parsed value, so that static
 * expression Strings won't have to go through a conversion step every
 * time they are used.  All instances of the evaluator share the same
 * cache.  The cache may be bypassed by setting a flag on the
 * evaluator's constructor.
 *
 * <p>The evaluator must be passed a VariableResolver in its
 * constructor.  The VariableResolver is used to resolve variable
 * names encountered in expressions, and can also be used to implement
 * "implicit objects" that are always present in the namespace.
 * Different applications will have different policies for variable
 * lookups and implicit objects - these differences can be
 * encapsulated in the VariableResolver passed to the evaluator's
 * constructor.
 *
 * <p>Most VariableResolvers will need to perform their resolution
 * against some context.  For example, a JSP environment needs a
 * PageContext to resolve variables.  The evaluate() method takes a
 * generic Object context which is eventually passed to the
 * VariableResolver - the VariableResolver is responsible for casting
 * the context to the proper type.
 *
 * <p>Once an evaluator instance has been constructed, it may be used
 * multiple times, and may be used by multiple simultaneous Threads.
 * In other words, an evaluator instance is well-suited for use as a
 * singleton.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class ELEvaluator
{
  //-------------------------------------
  // Properties
  //-------------------------------------

  //-------------------------------------
  // Member variables
  //-------------------------------------

  /** The mapping from expression String to its parsed form (String,
      Expression, or ExpressionString) **/
  static Map sCachedExpressionStrings = 
    Collections.synchronizedMap (new HashMap ());

  /** The mapping from ExpectedType to Maps mapping literal String to
      parsed value **/
  static Map sCachedExpectedTypes = new HashMap ();

  /** The static Logger **/
  static Logger sLogger = new Logger (System.out);

  /** The VariableResolver **/
  VariableResolver mResolver;

  /** Flag if the cache should be bypassed **/
  boolean mBypassCache;

  //-------------------------------------
  /**
   *
   * Constructor
   *
   * @param pResolver the object that should be used to resolve
   * variable names encountered in expressions.  If null, all variable
   * references will resolve to null.
   **/
  public ELEvaluator (VariableResolver pResolver)
  {
    mResolver = pResolver;
  }

  //-------------------------------------
  /**
   *
   * Constructor
   *
   * @param pResolver the object that should be used to resolve
   * variable names encountered in expressions.  If null, all variable
   * references will resolve to null.
   *
   * @param pBypassCache flag indicating if the cache should be
   * bypassed
   **/
  public ELEvaluator (VariableResolver pResolver,
		      boolean pBypassCache)
  {
    mResolver = pResolver;
    mBypassCache = pBypassCache;
  }

  //-------------------------------------
  /**
   *
   * Evaluates the given expression String
   *
   * @param pExpressionString the expression String to be evaluated
   * @param pContext the context passed to the VariableResolver for
   * resolving variable names
   * @param pExpectedType the type to which the evaluated expression
   * should be coerced
   * @return the expression String evaluated to the given expected
   * type
   **/
  public Object evaluate (String pExpressionString,
			  Object pContext,
			  Class pExpectedType,
			  Map functions,
			  String defaultPrefix)
    throws ELException
  {
    return evaluate (pExpressionString,
		     pContext,
		     pExpectedType,
		     functions,
		     defaultPrefix,
		     sLogger);
  }

  //-------------------------------------
  /**
   *
   * Evaluates the given expression string
   **/
  Object evaluate (String pExpressionString,
		   Object pContext,
		   Class pExpectedType,
		   Map functions,
		   String defaultPrefix,
		   Logger pLogger)
    throws ELException
  {
    // Check for null expression strings
    if (pExpressionString == null) {
      throw new ELException
	(Constants.NULL_EXPRESSION_STRING);
    }

    // Get the parsed version of the expression string
    Object parsedValue = parseExpressionString (pExpressionString);

    // Evaluate differently based on the parsed type
    if (parsedValue instanceof String) {
      // Convert the String, and cache the conversion
      String strValue = (String) parsedValue;
      return convertStaticValueToExpectedType (strValue, 
					       pExpectedType, 
					       pLogger);
    }

    else if (parsedValue instanceof Expression) {
      // Evaluate the expression and convert
      Object value = 
	((Expression) parsedValue).evaluate (pContext,
					     mResolver,
					     functions,
					     defaultPrefix,
					     pLogger);
      return convertToExpectedType (value, 
				    pExpectedType,
				    pLogger);
    }

    else if (parsedValue instanceof ExpressionString) {
      // Evaluate the expression/string list and convert
      String strValue = 
	((ExpressionString) parsedValue).evaluate (pContext, 
						   mResolver,
						   functions,
						   defaultPrefix,
						   pLogger);
      return convertToExpectedType (strValue,
				    pExpectedType,
				    pLogger);
    }

    else {
      // This should never be reached
      return null;
    }
  }

  //-------------------------------------
  /**
   *
   * Gets the parsed form of the given expression string.  If the
   * parsed form is cached (and caching is not bypassed), return the
   * cached form, otherwise parse and cache the value.  Returns either
   * a String, Expression, or ExpressionString.
   **/
  public Object parseExpressionString (String pExpressionString)
    throws ELException
  {
    // See if it's an empty String
    if (pExpressionString.length () == 0) {
      return "";
    }

    // See if it's in the cache
    Object ret = 
      mBypassCache ?
      null :
      sCachedExpressionStrings.get (pExpressionString);

    if (ret == null) {
      // Parse the expression
      Reader r = new StringReader (pExpressionString);
      ELParser parser = new ELParser (r);
      try {
	ret = parser.ExpressionString ();
	sCachedExpressionStrings.put (pExpressionString, ret);
      }
      catch (ParseException exc) {
	throw new ELException 
	  (formatParseException (pExpressionString,
				 exc));
      }
      catch (TokenMgrError exc) {
	// Note - this should never be reached, since the parser is
	// constructed to tokenize any input (illegal inputs get
	// parsed to <BADLY_ESCAPED_STRING_LITERAL> or
	// <ILLEGAL_CHARACTER>
	throw new ELException (exc.getMessage ());
      }
    }
    return ret;
  }

  //-------------------------------------
  /**
   *
   * Converts the given value to the specified expected type.
   **/
  Object convertToExpectedType (Object pValue,
				Class pExpectedType,
				Logger pLogger)
    throws ELException
  {
    return Coercions.coerce (pValue,
			     pExpectedType,
			     pLogger);
  }

  //-------------------------------------
  /**
   *
   * Converts the given String, specified as a static expression
   * string, to the given expected type.  The conversion is cached.
   **/
  Object convertStaticValueToExpectedType (String pValue,
					   Class pExpectedType,
					   Logger pLogger)
    throws ELException
  {
    // See if the value is already of the expected type
    if (pExpectedType == String.class ||
	pExpectedType == Object.class) {
      return pValue;
    }

    // Find the cached value
    Map valueByString = getOrCreateExpectedTypeMap (pExpectedType);
    if (!mBypassCache &&
	valueByString.containsKey (pValue)) {
      return valueByString.get (pValue);
    }
    else {
      // Convert from a String
      Object ret = Coercions.coerce (pValue, pExpectedType, pLogger);
      valueByString.put (pValue, ret);
      return ret;
    }
  }

  //-------------------------------------
  /**
   *
   * Creates or returns the Map that maps string literals to parsed
   * values for the specified expected type.
   **/
  static Map getOrCreateExpectedTypeMap (Class pExpectedType)
  {
    synchronized (sCachedExpectedTypes) {
      Map ret = (Map) sCachedExpectedTypes.get (pExpectedType);
      if (ret == null) {
	ret = Collections.synchronizedMap (new HashMap ());
	sCachedExpectedTypes.put (pExpectedType, ret);
      }
      return ret;
    }
  }

  //-------------------------------------
  // Formatting ParseException
  //-------------------------------------
  /**
   *
   * Formats a ParseException into an error message suitable for
   * displaying on a web page
   **/
  static String formatParseException (String pExpressionString,
				      ParseException pExc)
  {
    // Generate the String of expected tokens
    StringBuffer expectedBuf = new StringBuffer ();
    int maxSize = 0;
    boolean printedOne = false;

    if (pExc.expectedTokenSequences == null)
      return pExc.toString();

    for (int i = 0; i < pExc.expectedTokenSequences.length; i++) {
      if (maxSize < pExc.expectedTokenSequences [i].length) {
        maxSize = pExc.expectedTokenSequences [i].length;
      }
      for (int j = 0; j < pExc.expectedTokenSequences [i].length; j++) {
	if (printedOne) {
	  expectedBuf.append (", ");
	}
        expectedBuf.append 
	  (pExc.tokenImage [pExc.expectedTokenSequences [i] [j]]);
	printedOne = true;
      }
    }
    String expected = expectedBuf.toString ();

    // Generate the String of encountered tokens
    StringBuffer encounteredBuf = new StringBuffer ();
    Token tok = pExc.currentToken.next;
    for (int i = 0; i < maxSize; i++) {
      if (i != 0) encounteredBuf.append (" ");
      if (tok.kind == 0) {
        encounteredBuf.append (pExc.tokenImage [0]);
        break;
      }
      encounteredBuf.append (addEscapes (tok.image));
      tok = tok.next; 
    }
    String encountered = encounteredBuf.toString ();

    // Format the error message
    return MessageFormat.format
      (Constants.PARSE_EXCEPTION,
       new Object [] {
	 expected,
	 encountered,
       });
  }

  //-------------------------------------
  /**
   *
   * Used to convert raw characters to their escaped version when
   * these raw version cannot be used as part of an ASCII string
   * literal.
   **/
  static String addEscapes (String str)
  {
    StringBuffer retval = new StringBuffer ();
    char ch;
    for (int i = 0; i < str.length (); i++) {
      switch (str.charAt (i)) {
	case 0 :
	  continue;
	case '\b':
	  retval.append ("\\b");
	  continue;
	case '\t':
	  retval.append ("\\t");
	  continue;
	case '\n':
	  retval.append ("\\n");
	  continue;
	case '\f':
	  retval.append ("\\f");
	  continue;
	case '\r':
	  retval.append ("\\r");
	  continue;
	default:
	  if ((ch = str.charAt (i)) < 0x20 || ch > 0x7e) {
	    String s = "0000" + Integer.toString (ch, 16);
	    retval.append ("\\u" + s.substring (s.length () - 4, s.length ()));
	  }
	  else {
	    retval.append (ch);
	  }
	  continue;
        }
    }
    return retval.toString ();
  }

  //-------------------------------------
  // Testing methods
  //-------------------------------------
  /**
   *
   * Parses the given expression string, then converts it back to a
   * String in its canonical form.  This is used to test parsing.
   **/
  public String parseAndRender (String pExpressionString)
    throws ELException
  {
    Object val = parseExpressionString (pExpressionString);
    if (val instanceof String) {
      return (String) val;
    }
    else if (val instanceof Expression) {
      return "${" + ((Expression) val).getExpressionString () + "}";
    }
    else if (val instanceof ExpressionString) {
      return ((ExpressionString) val).getExpressionString ();
    }
    else {
      return "";
    }
  }

  //-------------------------------------

}
