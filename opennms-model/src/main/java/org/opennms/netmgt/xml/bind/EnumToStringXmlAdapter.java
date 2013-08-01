package org.opennms.netmgt.xml.bind;

import java.util.EnumSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 */
public class EnumToStringXmlAdapter<T extends Enum<T>> extends XmlAdapter<String, T> {

    private Class<T> m_class;
    private T m_defaultValue;

    protected EnumToStringXmlAdapter(Class<T> clazz, T defaultValue) {
        m_class = clazz;
        m_defaultValue = defaultValue;
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(T nodeType) throws Exception {
        return nodeType.toString();
    }

    /** {@inheritDoc} */
    @Override
    public T unmarshal(String status) throws Exception {
        for (T type : EnumSet.allOf(m_class)) {
            if (type.toString().equalsIgnoreCase(status)) {
                return type;
            }
        }
        return m_defaultValue;
    }

}