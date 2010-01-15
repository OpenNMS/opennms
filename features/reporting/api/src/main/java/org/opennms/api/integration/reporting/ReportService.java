/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: December 8th, 2009 jonathan@opennms.org
 * 
 * Modified: January 13th 2010 jonathan@opennms.org
 * 
 * Moved into report API package
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.api.integration.reporting;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * This interface provides an API for implementing additional database reports 
 * inside the opennms webapp
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 *
 */
public interface ReportService {

    /**
     * This method validates that the map of report parameters matches the report
     * parameters accepted by the report. Used by the web interface.
     * 
     * @param reportParms hashmap of parameters to be provided at runtime
     * @param reportID reportID as defined in database-reports.xml
     * @return true if the reportParms supplied match those in the report definition.
     */
    public abstract boolean validate(HashMap<String, Object> reportParms,
            String reportID);
    
    /**
     * This method runs the report
     * 
     * @param reportParms hashmap of parameters to be provided at runtime
     * @param options delivery options for the report
     * @param reportID reportID as defined in database-reports.xml
     */
    public abstract void run(HashMap<String, Object> reportParms, DeliveryOptions options,
            String reportID);
    
    /**
     * This method returns the delivery options for the report. Providing a userID will
     * allow the report service to pre-populate the destination address 
     * 
     * @param userId
     * @param reportId
     * @return a delivery options object containing information that describes how the report might
     *         be delivered.
     */
    public abstract DeliveryOptions getDeliveryOptions(String userId, String reportId);
    
    /**
     * This method provides a list of formats supported by the report
     * 
     * @param reportId reportID as defined in database-reports.xml
     * @return a list of supported formats
     */
    public abstract List<String> getAvailableFormats(String reportId);
    
    /**
     * This method renders the report into a given output stream.
     * 
     * @param id reportID as defined in database-reports.xml
     * @param location location of the report on disk
     * @param format format to render the report
     * @param outputStream stream to render the resulting report
     */
    public abstract void render(String id, String location, String format, OutputStream outputStream);

}
