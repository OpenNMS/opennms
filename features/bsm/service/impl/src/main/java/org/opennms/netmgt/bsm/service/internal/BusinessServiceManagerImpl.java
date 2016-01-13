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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.MostCritical;
import org.opennms.netmgt.bsm.persistence.api.ReductionFunctionDao;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.bsm.service.model.IpServiceDTO;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    public List<BusinessServiceDTO> findAll() {
        List<BusinessService> all = getDao().findAll();
        if (all == null) {
            return null;
        }
        return transform(all);
    }

    @Override
    public List<BusinessServiceDTO> findMatching(Criteria criteria) {
        List<BusinessService> all = getDao().findMatching(criteria);
        if (all == null) {
            return null;
        }
        return transform(all);
    }

    @Override
    public Long save(BusinessServiceDTO newObject) {
        BusinessService service = transform(newObject);

        // TODO: FIXME: HACK: The reduction function is required, but not exposed via the
        // DTOs yet, so we set a default one here, pending the development of BSM-97
        if (service.getReductionFunction() == null) {
            MostCritical mostCritical = new MostCritical();
            reductionFunctionDao.save(mostCritical);
            service.setReductionFunction(mostCritical);
        }

        return getDao().save(service);
    }

    @Override
    public void update(BusinessServiceDTO objectToUpdate) {
        final BusinessService existingBusinessService = getBusinessService(objectToUpdate.getId());
        BeanUtils.copyProperties(transform(objectToUpdate), existingBusinessService);

        // TODO: FIXME: HACK: The reduction function is required, but not exposed via the
        // DTOs yet, so we set a default one here, pending the development of BSM-97
        if (existingBusinessService.getReductionFunction() == null) {
            MostCritical mostCritical = new MostCritical();
            reductionFunctionDao.save(mostCritical);
            existingBusinessService.setReductionFunction(mostCritical);
        }

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

        service.getParentServices().forEach(p -> p.removeChildService(service));
        service.setParentServices(Collections.emptySet());

        service.getChildServices().forEach(p -> p.removeParentService(service));
        service.setChildServices(Collections.emptySet());

        getDao().delete(service);
    }

    @Override
    public boolean assignIpInterface(Long serviceId, Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);

        /* TODO: FIXME: HACK: JW, MVR
        // if already exists, no update
        if (service.getIpServices().contains(monitoredService)) {
            return false;
        }

        // add and update
        service.addIpService(monitoredService);
        */
        getDao().update(service);
        return true;
    }

    @Override
    public boolean removeIpInterface(Long serviceId, Integer ipServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final OnmsMonitoredService monitoredService = getIpService(ipServiceId);

        /* TODO: FIXME: HACK: JW, MVR
        // does not exist, no update necessary
        if (!service.getIpServices().contains(monitoredService)) {
            return false;
        }

        // remove and update
        service.removeIpService(monitoredService);
        */
        getDao().update(service);
        return true;
    }

    @Override
    public boolean assignChildService(Long serviceId, Long childServiceId) {
        final BusinessService service = getBusinessService(serviceId);
        final BusinessService childService = getBusinessService(childServiceId);

        if (this.checkDescendantForLoop(service, childService)) {
            throw new IllegalArgumentException("Service will form a loop");
        }

        // if already exists, no update
        if (service.getChildServices().contains(childService)) {
            return false;
        }

        // add and update
        service.addChildService(childService);
        childService.addParentService(service);
        getDao().update(service);
        getDao().update(childService);
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
        childService.removeParentService(service);
        getDao().update(service);
        getDao().update(childService);
        return true;
    }

    private boolean checkDescendantForLoop(final BusinessService parent,
                                           final BusinessService descendant) {
        if (parent.equals(descendant)) {
            return true;
        }

        for (BusinessService s : descendant.getChildServices()) {
            return this.checkDescendantForLoop(parent, s);
        }

        return false;
    }

    @Override
    public Set<BusinessServiceDTO> getFeasibleChildServices(final BusinessServiceDTO serviceDTO) {
        final BusinessService service = transform(serviceDTO);
        return getDao().findAll()
                       .stream()
                       .filter(s -> !this.checkDescendantForLoop(service, s))
                       .map(this::transform)
                       .collect(Collectors.toSet());
    }

    @Override
    public Set<BusinessServiceDTO> getParentServices(BusinessServiceDTO childServiceDTO) {
        final BusinessService childService = transform(childServiceDTO);
        return childService.getParentServices()
                           .stream()
                           .map(this::transform)
                           .collect(Collectors.toSet());
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

    @Override
    public List<IpServiceDTO> getAllIpServiceDTO() {
        return transformAll(monitoredServiceDao.findAll());
    }

    @Override
    public void triggerDaemonReload() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "BSM Master Page");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, "bsmd");
        eventForwarder.sendNow(eventBuilder.getEvent());
    }

    private BusinessServiceDao getDao() {
        return businessServiceDao;
    }

    private BusinessService transform(BusinessServiceDTO dto) {
        BusinessService service = new BusinessService();
        service.setId(dto.getId());
        service.setName(dto.getName());
        service.setAttributes(new HashMap<>(dto.getAttributes()));
        /* TODO: FIXME: HACK: JW, MVR
        for (IpServiceDTO eachService : dto.getIpServices()) {
            OnmsMonitoredService ipService = getIpService(Integer.valueOf(eachService.getId()));
            service.addIpService(ipService);
        }
        */
        return service;
    }

    private BusinessServiceDTO transform(BusinessService service) {
        return transform(service, true);
    }

    private BusinessServiceDTO transform(BusinessService service, boolean recurse) {
        BusinessServiceDTO dto = new BusinessServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setAttributes(new HashMap<>(service.getAttributes()));
        /* TODO: FIXME: HACK: JW, MVR
        for (OnmsMonitoredService eachService : service.getIpServices()) {
            IpServiceDTO ipServiceDTO = transform(eachService);
            if (ipServiceDTO != null) {
                dto.addIpService(ipServiceDTO);
            }
        }
        */
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
                output.setReductionKeys(OnmsMonitoredServiceHelper.getReductionKeys(input));
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
