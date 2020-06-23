/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.outage;

import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.filter.OutageCriteria;

/**
 * <p>WebOutageRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebOutageRepository {

    /**
     * <p>countMatchingOutages</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingOutages(OutageCriteria criteria);

    /**
     * <p>getOutage</p>
     *
     * @param OutageId a int.
     * @return a {@link org.opennms.web.outage.Outage} object.
     */
    public abstract Outage getOutage(int OutageId);

    /**
     * <p>getMatchingOutages</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     */
    public abstract Outage[] getMatchingOutages(OutageCriteria criteria);

    /**
     * <p>countMatchingOutageSummaries</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingOutageSummaries(OutageCriteria criteria);

    /**
     * <p>getMatchingOutageSummaries</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return an array of {@link org.opennms.netmgt.model.outage.OutageSummary} objects.
     */
    public abstract OutageSummary[] getMatchingOutageSummaries(OutageCriteria criteria);

    /**
     * Count the current number of nodes with outages.
     */
    public abstract int countCurrentOutages();

    /**
     * Get the current list of outages by node.
     * @param rows the number of outages to return.
     * @return
     */
    public abstract OutageSummary[] getCurrentOutages(int rows);
}
