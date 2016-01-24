/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toRequestDto;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toXml;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdge;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.MostCriticalEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BsmTestData;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionListDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionListDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-service.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
    "file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-common.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class BusinessServiceRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private BsmDatabasePopulator databasePopulator;

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Autowired
    private BusinessServiceEdgeDao m_businessServiceEdgeDao;

    @Autowired
    private MapFunctionDao m_mapFunctionDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    private MostCriticalEntity m_mostCritical;

    private IdentityEntity m_identity;

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        BeanUtils.assertAutowiring(this);
        databasePopulator.populateDatabase();

        // Create the reduction function
        m_mostCritical = new MostCriticalEntity();

        // Create the map function
        m_identity = new IdentityEntity();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        databasePopulator.resetDatabase(true);
    }

    public BusinessServiceRestServiceIT() {
        super("file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-rest-v2.xml");
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void canAddIpService() throws Exception {
        BusinessServiceEntity service = new BusinessServiceEntity();
        service.setName("Dummy Service");
        service.setReductionFunction(m_mostCritical);
        final Long serviceId = m_businessServiceDao.save(service);
        m_businessServiceDao.flush();

        // verify adding not existing ip service not possible
        sendPost(getIpServiceUrl(serviceId, -1), "", 404, null);

        // verify adding of existing ip service is possible
        sendPost(getIpServiceUrl(serviceId, 10), "", 200, null);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());

        // verify adding twice possible, but not modified
        sendPost(getIpServiceUrl(serviceId, 10), "", 304, null);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());

        // verify adding of existing ip service is possible
        sendPost(getIpServiceUrl(serviceId, 17), "", 200, null);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());
    }

    @Test
    public void canRemoveIpService() throws Exception {
        BusinessServiceEntity service = new BusinessServiceEntity();
        service.setName("Dummy Service");
        service.setReductionFunction(m_mostCritical);
        final Long serviceId = m_businessServiceDao.save(service);
        service.getIpServices().add(monitoredServiceDao.get(17));
        service.getIpServices().add(monitoredServiceDao.get(18));
        service.getIpServices().add(monitoredServiceDao.get(20));
        m_businessServiceDao.saveOrUpdate(service);
        m_businessServiceDao.flush();

        // verify removing not existing ip service not possible
        sendData(DELETE, MediaType.APPLICATION_XML, getIpServiceUrl(serviceId, -1), "", 404);

        // verify removing of existing ip service is possible
        sendData(DELETE, MediaType.APPLICATION_XML, getIpServiceUrl(serviceId, 17), "", 200);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());

        // verify removing twice possible, but not modified
        sendData(DELETE, MediaType.APPLICATION_XML, getIpServiceUrl(serviceId, 17), "", 304);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());

        // verify removing of existing ip service is possible
        sendData(DELETE, MediaType.APPLICATION_XML, getIpServiceUrl(serviceId, 18), "", 200);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getEdges(IPServiceEdge.class).size());
    }

    private void addIpServiceEdge(BusinessServiceEntity serviceEntity, OnmsMonitoredService ipService) {
        serviceEntity.addIpServiceEdge(ipService, m_identity);
        m_businessServiceDao.saveOrUpdate(serviceEntity);
        m_businessServiceDao.flush();
    }

    private String buildIpServiceUrl(Long serviceId, int ipServiceId) {
        return String.format("/business-services/%s/ip-service/%s", serviceId, ipServiceId);
    }

    private String buildServiceUrl(Long serviceId) {
        return String.format("/business-services/%s", serviceId);
    }

    private List<ResourceLocation> listBusinessServices() throws Exception {
        JAXBContext context = JAXBContext.newInstance(BusinessServiceListDTO.class, ResourceLocation.class);
        List<ResourceLocation> services = getXmlObject(context, "/business-services", 200, BusinessServiceListDTO.class).getServices();
        return services;
    }

    /**
     * Verifies that the given reduction key is atached to the provided Business Service
     */
    private void verifyReductionKey(String reductionKey, BusinessServiceResponseDTO responseDTO) {
        List<ReductionKeyEdgeResponseDTO> rkList = responseDTO.getReductionKeys().stream().filter(rkEdge -> rkEdge.getReductionKey().equals(reductionKey)).collect(Collectors.toList());
        Assert.assertTrue("Expect reduction key '" + reductionKey + "' to be present in retrieved BusinessServiceResponseDTO.", rkList.size() == 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canRetrieveBusinessServices() throws Exception {
        // Add business services to the DB
        BusinessServiceEntity bs = new BusinessServiceEntity();
        bs.setName("Application Servers");
        bs.setReductionFunction(m_mostCritical);
        Long id = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Retrieve the list of business services
        String url = "/business-services";
        JAXBContext context = JAXBContext.newInstance(BusinessServiceListDTO.class, ResourceLocation.class);
        List<ResourceLocation> businessServices = getXmlObject(context, url, 200, BusinessServiceListDTO.class).getServices();

        // Verify
        Assert.assertEquals(databasePopulator.expectedBsCount(1), businessServices.size());
        BusinessServiceResponseDTO expectedResponseDTO = toResponseDto(bs);
        expectedResponseDTO.setLocation(ResourceLocationFactory.createBusinessServiceLocation(id.toString()));
        expectedResponseDTO.setOperationalStatus(Status.INDETERMINATE);
        BusinessServiceResponseDTO actualResponseDTO = getXmlObject(
                JAXBContext.newInstance(BusinessServiceResponseDTO.class, ResourceLocation.class),
                buildServiceUrl(id),
                200, BusinessServiceResponseDTO.class);
        Assert.assertEquals(expectedResponseDTO, actualResponseDTO);
        verifyReductionKey("MyReductionKey", actualResponseDTO);
    }

    @Test
    public void canCreateBusinessService() throws Exception {
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("some-service")
                .addAttribute("some-key", "some-value")
                .toEntity();

        // TODO JSON cannot be deserialized by the rest service. Fix me.
//        sendData(POST, MediaType.APPLICATION_JSON, "/business-services", toJson(toRequestDto(bs)), 201);
        sendData(POST, MediaType.APPLICATION_XML, "/business-services", toXml(toRequestDto(bs)) , 201);
        Assert.assertEquals(databasePopulator.expectedBsCount(1), m_businessServiceDao.countAll());

        for (BusinessServiceEntity eachEntity : m_businessServiceDao.findAll()) {
            BusinessServiceResponseDTO responseDTO = verifyResponse(eachEntity);
            Assert.assertEquals(3, responseDTO.getReductionKeys().size());
        }
    }

    /**
     * Verify that the Rest API can deal with setting of a hierarchy
     */
    @Test
    public void canCreateHierarchy() throws Exception {
        final BsmTestData testData = new BsmTestData(databasePopulator.getDatabasePopulator());
        // clear hierarchy
        for(BusinessServiceEntity eachEntity : testData.getServices()) {
            eachEntity.getEdges().clear();
        }
        // save business services
        for (BusinessServiceEntity eachEntity : testData.getServices()) {
            sendData(POST, MediaType.APPLICATION_XML, "/business-services", toXml(toRequestDto(eachEntity)), 201);
        }
        // set hierarchy
        BusinessServiceEntity parentEntity = findEntityByName("Parent")
                .addChildServiceEdge(findEntityByName("Child 1"), m_identity)
                .addChildServiceEdge(findEntityByName("Child 2"), m_identity);
        sendData(PUT, MediaType.APPLICATION_XML, buildServiceUrl(parentEntity.getId()), toXml(toRequestDto(parentEntity)), 204);

        // Verify
        Assert.assertEquals(3, m_businessServiceDao.countAll());
        parentEntity = findEntityByName("Parent");
        BusinessServiceEntity child1Entity = findEntityByName("Child 1");
        BusinessServiceEntity child2Entity = findEntityByName("Child 2");
        Assert.assertEquals(0, m_businessServiceDao.findParents(parentEntity).size());
        Assert.assertEquals(2, parentEntity.getChildEdges().size());
        Assert.assertEquals(1, m_businessServiceDao.findParents(child1Entity).size());
        Assert.assertEquals(0, child1Entity.getChildEdges().size());
        Assert.assertEquals(1, m_businessServiceDao.findParents(child2Entity).size());
        Assert.assertEquals(0, child2Entity.getChildEdges().size());
        verifyResponse(parentEntity);
        verifyResponse(child1Entity);
        verifyResponse(child2Entity);
    }

    private BusinessServiceResponseDTO verifyResponse(BusinessServiceEntity entity) throws Exception {
        final BusinessServiceResponseDTO responseDTO = getXmlObject(
                JAXBContext.newInstance(BusinessServiceResponseDTO.class),
                buildServiceUrl(entity.getId()),
                200,
                BusinessServiceResponseDTO.class);
        Assert.assertEquals(entity.getId(), Long.valueOf(responseDTO.getId()));
        Assert.assertEquals(entity.getName(), responseDTO.getName());
        Assert.assertEquals(entity.getAttributes(), responseDTO.getAttributes());
        Assert.assertEquals(entity.getReductionKeyEdges().size(), responseDTO.getReductionKeys().size()); // TODO MVR we should verify the content not only the size
        Assert.assertEquals(responseDTO.getReductionKeys(), responseDTO.getReductionKeys());
        Assert.assertEquals(Status.INDETERMINATE, responseDTO.getOperationalStatus());
        Assert.assertEquals(entity.getChildEdges()
                .stream()
                .map(e -> e.getChild().getId())
                .collect(Collectors.toSet()), responseDTO.getChildren()); // TODO MVR this is going to fail...
        Assert.assertEquals(m_businessServiceDao.findParents(entity)
                .stream()
                .map(e -> e.getId())
                .collect(Collectors.toSet()), responseDTO.getParentServices()); // TODO MVR this is going to fail..
        Assert.assertEquals(entity.getIpServiceEdges()
                .stream()
                .map(e -> e.getIpService())
                .collect(Collectors.toSet()), responseDTO.getIpServices());
        return responseDTO;
    }

    private BusinessServiceEntity findEntityByName(String name) {
        Criteria criteria = new CriteriaBuilder(BusinessServiceEntity.class).eq("name", name).toCriteria();
        List<BusinessServiceEntity> matching = m_businessServiceDao.findMatching(criteria);
        if (matching.isEmpty()) {
            throw new NoSuchElementException("Did not find business service with name '" + name + "'.");
        }
        return matching.get(0);
    }

    @Test
    public void canUpdateBusinessService() throws Exception {
        // initialize
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("Dummy Service")
                .addAttribute("some-key", "some-value")
                .toEntity();

        final Long serviceId = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // update
        BusinessServiceRequestDTO requestDTO = toRequestDto(bs);
        requestDTO.setName("New Name");
        requestDTO.getAttributes().put("key", "value");
        requestDTO.getReductionKeys().clear();
        requestDTO.addReductionKey("key1updated", MapFunctionDTO.Type.Ignore.toDTO(new Ignore()));

        // TODO JSON cannot be de-serialized by the rest service. Fix me.
//        sendData(PUT, MediaType.APPLICATION_JSON, "/business-services/" + serviceId, toJson(requestDTO), 204);
        sendData(PUT, MediaType.APPLICATION_XML, "/business-services/" + serviceId, toXml(requestDTO), 204);

        // verify
        BusinessServiceResponseDTO responseDTO = getXmlObject(
                JAXBContext.newInstance(BusinessServiceResponseDTO.class, ResourceLocation.class),
                "/business-services/" + serviceId,
                200,
                BusinessServiceResponseDTO.class);
        Assert.assertEquals(requestDTO.getName(), responseDTO.getName());
        Assert.assertEquals(Sets.newHashSet(), responseDTO.getIpServices());
        Assert.assertEquals(requestDTO.getAttributes(), responseDTO.getAttributes());
        Assert.assertEquals(requestDTO.getChildServices().size(), responseDTO.getChildren().size()); // TODO MVR verify content instead of list size
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getReductionKeyEdges().size()); // TODO MVR verify content instead of list size
        verifyReductionKey("key1updated", responseDTO);
    }

    @Test
    public void verifyListFunctions() throws Exception {
        List<MapFunctionDTO> mapFunctions = getXmlObject(JAXBContext.newInstance(MapFunctionListDTO.class), "/business-services/functions/map", 200, MapFunctionListDTO.class).getFunctions();
        List<ReduceFunctionDTO> reduceFunctions = getXmlObject(JAXBContext.newInstance(ReduceFunctionListDTO.class), "/business-services/functions/reduce", 200, ReduceFunctionListDTO.class).getFunctions();

        Assert.assertEquals(5, mapFunctions.size());
        Assert.assertEquals(2, reduceFunctions.size());
    }
}
