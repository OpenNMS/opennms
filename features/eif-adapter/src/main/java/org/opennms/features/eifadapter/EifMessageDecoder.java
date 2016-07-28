package org.opennms.features.eifadapter;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.camel.component.netty4.ChannelHandlerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class EifMessageDecoder extends MessageToMessageDecoder<ByteBuf> implements ChannelHandlerFactory {

    private final StringBuilder buffer = new StringBuilder();
    private final Charset charset = Charset.defaultCharset();

    @Override
    public ChannelHandler newChannelHandler() {
        return new EifMessageDecoder();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        buffer.append(msg.toString(charset));
        if ( buffer.toString().contains("<START>>") && buffer.toString().contains(";END") ) {
            int eifStart = buffer.indexOf("<START>>");
            int eifEnd = buffer.lastIndexOf(";END");
            StringBuilder eif = new StringBuilder(buffer.substring(eifStart,eifEnd+4));
            buffer.delete(eifStart,eifEnd+4);
            List<Event> e = EifParser.translateEifToOpenNMS(eif);
            if (e != null) {
                Log eifEvents = new Log();
                e.forEach(event -> eifEvents.addEvent(event));
                out.add(eifEvents);
            }
        }
    }

}
