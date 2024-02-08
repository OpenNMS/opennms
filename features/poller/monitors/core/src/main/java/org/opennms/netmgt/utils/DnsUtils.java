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
