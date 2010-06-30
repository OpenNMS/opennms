package org.opennms.netmgt.tools.spectrum;

import java.util.regex.Pattern;

import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;

public class OidMapping {
    private String m_oid;
    private int m_eventVarNum;
    private int m_indexLength;
    
    private static final String s_oidExpr = "^\\.?([0-9]+\\.){3,}[0-9]+$";
    
    public OidMapping(String oid) {
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
        m_eventVarNum = -1;
        m_indexLength = -1;
    }

    public OidMapping(String oid, int eventVarNum, int indexLength) {
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
        m_eventVarNum = eventVarNum;
        m_indexLength = indexLength;
    }

    public String getOid() {
        return m_oid;
    }

    public void setOid(String oid) {
        if (oid == null) {
            throw new IllegalArgumentException("The OID must not be null");
        }
        if (! oid.matches(s_oidExpr)) {
            throw new IllegalArgumentException("The OID must be of the form .1.3.6.1 or 1.3.6.1 and must be at least three octets in length");
        }
        m_oid = oid;
    }

    public int getEventVarNum() {
        return m_eventVarNum;
    }

    public void setEventVarNum(int eventVarNum) {
        m_eventVarNum = eventVarNum;
    }

    public int getIndexLength() {
        return m_indexLength;
    }

    public void setIndexLength(int indexLength) {
        m_indexLength = indexLength;
    }

    public Event makeEvent() {
        Event evt = new Event();
        Mask mask = new Mask();

        // Trap-OID
        Maskelement me = new Maskelement();
        me.setMename("id");

        evt.setMask(mask);
        return evt;
    }
}