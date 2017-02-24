package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface ByteBufferParser<T> {
	CompletableFuture<T> parse(ByteBuffer buffer);
}
