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
package org.opennms.netmgt.collection.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.ReadonlyContext;
import org.apache.commons.jexl2.UnifiedJEXL;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.jexl.OnmsJexlEngine;
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

    protected final OnmsJexlEngine jexlEngine;
    private final UnifiedJEXL unifiedJexl;

    private final Map<String, String> m_parameters;

    public JexlIndexStorageStrategy() {
        super();
        final int cacheSize = SystemProperties.getInteger("org.opennms.netmgt.dao.support.JEXLIndexStorageStrategy.cacheSize", DEFAULT_JEXLENGINE_CACHESIZE);
        jexlEngine = new OnmsJexlEngine();
        jexlEngine.setCache(cacheSize);
        jexlEngine.setLenient(false);
        jexlEngine.setStrict(true);
        unifiedJexl = new UnifiedJEXL(jexlEngine);
        m_parameters = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        String resourceName = null;
        try {
            UnifiedJEXL.Expression expr = unifiedJexl.parse( m_parameters.get(PARAM_INDEX_FORMAT) );
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

    public void updateContext(JexlContext context, CollectionResource resource) throws IllegalArgumentException {
    }
}
