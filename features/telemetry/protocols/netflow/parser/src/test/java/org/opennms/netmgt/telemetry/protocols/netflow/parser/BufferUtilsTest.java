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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.sfloat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.sint;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint24;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint64;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BufferUtilsTest {

    @Test
    public void testSignedInteger() throws Exception {
        Assert.assertEquals(Long.valueOf(0), sint(from("000000"), 3));
        Assert.assertEquals(Long.valueOf(-1), sint(from("FFFFFF"), 3));
        Assert.assertEquals(Long.valueOf(-2), sint(from("FFFFFE"), 3));
        Assert.assertEquals(Long.valueOf(1), sint(from("000001"), 3));
        Assert.assertEquals(Long.valueOf(2), sint(from("000002"), 3));
    }

    @Test
    public void testUnsignedInteger32() throws Exception {
        Assert.assertEquals(0L, uint32(from("00000000")));
        Assert.assertEquals(1L, uint32(from("00000001")));
        Assert.assertEquals(1024L, uint32(from("00000400")));
        Assert.assertEquals(65536L - 1, uint32(from("0000FFFF")));
        Assert.assertEquals(65536L * 65536L - 1L, uint32(from("FFFFFFFF")));
    }

    @Test
    public void testUnsignedInteger64() throws Exception {
        Assert.assertEquals(UnsignedLong.valueOf(0L), uint64(from("0000000000000000")));
        Assert.assertEquals(UnsignedLong.valueOf(1L), uint64(from("0000000000000001")));
        Assert.assertEquals(UnsignedLong.valueOf(1024L), uint64(from("0000000000000400")));
        Assert.assertEquals(UnsignedLong.valueOf(65536L -1L), uint64(from("000000000000FFFF")));
        Assert.assertEquals(UnsignedLong.valueOf(65536L * 65536L - 1L), uint64(from("00000000FFFFFFFF")));
        Assert.assertEquals(UnsignedLong.MAX_VALUE, uint64(from("FFFFFFFFFFFFFFFF")));
    }

    @Test
    public void testSignedFloat() throws Exception {
        Assert.assertEquals(1.47F, sfloat(from("3FBC28F6")), 0.0);
        Assert.assertEquals(-1.47F, sfloat(from("BFBC28F6")), 0.0);
        Assert.assertEquals(0.0F, sfloat(from("00000000")), 0.0);
    }

    @Test
    public void testUnsigned() throws Exception {
        // This is random data chosen from the serial number of the finger print of the LDAP server of the university on the applied science of the fulda
        Assert.assertEquals(UnsignedLong.valueOf(0x20L), uint(from("207138408FABED99"), 1));
        Assert.assertEquals(UnsignedLong.valueOf(0x2071L), uint(from("207138408FABED99"), 2));
        Assert.assertEquals(UnsignedLong.valueOf(0x207138L), uint(from("207138408FABED99"), 3));
        Assert.assertEquals(UnsignedLong.valueOf(0x20713840L), uint(from("207138408FABED99"), 4));
        Assert.assertEquals(UnsignedLong.valueOf(0x207138408fL), uint(from("207138408FABED99"), 5));
        Assert.assertEquals(UnsignedLong.valueOf(0x207138408fabL), uint(from("207138408FABED99"), 6));
        Assert.assertEquals(UnsignedLong.valueOf(0x207138408fabedL), uint(from("207138408FABED99"), 7));
        Assert.assertEquals(UnsignedLong.valueOf(0x207138408fabed99L), uint(from("207138408FABED99"), 8));

        Assert.assertEquals(uint8(from("207138408FABED99")), uint(from("207138408FABED99"), 1).intValue());
        Assert.assertEquals(uint16(from("207138408FABED99")), uint(from("207138408FABED99"), 2).intValue());
        Assert.assertEquals(uint24(from("207138408FABED99")), uint(from("207138408FABED99"), 3).intValue());
        Assert.assertEquals(uint32(from("207138408FABED99")), uint(from("207138408FABED99"), 4).intValue());
        Assert.assertEquals(uint64(from("207138408FABED99")), uint(from("207138408FABED99"), 8));
    }

    private static ByteBuf from(final String hex) {
        return Unpooled.wrappedBuffer(BaseEncoding.base16().decode(hex));
    }
}
