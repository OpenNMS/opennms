package org.opennms.netmgt.snmp.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.joesnmp.JoeSnmpValueFactory;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

public class SnmpValueTest {
	private static final SnmpValueFactory[] m_factories = new SnmpValueFactory[] {
		new Snmp4JValueFactory(),
		new JoeSnmpValueFactory(),
		new MockSnmpValueFactory()
	};

	@Test
	public void testCounter32() {
		for (final SnmpValueFactory factory : m_factories) {
			final String methodName = "Counter32";
			final String stringResult = "42";
			final Long numberResult = 42L;
			final SnmpValue value = factory.getCounter32(numberResult);
			final String className = factory.getClass().getName();

			doNumericCheck(className, methodName, value, stringResult, numberResult);
		}
	}

	@Test
	public void testCounter64() {
		for (final SnmpValueFactory factory : m_factories) {
			final String methodName = "Counter64";
			final String stringResult = "42";
			final Long numberResult = 42L;
			final SnmpValue value = factory.getCounter64(BigInteger.valueOf(numberResult));
			final String className = factory.getClass().getName();

			doNumericCheck(className, methodName, value, stringResult, numberResult);
		}
	}

	@Test
	public void testGauge32() {
		for (final SnmpValueFactory factory : m_factories) {
			final String methodName = "Gauge32";
			final String stringResult = "42";
			final Long numberResult = 42L;
			final SnmpValue value = factory.getGauge32(numberResult);
			final String className = factory.getClass().getName();

			doNumericCheck(className, methodName, value, stringResult, numberResult);
		}
	}

	@Test
	public void testInt32() {
		for (final SnmpValueFactory factory : m_factories) {
			final String methodName = "Int32";
			final String stringResult = "42";
			final Long numberResult = 42L;
			final SnmpValue value = factory.getInt32(numberResult.intValue());
			final String className = factory.getClass().getName();

			doNumericCheck(className, methodName, value, stringResult, numberResult);
		}
	}

	@Test
	public void testNull() {
		for (final SnmpValueFactory factory : m_factories) {
			final SnmpValue value = factory.getNull();
			assertTrue(factory.getClass().getName() + ": Null isNull should be true", value.isNull());
			assertFalse(factory.getClass().getName() + ": Null isEndOfMib should be false", value.isEndOfMib());
			assertFalse(factory.getClass().getName() + ": Null isError should be false", value.isError());
		}
	}

	@Test
	public void testInetAddress() {
		for (final SnmpValueFactory factory : m_factories) {
			final InetAddress address = InetAddressUtils.addr("192.168.0.1");
			final SnmpValue value = factory.getIpAddress(address);
			final String className = factory.getClass().getName();
			
			assertEquals(className + ": getInetAddress to InetAddress should return 192.168.0.1", address, value.toInetAddress());
			assertEquals(className + ": getInetAddress to String should return 192.168.0.1", "192.168.0.1", value.toString());
			assertEquals(className + ": getInetAddress to DisplayString should return 192.168.0.1", "192.168.0.1", value.toDisplayString());
			try {
				value.toInt();
				fail(className + ": getInetAddress to int should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toLong();
				fail(className + ": getInetAddress to long should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toBigInteger();
				fail(className + ": getInetAddress to BigInteger should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toHexString();
				fail(className + ": getInetAddress to HexString should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toSnmpObjId();
				fail(className + ": getInetAddress to SnmpObjId should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
		}
	}

	@Test
	public void testSnmpObjId() {
		for (final SnmpValueFactory factory : m_factories) {
			final String oid = ".1.3.6.1.4.1.2925.4.5.2.1.1";
			final SnmpObjId id = SnmpObjId.get(oid);
			final SnmpValue value = factory.getObjectId(id);
			final String className = factory.getClass().getName();

			assertEquals(className + ": getObjectId to SnmpObjId should return " + oid, id, value.toSnmpObjId());
			assertEquals(className + ": getObjectId to String should return " + oid, oid, value.toString());
			assertEquals(className + ": getObjectId to DisplayString should return " + oid, oid, value.toDisplayString());
			try {
				value.toInt();
				fail(className + ": getObjectId to int should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toLong();
				fail(className + ": getObjectId to long should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toBigInteger();
				fail(className + ": getObjectId to BigInteger should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toHexString();
				fail(className + ": getObjectId to HexString should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toInetAddress();
				fail(className + ": getObjectId to InetAddress should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
		}
	}
	
	@Test
	public void testTimeTicks() {
		for (final SnmpValueFactory factory : m_factories) {
			final SnmpValue value = factory.getTimeTicks(42);
			final String className = factory.getClass().getName();
			
			assertEquals(className + ": getTimeTicks to int should return " + value.toInt(), 42, value.toInt());
			assertEquals(className + ": getTimeTicks to long should return " + value.toLong(), 42, value.toLong());
			assertEquals(className + ": getTimeTicks to BigInteger should return " + value.toBigInteger(), BigInteger.valueOf(42), value.toBigInteger());
			assertEquals(className + ": getTimeTicks to String should return 42", "42", value.toString());
			assertEquals(className + ": getTimeTicks to DisplayString should return 42", "42", value.toDisplayString());
			try {
				value.toHexString();
				fail(className + ": getTimeTicks to HexString should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toInetAddress();
				fail(className + ": getTimeTicks to InetAddress should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toSnmpObjId();
				fail(className + ": getTimeTicks to SnmpObjId should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
		}
	}
	
	@Test
	public void testMacAddressOctetString() {
		for (final SnmpValueFactory factory : m_factories) {
			final String hexString = "005056e7a72f";
			final byte[] rawBytes = { 0, (byte)0x50, (byte)0x56, (byte)0xe7, (byte)0xa7, (byte)0x2f };
			final String stringBytes = "." + new String(Arrays.copyOfRange(rawBytes, 1, rawBytes.length));
			final String className = factory.getClass().getName();

			final SnmpValue value = factory.getOctetString(rawBytes);
//			System.err.println("HexString = " + value.toHexString());

			assertFalse(className + ": getOctetString displayable should be false", value.isDisplayable());
			assertEquals(className + ": getOctetString to String should return " + stringBytes, stringBytes, value.toString());
			assertEquals(className + ": getOctetString to DisplayString should return " + stringBytes, stringBytes, value.toDisplayString());
			assertEquals(className + ": getOctetString to HexString should return " + hexString, hexString, value.toHexString());
			try {
				value.toInt();
				fail(className + ": getOctetString to int should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toLong();
				fail(className + ": getOctetString to long should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toBigInteger();
				fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
		}
	}
	
	@Test
	public void testOtherAddressOctetString() {
		for (final SnmpValueFactory factory : m_factories) {
			final String hexString = "abcd";
			final byte[] rawBytes = { (byte)0xab, (byte)0xcd };
			final String stringBytes = new String(rawBytes);
			final String className = factory.getClass().getName();

			final SnmpValue value = factory.getOctetString(rawBytes);

			assertFalse(className + ": getOctetString displayable should be false", value.isDisplayable());
			assertEquals(className + ": getOctetString to String should return " + stringBytes, stringBytes, value.toString());
			assertEquals(className + ": getOctetString to DisplayString should return " + stringBytes, stringBytes, value.toDisplayString());
			assertEquals(className + ": getOctetString to HexString should return " + hexString, hexString, value.toHexString());
			try {
				value.toInt();
				fail(className + ": getOctetString to int should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toLong();
				fail(className + ": getOctetString to long should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
			try {
				value.toBigInteger();
				fail(className + ": getOctetString to BigInteger should throw an IllegalArgumentException");
			} catch (final IllegalArgumentException e) { /* expected */ }
		}
	}
	
	private void doNumericCheck(final String className, final String methodName, final SnmpValue result, final String expectedResultString, final Long expectedResultNumber) {
		assertEquals(className + ": " + methodName + " to int should return " + expectedResultString, expectedResultNumber.intValue(), result.toInt());
		assertEquals(className + ": " + methodName + " to long should return " + expectedResultString, expectedResultNumber.longValue(), result.toLong());
		assertEquals(className + ": " + methodName + " to BigInteger should return " + expectedResultString, BigInteger.valueOf(expectedResultNumber.longValue()), result.toBigInteger());
		assertEquals(className + ": " + methodName + " to String should return " + expectedResultString, expectedResultString, result.toString());
		assertEquals(className + ": " + methodName + " to DisplayString should return " + expectedResultString, expectedResultString, result.toDisplayString());
		assertEquals(className + ": " + methodName + " to bytes should return [" + expectedResultString + "]", "[" + expectedResultString + "]", Arrays.toString(result.getBytes()));
		try {
			result.toHexString();
			fail(className + ": " + methodName + " to HexString should throw an IllegalArgumentException");
		} catch (final IllegalArgumentException e) { /* expected */ }
		try {
			result.toInetAddress();
			fail(className + ": " + methodName + " to InetAddress should throw an IllegalArgumentException");
		} catch (final IllegalArgumentException e) { /* expected */ }
		try {
			result.toSnmpObjId();
			fail(className + ": " + methodName + " to SnmpObjId should throw an IllegalArgumentException");
		} catch (final IllegalArgumentException e) { /* expected */ }
	}

	/*
	@Test
	public void testHexString() {
		final String hexString = "00 50 56 E7 A7 2F";
		final SnmpValue nativeStringBytes = m_nativeSnmpFactory.getOctetString(hexString.getBytes());
		final SnmpValue mockStringBytes = m_mockFactory.getOctetString(hexString.getBytes());
		
		final byte[] rawBytes = { 0, (byte)0x50, (byte)0x56, (byte)0xe7, (byte)0xa7, (byte)0x2f };
		
		final SnmpValue nativeRawBytes = m_nativeSnmpFactory.getOpaque(rawBytes);
		final SnmpValue mockRawBytes = m_mockFactory.getOctetString(rawBytes);

		assertEquals(hexString, nativeStringBytes.toString());
		assertEquals(hexString, mockStringBytes.toString());

		assertEquals(hexString.replaceAll(" ", ":"), nativeRawBytes.toString());
		assertEquals(hexString, mockRawBytes.toString());

		assertEquals(nativeStringBytes.toDisplayString(), mockStringBytes.toDisplayString());
	}
	*/

}
