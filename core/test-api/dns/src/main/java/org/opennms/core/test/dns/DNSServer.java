/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This file is a derivative work, containing both original code, included code,
 * and modified code that was published under the GNU General Public License.
 * 
 * Original code Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)
 * 
 * Refactored from DNSServer in the JDNSS server
 * http://sourceforge.net/projects/jdnss/
 *
 * Project site for JDNSS says "BSD and GPL license" but this file had no
 * specifics about which license it's specifically under, so assume the more
 * restrictive GPL until we can get more details.
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
package org.opennms.core.test.dns;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.xbill.DNS.*;

public class DNSServer {
    private static final int DEFAULT_SOCKET_TIMEOUT = 100;

    private final class TCPListener implements Stoppable {
        private final int m_port;
        private final InetAddress m_addr;
        private ServerSocket m_socket;
        private volatile boolean m_stopped = false;
        private CountDownLatch m_latch = new CountDownLatch(1);

        private TCPListener(final int port, final InetAddress addr) {
            m_port = port;
            m_addr = addr;
        }

        @Override
        public void run() {
            try {
                m_socket = new ServerSocket(m_port, 128, m_addr);
                m_socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
                while (!m_stopped) {
                    try {
                        final Socket s = m_socket.accept();
                        final Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        final InputStream is = s.getInputStream();
                                        final DataInputStream dataIn = new DataInputStream(is);
                                        final int inLength = dataIn.readUnsignedShort();
                                        final byte[] in = new byte[inLength];
                                        dataIn.readFully(in);

                                        final Message query;
                                        byte[] response = null;
                                        try {
                                            query = new Message(in);
                                            LogUtils.debugf(this, "received query: %s", query);
                                            response = generateReply(query, in, in.length, s);
                                        } catch (final IOException e) {
                                            response = formerrMessage(in);
                                        }
                                        LogUtils.debugf(this, "returned response: %s", response == null? null : new Message(response));
                                        if (response != null) {
                                            final DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
                                            dataOut.writeShort(response.length);
                                            dataOut.write(response);
                                        }
                                    } catch (final SocketTimeoutException e) {
                                        throw e;
                                    } catch (final IOException e) {
                                        LogUtils.warnf(this, e, "error while processing socket");
                                    } finally {
                                        try {
                                            s.close();
                                        } catch (final IOException e) {
                                            LogUtils.warnf(this, e, "unable to close TCP socket");
                                        }
                                    }
                                } catch (final SocketTimeoutException e) {
                                    if (LogUtils.isTraceEnabled(this)) {
                                        LogUtils.tracef(this, e, "timed out waiting for request");
                                    }
                                }
                            }
                        });
                        t.start();
                    } catch (final SocketTimeoutException e) {
                        if (LogUtils.isTraceEnabled(this)) {
                            LogUtils.tracef(this, e, "timed out waiting for request");
                        }
                    }
                }
            } catch (final IOException e) {
                LogUtils.warnf(this, e, "unable to serve socket on %s", addrport(m_addr, m_port));
            } finally {
                try {
                    m_socket.close();
                } catch (final IOException e) {
                    LogUtils.debugf(this, e, "error while closing socket");
                }
                m_latch.countDown();
            }
        }
        
        @Override
        public void stop() {
            m_stopped = true;
            try {
                m_latch.await();
            } catch (final InterruptedException e) {
                LogUtils.warnf(this, e, "interrupted while stopping TCP listener");
                Thread.currentThread().interrupt();
            }
        }
    }

    private final class UDPListener implements Stoppable {
        private final int m_port;
        private final InetAddress m_addr;
        private volatile boolean m_stopped = false;
        private CountDownLatch m_latch = new CountDownLatch(1);

        private UDPListener(int port, InetAddress addr) {
            m_port = port;
            m_addr = addr;
        }

        @Override
        public void run() {
            DatagramSocket sock = null;
            try {
                sock = new DatagramSocket(m_port, m_addr);
                sock.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
                final short udpLength = 512;
                byte[] in = new byte[udpLength];
                final DatagramPacket indp = new DatagramPacket(in, in.length);
                DatagramPacket outdp = null;
                while (!m_stopped) {
                    indp.setLength(in.length);
                    try {
                        sock.receive(indp);
                    } catch (final InterruptedIOException e) {
                        continue;
                    }
                    final Message query;
                    byte[] response = null;
                    try {
                        query = new Message(in);
                        response = generateReply(query, in, indp.getLength(), null);
                        if (response == null)
                            continue;
                    } catch (final IOException e) {
                        response = formerrMessage(in);
                    }
                    if (outdp == null)
                        outdp = new DatagramPacket(response, response.length, indp.getAddress(), indp.getPort());
                    else {
                        outdp.setData(response);
                        outdp.setLength(response.length);
                        outdp.setAddress(indp.getAddress());
                        outdp.setPort(indp.getPort());
                    }
                    sock.send(outdp);
                }
            } catch (final IOException e) {
                LogUtils.warnf(this, e, "error in the UDP listener: %s", addrport(m_addr, m_port));
            } finally {
                if (sock != null) {
                    try {
                        sock.close();
                    } catch (final Exception e) {
                        LogUtils.debugf(this, e, "error while closing socket");
                    }
                }
                m_latch.countDown();
            }
        }

        @Override
        public void stop() {
            m_stopped = true;
            try {
                m_latch.await();
            } catch (final InterruptedException e) {
                LogUtils.warnf(this, e, "interrupted while waiting for server to stop");
                Thread.currentThread().interrupt();
            }
        }
    }

    static final int FLAG_DNSSECOK = 1;
    static final int FLAG_SIGONLY = 2;

    final Map<Integer, Cache> m_caches = new HashMap<Integer, Cache>();
    final Map<Name, Zone> m_znames = new HashMap<Name, Zone>();
    final Map<Name, TSIG> m_TSIGs = new HashMap<Name, TSIG>();
    final List<Integer> m_ports = new ArrayList<Integer>();
    final List<InetAddress> m_addresses = new ArrayList<InetAddress>();
    
    final List<Stoppable> m_activeListeners = new ArrayList<Stoppable>();

    private static String addrport(final InetAddress addr, final int port) {
    	return InetAddressUtils.str(addr) + "#" + port;
    }

    public DNSServer(final String conffile) throws IOException, ZoneTransferException, ConfigurationException {
        parseConfiguration(conffile);
    }

    public DNSServer() throws UnknownHostException {
    }

    public void start() throws UnknownHostException {
        initializeDefaults();

        for (final InetAddress addr : m_addresses) {
            for (final Integer port : m_ports) {
                final UDPListener udpListener = new UDPListener(port, addr);
                final Thread udpThread = new Thread(udpListener);
                udpThread.start();
                m_activeListeners.add(udpListener);

                final TCPListener tcpListener = new TCPListener(port, addr);
                final Thread tcpThread = new Thread(tcpListener);
                tcpThread.start();
                m_activeListeners.add(tcpListener);

                LogUtils.infof(this, "listening on %s", addrport(addr, port));
            }
        }
        LogUtils.debugf(this, "finished starting up");
    }

    public void stop() {
        for (final Stoppable listener : m_activeListeners) {
            LogUtils.debugf(this, "stopping %s", listener);
            listener.stop();
            LogUtils.debugf(this, "stopped %s", listener);
        }
    }
    
    protected void parseConfiguration(final String conffile) throws ConfigurationException, IOException,
            ZoneTransferException, UnknownHostException {
        final FileInputStream fs;
        final InputStreamReader isr;
        final BufferedReader br;
        try {
            fs = new FileInputStream(conffile);
            isr = new InputStreamReader(fs);
            br = new BufferedReader(isr);
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "Cannot open %s", conffile);
            throw new ConfigurationException("unable to read from " + conffile, e);
        }

        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens()) {
                    continue;
                }
                final String keyword = st.nextToken();
                if (!st.hasMoreTokens()) {
                    LogUtils.warnf(this, "unable to parse line: %s", line);
                    continue;
                }
                if (keyword.charAt(0) == '#') {
                    continue;
                }
                if (keyword.equals("primary")) {
                    addPrimaryZone(st.nextToken(), st.nextToken());
                } else if (keyword.equals("secondary")) {
                    addSecondaryZone(st.nextToken(), st.nextToken());
                } else if (keyword.equals("cache")) {
                    final Cache cache = new Cache(st.nextToken());
                    m_caches.put(new Integer(DClass.IN), cache);
                } else if (keyword.equals("key")) {
                    final String s1 = st.nextToken();
                    final String s2 = st.nextToken();
                    if (st.hasMoreTokens()) {
                        addTSIG(s1, s2, st.nextToken());
                    } else {
                        addTSIG("hmac-md5", s1, s2);
                    }
                } else if (keyword.equals("port")) {
                    m_ports.add(Integer.valueOf(st.nextToken()));
                } else if (keyword.equals("address")) {
                    final String addr = st.nextToken();
                    m_addresses.add(Address.getByAddress(addr));
                } else {
                    LogUtils.warnf(this, "unknown keyword: %s", keyword);
                }

            }
        } finally {
            fs.close();
        }
    }

    protected void initializeDefaults() throws UnknownHostException {
        if (m_ports.size() == 0) {
            m_ports.add(new Integer(53));
        }

        if (m_addresses.size() == 0) {
            m_addresses.add(Address.getByAddress("0.0.0.0"));
        }
    }

    public void addPort(final int port) {
        m_ports.add(port);
    }

    public void setPorts(final List<Integer> ports) {
        if (m_ports == ports) return;
        m_ports.clear();
        m_ports.addAll(ports);
    }
    
    public void addAddress(final InetAddress address) {
        m_addresses.add(address);
    }
    
    public void setAddresses(final List<InetAddress> addresses) {
        if (m_addresses == addresses) return;
        m_addresses.clear();
        m_addresses.addAll(addresses);
    }

    public void addZone(final Zone zone) {
        m_znames.put(zone.getOrigin(), zone);
    }

    public void addPrimaryZone(final String zname, final String zonefile) throws IOException {
        Name origin = null;
        if (zname != null)
            origin = Name.fromString(zname, Name.root);
        final Zone newzone = new Zone(origin, zonefile);
        m_znames.put(newzone.getOrigin(), newzone);
    }

    public void addSecondaryZone(final String zone, final String remote) throws IOException, ZoneTransferException {
        final Name zname = Name.fromString(zone, Name.root);
        final Zone newzone = new Zone(zname, DClass.IN, remote);
        m_znames.put(zname, newzone);
    }

    public void addTSIG(final String algstr, final String namestr, final String key) throws IOException {
        final Name name = Name.fromString(namestr, Name.root);
        m_TSIGs.put(name, new TSIG(algstr, namestr, key));
    }

    public Cache getCache(final int dclass) {
        Cache c = m_caches.get(dclass);
        if (c == null) {
            c = new Cache(dclass);
            m_caches.put(new Integer(dclass), c);
        }
        return c;
    }

    public Zone findBestZone(final Name name) {
        Zone foundzone = m_znames.get(name);
        if (foundzone != null) {
            return foundzone;
        }
        final int labels = name.labels();
        for (int i = 1; i < labels; i++) {
            final Name tname = new Name(name, i);
            foundzone = m_znames.get(tname);
            if (foundzone != null) {
                return foundzone;
            }
        }
        return null;
    }

    public RRset findExactMatch(final Name name, final int type, final int dclass, final boolean glue) {
        final Zone zone = findBestZone(name);
        if (zone != null) {
            return zone.findExactMatch(name, type);
        } else {
            final RRset[] rrsets;
            final Cache cache = getCache(dclass);
            if (glue) {
                rrsets = cache.findAnyRecords(name, type);
            } else {
                rrsets = cache.findRecords(name, type);
            }
            if (rrsets == null) {
                return null;
            } else {
                return rrsets[0]; /* not quite right */
            }
        }
    }

    void addRRset(final Name name, final Message response, final RRset rrset, final int section, final int flags) {
        for (int s = 1; s <= section; s++) {
            if (response.findRRset(name, rrset.getType(), s)) return;
        }
        if ((flags & FLAG_SIGONLY) == 0) {
            @SuppressWarnings("unchecked")
            final Iterator<Record> it = rrset.rrs();
            while (it.hasNext()) {
                final Record r = it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    response.addRecord(r.withName(name), section);
                } else {
                    response.addRecord(r, section);
                }
            }
        }
        if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
            @SuppressWarnings("unchecked")
            final Iterator<Record> it = rrset.sigs();
            while (it.hasNext()) {
                final Record r = it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    response.addRecord(r.withName(name), section);
                } else {
                    response.addRecord(r, section);
                }
            }
        }
    }

    private final void addSOA(final Message response, final Zone zone) {
        response.addRecord(zone.getSOA(), Section.AUTHORITY);
    }

    private final void addNS(final Message response, final Zone zone, final int flags) {
        final RRset nsRecords = zone.getNS();
        addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
    }

    private final void addCacheNS(final Message response, final Cache cache, final Name name) {
        final SetResponse sr = cache.lookupRecords(name, Type.NS, Credibility.HINT);
        if (!sr.isDelegation()) return;
        final RRset nsRecords = sr.getNS();
        @SuppressWarnings("unchecked")
        final Iterator<Record> it = nsRecords.rrs();
        while (it.hasNext()) {
            final Record r = it.next();
            response.addRecord(r, Section.AUTHORITY);
        }
    }

    private void addGlue(final Message response, final Name name, final int flags) {
        final RRset a = findExactMatch(name, Type.A, DClass.IN, true);
        if (a == null) return;
        addRRset(name, response, a, Section.ADDITIONAL, flags);
    }

    private void addAdditional2(final Message response, final int section, final int flags) {
        final Record[] records = response.getSectionArray(section);
        for (int i = 0; i < records.length; i++) {
            final Record r = records[i];
            final Name glueName = r.getAdditionalName();
            if (glueName != null) addGlue(response, glueName, flags);
        }
    }

    private final void addAdditional(final Message response, final int flags) {
        addAdditional2(response, Section.ANSWER, flags);
        addAdditional2(response, Section.AUTHORITY, flags);
    }

    byte addAnswer(final Message response, final Name name, int type, int dclass, int iterations, int flags) {
        SetResponse sr;
        byte rcode = Rcode.NOERROR;

        if (iterations > 6)
            return Rcode.NOERROR;

        if (type == Type.SIG || type == Type.RRSIG) {
            type = Type.ANY;
            flags |= FLAG_SIGONLY;
        }

        final Zone zone = findBestZone(name);
        if (zone != null)
            sr = zone.findRecords(name, type);
        else {
            sr = getCache(dclass).lookupRecords(name, type, Credibility.NORMAL);
        }

        if (sr.isUnknown()) {
            addCacheNS(response, getCache(dclass), name);
        }
        if (sr.isNXDOMAIN()) {
            response.getHeader().setRcode(Rcode.NXDOMAIN);
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0) response.getHeader().setFlag(Flags.AA);
            }
            rcode = Rcode.NXDOMAIN;
        } else if (sr.isNXRRSET()) {
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0) response.getHeader().setFlag(Flags.AA);
            }
        } else if (sr.isDelegation()) {
            final RRset nsRecords = sr.getNS();
            addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
        } else if (sr.isCNAME()) {
            final CNAMERecord cname = sr.getCNAME();
            addRRset(name, response, new RRset(cname), Section.ANSWER, flags);
            if (zone != null && iterations == 0) response.getHeader().setFlag(Flags.AA);
            rcode = addAnswer(response, cname.getTarget(), type, dclass, iterations + 1, flags);
        } else if (sr.isDNAME()) {
            final DNAMERecord dname = sr.getDNAME();
            RRset rrset = new RRset(dname);
            addRRset(name, response, rrset, Section.ANSWER, flags);
            final Name newname;
            try {
                newname = name.fromDNAME(dname);
            } catch (final NameTooLongException e) {
                return Rcode.YXDOMAIN;
            }
            rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
            addRRset(name, response, rrset, Section.ANSWER, flags);
            if (zone != null && iterations == 0)
                response.getHeader().setFlag(Flags.AA);
            rcode = addAnswer(response, newname, type, dclass, iterations + 1, flags);
        } else if (sr.isSuccessful()) {
            final RRset[] rrsets = sr.answers();
            for (int i = 0; i < rrsets.length; i++)
                addRRset(name, response, rrsets[i], Section.ANSWER, flags);
            if (zone != null) {
                addNS(response, zone, flags);
                if (iterations == 0)
                    response.getHeader().setFlag(Flags.AA);
            } else
                addCacheNS(response, getCache(dclass), name);
        }
        return rcode;
    }

    byte[] doAXFR(final Name name, final Message query, final TSIG tsig, TSIGRecord qtsig, final Socket s) {
        final Zone zone = m_znames.get(name);
        boolean first = true;
        if (zone == null)
            return errorMessage(query, Rcode.REFUSED);
        @SuppressWarnings("unchecked")
        final Iterator<RRset> it = zone.AXFR();
        try {
            final DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            int id = query.getHeader().getID();
            while (it.hasNext()) {
                final RRset rrset = it.next();
                final Message response = new Message(id);
                final Header header = response.getHeader();
                header.setFlag(Flags.QR);
                header.setFlag(Flags.AA);
                addRRset(rrset.getName(), response, rrset, Section.ANSWER, FLAG_DNSSECOK);
                if (tsig != null) {
                    tsig.applyStream(response, qtsig, first);
                    qtsig = response.getTSIG();
                }
                first = false;
                final byte[] out = response.toWire();
                dataOut.writeShort(out.length);
                dataOut.write(out);
            }
        } catch (final IOException ex) {
            LogUtils.warnf(this, ex, "AXFR failed");
        }
        try {
            s.close();
        } catch (final IOException ex) {
            LogUtils.warnf(this, ex, "error closing socket");
        }
        return null;
    }

    /*
     * Note: a null return value means that the caller doesn't need to do
     * anything. Currently this only happens if this is an AXFR request over
     * TCP.
     */
    byte[] generateReply(final Message query, final byte[] in, final int length, final Socket s) throws IOException {
        final Header header = query.getHeader();
        int maxLength;
        int flags = 0;

        if (header.getFlag(Flags.QR))
            return null;
        if (header.getRcode() != Rcode.NOERROR)
            return errorMessage(query, Rcode.FORMERR);
        if (header.getOpcode() != Opcode.QUERY)
            return errorMessage(query, Rcode.NOTIMP);

        final Record queryRecord = query.getQuestion();

        final TSIGRecord queryTSIG = query.getTSIG();
        TSIG tsig = null;
        if (queryTSIG != null) {
            tsig = m_TSIGs.get(queryTSIG.getName());
            if (tsig == null || tsig.verify(query, in, length, null) != Rcode.NOERROR)
                return formerrMessage(in);
        }

        final OPTRecord queryOPT = query.getOPT();

        if (s != null)
            maxLength = 65535;
        else if (queryOPT != null)
            maxLength = Math.max(queryOPT.getPayloadSize(), 512);
        else
            maxLength = 512;

        if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0)
            flags = FLAG_DNSSECOK;

        final Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        if (query.getHeader().getFlag(Flags.RD)) {
            response.getHeader().setFlag(Flags.RD);
        }
        response.addRecord(queryRecord, Section.QUESTION);

        final Name name = queryRecord.getName();
        final int type = queryRecord.getType();
        final int dclass = queryRecord.getDClass();
        if ((type == Type.AXFR || type == Type.IXFR) && s != null)
            return doAXFR(name, query, tsig, queryTSIG, s);
        if (!Type.isRR(type) && type != Type.ANY)
            return errorMessage(query, Rcode.NOTIMP);

        final byte rcode = addAnswer(response, name, type, dclass, 0, flags);
        if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN)
            return errorMessage(query, rcode);

        addAdditional(response, flags);

        if (queryOPT != null) {
            final int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
            final OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0, optflags);
            response.addRecord(opt, Section.ADDITIONAL);
        }

        response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
        return response.toWire(maxLength);
    }

    byte[] buildErrorMessage(final Header header, final int rcode, final Record question) {
        final Message response = new Message();
        response.setHeader(header);
        for (int i = 0; i < 4; i++)
            response.removeAllRecords(i);
        if (rcode == Rcode.SERVFAIL)
            response.addRecord(question, Section.QUESTION);
        header.setRcode(rcode);
        return response.toWire();
    }

    public byte[] formerrMessage(final byte[] in) {
        try {
            return buildErrorMessage(new Header(in), Rcode.FORMERR, null);
        } catch (final IOException e) {
            LogUtils.debugf(this, e, "unable to build error message");
            return null;
        }
    }

    public byte[] errorMessage(final Message query, final int rcode) {
        return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
    }
}
