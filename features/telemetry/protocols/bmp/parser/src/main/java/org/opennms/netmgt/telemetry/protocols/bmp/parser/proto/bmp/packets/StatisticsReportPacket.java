/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.repeatCount;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint32;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibOut;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicatePrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateUpdate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateWithdraws;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.ExportRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsConfedLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsPathLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToClusterListLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToOriginatorId;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.LocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Metric;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiAdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiLocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PrefixTreatAsWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Rejected;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.UpdateTreatAsWithdraw;

import com.google.common.base.MoreObjects;

public class StatisticsReportPacket implements Packet {
    public final Header header;
    public final PeerHeader peerHeader;

    public final TLV.List<Element, Element.Type, Metric> statistics;

    public StatisticsReportPacket(final Header header, final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.statistics = TLV.List.wrap(repeatCount(buffer, (int) uint32(buffer), Element::new));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static class Element extends TLV<Element.Type, Metric, Void> {

        public Element(final ByteBuffer buffer) throws InvalidPacketException {
            super(buffer, Element.Type::from, null);
        }

        public enum Type implements TLV.Type<Metric, Void> {
            REJECTED(Rejected::new),
            DUPLICATE_PREFIX(DuplicatePrefix::new),
            DUPLICATE_WITHDRAW(DuplicateWithdraws::new),
            INVALID_UPDATE_DUE_TO_CLUSTER_LIST_LOOP(InvalidUpdateDueToClusterListLoop::new),
            INVALID_UPDATE_DUE_TO_AS_PATH_LOOP(InvalidUpdateDueToAsPathLoop::new),
            INVALID_UPDATE_DUE_TO_ORIGINATOR_ID(InvalidUpdateDueToOriginatorId::new),
            INVALID_UPDATE_DUE_TO_AS_CONFED_LOOP(InvalidUpdateDueToAsConfedLoop::new),
            ADJ_RIB_IN(AdjRibIn::new),
            LOC_RIB(LocRib::new),
            PER_AFI_ADJ_RIB_IN(PerAfiAdjRibIn::new),
            PER_AFI_LOC_RIB(PerAfiLocRib::new),
            UPDATE_TREAT_AS_WITHDRAW(UpdateTreatAsWithdraw::new),
            PREFIX_TREAT_AS_WITHDRAW(PrefixTreatAsWithdraw::new),
            DUPLICATE_UPDATE(DuplicateUpdate::new),
            ADJ_RIB_OUT(AdjRibOut::new),
            EXPORT_RIB(ExportRib::new),
            ;

            private final Function<ByteBuffer, Metric> parser;

            private Type(final Function<ByteBuffer, Metric> parser) {
                this.parser = Objects.requireNonNull(parser);
            }

            private static Type from(final int type) {
                switch (type) {
                    case 0: return REJECTED;
                    case 1: return DUPLICATE_PREFIX;
                    case 2: return DUPLICATE_WITHDRAW;
                    case 3: return INVALID_UPDATE_DUE_TO_CLUSTER_LIST_LOOP;
                    case 4: return INVALID_UPDATE_DUE_TO_AS_PATH_LOOP;
                    case 5: return INVALID_UPDATE_DUE_TO_ORIGINATOR_ID;
                    case 6: return INVALID_UPDATE_DUE_TO_AS_CONFED_LOOP;
                    case 7: return ADJ_RIB_IN;
                    case 8: return LOC_RIB;
                    case 9: return PER_AFI_ADJ_RIB_IN;
                    case 10: return PER_AFI_LOC_RIB;
                    case 11: return UPDATE_TREAT_AS_WITHDRAW;
                    case 12: return PREFIX_TREAT_AS_WITHDRAW;
                    case 13: return DUPLICATE_UPDATE;
                    case 14: return ADJ_RIB_OUT;
                    case 15: return EXPORT_RIB;

                    default:
                        throw new IllegalArgumentException("Unknown statistic type");
                }
            }

            @Override
            public Metric parse(final ByteBuffer buffer, final Void parameter) throws InvalidPacketException {
                return this.parser.apply(buffer);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("statistics", this.statistics)
                .toString();
    }
}
