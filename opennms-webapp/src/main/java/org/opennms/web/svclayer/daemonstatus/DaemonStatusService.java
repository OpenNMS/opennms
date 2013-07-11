/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.daemonstatus;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.model.ServiceInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>DaemonStatusService interface.</p>
 *
 * @author <a href="mailto:skareti@users.sourceforge.net">skareti</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = false)
public interface DaemonStatusService {

	/**
	 * <p>getCurrentDaemonStatus</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	@Transactional(readOnly = true)
	Map<String, ServiceInfo> getCurrentDaemonStatus();
	/**
	 * <p>getCurrentDaemonStatusColl</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<ServiceInfo> getCurrentDaemonStatusColl();

	/**
	 * <p>startDaemon</p>
	 *
	 * @param service a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, ServiceInfo> startDaemon(String service);

	/**
	 * <p>stopDaemon</p>
	 *
	 * @param service a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, ServiceInfo> stopDaemon(String service);

	/**
	 * <p>restartDaemon</p>
	 *
	 * @param service a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, ServiceInfo> restartDaemon(String service);

	/**
	 * <p>performOperationOnDaemons</p>
	 *
	 * @param operation a {@link java.lang.String} object.
	 * @param deamons an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.Map} object.
	 */
	Map<String, ServiceInfo> performOperationOnDaemons(String operation,
			String[] deamons);
}
