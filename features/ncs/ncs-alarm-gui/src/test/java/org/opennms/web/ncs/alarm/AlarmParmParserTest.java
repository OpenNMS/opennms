package org.opennms.web.ncs.alarm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AlarmParmParserTest {

    @Test
    public void testParseParms() {
        String parms = "url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=rtc(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)";
        
        assertEquals("http://localhost:8980/opennms/rtc/post/Network+Interfaces", getParm(parms, "url"));
    }

    private String getParm(String eventParms, String parm) {
        String retVal = null;
        if(eventParms.contains(parm + "=")){
            String[] colonSplit = eventParms.split(";");
            for(int i = 0; i < colonSplit.length; i++) {
                if(colonSplit[i].contains(parm + "=")) {
                    String[] tempArr = colonSplit[i].split("=");
                    retVal = tempArr[tempArr.length - 1].replace("(string,text)", "");
                }
            }
            return retVal;
        }else {
            return null;
        }
        
    }

}
