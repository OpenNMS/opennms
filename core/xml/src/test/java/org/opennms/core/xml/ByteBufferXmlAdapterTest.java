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
package org.opennms.core.xml;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteBufferXmlAdapterTest {

    private final ByteBufferXmlAdapter adapter = new ByteBufferXmlAdapter();

    /**
     * Verifies that a {@link ByteBuffer} can be successfully marshalled
     * when {@link ByteBuffer#hasArray()} is <code>true</code>.
     */
    @Test
    public void marshalByteBufferWithArray() {
        ByteBuffer bbWithArray = ByteBuffer.allocate(1);
        bbWithArray.put((byte)42);
        assertTrue("bytebuffer array should be accessbile", bbWithArray.hasArray());

        byte bytes[] = adapter.marshal(bbWithArray);
        assertArrayEquals(new byte[]{42}, bytes);
    }

    /**
     * Verifies that a {@link ByteBuffer} can be successfully marshalled
     * when {@link ByteBuffer#hasArray()} is <code>false</code>.
     */
    @Test
    public void marshalByteBufferWithoutArray() {
        ByteBuffer bbWithArray = ByteBuffer.allocate(1);
        bbWithArray.put((byte)42);

        assertTrue("bytebuffer array should be accessbile", bbWithArray.hasArray());
        ByteBuffer bbWithoutArray = bbWithArray.asReadOnlyBuffer();
        assertFalse("bytebuffer array should not be accessbile", bbWithoutArray.hasArray());

        byte bytes[] = adapter.marshal(bbWithoutArray);
        assertArrayEquals(new byte[]{42}, bytes);
    }
}
