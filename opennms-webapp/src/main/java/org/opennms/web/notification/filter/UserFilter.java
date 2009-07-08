/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.notification.filter;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;



/** Encapsulates all user filtering functionality. */
public class UserFilter extends OneArgFilter<String> {
    public static final String TYPE = "user";

    public UserFilter(String user) {
        super(TYPE, SQLType.STRING, "NOTIFICATIONS.NOTIFYID", "notifyId", user);
    }
    
    
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=%s)";
    }


    @Override
    public Criterion getCriterion() {
        
            
        return Restrictions.sqlRestriction(" {alias}.notifyId in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)", getValue(), Hibernate.STRING);
    }


    @Override
    public String getTextDescription() {
        return getValue() + " was notified";
    }

    public String toString() {
        return ("<NoticeFactory.UserFilter: " + this.getDescription() + ">");
    }

    public String getUser() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }

}