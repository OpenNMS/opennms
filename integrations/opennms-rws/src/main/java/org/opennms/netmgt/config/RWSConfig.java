/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.rws.BaseUrl;
import org.opennms.netmgt.config.rws.StandbyUrl;
import org.opennms.rancid.ConnectionProperties;

/**
 * <p>RWSConfig interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface RWSConfig {
    /**
     * <p>getBase</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    public ConnectionProperties getBase();
    
    /**
     * <p>getStandBy</p>
     *
     * @return an array of {@link org.opennms.rancid.ConnectionProperties} objects.
     */
    public ConnectionProperties[] getStandBy();
    
    /**
     * <p>getNextStandBy</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    public ConnectionProperties getNextStandBy();
    
    /**
     * <p>getBaseUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.BaseUrl} object.
     */
    public BaseUrl getBaseUrl();
    
    /**
     * <p>getStandbyUrls</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.rws.StandbyUrl} objects.
     */
    public List<StandbyUrl> getStandbyUrls();
    
    /**
     * <p>getNextStandbyUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.StandbyUrl} object.
     */
    public StandbyUrl getNextStandbyUrl();
    
    /**
     * <p>hasStandbyUrl</p>
     *
     * @return a boolean.
     */
    public boolean hasStandbyUrl();

    public Lock getReadLock();

    public Lock getWriteLock();
}
