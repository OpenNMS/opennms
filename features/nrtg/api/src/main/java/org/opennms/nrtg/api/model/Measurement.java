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
package org.opennms.nrtg.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Result of a single metric on a given node/interface to a given time.
 * <p/>
 * @author Christian Pape
 * @author Markus Neumann
 * 
 */
public interface Measurement extends Serializable {

    public void setNodeId(int nodeId);

    public void setNetInterface(String theInterface);

    public void setService(String service);

    public void setMetricId(String metricId);

    public void setMetricType(String metricType);

    public void setValue(String value);

    public void setTimestamp(Date timestamp);

    public void setOnmsLogicMetricId(String onmsLogicMetricId);

    public String getOnmsLogicMetricId();

    public int getNodeId();

    public String getNetInterface();

    public String getService();

    public String getMetricId();

    public String getMetricType();

    public String getValue();

    public Date getTimestamp();
}
