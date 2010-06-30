/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * December 19th, 2009 jonathan@opennms.org
 * 
 * Updated to render to an outputStream
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.reporting.availability.render;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.Resource;

/**
 * ReportRenderer is the interface for rendering xml reports to pdf, pdf with
 * embedded svg and html
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public interface ReportRenderer {

    /**
     * <p>render</p>
     *
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render() throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param outputFileName a {@link java.lang.String} object.
     * @param xlstResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(String inputFileName, String outputFileName, Resource xlstResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param outputStream a {@link java.io.OutputStream} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(String inputFileName, OutputStream outputStream, Resource xsltResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputStream a {@link java.io.InputStream} object.
     * @param outputStream a {@link java.io.OutputStream} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(InputStream inputStream, OutputStream outputStream, Resource xsltResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @return an array of byte.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    byte[] render(String inputFileName, Resource xsltResource) throws ReportRenderException;

    /**
     * <p>setOutputFileName</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     */
    void setOutputFileName(String outputFileName);
    
    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getOutputFileName();
    
    /**
     * <p>setInputFileName</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     */
    void setInputFileName(String inputFileName);

    /**
     * <p>setXsltResource</p>
     *
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     */
    void setXsltResource(Resource xsltResource);
    
    /**
     * <p>setBaseDir</p>
     *
     * @param baseDir a {@link java.lang.String} object.
     */
    void setBaseDir(String baseDir);
    
    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getBaseDir();

}
