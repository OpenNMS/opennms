/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.api.integration.ticketing;

/**
 * OpenNMS Trouble Ticket Plugin API
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface Plugin {
    
    /**
     * DAO like get method to be implemented by HelpDesk specific
     * plugin.
     *
     * @param ticketId a {@link java.lang.String} object.
     * @return a {@link org.opennms.api.integration.ticketing.Ticket} object.
     * @throws org.opennms.api.integration.ticketing.PluginException if any.
     */
    public Ticket get(String ticketId) throws PluginException;
    
    /**
     * DAO like saveOrUpdate method to be implemented by HelpDesk specific
     * plugin.
     *
     * @param ticket a {@link org.opennms.api.integration.ticketing.Ticket} object.
     * @throws org.opennms.api.integration.ticketing.PluginException if any.
     */
    public void saveOrUpdate(Ticket ticket) throws PluginException;

}
