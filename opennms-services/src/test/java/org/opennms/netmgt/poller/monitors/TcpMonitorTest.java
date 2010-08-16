package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ServerSocketFactory;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv6NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.ServiceMonitor;

public class TcpMonitorTest {
	
	private ServiceMonitor m_sm = null;

	@Before
	public void registerTcpUrlHandler() {
		m_sm = new TcpMonitor();
	}
	
	
	@Test
	public void parseParametersFromUrl() throws MalformedURLException, UnsupportedEncodingException {
		String urlStr = "onmsTCP://localhost:9999?banner=test&retry=7&timeout=1000";
		URL url = new URL(urlStr);
		assertEquals("test", TcpMonitor.parseParameterFromUrl(url, "banner", null));
		assertEquals(7, TcpMonitor.parseParameterFromUrl(url, "retry", 0));
		assertEquals(1000, TcpMonitor.parseParameterFromUrl(url, "timeout", 3000));
	}
	
	@Test
	public void pollIpv4MonitoredService() throws IOException {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		
		parameters.put("timeout", "1000");
		parameters.put("port", "9999");
		parameters.put("retry", "0");
		
		doPoll(new TestSocketListener("127.0.0.1"), parameters, "onmsTCP://127.0.0.1");
	}
	
	@Test
	public void pollIpv6MonitoredService() throws IOException {

		Map<String, Object> parameters = new TreeMap<String, Object>();
		
		parameters.put("timeout", "1000");
		parameters.put("port", "9999");
		parameters.put("retry", "0");

		//test both the abbreviated and long form of the local IPv6 address
		
		//test without URL parameters
		doPoll(new TestSocketListener("fe80::1%lo0"), parameters, "onmsTCP://[fe80::1%lo0]");
		
		//test with URL parameters
		doPoll(new TestSocketListener("fe80:0:0:0:0:0:0:1%lo0"), null, "onmsTCP://[fe80:0:0:0:0:0:0:1%lo0]:9999?timeout=3000&retry=0");
	}

	private void doPoll(final TestSocketListener sl, final Map<String, Object>parameters, final String urlStr) throws UnknownHostException {
		
		MonitoredService svc = new MonitoredService() {

			private InetAddress m_inetAddr = InetAddress.getByName(sl.getIpAddr());
			private NetworkInterface m_interface = new IPv6NetworkInterface(m_inetAddr);
			private String m_urlStr = urlStr;

			public InetAddress getAddress() {
				return m_inetAddr ;
			}

			public String getIpAddr() {
				return m_inetAddr.getHostAddress();
			}

			public NetworkInterface getNetInterface() {
				return m_interface;
			}

			public int getNodeId() {
				return 1;
			}

			public String getNodeLabel() {
				return "localhost";
			}

			public String getSvcName() {
				return "TCP";
			}

			public String getSvcUrl() {
				return m_urlStr;
			}
			
		};
				
		sl.startListener();
		
		PollStatus ps = m_sm.poll(svc, parameters);
		
        assertEquals(PollStatus.SERVICE_AVAILABLE, ps.getStatusCode());

        sl.stopListener();
	}

	
	private class TestSocketListener {
		
		private ServerSocket m_socket = null;
		private Thread m_thread = null;
		private String m_banner = null;
		
		public TestSocketListener(String address) throws UnknownHostException, IOException {
			m_socket = ServerSocketFactory.getDefault().createServerSocket(9999, 2, InetAddress.getByName(address));
		}
		
		public void startListener() {
			m_thread = new Thread(new Runnable() {

				public void run() {
					try {
						m_socket.setSoTimeout(0);
						Socket s = m_socket.accept();
						
						if (m_banner != null) {
							Thread.sleep(100);
							s.getOutputStream().write(m_banner.getBytes());
						}
						
					} catch (IOException e) {
						throw new RuntimeException(e);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							m_socket.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
			}, "TcpMonitorTestService");
			
			m_thread.start();
		}
		
		public void stopListener() {
			if (m_socket != null) {
				try {
					if (m_socket.isClosed()) {
						m_socket.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		public String getIpAddr() {
			return m_socket.getInetAddress().getHostAddress();
		}

	}
}
