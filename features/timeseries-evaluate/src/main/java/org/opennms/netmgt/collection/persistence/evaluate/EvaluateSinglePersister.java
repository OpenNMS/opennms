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

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EvaluateSinglePersister.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateSinglePersister extends AbstractEvaluatePersister {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EvaluateSinglePersister.class);

    /**
     * Instantiates a new evaluate persister.
     *
     * @param stats the evaluation statistics object
     * @param params the service parameters
     * @param repository the repository
     */
    public EvaluateSinglePersister(EvaluateStats stats, ServiceParameters params, RrdRepository repository) {
        super(stats, params, repository);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#visitAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        if (shouldPersist()) {
            final String resourceId = getResourceId(attribute.getResource());
            final String attribId = resourceId + '/' + attribute.getName();
            LOG.debug("visitAttribute: {}", attribId);
            stats.checkNode(attribute.getResource().getParent().getName());
            stats.checkResource(resourceId);
            if (attribute.getAttributeType().getType().isNumeric()) {
                stats.checkAttribute(attribId, true);
                stats.markNumericSamplesMeter();
            } else {
                stats.checkAttribute(attribId, false);
            }
            setBuilder(new EvaluatorPersistOperationBuilder());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.AbstractPersister#completeAttribute(org.opennms.netmgt.collection.api.CollectionAttribute)
     */
    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }

}
