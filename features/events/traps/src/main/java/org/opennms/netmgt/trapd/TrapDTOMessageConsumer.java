/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import javax.annotation.PostConstruct;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.trapd.mapper.TrapDto2TrapInformationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TrapDTOMessageConsumer implements MessageConsumer<TrapDTO> {
	private static final Logger LOG = LoggerFactory.getLogger(TrapDTOMessageConsumer.class);

	@Autowired
	private MessageConsumerManager messageConsumerManager;

	@Autowired
	private TrapQueueProcessorFactory factory;

	@Autowired
	private TrapdConfig config;


	@Override
	public SinkModule<TrapDTO> getModule() {
		return new TrapSinkModule(config);
	}

	@PostConstruct
	public void init() throws Exception {
		messageConsumerManager.registerConsumer(this);
	}

	@Override
	public void handleMessage(TrapDTO message) {
		final TrapInformation trapInformation = TrapDto2TrapInformationMapper.dto2object(message);
		factory.getInstance().process(trapInformation);
	}
}
