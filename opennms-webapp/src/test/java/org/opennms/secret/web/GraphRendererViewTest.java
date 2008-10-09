//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.secret.web;

import java.util.HashMap;

import org.opennms.secret.model.GraphDefinition;
import org.opennms.secret.service.impl.GraphRendererImpl;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import junit.framework.TestCase;

public class GraphRendererViewTest extends TestCase {
    private MockServletContext m_context;
    private MockHttpSession m_session;
    private MockHttpServletRequest m_request;
    private MockHttpServletResponse m_response;
    private GraphRendererView m_view;

    protected void setUp() throws Exception {
        m_context = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
        m_session = new MockHttpSession(m_context);
        resetRequestResponse();
        m_view = new GraphRendererView();
        m_view.setGraphRenderer(new GraphRendererImpl());
        
        m_session.setAttribute("graphDef", new GraphDefinition());
    }

    private void resetRequestResponse() {
        m_request = new MockHttpServletRequest();
        m_request.setSession(m_session);
        m_response = new MockHttpServletResponse();
    }

    protected void tearDown() throws Exception {
    }

    public void testRender() throws Exception {
        m_view.render(new HashMap(), m_request, m_response);
    }

    public void testRenderNullRenderer() throws Exception {
        boolean gotException = false;
        
        m_view.setGraphRenderer(null);

        try {
            m_view.render(new HashMap(), m_request, m_response);
        } catch (IllegalStateException e) {
            assertEquals("Received IllegalStateException as expected; getMessage() on exception does not match",
                    "graph renderer has not been set with setGraphRenderer",
                    e.getMessage());
            gotException = true;
        }
        
        if (!gotException) {
            fail("Expected IllegalStateException, but did not receive it");
        }
    }
    
    public void testRenderNullGraphDef() throws Exception {
        boolean gotException = false;
        
        m_session.setAttribute("graphDef", null);
        
        try {
            m_view.render(new HashMap(), m_request, m_response);
        } catch (IllegalStateException e) {
            assertEquals("Received IllegalStateException as expected; getMessage() on exception does not match",
                    "session has no \"graphDef\" attribute, or it is null",
                    e.getMessage());
            gotException = true;
        }
        
        if (!gotException) {
            fail("Expected IllegalStateException, but did not receive it");
        }
    }
    
    
    public void testRenderBadGraphDef() throws Exception {
        boolean gotException = false;
        
        m_session.setAttribute("graphDef", "not a graphDef");
        
        try {
            m_view.render(new HashMap(), m_request, m_response);
        } catch (IllegalStateException e) {
            assertEquals("Received IllegalStateException as expected; getMessage() on exception does not match",
                    "\"graphDef\" session attribute is not an instance of " +
                    GraphDefinition.class.getName(),
                    e.getMessage());
            gotException = true;
        }
        
        if (!gotException) {
            fail("Expected IllegalStateException, but did not receive it");
        }
    }

}
