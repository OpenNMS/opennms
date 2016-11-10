package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(UDPProcessor.class);

    private final SyslogdConfig m_config;

    public UDPProcessor(SyslogdConfig config) {
        m_config = Objects.requireNonNull(config);
    }

    public Log toEventLog(UDPMessageLogDTO messageLog) {
        final Log elog = new Log();
        final Events events = new Events();
        elog.setEvents(events);
        for (UDPMessageDTO message : messageLog.getMessages()) {
            try {
                LOG.debug("Converting syslog message into event.");
                ConvertToEvent re = new ConvertToEvent(
                        messageLog.getSystemId(),
                        messageLog.getLocation(),
                        messageLog.getSourceAddress(),
                        messageLog.getSourcePort(),
                        // Decode the packet content as ASCII
                        // TODO: Support more character encodings?
                        StandardCharsets.US_ASCII.decode(message.getBytes()).toString(),
                        m_config
                    );
                events.addEvent(re.getEvent());
            } catch (final UnsupportedEncodingException e) {
                LOG.info("Failure to convert package", e);
            } catch (final MessageDiscardedException e) {
                LOG.info("Message discarded, returning without enqueueing event.", e);
            } catch (final Throwable e) {
                LOG.error("Unexpected exception while processing SyslogConnection", e);
            }
        }
        return elog;
    }
}
