package org.opennms.netmgt.tools.spectrum;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class SpectrumUtils {
    
    /**
     * 
     * @param inToken the substitution token from the Spectrum event format
     * @return the OpenNMS event XML equivalent for the inToken
     */
    public static String translateFormatSubstToken(String inToken) {
        if (inToken == null) {
            throw new IllegalArgumentException("The input token must be non-null");
        }
        String outToken = inToken;
        
        if (inToken.startsWith("{d")) {
            outToken = "%eventtime%";
        } else if (inToken.equals("{t}")) {
            outToken = "%asset[manufacturer]%";
        } else if (inToken.equals("{m}")) {
            outToken = "%nodelabel%";
        } else if (inToken.equals("{e}")) {
            outToken = "%uei%";
        } else if (inToken.startsWith("{I") || inToken.startsWith("{S") || inToken.startsWith("{T")) {
            Matcher m = Pattern.compile("^\\{\\s*[IST]\\s+(\\w+\\s+)?(\\d+)\\s*\\}$").matcher(inToken);
            if (m.matches()) {
                outToken = "%parm[#" + m.group(2) + "]%";
            }
        }
        
        return outToken;
    }
    
    public static List<Varbindsdecode> translateAllEventTables(EventFormat ef, String eventTablePath) throws IOException {
        List<Varbindsdecode> vbds = new ArrayList<Varbindsdecode>();
        Pattern pat = Pattern.compile("^\\{\\s*T\\s+(\\w+)\\s+(\\d+)\\s*\\}");
        for (String token : ef.getSubstTokens()) {
            Matcher mat = pat.matcher(token);
            if (mat.matches()) {
                LogUtils.debugf(SpectrumUtils.class, "Token [%s] looks like an event-table, processing it", token);
                Resource tableFile = new FileSystemResource(eventTablePath + File.pathSeparator + mat.group(1));
                EventTableReader etr = new EventTableReader(tableFile);
                LogUtils.debugf(SpectrumUtils.class, "Attempting to load event-table [%s] from [%s]", mat.group(1), tableFile);
                EventTable et = etr.getEventTable();
                String parmId = "parm[#" + mat.group(2) + "]";
                Varbindsdecode vbd = translateEventTable(et, parmId);
                LogUtils.debugf(SpectrumUtils.class, "Loaded event-table [%s] with parm-ID [%s], with %d mappings", et.getTableName(), parmId, vbd.getDecodeCount());
                vbds.add(translateEventTable(et, parmId));
            } else {
                LogUtils.debugf(SpectrumUtils.class, "Token [%s] does not look like an event-table, skipping it", token);
            }
        }
        LogUtils.debugf(SpectrumUtils.class, "Translated %d event-tables for event-code [%s]", vbds.size(), ef.getEventCode());
        return vbds;
    }
    
    public static Varbindsdecode translateEventTable(EventTable et, String parmId) {
        Varbindsdecode vbd = new Varbindsdecode();
        vbd.setParmid(parmId);
        for (Integer key : et.keySet()) {
            Decode decode = new Decode();
            decode.setVarbindvalue(key.toString());
            decode.setVarbinddecodedstring(et.get(key));
            vbd.addDecode(decode);
        }
        return vbd;
    }
    
}
