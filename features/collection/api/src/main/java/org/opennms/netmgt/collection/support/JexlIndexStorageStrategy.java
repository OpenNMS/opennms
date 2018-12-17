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

package org.opennms.netmgt.collection.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.ReadonlyContext;
import org.apache.commons.jexl2.UnifiedJEXL;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roskens
 */
public class JexlIndexStorageStrategy extends IndexStorageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(JexlIndexStorageStrategy.class);
    private static final int DEFAULT_JEXLENGINE_CACHESIZE = 512;
    private static final String QUOTE = "\"";

    private static final String PARAM_INDEX_FORMAT = "index-format";
    private static final String PARAM_CLEAN_OUTPUT = "clean-output";

    private static final JexlEngine JEXL_ENGINE;
    private static final UnifiedJEXL EL;

    private final Map<String, String> m_parameters;

    static {
        final int cacheSize = Integer.getInteger("org.opennms.netmgt.dao.support.JEXLIndexStorageStrategy.cacheSize", DEFAULT_JEXLENGINE_CACHESIZE);
        JEXL_ENGINE = new JexlEngine();
        JEXL_ENGINE.setCache(cacheSize);
        JEXL_ENGINE.setLenient(false);
        JEXL_ENGINE.setStrict(true);

        EL = new UnifiedJEXL(JEXL_ENGINE);
    }

    public JexlIndexStorageStrategy() {
        super();
        m_parameters = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        String resourceName = null;
        try {
            UnifiedJEXL.Expression expr = EL.parse( m_parameters.get(PARAM_INDEX_FORMAT) );
            JexlContext context = new MapContext();
            m_parameters.entrySet().forEach((entry) -> {
                context.set(entry.getKey(), entry.getValue());
            });
            updateContext(context, resource);
            resourceName = (String) expr.evaluate(new ReadonlyContext(context));
        } catch (JexlException e) {
            LOG.error("getResourceNameFromIndex(): error evaluating index-format [{}] as a Jexl Expression", m_parameters.get(PARAM_INDEX_FORMAT), e);
        } finally {
            if (resourceName == null) {
                resourceName = resource.getInstance();
            }
        }
        if ("true".equals(m_parameters.get(PARAM_CLEAN_OUTPUT)) && resourceName != null) {
            resourceName = resourceName.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_").replaceAll("[|/]", "_").replaceAll("=", "").replaceAll("[_]+$", "").replaceAll("___", "_");
        }

        LOG.debug("getResourceNameFromIndex(): {}", resourceName);
        return resourceName;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        if (parameterCollection == null) {
            final String msg ="Got a null parameter list, but need one containing a '" + PARAM_INDEX_FORMAT + "' parameter.";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
        parameterCollection.forEach((param) -> {
            if (null == param.getKey()) {
                LOG.warn("Encountered unsupported parameter key=\"{}\". Can accept: {}, {}", param.getKey(), PARAM_INDEX_FORMAT, PARAM_CLEAN_OUTPUT);
            } else {
                m_parameters.put(param.getKey(), param.getValue());
            }
        });
        if (!m_parameters.containsKey(PARAM_INDEX_FORMAT)) {
            throw new IllegalArgumentException("Missing index-format expression");
        }
    }

    public void updateContext(JexlContext context, CollectionResource resource) {
    }
}
