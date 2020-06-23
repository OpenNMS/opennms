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
 * <p>An expression representing one or more unary operators on a
 * value
 * 
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class UnaryOperatorExpression
  extends Expression
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property operator

  UnaryOperator mOperator;
  public UnaryOperator getOperator ()
  { return mOperator; }
  public void setOperator (UnaryOperator pOperator)
  { mOperator = pOperator; }

  //-------------------------------------
  // property operators

  List mOperators;
  public List getOperators ()
  { return mOperators; }
  public void setOperators (List pOperators)
  { mOperators = pOperators; }

  //-------------------------------------
  // property expression

  Expression mExpression;
  public Expression getExpression ()
  { return mExpression; }
  public void setExpression (Expression pExpression)
  { mExpression = pExpression; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public UnaryOperatorExpression (UnaryOperator pOperator,
				  List pOperators,
				  Expression pExpression)
  {
    mOperator = pOperator;
    mOperators = pOperators;
    mExpression = pExpression;
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
    buf.append ("(");
    if (mOperator != null) {
      buf.append (mOperator.getOperatorSymbol ());
      buf.append (" ");
    }
    else {
      for (int i = 0; i < mOperators.size (); i++) {
	UnaryOperator operator = (UnaryOperator) mOperators.get (i);
	buf.append (operator.getOperatorSymbol ());
	buf.append (" ");
      }
    }
    buf.append (mExpression.getExpressionString ());
    buf.append (")");
    return buf.toString ();
  }

  //-------------------------------------
  /**
   *
   * Evaluates to the literal value
   **/
  public Object evaluate (Object pContext,
			  VariableResolver pResolver,
			  Map functions,
			  String defaultPrefix,
			  Logger pLogger)
    throws ELException
  {
    Object value = mExpression.evaluate (pContext, pResolver, functions,
					 defaultPrefix, pLogger);
    if (mOperator != null) {
      value = mOperator.apply (value, pContext, pLogger);
    }
    else {
      for (int i = mOperators.size () - 1; i >= 0; i--) {
	UnaryOperator operator = (UnaryOperator) mOperators.get (i);
	value = operator.apply (value, pContext, pLogger);
      }
    }
    return value;
  }

  //-------------------------------------
}
