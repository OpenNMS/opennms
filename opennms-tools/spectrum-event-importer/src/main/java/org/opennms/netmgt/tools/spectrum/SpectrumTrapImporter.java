/**
 * 
 */

package org.opennms.netmgt.tools.spectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @author OpenNMS <http://www.opennms.org/>
 *
 */
public class SpectrumTrapImporter {
    private List<AlertMapping> m_alertMappings;
    private List<EventDisposition> m_eventDispositions;
    private Map<String,EventFormat> m_eventFormats;
    
    /**
     * Something like uei.opennms.org/import/Spectrum/
     */
    private String m_baseUei = null;
    private Resource m_customEventsDir = null;
    
    private void initialize() throws Exception {
        Resource alertMapResource = new UrlResource(m_customEventsDir.getURI() + "/AlertMap");
        Resource eventDispResource = new UrlResource(m_customEventsDir.getURI() + "/EventDisp");
        
        m_alertMappings = new AlertMapReader(alertMapResource).getAlertMappings();
        m_eventDispositions = new EventDispositionReader(eventDispResource).getEventDispositions();
        loadEventFormats();
    }
    
    private void loadEventFormats() throws Exception {
        String csEvFormatDirName = m_customEventsDir.getURI() + "/CsEvFormat";
        Map<String,EventFormat> formats = new HashMap<String,EventFormat>();
        for (AlertMapping mapping : m_alertMappings) {
            if (formats.containsKey(mapping.getEventCode())) {
                LogUtils.debugf(this, "Already have read an event-format for event-code [%s], not loading again", mapping.getEventCode());
                continue;
            }
            String formatFileName = csEvFormatDirName + "/Event" + (mapping.getEventCode().substring(2));
            try {
                EventFormatReader reader = new EventFormatReader(new UrlResource(formatFileName));
                formats.put(mapping.getEventCode(), reader.getEventFormat());
            } catch (FileNotFoundException fnfe) {
                LogUtils.infof(this, "Unable to load an event-format for event-code [%s] from [%s]; continuing without it", mapping.getEventCode(), formatFileName);
                continue;
            }
        }
        m_eventFormats = formats;
    }
    
    public void afterPropertiesSet() throws Exception {
        if (m_baseUei == null) {
            throw new IllegalStateException("The baseUei property must be set");
        }
        if (m_customEventsDir == null) {
            throw new IllegalStateException("The customEventsDir property must be set");
        }
        
        initialize();
    }
    
    public void setBaseUei(String baseUei) {
        if (baseUei == null) {
            throw new IllegalArgumentException("The base-UEI must be non-null");
        }
        m_baseUei = baseUei;
    }
    
    public String getBaseUei() {
        return m_baseUei;
    }
    
    public void setCustomEventsDir(Resource customEventsDir) throws IOException {
        if (! customEventsDir.getFile().isDirectory()) {
            throw new IllegalArgumentException("The customEventsDir property must refer to a directory");
        }
        m_customEventsDir = customEventsDir;
    }
    
    public Resource getCustomEventsDir() {
        return m_customEventsDir;
    }
}
