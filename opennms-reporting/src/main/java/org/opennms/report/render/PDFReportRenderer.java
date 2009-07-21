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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.MessageHandler;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.Resource;

/**
 * PDFReportRenderer will transform its input.xml into pdf using the supplied
 * xslt resource. 
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */

public class PDFReportRenderer implements ReportRenderer {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private String m_outputFileName;

    private String m_inputFileName;

    private Resource m_xsltResource;

    private String m_baseDir;

    public void render() throws ReportRenderException {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(PDFReportRenderer.class);
        Logger avalonLogger = new Log4JLogger(log);

        try {

            log.debug("XSL File " + m_xsltResource.getDescription());

            Reader xsl = new FileReader(m_xsltResource.getFile());
            
            log.debug("Base directory " + m_baseDir);

            log.debug("file to render" + m_inputFileName);

            Reader xml = new FileReader(m_baseDir + m_inputFileName);

            log.debug("ouput File " + m_outputFileName);

            FileOutputStream pdfOutputStream = new FileOutputStream(
                                                                    new File(m_baseDir + 
                                                                             m_outputFileName));

            Logger nullLogger = new NullLogger();
            MessageHandler.setScreenLogger(nullLogger);
            MessageHandler.setOutputMethod(MessageHandler.NONE);

            Driver driver = new Driver();
            driver.setLogger(avalonLogger);
            driver.setOutputStream(pdfOutputStream);
            driver.setRenderer(Driver.RENDER_PDF);
            TransformerFactory tfact = TransformerFactory.newInstance();
            Transformer transformer = tfact.newTransformer(new StreamSource(
                                                                            xsl));
            transformer.transform(new StreamSource(xml),
                                  new SAXResult(driver.getContentHandler()));
            pdfOutputStream.close();

        } catch (IOException ioe) {
            log.fatal("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } catch (TransformerConfigurationException tce) {
            log.fatal("transformerConfigurationException ", tce);
            throw new ReportRenderException(tce);
        } catch (TransformerException te) {
            log.fatal("TransformerException ", te);
            throw new ReportRenderException(te);
        }
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

    public void setInputFileName(String inputFileName) {
        this.m_inputFileName = inputFileName;

    }
    
    public void setBaseDir(String baseDir) {
        this.m_baseDir = baseDir;

    }
    
    public String getBaseDir() {
        return m_baseDir;

    }

}
