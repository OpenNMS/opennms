/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;

public class MultiprotocolReachableNlri implements Attribute {
    public static final Logger LOG = LoggerFactory.getLogger(MultiprotocolReachableNlri.class);

    final static int BGP_AFI_IPV4 = 1;
    final static int BGP_AFI_IPV6 = 2;
    final static int BGP_AFI_L2VPN = 25;
    final static int BGP_AFI_BGPLS = 16388;

    final static int BGP_SAFI_UNICAST = 1;
    final static int BGP_SAFI_MULTICAST = 2;
    final static int BGP_SAFI_NLRI_LABEL = 4;
    final static int BGP_SAFI_MCAST_VPN = 5;
    final static int BGP_SAFI_VPLS = 65;
    final static int BGP_SAFI_MDT = 66;
    final static int BGP_SAFI_4over6 = 67;
    final static int BGP_SAFI_6over4 = 68;
    final static int BGP_SAFI_EVPN = 70;
    final static int BGP_SAFI_BGPLS = 71;
    final static int BGP_SAFI_MPLS = 128;
    final static int BGP_SAFI_MCAST_MPLS_VPN = 129;
    final static int BGP_SAFI_RT_CONSTRAINS = 132;

    final static int PREFIX_UNICAST_V4 = 1;
    final static int PREFIX_UNICAST_V6 = 2;
    final static int PREFIX_LABEL_UNICAST_V4 = 3;
    final static int PREFIX_LABEL_UNICAST_V6 = 4;
    final static int PREFIX_VPN_V4 = 5;
    final static int PREFIX_VPN_v6 = 6;
    final static int PREFIX_MULTICAST_V4 = 7;

    final static int NLRI_TYPE_NODE = 1;
    final static int NLRI_TYPE_LINK = 2;
    final static int NLRI_TYPE_IPV4_PREFIX = 3;
    final static int NLRI_TYPE_IPV6_PREFIX = 4;

    public final int afi;
    public final int safi;
    public int length;
    public byte[] nextHopBytes;
    public InetAddress nextHop;
    public List<UpdatePacket.Prefix> advertised = Lists.newArrayList();
    public List<UpdatePacket.Prefix> vpnAdvertised = Lists.newArrayList();

    public MultiprotocolReachableNlri(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        this.afi = BufferUtils.uint16(buffer);
        this.safi = BufferUtils.uint8(buffer);
        this.length = BufferUtils.uint8(buffer);
        this.nextHopBytes = BufferUtils.bytes(buffer, length);
        BufferUtils.skip(buffer, 1);
        try {
            parseAfi(buffer, peerInfo);
        } catch (UnknownHostException ex) {
            throw new InvalidPacketException(buffer, "Error parsing IP address", ex);
        } catch (Exception ex) {
            throw new InvalidPacketException(buffer, "Error parsing packet", ex);
        }
    }

    void parseAfi(final ByteBuf buffer, final Optional<PeerInfo> peerInfo) throws Exception {
        switch (this.afi) {
            case BGP_AFI_IPV6:
                parseAfi_IPv4IPv6(false, buffer, peerInfo);
                break;
            case BGP_AFI_IPV4:
                parseAfi_IPv4IPv6(true, buffer, peerInfo);
                break;
            case BGP_AFI_BGPLS:
                nextHop = InetAddressUtils.getInetAddress(nextHopBytes);
                LOG.info("MP_REACH AFI=bgp-ls SAFI={} is not implemented yet, skipping for now", safi);
                break;
            case BGP_AFI_L2VPN:
                if (nextHopBytes.length > 16) {
                    nextHop = InetAddressUtils.getInetAddress(Arrays.copyOf(nextHopBytes, 16));
                } else {
                    nextHop = InetAddressUtils.getInetAddress(nextHopBytes);
                }
                LOG.info("EVPN AFI=bgp_afi_l2vpn SAFI={} is not implemented yet, skipping", safi);
                break;
            default:
                LOG.info("MP_REACH AFI={} is not implemented yet, skipping", afi);
                break;
        }
    }

    void parseAfi_IPv4IPv6(boolean isIPv4, final ByteBuf buffer, final Optional<PeerInfo> peerInfo) throws Exception {
        switch (this.safi) {
            case BGP_SAFI_UNICAST:
                if (nextHopBytes.length > 16) {
                    nextHop = InetAddressUtils.getInetAddress(Arrays.copyOf(nextHopBytes, 16));
                } else {
                    nextHop = InetAddressUtils.getInetAddress(nextHopBytes);
                }
                this.advertised = parseNlriData_IPv4IPv6(isIPv4, buffer, peerInfo);
                break;
            case BGP_SAFI_NLRI_LABEL:
                if (nextHopBytes.length > 16) {
                    nextHop = InetAddressUtils.getInetAddress(Arrays.copyOf(nextHopBytes, 16));
                } else {
                    nextHop = InetAddressUtils.getInetAddress(nextHopBytes);
                }
                this.advertised = parseNlriData_LabelIPv4IPv6(isIPv4, buffer, peerInfo, false);
                break;
            case BGP_SAFI_MPLS:
                if (isIPv4) {
                    this.length -= 8;
                    byte[] bytes = new byte[4];
                    System.arraycopy(nextHopBytes, 8, bytes, 0, 4);
                    nextHopBytes = bytes;
                }
                if (nextHopBytes.length != 4) {
                    nextHop = InetAddressUtils.getInetAddress(Arrays.copyOf(nextHopBytes, 16));
                } else {
                    nextHop = InetAddressUtils.getInetAddress(nextHopBytes);
                }
                this.vpnAdvertised = parseNlriData_LabelIPv4IPv6(isIPv4, buffer, peerInfo, true);
                break;
            default:
                LOG.info("MP_REACH AFI=ipv4/ipv6 ({}) SAFI={} is not implemented yet, skipping for now", isIPv4, this.safi);
        }
    }

    static List<UpdatePacket.Prefix> parseNlriData_IPv4IPv6(boolean isIPv4, final ByteBuf buffer, final Optional<PeerInfo> peerInfo) {
        final boolean addPathCapabilityEnabled = peerInfo.map(info -> info.isAddPathEnabled(isIPv4 ? BGP_AFI_IPV4 : BGP_AFI_IPV6, BGP_SAFI_UNICAST)).orElse(false);

        return BufferUtils.repeatRemaining(buffer, b -> {
            final UpdatePacket.Prefix tuple = new UpdatePacket.Prefix();

            if (addPathCapabilityEnabled) {
                tuple.pathId = BufferUtils.uint32(b);
            }

            tuple.length = BufferUtils.uint8(b);
            final int byteCount = tuple.length / 8 + (tuple.length % 8 > 0 ? 1 : 0);
            final byte[] prefixBytes = BufferUtils.bytes(b, byteCount);

            tuple.prefix = isIPv4 ? InetAddressUtils.getInetAddress(Arrays.copyOf(prefixBytes, 4)) : InetAddressUtils.getInetAddress(Arrays.copyOf(prefixBytes, 16));

            return tuple;
        });
    }

    static List<UpdatePacket.Prefix> parseNlriData_LabelIPv4IPv6(boolean isIPv4, final ByteBuf buffer, final Optional<PeerInfo> peerInfo, boolean isVPN) throws Exception {
        final boolean addPathCapabilityEnabled = peerInfo.map(info -> info.isAddPathEnabled(isIPv4 ? BGP_AFI_IPV4 : BGP_AFI_IPV6, isVPN ? BGP_SAFI_MPLS : BGP_SAFI_NLRI_LABEL)).orElse(false);
        return BufferUtils.repeatRemaining(buffer, b -> {
            final UpdatePacket.Prefix tuple = new UpdatePacket.Prefix();

            if (addPathCapabilityEnabled && !isVPN && b.readableBytes() >= 4) {
                tuple.pathId = BufferUtils.uint32(b);
            } else {
                tuple.pathId = 0;
            }

            tuple.length = BufferUtils.uint8(b);
            int byteCount = tuple.length / 8 + (tuple.length % 8 > 0 ? 1 : 0);

            final List<String> labels = decodeLabel(b);
            tuple.labels = String.join(",", labels);

            byteCount = byteCount - (labels.size() * 3);
            tuple.length = tuple.length - (8 * 3 * labels.size());

            if (isVPN && byteCount >= 8) {
                final int type = BufferUtils.uint16(b);
                switch (type) {
                    case 0:
                        // Administrator subfield: 2 bytes, ASN
                        // Assigned Number subfield: 4 bytes, Number space number
                        BufferUtils.skip(b, 6);
                        break;
                    case 1:
                        // Administrator subfield: 4 bytes, IP Address
                        // Assigned Number subfield: 2 bytes, Number space number
                        BufferUtils.skip(b, 6);
                        break;
                    case 2:
                        // Administrator subfield: 4 bytes, 4-byte ASN
                        // Assigned Number subfield: 2 bytes, Number space number
                        BufferUtils.skip(b, 6);
                        break;
                }
                byteCount -= 8;
                tuple.length -= 64;
            }

            if (byteCount > 0) {
                final byte[] prefixBytes = BufferUtils.bytes(b, byteCount);
                tuple.prefix = isIPv4 ? InetAddressUtils.getInetAddress(Arrays.copyOf(prefixBytes, 4)) : InetAddressUtils.getInetAddress(Arrays.copyOf(prefixBytes, 16));
            } else {
                tuple.prefix = InetAddressUtils.addr(isIPv4 ? "0.0.0.0" : "::");
            }

            return tuple;
        });
    }

    private static List<String> decodeLabel(final ByteBuf buffer) {
        final List<String> labels = new ArrayList<>();

        while (buffer.readableBytes() > 0) {
            final int data = BufferUtils.uint24(buffer) & 0x000FFFFF;

            final int label = data >> 4;
            final int exp = (data & 0x0000000E) >> 1;
            final int bos = (data & 0x00000001);

            labels.add(String.valueOf(label));

            if (bos == 1 || data == 0x80000000 || data == 0) {
                break;
            }
        }
        return labels;
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
