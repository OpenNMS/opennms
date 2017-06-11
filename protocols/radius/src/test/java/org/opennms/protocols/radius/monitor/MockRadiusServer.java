package org.opennms.protocols.radius.monitor;

import java.net.InetSocketAddress;

import org.opennms.test.mock.MockUtil;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

public class MockRadiusServer extends RadiusServer {
		@Override
		public String getSharedSecret(InetSocketAddress arg0) {
			return "testing123";
		}

		@Override
		public String getUserPassword(String usename) {
			return "password";
		}
		@Override
		public RadiusPacket accessRequestReceived(AccessRequest ar, InetSocketAddress client)
				throws RadiusException{
			MockUtil.println(ar.getAuthProtocol());
			return super.accessRequestReceived(ar, client);
		}
		@Override
		public void start(boolean hasAuth,boolean hasAcct){
			MockUtil.println("Mock radius server starting");
			super.start(hasAuth, hasAcct);
		}
}
