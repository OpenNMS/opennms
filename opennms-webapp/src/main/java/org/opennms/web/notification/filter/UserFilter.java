/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.notification.filter;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
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
        
            
        return Restrictions.sqlRestriction(" {alias}.notifyId in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)", getValue(), Hibernate.STRING);
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
        return (this.toString().equals(obj.toString()));
    }

}
