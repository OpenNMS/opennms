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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct mib2_tcp_group {
//   unsigned int tcpRtoAlgorithm;
//   unsigned int tcpRtoMin;
//   unsigned int tcpRtoMax;
//   unsigned int tcpMaxConn;
//   unsigned int tcpActiveOpens;
//   unsigned int tcpPassiveOpens;
//   unsigned int tcpAttemptFails;
//   unsigned int tcpEstabResets;
//   unsigned int tcpCurrEstab;
//   unsigned int tcpInSegs;
//   unsigned int tcpOutSegs;
//   unsigned int tcpRetransSegs;
//   unsigned int tcpInErrs;
//   unsigned int tcpOutRsts;
//   unsigned int tcpInCsumErrs;
// };

public class Mib2TcpGroup implements CounterData {
    public final long tcpRtoAlgorithm;
    public final long tcpRtoMin;
    public final long tcpRtoMax;
    public final long tcpMaxConn;
    public final long tcpActiveOpens;
    public final long tcpPassiveOpens;
    public final long tcpAttemptFails;
    public final long tcpEstabResets;
    public final long tcpCurrEstab;
    public final long tcpInSegs;
    public final long tcpOutSegs;
    public final long tcpRetransSegs;
    public final long tcpInErrs;
    public final long tcpOutRsts;
    public final long tcpInCsumErrs;

    public Mib2TcpGroup(final ByteBuf buffer) throws InvalidPacketException {
        this.tcpRtoAlgorithm = BufferUtils.uint32(buffer);
        this.tcpRtoMin = BufferUtils.uint32(buffer);
        this.tcpRtoMax = BufferUtils.uint32(buffer);
        this.tcpMaxConn = BufferUtils.uint32(buffer);
        this.tcpActiveOpens = BufferUtils.uint32(buffer);
        this.tcpPassiveOpens = BufferUtils.uint32(buffer);
        this.tcpAttemptFails = BufferUtils.uint32(buffer);
        this.tcpEstabResets = BufferUtils.uint32(buffer);
        this.tcpCurrEstab = BufferUtils.uint32(buffer);
        this.tcpInSegs = BufferUtils.uint32(buffer);
        this.tcpOutSegs = BufferUtils.uint32(buffer);
        this.tcpRetransSegs = BufferUtils.uint32(buffer);
        this.tcpInErrs = BufferUtils.uint32(buffer);
        this.tcpOutRsts = BufferUtils.uint32(buffer);
        this.tcpInCsumErrs = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tcpRtoAlgorithm", this.tcpRtoAlgorithm)
                .add("tcpRtoMin", this.tcpRtoMin)
                .add("tcpRtoMax", this.tcpRtoMax)
                .add("tcpMaxConn", this.tcpMaxConn)
                .add("tcpActiveOpens", this.tcpActiveOpens)
                .add("tcpPassiveOpens", this.tcpPassiveOpens)
                .add("tcpAttemptFails", this.tcpAttemptFails)
                .add("tcpEstabResets", this.tcpEstabResets)
                .add("tcpCurrEstab", this.tcpCurrEstab)
                .add("tcpInSegs", this.tcpInSegs)
                .add("tcpOutSegs", this.tcpOutSegs)
                .add("tcpRetransSegs", this.tcpRetransSegs)
                .add("tcpInErrs", this.tcpInErrs)
                .add("tcpOutRsts", this.tcpOutRsts)
                .add("tcpInCsumErrs", this.tcpInCsumErrs)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("tcpRtoAlgorithm", this.tcpRtoAlgorithm);
        bsonWriter.writeInt64("tcpRtoMin", this.tcpRtoMin);
        bsonWriter.writeInt64("tcpRtoMax", this.tcpRtoMax);
        bsonWriter.writeInt64("tcpMaxConn", this.tcpMaxConn);
        bsonWriter.writeInt64("tcpActiveOpens", this.tcpActiveOpens);
        bsonWriter.writeInt64("tcpPassiveOpens", this.tcpPassiveOpens);
        bsonWriter.writeInt64("tcpAttemptFails", this.tcpAttemptFails);
        bsonWriter.writeInt64("tcpEstabResets", this.tcpEstabResets);
        bsonWriter.writeInt64("tcpCurrEstab", this.tcpCurrEstab);
        bsonWriter.writeInt64("tcpInSegs", this.tcpInSegs);
        bsonWriter.writeInt64("tcpOutSegs", this.tcpOutSegs);
        bsonWriter.writeInt64("tcpRetransSegs", this.tcpRetransSegs);
        bsonWriter.writeInt64("tcpInErrs", this.tcpInErrs);
        bsonWriter.writeInt64("tcpOutRsts", this.tcpOutRsts);
        bsonWriter.writeInt64("tcpInCsumErrs", this.tcpInCsumErrs);
        bsonWriter.writeEndDocument();
    }

}
