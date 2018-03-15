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

package org.apache.taglibs.standard.lang.jstl.test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 *
 * <p>This is a "dummy" implementation of PageContext whose only
 * purpose is to serve the attribute getter/setter API's.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class PageContextImpl
  extends PageContext
{
  //-------------------------------------
  // Properties
  //-------------------------------------

  //-------------------------------------
  // Member variables
  //-------------------------------------

  Map mPage = Collections.synchronizedMap (new HashMap ());
  Map mRequest = Collections.synchronizedMap (new HashMap ());
  Map mSession = Collections.synchronizedMap (new HashMap ());
  Map mApp = Collections.synchronizedMap (new HashMap ());

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public PageContextImpl ()
  {
  }

  //-------------------------------------
  // PageContext methods
  //-------------------------------------
  public void initialize (Servlet servlet,
			  ServletRequest request,
			  ServletResponse response,
			  String errorPageURL,
			  boolean needSession,
			  int bufferSize,
			  boolean autoFlush)
  {
  }

  //-------------------------------------
  public void release ()
  {
  }

  //-------------------------------------
  public void setAttribute (String name,
			    Object attribute)
  {
    mPage.put (name, attribute);
  }

  //-------------------------------------
  public void setAttribute (String name,
			    Object attribute,
			    int scope)
  {
    switch (scope) {
    case PAGE_SCOPE:
      mPage.put (name, attribute);
      break;
    case REQUEST_SCOPE:
      mRequest.put (name, attribute);
      break;
    case SESSION_SCOPE:
      mSession.put (name, attribute);
      break;
    case APPLICATION_SCOPE:
      mApp.put (name, attribute);
      break;
    default:
      throw new IllegalArgumentException  ("Bad scope " + scope);
    }
  }

  //-------------------------------------
  public Object getAttribute (String name)
  {
    return mPage.get (name);
  }

  //-------------------------------------
  public Object getAttribute (String name,
			      int scope)
  {
    switch (scope) {
    case PAGE_SCOPE:
      return mPage.get (name);
    case REQUEST_SCOPE:
      return mRequest.get (name);
    case SESSION_SCOPE:
      return mSession.get (name);
    case APPLICATION_SCOPE:
      return mApp.get (name);
    default:
      throw new IllegalArgumentException  ("Bad scope " + scope);
    }
  }

  //-------------------------------------
  public Object findAttribute (String name)
  {
    if (mPage.containsKey (name)) {
      return mPage.get (name);
    }
    else if (mRequest.containsKey (name)) {
      return mRequest.get (name);
    }
    else if (mSession.containsKey (name)) {
      return mSession.get (name);
    }
    else if (mApp.containsKey (name)) {
      return mApp.get (name);
    }
    else {
      return null;
    }
  }

  //-------------------------------------
  public void removeAttribute (String name)
  {
    if (mPage.containsKey (name)) {
      mPage.remove (name);
    }
    else if (mRequest.containsKey (name)) {
      mRequest.remove (name);
    }
    else if (mSession.containsKey (name)) {
      mSession.remove (name);
    }
    else if (mApp.containsKey (name)) {
      mApp.remove (name);
    }
  }

  //-------------------------------------
  public void removeAttribute (String name,
			       int scope)
  {
    switch (scope) {
    case PAGE_SCOPE:
      mPage.remove (name);
      break;
    case REQUEST_SCOPE:
      mRequest.remove (name);
      break;
    case SESSION_SCOPE:
      mSession.remove (name);
      break;
    case APPLICATION_SCOPE:
      mApp.remove (name);
      break;
    default:
      throw new IllegalArgumentException  ("Bad scope " + scope);
    }
  }

  //-------------------------------------
  public int getAttributesScope (String name)
  {
    if (mPage.containsKey (name)) {
      return PAGE_SCOPE;
    }
    else if (mRequest.containsKey (name)) {
      return REQUEST_SCOPE;
    }
    else if (mSession.containsKey (name)) {
      return SESSION_SCOPE;
    }
    else if (mApp.containsKey (name)) {
      return APPLICATION_SCOPE;
    }
    else {
      return 0;
    }
  }

  //-------------------------------------
  public Enumeration getAttributeNamesInScope (int scope)
  {
    return null;
  }

  //-------------------------------------
  public JspWriter getOut ()
  {
    return null;
  }

  //-------------------------------------
  public HttpSession getSession ()
  {
    return null;
  }

  //-------------------------------------
  public Object getPage ()
  {
    return null;
  }

  //-------------------------------------
  public ServletRequest getRequest ()
  {
    return null;
  }

  //-------------------------------------
  public ServletResponse getResponse ()
  {
    return null;
  }

  //-------------------------------------
  public Exception getException ()
  {
    return null;
  }

  //-------------------------------------
  public ServletConfig getServletConfig ()
  {
    return null;
  }

  //-------------------------------------
  public ServletContext getServletContext ()
  {
    return null;
  }

  //-------------------------------------
  public void forward (String path)
  {
  }

  //-------------------------------------
  public void include (String path)
  {
  }

  //-------------------------------------
  public void handlePageException (Exception exc)
  {
  }

  //-------------------------------------
  public void handlePageException (Throwable exc)
  {
  }

  //-------------------------------------
  
  // Since JSP 2.0
  public void include(java.lang.String relativeUrlPath, boolean flush) {}
  public ExpressionEvaluator getExpressionEvaluator() { return null; }
  public VariableResolver getVariableResolver() { return null; }  

  // Since JSP 2.1
  public javax.el.ELContext getELContext() { return null; }
  
}
