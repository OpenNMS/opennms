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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records;

import java.net.InetAddress;
import java.time.Instant;

import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;

public class UnicastPrefix extends Record {
    public Action action;
    public Long sequence;
    public String hash; // Hash of fields [ prefix, prefix length, peer hash, path_id, 1 if has label(s) ]
    public String routerHash;

    public InetAddress routerIp;
    public String baseAttrHash;
    public String peerHash;
    public InetAddress peerIp;
    public Long peerAsn;
    public Instant timestamp;
    public InetAddress prefix;
    public Integer length;
    public boolean ipv4;
    public String origin;
    public String asPath;
    public Integer asPathCount;
    public Long originAs;
    public InetAddress nextHop;
    public Long med;
    public Long localPref;
    public String aggregator;
    public String communityList;
    public String extCommunityList;
    public String clusterList;
    public boolean atomicAgg;
    public boolean nextHopIpv4;
    public String originatorId;
    public Long pathId;
    public String labels;
    public boolean prePolicy;
    public boolean adjIn;
    public String largeCommunityList;

    public UnicastPrefix() {
        super(Type.UNICAST_PREFIX);
    }

    @Override
    protected String[] fields() {
        return new String[]{
                this.action != null ? this.action.value : null,
                nullSafeStr(this.sequence),
                this.hash,
                this.routerHash,
                nullSafeStr(this.routerIp),
                this.baseAttrHash,
                this.peerHash,
                nullSafeStr(this.peerIp),
                nullSafeStr(this.peerAsn),
                formatTimestamp(this.timestamp),
                nullSafeStr(this.prefix),
                nullSafeStr(this.length),
                boolAsInt(this.ipv4),
                this.origin,
                this.asPath,
                nullSafeStr(this.asPathCount),
                nullSafeStr(this.originAs),
                nullSafeStr(this.nextHop),
                nullSafeStr(this.med),
                nullSafeStr(this.localPref),
                this.aggregator,
                this.communityList,
                this.extCommunityList,
                this.clusterList,
                boolAsInt(this.atomicAgg),
                boolAsInt(this.nextHopIpv4),
                this.originatorId,
                nullSafeStr(this.pathId),
                this.labels,
                boolAsInt(this.prePolicy),
                boolAsInt(this.adjIn),
                this.largeCommunityList
        };
    }

    public enum Action {
        ADD("add"),
        DELETE("del");

        public final String value;

        Action(final String value) {
            this.value = value;
        }
    }
}
