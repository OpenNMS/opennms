/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.ReplaceAllOperation;
import org.opennms.core.utils.ReplaceFirstOperation;
import org.opennms.core.utils.StringReplaceOperation;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.datacollection.Parameter;

/**
 * <p>SiblingColumnStorageStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SiblingColumnStorageStrategy extends IndexStorageStrategy {
    
    private static final Logger LOG = LoggerFactory.getLogger(SiblingColumnStorageStrategy.class);
    
    private static final String PARAM_SIBLING_COLUMN_NAME = "sibling-column-name";
    private String m_siblingColumnName;

    private static final String PARAM_REPLACE_FIRST = "replace-first";
    private static final String PARAM_REPLACE_ALL = "replace-all";
    private List<StringReplaceOperation> m_replaceOps;

    /**
     * <p>Constructor for SiblingColumnStorageStrategy.</p>
     */
    public SiblingColumnStorageStrategy() {
        super();
        m_replaceOps = new ArrayList<StringReplaceOperation>();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        LOG.debug("Finding the value of sibling column {} for resource {}@{}", m_siblingColumnName, resource.getInstance(), resource.getParent());
        StringAttributeVisitor visitor = new StringAttributeVisitor(m_siblingColumnName);
        resource.visit(visitor);
        String value = (visitor.getValue() != null ? visitor.getValue() : resource.getInstance());
        
        // First remove all non-US-ASCII characters and turn all forward slashes into dashes 
        String name = value.replaceAll("[^\\x00-\\x7F]", "").replaceAll("/", "-");
        
        // Then perform all replacement operations specified in the parameters
        for (StringReplaceOperation op : m_replaceOps) {
            LOG.debug("Doing string replacement on instance name '{}' using {}", name, op);
            name = op.replace(name);
        }

        LOG.debug("Inbound instance name was '{}', outbound was '{}'", resource.getInstance(), ("".equals(name) ? resource.getInstance() : name));
        return ("".equals(name) ? resource.getInstance() : name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        if (parameterCollection == null) {
            final String msg ="Got a null parameter list, but need one containing a '" + PARAM_SIBLING_COLUMN_NAME + "' parameter.";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        for (Parameter param : parameterCollection) {
            if (PARAM_SIBLING_COLUMN_NAME.equals(param.getKey())) {
                m_siblingColumnName = param.getValue();
            } else if (PARAM_REPLACE_FIRST.equals(param.getKey())) {
                m_replaceOps.add(new ReplaceFirstOperation(param.getValue()));
            } else if (PARAM_REPLACE_ALL.equals(param.getKey())) {
                m_replaceOps.add(new ReplaceAllOperation(param.getValue()));
            } else {
                if (param.getKey().equals("sibling-column-oid")) {
                    final String msg = "The parameter 'sibling-column-oid' has been deprecated and it is no longer used. You should configure 'sibling-column-name' instead. For this parameter, you should use the name of any MibObj defined as string for the same resource type.";
                    LOG.error(msg);
                    throw new IllegalArgumentException(msg);
                } else {
                    LOG.warn("Encountered unsupported parameter key=\"{}\". Can accept: {}, {}, {}", param.getKey(), PARAM_SIBLING_COLUMN_NAME, PARAM_REPLACE_FIRST, PARAM_REPLACE_ALL);
                }
            }
        }
        
        if (m_siblingColumnName == null) {
            final String msg = "The provided parameter list must contain a '" + PARAM_SIBLING_COLUMN_NAME + "' parameter.";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
