package org.opennms.web.rest.v1.config;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.TrapdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("trapdConfigurationResource")
public class TrapdConfigurationResource {
	
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(TrapdConfigurationResource.class);
    
    @Resource(name="trapd-configuration.xml")
    ConfigurationResource<TrapdConfig> m_trapdConfigResource;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getTrapdConfiguration() throws ConfigurationResourceException {
        return Response.ok(m_trapdConfigResource.get()).build();
    }

}