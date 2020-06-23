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

import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluator;

/**
 *
 * <p>This is the expression evaluator "adapter" that customizes it
 * for use with the JSP Standard Tag Library.  It uses a
 * VariableResolver implementation that looks up variables from the
 * PageContext and also implements its implicit objects.  It also
 * wraps ELExceptions in JspExceptions that describe the attribute
 * name and value causing the error.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class Evaluator
  implements ExpressionEvaluator
{
  //-------------------------------------
  // Properties
  //-------------------------------------

  //-------------------------------------
  // Member variables
  //-------------------------------------

  /** The singleton instance of the evaluator **/
  static ELEvaluator sEvaluator =
    new ELEvaluator
    (new JSTLVariableResolver ());

  //-------------------------------------
  // ExpressionEvaluator methods
  //-------------------------------------
  /** 
   *
   * Translation time validation of an attribute value.  This method
   * will return a null String if the attribute value is valid;
   * otherwise an error message.
   **/ 
  public String validate (String pAttributeName,
			  String pAttributeValue)
  {
    try {
      sEvaluator.parseExpressionString (pAttributeValue);
      return null;
    }
    catch (ELException exc) {
      return
	MessageFormat.format
	(Constants.ATTRIBUTE_PARSE_EXCEPTION,
	 new Object [] {
	   "" + pAttributeName,
	   "" + pAttributeValue,
	   exc.getMessage ()
	 });
    }
  }

  //-------------------------------------
  /**
   *
   * Evaluates the expression at request time
   **/
  public Object evaluate (String pAttributeName,
			  String pAttributeValue,
			  Class pExpectedType,
			  Tag pTag,
			  PageContext pPageContext,
			  Map functions,
			  String defaultPrefix)
    throws JspException
  {
    try {
      return sEvaluator.evaluate
	(pAttributeValue,
	 pPageContext,
	 pExpectedType,
	 functions,
	 defaultPrefix);
    }
    catch (ELException exc) {
      throw new JspException
	(MessageFormat.format
	 (Constants.ATTRIBUTE_EVALUATION_EXCEPTION,
	  new Object [] {
	    "" + pAttributeName,
	    "" + pAttributeValue,
	    exc.getMessage(),
	    exc.getRootCause()
	  }), exc.getRootCause());
    }
  }

  /** Conduit to old-style call for convenience. */
  public Object evaluate (String pAttributeName,
			  String pAttributeValue,
			  Class pExpectedType,
			  Tag pTag,
			  PageContext pPageContext)
    throws JspException
  {
    return evaluate(pAttributeName,
		   pAttributeValue,
		   pExpectedType,
		   pTag,
		   pPageContext,
		   null,
		   null);
  }


  //-------------------------------------
  // Testing methods
  //-------------------------------------
  /**
   *
   * Parses the given attribute value, then converts it back to a
   * String in its canonical form.
   **/
  public static String parseAndRender (String pAttributeValue)
    throws JspException
  {
    try {
      return sEvaluator.parseAndRender (pAttributeValue);
    }
    catch (ELException exc) {
      throw new JspException
	(MessageFormat.format
	 (Constants.ATTRIBUTE_PARSE_EXCEPTION,
	  new Object [] {
	    "test",
	    "" + pAttributeValue,
	    exc.getMessage ()
	  }));
    }
  }

  //-------------------------------------

}
