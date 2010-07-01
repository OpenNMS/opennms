//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: 27th January, 2009
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao;


import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.netmgt.config.reporting.StringParm;

/**
 * <p>OnmsReportConfigDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
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
