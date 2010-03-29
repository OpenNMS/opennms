//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.report.render;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.reporting.availability.render.ReportRenderException;
import org.opennms.reporting.availability.render.ReportRenderer;
import org.springframework.core.io.Resource;

/**
 * NullReportRenderer will do nothing.
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class NullReportRenderer implements ReportRenderer {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private String m_outputFileName;
    
    private String m_inputFileName;
    
    private String m_baseDir;

    private Resource m_xsltResource;

    public void render() throws ReportRenderException {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(NullReportRenderer.class);
        log.debug("Do nothing");
        m_outputFileName = m_inputFileName;
    }

    public void setXsltResource(Resource xsltResource) {
        this.m_xsltResource = xsltResource;
    }

    public void setOutputFileName(String outputFileName) {
        this.m_outputFileName = outputFileName;
    }
    
    public String getOutputFileName() {
        return m_outputFileName;
    }

    public void setInputFileName(String intputFileName) {
        this.m_inputFileName = intputFileName;
    }
    
    public void setBaseDir(String baseDir){
        this.m_baseDir = baseDir;
    }
    
    public String getBaseDir(){
       return m_baseDir;
    }

    public void render(String inputFileName, String outputFileName,
            Resource xlstResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    public void render(String inputFileName, OutputStream outputStream,
            Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    public void render(InputStream inputStream, OutputStream outputStream,
            Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
    }

    public byte[] render(String inputFileName, Resource xsltResource)
            throws org.opennms.reporting.availability.render.ReportRenderException {
        return new byte[0];
    }

}
