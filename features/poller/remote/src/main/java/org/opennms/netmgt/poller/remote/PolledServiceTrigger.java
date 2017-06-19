/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * <p>PolledServiceTrigger class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PolledServiceTrigger extends SimpleTriggerFactoryBean {
	
	private static final long serialVersionUID = -3224274965842979439L;

	/**
	 * <p>Constructor for PolledServiceTrigger.</p>
	 *
	 * @param polledService a {@link org.opennms.netmgt.poller.remote.PolledService} object.
	 * @throws java.lang.Exception if any.
	 */
	public PolledServiceTrigger(PollJobDetail jobDetail) throws Exception {
		super();

		final PolledService polledService = (PolledService)jobDetail.getJobDataMap().get(PollJobDetail.JOB_DATA_MAP_KEY_POLLEDSERVICE);
		setName(polledService.getNodeId()+':'+polledService.getIpAddr()+':'+polledService.getSvcName());
		setRepeatInterval(polledService.getPollModel().getPollInterval());
		setJobDetail(jobDetail);

		afterPropertiesSet();
	}

}
