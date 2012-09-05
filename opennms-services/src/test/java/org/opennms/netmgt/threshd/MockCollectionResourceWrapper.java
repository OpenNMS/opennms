/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.Date;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

public class MockCollectionResourceWrapper extends CollectionResourceWrapper {

    public MockCollectionResourceWrapper(final String instance) {
        super(new Date(), 0, null, null, null, new CollectionResource() {
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
            public String getParent() {
                return null;
            }
            public TimeKeeper getTimeKeeper() {
                return null;
            }
        }, null);
    }

}
