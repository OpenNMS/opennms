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

import java.util.Set;

/**
 * This class defines how the AggregateStatus object is to be
 * created and it's properties are to be populated.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AggregateStatusDefinition {
    
    private int m_id;
    private String m_name;
    private String m_reportCategory;
    private Set<OnmsCategory> m_categories;
    
    /**
     * <p>Constructor for AggregateStatusDefinition.</p>
     */
    public AggregateStatusDefinition() {
        
    }
    
    /**
     * <p>Constructor for AggregateStatusDefinition.</p>
     *
     * @param aggrStatus a {@link java.lang.String} object.
     * @param categories a {@link java.util.Set} object.
     */
    public AggregateStatusDefinition(String aggrStatus, Set<OnmsCategory> categories) {
        if (aggrStatus == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        m_name = aggrStatus;
        m_categories = categories;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
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
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = name;
	}
    

	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<OnmsCategory> getCategories() {
        return m_categories;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.Set} object.
     */
    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * <p>getReportCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportCategory() {
        return m_reportCategory;
    }

    /**
     * <p>setReportCategory</p>
     *
     * @param reportCategory a {@link java.lang.String} object.
     */
    public void setReportCategory(String reportCategory) {
        m_reportCategory = reportCategory;
    }

}
