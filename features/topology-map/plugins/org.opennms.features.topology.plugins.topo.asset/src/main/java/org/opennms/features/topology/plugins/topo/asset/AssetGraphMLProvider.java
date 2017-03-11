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
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.Utils;
import org.opennms.features.topology.plugins.topo.asset.repo.xml.NodeInfoRepositoryXML;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;

/**
 * Provider to generate and register GraphML graphs based on node asset fields.
 */
public class AssetGraphMLProvider implements EventListener {

	private static final Logger LOG = LoggerFactory
			.getLogger(AssetGraphMLProvider.class);
	
	// TODO optional add update capability
	// public static final String UPDATE_ASSET_TOPOLOGY =
	// "uei.opennms.plugins/assettopology/update";

	public static final String CREATE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/create";
	public static final String REMOVE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/remove";
	public static final String CREATE_ASSET_NODE_INFO = "uei.opennms.plugins/assettopology/nodeinfo";
	
	private final List<String> ueiList = Lists.newArrayList(
			CREATE_ASSET_TOPOLOGY, REMOVE_ASSET_TOPOLOGY, CREATE_ASSET_NODE_INFO);



	// folder created in OpenNMS to store asset topology info for debugging
	public static final String TEMP_FOLDER = "data/tmp"; 
	
	// file  generated for debugging
	public static final String ASSET_LIST_XML_FILE = "AssetListFile.xml"; 



	private final EventIpcManager eventIpcManager;

	private final GraphmlRepository graphmlRepository;

	private final NodeDao nodeDao;
	
	private final TransactionOperations transactionOperations;

	private final GeneratorConfig config;


	public AssetGraphMLProvider(GraphmlRepository repository,
			EventIpcManager eventIpcManager, NodeDao nodeDao, 
			TransactionOperations transactionOperations,
			GeneratorConfig config) {
		this.graphmlRepository = Objects.requireNonNull(repository);
		this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
		this.nodeDao = Objects.requireNonNull(nodeDao);
		this.transactionOperations=transactionOperations;
		this.config = Objects.requireNonNull(config);
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
				LOG.info("creating new asset topology providerid="+config.getProviderId()+" label="+config.getLabel());
				if (graphmlRepository.exists("asset")) {
					// TODO or log instead
					throw new IllegalStateException("Provider already exists");
				}
				GraphML graphML = new AssetGraphGenerator(nodeDao,transactionOperations)
						.generateGraphs(config);
				GraphmlType graphmlType = GraphMLWriter.convert(graphML);
				graphmlRepository.save(config.getProviderId(),
						config.getLabel(), graphmlType);
			} else if (REMOVE_ASSET_TOPOLOGY.equals(e.getUei())) {
				LOG.info("removing asset topology providerid="+config.getProviderId());
				if (!graphmlRepository.exists(config.getProviderId())) {
					// TODO or log instead
					throw new IllegalStateException(
							"Provider cannot be removed, because it does not exist");
				}
				graphmlRepository.delete(config.getProviderId());
			} else if (CREATE_ASSET_NODE_INFO.equals(e.getUei())) {
				try {
					LOG.info("creating nodeinfo file=" + ASSET_LIST_XML_FILE
							+ " in folder=" + TEMP_FOLDER);
					NodeInfoRepository nodeInfoRepository = new NodeInfoRepository();
					nodeInfoRepository.setNodeDao(nodeDao);
					nodeInfoRepository.setTransactionOperations(transactionOperations);
					nodeInfoRepository.initialiseNodeInfo(null);
					String nodeInfoxml = NodeInfoRepositoryXML
							.nodeInfoToXML(nodeInfoRepository.getNodeInfo());
					Utils.writeFileToDisk(nodeInfoxml, TEMP_FOLDER, ASSET_LIST_XML_FILE);
				} catch (Exception ex) {
					LOG.error("problem creating " + ASSET_LIST_XML_FILE, ex);
				}
			}
		} catch (Exception ex) {
			LOG.error("asset topology provider problem processing event " +e.getUei(), ex);
		}
	}
}
