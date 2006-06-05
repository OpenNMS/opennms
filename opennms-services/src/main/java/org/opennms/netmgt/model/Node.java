//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

/**
 * @hibernate.class table="node" dynamic-update="true"
 *
 * @author brozow
 */
public class Node {
    
    /*
    --########################################################################
    --# node Table - Contains information on nodes discovered and potentially
    --#              managed by OpenNMS.  nodeSys* fields map to SNMP MIB 2
    --#              system table information.
    --#
    --# This table contains the following fields:
    --#
    --#  nodeID          : Unique identifier for node.  Note that this is the
    --#                    enabler for overlapping IP ranges and that uniquity
    --#                    is dependent on combination of dpName & IP address
    --#  dpName          : Distributed Poller responsible for this node
    --#  nodeCreateTime  : Time node was added to the database
    --#  nodeParentID    : In the case that the node is virtual or an independent
    --#                    device in a chassis that should be reflected as a
    --#                    subcomponent or "child", this field reflects the nodeID
    --#                    of the chassis/physical node/"parent" device.
    --#                    Currently unused.
    --#  nodeType        :  Flag indicating status of node
    --#         'A' - active
    --#             'D' - deleted
    --#  nodeSysOID      : SNMP MIB-2 system.sysObjectID.0
    --#  nodeSysName     : SNMP MIB-2 system.sysName.0
    --#  nodeSysDescription    : SNMP MIB-2 system.sysDescr.0
    --#  nodeSysLocation : SNMP MIB-2 system.sysLocation.0
    --#  nodeSysContact  : SNMP MIB-2 system.sysContact.0
    --#  nodeLabel       : User-friendly name associated with the node.
    --#  nodeLabelSource : Flag indicating source of nodeLabel
    --#                      'U' = user defined
    --#                      'H' = IP hostname
    --#                      'S' = sysName
    --#                      'A' = IP address
    --# nodeNetBIOSName  : NetBIOS workstation name associated with the node.
    --# nodeDomainName   : NetBIOS damain name associated with the node.
    --# operatingSystem  : Operating system running on the node.
    --# lastCapsdPoll    : Date and time of last Capsd scan.
    --########################################################################

    create table node (
        nodeID      integer not null,
        dpName      varchar(12),
        nodeCreateTime  timestamp without time zone not null,
        nodeParentID    integer,
        nodeType    char(1),
        nodeSysOID  varchar(256),
        nodeSysName varchar(256),
        nodeSysDescription  varchar(256),
        nodeSysLocation varchar(256),
        nodeSysContact  varchar(256),
        nodeLabel   varchar(256),
        nodeLabelSource char(1),
            nodeNetBIOSName varchar(16),
        nodeDomainName  varchar(16),
        operatingSystem varchar(64),
        lastCapsdPoll   timestamp without time zone,

        constraint pk_nodeID primary key (nodeID),
        constraint fk_dpName foreign key (dpName) references distPoller
    );

    create index node_id_type_idx on node(nodeID, nodeType);
    create index node_label_idx on node(nodeLabel);

    ipInterface
        constraint fk_nodeID1 foreign key (nodeID) references node ON DELETE CASCADE
        
    snmpInterface
        constraint fk_nodeID2 foreign key (nodeID) references node ON DELETE CASCADE

    ifServices
        constraint fk_nodeID3 foreign key (nodeID) references node ON DELETE CASCADE,

    events
        constraint fk_nodeID6 foreign key (nodeID) references node ON DELETE CASCADE
        
    outages
        constraint fk_nodeID4 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,

    notifications
        constraint fk_nodeID7 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,
        
    alarms
        alarms references nodeID but has no foreign key constraint
        
    assets
        constraint fk_nodeID5 foreign key (nodeID) references node ON DELETE CASCADE
        
    vulnerability
        this references nodeId... do we actually use it?

    



*/
    Long m_id;
    String m_label;
    
    /**
     * @hibernate.id generator-class="sequence"
     * 
     * @return Long
     */
    public Long getId() {
            return m_id;
    }


    
    public void setLabel(String label) {
        m_label = label;
    }
    
    /**
     * @hibernate.property
     * 
     * @return String
     */
    public String getLabel() {
        return m_label;
    }

}
