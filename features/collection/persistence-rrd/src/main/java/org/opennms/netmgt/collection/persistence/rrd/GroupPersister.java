/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.rrd;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * <p>GroupPersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GroupPersister extends BasePersister {

    /**
     * <p>Constructor for GroupPersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public GroupPersister(ServiceParameters params, RrdRepository repository) {
        super(params, repository);

    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            
            Map<String, String> dsNamesToRrdNames = new LinkedHashMap<String , String>();
            for (CollectionAttribute a : group.getAttributes()) {
                if (ResourceTypeUtils.isNumericType(a.getType())) {
                    dsNamesToRrdNames.put(a.getName(), group.getName());
                }
            }
            
            createBuilder(group.getResource(), group.getName(), group.getGroupType().getAttributeTypes());
            try {
                File path = group.getResource().getResourceDir(getRepository());
                ResourceTypeUtils.updateDsProperties(path, dsNamesToRrdNames);
            } catch (FileNotFoundException e) {
                LOG.warn("Could not update datasource properties: " + e.getMessage(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }


}
