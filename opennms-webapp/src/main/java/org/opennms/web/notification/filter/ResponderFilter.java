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
 * Encapsulates all responder filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ResponderFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="responder"</code> */
    public static final String TYPE = "responder";

    //protected String responder;

    /**
     * <p>Constructor for ResponderFilter.</p>
     *
     * @param responder a {@link java.lang.String} object.
     */
    public ResponderFilter(String responder) {
        super(TYPE, SQLType.STRING, "ANSWEREDBY", "answeredBy", responder);
        //this.responder = responder;
    }

//    public String getSql() {
//        return (" ANSWEREDBY='" + this.responder + "'");
//    }
//    
//    public String getParamSql() {
//        return (" ANSWEREDBY=?");
//    }
//    
//    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, this.responder);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.responder);
//    }
//
//    public String getTextDescription() {
//        return this.getDescription();
//    }
//
//    public String toString() {
//        return ("<NoticeFactory.ResponderFilter: " + this.getDescription() + ">");
//    }
//
//    public String getResponder() {
//        return (this.responder);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
