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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto;

public enum Type {
    COLLECTOR {
        @Override
        public String toString() {
            return "collector";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.collector";
        }
    },
    ROUTER {
        @Override
        public String toString() {
            return "route";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.router";
        }
    },
    PEER {
        @Override
        public String toString() {
            return "peer";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.peer";
        }
    },
    BASE_ATTRIBUTE {
        @Override
        public String toString() {
            return "base_attribute";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.base_attribute";
        }
    },
    UNICAST_PREFIX {
        @Override
        public String toString() {
            return "unicast_prefix";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.unicast_prefix";
        }
    },
    L3VPN {
        @Override
        public String toString() {
            return "l3vpn";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.l3vpn";
        }
    },
    EVPN {
        @Override
        public String toString() {
            return "evpn";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.evpn";
        }
    },
    LS_LINK {
        @Override
        public String toString() {
            return "ls_link";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.ls_link";
        }
    },
    LS_NODE {
        @Override
        public String toString() {
            return "ls_node";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.ls_node";
        }
    },
    LS_PREFIX {
        @Override
        public String toString() {
            return "ls_prefix";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.ls_prefix";
        }
    },
    BMP_STAT {
        @Override
        public String toString() {
            return "bmp_stat";
        }

        @Override
        public String getTopic() {
            return "openbmp.parsed.bmp_stat";
        }
    },
    BMP_RAW {
        @Override
        public String toString() {
            return "bmp_raw";
        }

        @Override
        public String getTopic() {
            return "openbmp.bmp_raw";
        }
    },
    ;

    Type() {
    }

    public abstract String toString();

    public abstract String getTopic();
}
