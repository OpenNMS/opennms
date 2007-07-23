package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.savarese.rocksaw.net.RawSocket;
import org.savarese.vserv.tcpip.ICMPEchoPacket;
import org.savarese.vserv.tcpip.ICMPPacket;
import org.savarese.vserv.tcpip.OctetConverter;

public class Pinger {
	private static int defaultTimeout = 10000;
	private int id = Thread.currentThread().hashCode();
	private int sequence = 1;
	private RawSocket socket;
	private byte[] sendData;
	private ICMPEchoPacket sendPacket = new ICMPEchoPacket(1);
	static final int PACKET_SIZE = 84;

	public Pinger() throws IOException {
		this(defaultTimeout);
	}
	public Pinger(int timeout) throws IOException {
		this.socket = new RawSocket();
		this.socket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("icmp"));
	    setSocketTimeout(timeout);

		sendData = new byte[PACKET_SIZE];
	    sendPacket.setIPHeaderLength(5);
	    sendPacket.setICMPDataByteLength(56);
		sendPacket.setData(sendData);
		
		Thread pingerThread = new Thread() {
			public void run() {
				processEchoRequests();
			}
		};
	}

	public class Response {
		private long endNanos;
		private byte[] data;
		private ICMPEchoPacket packet;
		
		public Response(int size) {
			packet = new ICMPEchoPacket(1);
		    packet.setIPHeaderLength(5);
		    packet.setICMPDataByteLength(56);
			data = new byte[size];
			packet.setData(data);
		}

		byte[] getData() {
			return data;
		}

		boolean isValid() {
			return this.packet.getType() == ICMPPacket.TYPE_ECHO_REPLY && this.packet.getIdentifier() == getIdentifier();
		}

		int getSequenceNumber() {
			return packet.getSequenceNumber();
		}

		long getStartNanos() {
	        return OctetConverter.octetsToLong(data, packet.getCombinedHeaderByteLength());
		}

		long getEndNanos() {
			return this.endNanos;
		}
		
		void setEndNanos(long endNanos) {
			this.endNanos = endNanos;
		}
	}

	protected void processEchoRequests() {
		Response response = new Response(PACKET_SIZE);
		do {
			
		} while (response.isValid());
	}
	
	private void setSocketTimeout(int timeout) throws SocketException {
		try {
	        socket.setSendTimeout(timeout);
	        socket.setReceiveTimeout(timeout);
	      } catch(java.net.SocketException se) {
	        socket.setUseSelectTimeout(true);
	        socket.setSendTimeout(timeout);
	        socket.setReceiveTimeout(timeout);
	      }
	}

	private int getIdentifier() {
		return id;
	}

	public synchronized void ping(InetAddress host, int count, PingCallback pc)
			throws IOException {
		// throw new UnsupportedOperationException("ping(host, count, callback) not implemented");
		Response response = new Response(PACKET_SIZE);
		
		for (int sequence = 0; sequence < count; sequence++) {
			sendEchoRequest(host, this.getIdentifier(), sequence);
		}
	}

	public long ping(InetAddress host) {
		throw new UnsupportedOperationException("ping(host) not implemented");
	}

	private void sendEchoRequest(InetAddress host, int identifier, int sequence) throws IOException {
		sendPacket.setType(ICMPPacket.TYPE_ECHO_REQUEST);
		sendPacket.setCode(0);
		sendPacket.setIdentifier(identifier);
		sendPacket.setSequenceNumber(sequence);

		OctetConverter.longToOctets(System.nanoTime(), sendData, sendPacket.getCombinedHeaderByteLength());
		sendPacket.computeICMPChecksum();

		socket.write(host, sendData, sendPacket.getIPHeaderByteLength(), sendPacket.getICMPPacketByteLength());
	}

}
