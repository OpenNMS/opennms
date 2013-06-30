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

package org.opennms.web.controller.admin.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

public final class FormatterView implements View {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormatterView.class);


    private SystemReportFormatter m_formatter = null;
    private InternalResourceView m_view = null;

    public FormatterView(final SystemReportFormatter formatter) {
        super();
        m_formatter = formatter;
        
        if (m_formatter == null || !m_formatter.needsOutputStream() || m_formatter.getContentType() == null) {
            m_view = new InternalResourceView("/admin/support/systemReportList.htm");
        }
        
        LOG.debug("formatter = {}, view = {}", m_formatter, m_view);
    }

    @Override
    public String getContentType() {
        if (m_view != null) return m_view.getContentType();
        if (m_formatter == null) return "application/octet-stream";
        return m_formatter.getContentType();
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (m_view == null) {
            String fileName = getFileName(request);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        } else {
            final String outputParameter = request.getParameter("output");
            m_formatter.setOutput(outputParameter);
        }
        if (model.containsKey("report")) {
            final SystemReportInfo info = (SystemReportInfo)model.get("report");
            LOG.debug("found report = {}", info);
            final OutputStream output;

            if (m_view == null) {
                output = response.getOutputStream();
            } else {
                output = new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {}
                };
            }

            try {
                LOG.debug("beginning output");
                m_formatter.setOutputStream(output);
                m_formatter.begin();

                for (final SystemReportPlugin plugin : info.getPlugins()) {
                    LOG.debug("running plugin {}", plugin);
                    m_formatter.write(plugin);
                    output.flush();
                }
                
                LOG.debug("finishing output");
                m_formatter.end();
            } catch (final Throwable e) {
                LOG.warn("Error while formatting system report output", e);
                throw new SystemReportException(e);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } else {
            LOG.info("Invalid form input: {}", model);
            throw new SystemReportException("Form input was invalid.");
        }
        LOG.debug("done");
        if (m_view != null) {
            m_view.render(model, request, response);
        }
    }

    public String getFileName(final HttpServletRequest request) {
        final String outputParameter = request.getParameter("output");
        String fileName = null;
        if (outputParameter != null && !outputParameter.matches("^[\\s]*$")) {
            fileName = new File(outputParameter).getName().replaceAll("[^[:alnum:]\\.]", "");
        } else {
            fileName = "opennms-system-report." + m_formatter.getExtension();
        }
        return fileName;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("formatter", m_formatter)
            .toString();
    }
}