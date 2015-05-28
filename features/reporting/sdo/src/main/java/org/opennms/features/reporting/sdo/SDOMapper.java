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

package org.opennms.features.reporting.sdo;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SDOMapper {
	
	private static Logger logger = LoggerFactory.getLogger(SDOMapper.class);
	
	public static RemoteReportSDO getSDO(Object bean) {
		RemoteReportSDO reportResult = new RemoteReportSDO();
		try {
			BeanUtils.copyProperties(reportResult, bean);
		} catch (IllegalAccessException e) {
			logger.debug("getSDObyConnectReport IllegalAssessException while copyProperties from '{}' to '{}' with exception.", reportResult, bean);
			logger.error("getSDObyConnectReport IllegalAssessException while copyProperties '{}'", e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.debug("getSDObyConnectReport InvocationTargetException while copyProperties from '{}' to '{}' with exception.", reportResult, bean);
			logger.error("getSDObyConnectReport InvocationTargetException while copyProperties '{}'", e);
			e.printStackTrace();
		}
		return reportResult;
	}
}
