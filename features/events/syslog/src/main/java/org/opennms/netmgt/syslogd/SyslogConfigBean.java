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
package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;

/**
 * This is a bean container that can be used as a {@link SyslogdConfig}
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
	private TimeZone timeZone;
	private boolean includeRawSyslogmessage;

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

	@Override
	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(TimeZone timeZone){
		this.timeZone = timeZone;
	}

	@Override
	public boolean shouldIncludeRawSyslogmessage() {
		return includeRawSyslogmessage;
	}

	public void setIncludeRawSyslogmessage(boolean includeRawSyslogmessage) {
		this.includeRawSyslogmessage = includeRawSyslogmessage;
	}

	public void setBatchIntervalMs(int batchIntervalMs) {
        m_batchIntervalMs = batchIntervalMs;
    }

    @Override
    public void reload() throws IOException {
      // pass
    }
}
