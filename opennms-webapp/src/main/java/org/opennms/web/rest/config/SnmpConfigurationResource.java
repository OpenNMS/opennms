package org.opennms.web.rest.config;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class SnmpConfigurationResource {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(SnmpConfigurationResource.class);

    @Resource(name="snmp-config.xml")
    ConfigurationResource<SnmpConfig> m_snmpConfigResource;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getSnmpConfiguration() throws ConfigurationResourceException {
        return Response.ok(m_snmpConfigResource.get()).build();
    }
}
