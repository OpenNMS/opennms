/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        if (plugin.getFullOutputOnly()) return;
        
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

                atts = new AttributesImpl();
                atts.addAttribute("", "", "key", "CDATA", entry.getKey());

                m_handler.startElement("", "", "entry", atts);

                final String value = getResourceText(entry.getValue());
                if (value != null) {
                    m_handler.startCDATA();
                    m_handler.characters(value.toCharArray(), 0, value.length());
                    m_handler.endCDATA();
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
