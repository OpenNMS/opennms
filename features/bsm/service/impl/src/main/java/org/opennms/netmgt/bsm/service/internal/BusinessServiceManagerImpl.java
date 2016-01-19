/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class BusinessServiceManagerImpl implements BusinessServiceManager {
    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private ReductionFunctionDao reductionFunctionDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Override
    public List<BusinessService> getAllBusinessServices() {
        return getDao().findAll().stream()
                .map(s -> new BusinessServiceImpl(this, s))
                .collect(Collectors.toList());
    }

    @Override
    public BusinessService createBusinessService() {
        return new BusinessServiceImpl(this, new BusinessServiceEntity());
    }

    @Override
    public void saveBusinessService(BusinessService service) {
        BusinessServiceEntity entity = getBusinessServiceEntity(service);
        getDao().saveOrUpdate(entity);
    }

    @Override
    public BusinessService getBusinessServiceById(Long id) {
        BusinessServiceEntity entity = getBusinessServiceEntity(id);
        return new BusinessServiceImpl(this, entity);
    }

    @Override
    public void deleteBusinessService(BusinessService businessService) {
        BusinessServiceEntity entity = getBusinessServiceEntity(businessService);

        entity.getParentServices().forEach(p -> p.getChildServices().remove(entity));
        entity.setParentServices(Collections.emptySet());

        entity.getChildServices().forEach(p -> p.getChildServices().remove(entity));
        entity.setChildServices(Collections.emptySet());

        getDao().delete(entity);
    }

    @Override
    public void setIpServices(final BusinessService businessService, final Set<IpService> ipServices) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(businessService);

        entity.setIpServices(ipServices.stream()
                                       .map(this::getMonitoredService)
                                       .collect(Collectors.toSet()));
    }

    @Override
    public boolean assignIpService(BusinessService businessService, IpService ipService) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        final OnmsMonitoredService monitoredService = getMonitoredService(ipService);

        /* TODO: FIXME: HACK: JW, MVR
        // if already exists, no update
        if (entity.getIpServices().contains(monitoredService)) {
            return false;
        }

        // add and update
        entity.getIpServices().add(monitoredService);
        return true;
    }

    @Override
    public boolean removeIpService(BusinessService businessService, IpService ipService) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        final OnmsMonitoredService monitoredService = getMonitoredService(ipService);

        /* TODO: FIXME: HACK: JW, MVR
        // does not exist, no update necessary
        if (!entity.getIpServices().contains(monitoredService)) {
            return false;
        }

        // remove and update
        entity.getIpServices().remove(monitoredService);
        return true;
    }

    @Override
    public void setChildServices(BusinessService parentService, Set<BusinessService> childServices) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);

        for (final BusinessServiceEntity e : parentEntity.getChildServices()) {
            e.getParentServices().remove(parentEntity);
        }

        parentEntity.setChildServices(childServices.stream()
                                                   .map(s -> getBusinessServiceEntity(s))
                                                   .collect(Collectors.toSet()));

        for (final BusinessServiceEntity e : parentEntity.getChildServices()) {
            e.getParentServices().add(parentEntity);
        }
    }

    @Override
    public boolean assignChildService(BusinessService parentService, BusinessService childService) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        final BusinessServiceEntity childEntity = getBusinessServiceEntity(childService);

        if (this.checkDescendantForLoop(parentEntity, childEntity)) {
            throw new IllegalArgumentException("Service will form a loop");
        }

        // if already exists, no update
        if (parentEntity.getChildServices().contains(childEntity)) {
            return false;
        }

        // add and update
        parentEntity.getChildServices().add(childEntity);
        childEntity.getParentServices().add(parentEntity);
        return true;
    }

    @Override
    public boolean removeChildService(BusinessService parentService, BusinessService childService) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        final BusinessServiceEntity childEntity = getBusinessServiceEntity(childService);

        // does not exist, no update necessary
        if (!parentEntity.getChildServices().contains(childEntity)) {
            return false;
        }

        // remove and update
        parentEntity.getChildServices().remove(childEntity);
        childEntity.getParentServices().remove(parentEntity);
        return true;
    }

    private boolean checkDescendantForLoop(final BusinessServiceEntity parent,
                                           final BusinessServiceEntity descendant) {
        if (parent.equals(descendant)) {
            return true;
        }

        for (BusinessServiceEntity s : descendant.getChildServices()) {
            return this.checkDescendantForLoop(parent, s);
        }

        return false;
    }

    @Override
    public Set<BusinessService> getFeasibleChildServices(final BusinessService service) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(service);
        return getDao().findAll()
                       .stream()
                       .filter(s -> !this.checkDescendantForLoop(entity, s))
                       .map(s -> new BusinessServiceImpl(this, s))
                       .collect(Collectors.<BusinessService>toSet());
    }

    @Override
    public OnmsSeverity getOperationalStatusForBusinessService(BusinessService service) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(service);
        final OnmsSeverity severity = businessServiceStateMachine.getOperationalStatus(entity);
        return severity != null ? severity : OnmsSeverity.INDETERMINATE;
    }

    @Override
    public OnmsSeverity getOperationalStatusForIPService(IpService ipService) {
        final OnmsMonitoredService monitoredService = getMonitoredService(ipService);
        final OnmsSeverity severity = businessServiceStateMachine.getOperationalStatus(monitoredService);
        return severity != null ? severity : OnmsSeverity.INDETERMINATE;
    }

    @Override
    public List<IpService> getAllIpServices() {
        return monitoredServiceDao.findAll().stream()
                                  .map(s -> new IpServiceImpl(this, s))
                                  .collect(Collectors.toList());
    }

    @Override
    public IpService getIpServiceById(Integer id) {
        OnmsMonitoredService entity = getMonitoredService(id);
        return new IpServiceImpl(this, entity);
    }

    BusinessServiceDao getDao() {
        return this.businessServiceDao;
    }

    NodeDao getNodeDao() {
        return this.nodeDao;
    }

    private BusinessServiceEntity getBusinessServiceEntity(BusinessService service) throws NoSuchElementException {
        return ((BusinessServiceImpl) service).getEntity();
    }

    private BusinessServiceEntity getBusinessServiceEntity(Long serviceId) throws NoSuchElementException {
        final BusinessServiceEntity entity = getDao().get(serviceId);
        if (entity == null) {
            throw new NoSuchElementException();
        }
        return entity;
    }

    private OnmsMonitoredService getMonitoredService(IpService ipService) throws NoSuchElementException {
        return ((IpServiceImpl) ipService).getEntity();
    }

    private OnmsMonitoredService getMonitoredService(Integer serviceId) throws NoSuchElementException {
        final OnmsMonitoredService monitoredService = monitoredServiceDao.get(serviceId);
        if (monitoredService == null) {
            throw new NoSuchElementException();
        }
        return monitoredService;
    }
}
