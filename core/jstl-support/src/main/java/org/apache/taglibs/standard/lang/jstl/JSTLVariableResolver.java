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

import javax.servlet.jsp.PageContext;

/**
 *
 * <p>This is the JSTL-specific implementation of VariableResolver.
 * It looks up variable references in the PageContext, and also
 * recognizes references to implicit objects.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class JSTLVariableResolver
  implements VariableResolver
{
  //-------------------------------------
  /**
   *
   * Resolves the specified variable within the given context.
   * Returns null if the variable is not found.
   **/
  public Object resolveVariable (String pName,
				 Object pContext)
    throws ELException
  {
    PageContext ctx = (PageContext) pContext;

    // Check for implicit objects
    if ("pageContext".equals (pName)) {
      return ctx;
    }
    else if ("pageScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getPageScopeMap ();
    }
    else if ("requestScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getRequestScopeMap ();
    }
    else if ("sessionScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getSessionScopeMap ();
    }
    else if ("applicationScope".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getApplicationScopeMap ();
    }
    else if ("param".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getParamMap ();
    }
    else if ("paramValues".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getParamsMap ();
    }
    else if ("header".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getHeaderMap ();
    }
    else if ("headerValues".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getHeadersMap ();
    }
    else if ("initParam".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getInitParamMap ();
    }
    else if ("cookie".equals (pName)) {
      return ImplicitObjects.
	getImplicitObjects (ctx).
	getCookieMap ();
    }

    // Otherwise, just look it up in the page context
    else {
      return ctx.findAttribute (pName);
    }
  }
					
  //-------------------------------------
}
