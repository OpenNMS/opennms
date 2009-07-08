/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.io.File;

import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

public class MockCollectionResourceWrapper extends CollectionResourceWrapper {

    public MockCollectionResourceWrapper(final String instance) {
        super(0, null, null, null, new CollectionResource() {
            public String getInstance() {
                return instance;
            }
            public String getLabel() {
                return null;
            }
            public String getResourceTypeName() {
                return null;
            }
            public int getType() {
                return 0;
            }
            public boolean rescanNeeded() {
                return false;
            }
            public boolean shouldPersist(ServiceParameters params) {
                return false;
            }
            public void visit(CollectionSetVisitor visitor) {
            }
            public String getOwnerName() {
                return null;
            }
            public File getResourceDir(RrdRepository repository) {
                return null;
            }
        }, null);
    }

}
