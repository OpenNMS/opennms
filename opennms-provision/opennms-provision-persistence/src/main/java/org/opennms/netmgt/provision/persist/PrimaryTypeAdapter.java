package org.opennms.netmgt.provision.persist;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.netmgt.model.PrimaryType;

public class PrimaryTypeAdapter extends XmlAdapter<String, PrimaryType> {

    @Override
    public String marshal(final PrimaryType type) throws Exception {
        return type == null? null : type.getCode();
    }

    @Override
    public PrimaryType unmarshal(final String typeCode) throws Exception {
        return typeCode == null? null : PrimaryType.get(typeCode);
    }

}
