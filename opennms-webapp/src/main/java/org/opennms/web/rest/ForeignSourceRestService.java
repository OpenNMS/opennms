//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.rest;

import java.text.ParseException;
import java.util.List;

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
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyCollection;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.OnmsForeignSourceCollection;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("foreignSources")
public class ForeignSourceRestService extends OnmsRestService {
    
    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository;
    
    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    /**
     * Returns all the foreign sources
     * 
     * @return Collection of OnmsForeignSources (ready to be XML-ified)
     * @throws ParseException
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional
    public OnmsForeignSourceCollection getForeignSources() throws ParseException {
        return new OnmsForeignSourceCollection(m_foreignSourceRepository.getForeignSources());
    }

    /**
     * Returns the requested {@link ForeignSource}
     * @param foreignSource the foreign source name
     * @return the foreign source
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}")
    @Transactional
    public ForeignSource getForeignSource(@PathParam("foreignSource") String foreignSource) {
        return m_foreignSourceRepository.getForeignSource(foreignSource);
    }

    /**
     * returns a plaintext string being the number of foreign sources
     * @return
     */

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_foreignSourceRepository.getForeignSourceCount());
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}/detectors")
    @Transactional
    public DetectorCollection getDetectors(@PathParam("foreignSource") String foreignSource) {
        return new DetectorCollection(m_foreignSourceRepository.getForeignSource(foreignSource).getDetectors());
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}/detectors/{detector}")
    @Transactional
    public DetectorWrapper getDetector(@PathParam("foreignSource") String foreignSource, @PathParam("detector") String detector) {
        for (PluginConfig pc : m_foreignSourceRepository.getForeignSource(foreignSource).getDetectors()) {
            if (pc.getName().equals(detector)) {
                return new DetectorWrapper(pc);
            }
        }
        return null;
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}/policies")
    @Transactional
    public PolicyCollection getPolicies(@PathParam("foreignSource") String foreignSource) {
        return new PolicyCollection(m_foreignSourceRepository.getForeignSource(foreignSource).getPolicies());
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{foreignSource}/policies/{policy}")
    @Transactional
    public PolicyWrapper getPolicy(@PathParam("foreignSource") String foreignSource, @PathParam("policy") String policy) {
        for (PluginConfig pc : m_foreignSourceRepository.getForeignSource(foreignSource).getPolicies()) {
            if (pc.getName().equals(policy)) {
                return new PolicyWrapper(pc);
            }
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addForeignSource(ForeignSource foreignSource) {
        log().debug("addForeignSource: Adding foreignSource " + foreignSource.getName());
        m_foreignSourceRepository.save(foreignSource);
        return Response.ok(foreignSource).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{foreignSource}/detectors")
    @Transactional
    public Response addDetector(@PathParam("foreignSource") String foreignSource, DetectorWrapper detector) {
        log().debug("addDetector: Adding detector " + detector.getName());
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        fs.addDetector(detector);
        m_foreignSourceRepository.save(fs);
        return Response.ok(detector).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{foreignSource}/policies")
    @Transactional
    public Response addPolicy(@PathParam("foreignSource") String foreignSource, PolicyWrapper policy) {
        log().debug("addPolicy: Adding policy " + policy.getName());
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        fs.addPolicy(policy);
        m_foreignSourceRepository.save(fs);
        return Response.ok(policy).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{foreignSource}")
    public Response updateForeignSource(@PathParam("foreignSource") String foreignSource, MultivaluedMapImpl params) {
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        log().debug("updateForeignSource: updating foreign source " + foreignSource);
        BeanWrapper wrapper = new BeanWrapperImpl(fs);
        wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                Object value = null;
                String stringValue = params.getFirst(key);
                value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateForeignSource: foreign source " + foreignSource + " updated");
        m_foreignSourceRepository.save(fs);
        return Response.ok(fs).build();
    }

    @DELETE
    @Path("{foreignSource}")
    public Response deleteForeignSource(@PathParam("foreignSource") String foreignSource) {
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        log().debug("deleteForeignSource: deleting foreign source " + foreignSource);
        m_foreignSourceRepository.delete(fs);
        return Response.ok(fs).build();
    }

    @DELETE
    @Path("{foreignSource}/detectors/{detector}")
    public Response deleteDetector(@PathParam("foreignSource") String foreignSource, @PathParam("detector") String detector) {
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        List<PluginConfig> detectors = fs.getDetectors();
        PluginConfig removed = removeEntry(detectors, detector);
        if (removed != null) {
            fs.setDetectors(detectors);
            m_foreignSourceRepository.save(fs);
            return Response.ok(removed).build();
        }
        return Response.notModified().build();
    }

    @DELETE
    @Path("{foreignSource}/policies/{policy}")
    public Response deletePolicy(@PathParam("foreignSource") String foreignSource, @PathParam("policy") String policy) {
        ForeignSource fs = m_foreignSourceRepository.getForeignSource(foreignSource);
        List<PluginConfig> policies = fs.getPolicies();
        PluginConfig removed = removeEntry(policies, policy);
        if (removed != null) {
            fs.setPolicies(policies);
            m_foreignSourceRepository.save(fs);
            return Response.ok(removed).build();
        }
        return Response.notModified().build();
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

}
