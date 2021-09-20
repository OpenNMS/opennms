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
