/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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
            stats.checkNode(group.getResource().getParent());
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