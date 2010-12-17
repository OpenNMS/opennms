package org.opennms.systemreport.formatters;

import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;
import org.xml.sax.helpers.AttributesImpl;

public class XmlSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private TransformerHandler m_handler = null;

    public String getName() {
        return "xml";
    }
    
    public String getDescription() {
        return "XML text";
    }

    public String getContentType() {
        return "text/xml";
    }

    public String getExtension() {
        return "xml";
    }

    public boolean canStdout() {
        return true;
    }

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
                LogUtils.errorf(this, e, "Unable to create XML stream writer.");
                m_handler = null;
            }

            try {
                m_handler.startDocument();
                m_handler.startElement("", "", "systemReportPlugins", null);
            } catch (final Exception e) {
                LogUtils.warnf(this, e, "Unable to start document.");
                m_handler = null;
            }
        }

        if (m_handler == null) {
            LogUtils.warnf(this, "Unable to write, no handler defined!");
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
            LogUtils.warnf(this, e, "An error occurred while attempting to write XML data.");
        }
    }
    
    public void end() {
        if (m_handler != null) {
            try {
                m_handler.endElement("", "", "systemReportPlugins");
                m_handler.endDocument();
            } catch (final Exception e) {
                LogUtils.warnf(this, e, "Unable to end document.");
            }
        }
    }
}
