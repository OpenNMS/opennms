/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.config.ackd.ReaderSchedule;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DAO interface for Ackd configuration
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface AckdConfigurationDao {
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.ackd.AckdConfiguration} object.
     */
    AckdConfiguration getConfig();
    
    /**
     * Utility method for determining if a reply email should acknowledge an acknowledgable
     *
     * @param messageText a {@link java.util.List} object.
     * @return Boolean
     */
    Boolean acknowledgmentMatch(List<String> messageText);
    
    /**
     * Utility method for determining if a reply email should clear an acknowledgable
     *
     * @param messageText a {@link java.util.List} object.
     * @return Boolean
     */
    Boolean clearMatch(List<String> messageText);
    
    /**
     * Utility method for determining if a reply email should escalate an acknowledgable
     *
     * @param messageText a {@link java.util.List} object.
     * @return Boolean
     */
    Boolean escalationMatch(List<String> messageText);

    /**
     * Utility method for determining if a reply email should unacknoweledge an acknowledgable
     *
     * @param messageText a {@link java.util.List} object.
     * @return Boolean
     */
    Boolean unAcknowledgmentMatch(List<String> messageText);

    /**
     * Utility method to retrieve a schedule defined for a reader.  Each <code>AckdReader</code> requires that a name property
     * is defined and the configuration uses that name to retrieve configuration details for that named reader.
     *
     * @param readerName a {@link java.lang.String} object.
     * @return a ReaderSchedule
     */
    ReaderSchedule getReaderSchedule(String readerName);

    /**
     * Utility method to retrieve a readers configuration by name.  Each <code>AckdReader</code> requires that a name property
     * is defined and the configuration uses that name to retrieve configuration details for that named reader.
     *
     * @param readerName a {@link java.lang.String} object.
     * @return a Reader configuration
     */
    Reader getReader(String readerName);

    /**
     * Utility method that determines if a named reader's configuration is enabled.  Each <code>AckdReader</code> requires that
     * a name property is defined and the configuration uses that name to retrieve configuration details for that named reader.
     *
     * @param readerName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean isReaderEnabled(String readerName);
    
    /**
     * The underlying Castor based DAO abstraction in the default implementation doesn't provide access to the container so
     * this method is defined so that access to the container doesn't have to be exposed and a reload can still be controlled
     * by the user.
     *
     * Automatically reading in new values if the file changes is a different use case from expecting the services to alter
     * their state based on a configuration change.  This method will most likely be used with event processing and possibly
     * in the ReST API.
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;

    /**
     * <p>getEnabledReaderCount</p>
     *
     * @return a int.
     */
    int getEnabledReaderCount();

    /**
     * <p>getParametersForReader</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<Parameter> getParametersForReader(String name);

}
