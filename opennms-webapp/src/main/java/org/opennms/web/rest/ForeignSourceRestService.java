/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.Duration;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.netmgt.provision.persist.foreignsource.DetectorCollection;
import org.opennms.netmgt.provision.persist.foreignsource.DetectorWrapper;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 *<p>RESTful service to the OpenNMS Provisioning Foreign Source definitions.  Foreign source
 *definitions are used to control the scanning (service detection) of services for SLA monitoring
 *as well as the data collection settings for physical interfaces (resources).</p>
 *<p>This API supports CRUD operations for managing the Provisioner's foreign source definitions. Foreign
 *source definitions are POSTed and will be deployed when the corresponding requisition (provisioning group)
 *gets imported by provisiond.
 *<ul>
 *<li>GET/PUT/POST pending foreign sources</li>
 *<li>GET pending and deployed count</li>
 *</ul>
 *</p>
 *<p>Example 1: Create a new foreign source<i>Note: The foreign-source attribute typically has a 1 to 1
 *relationship to a provisioning group (a.k.a. requisition).  The relationship is only
 *implied by name and it is a best practice to use the same name for all three.  If a requisition exists with
 *the same name as a foreign source, it will be used during the provisioning (import) operations in lieu
 *of the default foreign source.</i></p>
 *<pre>
 *curl -X POST \
 *     -H "Content-Type: application/xml" \
 *     -d &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
 *         &lt;foreign-source date-stamp="2009-03-07T20:22:45.625-05:00" name="Cisco"
 *           xmlns:ns2="http://xmlns.opennms.org/xsd/config/model-import"
 *           xmlns="http://xmlns.opennms.org/xsd/config/foreign-source"&gt;
 *           &lt;scan-interval&gt;1d&lt;/scan-interval&gt;
 *           &lt;detectors&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.datagram.DnsDetector" name="DNS"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.simple.FtpDetector" name="FTP"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.simple.HttpDetector" name="HTTP"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.simple.HttpsDetector" name="HTTPS"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector" name="ICMP"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.simple.LdapDetector" name="LDAP"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector" name="SNMP"/&gt;
 *             &lt;detector class="org.opennms.netmgt.provision.detector.ssh.SshDetector" name="SSH"/&gt;
 *           &lt;/detectors&gt;
 *           &lt;policies&gt;
 *             &lt;policy class="org.opennms.netmgt.provision.persist.policies.MatchingInterfacePolicy" name="policy1"&gt;
 *               &lt;parameter value="~10\.*\.*\.*" key="ipAddress"/&gt;
 *             &lt;/policy&gt;
 *           &lt;/policies&gt;
 *         &lt;/foreign-source&gt; \
 *     -u admin:admin \
 *     http://localhost:8980/opennms/rest/foreignSources
 *</pre>
 *<p>Example 2: Query SNMP community string.</p>
 *<pre>
 *curl -X GET \
 *     -H "Content-Type: application/xml" \
 *     -u admin:admin \
 *        http://localhost:8980/opennms/rest/foreignSources/deployed \
 *        2>/dev/null \
 *        |xmllint --format -</pre>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("foreignSources")
public class ForeignSourceRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ForeignSourceRestService.class);

    
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;

    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("default")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ForeignSource getDefaultForeignSource() throws ParseException {
        readLock();
        try {
            m_deployedForeignSourceRepository.flush();
            return m_deployedForeignSourceRepository.getDefaultForeignSource();
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns all the deployed foreign sources
     *
     * @return Collection of OnmsForeignSources (ready to be XML-ified)
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("deployed")
    public ForeignSourceCollection getDeployedForeignSources() throws ParseException {
        readLock();
        try {
            m_deployedForeignSourceRepository.flush();
            return new ForeignSourceCollection(m_deployedForeignSourceRepository.getForeignSources());
        } finally {
            readUnlock();
        }
    }

    /**
     * returns a plaintext string being the number of pending foreign sources
     *
     * @return a int.
     */
    @GET
    @Path("deployed/count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDeployedCount() {
        readLock();
        try {
            m_pendingForeignSourceRepository.flush();
            return Integer.toString(m_pendingForeignSourceRepository.getForeignSourceCount());
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the union of deployed and pending foreign sources
     *
     * @return Collection of OnmsForeignSources (ready to be XML-ified)
     * @throws java.text.ParseException if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ForeignSourceCollection getForeignSources() throws ParseException {
        readLock();
        
        try {
            final Set<ForeignSource> foreignSources = new TreeSet<ForeignSource>();
            for (final String fsName : getActiveForeignSourceNames()) {
                foreignSources.add(getActiveForeignSource(fsName));
            }
            return new ForeignSourceCollection(foreignSources);
        } finally {
            readUnlock();
        }
    }
    
    /**
     * returns a plaintext string being the number of pending foreign sources
     *
     * @return a int.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTotalCount() throws ParseException {
        readLock();
        try {
            return Integer.toString(getActiveForeignSourceNames().size());
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the requested {@link ForeignSource}
     *
     * @param foreignSource the foreign source name
     * @return the foreign source
     */
    @GET
    @Path("{foreignSource}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ForeignSource getForeignSource(@PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            return getActiveForeignSource(foreignSource);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getDetectors</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.DetectorCollection} object.
     */
    @GET
    @Path("{foreignSource}/detectors")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public DetectorCollection getDetectors(@PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            return new DetectorCollection(getActiveForeignSource(foreignSource).getDetectors());
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getDetector</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param detector a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.DetectorWrapper} object.
     */
    @GET
    @Path("{foreignSource}/detectors/{detector}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public DetectorWrapper getDetector(@PathParam("foreignSource") String foreignSource, @PathParam("detector") String detector) {
        readLock();
        try {
            for (final PluginConfig pc : getActiveForeignSource(foreignSource).getDetectors()) {
                if (pc.getName().equals(detector)) {
                    return new DetectorWrapper(pc);
                }
            }
            return null;
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getPolicies</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PolicyCollection} object.
     */
    @GET
    @Path("{foreignSource}/policies")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public PolicyCollection getPolicies(@PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            return new PolicyCollection(getActiveForeignSource(foreignSource).getPolicies());
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getPolicy</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param policy a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper} object.
     */
    @GET
    @Path("{foreignSource}/policies/{policy}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public PolicyWrapper getPolicy(@PathParam("foreignSource") String foreignSource, @PathParam("policy") String policy) {
        readLock();
        try {
            for (final PluginConfig pc : getActiveForeignSource(foreignSource).getPolicies()) {
                if (pc.getName().equals(policy)) {
                    return new PolicyWrapper(pc);
                }
            }
            return null;
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>addForeignSource</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public Response addForeignSource(ForeignSource foreignSource) {
        writeLock();
        try {
            LOG.debug("addForeignSource: Adding foreignSource {}", foreignSource.getName());
            m_pendingForeignSourceRepository.save(foreignSource);
            return Response.seeOther(getRedirectUri(m_uriInfo, foreignSource.getName())).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>addDetector</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param detector a {@link org.opennms.netmgt.provision.persist.foreignsource.DetectorWrapper} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/detectors")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public Response addDetector(@PathParam("foreignSource") String foreignSource, DetectorWrapper detector) {
        writeLock();
        try {
            LOG.debug("addDetector: Adding detector {}", detector.getName());
            ForeignSource fs = getActiveForeignSource(foreignSource);
            fs.updateDateStamp();
            fs.addDetector(detector);
            m_pendingForeignSourceRepository.save(fs);
            return Response.seeOther(getRedirectUri(m_uriInfo, detector.getName())).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>addPolicy</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param policy a {@link org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/policies")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public Response addPolicy(@PathParam("foreignSource") String foreignSource, PolicyWrapper policy) {
        writeLock();
        try {
            LOG.debug("addPolicy: Adding policy {}", policy.getName());
            ForeignSource fs = getActiveForeignSource(foreignSource);
            fs.updateDateStamp();
            fs.addPolicy(policy);
            m_pendingForeignSourceRepository.save(fs);
            return Response.seeOther(getRedirectUri(m_uriInfo, policy.getName())).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>updateForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateForeignSource(@PathParam("foreignSource") String foreignSource, MultivaluedMapImpl params) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            LOG.debug("updateForeignSource: updating foreign source {}", foreignSource);
            
            if (params.isEmpty()) return Response.seeOther(getRedirectUri(m_uriInfo)).build();

            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(fs);
            wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    Object value = null;
                    String stringValue = params.getFirst(key);
                    value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            LOG.debug("updateForeignSource: foreign source {} updated", foreignSource);
            fs.updateDateStamp();
            m_pendingForeignSourceRepository.save(fs);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deletePendingForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}")
    @Transactional
    public Response deletePendingForeignSource(@PathParam("foreignSource") String foreignSource) {
        writeLock();
        try {
            ForeignSource fs = getForeignSource(foreignSource);
            LOG.debug("deletePendingForeignSource: deleting foreign source {}", foreignSource);
            m_pendingForeignSourceRepository.delete(fs);
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteDeployedForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("deployed/{foreignSource}")
    @Transactional
    public Response deleteDeployedForeignSource(@PathParam("foreignSource") String foreignSource) {
        writeLock();
        try {
            ForeignSource fs = getForeignSource(foreignSource);
            LOG.debug("deleteDeployedForeignSource: deleting foreign source {}", foreignSource);
            m_deployedForeignSourceRepository.delete(fs);
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteDetector</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param detector a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/detectors/{detector}")
    @Transactional
    public Response deleteDetector(@PathParam("foreignSource") String foreignSource, @PathParam("detector") String detector) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            List<PluginConfig> detectors = fs.getDetectors();
            PluginConfig removed = removeEntry(detectors, detector);
            if (removed != null) {
                fs.updateDateStamp();
                fs.setDetectors(detectors);
                m_pendingForeignSourceRepository.save(fs);
                return Response.ok().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deletePolicy</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param policy a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/policies/{policy}")
    @Transactional
    public Response deletePolicy(@PathParam("foreignSource") String foreignSource, @PathParam("policy") String policy) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            List<PluginConfig> policies = fs.getPolicies();
            PluginConfig removed = removeEntry(policies, policy);
            if (removed != null) {
                fs.updateDateStamp();
                fs.setPolicies(policies);
                m_pendingForeignSourceRepository.save(fs);
                return Response.ok().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    private PluginConfig removeEntry(List<PluginConfig> plugins, String name) {
        PluginConfig removed = null;
        java.util.Iterator<PluginConfig> i = plugins.iterator();
        while (i.hasNext()) {
            PluginConfig pc = i.next();
            if (pc.getName().equals(name)) {
                removed = pc;
                i.remove();
                break;
            }
        }
        return removed;
    }

    private Set<String> getActiveForeignSourceNames() {
        Set<String> fsNames = m_pendingForeignSourceRepository.getActiveForeignSourceNames();
        fsNames.addAll(m_deployedForeignSourceRepository.getActiveForeignSourceNames());
        return fsNames;
    }

    private ForeignSource getActiveForeignSource(String foreignSourceName) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSourceName);
        if (fs.isDefault()) {
            return m_deployedForeignSourceRepository.getForeignSource(foreignSourceName);
        }
        return fs;
    }

    
}
