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
import java.util.Properties;

import org.opennms.netmgt.dao.support.ResourceTypeUtils;

public class GroupPersister extends BasePersister {

    public GroupPersister(ServiceParameters params) {
        super(params);

    }

    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            createBuilder(group.getResource(), group.getName(), group.getGroupType().getAttributeTypes());
            File path = new File(group.getResource().getResourceDir(getRepository()).getAbsolutePath());
            Properties dsProperties = ResourceTypeUtils.getDsProperties(path); 
            boolean save = false;
            for (Attribute a : group.getAttributes()) {
                if (NumericAttributeType.supportsType(a.getType()) && !dsProperties.containsKey(a.getName())) {
                    dsProperties.setProperty(a.getName(), group.getName());
                    save = true;
                }
            }
            try {
                if (save) {
                    File dsFile = new File(path.getAbsolutePath(), ResourceTypeUtils.DS_PROPERTIES_FILE);
                    saveUpdatedProperties(dsFile, dsProperties);
                }
            } catch (Exception e) {
                log().error("Unable to save DataSource Properties file" + e, e);
            }
        }
    }


    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) commitBuilder();
        popShouldPersist();
    }


}
