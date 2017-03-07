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

import com.google.common.collect.Lists;

/**
 * Provider to generate and register GraphML graphs based on node asset fields.
 */
public class AssetGraphMLProvider implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(AssetGraphMLProvider.class);

    public static final String CREATE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/create";
	public static final String REMOVE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/remove";
	public static final String CREATE_ASSET_NODE_INFO = "uei.opennms.plugins/assettopology/nodeinfo";

	// TODO optional add update capability
//	public static final String UPDATE_ASSET_TOPOLOGY = "uei.opennms.plugins/assettopology/update";
	
	public static final String TEMP_FOLDER = "data/tmp"; // folder created in OpenNMS to store asset topology info for debugging
	public static final String ASSET_LIST_XML_FILE = "AssetListFile.xml"; // file generated for debugging


	private final List<String> ueiList = Lists.newArrayList(CREATE_ASSET_TOPOLOGY, REMOVE_ASSET_TOPOLOGY);

	private final EventIpcManager eventIpcManager;

    private final GraphmlRepository graphmlRepository;

    private final NodeDao nodeDao;

    private final GeneratorConfig config;


    public AssetGraphMLProvider(GraphmlRepository repository,
                                EventIpcManager eventIpcManager,
                                NodeDao nodeDao,
                                GeneratorConfig config) {
        this.graphmlRepository = Objects.requireNonNull(repository);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.config = Objects.requireNonNull(config);
    }

    public void init() {
        eventIpcManager.addEventListener(this, ueiList);
    }

    public void destroy() {
        eventIpcManager.removeEventListener(this, ueiList);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(Event e) {
        try {
            if (CREATE_ASSET_TOPOLOGY.equals(e.getUei())) {
                if (graphmlRepository.exists("asset")) {
                    // TODO or log instead
                    throw new IllegalStateException("Provider already exists");
                }
                GraphML graphML = new AssetGraphGenerator(nodeDao).generateGraphs(config);
                GraphmlType graphmlType = GraphMLWriter.convert(graphML);
                graphmlRepository.save(config.getProviderId(), config.getLabel(), graphmlType);
            } else if (REMOVE_ASSET_TOPOLOGY.equals(e.getUei())) {
                if (!graphmlRepository.exists(config.getProviderId())) {
                    // TODO or log instead
                    throw new IllegalStateException("Provider cannot be removed, because it does not exist");
                }
                graphmlRepository.delete(config.getProviderId());
            } else if (CREATE_ASSET_NODE_INFO.equals(e.getUei())) {
            	NodeInfoRepository nir = new NodeInfoRepository();
            	nir.setNodeDao(nodeDao);
            	nir.initialiseNodeInfo(null);
        		String nodeInfoxml = NodeInfoRepositoryXML.nodeInfoToXML(nir.getNodeInfo());
        		Utils.writeFileToDisk(nodeInfoxml, ASSET_LIST_XML_FILE, TEMP_FOLDER);
            }
        } catch (Exception ex) {
            // TODO or log instead
            throw new RuntimeException(ex);
        }
    }
}
