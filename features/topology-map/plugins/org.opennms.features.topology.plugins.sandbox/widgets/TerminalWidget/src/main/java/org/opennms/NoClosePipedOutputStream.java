package org.opennms;

import java.io.IOException;
import java.io.PipedOutputStream;

public class NoClosePipedOutputStream extends PipedOutputStream {

	public NoClosePipedOutputStream(){
		super();
	}
	
	public NoClosePipedOutputStream(NoClosePipedInputStream out) throws IOException {
		super(out);
	}
	
	@Override
	public void close(){
		//DO NOTHING
	}

}
