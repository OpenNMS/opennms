// Refactored from DNSServer in the JDNSS server
//
// Project site for JDNSS says "BSD and GPL license" but this file had no
// specifics about which license it's specifically under, so assume the more
// restrictive GPL until we can get more details.  So:
//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/

package org.opennms.core.test.dns;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.opennms.core.test.ConfigurationException;
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

        public void run() {
            try {
                m_socket = new ServerSocket(m_port, 128, m_addr);
                m_socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
                while (!m_stopped) {
                    try {
                        final Socket s = m_socket.accept();
                        Thread t;
                        t = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    handleTCPRequest(s);
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
            } catch (IOException e) {
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
        return addr.getHostAddress() + "#" + port;
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
                Thread t1 = new Thread(udpListener);
                t1.start();
                m_activeListeners.add(udpListener);

                final TCPListener tcpListener = new TCPListener(port, addr);
                Thread t = new Thread(tcpListener);
                t.start();
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
                StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens())
                    continue;
                final String keyword = st.nextToken();
                if (!st.hasMoreTokens()) {
                    LogUtils.warnf(this, "unable to parse line: %s", line);
                    continue;
                }
                if (keyword.charAt(0) == '#')
                    continue;
                if (keyword.equals("primary"))
                    addPrimaryZone(st.nextToken(), st.nextToken());
                else if (keyword.equals("secondary"))
                    addSecondaryZone(st.nextToken(), st.nextToken());
                else if (keyword.equals("cache")) {
                    Cache cache = new Cache(st.nextToken());
                    m_caches.put(new Integer(DClass.IN), cache);
                } else if (keyword.equals("key")) {
                    String s1 = st.nextToken();
                    String s2 = st.nextToken();
                    if (st.hasMoreTokens())
                        addTSIG(s1, s2, st.nextToken());
                    else
                        addTSIG("hmac-md5", s1, s2);
                } else if (keyword.equals("port")) {
                    m_ports.add(Integer.valueOf(st.nextToken()));
                } else if (keyword.equals("address")) {
                    String addr = st.nextToken();
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
        m_ports.clear();
        m_ports.addAll(ports);
    }
    
    public void addAddress(final InetAddress address) {
        m_addresses.add(address);
    }
    
    public void setAddresses(final List<InetAddress> addresses) {
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
        Zone newzone = new Zone(origin, zonefile);
        m_znames.put(newzone.getOrigin(), newzone);
    }

    public void addSecondaryZone(final String zone, final String remote) throws IOException, ZoneTransferException {
        Name zname = Name.fromString(zone, Name.root);
        Zone newzone = new Zone(zname, DClass.IN, remote);
        m_znames.put(zname, newzone);
    }

    public void addTSIG(String algstr, String namestr, String key) throws IOException {
        Name name = Name.fromString(namestr, Name.root);
        m_TSIGs.put(name, new TSIG(algstr, namestr, key));
    }

    public Cache getCache(int dclass) {
        Cache c = m_caches.get(new Integer(dclass));
        if (c == null) {
            c = new Cache(dclass);
            m_caches.put(new Integer(dclass), c);
        }
        return c;
    }

    public Zone findBestZone(Name name) {
        Zone foundzone = null;
        foundzone = m_znames.get(name);
        if (foundzone != null)
            return foundzone;
        int labels = name.labels();
        for (int i = 1; i < labels; i++) {
            Name tname = new Name(name, i);
            foundzone = m_znames.get(tname);
            if (foundzone != null)
                return foundzone;
        }
        return null;
    }

    public RRset findExactMatch(Name name, int type, int dclass, boolean glue) {
        Zone zone = findBestZone(name);
        if (zone != null)
            return zone.findExactMatch(name, type);
        else {
            RRset[] rrsets;
            Cache cache = getCache(dclass);
            if (glue)
                rrsets = cache.findAnyRecords(name, type);
            else
                rrsets = cache.findRecords(name, type);
            if (rrsets == null)
                return null;
            else
                return rrsets[0]; /* not quite right */
        }
    }

    void addRRset(Name name, Message response, RRset rrset, int section, int flags) {
        for (int s = 1; s <= section; s++)
            if (response.findRRset(name, rrset.getType(), s))
                return;
        if ((flags & FLAG_SIGONLY) == 0) {
            @SuppressWarnings("rawtypes")
            Iterator it = rrset.rrs();
            while (it.hasNext()) {
                Record r = (Record) it.next();
                if (r.getName().isWild() && !name.isWild())
                    r = r.withName(name);
                response.addRecord(r, section);
            }
        }
        if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
            @SuppressWarnings("rawtypes")
            Iterator it = rrset.sigs();
            while (it.hasNext()) {
                Record r = (Record) it.next();
                if (r.getName().isWild() && !name.isWild())
                    r = r.withName(name);
                response.addRecord(r, section);
            }
        }
    }

    private final void addSOA(Message response, Zone zone) {
        response.addRecord(zone.getSOA(), Section.AUTHORITY);
    }

    private final void addNS(Message response, Zone zone, int flags) {
        RRset nsRecords = zone.getNS();
        addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
    }

    private final void addCacheNS(Message response, Cache cache, Name name) {
        SetResponse sr = cache.lookupRecords(name, Type.NS, Credibility.HINT);
        if (!sr.isDelegation())
            return;
        RRset nsRecords = sr.getNS();
        @SuppressWarnings("rawtypes")
        Iterator it = nsRecords.rrs();
        while (it.hasNext()) {
            Record r = (Record) it.next();
            response.addRecord(r, Section.AUTHORITY);
        }
    }

    private void addGlue(Message response, Name name, int flags) {
        RRset a = findExactMatch(name, Type.A, DClass.IN, true);
        if (a == null)
            return;
        addRRset(name, response, a, Section.ADDITIONAL, flags);
    }

    private void addAdditional2(Message response, int section, int flags) {
        Record[] records = response.getSectionArray(section);
        for (int i = 0; i < records.length; i++) {
            Record r = records[i];
            Name glueName = r.getAdditionalName();
            if (glueName != null)
                addGlue(response, glueName, flags);
        }
    }

    private final void addAdditional(Message response, int flags) {
        addAdditional2(response, Section.ANSWER, flags);
        addAdditional2(response, Section.AUTHORITY, flags);
    }

    byte addAnswer(Message response, Name name, int type, int dclass, int iterations, int flags) {
        SetResponse sr;
        byte rcode = Rcode.NOERROR;

        if (iterations > 6)
            return Rcode.NOERROR;

        if (type == Type.SIG || type == Type.RRSIG) {
            type = Type.ANY;
            flags |= FLAG_SIGONLY;
        }

        Zone zone = findBestZone(name);
        if (zone != null)
            sr = zone.findRecords(name, type);
        else {
            Cache cache = getCache(dclass);
            sr = cache.lookupRecords(name, type, Credibility.NORMAL);
        }

        if (sr.isUnknown()) {
            addCacheNS(response, getCache(dclass), name);
        }
        if (sr.isNXDOMAIN()) {
            response.getHeader().setRcode(Rcode.NXDOMAIN);
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0)
                    response.getHeader().setFlag(Flags.AA);
            }
            rcode = Rcode.NXDOMAIN;
        } else if (sr.isNXRRSET()) {
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0)
                    response.getHeader().setFlag(Flags.AA);
            }
        } else if (sr.isDelegation()) {
            RRset nsRecords = sr.getNS();
            addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
        } else if (sr.isCNAME()) {
            CNAMERecord cname = sr.getCNAME();
            RRset rrset = new RRset(cname);
            addRRset(name, response, rrset, Section.ANSWER, flags);
            if (zone != null && iterations == 0)
                response.getHeader().setFlag(Flags.AA);
            rcode = addAnswer(response, cname.getTarget(), type, dclass, iterations + 1, flags);
        } else if (sr.isDNAME()) {
            DNAMERecord dname = sr.getDNAME();
            RRset rrset = new RRset(dname);
            addRRset(name, response, rrset, Section.ANSWER, flags);
            Name newname;
            try {
                newname = name.fromDNAME(dname);
            } catch (NameTooLongException e) {
                return Rcode.YXDOMAIN;
            }
            rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
            addRRset(name, response, rrset, Section.ANSWER, flags);
            if (zone != null && iterations == 0)
                response.getHeader().setFlag(Flags.AA);
            rcode = addAnswer(response, newname, type, dclass, iterations + 1, flags);
        } else if (sr.isSuccessful()) {
            RRset[] rrsets = sr.answers();
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

    byte[] doAXFR(Name name, Message query, TSIG tsig, TSIGRecord qtsig, Socket s) {
        Zone zone = m_znames.get(name);
        boolean first = true;
        if (zone == null)
            return errorMessage(query, Rcode.REFUSED);
        @SuppressWarnings("rawtypes")
        Iterator it = zone.AXFR();
        try {
            DataOutputStream dataOut;
            dataOut = new DataOutputStream(s.getOutputStream());
            int id = query.getHeader().getID();
            while (it.hasNext()) {
                RRset rrset = (RRset) it.next();
                Message response = new Message(id);
                Header header = response.getHeader();
                header.setFlag(Flags.QR);
                header.setFlag(Flags.AA);
                addRRset(rrset.getName(), response, rrset, Section.ANSWER, FLAG_DNSSECOK);
                if (tsig != null) {
                    tsig.applyStream(response, qtsig, first);
                    qtsig = response.getTSIG();
                }
                first = false;
                byte[] out = response.toWire();
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
    byte[] generateReply(Message query, byte[] in, int length, Socket s) throws IOException {
        Header header;
        int maxLength;
        int flags = 0;

        header = query.getHeader();
        if (header.getFlag(Flags.QR))
            return null;
        if (header.getRcode() != Rcode.NOERROR)
            return errorMessage(query, Rcode.FORMERR);
        if (header.getOpcode() != Opcode.QUERY)
            return errorMessage(query, Rcode.NOTIMP);

        Record queryRecord = query.getQuestion();

        TSIGRecord queryTSIG = query.getTSIG();
        TSIG tsig = null;
        if (queryTSIG != null) {
            tsig = m_TSIGs.get(queryTSIG.getName());
            if (tsig == null || tsig.verify(query, in, length, null) != Rcode.NOERROR)
                return formerrMessage(in);
        }

        OPTRecord queryOPT = query.getOPT();

        if (s != null)
            maxLength = 65535;
        else if (queryOPT != null)
            maxLength = Math.max(queryOPT.getPayloadSize(), 512);
        else
            maxLength = 512;

        if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0)
            flags = FLAG_DNSSECOK;

        Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        if (query.getHeader().getFlag(Flags.RD))
            response.getHeader().setFlag(Flags.RD);
        response.addRecord(queryRecord, Section.QUESTION);

        Name name = queryRecord.getName();
        int type = queryRecord.getType();
        int dclass = queryRecord.getDClass();
        if (type == Type.AXFR && s != null)
            return doAXFR(name, query, tsig, queryTSIG, s);
        if (!Type.isRR(type) && type != Type.ANY)
            return errorMessage(query, Rcode.NOTIMP);

        byte rcode = addAnswer(response, name, type, dclass, 0, flags);
        if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN)
            return errorMessage(query, rcode);

        addAdditional(response, flags);

        if (queryOPT != null) {
            int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
            OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0, optflags);
            response.addRecord(opt, Section.ADDITIONAL);
        }

        response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
        return response.toWire(maxLength);
    }

    byte[] buildErrorMessage(Header header, int rcode, Record question) {
        Message response = new Message();
        response.setHeader(header);
        for (int i = 0; i < 4; i++)
            response.removeAllRecords(i);
        if (rcode == Rcode.SERVFAIL)
            response.addRecord(question, Section.QUESTION);
        header.setRcode(rcode);
        return response.toWire();
    }

    public byte[] formerrMessage(byte[] in) {
        Header header;
        try {
            header = new Header(in);
        } catch (IOException e) {
            return null;
        }
        return buildErrorMessage(header, Rcode.FORMERR, null);
    }

    public byte[] errorMessage(Message query, int rcode) {
        return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
    }

    public void handleTCPRequest(final Socket s) throws SocketTimeoutException {
        try {
            final InputStream is = s.getInputStream();
            final DataInputStream dataIn = new DataInputStream(is);
            int inLength = dataIn.readUnsignedShort();
            byte[] in = new byte[inLength];
            dataIn.readFully(in);

            Message query;
            byte[] response = null;
            try {
                query = new Message(in);
                response = generateReply(query, in, in.length, s);
                if (response == null)
                    return;
            } catch (final IOException e) {
                response = formerrMessage(in);
            }
            final DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            dataOut.writeShort(response.length);
            dataOut.write(response);
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
    }
}
