/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;


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
    DateParm[] getDateParms(String id);
    
    /**
     * <p>getStringParms</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.reporting.StringParm} objects.
     */
    StringParm[] getStringParms(String id);
    
    /**
     * <p>getIntParms</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.reporting.IntParm} objects.
     */
    IntParm[] getIntParms(String id);
    
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
