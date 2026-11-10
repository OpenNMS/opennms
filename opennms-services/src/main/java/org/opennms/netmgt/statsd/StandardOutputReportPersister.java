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
package org.opennms.netmgt.statsd;

import java.util.Date;
import java.util.SortedSet;

import org.opennms.netmgt.model.AttributeStatistic;

/**
 * <p>StandardOutputReportPersister class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class StandardOutputReportPersister implements ReportPersister {
    /** {@inheritDoc} */
    @Override
    public void persist(ReportInstance report) {
        System.out.println("Top " + report.getCount() + " " + report.getAttributeMatch() + " data sources on resources of type " + report.getResourceTypeMatch() + " from " + new Date(report.getStartTime()) + " to " + new Date(report.getEndTime()));
        SortedSet<AttributeStatistic> top = report.getResults();
        for (AttributeStatistic stat : top) {
            System.out.println(stat.getAttribute().getResource().getId() + "/" + stat.getAttribute().getName() + ": " + stat.getStatistic());
        }
        System.out.println("");
    }
}
