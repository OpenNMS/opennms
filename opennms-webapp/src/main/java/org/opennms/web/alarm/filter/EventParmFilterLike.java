package org.opennms.web.alarm.filter;

import org.opennms.web.filter.SubstringFilter;

public class EventParmFilterLike extends SubstringFilter {

    /** Constant <code>TYPE="msgmatchany"</code> */
    public static final String TYPE = "parmmatchany";
    
    public EventParmFilterLike(String parm) {
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
