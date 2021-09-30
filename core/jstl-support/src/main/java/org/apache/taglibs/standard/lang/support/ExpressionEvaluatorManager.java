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

package org.apache.taglibs.standard.lang.support;

import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.lang.jstl.Coercions;
import org.apache.taglibs.standard.lang.jstl.ELException;
import org.apache.taglibs.standard.lang.jstl.Logger;

/**
 * <p>A conduit to the JSTL EL.  Based on...</p>
 * 
 * <p>An implementation of the ExpressionEvaluatorManager called for by
 * the JSTL rev1 draft.  This class is responsible for delegating a
 * request for expression evaluating to the particular, "active"
 * ExpressionEvaluator for the given point in the PageContext object
 * passed in.</p>
 *
 * @author Shawn Bayern
 */
public class ExpressionEvaluatorManager { 

    //*********************************************************************
    // Constants

    public static final String EVALUATOR_CLASS =
        "org.apache.taglibs.standard.lang.jstl.Evaluator";
    // private static final String EVALUATOR_PARAMETER =
    //    "javax.servlet.jsp.jstl.temp.ExpressionEvaluatorClass";

    //*********************************************************************
    // Internal, static state

    private static HashMap nameMap = new HashMap();
    private static Logger logger = new Logger(System.out);

    //*********************************************************************
    // Public static methods

    /** 
     * Invokes the evaluate() method on the "active" ExpressionEvaluator
     * for the given pageContext.
     */ 
    public static Object evaluate(String attributeName, 
                                  String expression, 
                                  Class expectedType, 
                                  Tag tag, 
                                  PageContext pageContext) 
           throws JspException
    {

        // the evaluator we'll use
        ExpressionEvaluator target = getEvaluatorByName(EVALUATOR_CLASS);

        // delegate the call
        return (target.evaluate(
            attributeName, expression, expectedType, tag, pageContext));
    }

    /** 
     * Invokes the evaluate() method on the "active" ExpressionEvaluator
     * for the given pageContext.
     */ 
    public static Object evaluate(String attributeName, 
                                  String expression, 
                                  Class expectedType, 
                                  PageContext pageContext) 
           throws JspException
    {

        // the evaluator we'll use
        ExpressionEvaluator target = getEvaluatorByName(EVALUATOR_CLASS);

        // delegate the call
        return (target.evaluate(
            attributeName, expression, expectedType, null, pageContext));
    }

    /**
     * Gets an ExpressionEvaluator from the cache, or seeds the cache
     * if we haven't seen a particular ExpressionEvaluator before.
     */
    public static
	    ExpressionEvaluator getEvaluatorByName(String name)
            throws JspException {

        Object oEvaluator = nameMap.get(name);
        if (oEvaluator != null) {
            return ((ExpressionEvaluator) oEvaluator);
        }
        try {
            synchronized (nameMap) {
                oEvaluator = nameMap.get(name);
                if (oEvaluator != null) {
                    return ((ExpressionEvaluator) oEvaluator);
                }
                ExpressionEvaluator e = (ExpressionEvaluator)
                    Class.forName(name).newInstance();
                nameMap.put(name, e);
                return (e);
            }
        } catch (ClassCastException ex) {
            // just to display a better error message
            throw new JspException("invalid ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (ClassNotFoundException ex) {
            throw new JspException("couldn't find ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (IllegalAccessException ex) {
            throw new JspException("couldn't access ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (InstantiationException ex) {
            throw new JspException(
                "couldn't instantiate ExpressionEvaluator: " +
                ex.toString(), ex);
        }
    }

    /** Performs a type conversion according to the EL's rules. */
    public static Object coerce(Object value, Class classe)
            throws JspException {
	try {
	    // just delegate the call
	    return Coercions.coerce(value, classe, logger);
	} catch (ELException ex) {
	    throw new JspException(ex);
	}
    }

} 
