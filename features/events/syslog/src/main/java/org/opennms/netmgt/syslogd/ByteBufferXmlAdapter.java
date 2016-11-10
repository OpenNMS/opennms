package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ByteBufferXmlAdapter extends XmlAdapter<byte[], ByteBuffer> {

    @Override
    public ByteBuffer unmarshal(byte[] bytes) throws Exception {
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public byte[] marshal(ByteBuffer bb) throws Exception {
        return bb.array();
    }

}
