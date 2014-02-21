package org.opennms.netmgt.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class NodeIdAdapter extends XmlAdapter<Integer, OnmsNode> {

    @Override
    public Integer marshal(final OnmsNode v) throws Exception {
        return v == null? null : v.getId();
    }

    @Override
    public OnmsNode unmarshal(final Integer v) throws Exception {
        if (v == null) return null;
        final OnmsNode node = new OnmsNode();
        node.setId(v);
        return node;
    }

}