package org.opennms.features.reporting.sdo;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDOMapper {
	
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
