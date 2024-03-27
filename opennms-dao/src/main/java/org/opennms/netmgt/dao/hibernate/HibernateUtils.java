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
package org.opennms.netmgt.dao.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class HibernateUtils {

    /**
     * Uses hibernate to get all appropriate table column names for the given Hibernate model class.
     *
     * @param factory               The active Hibernate session
     * @param klass                 The Hibernate model object
     * @param includeTablePrefix    If true, each column name will have the table name added as a prefix
     * @return                      A lower-case list of valid column names for the given model class.
     */
    public static List<String> getHibernateTableColumnNames(SessionFactory factory, Class klass, boolean includeTablePrefix) {

        List<String> validColumnNames = new ArrayList<>();
        AbstractEntityPersister aep = (AbstractEntityPersister) factory.getClassMetadata(klass);
        for(int propertyIndex = 0; propertyIndex < aep.getPropertyNames().length; propertyIndex++) {
            for (String columnName : aep.getPropertyColumnNames(propertyIndex)) {
                if (columnName == null) {
                    continue;
                }
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

    /**
     * Checks that all given column names, if not null, are appropriate table column names for the given
     * Hibernate model class.
     *
     * @param factory               Active hibernate session factory
     * @param klasses               Hibernate model object classes
     * @param includeTablePrefix    If true, the given column names include the table name as a prefix
     * @param columnNames           List of column names to check the validity of.
     *
     * @throws IllegalArgumentException If any of the column names are invalid.
     */
    public static void validateHibernateColumnNames(final SessionFactory factory, final List<Class> klasses, final boolean includeTablePrefix, final String ... columnNames) {
        List<String> validColumnNames = new ArrayList<>();
        for(final Class klass : klasses) {
            validColumnNames.addAll(getHibernateTableColumnNames(factory, klass, includeTablePrefix));
        }
        for (String columnName : columnNames) {
            if (columnName != null && !validColumnNames.contains(columnName)) {
                throw new IllegalArgumentException(String.format("Invalid column name specified: %s", columnName));
            }
        }
    }

    public static void validateHibernateColumnNames(final SessionFactory factory, final Class klass, final boolean includeTablePrefix, final String ... columnNames) {
        validateHibernateColumnNames(factory, Lists.newArrayList(klass), includeTablePrefix, columnNames);
    }
}
