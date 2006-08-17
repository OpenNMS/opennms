package org.opennms.netmgt.poller.remote;

import org.springframework.scheduling.quartz.SimpleTriggerBean;

public class PollModelTrigger extends SimpleTriggerBean {
	
	private static final long serialVersionUID = -3224274965842979439L;

	private OnmsPollModel m_pollModel;
	
	public PollModelTrigger(String name, OnmsPollModel pollModel) throws Exception {
		super();
		m_pollModel = pollModel;
		setRepeatInterval(m_pollModel.getPollInterval());
		setName(name);
		afterPropertiesSet();
	}

}
