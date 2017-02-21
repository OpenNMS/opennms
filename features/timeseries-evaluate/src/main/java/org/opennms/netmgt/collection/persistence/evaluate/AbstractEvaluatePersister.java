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
        final StringBuffer sb = new StringBuffer();
        sb.append(resource.getParent());
        sb.append('/').append(resource.getResourceTypeName());
        if (!CollectionResource.RESOURCE_TYPE_NODE.equals(resource.getResourceTypeName())) {
            sb.append('/').append(resource.getInterfaceLabel());
        }
        return sb.toString();
    }
}