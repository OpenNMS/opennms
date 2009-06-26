package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;



/** Encapsulates all responder filtering functionality. */
public class ResponderFilter extends EqualsFilter<String> {
    public static final String TYPE = "responder";

    //protected String responder;

    public ResponderFilter(String responder) {
        super(TYPE, SQLType.STRING, "ANSWEREDBY", "answeredBy", responder);
        //this.responder = responder;
    }

//    public String getSql() {
//        return (" ANSWEREDBY='" + this.responder + "'");
//    }
//    
//    public String getParamSql() {
//        return (" ANSWEREDBY=?");
//    }
//    
//    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, this.responder);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.responder);
//    }
//
//    public String getTextDescription() {
//        return this.getDescription();
//    }
//
//    public String toString() {
//        return ("<NoticeFactory.ResponderFilter: " + this.getDescription() + ">");
//    }
//
//    public String getResponder() {
//        return (this.responder);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}