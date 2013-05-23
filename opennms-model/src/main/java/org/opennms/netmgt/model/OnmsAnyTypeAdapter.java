package org.opennms.netmgt.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class OnmsAnyTypeAdapter extends XmlAdapter<Object, Object> {
    @Override public Object unmarshal(final Object o) throws Exception {
        return o;
    }
    @Override public Object marshal(final Object o) throws Exception {
        return o;
    }
}
