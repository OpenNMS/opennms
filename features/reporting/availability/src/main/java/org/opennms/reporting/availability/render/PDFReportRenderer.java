/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.xmlgraphics.util.MimeConstants.MIME_PDF;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfiguration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
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
        } catch (final ReportRenderException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final String inputFileName, final OutputStream outputStream, final Resource xsltResource) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    LOG.debug("Rendering {} with XSL File {} to OutputStream", inputFileName, xsltResource.getDescription());

                    try (
                        final var in = new FileInputStream(xsltResource.getFile());
                        final var xsl = new InputStreamReader(in, UTF_8);
                        final var xslt = new FileInputStream(inputFileName);
                        final var xml = new InputStreamReader(xslt, UTF_8);
                    ) {
                        render(xml, outputStream, xsl);
                        return null;
                    }
                }
            });
        } catch (final ReportRenderException e) {
            throw e;
        } catch (final Exception e) {
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

                    try (
                        final var xslt = new FileInputStream(xsltResource.getFile());
                        final var xsl = new InputStreamReader(xslt, UTF_8);
                        final var xml = new InputStreamReader(inputStream, UTF_8);
                    ) {
                        render(xml, outputStream, xsl);
                        return null;
                    }
                }
            });
        } catch (final ReportRenderException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void render(final String inputFileName, final String outputFileName, final Resource xsltResource) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    Objects.requireNonNull(m_baseDir);

                    LOG.debug("Rendering {} with XSL File {} to {} with base directory of {}", inputFileName, xsltResource.getDescription(), outputFileName, m_baseDir);

                    try (
                        final var xslt = new FileInputStream(xsltResource.getFile());
                        final var xsl = new InputStreamReader(xslt, UTF_8);
                        final var in = new FileInputStream(Path.of(m_baseDir, inputFileName).toFile());
                        final var xml = new InputStreamReader(in, UTF_8);
                        final var out = new FileOutputStream(Path.of(m_baseDir, outputFileName).toFile());
                    ) {
                        render(xml, out, xsl);
                        return null;
                    }
                }
            });
        } catch (final ReportRenderException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReportRenderException(e);
        }
    }

    /** {@inheritDoc} */
    public void render(final Reader in, final OutputStream out, final Reader xslt) throws ReportRenderException {
        try {
            Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {
                @Override public Void call() throws Exception {
                    Objects.requireNonNull(m_baseDir);

                    final var base = Path.of(getBaseDir());

                    final FopFactory fopFactory = getFopFactoryForBase(base);
                    final Fop fop = fopFactory.newFop(MIME_PDF, out);

                    final TransformerFactory tfact = TransformerFactory.newInstance();

                    final Transformer transformer = tfact.newTransformer(new StreamSource(xslt));
                    transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8.name());

                    final StreamSource streamSource = new StreamSource(in);
                    transformer.transform(streamSource, new SAXResult(fop.getDefaultHandler()));

                    return null;
                }
            });
        } catch (final Exception e) {
            throw new ReportRenderException(e);
        }
    }

    public static FopFactory getFopFactoryForBase(final Path base) throws ConfigurationException {
        final DefaultConfiguration cfg = new DefaultConfigurationBuilder().build(PDFReportRenderer.class.getResourceAsStream("fop-configuration.xml"));
        return new FopFactoryBuilder(base.toAbsolutePath().toUri())
            .setConfiguration(cfg)
            .setStrictFOValidation(false)
            .setComplexScriptFeatures(false)
            .build();
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
