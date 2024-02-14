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
 * <p>ReportInstance interface.</p>
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface ReportInstance {

    /**
     * <p>walk</p>
     */
    void walk();

    /**
     * <p>getResults</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    SortedSet<AttributeStatistic> getResults();

    /**
     * <p>getResourceTypeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getResourceTypeMatch();

    /**
     * <p>setResourceTypeMatch</p>
     *
     * @param resourceType a {@link java.lang.String} object.
     */
    void setResourceTypeMatch(String resourceType);

    /**
     * <p>getAttributeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAttributeMatch();

    /**
     * <p>setAttributeMatch</p>
     *
     * @param attr a {@link java.lang.String} object.
     */
    void setAttributeMatch(String attr);

    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    long getStartTime();

    /**
     * <p>setStartTime</p>
     *
     * @param start a long.
     */
    void setStartTime(long start);

    /**
     * <p>getEndTime</p>
     *
     * @return a long.
     */
    long getEndTime();

    /**
     * <p>setEndTime</p>
     *
     * @param end a long.
     */
    void setEndTime(long end);

    /**
     * <p>getConsolidationFunction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getConsolidationFunction();

    /**
     * <p>setConsolidationFunction</p>
     *
     * @param cf a {@link java.lang.String} object.
     */
    void setConsolidationFunction(String cf);

    /**
     * <p>getCount</p>
     *
     * @return a int.
     */
    int getCount();

    /**
     * <p>setCount</p>
     *
     * @param count a int.
     */
    void setCount(int count);

    /**
     * <p>getJobStartedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getJobStartedDate();
    
    /**
     * <p>getJobCompletedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getJobCompletedDate();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getDescription();

    /**
     * <p>getRetainInterval</p>
     *
     * @return a long.
     */
    long getRetainInterval();

    /**
     * <p>getReportDefinition</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    ReportDefinition getReportDefinition();
    
    /**
     * <p>setReportDefinition</p>
     *
     * @param definition a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    void setReportDefinition(ReportDefinition definition);

    /**
     * <p>setResourceAttributeKey</p>
     *
     * @param resourceAttributeKey a {@link java.lang.String} object.
     */
    void setResourceAttributeKey(String resourceAttributeKey);
    
    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getResourceAttributeKey();

    /**
     * <p>setResourceAttributeValueMatch</p>
     *
     * @param resourceAttributeValueMatch a {@link java.lang.String} object.
     */
    void setResourceAttributeValueMatch(String resourceAttributeValueMatch);

    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getResourceAttributeValueMatch();
}
