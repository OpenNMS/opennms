/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;

/**
 * This is a bean container that can be used as a {@link SyslogConfig}
 * service.
 */
public final class SyslogConfigBean implements SyslogdConfig {

	private Integer m_syslogPort;
	private String m_listenAddress;
	private String m_forwardingRegexp;
	private Integer m_matchingGroupHost;
	private Integer m_matchingGroupMessage;
	private String m_parser;
	private String m_discardUei;
	private boolean m_newSuspectOnMessage;
	private int m_numThreads;
	private int m_queueSize;
	private int m_batchSize;
	private int m_batchIntervalMs;

	@Override
	public int getSyslogPort() {
		return m_syslogPort;
	}

	public void setSyslogPort(int syslogPort) {
		m_syslogPort = syslogPort;
	}

	@Override
	public String getListenAddress() {
		return m_listenAddress;
	}

	public void setListenAddress(String listenAddress) {
		m_listenAddress = listenAddress;
	}

	@Override
	public boolean getNewSuspectOnMessage() {
		return m_newSuspectOnMessage;
	}

	public void setNewSuspectOnMessage(boolean newSuspectOnMessage) {
		m_newSuspectOnMessage = newSuspectOnMessage;
	}

	@Override
	public String getForwardingRegexp() {
		return m_forwardingRegexp;
	}

	public void setForwardingRegexp(String forwardingRegexp) {
		m_forwardingRegexp = forwardingRegexp;
	}

	@Override
	public Integer getMatchingGroupHost() {
		return m_matchingGroupHost;
	}

	public void setMatchingGroupHost(int matchingGroupHost) {
		m_matchingGroupHost = matchingGroupHost;
	}

	@Override
	public Integer getMatchingGroupMessage() {
		return m_matchingGroupMessage;
	}

	public void setMatchingGroupMessage(int matchingGroupMessage) {
		m_matchingGroupMessage = matchingGroupMessage;
	}

	@Override
	public String getParser() {
		return m_parser;
	}

	public void setParser(String parser) {
		m_parser = parser;
	}

	@Override
	public List<UeiMatch> getUeiList() {
		return Collections.emptyList();
	}

	@Override
	public List<HideMatch> getHideMessages() {
		return Collections.emptyList();
	}

	@Override
	public String getDiscardUei() {
		return m_discardUei;
	}

	public void setDiscardUei(String discardUei) {
		m_discardUei = discardUei;
	}

    @Override
    public int getNumThreads() {
        if (m_numThreads <= 0) {
            return Runtime.getRuntime().availableProcessors() * 2;
        }
        return m_numThreads;
    }

    public void setNumThreads(int numThreads) {
        m_numThreads = numThreads;
    }

    @Override
    public int getQueueSize() {
        return m_queueSize;
    }

    public void setQueueSize(int queueSize) {
        m_queueSize = queueSize;
    }

    @Override
    public int getBatchSize() {
        return m_batchSize;
    }

    public void setBatchSize(int batchSize) {
        m_batchSize = batchSize;
    }

    @Override
    public int getBatchIntervalMs() {
        return m_batchIntervalMs;
    }

    public void setBatchIntervalMs(int batchIntervalMs) {
        m_batchIntervalMs = batchIntervalMs;
    }
}
