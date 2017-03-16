/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.List;
import java.util.Objects;

import org.graphdrawing.graphml.GraphmlType;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.service.GraphmlRepository;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;

public class AssetGraphMLProvider implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphMLProvider.class);

	public static final String CREATE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/create";
	public static final String REMOVE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/remove";

	private static final List<String> ueiList = Lists.newArrayList(CREATE_ASSET_TOPOLOGY, REMOVE_ASSET_TOPOLOGY);

	// folder created in OpenNMS to store asset topology info for debugging
	public static final String TEMP_FOLDER = "data/tmp";

	// file  generated for debugging
	public static final String ASSET_LIST_XML_FILE = "AssetListFile.xml";

	private final EventIpcManager eventIpcManager;

	private final GraphmlRepository graphmlRepository;

	private final TransactionOperations transactionOperations;

	private final DataProvider dataProvider;

	public AssetGraphMLProvider(GraphmlRepository repository,
									EventIpcManager eventIpcManager, DataProvider dataProvider,
									TransactionOperations transactionOperations) {
		this.graphmlRepository = Objects.requireNonNull(repository);
		this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
		this.dataProvider = Objects.requireNonNull(dataProvider);
		this.transactionOperations=transactionOperations;
	}

	/**
	 * Generates and installs a new AssetTopology defined by the config
	 * @param config if null the default config is used
	 */
	public synchronized void createAssetTopology(GeneratorConfig config){
		Objects.requireNonNull(config);
		try {
			LOG.debug("Creating Asset Topology providerId: {}, label: {}, config: {}", config.getProviderId(), config.getLabel(), config);
			if (graphmlRepository.exists("asset")) {
				throw new IllegalStateException(String.format("Provider with id '%s' (label: %s) already exists", config.getProviderId(), config.getLabel()));
			}
			final GraphML graphML = new AssetGraphGenerator(dataProvider).generateGraphs(config);
			final GraphmlType graphmlType = GraphMLWriter.convert(graphML);
			graphmlRepository.save(config.getProviderId(), config.getLabel(), graphmlType);
		} catch (Exception ex){
			LOG.error("Could not create Asset Topology", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Removes the AssetTopology defined by the config
	 * @param config if null the default config is used
	 */
	public synchronized void removeAssetTopology(String providerId){
		Objects.requireNonNull(providerId);
		try {
			LOG.debug("Removing Asset Topology providerId: {}", providerId);
			if (!graphmlRepository.exists(providerId)) {
				throw new IllegalStateException(String.format("Provider with id '%s' cannot be removed, because it does not exist", providerId));
			} else 	{
				graphmlRepository.delete(providerId);
			}
		} catch (Exception ex){
			LOG.error("problem removing asset topology ", ex);
			throw new RuntimeException(ex);
		}
	}

	public void init() {
		eventIpcManager.addEventListener(this, ueiList);
		LOG.info("asset topology provider started");
	}

	public void destroy() {
		eventIpcManager.removeEventListener(this, ueiList);
		LOG.info("asset topology provider stopped");
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void onEvent(Event e) {
		try {
			if (CREATE_ASSET_TOPOLOGY.equals(e.getUei())) {
				final GeneratorConfig config = GeneratorConfigBuilder.buildFrom(e);
				createAssetTopology(config);
			} else if (REMOVE_ASSET_TOPOLOGY.equals(e.getUei())) {
				final String providerId = EventUtils.getParm(e, EventParameterNames.PROVIDER_ID);
				this.removeAssetTopology(providerId);
			}
		} catch (Exception ex) {
			LOG.error("asset topology provider problem processing event " +e.getUei(), ex);
		}
	}

}