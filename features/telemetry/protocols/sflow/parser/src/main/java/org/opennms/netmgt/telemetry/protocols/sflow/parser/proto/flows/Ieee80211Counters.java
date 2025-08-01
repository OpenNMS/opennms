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

// struct ieee80211_counters {
//    unsigned int dot11TransmittedFragmentCount;
//    unsigned int dot11MulticastTransmittedFrameCount;
//    unsigned int dot11FailedCount;
//    unsigned int dot11RetryCount;
//    unsigned int dot11MultipleRetryCount;
//    unsigned int dot11FrameDuplicateCount;
//    unsigned int dot11RTSSuccessCount;
//    unsigned int dot11RTSFailureCount;
//    unsigned int dot11ACKFailureCount;
//    unsigned int dot11ReceivedFragmentCount;
//    unsigned int dot11MulticastReceivedFrameCount;
//    unsigned int dot11FCSErrorCount;
//    unsigned int dot11TransmittedFrameCount;
//    unsigned int dot11WEPUndecryptableCount;
//    unsigned int dot11QoSDiscardedFragmentCount;
//    unsigned int dot11AssociatedStationCount;
//    unsigned int dot11QoSCFPollsReceivedCount;
//    unsigned int dot11QoSCFPollsUnusedCount;
//    unsigned int dot11QoSCFPollsUnusableCount;
//    unsigned int dot11QoSCFPollsLostCount;
// };

public class Ieee80211Counters implements CounterData {
    public final long dot11TransmittedFragmentCount;
    public final long dot11MulticastTransmittedFrameCount;
    public final long dot11FailedCount;
    public final long dot11RetryCount;
    public final long dot11MultipleRetryCount;
    public final long dot11FrameDuplicateCount;
    public final long dot11RTSSuccessCount;
    public final long dot11RTSFailureCount;
    public final long dot11ACKFailureCount;
    public final long dot11ReceivedFragmentCount;
    public final long dot11MulticastReceivedFrameCount;
    public final long dot11FCSErrorCount;
    public final long dot11TransmittedFrameCount;
    public final long dot11WEPUndecryptableCount;
    public final long dot11QoSDiscardedFragmentCount;
    public final long dot11AssociatedStationCount;
    public final long dot11QoSCFPollsReceivedCount;
    public final long dot11QoSCFPollsUnusedCount;
    public final long dot11QoSCFPollsUnusableCount;
    public final long dot11QoSCFPollsLostCount;

    public Ieee80211Counters(final ByteBuf buffer) throws InvalidPacketException {
        this.dot11TransmittedFragmentCount = BufferUtils.uint32(buffer);
        this.dot11MulticastTransmittedFrameCount = BufferUtils.uint32(buffer);
        this.dot11FailedCount = BufferUtils.uint32(buffer);
        this.dot11RetryCount = BufferUtils.uint32(buffer);
        this.dot11MultipleRetryCount = BufferUtils.uint32(buffer);
        this.dot11FrameDuplicateCount = BufferUtils.uint32(buffer);
        this.dot11RTSSuccessCount = BufferUtils.uint32(buffer);
        this.dot11RTSFailureCount = BufferUtils.uint32(buffer);
        this.dot11ACKFailureCount = BufferUtils.uint32(buffer);
        this.dot11ReceivedFragmentCount = BufferUtils.uint32(buffer);
        this.dot11MulticastReceivedFrameCount = BufferUtils.uint32(buffer);
        this.dot11FCSErrorCount = BufferUtils.uint32(buffer);
        this.dot11TransmittedFrameCount = BufferUtils.uint32(buffer);
        this.dot11WEPUndecryptableCount = BufferUtils.uint32(buffer);
        this.dot11QoSDiscardedFragmentCount = BufferUtils.uint32(buffer);
        this.dot11AssociatedStationCount = BufferUtils.uint32(buffer);
        this.dot11QoSCFPollsReceivedCount = BufferUtils.uint32(buffer);
        this.dot11QoSCFPollsUnusedCount = BufferUtils.uint32(buffer);
        this.dot11QoSCFPollsUnusableCount = BufferUtils.uint32(buffer);
        this.dot11QoSCFPollsLostCount = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dot11TransmittedFragmentCount", this.dot11TransmittedFragmentCount)
                .add("dot11MulticastTransmittedFrameCount", this.dot11MulticastTransmittedFrameCount)
                .add("dot11FailedCount", this.dot11FailedCount)
                .add("dot11RetryCount", this.dot11RetryCount)
                .add("dot11MultipleRetryCount", this.dot11MultipleRetryCount)
                .add("dot11FrameDuplicateCount", this.dot11FrameDuplicateCount)
                .add("dot11RTSSuccessCount", this.dot11RTSSuccessCount)
                .add("dot11RTSFailureCount", this.dot11RTSFailureCount)
                .add("dot11ACKFailureCount", this.dot11ACKFailureCount)
                .add("dot11ReceivedFragmentCount", this.dot11ReceivedFragmentCount)
                .add("dot11MulticastReceivedFrameCount", this.dot11MulticastReceivedFrameCount)
                .add("dot11FCSErrorCount", this.dot11FCSErrorCount)
                .add("dot11TransmittedFrameCount", this.dot11TransmittedFrameCount)
                .add("dot11WEPUndecryptableCount", this.dot11WEPUndecryptableCount)
                .add("dot11QoSDiscardedFragmentCount", this.dot11QoSDiscardedFragmentCount)
                .add("dot11AssociatedStationCount", this.dot11AssociatedStationCount)
                .add("dot11QoSCFPollsReceivedCount", this.dot11QoSCFPollsReceivedCount)
                .add("dot11QoSCFPollsUnusedCount", this.dot11QoSCFPollsUnusedCount)
                .add("dot11QoSCFPollsUnusableCount", this.dot11QoSCFPollsUnusableCount)
                .add("dot11QoSCFPollsLostCount", this.dot11QoSCFPollsLostCount)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("dot11TransmittedFragmentCount", this.dot11TransmittedFragmentCount);
        bsonWriter.writeInt64("dot11MulticastTransmittedFrameCount", this.dot11MulticastTransmittedFrameCount);
        bsonWriter.writeInt64("dot11FailedCount", this.dot11FailedCount);
        bsonWriter.writeInt64("dot11RetryCount", this.dot11RetryCount);
        bsonWriter.writeInt64("dot11MultipleRetryCount", this.dot11MultipleRetryCount);
        bsonWriter.writeInt64("dot11FrameDuplicateCount", this.dot11FrameDuplicateCount);
        bsonWriter.writeInt64("dot11RTSSuccessCount", this.dot11RTSSuccessCount);
        bsonWriter.writeInt64("dot11RTSFailureCount", this.dot11RTSFailureCount);
        bsonWriter.writeInt64("dot11ACKFailureCount", this.dot11ACKFailureCount);
        bsonWriter.writeInt64("dot11ReceivedFragmentCount", this.dot11ReceivedFragmentCount);
        bsonWriter.writeInt64("dot11MulticastReceivedFrameCount", this.dot11MulticastReceivedFrameCount);
        bsonWriter.writeInt64("dot11FCSErrorCount", this.dot11FCSErrorCount);
        bsonWriter.writeInt64("dot11TransmittedFrameCount", this.dot11TransmittedFrameCount);
        bsonWriter.writeInt64("dot11WEPUndecryptableCount", this.dot11WEPUndecryptableCount);
        bsonWriter.writeInt64("dot11QoSDiscardedFragmentCount", this.dot11QoSDiscardedFragmentCount);
        bsonWriter.writeInt64("dot11AssociatedStationCount", this.dot11AssociatedStationCount);
        bsonWriter.writeInt64("dot11QoSCFPollsReceivedCount", this.dot11QoSCFPollsReceivedCount);
        bsonWriter.writeInt64("dot11QoSCFPollsUnusedCount", this.dot11QoSCFPollsUnusedCount);
        bsonWriter.writeInt64("dot11QoSCFPollsUnusableCount", this.dot11QoSCFPollsUnusableCount);
        bsonWriter.writeInt64("dot11QoSCFPollsLostCount", this.dot11QoSCFPollsLostCount);
        bsonWriter.writeEndDocument();
    }
}
