/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.inventory;

import java.util.Date;

public class RancidNodeWrapper {
    
    private String deviceName;
    private String group;
    private String deviceType;
    private String comment;
    private String headRevision;
    private int totalRevisions;
    private Date creationDate;
    private String rootConfigurationUrl;
    
    public RancidNodeWrapper(String _deviceName, String _group, String _deviceType, String _comment, String _headRevision,
                      int _totalRevision, Date _expirationDate, String _rootConfigurationUrl) {
         deviceName=_deviceName;
         group=_group;
         deviceType=_deviceType;
         comment=_comment;
         headRevision=_headRevision;
         totalRevisions=_totalRevision;
         creationDate=_expirationDate;
         rootConfigurationUrl=_rootConfigurationUrl;
    }
            
    public String getDeviceName(){
        return deviceName;
    }
    public String getGroup(){
        return group;
    }
    public String getDeviceType(){
        return deviceType;
    }
    public String getComment(){
        return comment;
    }
    public String getHeadRevision(){
        return headRevision;
    }
    public int getTotalRevisions(){
        return totalRevisions;
    }
    public Date getCreationDate(){
        return creationDate;
    }
    public String getRootConfigurationUrl(){
        return rootConfigurationUrl;
    }

}
