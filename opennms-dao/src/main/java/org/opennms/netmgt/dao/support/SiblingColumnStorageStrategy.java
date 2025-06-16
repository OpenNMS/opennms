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
package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.ReplaceAllOperation;
import org.opennms.core.utils.ReplaceFirstOperation;
import org.opennms.core.utils.StringReplaceOperation;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        m_replaceOps = new ArrayList<>();
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
