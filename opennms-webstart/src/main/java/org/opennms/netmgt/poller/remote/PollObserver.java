package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.PollStatus;

public interface PollObserver {

	void pollStarted(String pollId);

	void pollCompleted(String pollId, PollStatus pollStatus);

}
