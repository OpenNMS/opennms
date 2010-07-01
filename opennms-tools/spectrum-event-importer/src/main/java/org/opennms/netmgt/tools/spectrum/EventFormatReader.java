package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
import org.springframework.core.io.Resource;

public class EventFormatReader {
    private Resource m_resource;
    private BufferedReader m_reader;
    
    /**
     *
     * {d "%w- %d %m-, %Y - %T"} - A "bwNetworkRoutingServiceRouteExhaustion" event has occurred, from {t} device, named {m}.
     *
     * "For the actual description, refer the BroadWorks FaultManagementGuide as it may contain variable data."
     *
     * identifier = {I 1}
     * timeStamp = {S 2}
     * alarmName = {S 3}
     * systemName = {S 4}
     * severity = {T severity 5}
     * component = {T component 6}
     * subcomponent = {T subcomponent 7}
     * problemText = {S 8}
     * recommendedActionsText = {S 9}
     * (event [{e}])
     * 
     */
    
    public EventFormatReader(Resource rsrc) throws IOException {
        m_resource = rsrc;
        m_reader = new BufferedReader(new InputStreamReader(m_resource.getInputStream()));
    }
    
    public EventFormat getEventFormat() throws IOException {
        String fileName = m_resource.getFilename();
        String eventCode = "deadbeef";
        Matcher m = Pattern.compile("^Event([0-9A-Fa-f]+)$").matcher(fileName);
        if (m.matches()) {
            eventCode = "0x" + m.group(1);
        }
        
        EventFormat ef = new EventFormat(eventCode);
        StringBuilder contents = new StringBuilder("");
        String thisLine;
        
        while ((thisLine = m_reader.readLine()) != null) {
            contents.append(thisLine).append("\n");
        }
        
        LogUtils.debugf(this, "Got contents for %s:\n%s", eventCode, contents.toString());
        ef.setContents(contents.toString());
        return ef;
    }    
}
