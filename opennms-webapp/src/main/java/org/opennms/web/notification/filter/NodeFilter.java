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
package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="node"</code> */
    public static final String TYPE = "node";

    //protected int nodeId;

    /**
     * <p>Constructor for NodeFilter.</p>
     *
     * @param nodeId a int.
     */
    public NodeFilter(int nodeId) {
        super(TYPE, SQLType.INT, "NODEID", "node.id", nodeId);
        //this.nodeId = nodeId;
    }

//    public String getSql() {
//        return (" NODEID=" + this.nodeId);
//    }
//    
//    public String getParamSql() {
//        return (" NODEID=?");
//    }
//    
//    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setInt(parameterIndex, this.nodeId);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.nodeId);
//    }
//
//    public String getTextDescription() {
//        String nodeName = Integer.toString(this.nodeId);
//        try {
//            nodeName = NetworkElementFactory.getNodeLabel(this.nodeId);
//        } catch (SQLException e) {
//        }
//
//        return (TYPE + "=" + nodeName);
//    }
//
//    public String toString() {
//        return ("<NoticeFactory.NodeFilter: " + this.getDescription() + ">");
//    }
//
//    public int getNodeId() {
//        return (this.nodeId);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
