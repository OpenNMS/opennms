package org.opennms.rrd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos;

public class PerfDataReceiver {

	private volatile static Thread m_listenerThread;

	public static void main(String[] args) {
		Thread listener = null;

		Runtime.getRuntime().addShutdownHook(createShutdownHook());

		int port = 8999;
		
		if (args.length < 1) {
			System.err.println("Defaulting to port: 8999.  To change, pass valid port value as first argument.");
		} else {
			port = Integer.valueOf(args[0]);
		}
		
		System.err.println("Ready to receive OpenNMS QOS Data on TCP Port:"+String.valueOf(port)+"...");
		try {
			listener = createListenerThread(port);
			listener.start();
			listener.join();
		} catch (Throwable t) {
			System.err.print(t.getLocalizedMessage() + "\n\n" + t);
		}
		
	}
	
	public static Thread createShutdownHook() {
		Thread t = new Thread() {
			@Override
			public void run() {
				System.out.println("\nHave a nice day! :)");
				Runtime.getRuntime().halt(0);
			}
		};
		return t;
	}

	public static Thread createListenerThread(final int port) {
		m_listenerThread = new Thread() {
			public void run() {
				this.setName("fail");
				try {
					ServerSocket ssocket = new ServerSocket(port);
					ssocket.setSoTimeout(0);
					while (true) {
						try {
							Socket socket = ssocket.accept();
							InputStream is = socket.getInputStream();
							PerformanceDataProtos.PerformanceDataReadings messages = PerformanceDataProtos.PerformanceDataReadings.parseFrom(is);
							for (PerformanceDataProtos.PerformanceDataReading message : messages.getMessageList()) {
								StringBuffer values = new StringBuffer();
								values.append("{ ");
								for (int i = 0; i < message.getValueCount(); i++) {
									if (i != 0) {
										values.append(", ");
									}
									values.append(message.getValue(i));
								}
								values.append(" }");
								System.out
										.println("Message received: { "
												+ "path: \""
												+ message.getPath() + "\", "
												+ "owner: \""
												+ message.getOwner() + "\", "
												+ "timestamp: \""
												+ message.getTimestamp()
												+ "\", " + "values: "
												+ values.toString() + " }");

							}
						} catch (SocketTimeoutException e) {
							System.err.println(e.getLocalizedMessage());
							if (this.isInterrupted()) {
								System.err.println("Interrupted.");
								this.setName("notfailed");
								return;
							}
						} catch (IOException e) {
							System.err.println(e.getLocalizedMessage());
						}
					}
					
					
				} catch (IOException e) {
					System.err.println(e.getLocalizedMessage());
				} catch (Throwable e) {
					System.err.println(e.getLocalizedMessage());
				}
			}
		};
		
		return m_listenerThread;

	}
}
