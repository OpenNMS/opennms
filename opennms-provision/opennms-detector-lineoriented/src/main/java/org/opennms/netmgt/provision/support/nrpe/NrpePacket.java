/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.support.nrpe;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.CRC32;

public class NrpePacket {
	/** Constant <code>QUERY_PACKET=1</code> */
	public static final short QUERY_PACKET = 1;

	/** Constant <code>RESPONSE_PACKET=2</code> */
	public static final short RESPONSE_PACKET = 2;

	/** Constant <code>HELLO_COMMAND="_NRPE_CHECK"</code> */
	public static final String HELLO_COMMAND = "_NRPE_CHECK";

	/** Constant <code>PACKET_VERSION_2=2</code> */
	public static final short PACKET_VERSION_2 = 2;

	/** Constant <code>MAX_PACKETBUFFER_LENGTH=1024</code> */
	public static final int MAX_PACKETBUFFER_LENGTH = 1024;

	/** Constant <code>PACKET_SIZE=2 + // packet version, 16 bit integer
			2 + // packet type, 16 bit integer
			4 + // crc32, 32 bit unsigned integer
			2 + // result code
			MAX_PACKETBUFFER_LENGTH</code> */
	public static final int PACKET_SIZE =
			2 + // packet version, 16 bit integer
			2 + // packet type, 16 bit integer
			4 + // crc32, 32 bit unsigned integer
			2 + // result code
			MAX_PACKETBUFFER_LENGTH; // buffer

	/** Constant <code>DEFAULT_PADDING=2</code> */
	public static final int DEFAULT_PADDING = 2;

	private short m_version = PACKET_VERSION_2;

	private short m_type;

	private short m_resultCode;

	private String m_buffer;

	/**
	 * <p>Constructor for NrpePacket.</p>
	 */
	public NrpePacket() {
	}

	/**
	 * <p>Constructor for NrpePacket.</p>
	 *
	 * @param type a short.
	 * @param resultCode a short.
	 * @param buffer a {@link java.lang.String} object.
	 */
	public NrpePacket(final short type, final short resultCode, final String buffer) {
		m_type = type;
		m_resultCode = resultCode;
		m_buffer = buffer;
	}

	/**
	 * <p>getVersion</p>
	 *
	 * @return a short.
	 */
	public short getVersion() {
		return m_version;
	}

	/**
	 * <p>setVersion</p>
	 *
	 * @param version a short.
	 */
	public void setVersion(final short version) {
		m_version = version;
	}

	/**
	 * <p>getType</p>
	 *
	 * @return a short.
	 */
	public short getType() {
		return m_type;
	}

	/**
	 * <p>setType</p>
	 *
	 * @param type a short.
	 */
	public void setType(final short type) {
		m_type = type;
	}

	/**
	 * <p>getResultCode</p>
	 *
	 * @return a short.
	 */
	public short getResultCode() {
		return m_resultCode;
	}

	/**
	 * <p>setResultCode</p>
	 *
	 * @param resultCode a short.
	 */
	public void setResultCode(final short resultCode) {
		m_resultCode = resultCode;
	}

	/**
	 * <p>getBuffer</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBuffer() {
		return m_buffer;
	}

	/**
	 * <p>setBuffer</p>
	 *
	 * @param buffer a {@link java.lang.String} object.
	 */
	public void setBuffer(String buffer) {
		m_buffer = buffer;
	}

	/**
	 * <p>receivePacket</p>
	 *
	 * @param i a {@link java.io.InputStream} object.
	 * @param padding a int.
	 * @return a {@link org.opennms.netmgt.provision.support.nrpe.NrpePacket} object.
	 * @throws org.opennms.netmgt.provision.support.nrpe.NrpeException if any.
	 * @throws java.io.IOException if any.
	 */
	public static NrpePacket receivePacket(final InputStream i, final int padding) throws NrpeException, IOException {

	    final NrpePacket p = new NrpePacket();

	    final byte[] packet = new byte[PACKET_SIZE + padding];

		int j, k;
		for (k = 0; (j = i.read()) != -1; k++) {
			packet[k] = (byte) j;
		}

		if (k < PACKET_SIZE) {
			throw new NrpeException("Received packet is too small.  " + "Received " + k + ", expected at least " + PACKET_SIZE);
		}

		p.m_version = (short) ((positive(packet[0]) << 8) + positive(packet[1]));
		p.m_type = (short) ((positive(packet[2]) << 8) + positive(packet[3]));
		long crc_l = ((long) positive(packet[4]) << 24)
				+ ((long) positive(packet[5]) << 16)
				+ ((long) positive(packet[6]) << 8)
				+ ((long) positive(packet[7]));
		p.m_resultCode = (short) ((positive(packet[8]) << 8) + positive(packet[9]));

		packet[4] = 0;
		packet[5] = 0;
		packet[6] = 0;
		packet[7] = 0;

		final CRC32 crc = new CRC32();
		crc.update(packet);

		final long crc_calc = crc.getValue();
		if (crc_calc != crc_l) {
			throw new NrpeException("CRC mismatch: " + crc_calc + " vs. " + crc_l);
		}

		final byte[] buffer = new byte[MAX_PACKETBUFFER_LENGTH];
		System.arraycopy(packet, 10, buffer, 0, buffer.length);

		p.m_buffer = new String(buffer);
		if ((j = p.m_buffer.indexOf(0)) > 0) {
			p.m_buffer = p.m_buffer.substring(0, j);
		}

		return p;
	}

	/**
	 * <p>positive</p>
	 *
	 * @param b a byte.
	 * @return a int.
	 */
	public static int positive(final byte b) {
		if (b < 0) {
			return b + 256;
		} else {
			return b;
		}
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "Version: " + m_version + "\n" + "Type: " + m_type + "\n"
				+ "Result Code: " + m_resultCode + "\n" + "Buffer: " + m_buffer;
	}
	
	/**
	 * <p>buildPacket</p>
	 *
	 * @param padding a int.
	 * @return an array of byte.
	 */
	public byte[] buildPacket(final int padding) {
		SecureRandom random;
		
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (final NoSuchAlgorithmException e) {
			random = null;
		}
		
		return buildPacket(padding, random);
	}

	/**
	 * <p>buildPacket</p>
	 *
	 * @param padding a int.
	 * @param random a {@link java.security.SecureRandom} object.
	 * @return an array of byte.
	 */
	public byte[] buildPacket(final int padding, final SecureRandom random) {
	    final byte[] packet = new byte[PACKET_SIZE + padding];
		final byte[] buffer = m_buffer.getBytes();

		if (random != null) {
			random.nextBytes(packet);
		} else {
			// If we can't do random, at least zero out the packet
			for (int i = 10 + buffer.length; i < packet.length; i++) {
				packet[i] = 0;
			}
		}

		packet[0] = (byte) ((m_version >> 8) & 0xff);
		packet[1] = (byte) (m_version & 0xff);

		packet[2] = (byte) ((m_type >> 8) & 0xff);
		packet[3] = (byte) (m_type & 0xff);

		// These will get filled in later when we compute the CRC.
		packet[4] = 0;
		packet[5] = 0;
		packet[6] = 0;
		packet[7] = 0;

		packet[8] = (byte) ((m_resultCode >> 8) & 0xff);
		packet[9] = (byte) (m_resultCode & 0xff);

		System.arraycopy(buffer, 0, packet, 10, buffer.length);

		// Make sure that the character after m_buffer is null
		if ((10 + buffer.length) < PACKET_SIZE - 1) {
			packet[10 + buffer.length] = 0;
		}

		// Make sure that the last character in the buffer is null.
		packet[PACKET_SIZE - 1] = 0;

		final CRC32 crc = new CRC32();
		crc.update(packet);

		final long crc_l = crc.getValue();

		packet[4] = (byte) ((crc_l >> 24) & 0xff);
		packet[5] = (byte) ((crc_l >> 16) & 0xff);
		packet[6] = (byte) ((crc_l >> 8) & 0xff);
		packet[7] = (byte) (crc_l & 0xff);

		return packet;
	}
}
