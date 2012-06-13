package org.opennms.sandbox;

import java.io.IOException;
import java.io.InputStream;

public class ConsoleInputStream extends InputStream {

	protected byte[] buf;
	protected int count;
	protected int pos;
	

	@Override
	public synchronized int read(byte b[], int off, int len) throws IOException{
		if (b == null){
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		
		int c = read();
		if (c == -1) {
			return -1;
		}
		b[off] = (byte)c;
		
		int i = 1;
		try {
			for (; i < len; i++) {
				c = read();
				if (c == -1) {
					break;
				}
				b[off + i] = (byte)c;
			}
		} catch (IOException ee){}
		return i;
	}


	@Override
	public int read() throws IOException {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}
	
	@Override
	public void close(){
		
	}
	public void setBuffer(byte[] buf){
		this.pos = 0;
		this.count = buf.length;
		this.buf = buf;	
	}
}
