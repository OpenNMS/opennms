/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import static org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex.Type;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.BusinessServiceHierarchy;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BusinessServicesTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "bsm";

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServicesTopologyProvider.class);

    private BusinessServiceManager businessServiceManager;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServicesTopologyProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        super(new BusinessServiceVertexProvider(TOPOLOGY_NAMESPACE), new SimpleEdgeProvider(TOPOLOGY_NAMESPACE));
        this.transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    @Override
    public void save() {
        // we do not support save at the moment
    }

    private void load() {
        resetContainer();
        BusinessServiceHierarchy hierarchy = businessServiceManager.getHierarchy();
        // we only consider root business services to build the graph
        addBusinessServices(null, hierarchy.getRoots());
    }

    private void addBusinessServices(BusinessServiceVertex parentVertex, Collection<BusinessServiceDTO> businessServices) {
        for (BusinessServiceDTO eachBusinessService : businessServices) {
            addBusinessService(parentVertex, eachBusinessService);
        }
    }

    private void addBusinessService(BusinessServiceVertex parentVertex, BusinessServiceDTO businessService) {
        // create the vertex itself
        BusinessServiceVertex businessServiceVertex = new BusinessServiceVertex(businessService);
        addVertices(businessServiceVertex);

        // if we have a parent, connect the parent at the current business
        // service vertex as well
        if (parentVertex != null) {
            parentVertex.addChildren(businessServiceVertex);
            addEdges(createConnection(parentVertex, businessServiceVertex));
        }

        // add ip services
        for (IpServiceEdge eachIpEdge : businessService.getIpServiceEdges()) {
            AbstractBusinessServiceVertex serviceVertex = new IpServiceVertex(eachIpEdge.getIpService(), businessService.getLevel() + 1);
            businessServiceVertex.addChildren(serviceVertex);
            addVertices(serviceVertex);

            // connect with businessService
            Edge edge = createConnection(businessServiceVertex, serviceVertex);
            addEdges(edge);
        }

        // add reduction keys
        for (ReductionKeyEdge eachRkEdge : businessService.getReductionKeyEdges()) {
            AbstractBusinessServiceVertex rkVertex = new ReductionKeyVertex(eachRkEdge.getReductionKey(), businessService.getLevel() + 1);
            businessServiceVertex.addChildren(rkVertex);
            addVertices(rkVertex);

            // connect with businessService
            Edge edge = createConnection(businessServiceVertex, rkVertex);
            addEdges(edge);
        }

        // add children to the hierarchy as well
        addBusinessServices(businessServiceVertex, businessService.getChildServices());
    }

    private Edge createConnection(AbstractBusinessServiceVertex v1, AbstractBusinessServiceVertex v2) {
        String id = String.format("connection:%s:%s", v1.getId(), v2.getId());
        Edge edge = new AbstractEdge(getEdgeNamespace(), id, v1, v2);
        return edge;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        Objects.requireNonNull(businessServiceManager);
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    @Override
    public void refresh() {
        load();
    }

    @Override
    public Criteria getDefaultCriteria() {
        // Grab the business service with the smallest id
        List<BusinessService> businessServices = businessServiceManager.findMatching(new CriteriaBuilder(BusinessService.class).orderBy("id", true).limit(1).toCriteria());
        // If one was found, use it for the default focus
        if (!businessServices.isEmpty()) {
            BusinessService businessService = businessServices.iterator().next();
            BusinessServiceVertex businessServiceVertex = new BusinessServiceVertex(businessService);
            return new VertexHopGraphProvider.DefaultVertexHopCriteria(businessServiceVertex);
        }
        return null;
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        load();
    }

    @Override
    public void resetContainer() {
        super.resetContainer();
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType contentType) {
        // only consider vertices of our namespace and of the correct type
        final Set<AbstractBusinessServiceVertex> filteredSet = selectedVertices.stream()
                .filter(e -> TOPOLOGY_NAMESPACE.equals(e.getNamespace()))
                .filter(e -> e instanceof AbstractBusinessServiceVertex)
                .map(e -> (AbstractBusinessServiceVertex) e)
                .collect(Collectors.toSet());
        if (filteredSet.isEmpty()) {
            return SelectionChangedListener.Selection.NONE;
        }
        switch (contentType) {
            case Alarm:
                // show alarms with reduction keys associated with the current selection.
                final Set<String> reductionKeys = filteredSet.stream()
                        .map(vertex -> vertex.getReductionKeys())
                        .flatMap(rkSet -> rkSet.stream())
                        .collect(Collectors.toSet());
                return () -> {
                    if (reductionKeys != null && !reductionKeys.isEmpty()) {
                        return Lists.newArrayList(Restrictions.in("reductionKey", reductionKeys));
                    }
                    return Lists.newArrayList(Restrictions.isNull("id")); // is always false, so nothing is shown
                };
            case BusinessService:
                final Set<Long> businessServiceIds = Sets.newHashSet();

                // Business Service
                filteredSet.stream()
                        .filter(v -> v.getType() == Type.BusinessService)
                        .forEach(v -> businessServiceIds.add(((BusinessServiceVertex) v).getServiceId()));

                // Ip Service or Reduction Key
                filteredSet.stream()
                        .filter(v -> v.getType() == Type.IpService
                                || v.getType() == Type.ReductionKey)
                        .forEach(v -> businessServiceIds.add(((BusinessServiceVertex) v.getParent()).getServiceId()));
                return new SelectionChangedListener.IdSelection<>(businessServiceIds);
            case Node:
                final Set<Integer> nodeIds = filteredSet.stream()
                        .filter(v -> v.getType() == Type.IpService)
                        .map(v -> businessServiceManager.getIpServiceById(((IpServiceVertex) v).getIpServiceId()).getNodeId())
                        .collect(Collectors.toSet());
                return () -> {
                    if (nodeIds != null && !nodeIds.isEmpty()) {
                        return Lists.newArrayList(Restrictions.in("id", nodeIds));
                    }
                    return Lists.newArrayList(Restrictions.isNull("id")); // is always false, so nothing is shown
                };
        }
        throw new IllegalArgumentException(getClass().getSimpleName() + " does not support filtering vertices for contentType " + contentType);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(ContentType.Alarm, ContentType.Node, ContentType.BusinessService).contains(type);
    }
}
