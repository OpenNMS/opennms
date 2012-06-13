package org.opennms.sandbox;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

public class PipedStreamTest {

	@Test
	public void testPipeLine() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream(output);
		String command = "ls -al";
		output.write(command.getBytes());
		byte[] buf = new byte[6];
		input.read(buf);
		String outputstr = new String(buf);
		assertEquals(command, outputstr);
	}

}
