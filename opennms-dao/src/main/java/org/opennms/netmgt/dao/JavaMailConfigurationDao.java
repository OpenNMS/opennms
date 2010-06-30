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
 * Created: January 23, 2009
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
package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.config.common.End2endMailConfig;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.config.common.SendmailConfig;
import org.springframework.dao.DataAccessResourceFailureException;


/**
 * <p>JavaMailConfigurationDao interface.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface JavaMailConfigurationDao {

    /**
     * <p>getDefaultSendmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.common.SendmailConfig} object.
     */
    SendmailConfig getDefaultSendmailConfig();
    
    /**
     * <p>getSendMailConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.common.SendmailConfig} object.
     */
    SendmailConfig getSendMailConfig(String name);
    
    /**
     * <p>getSendmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<SendmailConfig> getSendmailConfigs();
    
    /**
     * <p>getDefaultReadmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.common.ReadmailConfig} object.
     */
    ReadmailConfig getDefaultReadmailConfig();
    
    /**
     * <p>getReadMailConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.common.ReadmailConfig} object.
     */
    ReadmailConfig getReadMailConfig(String name);
    
    /**
     * <p>getReadmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ReadmailConfig> getReadmailConfigs();
    
    /**
     * <p>getEnd2EndConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.common.End2endMailConfig} object.
     */
    End2endMailConfig getEnd2EndConfig(String name);
    
    /**
     * <p>getEnd2EndConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<End2endMailConfig> getEnd2EndConfigs();
    
    /**
     * <p>verifyMarshaledConfiguration</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    void verifyMarshaledConfiguration() throws IllegalStateException;
    
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;
    
}
