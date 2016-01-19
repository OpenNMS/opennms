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

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toJson;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toRequestDto;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toResponseDto;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.toXml;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
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
@JUnitTemporaryDatabase
public class BusinessServiceRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private BsmDatabasePopulator databasePopulator;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        BeanUtils.assertAutowiring(this);
        databasePopulator.populateDatabase();
    }

    @After
    @Transactional
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
    @JUnitTemporaryDatabase
    @Transactional
    public void canAddIpService() throws Exception {
        BusinessServiceEntity service = new BusinessServiceEntity();
        service.setName("Dummy Service");
        final Long serviceId = m_businessServiceDao.save(service);
        m_businessServiceDao.flush();

        // verify adding not existing ip service not possible
        sendPost(buildIpServiceUrl(serviceId, -1), "", 404, null);

        // verify adding of existing ip service is possible
        sendPost(buildIpServiceUrl(serviceId, 10), "", 200, null);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getIpServices().size());

        // verify adding twice possible, but not modified
        sendPost(buildIpServiceUrl(serviceId, 10), "", 304, null);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getIpServices().size());

        // verify adding of existing ip service is possible
        sendPost(buildIpServiceUrl(serviceId, 17), "", 200, null);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getIpServices().size());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void canRemoveIpService() throws Exception {
        BusinessServiceEntity service = new BusinessServiceEntity();
        service.setName("Dummy Service");
        final Long serviceId = m_businessServiceDao.save(service);
        service.getIpServices().add(monitoredServiceDao.get(17));
        service.getIpServices().add(monitoredServiceDao.get(18));
        service.getIpServices().add(monitoredServiceDao.get(20));
        m_businessServiceDao.saveOrUpdate(service);
        m_businessServiceDao.flush();

        // verify removing not existing ip service not possible
        sendData(DELETE, MediaType.APPLICATION_XML, buildIpServiceUrl(serviceId, -1), "", 404);

        // verify removing of existing ip service is possible
        sendData(DELETE, MediaType.APPLICATION_XML, buildIpServiceUrl(serviceId, 17), "", 200);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getIpServices().size());

        // verify removing twice possible, but not modified
        sendData(DELETE, MediaType.APPLICATION_XML, buildIpServiceUrl(serviceId, 17), "", 304);
        Assert.assertEquals(2, m_businessServiceDao.get(serviceId).getIpServices().size());

        // verify removing of existing ip service is possible
        sendData(DELETE, MediaType.APPLICATION_XML, buildIpServiceUrl(serviceId, 18), "", 200);
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getIpServices().size());
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
        Assert.assertTrue("Expect reduction key '" + reductionKey + "' to be present in retrieved BusinessServiceResponseDTO.", responseDTO.getReductionKeys().contains(reductionKey));
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    @SuppressWarnings("unchecked")
    public void canRetrieveBusinessServices() throws Exception {
        // Add business services to the DB
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("Application Servers")
                .addReductionKey("MyReductionKey")
                .toEntity();
        Long id = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Retrieve the list of business services
        List<ResourceLocation> businessServices = listBusinessServices();

        // Verify
        Assert.assertEquals(databasePopulator.expectedBsCount(1), businessServices.size());
        BusinessServiceResponseDTO expectedResponseDTO = toResponseDto(bs);
        expectedResponseDTO.setLocation(ResourceLocationFactory.createBusinessServiceLocation(id.toString()));
        expectedResponseDTO.setOperationalStatus(OnmsSeverity.INDETERMINATE);
        BusinessServiceResponseDTO actualResponseDTO = getXmlObject(
                JAXBContext.newInstance(BusinessServiceResponseDTO.class, ResourceLocation.class),
                buildServiceUrl(id),
                200, BusinessServiceResponseDTO.class);
        Assert.assertEquals(expectedResponseDTO, actualResponseDTO);
        verifyReductionKey("MyReductionKey", actualResponseDTO);
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void canCreateBusinessService() throws Exception {
        final BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("some-service")
                .addAttribute("some-key", "some-value")
                .addReductionKey("reductionKey-1")
                .addReductionKey("reductionKey-2")
                .addReductionKey("reductionKey-3")
                .toEntity();

        sendData(POST, MediaType.APPLICATION_JSON, "/business-services", toJson(toRequestDto(bs)), 201);
        sendData(POST, MediaType.APPLICATION_XML, "/business-services", toXml(toRequestDto(bs)) , 201);
        Assert.assertEquals(databasePopulator.expectedBsCount(2), m_businessServiceDao.countAll());

        for (BusinessServiceEntity eachEntity : m_businessServiceDao.findAll()) {
            BusinessServiceResponseDTO responseDTO = getXmlObject(
                    JAXBContext.newInstance(BusinessServiceResponseDTO.class),
                    buildServiceUrl(eachEntity.getId()),
                    200,
                    BusinessServiceResponseDTO.class);
            Assert.assertEquals(eachEntity.getId(), Long.valueOf(responseDTO.getId()));
            Assert.assertEquals(eachEntity.getName(), responseDTO.getName());
            Assert.assertEquals(eachEntity.getAttributes(), responseDTO.getAttributes());
            Assert.assertEquals(eachEntity.getReductionKeys(), responseDTO.getReductionKeys());
            Assert.assertEquals(OnmsSeverity.INDETERMINATE, responseDTO.getOperationalStatus());
            Assert.assertEquals(eachEntity.getChildServices()
                    .stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet()), responseDTO.getChildServices());
            Assert.assertEquals(eachEntity.getParentServices()
                    .stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toSet()), responseDTO.getChildServices());
            Assert.assertEquals(eachEntity.getName(), responseDTO.getName());
            Assert.assertEquals(3, responseDTO.getReductionKeys().size());
        }
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void canUpdateBusinessService() throws Exception {
        // initialize
        BusinessServiceEntity bs = new BusinessServiceEntityBuilder()
                .name("Dummy Service")
                .addAttribute("some-key", "some-value")
                .addReductionKey("key1")
                .addReductionKey("key2-deleteMe")
                .toEntity();
        final Long serviceId = m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // update
        BusinessServiceRequestDTO requestDTO = toRequestDto(bs);
        requestDTO.setName("New Name");
        requestDTO.getAttributes().put("key", "value");
        requestDTO.getReductionKeys().clear();
        requestDTO.getReductionKeys().add("key1updated");

        sendData(PUT, MediaType.APPLICATION_JSON, "/business-services/" + serviceId, toJson(requestDTO), 204);
        sendData(PUT, MediaType.APPLICATION_XML, "/business-services/" + serviceId, toXml(requestDTO), 204);

        // verify
        BusinessServiceResponseDTO responseDTO = getXmlObject(
                JAXBContext.newInstance(BusinessServiceResponseDTO.class, ResourceLocation.class),
                buildServiceUrl(serviceId),
                200,
                BusinessServiceResponseDTO.class);
        Assert.assertEquals(1, m_businessServiceDao.findAll().size());
        Assert.assertEquals(requestDTO.getName(), responseDTO.getName());
        Assert.assertEquals(Sets.newHashSet(), responseDTO.getIpServices());
        Assert.assertEquals(requestDTO.getAttributes(), responseDTO.getAttributes());
        Assert.assertEquals(requestDTO.getChildServices(), responseDTO.getChildServices());
        Assert.assertEquals(1, m_businessServiceDao.get(serviceId).getReductionKeys().size());
        verifyReductionKey("key1updated", responseDTO);
    }
}
