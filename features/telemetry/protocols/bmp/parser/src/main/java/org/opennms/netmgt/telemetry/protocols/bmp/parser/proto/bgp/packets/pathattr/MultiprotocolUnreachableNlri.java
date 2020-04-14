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

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;

public class MultiprotocolUnreachableNlri implements Attribute {
    public static final Logger LOG = LoggerFactory.getLogger(MultiprotocolUnreachableNlri.class);

    public final int afi;
    public final int safi;
    public List<UpdatePacket.Prefix> withdrawn = Lists.newArrayList();
    public List<UpdatePacket.Prefix> vpnWithdrawn = Lists.newArrayList();

    public MultiprotocolUnreachableNlri(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        this.afi = BufferUtils.uint16(buffer);
        this.safi = BufferUtils.uint8(buffer);
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
            case MultiprotocolReachableNlri.BGP_AFI_IPV6:
                parseAfi_IPv4IPv6(false, buffer, peerInfo);
                break;
            case MultiprotocolReachableNlri.BGP_AFI_IPV4:
                parseAfi_IPv4IPv6(true, buffer, peerInfo);
                break;
            case MultiprotocolReachableNlri.BGP_AFI_BGPLS:
                LOG.info("MP_UNREACH AFI=bgp-ls SAFI={} is not implemented yet, skipping for now", safi);
                break;
            case MultiprotocolReachableNlri.BGP_AFI_L2VPN:
                LOG.info("EVPN AFI=bgp_afi_l2vpn SAFI={} is not implemented yet, skipping", safi);
                break;
            default:
                LOG.info("MP_UNREACH AFI={} is not implemented yet, skipping", afi);
                break;
        }
    }

    void parseAfi_IPv4IPv6(boolean isIPv4, final ByteBuf buffer, final Optional<PeerInfo> peerInfo) throws Exception {
        switch (this.safi) {
            case MultiprotocolReachableNlri.BGP_SAFI_UNICAST:
                this.withdrawn = MultiprotocolReachableNlri.parseNlriData_IPv4IPv6(isIPv4, buffer, peerInfo);
                break;
            case MultiprotocolReachableNlri.BGP_SAFI_NLRI_LABEL:
                this.withdrawn = MultiprotocolReachableNlri.parseNlriData_LabelIPv4IPv6(isIPv4, buffer, peerInfo, false);
                break;
            case MultiprotocolReachableNlri.BGP_SAFI_MPLS:
                this.vpnWithdrawn = MultiprotocolReachableNlri.parseNlriData_LabelIPv4IPv6(isIPv4, buffer, peerInfo, true);
                break;
            default:
                LOG.info("MP_UNREACH AFI=ipv4/ipv6 ({}) SAFI={} is not implemented yet, skipping for now", isIPv4, this.safi);
        }
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
