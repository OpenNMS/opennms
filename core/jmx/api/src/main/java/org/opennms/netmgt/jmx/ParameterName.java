package org.opennms.netmgt.jmx;

/**
 * Defines parameter names which should be available as
 * parameters in service definition in collectd-configuratin.xml.
 *
 * */
public enum ParameterName {
    COLLECTION("collection"),
    RETRY("retry"),
    FRIENDLY_NAME("friendly-name"),
    PORT("port"),
    USE_MBEAN_NAME_FOR_RRDS("use-mbean-name-for-rrds");

    private final String m_value;

    private ParameterName(String value) {
        m_value = value;
    }

    @Override
    public String toString() {
        return m_value;
    }
}
