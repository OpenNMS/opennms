package org.opennms.jicmp.jna;

public abstract class SocketUtils {
	public static void assertSocketValid(final int socket) {
		if (socket == 0) {
			throw new InvalidSocketException("Socket not initialized!");
		} else if (socket == -1) {
			throw new InvalidSocketException("Socket already closed!");
		}
	}
}
