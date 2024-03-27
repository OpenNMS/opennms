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
package org.opennms.core.utils;

import java.util.Comparator;

/**
 * Comparator that is used to compare byte arrays. This should be used to compare
 * IP addresses using {@link java.net.InetAddress#getAddress()} and can be used to
 * compare any pair of IPv4 and/or IPv6 addresses.
 * 
 * @author Seth &lt;seth@opennms.org&gt;
 */
public class ByteArrayComparator implements Comparator<byte[]> {

    @Override
    public int compare(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            // Make shorter byte arrays "less than" longer arrays
            int comparison = Integer.valueOf(a.length).compareTo(Integer.valueOf(b.length));
            if (comparison != 0) {
                return comparison;
            } else {
                // Compare byte-by-byte
                for (int i = 0; i < a.length; i++) {
                    int byteComparison = Integer.valueOf(unsignedByteToInt(a[i])).compareTo(Integer.valueOf(unsignedByteToInt(b[i])));
                    if (byteComparison != 0) {
                        return byteComparison;
                    }
                }
                // OK both arrays are the same length and every byte is identical so they are equal
                return 0;
            }
        }
    }

    private static int unsignedByteToInt(byte b) {
        return b < 0 ? ((int)b)+256 : ((int)b);
    }
}