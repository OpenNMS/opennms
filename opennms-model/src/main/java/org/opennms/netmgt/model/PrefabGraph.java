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
package org.opennms.netmgt.model;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

/**
 * <p>PrefabGraph class.</p>
 */
@XmlRootElement(name = "prefab-graph")
@XmlAccessorType(XmlAccessType.NONE)
public class PrefabGraph extends Object implements Comparable<PrefabGraph> {

    @XmlAttribute(name = "name")
    private String m_name;

    @XmlAttribute(name = "title")
    private String m_title;

    @XmlElement(name = "columns")
    private String[] m_columns;

    @XmlElement(name = "command")
    private String m_command;

    @XmlElement(name = "externalValues")
    private String[] m_externalValues;

    @XmlElement(name = "propertiesValues")
    private String[] m_propertiesValues;

    @XmlAttribute(name = "order")
    private int m_order;

    @XmlElement(name = "types")
    private String[] m_types = new String[0];

    @XmlAttribute(name = "description")
    private String m_description;

    @XmlAttribute(name = "width")
    private Integer m_graphWidth;

    @XmlAttribute(name = "height")
    private Integer m_graphHeight;

    @XmlElement(name = "suppress")
    private String[] m_suppress;

    @SuppressWarnings("unused")
    private PrefabGraph() {
        throw new UnsupportedOperationException("No-arg constructor for JAXB.");
    }

    /**
     * <p>Constructor for PrefabGraph.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param title a {@link java.lang.String} object.
     * @param columns an array of {@link java.lang.String} objects.
     * @param command a {@link java.lang.String} object.
     * @param externalValues an array of {@link java.lang.String} objects.
     * @param propertiesValues an array of {@link java.lang.String} objects.
     * @param order a int.
     * @param types an array of {@link java.lang.String} objects.
     * @param description a {@link java.lang.String} object.
     * @param graphWidth a {@link java.lang.Integer} object.
     * @param graphHeight a {@link java.lang.Integer} object.
     * @param suppress an array of {@link java.lang.String} objects.
     */
    public PrefabGraph(String name, String title, String[] columns,
            String command, String[] externalValues,
            String[] propertiesValues, int order, String[] types,
            String description, Integer graphWidth, Integer graphHeight,
            String[] suppress) {
        Assert.notNull(name, "name argument cannot be null");
        Assert.notNull(title, "title argument cannot be null");
        Assert.notNull(columns, "columns argument cannot be null");
        Assert.notNull(command, "command argument cannot be null");
        Assert.notNull(externalValues, "externalValues argument cannot be null");
        Assert.notNull(propertiesValues, "propertiesValues argument cannot be null");
        Assert.notNull(suppress, "suppress argument cannot be null");

        m_name = name;
        m_title = title;
        m_columns = Arrays.copyOf(columns, columns.length);
        m_command = command;
        m_externalValues = Arrays.copyOf(externalValues, externalValues.length);
        m_propertiesValues = Arrays.copyOf(propertiesValues, propertiesValues.length);
        m_order = order;
        m_suppress = Arrays.copyOf(suppress, suppress.length);

        m_types = types == null ? null : Arrays.copyOf(types, types.length);
        m_description = description;
        m_graphWidth = graphWidth;
        m_graphHeight = graphHeight;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * <p>getOrder</p>
     *
     * @return a int.
     */
    public int getOrder() {
        return m_order;
    }

    /**
     * <p>getColumns</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getColumns() {
        return m_columns;
    }

    /**
     * <p>getCommand</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommand() {
        return m_command;
    }

    /**
     * <p>getExternalValues</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getExternalValues() {
        return m_externalValues;
    }

    /**
     * <p>getPropertiesValues</p>
     *
     * @return list of required string properties
     * @see org.opennms.netmgt.model.StringPropertyAttribute
     */
    public String[] getPropertiesValues() {
        return m_propertiesValues;
    }

    /**
     * Can be null.
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getTypes() {
        return m_types;
    }
    
    /**
     * <p>hasMatchingType</p>
     *
     * @param matchingTypes a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasMatchingType(String... matchingTypes) {
        for (String matchingType : matchingTypes) {
            for (String type : m_types) {
                if (type != null && type.equals(matchingType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Can be null.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * Can be null.
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getGraphWidth() {
        return m_graphWidth;
    }

    /**
     * Can be null.
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getGraphHeight() {
        return m_graphHeight;
    }
    
    /**
     * <p>getSuppress</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getSuppress() {
        return m_suppress;
    }
    
    /**
     * <p>compareTo</p>
     *
     * @param other a {@link org.opennms.netmgt.model.PrefabGraph} object.
     * @return a int.
     */
    @Override
    public int compareTo(PrefabGraph other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return getOrder() - other.getOrder();
    }
}
