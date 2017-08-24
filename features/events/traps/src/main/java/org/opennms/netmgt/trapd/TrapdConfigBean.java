/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.slf4j.LoggerFactory;

/**
 * This is a bean container that can be used as a {@link TrapdConfig}
 * service.
 * 
 * @author dp044946
 */
public class TrapdConfigBean implements TrapdConfig, Serializable {

	private static final long serialVersionUID = 2L;

	private String snmpTrapAddress;
	private int snmpTrapPort;
	private boolean newSuspectOnTrap;
	private List<SnmpV3User> snmpV3Users= new ArrayList<>();
	private boolean includeRawMessage;
	private int batchIntervalInMs;
	private int batchSize;
	private int queueSize;
	private int numThreads;
	private boolean useAddressFromVarbind;

	public TrapdConfigBean() {

	}

	public TrapdConfigBean(TrapdConfig configToClone) {
		update(configToClone);
	}

	public TrapdConfigBean(TrapdConfiguration trapdConfiguration) {
		setSnmpTrapAddress(trapdConfiguration.getSnmpTrapAddress());
		setSnmpTrapPort(trapdConfiguration.getSnmpTrapPort());
		setNewSuspectOnTrap(trapdConfiguration.isNewSuspectOnTrap());
		setIncludeRawMessage(trapdConfiguration.isIncludeRawMessage());
		setBatchIntervalMs(trapdConfiguration.getBatchInterval());
		setBatchSize(trapdConfiguration.getBatchSize());
		setQueueSize(trapdConfiguration.getQueueSize());
		setNumThreads(trapdConfiguration.getThreads());
		if (trapdConfiguration.getSnmpv3UserCollection() != null) {
			setSnmpV3Users(trapdConfiguration.getSnmpv3UserCollection().stream()
						.map(TrapdConfigBean::toSnmpV3User)
						.collect(Collectors.toList()));
		}
	}

	public void setSnmpTrapAddress(String snmpTrapAddress) {
		this.snmpTrapAddress = snmpTrapAddress;
	}

	public void setSnmpTrapPort(int snmpTrapPort) {
		this.snmpTrapPort = snmpTrapPort;
	}

	public void setNewSuspectOnTrap(boolean newSuspectOnTrap) {
		this.newSuspectOnTrap = newSuspectOnTrap;
	}

	@Override
	public String getSnmpTrapAddress() {
		return snmpTrapAddress;
	}

	@Override
	public int getSnmpTrapPort() {
		return snmpTrapPort;
	}

	public void setSnmpV3Users(List<SnmpV3User> snmpV3Users) {
		Objects.requireNonNull(snmpV3Users);
		final Map<String, SnmpV3User> collect = snmpV3Users.stream().collect(Collectors.toMap(SnmpV3User::getSecurityName, Function.identity(), (a, b) -> {
			LoggerFactory.getLogger(getClass()).warn("Multiple SNMPv3 user entries found for security name \"{}\", using entry {}", a.getSecurityName(), a);
			return a;
		}));
		this.snmpV3Users = new ArrayList<>(collect.values());
	}

	@Override
	public boolean getNewSuspectOnTrap() {
		return newSuspectOnTrap;
	}

	@Override
	public List<SnmpV3User> getSnmpV3Users() {
		return Collections.unmodifiableList(snmpV3Users);
	}

	@Override
	public boolean isIncludeRawMessage() {
		return includeRawMessage;
	}

	public void setIncludeRawMessage(boolean includeRawMessage) {
		this.includeRawMessage = includeRawMessage;
	}

	@Override
	public int getNumThreads() {
		if (numThreads <= 0) {
			return Runtime.getRuntime().availableProcessors() * 2;
		}
		return numThreads;
	}

	@Override
	public int getQueueSize() {
		return queueSize;
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public int getBatchIntervalMs() {
		return batchIntervalInMs;
	}

	@Override
	public void update(TrapdConfig config) {
		setSnmpTrapAddress(config.getSnmpTrapAddress());
		setSnmpTrapPort(config.getSnmpTrapPort());
		setNewSuspectOnTrap(config.getNewSuspectOnTrap());
		setIncludeRawMessage(config.isIncludeRawMessage());
		setBatchIntervalMs(config.getBatchIntervalMs());
		setBatchSize(config.getBatchSize());
		setQueueSize(config.getQueueSize());
		setNumThreads(config.getNumThreads());
		setSnmpV3Users(config.getSnmpV3Users());
	}

	public void setBatchIntervalMs(int batchIntervalInMs) {
		this.batchIntervalInMs = batchIntervalInMs;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	private static SnmpV3User toSnmpV3User(Snmpv3User snmpv3User) {
		SnmpV3User snmpV3User = new SnmpV3User();
		snmpV3User.setAuthPassPhrase(snmpv3User.getAuthPassphrase());
		snmpV3User.setAuthProtocol(snmpv3User.getAuthProtocol());
		snmpV3User.setEngineId(snmpv3User.getEngineId());
		snmpV3User.setPrivPassPhrase(snmpv3User.getPrivacyPassphrase());
		snmpV3User.setPrivProtocol(snmpv3User.getPrivacyProtocol());
		snmpV3User.setSecurityName(snmpv3User.getSecurityName());
		return snmpV3User;
	}

	@Override
	public boolean shouldUseAddressFromVarbind() {
		return this.useAddressFromVarbind;
	}

	public void setUseAddressFromVarbind(boolean useAddressFromVarbind) {
		this.useAddressFromVarbind = useAddressFromVarbind;
	}
}
