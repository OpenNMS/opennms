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
package org.opennms.netmgt.collection.persistence.evaluate;

import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * The Class AbstractEvaluatePersister.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractEvaluatePersister extends AbstractPersister {

    /** The evaluation statistics. */
    protected EvaluateStats stats;

    /**
     * Instantiates a new evaluate persister.
     *
     * @param stats the evaluation statistics object
     * @param params the service parameters
     * @param repository the repository
     */
    public AbstractEvaluatePersister(EvaluateStats stats, ServiceParameters params, RrdRepository repository) {
        super(params, repository);
        this.stats = stats;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#persistStringAttribute(org.opennms.netmgt.model.ResourcePath, java.lang.String, java.lang.String)
     */
    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#isIgnorePersist()
     */
    @Override
    public boolean isIgnorePersist() {
        return true;
    }

    /**
     * Gets the resource id.
     *
     * @param resource the resource
     * @return the resource id
     */
    protected String getResourceId(CollectionResource resource) {
        final StringBuilder sb = new StringBuilder();
        sb.append(resource.getParent());
        sb.append('/').append(resource.getResourceTypeName());
        if (!CollectionResource.RESOURCE_TYPE_NODE.equals(resource.getResourceTypeName())) {
            sb.append('/').append(resource.getInterfaceLabel());
        }
        return sb.toString();
    }
}