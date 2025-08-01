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
