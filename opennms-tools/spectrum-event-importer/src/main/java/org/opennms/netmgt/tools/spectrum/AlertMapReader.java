package org.opennms.netmgt.tools.spectrum;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.FileSystemResource;

public class AlertMapReader {
    FileSystemResource m_resource;
    
    public AlertMapReader(FileSystemResource rsrc) throws IOException {
        m_resource = rsrc;
    }
    
    public List<AlertMapping> getAlertMaps() throws IOException {
        List<AlertMapping> alertMappings = new ArrayList<AlertMapping>();
        Pattern p = Pattern.compile("(?s)^(\\.?([0-9]+\\.){3,}[0-9]+)\\s+(0x[0-9A-Fa-f]+)\\s+(((\\.?([0-9]+\\.){3,}[0-9]+)\\([0-9]+,\\s*[0-9]+\\)(\\s*\\\\\\s*)$\\s*#.*?$)+)");
        Matcher m = p.matcher(getContents());
        while (m.find()) {
            String trapOid = m.group(1);
            String eventId = m.group(2);
            String oidMappings = m.group(4);
            Pattern omp = Pattern.compile("(?s)(\\s*((\\.?([0-9]+\\.){3,}[0-9]+)\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*)\\)(\\s*\\\\\\s*)$)+)");
            Matcher omm = omp.matcher(oidMappings);
            List<OidMapping> oidMappingList = new ArrayList<OidMapping>();
            while (omm.find()) {
                String oid = omm.group(2);
                int varNum = Integer.valueOf(omm.group(4));
                int indexLen = Integer.valueOf(omm.group(5));
                oidMappingList.add(new OidMapping(oid, varNum, indexLen));
            }
            alertMappings.add(new AlertMapping(trapOid, eventId, oidMappingList));
        }
        return alertMappings;
    }
    
    private String getContents() throws IOException {
        FileReader rdr = new FileReader(m_resource.getFile());
        CharBuffer buf = CharBuffer.allocate(65535);
        StringBuilder contents = new StringBuilder(""); 
        while (rdr.read(buf) > -1) {
            contents.append(buf.toString());
            buf.clear();
        }
        return contents.toString();
    }
}
