/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;

/**
 * @author MS043660
 */
public class EventdTrapStresser {

	private static final String DEFAULT_IPADDRESS = "127.0.0.1";
	private static InetAddress m_agentAddress;
	private static InetAddress m_trapSink;
	private static Integer m_trapPort = Integer.valueOf(162);
	private static String m_trapCommunity = "public";
	private static Double m_trapRate = Double.valueOf(100); // seconds
	private static Integer m_trapCount = Integer.valueOf(500);
	private static Integer m_batchDelay = Integer.valueOf(1); // seconds
	private static Integer m_batchSize = m_trapCount;
	private static int m_batchCount = 1;
	private static String m_postgresAddress;

	private static SnmpTrapBuilder builderV1;
	private static SnmpTrapBuilder builderV2;

	@SuppressWarnings("unused")
	private static long m_sleepMillis = 0;
	private static Long totalTimeNeededToSend;
	private static Connection connection;
	private static long totalElapsedTime=0L;

	public static void main(String[] args) throws Exception {

		m_trapRate = Double.valueOf(args[0]); // seconds
		m_trapCount = Integer.valueOf(args[1]);
		m_agentAddress = InetAddrUtils.addr(args[2]);
		m_batchCount = Integer.valueOf(args[3]);
		if (args.length == 5)
			m_postgresAddress = args[4];

		setIpAddresses();
		// if (m_postgresAddress != null)
		// dataBaseConnect();

		m_batchSize = m_trapCount;

		System.out.println();
		System.out.println("m_trapRate : " + m_trapRate);
		System.out.println("m_trapCount : " + m_trapCount);
		System.out.println("m_agentAddress : " + m_agentAddress);
		System.out.println("m_batchCount : " + m_batchCount);

		System.out.println("Commencing the Eventd Stress Test...");

		builderV1 = createBuilderV1();
		builderV2 = createBuilderV2();

		executeStressTest();
		// if (m_postgresAddress != null)
		// eventsTableCount();

	}

	private static void eventsTableCount() {
		try {
			Thread.sleep(1000);
			Statement stmt = null;
			if (connection != null) {
				stmt = connection.createStatement();
				String sql = "Select count(*) from events";
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					System.out
							.println("Total number of traps events inserted into opennms database :"
									+ rs.getInt(1));
				}
				System.out.println();
				stmt.close();
				connection.close();
			}
		} catch (Exception e) {
			System.out.println("Counting events failed!!");
		}

	}

	private static void executeStressTest() {
		try {
			stressEventd();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void setIpAddresses() {
		try {
			if (!m_agentAddress.getHostAddress().equalsIgnoreCase(
					DEFAULT_IPADDRESS)) {
				m_trapSink = m_agentAddress;
			} else {
				m_trapSink = InetAddress.getByName(DEFAULT_IPADDRESS);
			}
		} catch (UnknownHostException e1) {
			System.exit(1);
		}
	}

	public static void stressEventd() throws Exception {

		if (m_batchCount < 1) {
			throw new IllegalArgumentException(
					"Batch count of < 1 is not allowed.");
		} else if (m_batchCount > m_trapCount) {
			throw new IllegalArgumentException(
					"Batch count is > than trap count.");
		}

		totalTimeNeededToSend = (long) (m_trapCount.doubleValue() / m_trapRate
				.doubleValue()) * m_batchCount;
		System.out.println("Estimated time to send Complete: "
				+ (m_trapCount.doubleValue() / m_trapRate.doubleValue())
				* m_batchCount + " seconds");
		System.out.println("Sending " + m_trapCount + " traps in "
				+ m_batchCount + " batches with a batch interval of "
				+ m_batchDelay.toString() + " seconds...");

		long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
		int trapsSent = sendTraps(builderV1, builderV2, startTimeInMillis);
		long StopTimeInMillis = Calendar.getInstance().getTimeInMillis();

		systemReport(0L, trapsSent, totalElapsedTime);
	}

	private static void systemReport(long beginMillis, int trapsSent,
			long stopTimeInMillis) {

		System.out.println("  Traps sent: " + trapsSent);
		long totalMillis = stopTimeInMillis - beginMillis;
		Long totalSeconds = totalMillis / 1000L;
		System.out.println("Total Elapsed time (secs): " + totalSeconds);

		Long finalTime = (totalSeconds - totalTimeNeededToSend);
		if (finalTime.intValue() < 0) {
			System.out.println("Time left over than estimated time(secs): "
					+ Math.abs(finalTime));
		} else {
			System.out
					.println("Extra Time taken to finish(secs): " + finalTime);
		}

		System.out.println();
	}

	private static void dataBaseConnect() {
		connection = null;
		Statement stmt = null;
		try {
			connection = DriverManager
					.getConnection("jdbc:postgresql://" + m_postgresAddress
							+ ":5432/opennms", "opennms", "opennms");

			if (connection != null) {
				System.out.println("Connected to database now!");
				stmt = connection.createStatement();
				String sql = "DELETE FROM events";
				stmt.executeUpdate(sql);
				System.out.println("Deleted rows in Events Table successfully");
				stmt.close();
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			System.out.println("Connection Failed");
			return;
		}
	}

	private static int sendTraps(SnmpTrapBuilder builderV1,
			SnmpTrapBuilder builderV2, long beginMillis)
			throws IllegalStateException, InterruptedException, SQLException {

		m_sleepMillis = 0;
		int totalTrapsSent = 0;
		Double currentRate = 0.0;
		Long batchTrapsSent = 0L;
		Long elapsedMillis = 0L;
		Long batchElapsedMillis = 0L;
		Long batchBegin = 0L;
		int remainingTraps = 0;
		totalElapsedTime=0L;

		for (int i = 1; i <= m_batchCount; i++) {

			batchBegin = Calendar.getInstance().getTimeInMillis();
			batchTrapsSent = 0L;
			System.out.println("Sending batch " + i + " of "
					+ Integer.valueOf(m_batchCount) + " batches of "
					+ m_batchSize.intValue() + " traps at the rate of "
					+ m_trapRate.toString() + " traps/sec...");
			System.out.println("Estimated time to send: "
					+ m_batchSize.doubleValue() / m_trapRate.doubleValue()
					+ " seconds");

			while (batchTrapsSent.intValue() < m_batchSize.intValue()) {

				if (currentRate <= m_trapRate || batchElapsedMillis == 0) {
					batchTrapsSent += sendTrap(getTrapbuilder(batchTrapsSent));
				} else {
					Thread.sleep(1);
					m_sleepMillis++;
				}

				batchElapsedMillis = Calendar.getInstance().getTimeInMillis()
						- batchBegin;
				currentRate = batchTrapsSent.doubleValue()
						/ batchElapsedMillis.doubleValue() * 1000.0;

				if (batchElapsedMillis % 1000 == 0) {
					System.out.print(".");
				}

			}
			System.out.println();
			totalTrapsSent += batchTrapsSent;
			System.out.println("   Actual time to send: "
					+ (batchElapsedMillis / 1000.0 + " seconds"));

			totalElapsedTime += batchElapsedMillis;
			System.out.println("Elapsed Time (secs): "
					+ (totalElapsedTime / 1000L));

			System.out.println("         Traps sent: "
					+ Integer.valueOf(totalTrapsSent).toString());
			System.out.println();
			Thread.sleep(m_batchDelay.longValue() * 1000L);
			m_sleepMillis += m_batchDelay.longValue() * 1000L;
		}

		remainingTraps = (m_trapCount * m_batchCount) - totalTrapsSent;
		if (remainingTraps > 0) {
			System.out.println("Sending batch remainder of " + remainingTraps
					+ " traps...");
			batchBegin = Calendar.getInstance().getTimeInMillis();

			while (batchTrapsSent.intValue() < remainingTraps) {

				if (currentRate <= m_trapRate || elapsedMillis == 0) {
					batchTrapsSent += sendTrap(getTrapbuilder(batchTrapsSent));
				} else {
					Thread.sleep(1);
					m_sleepMillis++;
				}

				elapsedMillis = Calendar.getInstance().getTimeInMillis()
						- batchBegin;
				currentRate = batchTrapsSent.doubleValue()
						/ elapsedMillis.doubleValue() * 1000.0;
			}

			totalTrapsSent += batchTrapsSent;
			System.out.println("Elapsed Time (secs): "
					+ ((System.currentTimeMillis() - beginMillis) / 1000L));
			System.out.println("         Traps sent: "
					+ Integer.valueOf(totalTrapsSent).toString());
		}
		
		return totalTrapsSent;
	}

	private static SnmpTrapBuilder getTrapbuilder(Long batchTrapsSent) {
		if (batchTrapsSent % 2 == 0) {
			return builderV2;
		}
		return builderV1;

	}

	private static int sendTrap(final SnmpTrapBuilder builder) {
		int trapsSent = 0;
		try {
			builder.send(m_trapSink.getHostAddress(), m_trapPort,
					m_trapCommunity);
			trapsSent++;
			return trapsSent;
		} catch (Exception e) {
			throw new IllegalStateException("Caught Exception sending trap.", e);
		}
	}

	public static SnmpTrapBuilder createBuilderV1() throws Exception {
		// Comes as Normal
		SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
		pdu.setEnterprise(SnmpObjId.get(".1.3.6.1.4.1.9.9.70.2"));
		pdu.setGeneric(6);
		pdu.setSpecific(1);
		pdu.setTimeStamp(0);
		pdu.setAgentAddress(InetAddress.getLocalHost());
		return pdu;
	}

	public static SnmpTrapBuilder createBuilderV2() {
		// Comes as warning
		SnmpObjId enterpriseId = SnmpObjId.get(".1.3.6.1.4.1.9.9.87.2");
		boolean isGeneric = false;
		SnmpObjId trapOID;
		if ((SnmpObjId.get(".1.3.6.1.4.1.9.10").toString())
				.contains(enterpriseId.toString())) {
			isGeneric = true;
			trapOID = enterpriseId;
		} else {
			trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
		}

		SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
		pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils
				.getValueFactory().getTimeTicks(0));
		pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils
				.getValueFactory().getObjectId(trapOID));
		if (isGeneric) {
			pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils
					.getValueFactory().getObjectId(enterpriseId));
		}
		return pdu;
	}
}
