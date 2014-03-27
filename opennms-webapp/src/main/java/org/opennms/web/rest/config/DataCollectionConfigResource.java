package org.opennms.web.rest.config;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.internal.collection.DataCollectionConfigConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
public class DataCollectionConfigResource implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfigResource.class);
    private static final List<IncludeCollection> EMPTY_INCLUDE_LIST = Collections.emptyList();

    @Resource(name="dataCollectionConfigDao")
    private DataCollectionConfigDao m_dataCollectionConfigDao;

    @Context
    private ResourceContext m_context;

    @Context 
    private UriInfo m_uriInfo;

    public void setDataCollectionConfigDao(final DataCollectionConfigDao dao) {
        m_dataCollectionConfigDao = dao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_dataCollectionConfigDao, "DataCollectionConfigDao must be set!");
        Assert.isTrue(m_dataCollectionConfigDao instanceof AbstractJaxbConfigDao<?,?>);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getDataCollectionConfiguration() throws ConfigurationResourceException {
        LOG.info("getDatacollectionConfigurationForLocation()");

        @SuppressWarnings("unchecked")
        final AbstractJaxbConfigDao<DatacollectionConfig,DatacollectionConfig> dao = (AbstractJaxbConfigDao<DatacollectionConfig,DatacollectionConfig>)m_dataCollectionConfigDao;
        final DatacollectionConfig dcc = dao.getContainer().getObject();
        if (dcc == null) {
            return Response.status(404).build();
        }

        // we want our own copy so we don't modify anything in the datacollection config dao
        final DatacollectionConfig modifiable = new DatacollectionConfig();
        modifiable.setRrdRepository(dcc.getRrdRepository());

        final String resourceTypeName = "__resource_type_collection";
        final SnmpCollection resourceTypeCollection = dcc.getSnmpCollection(resourceTypeName);
        
        for (final SnmpCollection collection : dcc.getSnmpCollections()) {
            if (resourceTypeName.equals(collection.getName())) {
                // skip the special case collection
                continue;
            }
            final SnmpCollection cloned = collection.clone();
            // DefaultDataCollectionConfigDao already does all the include work, so don't pass them along
            cloned.setIncludeCollections(EMPTY_INCLUDE_LIST);
            if (resourceTypeCollection != null) {
                cloned.setResourceTypes(resourceTypeCollection.getResourceTypes());
            }
            modifiable.addSnmpCollection(cloned);
        }

        final DataCollectionConfigConverter converter = new DataCollectionConfigConverter();
        modifiable.visit(converter);

        return Response.ok(converter.getDataCollectionConfig()).build();
    }
}
