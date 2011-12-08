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
        StringBuffer buffer = new StringBuffer("parm containing \"");
        buffer.append(getValue());
        buffer.append("\"");

        return buffer.toString();
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

}
