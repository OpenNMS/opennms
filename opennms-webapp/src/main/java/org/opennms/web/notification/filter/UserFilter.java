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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;



/**
 * Encapsulates all user filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class UserFilter extends OneArgFilter<String> {
    /** Constant <code>TYPE="user"</code> */
    public static final String TYPE = "user";

    /**
     * <p>Constructor for UserFilter.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public UserFilter(String user) {
        super(TYPE, SQLType.STRING, "NOTIFICATIONS.NOTIFYID", "notifyId", user);
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=%s)";
    }


    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        
            
        return Restrictions.sqlRestriction(" {alias}.notifyId in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)", getValue(), StringType.INSTANCE);
    }


    /** {@inheritDoc} */
    @Override
    public String getTextDescription() {
        return getValue() + " was notified";
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<NoticeFactory.UserFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }

}
