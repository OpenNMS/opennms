package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class EventPolicyRuleDefaultImpl extends AbstractEventPolicyRule implements
		EventPolicyRule {

	@Override
	/**
	 * This method do nothing 
	 * 
	 */
	protected Event expand(Event event) {
		return event;
	}

}
