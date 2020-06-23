/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * @author Seth
 */
public abstract class DnsUtils {

    public static InetAddress resolveHostname(final String hostname) throws UnknownHostException {
        return resolveHostname(hostname, false);
    }

    public static InetAddress resolveHostname(final String hostname, final boolean preferInet6Address) throws UnknownHostException {
        return resolveHostname(hostname, preferInet6Address, true);
    }

    /**
     * This function is used inside XSLT documents, do a string search before refactoring.
     */
    public static InetAddress resolveHostname(final String hostname, final boolean preferInet6Address, final boolean throwException) throws UnknownHostException {
        InetAddress retval = null;
        //System.out.println(String.format("%s (%s)", hostname, preferInet6Address ? "6" : "4"));

        // Do a special case for localhost since the DNS server will generally not
        // return valid A and AAAA records for "localhost".
        if ("localhost".equals(hostname)) {
            return preferInet6Address ? InetAddress.getByName("::1") : InetAddress.getByName("127.0.0.1");
        }

        try {
            // 2011-05-22 - Matt is seeing some platform-specific inconsistencies when using
            // InetAddress.getAllByName(). It seems to miss some addresses occasionally on Mac.
            // We need to use dnsjava here instead since it should be 100% reliable.
            //
            // InetAddress[] addresses = InetAddress.getAllByName(hostname);
            //
            List<InetAddress> v4Addresses = new ArrayList<>();
            try {
                Record[] aRecs = new Lookup(hostname, Type.A).run();
                if (aRecs != null) {
                    for (Record aRec : aRecs) {
                        if (aRec instanceof ARecord) {
                            InetAddress addr = ((ARecord)aRec).getAddress();
                            if (addr instanceof Inet4Address) {
                                v4Addresses.add(addr);
                            } else {
                                // Should never happen
                                throw new UnknownHostException("Non-IPv4 address found via A record DNS lookup of host: " + hostname + ": " + addr.toString());
                            }
                        }
                    }
                } else {
                    //throw new UnknownHostException("No IPv4 addresses found via A record DNS lookup of host: " + hostname);
                }
            } catch (final TextParseException e) {
                final UnknownHostException ex = new UnknownHostException("Could not perform A record lookup for host: " + hostname);
                ex.initCause(e);
                throw ex;
            }

            final List<InetAddress> v6Addresses = new ArrayList<>();
            try {
                final Record[] quadARecs = new Lookup(hostname, Type.AAAA).run();
                if (quadARecs != null) {
                    for (final Record quadARec : quadARecs) {
                        final InetAddress addr = ((AAAARecord)quadARec).getAddress();
                        if (addr instanceof Inet6Address) {
                            v6Addresses.add(addr);
                        } else {
                            // Should never happen
                            throw new UnknownHostException("Non-IPv6 address found via AAAA record DNS lookup of host: " + hostname + ": " + addr.toString());
                        }
                    }
                } else {
                    // throw new UnknownHostException("No IPv6 addresses found via AAAA record DNS lookup of host: " + hostname);
                }
            } catch (final TextParseException e) {
                final UnknownHostException ex = new UnknownHostException("Could not perform AAAA record lookup for host: " + hostname);
                ex.initCause(e);
                throw ex;
            }

            final List<InetAddress> addresses = new ArrayList<>();
            if (preferInet6Address) {
                addresses.addAll(v6Addresses);
                addresses.addAll(v4Addresses);
            } else {
                addresses.addAll(v4Addresses);
                addresses.addAll(v6Addresses);
            }

            for (final InetAddress address : addresses) {
                retval = address;
                if (!preferInet6Address && retval instanceof Inet4Address) break;
                if (preferInet6Address && retval instanceof Inet6Address) break;
            }
            if (preferInet6Address && !(retval instanceof Inet6Address)) {
                throw new UnknownHostException("No IPv6 address could be found for the hostname: " + hostname);
            }
        } catch (final UnknownHostException e) {
            if (throwException) {
                throw e;
            } else {
                //System.out.println(String.format("UnknownHostException for : %s (%s)", hostname, preferInet6Address ? "6" : "4"));
                //e.printStackTrace();
                return null;
            }
        }
        return retval;
    }
}
