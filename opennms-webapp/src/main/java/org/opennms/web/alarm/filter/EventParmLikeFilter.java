package org.opennms.web.alarm.filter;

import org.opennms.web.filter.SubstringFilter;

public class EventParmLikeFilter extends SubstringFilter {

    /** Constant <code>TYPE="parmmatchany"</code> */
    public static final String TYPE = "parmmatchany";
    
    public EventParmLikeFilter(String parm) {
        super(TYPE, "eventParms", "eventParms", parm + "(string,text)");
    }

    @Override
    public String getTextDescription() {
        String strippedType = getValue().replace("(string,text)", "");
        String[] parms = strippedType.split("=");
        StringBuffer buffer = new StringBuffer(parms[0] + "=\"");
        buffer.append(parms[parms.length - 1]);
        buffer.append("\"");

        return buffer.toString();
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
    
    @Override
    public String getDescription() {
        return TYPE + "=" + getValueString().replace("(string,text)", "");
        
    }

}
