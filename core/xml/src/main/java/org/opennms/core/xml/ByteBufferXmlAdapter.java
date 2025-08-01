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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.ByteBuffer;

public class ByteBufferXmlAdapter extends XmlAdapter<byte[], ByteBuffer> {

    @Override
    public ByteBuffer unmarshal(byte[] bytes) {
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public byte[] marshal(ByteBuffer bb) {
        if (bb.hasArray()) {
            // Use the backing array when available
            return bb.array();
        } else {
            // Otherwise, create a new array, and copy the available
            // bytes while preserving the original position
            final int originalPosition = bb.position();
            bb.rewind();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            bb.position(originalPosition);
            return bytes;
        }
    }

}
