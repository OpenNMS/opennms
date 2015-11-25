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

package org.opennms.web.rest.v2.bsm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.model.BusinessService;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.rest.v2.ResourceLocationFactory;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceDTO;
import org.opennms.web.rest.v2.bsm.model.IpServiceDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BusinessServiceManager {

    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    // we need this for the blueprint.xml integration
    public void setBusinessServiceDao(BusinessServiceDao businessServiceDao) {
        this.businessServiceDao = businessServiceDao;
    }

    // we need this for the blueprint.xml integration
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        this.monitoredServiceDao = monitoredServiceDao;
    }

    public List<BusinessServiceDTO> findAll() {
        List<BusinessService> all = getDao().findAll();
        if (all == null) {
            return null;
        }
        return transform(all);
    }

    public Long save(BusinessServiceDTO newObject) {
        BusinessService service = transform(newObject);
        return getDao().save(service);
    }

    public void update(BusinessServiceDTO objectToUpdate) {
        final BusinessService existingBusinessService = getBusinessService(objectToUpdate.getId());
        BeanUtils.copyProperties(transform(objectToUpdate), existingBusinessService);
        getDao().update(existingBusinessService);
    }

    public BusinessServiceDTO getById(Long id) {
        BusinessService service = getBusinessService(id);
        BusinessServiceDTO entity = transform(service);
        return entity;
    }

    public void delete(Long id) {
        BusinessService service = getBusinessService(id);
        getDao().delete(service);
    }

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

    private BusinessServiceDao getDao() {
        return businessServiceDao;
    }

    private BusinessService transform(BusinessServiceDTO dto) {
        BusinessService service = new BusinessService();
        service.setId(dto.getId());
        service.setName(dto.getName());
        service.setAttributes(new HashMap<>(dto.getAttributes()));
        for (IpServiceDTO eachService : dto.getIpServices()) {
            OnmsMonitoredService ipService = getIpService(Integer.valueOf(eachService.getId()));
            service.addIpService(ipService);
        }
        return service;
    }

    private BusinessServiceDTO transform(BusinessService service) {
        BusinessServiceDTO dto = new BusinessServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setAttributes(new HashMap<>(service.getAttributes()));
        for (OnmsMonitoredService eachService : service.getIpServices()) {
            IpServiceDTO ipServiceDTO = transform(eachService);
            if (ipServiceDTO != null) {
                dto.addIpService(ipServiceDTO);
            }
        }
        return dto;
    }

    private IpServiceDTO transform(OnmsMonitoredService input) {
        if (input != null) {
            IpServiceDTO output = new IpServiceDTO();
            if (input.getId() != null) {
                output.setId(String.valueOf(input.getId()));
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
