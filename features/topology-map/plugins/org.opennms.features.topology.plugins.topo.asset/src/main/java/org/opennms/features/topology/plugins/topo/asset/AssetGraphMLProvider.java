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
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;

public class AssetGraphMLProvider implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphMLProvider.class);

	public static final String CREATE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/create";
	public static final String REMOVE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/remove";
	public static final String CREATE_ASSET_NODE_INFO = "uei.opennms.plugins/assettopology/nodeinfo";

	private static final List<String> ueiList = Lists.newArrayList(
			CREATE_ASSET_TOPOLOGY, REMOVE_ASSET_TOPOLOGY, CREATE_ASSET_NODE_INFO);

	// folder created in OpenNMS to store asset topology info for debugging
	public static final String TEMP_FOLDER = "data/tmp";

	// file  generated for debugging
	public static final String ASSET_LIST_XML_FILE = "AssetListFile.xml";

	private final EventIpcManager eventIpcManager;

	private final GraphmlRepository graphmlRepository;

	private final TransactionOperations transactionOperations;

	private final DataProvider dataProvider;

	private final GeneratorConfig defaultConfig;


	public AssetGraphMLProvider(GraphmlRepository repository,
									EventIpcManager eventIpcManager, DataProvider dataProvider,
									TransactionOperations transactionOperations,
									GeneratorConfig defaultConfig) {
		this.graphmlRepository = Objects.requireNonNull(repository);
		this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
		this.dataProvider = Objects.requireNonNull(dataProvider);
		this.transactionOperations=transactionOperations;
		this.defaultConfig = Objects.requireNonNull(defaultConfig);
	}

	/**
	 * Generates and installs a new AssetTopology defined by the config
	 * @param config if null the default config is used
	 */
	public synchronized void createAssetTopology(GeneratorConfig config){
		try {
			GeneratorConfig localConfig= (config==null) ? defaultConfig : config;
			LOG.info("creating new asset topology providerid="+localConfig.getProviderId()+" label="+localConfig.getLabel()
					+ "from "+ ((config==null) ? "default ": "supplied ")+localConfig.toString() );

			if (graphmlRepository.exists("asset")) {
				throw new IllegalStateException("Provider providerid="+config.getProviderId()+" label="+localConfig.getLabel()
						+ "already exists");
			}
			GraphML graphML = new AssetGraphGenerator(dataProvider).generateGraphs(localConfig);
			GraphmlType graphmlType = GraphMLWriter.convert(graphML);
			graphmlRepository.save(localConfig.getProviderId(), localConfig.getLabel(), graphmlType);
		} catch (Exception ex){
			LOG.error("problem creating asset topology ", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Removes the AssetTopology defined by the config
	 * @param config if null the default config is used
	 */
	public synchronized void removeAssetTopology(GeneratorConfig config){
		try {
			GeneratorConfig localConfig= (config==null) ? defaultConfig : config;
			LOG.info("removing asset topology providerid="+localConfig.getProviderId()
					+ "from "+ ((config==null) ? "default ": "supplied ")+localConfig.toString() );
			if (!graphmlRepository.exists(localConfig.getProviderId())) {
				throw new IllegalStateException(
						"Provider providerid="+config.getProviderId()+" label="+localConfig.getLabel()
								+ " cannot be removed, because it does not exist");
			} else 	graphmlRepository.delete(localConfig.getProviderId());
		} catch (Exception ex){
			LOG.error("problem removing asset topology ", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Generates an XML node info file using the config. The file is intended to be used
	 * in debugging a config.
	 * @param config if null the default config is used
	 */
	public synchronized void createNodeInfoFile(GeneratorConfig config){
		// TODO MVR ...
//		try {
//			GeneratorConfig localConfig= (config==null) ? defaultConfig : config;
//
//			LOG.info("creating nodeinfo file=" + ASSET_LIST_XML_FILE
//					+ " in folder=" + TEMP_FOLDER + " from "+ ((config==null) ? "default ": "supplied ")+localConfig.toString() );
//
//			Map<String, Map<String, String>> nodeInfo = new AssetGraphGenerator(nodeDao,transactionOperations).generateNodeInfo(localConfig);
//
//			String nodeInfoxml = NodeInfoRepositoryXML.nodeInfoToXML(nodeInfo);
//			Utils.writeFileToDisk(nodeInfoxml, TEMP_FOLDER, ASSET_LIST_XML_FILE);
//		} catch (Exception ex) {
//			LOG.error("problem creating " + ASSET_LIST_XML_FILE, ex);
//			throw new RuntimeException("problem creating " + ASSET_LIST_XML_FILE, ex);
//		}
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
			//TODO read config from event params
			GeneratorConfig localconfig = defaultConfig;

			if (CREATE_ASSET_TOPOLOGY.equals(e.getUei())) {
				this.createAssetTopology(localconfig);

			} else if (REMOVE_ASSET_TOPOLOGY.equals(e.getUei())) {
				this.removeAssetTopology(localconfig);

			} else if (CREATE_ASSET_NODE_INFO.equals(e.getUei())) {
				this.createNodeInfoFile(localconfig);
			}
		} catch (Exception ex) {
			LOG.error("asset topology provider problem processing event " +e.getUei(), ex);
		}
	}

}