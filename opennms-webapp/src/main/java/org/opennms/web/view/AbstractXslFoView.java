/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Driver;
import org.springframework.web.servlet.view.xslt.AbstractXsltView;

/**
 * Convenient superclass for views rendered to PDF (or other FOP output
 * format) using XSLT-FO stylesheet.
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public abstract class AbstractXslFoView extends AbstractXsltView {

    /** default renderer will be PDF unless overridden */
    private static final int DEFAULT_RENDERER = Driver.RENDER_PDF;

    private int renderer = DEFAULT_RENDERER;

    Driver driver;

    /**
     * Perform the actual transformation, writing to the HTTP response via the
     * FOP Driver.
     */

    protected void doTransform(Map model, javax.xml.transform.Source source,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        driver = new Driver();
        driver.setRenderer(renderer);
        driver.setOutputStream(response.getOutputStream());
        Result result = new SAXResult(driver.getContentHandler());

        // delegate to the superclass for the actual output having constructed
        // the Result
        doTransform(source, getParameters(request), result,
                    response.getCharacterEncoding());
    }

    /**
     * Sets the renderer to use for this FOP transformation. See the available
     * types in org.apache.fop.apps.Driver. Defaults to Driver.RENDER_PDF
     * 
     * @param renderer
     *            the type of renderer
     */
    public void setRenderer(int renderer) {
        this.renderer = renderer;
    }

}
