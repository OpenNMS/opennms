package org.opennms.web.alarm.filter;

import org.opennms.web.filter.SubstringFilter;

public class EventParmLikeFilter extends SubstringFilter {

    /** Constant <code>TYPE="parmmatchany"</code> */
    public static final String TYPE = "parmmatchany";
    
    public EventParmLikeFilter(String parm) {
        super(TYPE, "eventParms", "eventParms", parm);
    }

    @Override
    public String getTextDescription() {
        String[] parms = getValue().split("=");
        StringBuffer buffer = new StringBuffer(parms[0] + "= \"");
        buffer.append(parms[parms.length - 1]);
        buffer.append("\"");

        return buffer.toString();
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
    
    /** {@inheritDoc} */
    @Override
    public String getBoundValue(String value) {
        return '%' + value + "(string,text)%";
    }
    
    /** {@inheritDoc} */
    @Override
    public String formatValue(String value) {
        return super.formatValue('%'+value+"(string,text)%");
    }

}
