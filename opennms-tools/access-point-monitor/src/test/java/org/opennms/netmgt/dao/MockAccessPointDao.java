/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;

public class MockAccessPointDao extends AbstractMockDao<OnmsAccessPoint,String> implements AccessPointDao {

    @Override
    protected void generateId(final OnmsAccessPoint ap) {
    }

    @Override
    protected String getId(final OnmsAccessPoint ap) {
        return ap.getPhysAddr();
    }

    @Override
    public OnmsAccessPointCollection findByPackage(final String pkg) {
        final OnmsAccessPointCollection collection = new OnmsAccessPointCollection();
        for (final OnmsAccessPoint ap : findAll()) {
            if (pkg.equals(ap.getPollingPackage())) {
                collection.add(ap);
            }
        }
        return collection;
    }

    @Override
    public List<String> findDistinctPackagesLike(final String pkg) {
        final Set<String> packages = new HashSet<>();
        for (final OnmsAccessPoint ap : findAll()) {
            packages.add(ap.getPollingPackage());
        }
        return new ArrayList<String>(packages);
    }

    @Override
    public void merge(OnmsAccessPoint ap) {
        save(ap);
    }

}
