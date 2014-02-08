/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.StringTokenizer;

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.Persister;

/**
 * The Class JMXCollectionAttributeType.
 */
public class JMXCollectionAttributeType implements CollectionAttributeType {

    /** The data source. */
    JMXDataSource m_dataSource;

    /** The group type. */
    AttributeGroupType m_groupType;

    /** The name. */
    String m_name;

    /**
     * Instantiates a new JMX collection attribute type.
     *
     * @param dataSource
     *            the data source
     * @param key
     *            the key
     * @param substitutions
     *            the substitutions
     * @param groupType
     *            the group type
     */
    protected JMXCollectionAttributeType(JMXDataSource dataSource, String key, String substitutions, AttributeGroupType groupType) {
        m_groupType = groupType;
        m_dataSource = dataSource;
        m_name = createName(key, substitutions);
    }

    /**
     * Creates the name.
     *
     * @param key
     *            the key
     * @param substitutions
     *            the substitutions
     * @return the string
     */
    private String createName(String key, String substitutions) {
        String name = m_dataSource.getName();
        if (key != null && !key.equals("")) {
            name = fixKey(key, m_dataSource.getName(), substitutions) + "_" + name;
        }
        return name;
    }

    /*
     * This method strips out the illegal character '/' and attempts to keep
     * the length of the key plus ds name to 19 or less characters. The slash
     * character cannot be in the name since it is an illegal character in
     * file names.
     */
    /**
     * Fix key.
     *
     * @param key
     *            the key
     * @param attrName
     *            the attr name
     * @param substitutions
     *            the substitutions
     * @return the string
     */
    private String fixKey(String key, String attrName, String substitutions) {
        String newKey = key;
        if (key.startsWith(File.separator)) {
            newKey = key.substring(1);
        }
        if (substitutions != null && substitutions.length() > 0) {
            StringTokenizer st = new StringTokenizer(substitutions, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int index = token.indexOf('|');
                if (newKey.equals(token.substring(0, index))) {
                    newKey = token.substring(index + 1);
                }
            }
        }
        return newKey;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionAttributeType#getGroupType()
     */
    @Override
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.AttributeDefinition#getType()
     */
    @Override
    public String getType() {
        return m_dataSource.getType();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.netmgt.config.collector.CollectionAttributeType#storeAttribute(org.opennms.netmgt.config.collector
     * .CollectionAttribute, org.opennms.netmgt.config.collector.Persister)
     */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        // Only numeric data comes back from JMX in data collection
        persister.persistNumericAttribute(attribute);
    }
}
