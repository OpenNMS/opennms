package org.opennms.netmgt.syslogd;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  * Convert to event junit test file to test the performance of Syslogd ConvertToEvent processor
 * @author ms043660
 */
public class ConvertToEventTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertToEventTest.class);

    /**
     * Test method which calls the ConvertToEvent constructor.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    @Test
    public void testConvertToEvent() throws MarshalException,
            ValidationException, IOException {

        // 10000 sample syslogmessages from xml file are taken and passed as
        // Inputstream to create syslogdconfiguration
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this,
                                                                              "/etc/syslogd-loadtest-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        // Sample message which is embedded in packet and passed as parameter
        // to
        // ConvertToEvent constructor
        byte[] bytes = "<34> 2010-08-19 localhost foo10000: load test 10000 on tty1".getBytes();

        // Datagram packet which is passed as parameter for ConvertToEvent
        // constructor
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length,
                                                InetAddress.getLocalHost(),
                                                SyslogClient.PORT);
        String data = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(pkt.getData())).toString();

        // ConvertToEvent takes 4 parameter
        // @param addr The remote agent's address.
        // @param port The remote agent's port
        // @param data The XML data in US-ASCII encoding.
        // @param len The length of the XML data in the buffer
        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                pkt.getAddress(),
                pkt.getPort(),
                data, config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }

    @Test
    public void testCiscoEventConversion() throws MarshalException, ValidationException, IOException {

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-cisco-configuration.xml");
        SyslogdConfig config = new SyslogdConfigFactory(stream);

        try {
            ConvertToEvent convertToEvent = new ConvertToEvent(
                DistPollerDao.DEFAULT_DIST_POLLER_ID,
                InetAddressUtils.ONE_TWENTY_SEVEN,
                9999,
                "<190>Mar 11 08:35:17 aaa_host 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet", 
                config
            );
            LOG.info("Generated event: {}", convertToEvent.getEvent().toString());
        } catch (MessageDiscardedException e) {
            LOG.error("Message Parsing failed", e);
            fail("Message Parsing failed: " + e.getMessage());
        }
    }
}
