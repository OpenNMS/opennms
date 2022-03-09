/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.persister.entity.AbstractEntityPersister;

import java.util.ArrayList;
import java.util.List;

public class HibernateUtils {

    /**
     * Uses hibernate to get all appropriate table column names for the given Hibernate model class.
     *
     * @param session               The active Hibernate session
     * @param klass                 The Hibernate model object
     * @param includeTablePrefix    If true, each column name will have the table name added as a prefix
     * @return                      A lower-case list of valid column names for the given model class.
     */
    public static List<String> getHibernateTableColumnNames(Session session, Class klass, boolean includeTablePrefix) {

        List<String> validColumnNames = new ArrayList<>();
        AbstractEntityPersister aep = (AbstractEntityPersister) session.getSessionFactory().getClassMetadata(klass);
        for(int propertyIndex = 0; propertyIndex < aep.getPropertyNames().length; propertyIndex++) {
            for (String columnName : aep.getPropertyColumnNames(propertyIndex)) {
                if (includeTablePrefix) {
                    validColumnNames.add(String.format("%s.%s", aep.getTableName().toLowerCase(), columnName.toLowerCase()));
                }
                else {
                    validColumnNames.add(columnName.toLowerCase());
                }
            }
        }
        // Identifiers aren't considered 'properties', but can have columns
        for(String identifierColumn : aep.getIdentifierColumnNames()) {
            if (includeTablePrefix) {
                validColumnNames.add(String.format("%s.%s", aep.getTableName().toLowerCase(), identifierColumn.toLowerCase()));
            }
            else {
                validColumnNames.add(identifierColumn.toLowerCase());
            }
        }

        return validColumnNames;
    }


}
