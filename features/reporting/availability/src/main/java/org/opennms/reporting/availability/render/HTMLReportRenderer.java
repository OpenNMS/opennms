/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * HTMLReportRenderer will transform its input XML into HTML using the
 * supplied XSLT resource.
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class HTMLReportRenderer implements ReportRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(HTMLReportRenderer.class);

    private static final String LOG4J_CATEGORY = "reports";

    private String m_outputFileName;

    private String m_inputFileName;

    private Resource m_xsltResource;

    private String m_baseDir;

    /**
     * <p>Constructor for HTMLReportRenderer.</p>
     */
    public HTMLReportRenderer() {
        // TODO This shoud wrap the methods I think
        Logging.putPrefix(LOG4J_CATEGORY);
    }

    /**
     * <p>render</p>
     *
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    @Override
    public void render() throws ReportRenderException {
        render(m_inputFileName, m_outputFileName, m_xsltResource);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] render(String inputFileName, Resource xsltResource) throws ReportRenderException {


        LOG.debug("Rendering {} with XSL File {} to byte array", inputFileName, xsltResource.getDescription());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.render(inputFileName, outputStream, xsltResource);

        return outputStream.toByteArray();
    }

    /** {@inheritDoc} */
    @Override
    public void render(String inputFileName, OutputStream outputStream, Resource xsltResource) throws ReportRenderException {

        LOG.debug("Rendering {} with XSL File {} to OutputStream", inputFileName, xsltResource.getDescription());

        FileInputStream in = null, xslt = null;
        try {
            in = new FileInputStream(xsltResource.getFile());
            Reader xsl = new InputStreamReader(in, "UTF-8");
            xslt = new FileInputStream(inputFileName);
            Reader xml = new InputStreamReader(xslt, "UTF-8");

            this.render(xml, outputStream, xsl);
        } catch (IOException ioe) {
            LOG.error("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing XML stream: {}", e.getMessage());
                }
            }
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing XSLT stream: {}", e.getMessage());
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(InputStream inputStream, OutputStream outputStream, Resource xsltResource) throws ReportRenderException {

        LOG.debug("Rendering InputStream with XSL File {} to OutputStream", xsltResource.getDescription());

        FileInputStream xslt = null;
        try {
            xslt = new FileInputStream(xsltResource.getFile());
            Reader xsl = new InputStreamReader(xslt, "UTF-8");
            Reader xml = new InputStreamReader(inputStream, "UTF-8");

            this.render(xml, outputStream, xsl);
        } catch (IOException ioe) {
            LOG.error("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing XSLT stream: {}", e.getMessage());
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(String inputFileName, String outputFileName, Resource xsltResource) throws ReportRenderException {

        LOG.debug("Rendering {} with XSL File {} to {} with base directory of {}", m_baseDir, inputFileName, xsltResource.getDescription(), outputFileName);

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
            LOG.error("IOException ", ioe);
            throw new ReportRenderException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing XML stream: {}", e.getMessage());
                }
            }
            if (xslt != null) {
                try {
                    xslt.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing XSLT stream: {}", e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.warn("Error while closing PDF stream: {}", e.getMessage());
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
            TransformerFactory tfact = TransformerFactory.newInstance();
            Transformer transformer = tfact.newTransformer(new StreamSource(xslt));
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new StreamSource(in), new StreamResult(out));
        } catch (TransformerConfigurationException tce) {
            LOG.error("TransformerConfigurationException", tce);
            throw new ReportRenderException(tce);
        } catch (TransformerException te) {
            LOG.error("TransformerException", te);
            throw new ReportRenderException(te);
        }
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
    public void setInputFileName(String inputFileName) {
        this.m_inputFileName = inputFileName;
    }

    /** {@inheritDoc} */
    @Override
    public void setBaseDir(String baseDir) {
        this.m_baseDir = baseDir;
    }

    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getBaseDir() {
        return m_baseDir;
    }
}
