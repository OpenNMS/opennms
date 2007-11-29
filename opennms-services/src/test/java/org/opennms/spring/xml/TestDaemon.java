package org.opennms.spring.xml;

public class TestDaemon {
    
    private String m_attr;

    public String getAttr() {
        return m_attr;
    }

    public void setAttr(String attr) {
        m_attr = attr;
    }

    @Override
    public String toString() {
        return ""+m_attr;
    }
    
}
