/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm;

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toRequestDto;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toResponseDTO;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toResponseDto;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.transform;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_CAROLINA_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_DUKE_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_NCSTATE_REDUCTION_KEY;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.SingleReductionKeyEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IncreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityAboveEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy;
import org.opennms.netmgt.bsm.test.hierarchies.SimpleTestHierarchy;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaListDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Used to test the business services REST endpoints using both
 * XML and JSON encoding.
 *
 * @author jesse
 */
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
    "file:../../../../opennms-webapp-rest/src/test/resources/META-INF/opennms/component-dao.xml",
    "file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
    "file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-common.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public abstract class AbstractBusinessServiceRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    @Qualifier("bsmDatabasePopulator")
    private BsmDatabasePopulator databasePopulator;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    public abstract String getMediaType();

    public abstract String marshal(Object o);

    public abstract <T> T getAndUnmarshal(String url, int expectedStatus, Class<T> expectedClass) throws Exception;

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        BeanUtils.assertAutowiring(this);
        databasePopulator.populateDatabase();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        databasePopulator.resetDatabase(true);
    }

    public AbstractBusinessServiceRestServiceIT() {
        super("file:../../../../opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-rest-v2.xml");
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void canAddIpServiceEdge() throws Exception {
        // Create a business service without any edges
        BusinessServiceEntity service = new BusinessServiceEntityBuilder()
                .name("Dummy Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        final Long serviceId = m_businessServiceDao.save(service);
        m_businessServiceDao.flush();

        // The Request to send to create an edge
        IpServiceEdgeRequestDTO edgeRequestDTO = new IpServiceEdgeRequestDTO();
        edgeRequestDTO.setMapFunction(new FunctionsManager().getMapFunctionDTO(new Identity()));

        // verify adding of not existing ip service is not possible
        edgeRequestDTO.setIpServiceId(-1);
        sendData(POST, getMediaType(), buildIpServiceEdgeUrl(serviceId), marshal(edgeRequestDTO), 404);

        // verify adding of existing ip service is possible
        edgeRequestDTO.setIpServiceId(10);
        sendData(POST, getMediaType(), buildIpServiceEdgeUrl(serviceId), marshal(edgeRequestDTO), 200);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getIpServiceEdges().size());

        // verify adding twice possible, but not modified
        sendData(POST, getMediaType(), buildIpServiceEdgeUrl(serviceId), marshal(edgeRequestDTO), 304);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getIpServiceEdges().size());

        // verify adding of existing ip service is possible
        edgeRequestDTO.setIpServiceId(17);
        sendData(POST, getMediaType(), buildIpServiceEdgeUrl(serviceId), marshal(edgeRequestDTO), 200);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getIpServiceEdges().size());
    }

    @Test
    public void canAddChildServiceEdge() throws Exception {
        // Create a child and parent Business Service without any edges
        BusinessServiceEntity childEntity = new BusinessServiceEntityBuilder()
                .name("Child Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        BusinessServiceEntity parentEntity = new BusinessServiceEntityBuilder()
                .name("Parent Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        final Long parentServiceId = m_businessServiceDao.save(parentEntity);
        final Long childServiceId = m_businessServiceDao.save(childEntity);
        m_businessServiceDao.flush();

        // The Request to send to create the edge
        ChildEdgeRequestDTO edgeRequestDTO = new ChildEdgeRequestDTO();
        edgeRequestDTO.setMapFunction(new FunctionsManager().getMapFunctionDTO(new Identity()));

        // verify adding of not existing ip parentEntity is not possible
        edgeRequestDTO.setChildId(-1L);
        sendData(POST, getMediaType(), buildChildServiceEdgeUrl(parentServiceId), marshal(edgeRequestDTO), 404);

        // verify adding of existing ip parentEntity is possible
        edgeRequestDTO.setChildId(childServiceId);
        sendData(POST, getMediaType(), buildChildServiceEdgeUrl(parentServiceId), marshal(edgeRequestDTO), 200);
        Assert.assertEquals(1, m_businessServiceDao.get(parentServiceId).getChildEdges().size());

        // verify adding twice possible, but not modified
        sendData(POST, getMediaType(), buildChildServiceEdgeUrl(parentServiceId), marshal(edgeRequestDTO), 304);
        Assert.assertEquals(1, m_businessServiceDao.get(parentServiceId).getChildEdges().size());
    }

    @Test
    public void canAddReductionKeyEdge() throws Exception {
        // Create a business service without any edges
        BusinessServiceEntity service = new BusinessServiceEntityBuilder()
                .name("Dummy Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        final Long serviceId = m_businessServiceDao.save(service);
        m_businessServiceDao.flush();

        // The Request to send to create an edge
        ReductionKeyEdgeRequestDTO edgeRequestDTO = new ReductionKeyEdgeRequestDTO();
        edgeRequestDTO.setMapFunction(new FunctionsManager().getMapFunctionDTO(new Identity()));

        // verify adding of existing ip service is possible
        edgeRequestDTO.setReductionKey("1st reduction key");
        sendData(POST, getMediaType(), buildReductionKeyEdgeUrl(serviceId), marshal(edgeRequestDTO), 200);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getReductionKeyEdges().size());

        // verify adding twice possible, but not modified
        sendData(POST, getMediaType(), buildReductionKeyEdgeUrl(serviceId), marshal(edgeRequestDTO), 304);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getReductionKeyEdges().size());

        // verify adding of existing ip service is possible
        edgeRequestDTO.setReductionKey("2nd reduction key");
        sendData(POST, getMediaType(), buildReductionKeyEdgeUrl(serviceId), marshal(edgeRequestDTO), 200);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getReductionKeyEdges().size());
    }

    @Test
    public void canRemoveEdges() throws Exception {
        BusinessServiceEntity child = new BusinessServiceEntityBuilder()
                .name("Child Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        m_businessServiceDao.save(child);
        BusinessServiceEntity parent = new BusinessServiceEntityBuilder()
                .name("Parent Service")
                .reduceFunction(new HighestSeverityEntity())
                // add the IP services edges
                .addIpService(monitoredServiceDao.get(17), new SetToEntity(OnmsSeverity.CRITICAL.getId()))
                .addIpService(monitoredServiceDao.get(18), new IgnoreEntity())
                .addIpService(monitoredServiceDao.get(20), new IdentityEntity())
                .addReductionKey("abc", new IgnoreEntity())
                .addReductionKey("abcd", new IgnoreEntity())
                .addChildren(child, new IncreaseEntity())
                .toEntity();
        final Long parentServiceId = m_businessServiceDao.save(parent);
        m_businessServiceDao.flush();

        // verify that test data is set up correctly
        Assert.assertEquals(3, parent.getIpServiceEdges().size());
        Assert.assertEquals(2, parent.getReductionKeyEdges().size());
        Assert.assertEquals(1, parent.getChildEdges().size());
        Assert.assertEquals(6, parent.getEdges().size());

        // determine edge ids
        List<Long> edgeIdList = parent.getEdges().stream().map(e -> e.getId()).sorted().collect(Collectors.toList());

        // verify removing not existing ip service not possible
        sendData(DELETE, getMediaType(), buildEdgeUrl(parentServiceId, -1), "", 404);

        // verify removing of existing ip service is possible
        for (int i = 0; i < edgeIdList.size(); i++) {
            long edgeId = edgeIdList.get(i);
            int edgesLeftCount = edgeIdList.size() - i - 1;

            // verify removing of existing ip service is possible
            sendData(DELETE, getMediaType(), buildEdgeUrl(parentServiceId, edgeId), "", 200);
            Assert.assertEquals(edgesLeftCount, m_businessServiceDao.get(parentServiceId).getEdges().size());

            // verify removing twice possible, but not modified
            sendData(DELETE, getMediaType(), buildEdgeUrl(parentServiceId, edgeId), "", 304);
            Assert.assertEquals(edgesLeftCount, m_businessServiceDao.get(parentServiceId).getEdges().size());
        }
    }

    @Test
    public void canReloadDaemon() throws Exception {
        sendPost("business-services/daemon/reload", "", 200, null);
    }

    private String buildIpServiceEdgeUrl(Long serviceId) {
        return String.format("%s/ip-service-edge", buildServiceUrl(serviceId));
    }

    private String buildReductionKeyEdgeUrl(Long serviceId) {
        return String.format("%s/reduction-key-edge", buildServiceUrl(serviceId));
    }

    private String buildChildServiceEdgeUrl(Long serviceId) {
        return String.format("%s/child-edge", buildServiceUrl(serviceId));
    }

    private String buildEdgeUrl(Long serviceId, long edgeId) {
        return String.format("%s/edges/%s", buildServiceUrl(serviceId), edgeId);
    }

    private String buildServiceUrl(Long serviceId) {
        return String.format("/business-services/%s", serviceId);
    }

    private List<ResourceLocation> listBusinessServices() throws Exception {
        List<ResourceLocation> services = getAndUnmarshal("/business-services", 200, BusinessServiceListDTO.class).getServices();
        return services;
    }

    /**
     * Verifies that the given reduction key is atached to the provided Business Service
     */
    private void verifyReductionKey(String reductionKey, BusinessServiceResponseDTO responseDTO) {
        final Set<String> reductionKeys = Sets.newHashSet();
        responseDTO.getReductionKeys().forEach(e -> reductionKeys.addAll(e.getReductionKeys()));
        Assert.assertTrue("Expect reduction key '" + reductionKey + "' to be present in retrieved BusinessServiceResponseDTO.", reductionKeys.contains(reductionKey));
    }

    @Test
    public void canRetrieveBusinessServices() throws Exception {
        // Add business services to the DB
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("Application Servers")
                .addReductionKey("MyReductionKey", new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        Long id = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Retrieve the list of business services
        List<ResourceLocation> businessServices = listBusinessServices();

        // Verify
        Assert.assertEquals(1, businessServices.size());
        BusinessServiceResponseDTO expectedResponseDTO = toResponseDto(bs);
        BusinessServiceResponseDTO actualResponseDTO = getAndUnmarshal(
                buildServiceUrl(id),
                200, BusinessServiceResponseDTO.class);
        Assert.assertEquals(expectedResponseDTO, actualResponseDTO);
        verifyReductionKey("MyReductionKey", actualResponseDTO);
    }

    @Test
    public void canCreateBusinessService() throws Exception {
        final BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("some-service")
                .addAttribute("some-key", "some-value")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("reductionKey-1", new IdentityEntity())
                .addReductionKey("reductionKey-2", new IdentityEntity())
                .addReductionKey("reductionKey-3", new IdentityEntity())
                .toEntity();

        sendData(POST, getMediaType(), "/business-services", marshal(toRequestDto(bs)) , 201);
        Assert.assertEquals(1, m_businessServiceDao.countAll());

        for (BusinessServiceEntity eachEntity : m_businessServiceDao.findAll()) {
            BusinessServiceResponseDTO responseDTO = verifyResponse(eachEntity);
            Assert.assertEquals(3, responseDTO.getReductionKeys().size());
        }
    }

    /**
     * Verify that the Rest API can deal with setting of a hierarchy
     */
    @Test
    public void canCreateSimpleHierarchy() throws Exception {
        final SimpleTestHierarchy testData = new SimpleTestHierarchy(databasePopulator);
        // clear hierarchy
        for(BusinessServiceEntity eachEntity : testData.getServices()) {
            eachEntity.getEdges().clear();
        }
        // save business services
        for (BusinessServiceEntity eachEntity : testData.getServices()) {
            sendData(POST, getMediaType(), "/business-services", marshal(toRequestDto(eachEntity)), 201);
        }
        // set hierarchy
        BusinessServiceEntity parentEntity = findEntityByName("Parent")
                .addChildServiceEdge(findEntityByName("Child 1"), new IdentityEntity(), 1)
                .addChildServiceEdge(findEntityByName("Child 2"), new IdentityEntity(), 1);
        sendData(PUT, getMediaType(), buildServiceUrl(parentEntity.getId()), marshal(toRequestDto(parentEntity)), 204);

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

    @Test
    public void canCreateBambooHierarchy() throws Exception {
        final BambooTestHierarchy testData = new BambooTestHierarchy();

        // save hierarchy for later use
        final Map<BusinessServiceEntity, Set<BusinessServiceEdgeEntity>> edgeMap = Maps.newHashMap();
        testData.getServices().forEach(eachEntity -> edgeMap.put(eachEntity, Sets.newHashSet(eachEntity.getEdges())));
        testData.getServices().forEach(eachEntity -> eachEntity.setEdges(Sets.newHashSet())); // clear hierarchy

        // save business services to database
        for (BusinessServiceEntity eachEntity : testData.getServices()) {
            sendData(POST, getMediaType(), "/business-services", marshal(toRequestDto(eachEntity)), 201);
        }

        // apply ids (we created objects via rest)
        edgeMap.keySet().forEach(s -> s.setId(findEntityByName(s.getName()).getId()));

        // set hierarchy
        for (Map.Entry<BusinessServiceEntity, Set<BusinessServiceEdgeEntity>> eachEntry : edgeMap.entrySet()) {
            eachEntry.getKey().setEdges(eachEntry.getValue());
            sendData(PUT, getMediaType(), buildServiceUrl(eachEntry.getKey().getId()), marshal(toRequestDto(eachEntry.getKey())), 204);
        }

        // verify
        Assert.assertEquals(3, m_businessServiceDao.countAll());
        BusinessServiceEntity parentEntity = findEntityByName("Bamboo");
        BusinessServiceEntity mastersBusinessServiceEntity = findEntityByName("Master");
        BusinessServiceEntity agentsBusinessServiceEntity = findEntityByName("Agents");
        Assert.assertEquals(0, m_businessServiceDao.findParents(parentEntity).size());
        Assert.assertEquals(2, parentEntity.getChildEdges().size());
        Assert.assertEquals(1, m_businessServiceDao.findParents(mastersBusinessServiceEntity).size());
        Assert.assertEquals(2, mastersBusinessServiceEntity.getReductionKeyEdges().size());
        Assert.assertEquals(1, m_businessServiceDao.findParents(agentsBusinessServiceEntity).size());
        Assert.assertEquals(3, agentsBusinessServiceEntity.getReductionKeyEdges().size());

        // Verify Weight
        Assert.assertEquals(2, getReductionKeyEdge(agentsBusinessServiceEntity, BAMBOO_AGENT_DUKE_REDUCTION_KEY).getWeight());
        Assert.assertEquals(2, getReductionKeyEdge(agentsBusinessServiceEntity, BAMBOO_AGENT_CAROLINA_REDUCTION_KEY).getWeight());
        Assert.assertEquals(1, getReductionKeyEdge(agentsBusinessServiceEntity, BAMBOO_AGENT_NCSTATE_REDUCTION_KEY).getWeight());

        // verify rest
        verifyResponse(parentEntity);
        verifyResponse(mastersBusinessServiceEntity);
        verifyResponse(agentsBusinessServiceEntity);
    }

    /**
     * Ensures that the given BusinessServiceEntity matches the result returned from the Rest API when asking for the
     * business service with the Business Service Entities id.
     * The Verification is done by transforming the given entity to a BusinessServiceResponseDTO and compare it with the
     * returned one from the Rest API.
     * @param expectedEntity The values one expects.
     * @return The returned response from the Rest API.
     * @throws Exception
     */
    private BusinessServiceResponseDTO verifyResponse(BusinessServiceEntity expectedEntity) throws Exception {
        final BusinessServiceResponseDTO responseDTO = getAndUnmarshal(
                buildServiceUrl(expectedEntity.getId()),
                200,
                BusinessServiceResponseDTO.class);
        Assert.assertEquals(expectedEntity.getId(), Long.valueOf(responseDTO.getId()));
        Assert.assertEquals(expectedEntity.getName(), responseDTO.getName());
        Assert.assertEquals(expectedEntity.getAttributes(), responseDTO.getAttributes());
        Assert.assertEquals(Status.INDETERMINATE, responseDTO.getOperationalStatus());
        Assert.assertEquals(expectedEntity.getReductionKeyEdges().size(), responseDTO.getReductionKeys().size());
        Assert.assertEquals(expectedEntity.getReductionKeyEdges()
                .stream()
                .map(it -> toResponseDTO(it))
                .collect(Collectors.toList()), responseDTO.getReductionKeys());
        Assert.assertEquals(expectedEntity.getChildEdges().size(), responseDTO.getChildren().size());
        Assert.assertEquals(expectedEntity.getChildEdges()
                .stream()
                .map(e -> toResponseDTO(e))
                .collect(Collectors.toList()), responseDTO.getChildren());
        Assert.assertEquals(expectedEntity.getIpServiceEdges().size(), responseDTO.getIpServices().size());
        Assert.assertEquals(expectedEntity.getIpServiceEdges()
                .stream()
                .map(e -> toResponseDTO(e))
                .collect(Collectors.toList()), responseDTO.getIpServices());
        Assert.assertEquals(m_businessServiceDao.findParents(expectedEntity)
                .stream()
                .map(e -> e.getId())
                .collect(Collectors.toSet()), responseDTO.getParentServices());
        Assert.assertEquals(transform(expectedEntity.getReductionFunction()), responseDTO.getReduceFunction());
        return responseDTO;
    }

    private SingleReductionKeyEdgeEntity getReductionKeyEdge(BusinessServiceEntity businessService, String reductionKey) {
        Optional<SingleReductionKeyEdgeEntity> first = businessService.getReductionKeyEdges().stream().filter(e -> e.getReductionKey().equals(reductionKey)).findFirst();
        return first.get(); // throws NoSuchElement if not present
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
                .addReductionKey("key1", new IdentityEntity())
                .addReductionKey("key2-deleteMe", new IdentityEntity())
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        final Long serviceId = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // update
        BusinessServiceRequestDTO requestDTO = toRequestDto(bs);
        requestDTO.setName("New Name");
        requestDTO.getAttributes().put("key", "value");
        requestDTO.getReductionKeys().clear();
        requestDTO.addReductionKey("key1updated", new FunctionsManager().getMapFunctionDTO(new Ignore()), Edge.DEFAULT_WEIGHT);
        sendData(PUT, getMediaType(), "/business-services/" + serviceId, marshal(requestDTO), 204);

        // Reload from database and verify changes
        bs = m_businessServiceDao.get(serviceId);
        Assert.assertEquals(requestDTO.getName(), bs.getName());
        Assert.assertEquals(requestDTO.getAttributes(), bs.getAttributes());
        Assert.assertEquals(1, bs.getReductionKeyEdges().size());
        Assert.assertEquals(1, bs.getEdges().size());
        Assert.assertEquals(1, m_businessServiceDao.findAll().size());
        Assert.assertEquals(Sets.newHashSet(), bs.getIpServiceEdges());
        BusinessServiceResponseDTO responseDTO = verifyResponse(bs);
        verifyReductionKey("key1updated", responseDTO);
    }

    @Test
    public void verifyListFunctions() throws Exception {
        List<FunctionMetaDTO> mapFunctions = getAndUnmarshal("/business-services/functions/map", 200, FunctionMetaListDTO.class).getFunctions();
        Assert.assertEquals(5, mapFunctions.size());
        for (FunctionMetaDTO eachFunction : mapFunctions) {
            FunctionMetaDTO manuallyRead = getAndUnmarshal("/business-services/functions/map/" + eachFunction.getName(), 200, FunctionMetaDTO.class);
            Assert.assertEquals(eachFunction, manuallyRead);
        }

        List<FunctionMetaDTO> reduceFunctions = getAndUnmarshal("/business-services/functions/reduce", 200, FunctionMetaListDTO.class).getFunctions();
        Assert.assertEquals(4, reduceFunctions.size());
        for (FunctionMetaDTO eachFunction : reduceFunctions) {
            FunctionMetaDTO manuallyRead = getAndUnmarshal("/business-services/functions/reduce/" + eachFunction.getName(), 200, FunctionMetaDTO.class);
            Assert.assertEquals(eachFunction, manuallyRead);
        }
    }

    @Test
    public void verifyHighestSeverityAboveReduceFunction() throws Exception {
        BusinessServiceEntity entity = new BusinessServiceEntityBuilder()
                .name("Dummy Service")
                .reduceFunction(new HighestSeverityAboveEntity(Status.CRITICAL.ordinal()))
                .toEntity();
        sendData(POST, getMediaType(), "/business-services", marshal(toRequestDto(entity)), 201);

        entity.setId(findEntityByName("Dummy Service").getId());

        verifyResponse(entity);
    }

    @Test
    public void verifyFriendlyName() throws Exception {
        BusinessServiceEntity entity = new BusinessServiceEntityBuilder()
                .name("Some Custom Name")
                .addReductionKey("My Reduction Key", new IdentityEntity(), "so friendly")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();

        sendData(POST, getMediaType(), "/business-services", marshal(toRequestDto(entity)), 201);
        BusinessServiceResponseDTO responseDTO = verifyResponse(findEntityByName("Some Custom Name"));
        Assert.assertEquals(1, responseDTO.getReductionKeys().size());
        Assert.assertEquals("so friendly", responseDTO.getReductionKeys().get(0).getFriendlyName());
    }
}
