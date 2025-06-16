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
package org.opennms.netmgt.dao.api;


import java.util.List;

import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.netmgt.config.reporting.StringParm;

/**
 * <p>OnmsReportConfigDao interface.</p>
 */
public interface OnmsReportConfigDao {
    
    /**
     * <p>getParameters</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.reporting.Parameters} object.
     */
    Parameters getParameters(String id);
    
    /**
     * <p>getDateParms</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.reporting.DateParm} objects.
     */
    List<DateParm> getDateParms(String id);
    
    /**
     * <p>getStringParms</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.reporting.StringParm} objects.
     */
    List<StringParm> getStringParms(String id);
    
    /**
     * <p>getIntParms</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.reporting.IntParm} objects.
     */
    List<IntParm> getIntParms(String id);
    
    /**
     * <p>getPdfStylesheetLocation</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getPdfStylesheetLocation(String id);
    
    /**
     * <p>getSvgStylesheetLocation</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getSvgStylesheetLocation(String id);
    
    /**
     * <p>getHtmlStylesheetLocation</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getHtmlStylesheetLocation(String id);
    
    /**
     * <p>getType</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getType(String id);
    
    /**
     * <p>getLogo</p>
     *
     * @param logo a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getLogo(String logo);

}
