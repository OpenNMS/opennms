package org.opennms.netmgt.bsm.service.model;

import org.opennms.web.rest.api.ResourceLocation;

import java.util.Map;
import java.util.Set;

public interface BusinessService {
    Long getId();

    String getName();

    void setName(String name);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    void addAttribute(String key, String value);

    String removeAttribute(String key);

    Set<IpService> getIpServices();

    void setIpServices(Set<IpService> ipServices);

    void addIpService(IpService ipService);

    void removeIpService(IpService ipService);

    Set<BusinessService> getChildServices();

    void setChildServices(Set<BusinessService> childServices);

    void addChildService(BusinessService childService);

    void removeChildService(BusinessService childService);

    Set<BusinessService> getParentServices();

    void save();

    void delete();
}
