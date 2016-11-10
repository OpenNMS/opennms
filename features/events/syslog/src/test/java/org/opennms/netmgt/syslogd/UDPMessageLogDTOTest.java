package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;

public class UDPMessageLogDTOTest extends XmlTestNoCastor<UDPMessageLogDTO> {

    public UDPMessageLogDTOTest(UDPMessageLogDTO sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException, UnknownHostException {
        UDPMessageLogDTO messageLog = new UDPMessageLogDTO();
        messageLog.setLocation("loc");
        messageLog.setSystemId("99");
        messageLog.setSourceAddress(InetAddress.getByName("127.0.0.1"));
        messageLog.setSourcePort(1514);

        UDPMessageDTO message = new UDPMessageDTO();
        message.setTimestamp(new Date(0));
        message.setBytes(ByteBuffer.wrap(new byte[] {0, 1, 2, 3}));
        messageLog.getMessages().add(message);

        return Arrays.asList(new Object[][] { { messageLog,
                "<udp-message-log source-address=\"127.0.0.1\" source-port=\"1514\" system-id=\"99\" location=\"loc\">\n" + 
                "   <messages timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">AAECAw==</messages>\n" + 
                "</udp-message-log>",
                null }
        });
    }
}
