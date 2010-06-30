//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;

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
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public GroupPersister(ServiceParameters params, RrdRepository repository) {
        super(params, repository);

    }

    /** {@inheritDoc} */
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            
            Map<String, String> dsNamesToRrdNames = new LinkedHashMap<String , String>();
            for (CollectionAttribute a : group.getAttributes()) {
                if (NumericAttributeType.supportsType(a.getType())) {
                    dsNamesToRrdNames.put(a.getName(), group.getName());
                }
            }
            
            createBuilder(group.getResource(), group.getName(), group.getGroupType().getAttributeTypes());
            File path = group.getResource().getResourceDir(getRepository());
            ResourceTypeUtils.updateDsProperties(path, dsNamesToRrdNames);
        }
    }

    /** {@inheritDoc} */
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }


}
