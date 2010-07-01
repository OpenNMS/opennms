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
 * Created: April 29, 2009
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
package org.opennms.netmgt.provision.persist;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

/**
 * <p>NodeProvisionService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface NodeProvisionService {

    /**
     * <p>getModelAndView</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     */
    public ModelAndView getModelAndView(HttpServletRequest request) ;
    
    /**
     * <p>provisionNode</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param categories an array of {@link java.lang.String} objects.
     * @param snmpCommunity a {@link java.lang.String} object.
     * @param snmpVersion a {@link java.lang.String} object.
     * @param deviceUsername a {@link java.lang.String} object.
     * @param devicePassword a {@link java.lang.String} object.
     * @param enablePassword a {@link java.lang.String} object.
     * @param accessMethd a {@link java.lang.String} object.
     * @param autoEnable a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    public boolean provisionNode(String user, String foreignSource, String foreignId, String nodeLabel, String ipAddress,
            String[] categories,
            String snmpCommunity, String snmpVersion,
            String deviceUsername, String devicePassword, String enablePassword,
            String accessMethd, String autoEnable) throws Exception;
}
