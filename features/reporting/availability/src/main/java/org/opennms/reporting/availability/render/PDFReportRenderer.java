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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.Resource;

/**
 * PDFReportRenderer will transform its input XML into PDF using the supplied
 * XSLT resource.
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class PDFReportRenderer implements ReportRenderer {

    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private String m_outputFileName;

    private String m_inputFileName;

    private Resource m_xsltResource;

    private String m_baseDir;

    private final ThreadCategory log;

    /**
     * <p>Constructor for PDFReportRenderer.</p>
     */
    public PDFReportRenderer() {
        String oldPrefix = ThreadCategory.getPrefix();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(PDFReportRenderer.class);
        ThreadCategory.setPrefix(oldPrefix);
    }

    /**
     * <p>render</p>
     *
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    public void render() throws ReportRenderException {
        render(m_inputFileName, m_outputFileName, m_xsltResource);
    }

    /** {@inheritDoc} */
    public byte[] render(String inputFileName, Resource xsltResource) throws ReportRenderException {

        if (log.isDebugEnabled())
            log.debug("Rendering " + inputFileName + " with XSL File " + xsltResource.getDescription() + " to byte array");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.render(inputFileName, outputStream, xsltResource);

        return outputStream.toByteArray();
    }

    /** {@inheritDoc} */
    public void render(String inputFileName, OutputStream outputStream, Resource xsltResource) throws ReportRenderException {
        if (log.isDebugEnabled())
            log.debug("Rendering " + inputFileName + " with XSL File " + xsltResource.getDescription() + " to OutputStream");

        FileInputStream in = null, xslt = null;
        try {
            in = new FileInputStream(xsltResource.getFile());
            Reader xsl = new InputStreamReader(in, "UTF-8");
            xslt = new FileInputStream(inputFileName);
            Reader xml = new InputStreamReader(xslt, "UTF-8");

            this.render(xml, outputStream, xsl);
        } catch (IOException ioe) {
            log.fatal("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Error while closing XML stream: " + e.getMessage());
                }
            }
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    log.warn("Error while closing XSLT stream: " + e.getMessage());
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void render(InputStream inputStream, OutputStream outputStream, Resource xsltResource) throws ReportRenderException {
        if (log.isDebugEnabled())
            log.debug("Rendering InputStream with XSL File " + xsltResource.getDescription() + " to OutputStream");

        FileInputStream xslt = null;
        try {
            xslt = new FileInputStream(xsltResource.getFile());
            Reader xsl = new InputStreamReader(xslt, "UTF-8");
            Reader xml = new InputStreamReader(inputStream, "UTF-8");

            this.render(xml, outputStream, xsl);
        } catch (IOException ioe) {
            log.fatal("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    log.warn("Error while closing XSLT stream: " + e.getMessage());
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void render(String inputFileName, String outputFileName, Resource xsltResource) throws ReportRenderException {
        if (log.isDebugEnabled())
            log.debug("Rendering " + inputFileName + " with XSL File " + xsltResource.getDescription() + " to " + outputFileName + " with base directory of " + m_baseDir);

        FileInputStream in = null, xslt = null;
        FileOutputStream out = null;
        try {

            xslt = new FileInputStream(xsltResource.getFile());
            Reader xsl = new InputStreamReader(xslt, "UTF-8");
            in = new FileInputStream(m_baseDir + "/" + inputFileName);
            Reader xml = new InputStreamReader(in, "UTF-8");

            out = new FileOutputStream(new File(m_baseDir + "/" + outputFileName));
            
            this.render(xml, out, xsl);

        } catch (IOException ioe) {
            log.fatal("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Error while closing XML stream: " + e.getMessage());
                }
            }
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    log.warn("Error while closing XSLT stream: " + e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.warn("Error while closing PDF stream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * <p>render</p>
     *
     * @param in a {@link java.io.Reader} object.
     * @param out a {@link java.io.OutputStream} object.
     * @param xslt a {@link java.io.Reader} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    public void render(Reader in, OutputStream out, Reader xslt) throws ReportRenderException {
        try {

            FopFactory fopFactory = FopFactory.newInstance();
            fopFactory.setStrictValidation(false);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            TransformerFactory tfact = TransformerFactory.newInstance();
            Transformer transformer = tfact.newTransformer(new StreamSource(xslt));
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new StreamSource(in), new SAXResult(fop.getDefaultHandler()));

        } catch (TransformerConfigurationException tce) {
            log.fatal("TransformerConfigurationException", tce);
            throw new ReportRenderException(tce);
        } catch (TransformerException te) {
            log.fatal("TransformerException", te);
            throw new ReportRenderException(te);
        } catch (FOPException e) {
            log.fatal("FOPException", e);
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    public void setXsltResource(Resource xsltResource) {
        this.m_xsltResource = xsltResource;
    }

    /** {@inheritDoc} */
    public void setOutputFileName(String outputFileName) {
        this.m_outputFileName = outputFileName;
    }

    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputFileName() {
        return m_outputFileName;
    }

    /** {@inheritDoc} */
    public void setInputFileName(String inputFileName) {
        this.m_inputFileName = inputFileName;
    }

    /** {@inheritDoc} */
    public void setBaseDir(String baseDir) {
        this.m_baseDir = baseDir;
    }

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBaseDir() {
        return m_baseDir;
    }
}
