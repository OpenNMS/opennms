/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.config.wsman.SystemDefinition;
import org.opennms.netmgt.config.wsman.WsmanAgentConfig;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WSManDataCollectionConfigDaoJaxb extends AbstractMergingJaxbConfigDao<WsmanDatacollectionConfig, WsmanDatacollectionConfig> implements WSManDataCollectionConfigDao {

    private static final Logger LOG = LoggerFactory.getLogger(WSManDataCollectionConfigDaoJaxb.class);

    public WSManDataCollectionConfigDaoJaxb() {
        super(WsmanDatacollectionConfig.class, "WS-Man Data Collection Configuration",
                Paths.get("etc", "wsman-datacollection-config.xml"),
                Paths.get("etc", "wsman-datacollection.d"));
    }

    @Override
    public WsmanDatacollectionConfig translateConfig(WsmanDatacollectionConfig config) {
        return config;
    }

    @Override
    public WsmanDatacollectionConfig mergeConfigs(WsmanDatacollectionConfig source, WsmanDatacollectionConfig target) {
        if (target == null) {
            target = new WsmanDatacollectionConfig();
        }
        return target.merge(source);
    }

    @Override
    public WsmanDatacollectionConfig getConfig() {
        return getObject();
    }

    @Override
    public Collection getCollectionByName(String name) {
        return getConfig().getCollection().stream()
            .filter(c -> name.equals(c.getName())).findFirst().orElse(null);
    }

    @Override
    public List<Group> getGroupsForAgent(Collection collection, CollectionAgent agent, WsmanAgentConfig agentConfig, OnmsNode node) {
        // Fetch the system definitions for the given collection
        List<SystemDefinition> sysDefs = getSystemDefinitionsForCollection(collection);

        // Map all of the available groups by name for easy lookup
        final Map<String, Group> groupsByName = Maps.uniqueIndex(getConfig().getGroup(), Group::getName);

        // Gather the groups from all of the supported system definitions
        final List<Group> groups = Lists.newArrayList();
        for (SystemDefinition sysDef : sysDefs) {
            if (isAgentSupportedBySystemDefinition(sysDef, agent, agentConfig, node)) {
                for (String groupName : sysDef.getIncludeGroup()) {
                    Group group = groupsByName.get(groupName);
                    if (group == null) {
                        LOG.warn("System definition with name {} includes group with name {}, "
                                + "but no such group was found.", sysDef.getName(), groupName);
                        continue;
                    }
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    public List<SystemDefinition> getSystemDefinitionsForCollection(Collection collection) {
        if (collection.getIncludeAllSystemDefinitions() != null) {
            // Return all available system definitions if requested
            return getConfig().getSystemDefinition();
        } else {
            // Map all of the available system definitions by name for easy lookup
            final Map<String, SystemDefinition> sysDefsByName = Maps.uniqueIndex(getConfig().getSystemDefinition(), SystemDefinition::getName);
            // Gather the requested system definitions
            final List<SystemDefinition> sysDefs = Lists.newArrayList();
            for (String sysDefName : collection.getIncludeSystemDefinition()) {
                SystemDefinition sysDef = sysDefsByName.get(sysDefName);
                if (sysDef == null) {
                    LOG.warn("Collection with name {} includes system definition with name {}, but no such definition was found.",
                            collection.getName(), sysDefName);
                    continue;
                }
                sysDefs.add(sysDef);
            }
            return sysDefs;
        }
    }

    public static boolean isAgentSupportedBySystemDefinition(SystemDefinition sysDef, CollectionAgent agent, WsmanAgentConfig agentConfig, OnmsNode node) {
        // Determine the effective values for the productVendor and productVersion:
        // The detected values are stored in the assets table, we allow these
        // to be overridden by the agent specific configuration
        String productVendor;
        if (agentConfig.getProductVendor() != null) {
            productVendor = agentConfig.getProductVendor();
        } else {
            productVendor = node.getAssetRecord().getVendor();
            // Guarantee that the values are non-null
            productVendor = Strings.nullToEmpty(productVendor);
        }

        String productVersion;
        if (agentConfig.getProductVersion() != null) {
            productVersion = agentConfig.getProductVersion();
        } else {
            productVersion = node.getAssetRecord().getModelNumber();
            // Guarantee that the values are non-null
            productVersion = Strings.nullToEmpty(productVersion);
        }

        // Build the evaluation context
        StandardEvaluationContext context = new StandardEvaluationContext(node);
        // Add the agent, so that the rule can determine the IP address in question, if required
        context.setVariable("agent", agent);
        context.setVariable("productVendor", productVendor);
        context.setVariable("productVersion", productVersion);

        // Evaluate the rules. Multiple rules are logically ORed.
        for (String rule : sysDef.getRule()) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(rule);
            boolean passed = false;
            try {
                passed = exp.getValue(context, Boolean.class);
            } catch (Exception e) {
                LOG.error("Failed to evaluate expression {} for agent {} with context {}. System defintion with name {} will not be used. Msg: {}", rule, agent, context, sysDef.getName(), e.getMessage());
            }
            LOG.debug("Rule '{}' on {} passed? {}", rule, agent, passed);

            if (passed) {
                return true;
            }
        }
        return false;
    }
}
