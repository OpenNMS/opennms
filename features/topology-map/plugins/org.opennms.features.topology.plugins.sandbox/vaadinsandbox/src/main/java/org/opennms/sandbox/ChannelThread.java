package org.opennms.sandbox;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;

public class ChannelThread extends Thread{

	private InputStream input = null;
	private OutputStream output = null;
	private ClientChannel channel = null;
	private ClientSession session = null;
	
	public ChannelThread(){}
	
	public void run(){
		openChannel();
	}
	
	private void openChannel(){
		try {
			channel = session.createChannel("shell");
			channel.setIn(input);
			channel.setOut(output);
			channel.setErr(output);
			channel.open();
			//channel.waitFor(ClientChannel.CLOSED, 0);
			//session.close(false);
		} catch (Exception e){
			e.printStackTrace();
		}finally {
			//client.stop();
		}
	}
	
	public void setInputStream(InputStream input){
		this.input = input;
	}
	
	public void setOutputStream(OutputStream output){
		this.output = output;
	}
	
	public void setClientChannel(ClientChannel channel){
		this.channel = channel;
	}
	
	public void setClientSession(ClientSession session){
		this.session = session;
	}
}
