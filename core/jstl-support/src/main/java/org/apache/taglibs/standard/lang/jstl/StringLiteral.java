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
 * <p>An expression representing a String literal value.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class StringLiteral
  extends Literal
{
  //-------------------------------------
  /**
   *
   * Constructor
   **/
  StringLiteral (Object pValue)
  {
    super (pValue);
  }

  //-------------------------------------
  /**
   *
   * Returns a StringLiteral parsed from the given token (enclosed by
   * single or double quotes)
   **/
  public static StringLiteral fromToken (String pToken)
  {
    return new StringLiteral (getValueFromToken (pToken));
  }

  //-------------------------------------
  /**
   *
   * Returns a StringLiteral with the given string value
   **/
  public static StringLiteral fromLiteralValue (String pValue)
  {
    return new StringLiteral (pValue);
  }

  //-------------------------------------
  /**
   *
   * Parses the given token into the literal value
   **/
  public static String getValueFromToken (String pToken)
  {
    StringBuffer buf = new StringBuffer ();
    int len = pToken.length () - 1;
    boolean escaping = false;
    for (int i = 1; i < len; i++) {
      char ch = pToken.charAt (i);
      if (escaping) {
	buf.append (ch);
	escaping = false;
      }
      else if (ch == '\\') {
	escaping = true;
      }
      else {
	buf.append (ch);
      }
    }
    return buf.toString ();
  }

  //-------------------------------------
  /**
   *
   * Converts the specified value to a String token, using " as the
   * enclosing quotes and escaping any characters that need escaping.
   **/
  public static String toStringToken (String pValue)
  {
    // See if any escaping is needed
    if (pValue.indexOf ('\"') < 0 &&
	pValue.indexOf ('\\') < 0) {
      return "\"" + pValue + "\"";
    }

    // Escaping is needed
    else {
      StringBuffer buf = new StringBuffer ();
      buf.append ('\"');
      int len = pValue.length ();
      for (int i = 0; i < len; i++) {
	char ch = pValue.charAt (i);
	if (ch == '\\') {
	  buf.append ('\\');
	  buf.append ('\\');
	}
	else if (ch == '\"') {
	  buf.append ('\\');
	  buf.append ('\"');
	}
	else {
	  buf.append (ch);
	}
      }
      buf.append ('\"');
      return buf.toString ();
    }
  }

  //-------------------------------------
  /**
   *
   * Converts the specified value to an identifier token, escaping it
   * as a string literal if necessary.
   **/
  public static String toIdentifierToken (String pValue)
  {
    // See if it's a valid java identifier
    if (isJavaIdentifier (pValue)) {
      return pValue;
    }

    // Return as a String literal
    else {
      return toStringToken (pValue);
    }
  }

  //-------------------------------------
  /**
   *
   * Returns true if the specified value is a legal java identifier
   **/
  static boolean isJavaIdentifier (String pValue)
  {
    int len = pValue.length ();
    if (len == 0) {
      return false;
    }
    else {
      if (!Character.isJavaIdentifierStart (pValue.charAt (0))) {
	return false;
      }
      else {
	for (int i = 1; i < len; i++) {
	  if (!Character.isJavaIdentifierPart (pValue.charAt (i))) {
	    return false;
	  }
	}
	return true;
      }
    }
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
    return toStringToken ((String) getValue ());
  }

  //-------------------------------------
}
