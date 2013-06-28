/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.report.render;

import java.io.InputStream;
import java.io.OutputStream;

import org.opennms.core.logging.Logging;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.Resource;

/**
 * NullReportRenderer will do nothing.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class NullReportRenderer implements ReportRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(NullReportRenderer.class);

    private static final String LOG4J_CATEGORY = "reports";

    private String m_outputFileName;
    
    private String m_inputFileName;
    
    private String m_baseDir;

    @SuppressWarnings("unused")
    private Resource m_xsltResource;

    /**
     * <p>render</p>
     *
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    @Override
    public void render() throws ReportRenderException {
        Logging.putPrefix(LOG4J_CATEGORY);
        LOG.debug("Do nothing");
        m_outputFileName = m_inputFileName;
    }

    /** {@inheritDoc} */
    @Override
    public void setXsltResource(Resource xsltResource) {
        this.m_xsltResource = xsltResource;
    }

    /** {@inheritDoc} */
    @Override
    public void setOutputFileName(String outputFileName) {
        this.m_outputFileName = outputFileName;
    }
    
    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOutputFileName() {
        return m_outputFileName;
    }

    /** {@inheritDoc} */
    @Override
    public void setInputFileName(String intputFileName) {
        this.m_inputFileName = intputFileName;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setBaseDir(String baseDir){
        this.m_baseDir = baseDir;
    }
    
    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getBaseDir(){
       return m_baseDir;
    }

    /** {@inheritDoc} */
    @Override
    public void render(String inputFileName, String outputFileName,
            Resource xlstResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    /** {@inheritDoc} */
    @Override
    public void render(String inputFileName, OutputStream outputStream,
            Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    /** {@inheritDoc} */
    @Override
    public void render(InputStream inputStream, OutputStream outputStream,
            Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    /** {@inheritDoc} */
    @Override
    public byte[] render(String inputFileName, Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
        return new byte[0];
    }

}
