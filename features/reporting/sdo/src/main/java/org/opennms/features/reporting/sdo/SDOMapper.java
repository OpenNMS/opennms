/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
