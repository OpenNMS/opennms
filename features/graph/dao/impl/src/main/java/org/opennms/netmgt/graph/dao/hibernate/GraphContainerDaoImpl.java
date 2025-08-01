/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
