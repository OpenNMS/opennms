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
package org.opennms.features.vaadin.components.graph;

import org.opennms.core.utils.TimeSeries;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * Stores all of the details required to render the graph.
 *
 * @author jwhite
 */
public class GraphContainerState extends JavaScriptComponentState {
    private static final long serialVersionUID = -6846721022019894325L;

    public String baseHref = "/opennms/";
    public String engine = TimeSeries.getGraphEngine();

    public String graphName;
    public String resourceId;

    public Long start;
    public Long end;

    public String title;

    public Double widthRatio;
    public Double heightRatio;

}
