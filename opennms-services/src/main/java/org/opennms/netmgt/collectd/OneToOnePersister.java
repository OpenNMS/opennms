/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;


/**
 * <p>OneToOnePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OneToOnePersister extends BasePersister {

    /**
     * <p>Constructor for OneToOnePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public OneToOnePersister(ServiceParameters params,  RrdRepository repository) {
        super(params, repository);
    }

    /** {@inheritDoc} */
    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        if (shouldPersist()) {
            createBuilder(attribute.getResource(), attribute.getName(), attribute.getAttributeType());
            storeAttribute(attribute);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
        	commitBuilder();
        }
        popShouldPersist();
    }



}
