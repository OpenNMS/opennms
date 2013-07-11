/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/ Advance filter
 * functionalities are present in this class.
 */
package org.opennms.netmgt.notification.filter;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.notification.NBIAlarm;
import org.opennms.netmgt.notification.parser.Errorhandling;
import org.opennms.netmgt.notification.parser.Script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvanceFilter {
	private static final Logger LOG = LoggerFactory.getLogger(AdvanceFilter.class);

	public boolean callAdvanceFilter(NBIAlarm nbiAlarm,
			String notificationName, String alarmXML, Script script)
			throws NorthbounderException {
		StatefulKnowledgeSession session = null;
		try {
			KnowledgeBase knowledgeBase = DroolsFileLoader
					.getKnowledgeBaseForDrl(notificationName + ".drl");
			if (knowledgeBase == null) {
				LOG.debug("Drool file with name " + notificationName + ".drl is not present or the knowledgebase is not loaded.");
				return false;
			} else {
				nbiAlarm.setAlarmXML(alarmXML);
				nbiAlarm.setScriptName(script.getScriptname());
				Errorhandling errorhandling = script.getErrorhandling();
				nbiAlarm.setErrorHandlingEnabled(errorhandling.isEnable());
				nbiAlarm.setNumberOfRetries(errorhandling.getNumberOfRetries());
				nbiAlarm.setRetryInterval(errorhandling
						.getRetryIntervalInseconds());
				nbiAlarm.setTimeoutInSeconds(script.getTimeoutInSeconds());
			}
			session = knowledgeBase.newStatefulKnowledgeSession();
			FactHandle factHandle = session.insert(nbiAlarm);
			LOG.debug("Fact Handle for " + notificationName
					+ ".drl is " + factHandle.toString());
			LOG.debug("Rules in " + notificationName
					+ ".drl is being triggerred.");
			int rulesFired = session.fireAllRules();
			LOG.debug("Number of rules triggered in "
					+ notificationName + ".drl is " + rulesFired);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NorthbounderException(e);
		} finally {
			if (session != null)
				session.dispose();
		}
		return true;
	}
}
