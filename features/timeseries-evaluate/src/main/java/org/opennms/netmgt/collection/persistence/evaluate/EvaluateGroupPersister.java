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

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EvaluateGroupPersister.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateGroupPersister extends AbstractEvaluatePersister {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EvaluateGroupPersister.class);

    /**
     * Instantiates a new evaluate persister.
     *
     * @param stats the evaluation statistics object
     * @param params the service parameters
     * @param repository the repository
     */
    public EvaluateGroupPersister(EvaluateStats stats, ServiceParameters params, RrdRepository repository) {
        super(stats, params, repository);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#visitGroup(org.opennms.netmgt.collection.api.AttributeGroup)
     */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            final String resourceId = getResourceId(group.getResource());
            final String groupId = resourceId + '/' + group.getName();
            LOG.debug("visitGroup: {} = {}", groupId, group.getAttributes().size());
            stats.checkNode(group.getResource());
            stats.checkGroup(groupId);
            stats.checkResource(resourceId);
            group.getAttributes().forEach(a -> {
                final String attribId = resourceId + '/' + a.getName();
                if (a.getAttributeType().getType().isNumeric()) {
                    LOG.debug("visitGroup: attribute {}", attribId);
                    stats.checkAttribute(attribId, true);
                    stats.markNumericSamplesMeter();
                } else {
                    stats.checkAttribute(attribId, false);
                }
            });
            setBuilder(new EvaluatorPersistOperationBuilder());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#completeGroup(org.opennms.netmgt.collection.api.AttributeGroup)
     */
    @Override
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }

}
