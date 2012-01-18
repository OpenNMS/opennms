package org.opennms.web.alarm.filter;

import org.opennms.web.filter.NoSubstringFilter;

public class NegativeEventParmLikeFilter extends NoSubstringFilter {

    public static final String TYPE = "noparmmatchany";
    
    public NegativeEventParmLikeFilter(String value) {
        super(TYPE, "eventParms", "eventParms", value + "(string,text)");
    }

    @Override
    public String getTextDescription() {
        String strippedType = getValue().replace("(string,text)", "");
        String[] parms = strippedType.split("=");
        StringBuffer buffer = new StringBuffer(parms[0] + " is not \"");
        buffer.append(parms[parms.length - 1]);
        buffer.append("\"");

        return buffer.toString();
    }
    
    @Override
    public String getDescription() {
        return TYPE + "=" + getValueString().replace("(string,text)", "");
        
    }

}
