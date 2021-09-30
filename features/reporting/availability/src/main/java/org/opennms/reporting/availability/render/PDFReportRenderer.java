/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * PDFReportRenderer will transform its input XML into PDF using the supplied
 * XSLT resource.
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class PDFReportRenderer implements ReportRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(PDFReportRenderer.class);

    private static final String LOG4J_CATEGORY = "reports";

    private String m_outputFileName;

    private String m_inputFileName;

    private Resource m_xsltResource;

    private String m_baseDir;

    /**
     * <p>Constructor for PDFReportRenderer.</p>
     */
    public PDFReportRenderer() {
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
    public byte[] render(final String inputFileName, final Resource xsltResource) throws ReportRenderException {
        try {
            return Logging.withPrefix(LOG4J_CATEGORY, new Callable<byte[]>() {
                @Override public byte[] call() throws Exception {
                    LOG.debug("Rendering {} with XSL File {} to byte array", inputFileName, xsltResource.getDescription());

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    render(inputFileName, outputStream, xsltResource);

                    return outputStream.toByteArray();
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportRenderException) throw (ReportRenderException)e;
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final String inputFileName, final OutputStream outputStream, final Resource xsltResource) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    FileInputStream in = null, xslt = null;
                    Reader xsl = null, xml = null;

                    try {
                        LOG.debug("Rendering {} with XSL File {} to OutputStream", inputFileName, xsltResource.getDescription());
    
                        in = new FileInputStream(xsltResource.getFile());
                        xsl = new InputStreamReader(in, StandardCharsets.UTF_8);
                        xslt = new FileInputStream(inputFileName);
                        xml = new InputStreamReader(xslt, StandardCharsets.UTF_8);
            
                        render(xml, outputStream, xsl);
                        return null;
                    } finally {
                        IOUtils.closeQuietly(xml);
                        IOUtils.closeQuietly(xslt);
                        IOUtils.closeQuietly(xsl);
                        IOUtils.closeQuietly(in);
                    }
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportRenderException) throw (ReportRenderException)e;
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final InputStream inputStream, final OutputStream outputStream, final Resource xsltResource) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    LOG.debug("Rendering InputStream with XSL File {} to OutputStream", xsltResource.getDescription());
            
                    FileInputStream xslt = null;
                    Reader xsl = null, xml = null;
                    try {
                        xslt = new FileInputStream(xsltResource.getFile());
                        xsl = new InputStreamReader(xslt, StandardCharsets.UTF_8);
                        xml = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            
                        render(xml, outputStream, xsl);
                        return null;
                    } finally {
                        IOUtils.closeQuietly(xml);
                    }
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportRenderException) throw (ReportRenderException)e;
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final String inputFileName, final String outputFileName, final Resource xsltResource) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
            
                    LOG.debug("Rendering {} with XSL File {} to {} with base directory of {}", m_baseDir, inputFileName, xsltResource.getDescription(), outputFileName);
            
                    FileInputStream in = null, xslt = null;
                    FileOutputStream out = null;
                    Reader xsl = null, xml = null;
                    try {
            
                        xslt = new FileInputStream(xsltResource.getFile());
                        xsl = new InputStreamReader(xslt, StandardCharsets.UTF_8);
                        in = new FileInputStream(m_baseDir + "/" + inputFileName);
                        xml = new InputStreamReader(in, StandardCharsets.UTF_8);
            
                        out = new FileOutputStream(new File(m_baseDir + "/" + outputFileName));
                        
                        render(xml, out, xsl);
                        return null;
                    } finally {
                        IOUtils.closeQuietly(out);
                        IOUtils.closeQuietly(xml);
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(xsl);
                        IOUtils.closeQuietly(xslt);
                    }
                }
            });
        } catch (final Exception e) {
            if (e instanceof ReportRenderException) throw (ReportRenderException)e;
            throw new ReportRenderException(e);
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
    public void render(final Reader in, final OutputStream out, final Reader xslt) throws ReportRenderException {
        try {
            final FopFactory fopFactory = FopFactory.newInstance();
            fopFactory.setStrictValidation(false);
            final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            final TransformerFactory tfact = TransformerFactory.newInstance();
            final Transformer transformer = tfact.newTransformer(new StreamSource(xslt));
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            final StreamSource streamSource = new StreamSource(in);
            transformer.transform(streamSource, new SAXResult(fop.getDefaultHandler()));
        } catch (final Exception e) {
            throw new ReportRenderException(e);
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
