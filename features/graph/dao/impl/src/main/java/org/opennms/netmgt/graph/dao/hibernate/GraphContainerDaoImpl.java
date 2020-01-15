/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.dao.hibernate;

import java.util.List;
import java.util.NoSuchElementException;

import org.opennms.netmgt.graph.dao.api.GraphContainerDao;
import org.opennms.netmgt.graph.GraphContainerEntity;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class GraphContainerDaoImpl extends HibernateDaoSupport implements GraphContainerDao {

    @Override
    public void save(GraphContainerEntity graphContainerEntity) {
        getHibernateTemplate().save(graphContainerEntity);
    }

    @Override
    public void update(GraphContainerEntity graphContainerEntity) {
        getHibernateTemplate().update(graphContainerEntity);
    }

    @Override
    public GraphContainerEntity findContainerById(String containerId) {
        final List<GraphContainerEntity> containers = (List<GraphContainerEntity>) getHibernateTemplate().find("Select ge from GraphContainerEntity ge where ge.namespace = ?", containerId);
        if (containers.isEmpty()) {
            return null;
        }
        return containers.get(0);
    }

    @Override
    public GraphContainerEntity findContainerInfoById(String containerId) {
        // Fetch all meta data of the container and graphs (no vertices and edges) with one select
        // We load the container and all its graph entities as well as the related properties.
        // This may load unnecessary properties, but is probably insignificant at the moment.
        // Vertices and Edges are not loaded, as they are lazy loaded.
        final List<GraphContainerEntity> graphContainerEntities = (List<GraphContainerEntity>) getHibernateTemplate()
                .find("select distinct ge from GraphContainerEntity ge join ge.properties join ge.graphs as graphs join graphs.properties where ge.namespace = ?", containerId);
        if (graphContainerEntities.isEmpty()) {
            return null;
        }
        return graphContainerEntities.get(0);
    }

    @Override
    public void delete(String containerId) {
        final GraphContainerEntity containerEntity = findContainerById(containerId);
        if (containerEntity == null) {
            throw new NoSuchElementException("No container with id " + containerId + " found.");
        }
        getHibernateTemplate().delete(containerEntity);
    }
}
