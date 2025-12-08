/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.deviceconfig.tftp.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPAckPacket;
import org.apache.commons.net.tftp.TFTPDataPacket;
import org.apache.commons.net.tftp.TFTPErrorPacket;
import org.apache.commons.net.tftp.TFTPPacket;
import org.apache.commons.net.tftp.TFTPPacketException;
import org.apache.commons.net.tftp.TFTPReadRequestPacket;
import org.apache.commons.net.tftp.TFTPWriteRequestPacket;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;
import org.opennms.features.deviceconfig.tftp.TftpServer;
import org.opennms.features.deviceconfig.tftp.TftpStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Copied from org.apache.commons.net.tftp.TFTPServer with some adjustments
// The original source is located in the src/test folder of the commons-net project. The compiled TFTPServer is
// not part of a jar distribution.
//
// Adjustments are:
//
// - received files are not stored on disk but in memory and are handed off to registered listeners
// - the maximum file size to receive can be configured
// - requesting files is not supported
// - added support for RFC2348 blksize

public class TftpServerImpl implements TftpServer, Runnable, AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger(TftpServerImpl.class);

    /*
     * An instance of an ongoing transfer.
     */
    private class TFTPTransfer implements Runnable {

        private final TFTPPacket tftpPacket_;

        private boolean shutdownTransfer;

        TFTP transferTftp_;

        public TFTPTransfer(final TFTPPacket tftpPacket) {
            tftpPacket_ = tftpPacket;
        }
        
        /*
         * handle a tftp write request.
         */
        private void handleWrite(final TFTPWriteRequestPacket twrp) throws IOException,
                                                                           TFTPPacketException {
            var underlyingByteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream bos = underlyingByteArrayOutputStream;
            var bytesReceived = 0l;

            try {
                int lastBlock = 0;
                final String fileName = twrp.getFilename();
                int negotiatedBlksize = TFTPDataPacket.MAX_DATA_LENGTH;
                Map<String,String> twrpOptions = twrp.getOptions();

                try {
                    if (twrp.getMode() == TFTP.NETASCII_MODE) {
                        bos = new FromNetASCIIOutputStream(bos);
                    }
                } catch (final Exception e) {
                    statistics.incErrors();
                    LOG.error("can not handle net-ascii mode", e);
                    transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp
                            .getPort(), TFTPErrorPacket.UNDEFINED, e.getMessage()));
                    return;
                }
                TFTPAckPacket lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);

                // Handle RFC2348 TFTP blksize
                if (twrpOptions.containsKey("blksize")) {
                    LOG.debug("Received blksize option from client '{}', attempting negotiation", twrp.getAddress());
                    try {
                        negotiatedBlksize = Integer.parseInt(twrpOptions.get("blksize"));
                        if (negotiatedBlksize < 8 || negotiatedBlksize > 65464) {
                            LOG.error("Received invalid blksize '{}' option from client {}", negotiatedBlksize, twrp.getAddress());
                            transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(),
                                    twrp.getPort(), TFTPErrorPacket.INVALID_OPTIONS_VALUE,
                                    "Invalid blksize"));
                            return;
                        } else {
                            LOG.debug("Negotiating tftp blksize '{}' with client '{}'", negotiatedBlksize, twrp.getAddress());
                            // Create an OAck response packet - commons doesn't provide a method for this yet.
                            DatagramPacket oackPacket = createBlksizeOAckPacket(negotiatedBlksize, twrp.getAddress(), twrp.getPort());
                           
                            try {
                                int localPort = transferTftp_.getLocalPort();
                                transferTftp_.close();
                                while (true) {
                                    if (!transferTftp_.isOpen()) { // wait until we know the socket is not open
                                        break;
                                    }
                                }
                                DatagramSocket socket = new DatagramSocket(localPort);
                                socket.send(oackPacket);
                                socket.close();
                                while (true) {
                                    if (socket.isClosed()) { // wait until we know the socket is closed
                                        break;
                                    }
                                }
                                transferTftp_.open(localPort); // every day I'm shufflin'
                            } catch (Exception e) {
                                LOG.debug("Error sending OACK packet in response to 'blksize={}',", negotiatedBlksize, e);
                                return;
                            }
                            transferTftp_.resetBuffersToSize(negotiatedBlksize);
                            LOG.debug("Successfully negotiated blksize '{}' with client '{}'", negotiatedBlksize, twrp.getAddress());
                        }
                    } catch (final Exception e) {
                        LOG.error("Received invalid blksize '{}' option from client {}:", negotiatedBlksize, twrp.getAddress(), e);
                        transferTftp_.bufferedSend(new TFTPErrorPacket(twrp.getAddress(),
                                twrp.getPort(), TFTPErrorPacket.INVALID_OPTIONS_VALUE,
                                "Invalid blksize"));
                        return;
                    }
                } else {
                    sendData(transferTftp_, lastSentAck); // send the ack
                }

                while (true) {
                    // get the response - ensure it is from the right place.
                    TFTPPacket dataPacket = null;

                    int timeoutCount = 0;

                    while (!shutdownTransfer
                           && (dataPacket == null
                               || !dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket
                                                                                                .getPort() != twrp.getPort())) {
                        // listen for an answer.
                        if (dataPacket != null) {
                            // The data that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            statistics.incWarnings();
                            LOG.warn("TFTP Server ignoring message from unexpected source.");
                            transferTftp_.bufferedSend(new TFTPErrorPacket(dataPacket.getAddress(),
                                    dataPacket.getPort(), TFTPErrorPacket.UNKNOWN_TID,
                                    "Unexpected Host or Port"));
                        }

                        try {
                            dataPacket = transferTftp_.bufferedReceive();
                        } catch (final SocketTimeoutException e) {
                            statistics.incErrors();
                            LOG.error("did not receive data packet from client '{}': ", twrp.getAddress(), e);
                            if (timeoutCount >= maxTimeoutRetries_) {
                                throw e;
                            }
                            // It didn't get our ack. Resend it.
                            LOG.debug("Resending missed ack to '{}'", twrp.getAddress());
                            transferTftp_.bufferedSend(lastSentAck);
                            timeoutCount++;
                            continue;
                        }
                    }

                    if (dataPacket instanceof TFTPWriteRequestPacket) {
                        // it must have missed our initial ack. Send another.
                        LOG.debug("Resending WRQ ack to '{}'", twrp.getAddress());
                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);
                        transferTftp_.bufferedSend(lastSentAck);
                    } else if (dataPacket == null || !(dataPacket instanceof TFTPDataPacket)) {
                        if (!shutdownTransfer) {
                            statistics.incErrors();
                            LOG.error("Unexpected response from tftp client during transfer ("
                                             + dataPacket + ").  Transfer aborted.");
                        }
                        break;
                    } else {
                        final int block = ((TFTPDataPacket) dataPacket).getBlockNumber();
                        final byte[] data = ((TFTPDataPacket) dataPacket).getData();
                        final int dataLength = ((TFTPDataPacket) dataPacket).getDataLength();
                        final int dataOffset = ((TFTPDataPacket) dataPacket).getDataOffset();
                        LOG.debug("Processing block {} from client '{}'", block, twrp.getAddress());

                        if (block > lastBlock || lastBlock == 65535 && block == 0) {
                            // it might resend a data block if it missed our ack
                            // - don't rewrite the block.
                            bytesReceived += dataLength;
                            statistics.incBytesReceived(dataLength);
                            if (bytesReceived > maximumReceiveSize) {
                                statistics.incErrors();
                                LOG.error("Maximum receive size exceeded - address: {}, fileName: {}; max: {}; received: ", twrp.getAddress(), twrp.getFilename(), maximumReceiveSize, bytesReceived);
                                // make sure it was from the right client...
                                transferTftp_
                                        .bufferedSend(new TFTPErrorPacket(dataPacket
                                                .getAddress(), dataPacket.getPort(),
                                                TFTPErrorPacket.OUT_OF_SPACE,
                                                "Maximum size (" + maximumReceiveSize + ") exceeded"));
                                break;
                            }
                            bos.write(data, dataOffset, dataLength);
                            lastBlock = block;

                        }

                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), block);
                        LOG.debug("Sending Ack for block {} from client '{}'", block, twrp.getAddress());
                        sendData(transferTftp_, lastSentAck); // send the data
                        if (dataLength < negotiatedBlksize) {
                            // end of stream signal - The transfer is complete.
                            bos.close();

                            var content = underlyingByteArrayOutputStream.toByteArray();

                            statistics.incFilesReceived();

                            List<TftpFileReceiver> rs;
                            synchronized (receivers) {
                                rs = new ArrayList<>(receivers);
                            }
                            rs.forEach(r -> {
                                r.onFileReceived(twrp.getAddress(), fileName, content);
                            });

                            // But my ack may be lost - so listen to see if I
                            // need to resend the ack.
                            for (int i = 0; i < maxTimeoutRetries_; i++) {
                                try {
                                    dataPacket = transferTftp_.bufferedReceive();
                                } catch (final SocketTimeoutException e) {
                                    // this is the expected route - the client
                                    // shouldn't be sending any more packets.
                                    LOG.debug("tftp transfer from client '{}' complete.", twrp.getAddress());
                                    break;
                                }

                                if (dataPacket != null
                                    && (!dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket
                                                                                                      .getPort() != twrp.getPort())) {

                                    statistics.incErrors();
                                    LOG.error("unexpected host or port - expectedHost: {}; expectedPort: {}; actualHost: {}; actualPort: {}",
                                            twrp.getAddress(), twrp.getPort(),
                                            dataPacket.getAddress(), dataPacket.getPort()
                                    );
                                    // make sure it was from the right client...
                                    transferTftp_
                                            .bufferedSend(new TFTPErrorPacket(dataPacket
                                                    .getAddress(), dataPacket.getPort(),
                                                    TFTPErrorPacket.UNKNOWN_TID,
                                                    "Unexpected Host or Port"));
                                } else {
                                    // This means they sent us the last
                                    // datapacket again, must have missed our
                                    // ack. resend it.
                                    transferTftp_.bufferedSend(lastSentAck);
                                }
                            }

                            // all done.
                            LOG.debug("Timing out transfer from client '{}'", twrp.getAddress());
                            break;
                        }
                    }
                }
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }

        @Override
        public void run() {
            try {
                transferTftp_ = new TFTP();

                transferTftp_.beginBufferedOps();
                transferTftp_.setDefaultTimeout(socketTimeout_);

                transferTftp_.open();

                if (tftpPacket_ instanceof TFTPReadRequestPacket) {
                    // for now we do not support read requests; uncomment the following line
                    // handleRead((TFTPReadRequestPacket) tftpPacket_);
                    statistics.incWarnings();
                    LOG.warn("illegal operation; tried to read file - host: ", tftpPacket_.getAddress());
                    transferTftp_.bufferedSend(new TFTPErrorPacket(tftpPacket_.getAddress(), tftpPacket_
                            .getPort(), TFTPErrorPacket.ILLEGAL_OPERATION,
                            "Read not allowed by server."));
                } else if (tftpPacket_ instanceof TFTPWriteRequestPacket) {
                    LOG.debug("Received write request from client '{}'", tftpPacket_.getAddress());
                    handleWrite((TFTPWriteRequestPacket) tftpPacket_);
                } else {
                    statistics.incWarnings();
                    LOG.warn("Unsupported TFTP request (" + tftpPacket_ + ") - ignored.");
                }
            } catch (final Exception e) {
                if (!shutdownTransfer) {
                    statistics.incErrors();
                    LOG.error("Unexpected Error in during TFTP file transfer.  Transfer aborted.", e);
                }
            } finally {
                try {
                    if (transferTftp_ != null && transferTftp_.isOpen()) {
                        transferTftp_.endBufferedOps();
                        transferTftp_.close();
                    }
                } catch (final Exception e) {
                    // noop
                }
                synchronized (transfers_) {
                    transfers_.remove(this);
                }
            }
        }

        public void shutdown() {
            shutdownTransfer = true;
            try {
                transferTftp_.close();
            } catch (final RuntimeException e) {
                // noop
            }
        }
    }

    private static class TftpStatisticsImpl implements TftpStatistics, Cloneable {

        private int filesReceived;
        private long bytesReceived;
        private int errors;
        private int warnings;

        @Override
        public synchronized int filesReceived() {
            return filesReceived;
        }

        @Override
        public synchronized long bytesReceived() {
            return bytesReceived;
        }

        @Override
        public synchronized int errors() {
            return errors;
        }

        @Override
        public synchronized int warnings() {
            return warnings;
        }

        public synchronized void reset() {
            filesReceived = 0;
            bytesReceived = 0;
            errors = 0;
        }

        public synchronized void incFilesReceived() {
            filesReceived++;
        }

        public synchronized void incBytesReceived(long bytes) {
            bytesReceived += bytes;
        }

        public synchronized void incErrors() {
            errors++;
        }

        public synchronized void incWarnings() {
            warnings++;
        }

        @Override
        public synchronized TftpStatisticsImpl clone() {
            try {
                return (TftpStatisticsImpl)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public synchronized TftpStatisticsImpl cloneAndReset() {
            var c = clone();
            reset();
            return c;
        }

    }

    private final HashSet<TFTPTransfer> transfers_ = new HashSet<>();
    private volatile boolean shutdownServer;
    private TFTP serverTftp_;
    private int port_ = 69;
    private InetAddress laddr_ = new InetSocketAddress(0).getAddress();

    private Exception serverException;

    private int maxTimeoutRetries_ = 3;
    private int socketTimeout_;

    private Thread serverThread;

    private Set<TftpFileReceiver> receivers = new HashSet<>();
    private long maximumReceiveSize = 50_000l;

    private TftpStatisticsImpl statistics = new TftpStatisticsImpl();

    public TftpServerImpl() {
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    public int getPort() {
        return port_;
    }

    /**
     * Get the current value for maxTimeoutRetries
     *
     * @return the max allowed number of retries
     */
    public int getMaxTimeoutRetries() {
        return maxTimeoutRetries_;
    }

    /**
     * The current socket timeout used during transfers in milliseconds.
     *
     * @return the timeout value
     */
    public int getSocketTimeout() {
        return socketTimeout_;
    }

    /**
     * check if the server thread is still running.
     *
     * @return true if running, false if stopped.
     * @throws Exception throws the exception that stopped the server if the server is stopped from
     *                   an exception.
     */
    public boolean isRunning() throws Exception {
        if (shutdownServer && serverException != null) {
            throw serverException;
        }
        return !shutdownServer;
    }

    /*
     * start the server, throw an error if it can't start.
     */
    public void launch() throws IOException {
        LOG.info("Starting TFTP Server on port " + port_);
        shutdownServer = false;
        serverTftp_ = new TFTP();

        // This is the value used in response to each client.
        socketTimeout_ = serverTftp_.getDefaultTimeout();

        // we want the server thread to listen forever.
        serverTftp_.setDefaultTimeout(0);

        try {
            if (laddr_ != null) {
                serverTftp_.open(port_, laddr_);
            } else {
                serverTftp_.open(port_);
            }
        } catch (SocketException e) {
            statistics.incErrors();
            throw new IOException("could not open tftp server - port: " + port_ + (laddr_ != null ? "; address: " + laddr_ : ""), e);
        }

        serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void run() {
        try {
            while (!shutdownServer) {
                final TFTPPacket tftpPacket;

                tftpPacket = serverTftp_.receive();

                final TFTPTransfer tt = new TFTPTransfer(tftpPacket);
                synchronized (transfers_) {
                    transfers_.add(tt);
                }

                final Thread thread = new Thread(tt);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (final Exception e) {
            if (!shutdownServer) {
                serverException = e;
                statistics.incErrors();
                LOG.error("Unexpected Error in TFTP Server - Server shut down!", e);
            }
        } finally {
            shutdownServer = true; // set this to true, so the launching thread can check to see if it started.
            if (serverTftp_ != null && serverTftp_.isOpen()) {
                serverTftp_.close();
            }
        }
    }

    /*
     * Also allow customisation of sending data/ack so can generate errors if needed
     */
    void sendData(final TFTP tftp, final TFTPPacket data) throws IOException {
        tftp.bufferedSend(data);
    }

    /**
     * Set the max number of retries in response to a timeout. Default 3. Min 0.
     *
     * @param retries number of retries, must be &gt; 0
     */
    public void setMaxTimeoutRetries(final int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException("Invalid Value");
        }
        maxTimeoutRetries_ = retries;
    }

    /**
     * Set the socket timeout in milliseconds used in transfers. Defaults to the value here:
     * https://commons.apache.org/net/apidocs/org/apache/commons/net/tftp/TFTP.html#DEFAULT_TIMEOUT
     * (5000 at the time I write this) Min value of 10.
     *
     * @param timeout the timeout; must be larger than 10
     */
    public void setSocketTimeout(final int timeout) {
        if (timeout < 10) {
            throw new IllegalArgumentException("Invalid Value");
        }
        socketTimeout_ = timeout;
    }

    public void setAddress(String addr) throws UnknownHostException {
        this.laddr_ = InetAddress.getByName(addr);
    }

    public void setPort(int port) {
        this.port_ = port;
    }

    public void setMaximumReceiveSize(long maximumReceiveSize) {
        this.maximumReceiveSize = maximumReceiveSize;
    }

    /**
     * Stop the tftp server (and any currently running transfers) and release all opened network
     * resources.
     */
    public void close() {
        shutdownServer = true;

        synchronized (transfers_) {
            final Iterator<TFTPTransfer> it = transfers_.iterator();
            while (it.hasNext()) {
                it.next().shutdown();
            }
        }

        if (serverTftp_ != null) {
            try {
                serverTftp_.close();
            } catch (final RuntimeException e) {
                // noop
            }
        }

        if (serverThread != null) {
            try {
                serverThread.join();
            } catch (final InterruptedException e) {
                // we've done the best we could, return
            }
        }
    }

    @Override
    public void register(TftpFileReceiver receiver) throws IOException {
        synchronized (receivers) {
            if (serverTftp_ == null) {
                launch();
            }
            receivers.add(receiver);
            LOG.info("Registered new TFTP receiver, current receiver count {}", receivers.size());
        }
    }

    @Override
    public void unregister(TftpFileReceiver receiver) {
        synchronized (receivers) {
            receivers.remove(receiver);
            LOG.info("Unregistered TFTP receiver, current receiver count {}", receivers.size());
            if(receivers.isEmpty()) {
                LOG.info("No receivers exist currently, closing the tftp server");
                close();
                serverTftp_ = null;
            }
        }
    }

    @Override
    public TftpStatistics getStatistics() {
        return statistics.clone();
    }

    @Override
    public TftpStatistics getAndResetStatistics() {
        return statistics.cloneAndReset();
    }

    private DatagramPacket createBlksizeOAckPacket(int size, InetAddress addr, int port) throws IOException {
        // build the oack packet - commons doesn't provide a method for this yet...
        ByteArrayOutputStream oack = new ByteArrayOutputStream();
        oack.write(0);
        oack.write(TFTPPacket.OACK);
        oack.write("blksize".getBytes(StandardCharsets.US_ASCII));
        oack.write(0);
        oack.write(String.valueOf(size).getBytes(StandardCharsets.US_ASCII));
        oack.write(0);
        //make it a byte array
        byte[] oackData = oack.toByteArray();

        return new DatagramPacket(oackData, oackData.length, addr, port);
    }
}
