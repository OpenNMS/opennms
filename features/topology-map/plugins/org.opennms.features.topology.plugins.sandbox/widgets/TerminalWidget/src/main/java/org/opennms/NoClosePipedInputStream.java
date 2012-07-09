package org.opennms;

import java.io.IOException;
import java.io.PipedInputStream;

public class NoClosePipedInputStream extends PipedInputStream {

	public NoClosePipedInputStream(NoClosePipedOutputStream in) throws IOException {
		super(in);
	}

	public NoClosePipedInputStream() {
		super();
	}

	@Override
	public void close() {
		//DO NOTHING
	}
	
}
