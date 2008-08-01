package org.opennms.netmgt.tl1d;

import java.net.Socket;

public interface Tl1Client {

	void start();

	void stop();

	String getHost();

	void setHost(String host);

	int getPort();

	void setPort(int port);

	Socket getTl1Socket();

	void setTl1Socket(Socket tl1Socket);

	Thread getSocketReader();

	void setSocketReader(Thread socketReader);

}