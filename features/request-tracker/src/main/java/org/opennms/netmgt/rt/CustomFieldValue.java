package org.opennms.netmgt.rt;

import java.io.Serializable;

public class CustomFieldValue implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -1023360304505747481L;
    private String m_value;

    public CustomFieldValue() {
        // Empty default constructor
    }

    public CustomFieldValue(final String value) {
        setValue(value);
    }

    public void setValue(String value) {
        m_value = value;
    }

    public String getValue() {
        return m_value;
    }

    public String toString() {
        return m_value;
    }
}
