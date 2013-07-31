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

package org.opennms.systemreport.formatters;

import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;
import org.xml.sax.helpers.AttributesImpl;

public class XmlSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(XmlSystemReportFormatter.class);
    private TransformerHandler m_handler = null;

    @Override
    public String getName() {
        return "xml";
    }
    
    @Override
    public String getDescription() {
        return "Simple output in XML text format";
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public String getExtension() {
        return "xml";
    }

    @Override
    public boolean canStdout() {
        return true;
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        if (!hasDisplayable(plugin)) return;
        
        if (m_handler == null) {
            try {
                StreamResult streamResult = new StreamResult(getOutputStream());
                SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                m_handler = tf.newTransformerHandler();
                Transformer serializer = m_handler.getTransformer();
                serializer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "entry");
                m_handler.setResult(streamResult);
            } catch (final Exception e) {
                LOG.error("Unable to create XML stream writer.", e);
                m_handler = null;
            }

            try {
                m_handler.startDocument();
                m_handler.startElement("", "", "systemReportPlugins", null);
            } catch (final Exception e) {
                LOG.warn("Unable to start document.", e);
                m_handler = null;
            }
        }

        if (m_handler == null) {
            LOG.warn("Unable to write, no handler defined!");
            return;
        }
        try {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "", "name", "CDATA", plugin.getName());
            atts.addAttribute("", "", "description", "CDATA", plugin.getDescription());
            m_handler.startElement("", "", "plugin", atts);
            for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
                final boolean displayable = isDisplayable(entry.getValue());

                atts = new AttributesImpl();
                atts.addAttribute("", "", "key", "CDATA", entry.getKey());

                if (!displayable) {
                    atts.addAttribute("", "", "skipped", "CDATA", "true");
                }

                m_handler.startElement("", "", "entry", atts);
                if (displayable) {
                    final String value = getResourceText(entry.getValue());
                    if (value != null) {
                        m_handler.startCDATA();
                        m_handler.characters(value.toCharArray(), 0, value.length());
                        m_handler.endCDATA();
                    }
                }
                m_handler.endElement("", "", "entry");
            }
            m_handler.endElement("", "", "plugin");
        } catch (final Exception e) {
            LOG.warn("An error occurred while attempting to write XML data.", e);
        }
    }
    
    @Override
    public void end() {
        if (m_handler != null) {
            try {
                m_handler.endElement("", "", "systemReportPlugins");
                m_handler.endDocument();
            } catch (final Exception e) {
                LOG.warn("Unable to end document.", e);
            }
        }
    }
}
