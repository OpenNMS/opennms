package org.opennms.netmgt.icmp;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullPinger implements Pinger {
    private static final UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException("ICMP is not available.");
    private static final Logger LOG = LoggerFactory.getLogger(NullPinger.class);

    @Override
    public void ping(InetAddress host, long timeout, int retries, int packetsize, int sequenceId, PingResponseCallback cb) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, packetsize={}, sequenceId={}, callback={}", host, timeout, retries, packetsize, sequenceId, cb);
        cb.handleError(host, null, UNSUPPORTED);
    }

    @Override
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, sequenceId={}, callback={}", host, timeout, retries, sequenceId, cb);
        cb.handleError(host, null, UNSUPPORTED);
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries, int packetsize) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, packetsize={}", host, timeout, retries, packetsize);
        throw UNSUPPORTED;
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}", host, timeout, retries);
        throw UNSUPPORTED;
    }

    @Override
    public Number ping(final InetAddress host) throws Exception {
        LOG.trace("ping: host={}", host);
        throw UNSUPPORTED;
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
        LOG.trace("parallel-ping: host={}, count={}, timeout={}, interval={}", host, count, timeout, pingInterval);
        throw UNSUPPORTED;
    }

    @Override
    public void initialize4() throws Exception {
        LOG.info("initialize4() called.");
    }

    @Override
    public void initialize6() throws Exception {
        LOG.info("initialize6() called.");
    }

    @Override
    public boolean isV4Available() {
        LOG.info("isV4Available() called, lying and saying 'true'");
        return true;
    }

    @Override
    public boolean isV6Available() {
        LOG.info("isV6Available() called, lying and saying 'true'");
        return true;
    }

}
