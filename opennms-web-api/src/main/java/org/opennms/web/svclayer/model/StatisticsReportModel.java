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
package org.opennms.web.svclayer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.StatisticsReport;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

/**
 * Model object for web statistics reports.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class StatisticsReportModel {

    /**
     * This class extends OnmsResource and overrides the toString() method, providing
     * a more human-readable description of the resource.
     * 
     * @author jeffg
     */
    public static class PrettyOnmsResource extends OnmsResource {
        /**
         * <p>Constructor for PrettyOnmsResource.</p>
         *
         * @param rs a {@link org.opennms.netmgt.model.OnmsResource} object.
         */
        public PrettyOnmsResource(OnmsResource rs) {
            super(rs.getName(), rs.getLabel(), rs.getResourceType(), rs.getAttributes(), rs.getChildResources(), rs.getPath());
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        @Override
        public String toString() {
            return this.getResourceType().getLabel() + ": " + this.getLabel();
        }
    }

    public static class Datum implements Comparable<Datum> {
        private Double m_value;
        private OnmsResource m_resource;
    
        @Override
        public int compareTo(Datum o) {
            return m_value.compareTo(o.getValue());
        }
    
        public OnmsResource getResource() {
            return m_resource;
        }
        
        public OnmsResource getPrettyResource() {
            return new PrettyOnmsResource(m_resource);
        }
    
        public void setResource(OnmsResource resource) {
            m_resource = resource;
        }
        
        public String getResourceParentLabel() {
            Assert.notNull(m_resource, "the resource must be set before calling this method");
            
            final StringBuilder buffer = new StringBuilder();
            
            OnmsResource parent = m_resource.getParent();
            while (parent != null) {
                if (buffer.length() > 0) {
                    buffer.append("<br/>");
                }
                buffer.append(parent.getResourceType().getLabel());
                buffer.append(": ");
                buffer.append(parent.getLabel());
                
                parent = parent.getParent();
            }
            
            return buffer.toString();
        }
        
        public List<OnmsResource> getResourceParentsReversed() {
            if (m_resource == null) {
                return new ArrayList<OnmsResource>(0);
            }
            
            List<OnmsResource> resources = new ArrayList<>();
            
            OnmsResource parent = m_resource.getParent();
            while (parent != null) {
                resources.add(0, parent);
                parent = parent.getParent();
            }
            
            return resources;
        }
        
        public List<OnmsResource> getPrettyResourceParentsReversed() {
            if (m_resource == null) {
                return new ArrayList<OnmsResource>(0);
            }
            
            List<OnmsResource> resources = new ArrayList<>();
            
            OnmsResource parent = new PrettyOnmsResource(m_resource.getParent());
            while (parent != null) {
                resources.add(0, new PrettyOnmsResource(parent));
                parent = parent.getParent();
            }
            
            return resources;
        }
        
        public Double getValue() {
            return m_value;
        }
    
        public void setValue(Double value) {
            m_value = value;
        }
    }
    
    private BindingResult m_errors;
    private StatisticsReport m_report;
    private SortedSet<Datum> m_data = new TreeSet<>();
    
    /**
     * <p>getData</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    public SortedSet<Datum> getData() {
        return m_data;
    }
    /**
     * <p>setData</p>
     *
     * @param data a {@link java.util.SortedSet} object.
     */
    public void setData(SortedSet<StatisticsReportModel.Datum> data) {
        m_data = data;
    }
    /**
     * <p>addData</p>
     *
     * @param datum a {@link org.opennms.web.svclayer.model.StatisticsReportModel.Datum} object.
     */
    public void addData(Datum datum) {
        m_data.add(datum);
    }
    /**
     * <p>getErrors</p>
     *
     * @return a {@link org.springframework.validation.BindingResult} object.
     */
    public BindingResult getErrors() {
        return m_errors;
    }
    /**
     * <p>setErrors</p>
     *
     * @param errors a {@link org.springframework.validation.BindingResult} object.
     */
    public void setErrors(BindingResult errors) {
        m_errors = errors;
    }
    /**
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.netmgt.model.StatisticsReport} object.
     */
    public StatisticsReport getReport() {
        return m_report;
    }
    /**
     * <p>setReport</p>
     *
     * @param report a {@link org.opennms.netmgt.model.StatisticsReport} object.
     */
    public void setReport(StatisticsReport report) {
        m_report = report;
    }
    
}
