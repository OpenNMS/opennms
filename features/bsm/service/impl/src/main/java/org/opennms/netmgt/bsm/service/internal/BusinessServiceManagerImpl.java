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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.bsm.service.model.IpServiceDTO;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BusinessServiceManagerImpl implements BusinessServiceManager {
    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Override
    public List<BusinessServiceDTO> findAll() {
        List<BusinessService> all = getDao().findAll();
        if (all == null) {
            return null;
        }
        return transform(all);
    }

    @Override
    public Long save(BusinessServiceDTO newObject) {
        BusinessService service = transform(newObject);
        return getDao().save(service);
    }

    @Override
    public void update(BusinessServiceDTO objectToUpdate) {
        final BusinessService existingBusinessService = getBusinessService(objectToUpdate.getId());
        BeanUtils.copyProperties(transform(objectToUpdate), existingBusinessService);
        getDao().update(existingBusinessService);
    }

    @Override
    public BusinessServiceDTO getById(Long id) {
        BusinessService service = getBusinessService(id);
        BusinessServiceDTO entity = transform(service);
        return entity;
    }

    @Override
    public void delete(Long id) {
        BusinessService service = getBusinessService(id);
        getDao().delete(service);
    }



    @Override
    public boolean assignIpInterface(Long serviceId, Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);

        // if already exists, no update
        if (service.getIpServices().contains(monitoredService)) {
            return false;
        }

        // add and update
        service.addIpService(monitoredService);
        getDao().update(service);
        return true;
    }

    @Override
    public boolean removeIpInterface(Long serviceId, Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);

        // does not exist, no update necessary
        if (!service.getIpServices().contains(monitoredService)) {
            return false;
        }

        // remove and update
        service.removeIpService(monitoredService);
        getDao().update(service);
        return true;
    }

    @Override
    public boolean assignChildService(Long serviceId, Long childServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final BusinessService childService = getBusinessService(childServiceId);

        // if already exists, no update
        if (service.getChildServices().contains(childService)) {
            return false;
        }

        // add and update
        service.addChildService(childService);
        getDao().update(service);
        return true;
    }

    @Override
    public boolean removeChildService(Long serviceId, Long childServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final BusinessService childService = getBusinessService(childServiceId);

        // does not exist, no update necessary
        if (!service.getChildServices().contains(childService)) {
            return false;
        }

        // remove and update
        service.removeChildService(childService);
        getDao().update(service);
        return true;
    }

    @Override
    public OnmsSeverity getOperationalStatusForBusinessService(Long serviceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsSeverity severity = businessServiceStateMachine.getOperationalStatus(service);
        return severity != null ? severity : OnmsSeverity.INDETERMINATE;
    }

    @Override
    public OnmsSeverity getOperationalStatusForIPService(Integer ipServiceId) {
        final OnmsMonitoredService ipService = getIpService(ipServiceId);
        final OnmsSeverity severity = businessServiceStateMachine.getOperationalStatus(ipService);
        return severity != null ? severity : OnmsSeverity.INDETERMINATE;
    }

    private BusinessServiceDao getDao() {
        return businessServiceDao;
    }

    private BusinessService transform(BusinessServiceDTO dto) {
        BusinessService service = new BusinessService();
        service.setId(dto.getId());
        service.setName(dto.getName());
        service.setAttributes(new HashMap<>(dto.getAttributes()));
        service.setReductionKeys(new HashSet<>(dto.getReductionKeys()));
        for (IpServiceDTO eachService : dto.getIpServices()) {
            OnmsMonitoredService ipService = getIpService(Integer.valueOf(eachService.getId()));
            service.addIpService(ipService);
        }
        for (BusinessServiceDTO eachService : dto.getChildServices()) {
            BusinessService childService = getBusinessService(Long.valueOf(eachService.getId()));
            service.addChildService(childService);
        }
        return service;
    }

    private BusinessServiceDTO transform(BusinessService service) {
        BusinessServiceDTO dto = new BusinessServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setAttributes(new HashMap<>(service.getAttributes()));
        dto.setReductionKeys(new HashSet<>(service.getReductionKeys()));
        for (OnmsMonitoredService eachService : service.getIpServices()) {
            IpServiceDTO ipServiceDTO = transform(eachService);
            if (ipServiceDTO != null) {
                dto.addIpService(ipServiceDTO);
            }
        }
        for (BusinessService eachService : service.getChildServices()) {
            BusinessServiceDTO childServiceDTO = transform(eachService);
            if (childServiceDTO != null) {
                dto.addChildService(childServiceDTO);
            }
        }
        return dto;
    }

    private List<IpServiceDTO> transformAll(List<OnmsMonitoredService> all) {
        if (all != null) {
            return all.stream().map(this::transform).collect(Collectors.toList());
        }
        return null;
    }

    private IpServiceDTO transform(OnmsMonitoredService input) {
        if (input != null) {
            IpServiceDTO output = new IpServiceDTO();
            if (input.getId() != null) {
                output.setId(String.valueOf(input.getId()));
                output.setNodeLabel(nodeDao.get(input.getNodeId()).getLabel());
                output.setServiceName(input.getServiceName());
                output.setIpAddress(input.getIpAddress().toString());
                output.setLocation(ResourceLocationFactory.createIpServiceLocation(output.getId()));
                return output;
            }
        }
        return null;
    }

    private List<BusinessServiceDTO> transform(List<BusinessService> all) {
        if (all != null) {
            List<BusinessServiceDTO> transformedList = new ArrayList<>();
            for (BusinessService eachService : all) {
                BusinessServiceDTO serviceDTO = transform(eachService);
                if (serviceDTO != null) {
                    transformedList.add(serviceDTO);
                }
            }
            return transformedList;
        }
        return null;
    }

    private BusinessService getBusinessService(Long serviceId) throws NoSuchElementException {
        final BusinessService service = getDao().get(serviceId);
        if (service == null) {
            throw new NoSuchElementException();
        }
        return service;
    }

    private OnmsMonitoredService getIpService(Integer serviceId) throws NoSuchElementException {
        final OnmsMonitoredService monitoredService = monitoredServiceDao.get(serviceId);
        if (monitoredService == null) {
            throw new NoSuchElementException();
        }
        return monitoredService;
    }
}
