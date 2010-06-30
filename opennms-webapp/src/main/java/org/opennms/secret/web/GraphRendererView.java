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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.jrobin.core.RrdException;
import org.opennms.secret.model.GraphDefinition;
import org.opennms.secret.service.GraphRenderer;
import org.springframework.web.servlet.View;

/**
 * <p>GraphRendererView class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class GraphRendererView implements View {

    private GraphRenderer m_renderer;

    private static final String s_contentType = "image/png";

    /** {@inheritDoc} */
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(s_contentType);
        response.setHeader("Cache-control", "no-cache");
        
        InputStream graphStream = getGraphStream(request);

        OutputStream out = getOutputStream(response);
        IOUtils.copy(graphStream, out);
        out.flush();
    }

    private InputStream getGraphStream(HttpServletRequest request) throws IOException, RrdException {
        Object o = request.getSession().getAttribute("graphDef");

        
        if (m_renderer == null) {
            throw new IllegalStateException("graph renderer has not been set with setGraphRenderer");
        }
        if (o == null) {
            throw new IllegalStateException("session has no \"graphDef\" attribute, or it is null");
        }
        if (!(o instanceof GraphDefinition)) {
            throw new IllegalStateException("\"graphDef\" session attribute is not an instance of " +
                    GraphDefinition.class.getName());
        }
        
        GraphDefinition graphDef = (GraphDefinition) o;
        
        InputStream graphStream = m_renderer.getPNG(graphDef);
        return graphStream;
    }

    private OutputStream getOutputStream(HttpServletResponse response) throws IOException, FileNotFoundException {
        OutputStream servletOut = response.getOutputStream();
        OutputStream testOut = new FileOutputStream("/tmp/chart.png");
        TeeOutputStream out = new TeeOutputStream(servletOut, testOut);
        return out;
    }

    /**
     * <p>setGraphRenderer</p>
     *
     * @param renderer a {@link org.opennms.secret.service.GraphRenderer} object.
     */
    public void setGraphRenderer(GraphRenderer renderer) {
        m_renderer = renderer;
    }

    /**
     * <p>getContentType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContentType() {
        return s_contentType;
    }
}
