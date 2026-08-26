/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v1;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@Component("foreignSourceRestService")
@Path("foreignSources")
@Tag(name = "ForeignSources", description = """
        Foreign Sources API.

        A foreign source definition holds the scan interval, the service detectors and the provisioning
        policies applied to the nodes of the requisition with the same name. Definitions live in two
        repositories: *pending* (`etc/foreign-sources/pending`), written by `POST` and `PUT`, and *deployed*
        (`etc/foreign-sources`), written by provisiond when the matching requisition is imported.

        Reads resolve pending first and fall back to deployed. A name with no definition in either
        repository is **not** a 404: the repository synthesizes a copy of the default definition under the
        requested name, so `GET /foreignSources/{name}` answers 200 for any name. Compare the returned
        `detectors` and `policies` against `GET /foreignSources/default` if you need to know whether a
        definition of your own exists.

        `date-stamp` is epoch milliseconds in JSON and an ISO-8601 timestamp in XML.""")
public class ForeignSourceRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ForeignSourceRestService.class);

    
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;

    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    @GET
    @Path("default")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the default foreign source definition",
            description = """
                    The definition applied to any requisition that has no definition of its own. This is the
                    same document that a read of an unknown foreign source name returns, except for the `name`
                    field.""",
            operationId = "getDefaultForeignSource")
    @ApiResponse(responseCode = "200", description = "The default definition.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "default",
                                      "date-stamp": 1787727258567,
                                      "scan-interval": "1d",
                                      "detectors": [
                                        { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                        { "name": "SNMP", "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector", "parameter": [] }
                                      ],
                                      "policies": []
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    <foreign-source xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="default" date-stamp="2026-08-26T02:54:18.567-04:00">
                                      <scan-interval>1d</scan-interval>
                                      <detectors>
                                        <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                        <detector name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector"/>
                                      </detectors>
                                      <policies/>
                                    </foreign-source>"""))
            })
    public ForeignSource getDefaultForeignSource() {
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
     */
    @GET
    @Path("deployed")
    @Operation(
            summary = "List the deployed foreign source definitions",
            description = """
                    Only definitions in the deployed repository, that is, definitions provisiond has written
                    out because the matching requisition was imported. A definition that has been `POST`ed but
                    whose requisition has not been imported yet does not appear here; use `GET /foreignSources`
                    for the pending-and-deployed view.

                    The handler declares no `@Produces`, so the response media type is negotiated from the
                    `Accept` header. In JSON the list appears twice, once under `foreignSources` and once under
                    `foreign-source`, both holding the same objects.""",
            operationId = "getDeployedForeignSources")
    @ApiResponse(responseCode = "200", description = "Deployed definitions.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ForeignSourceCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "foreignSources": [
                                        {
                                          "name": "selfmonitor",
                                          "date-stamp": 1787727258593,
                                          "scan-interval": "1d",
                                          "detectors": [
                                            { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] }
                                          ],
                                          "policies": []
                                        }
                                      ],
                                      "count": 1,
                                      "foreign-source": [
                                        {
                                          "name": "selfmonitor",
                                          "date-stamp": 1787727258593,
                                          "scan-interval": "1d",
                                          "detectors": [
                                            { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] }
                                          ],
                                          "policies": []
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ForeignSourceCollection.class),
                            examples = @ExampleObject(value = """
                                    <foreign-sources xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" count="1">
                                      <foreign-source name="selfmonitor" date-stamp="2026-08-26T02:54:18.609-04:00">
                                        <scan-interval>1d</scan-interval>
                                        <detectors>
                                          <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                        </detectors>
                                        <policies/>
                                      </foreign-source>
                                    </foreign-sources>"""))
            })
    public ForeignSourceCollection getDeployedForeignSources() {
        readLock();
        try {
            m_deployedForeignSourceRepository.flush();
            ForeignSourceCollection retval = new ForeignSourceCollection();
            retval.getForeignSources().addAll(m_deployedForeignSourceRepository.getForeignSources());
            return retval;
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
    @Operation(
            summary = "Count the deployed foreign source definitions",
            description = "Plain-text decimal count of the definitions in the deployed repository. The default definition is not counted.",
            operationId = "getDeployedForeignSourceCount")
    @ApiResponse(responseCode = "200", description = "The count.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string", example = "1"),
                    examples = @ExampleObject(value = "1")))
    public String getDeployedCount() {
        readLock();
        try {
            m_deployedForeignSourceRepository.flush();
            return Integer.toString(m_deployedForeignSourceRepository.getForeignSourceCount());
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
    @Operation(
            summary = "List a definition for every active foreign source name",
            description = """
                    The union of the active names in the pending and deployed repositories, resolved one at a
                    time through the pending-then-deployed lookup. A name is active when a requisition or a
                    foreign source definition exists for it, so a requisition with no definition of its own
                    still produces an entry here: a copy of the default definition carrying that name. Deleting
                    a definition therefore does not remove the entry while the requisition still exists.

                    As with `/foreignSources/deployed`, the JSON body repeats the list under `foreignSources`
                    and `foreign-source`.""",
            operationId = "getForeignSources")
    @ApiResponse(responseCode = "200", description = "One definition per active foreign source name.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ForeignSourceCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "foreignSources": [
                                        {
                                          "name": "selfmonitor",
                                          "date-stamp": 1787727258535,
                                          "scan-interval": "1d",
                                          "detectors": [
                                            { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                            { "name": "SNMP", "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector", "parameter": [] }
                                          ],
                                          "policies": []
                                        }
                                      ],
                                      "count": 1,
                                      "foreign-source": [ { "name": "selfmonitor", "date-stamp": 1787727258535, "scan-interval": "1d", "detectors": [], "policies": [] } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ForeignSourceCollection.class),
                            examples = @ExampleObject(value = """
                                    <foreign-sources xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" count="1">
                                      <foreign-source name="selfmonitor" date-stamp="2026-08-26T02:54:18.535-04:00">
                                        <scan-interval>1d</scan-interval>
                                        <detectors>
                                          <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                        </detectors>
                                        <policies/>
                                      </foreign-source>
                                    </foreign-sources>"""))
            })
    public ForeignSourceCollection getForeignSources() {
        readLock();
        try {
            final Set<ForeignSource> foreignSources = new TreeSet<>();
            for (final String fsName : getActiveForeignSourceNames()) {
                foreignSources.add(getActiveForeignSource(fsName));
            }
            ForeignSourceCollection retval = new ForeignSourceCollection();
            retval.getForeignSources().addAll(foreignSources);
            return retval;
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
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Count the active foreign source names",
            description = """
                    Plain-text decimal size of the list `GET /foreignSources` returns, that is, the number of
                    distinct active names across the pending and deployed repositories. This counts names, not
                    stored definitions, so it includes names that only have a synthesized default definition.""",
            operationId = "getForeignSourceCount")
    @ApiResponse(responseCode = "200", description = "The count.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string", example = "1"),
                    examples = @ExampleObject(value = "1")))
    public String getTotalCount() {
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
    @Operation(
            summary = "Get a foreign source definition",
            description = """
                    Resolves the pending repository first and falls back to deployed. An unknown name answers
                    200 with a copy of the default definition renamed to the requested name, so a 200 here is
                    not evidence that a definition of your own exists.""",
            operationId = "getForeignSource")
    @ApiResponse(responseCode = "200", description = "The definition, or the default definition renamed, when the name has none of its own.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "selfmonitor",
                                      "date-stamp": 1787727462179,
                                      "scan-interval": "1d",
                                      "detectors": [
                                        { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                        { "name": "OpenNMS-JVM", "class": "org.opennms.netmgt.provision.detector.jmx.Jsr160Detector",
                                          "parameter": [ { "key": "port", "value": "18980" }, { "key": "protocol", "value": "rmi" } ] }
                                      ],
                                      "policies": []
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    <foreign-source xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="selfmonitor" date-stamp="2026-08-26T02:57:42.179-04:00">
                                      <scan-interval>1d</scan-interval>
                                      <detectors>
                                        <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                      </detectors>
                                      <policies/>
                                    </foreign-source>"""))
            })
    public ForeignSource getForeignSource(@Parameter(required = true, description = "Foreign source name.", example = "selfmonitor") @PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            final ForeignSource fs = getActiveForeignSource(foreignSource);
            if (fs == null) {
                throw getException(Status.NOT_FOUND, "Foreign source definition '{}' not found.", foreignSource);
            }
            return fs;
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
    @Operation(
            summary = "List the detectors on a foreign source definition",
            description = """
                    The `detectors` element of the resolved definition. An unknown foreign source name answers
                    200 with the default definition's detectors. In JSON the list is repeated under `detectors`
                    and `detector`.""",
            operationId = "getForeignSourceDetectors")
    @ApiResponse(responseCode = "200", description = "The detectors.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DetectorCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "detectors": [
                                        { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                        { "name": "SNMP", "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector", "parameter": [ { "key": "timeout", "value": "3000" } ] }
                                      ],
                                      "count": 2,
                                      "detector": [
                                        { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                        { "name": "SNMP", "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector", "parameter": [ { "key": "timeout", "value": "3000" } ] }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DetectorCollection.class),
                            examples = @ExampleObject(value = """
                                    <detectors xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" count="2">
                                      <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                      <detector name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector">
                                        <parameter key="timeout" value="3000"/>
                                      </detector>
                                    </detectors>"""))
            })
    public DetectorCollection getDetectors(@Parameter(required = true, description = "Foreign source name.", example = "selfmonitor") @PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            DetectorCollection retval = new DetectorCollection();
            retval.getDetectors().addAll(getActiveForeignSource(foreignSource).getDetectors());
            return retval;
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
    @Operation(
            summary = "Get one detector from a foreign source definition",
            description = """
                    Matched on the detector's `name`, not its class. The foreign source name itself cannot 404,
                    but a detector name that is absent from the resolved definition does.""",
            operationId = "getForeignSourceDetector")
    @ApiResponse(responseCode = "200", description = "The detector.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DetectorWrapper.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "SNMP",
                                      "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector",
                                      "parameter": [ { "key": "timeout", "value": "3000" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DetectorWrapper.class),
                            examples = @ExampleObject(value = """
                                    <detector xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector">
                                      <parameter key="timeout" value="3000"/>
                                    </detector>"""))
            })
    @ApiResponse(responseCode = "404", description = "No detector of that name on the resolved definition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Detector NoSuch on foreign source definition 'selfmonitor' not found.")))
    public DetectorWrapper getDetector(@Parameter(required = true, description = "Foreign source name.", example = "selfmonitor") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Detector name as it appears in the definition.", example = "SNMP") @PathParam("detector") String detector) {
        readLock();
        try {
            for (final PluginConfig pc : getActiveForeignSource(foreignSource).getDetectors()) {
                if (pc.getName().equals(detector)) {
                    return new DetectorWrapper(pc);
                }
            }
            throw getException(Status.NOT_FOUND, "Detector {} on foreign source definition '{}' not found.", detector, foreignSource);
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
    @Operation(
            summary = "List the policies on a foreign source definition",
            description = """
                    The `policies` element of the resolved definition. An unknown foreign source name answers
                    200 with the default definition's policies, which is normally an empty list. In JSON the
                    list is repeated under `policies` and `policy`.""",
            operationId = "getForeignSourcePolicies")
    @ApiResponse(responseCode = "200", description = "The policies.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = PolicyCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "policies": [
                                        {
                                          "name": "no-link-local",
                                          "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
                                          "parameter": [
                                            { "key": "action", "value": "DO_NOT_PERSIST" },
                                            { "key": "ipAddress", "value": "~^169\\\\.254\\\\..*" },
                                            { "key": "matchBehavior", "value": "ALL_PARAMETERS" }
                                          ]
                                        }
                                      ],
                                      "count": 1,
                                      "policy": [
                                        {
                                          "name": "no-link-local",
                                          "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
                                          "parameter": [ { "key": "action", "value": "DO_NOT_PERSIST" } ]
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = PolicyCollection.class),
                            examples = @ExampleObject(value = """
                                    <policies xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" count="1">
                                      <policy name="no-link-local" class="org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy">
                                        <parameter key="action" value="DO_NOT_PERSIST"/>
                                        <parameter key="matchBehavior" value="ALL_PARAMETERS"/>
                                      </policy>
                                    </policies>"""))
            })
    public PolicyCollection getPolicies(@Parameter(required = true, description = "Foreign source name.", example = "selfmonitor") @PathParam("foreignSource") String foreignSource) {
        readLock();
        try {
            PolicyCollection retval = new PolicyCollection();
            retval.getPolicies().addAll(getActiveForeignSource(foreignSource).getPolicies());
            return retval;
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
    @Operation(
            summary = "Get one policy from a foreign source definition",
            description = "Matched on the policy's `name`, not its class. A policy name absent from the resolved definition answers 404.",
            operationId = "getForeignSourcePolicy")
    @ApiResponse(responseCode = "200", description = "The policy.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = PolicyWrapper.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "no-link-local",
                                      "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
                                      "parameter": [
                                        { "key": "action", "value": "DO_NOT_PERSIST" },
                                        { "key": "ipAddress", "value": "~^169\\\\.254\\\\..*" },
                                        { "key": "matchBehavior", "value": "ALL_PARAMETERS" }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = PolicyWrapper.class),
                            examples = @ExampleObject(value = """
                                    <policy xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="no-link-local" class="org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy">
                                      <parameter key="action" value="DO_NOT_PERSIST"/>
                                      <parameter key="matchBehavior" value="ALL_PARAMETERS"/>
                                    </policy>"""))
            })
    @ApiResponse(responseCode = "404", description = "No policy of that name on the resolved definition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Policy NoSuch on foreign source definition 'selfmonitor' not found.")))
    public PolicyWrapper getPolicy(@Parameter(required = true, description = "Foreign source name.", example = "selfmonitor") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Policy name as it appears in the definition.", example = "no-link-local") @PathParam("policy") String policy) {
        readLock();
        try {
            for (final PluginConfig pc : getActiveForeignSource(foreignSource).getPolicies()) {
                if (pc.getName().equals(policy)) {
                    return new PolicyWrapper(pc);
                }
            }
            throw getException(Status.NOT_FOUND, "Policy {} on foreign source definition '{}' not found.", policy, foreignSource);
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
    @Operation(
            summary = "Add or replace a foreign source definition",
            description = """
                    Writes the whole definition to the pending repository under the `name` carried in the body;
                    the definition is replaced outright rather than merged. The definition takes effect for the
                    requisition of the same name at the next import, which is also when it is copied into the
                    deployed repository.

                    `date-stamp` in the body is ignored: the repository stamps the definition on save. The body
                    is not validated against the available plugin classes, so a detector or policy `class` that
                    cannot be loaded is accepted here and fails later during the scan.""",
            operationId = "addForeignSource")
    @RequestBody(required = true, description = "The complete foreign source definition.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "datacenter-east",
                                      "scan-interval": "1d",
                                      "detectors": [
                                        { "name": "ICMP", "class": "org.opennms.netmgt.provision.detector.icmp.IcmpDetector", "parameter": [] },
                                        { "name": "SNMP", "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector", "parameter": [ { "key": "timeout", "value": "3000" } ] }
                                      ],
                                      "policies": []
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ForeignSource.class),
                            examples = @ExampleObject(value = """
                                    <foreign-source xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="datacenter-east">
                                      <scan-interval>1d</scan-interval>
                                      <detectors>
                                        <detector name="ICMP" class="org.opennms.netmgt.provision.detector.icmp.IcmpDetector"/>
                                        <detector name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector">
                                          <parameter key="timeout" value="3000"/>
                                        </detector>
                                      </detectors>
                                      <policies/>
                                    </foreign-source>"""))
            })
    @ApiResponse(responseCode = "202", description = "Saved to the pending repository. No body; `Location` addresses the saved definition.",
            headers = @Header(name = "Location", description = "URI of the saved definition.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/foreignSources/datacenter-east")))
    public Response addForeignSource(@Context final UriInfo uriInfo, ForeignSource foreignSource) {
        writeLock();
        try {
            LOG.debug("addForeignSource: Adding foreignSource {}", foreignSource.getName());
            m_pendingForeignSourceRepository.save(foreignSource);
            return Response.accepted().header("Location", getRedirectUri(uriInfo, foreignSource.getName())).build();
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
    @Operation(
            summary = "Add a detector to a foreign source definition",
            description = """
                    Appends the detector to the resolved definition and saves the whole definition to the
                    pending repository. Because the resolved definition for an unknown name is a copy of the
                    default, calling this on a name that has no definition of its own creates one seeded with
                    every default detector plus the one posted.""",
            operationId = "addForeignSourceDetector")
    @RequestBody(required = true, description = "The detector to add.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DetectorWrapper.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "SNMP",
                                      "class": "org.opennms.netmgt.provision.detector.snmp.SnmpDetector",
                                      "parameter": [ { "key": "timeout", "value": "3000" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DetectorWrapper.class),
                            examples = @ExampleObject(value = """
                                    <detector xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="SNMP" class="org.opennms.netmgt.provision.detector.snmp.SnmpDetector">
                                      <parameter key="timeout" value="3000"/>
                                    </detector>"""))
            })
    @ApiResponse(responseCode = "202", description = "Saved. No body.",
            headers = @Header(name = "Location", description = "URI of the added detector.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/foreignSources/datacenter-east/detectors/SNMP")))
    public Response addDetector(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, DetectorWrapper detector) {
        writeLock();
        try {
            LOG.debug("addDetector: Adding detector {}", detector.getName());
            ForeignSource fs = getActiveForeignSource(foreignSource);
            fs.updateDateStamp();
            fs.addDetector(detector);
            m_pendingForeignSourceRepository.save(fs);
            return Response.accepted().header("Location", getRedirectUri(uriInfo, detector.getName())).build();
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
    @Operation(
            summary = "Add a policy to a foreign source definition",
            description = """
                    Appends the policy to the resolved definition and saves the whole definition to the pending
                    repository. As with adding a detector, doing this to a name that has no definition of its
                    own creates one seeded from the default definition.

                    `GET /foreignSourcesConfig/policies` lists the policy classes available and the parameter
                    keys and permitted values each accepts. The parameters are not validated here.""",
            operationId = "addForeignSourcePolicy")
    @RequestBody(required = true, description = "The policy to add.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = PolicyWrapper.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "no-link-local",
                                      "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
                                      "parameter": [
                                        { "key": "action", "value": "DO_NOT_PERSIST" },
                                        { "key": "matchBehavior", "value": "ALL_PARAMETERS" },
                                        { "key": "ipAddress", "value": "~^169\\\\.254\\\\..*" }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = PolicyWrapper.class),
                            examples = @ExampleObject(value = """
                                    <policy xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="no-link-local" class="org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy">
                                      <parameter key="action" value="DO_NOT_PERSIST"/>
                                      <parameter key="matchBehavior" value="ALL_PARAMETERS"/>
                                      <parameter key="ipAddress" value="~^169\\.254\\..*"/>
                                    </policy>"""))
            })
    @ApiResponse(responseCode = "202", description = "Saved. No body.",
            headers = @Header(name = "Location", description = "URI of the added policy.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/foreignSources/datacenter-east/policies/no-link-local")))
    public Response addPolicy(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, PolicyWrapper policy) {
        writeLock();
        try {
            LOG.debug("addPolicy: Adding policy {}", policy.getName());
            ForeignSource fs = getActiveForeignSource(foreignSource);
            fs.updateDateStamp();
            fs.addPolicy(policy);
            m_pendingForeignSourceRepository.save(fs);
            return Response.accepted().header("Location", getRedirectUri(uriInfo, policy.getName())).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>updateForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Update scalar fields of a foreign source definition",
            description = """
                    Form-encoded key/value pairs, applied to the resolved definition and saved to the pending
                    repository. Keys are matched against **bean property names**, not the hyphenated XML
                    attribute names: `scanInterval=2d` is applied, `scan-interval=2d` is not. Unrecognized keys
                    are silently ignored.

                    If no key matched a writable property, or the body was empty, nothing is saved and the
                    response is 304 with no body. Detectors and policies are collections and are not settable
                    this way; use the `/detectors` and `/policies` sub-resources.""",
            operationId = "updateForeignSource")
    @RequestBody(required = true, description = "Form-encoded bean property names and values.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = MultivaluedMapImpl.class),
                    examples = @ExampleObject(value = "scanInterval=2d")))
    @ApiResponse(responseCode = "202", description = "At least one property was written and the definition was saved. No body.",
            headers = @Header(name = "Location", description = "URI of the updated definition.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/foreignSources/datacenter-east")))
    @ApiResponse(responseCode = "304", description = "Empty body, or no key matched a writable property. Nothing was saved.")
    public Response updateForeignSource(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, MultivaluedMapImpl params) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            LOG.debug("updateForeignSource: updating foreign source {}", foreignSource);
            
            if (params.isEmpty()) return Response.notModified().build();

            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(fs);
            wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    Object value = null;
                    String stringValue = params.getFirst(key);
                    value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                LOG.debug("updateForeignSource: foreign source {} updated", foreignSource);
                fs.updateDateStamp();
                m_pendingForeignSourceRepository.save(fs);
                return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
            } else {
                return Response.notModified().build();
            }
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
    @Operation(
            summary = "Delete a foreign source definition from the pending repository",
            description = """
                    Removes the pending definition file only; the deployed copy, if any, survives. A name with
                    no pending definition is not reported as an error, the response is still 202.

                    Deleting the definition does not remove the name from `GET /foreignSources` while a
                    requisition of that name still exists: reads then fall back to a synthesized copy of the
                    default definition.""",
            operationId = "deletePendingForeignSource")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not a pending definition existed.")
    public Response deletePendingForeignSource(@Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        writeLock();
        try {
            ForeignSource fs = getForeignSource(foreignSource);
            LOG.debug("deletePendingForeignSource: deleting foreign source {}", foreignSource);
            m_pendingForeignSourceRepository.delete(fs);
            return Response.accepted().build();
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
    @Operation(
            summary = "Delete a foreign source definition from the deployed repository",
            description = """
                    Removes the deployed definition file only; the pending copy, if any, survives. The name
                    `default` is treated specially: rather than being deleted, the default definition is reset
                    to the built-in one.

                    A name with no deployed definition is not reported as an error, the response is still 202.
                    Nodes already provisioned from the matching requisition are untouched; the definition is
                    only consulted during a scan.""",
            operationId = "deleteDeployedForeignSource")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not a deployed definition existed.")
    public Response deleteDeployedForeignSource(@Parameter(required = true, description = "Foreign source name. `default` resets the built-in default definition instead of deleting it.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        writeLock();
        try {
            ForeignSource fs = getForeignSource(foreignSource);
            LOG.debug("deleteDeployedForeignSource: deleting foreign source {}", foreignSource);
            if ("default".equals(foreignSource)) {
                m_deployedForeignSourceRepository.resetDefaultForeignSource();
            } else {
                m_deployedForeignSourceRepository.delete(fs);
            }
            return Response.accepted().build();
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
    @Operation(
            summary = "Remove a detector from a foreign source definition",
            description = """
                    Removes the first detector whose `name` matches and saves the definition to the pending
                    repository. If no detector matched, nothing is saved and the response is 304 with no
                    body.""",
            operationId = "deleteForeignSourceDetector")
    @ApiResponse(responseCode = "202", description = "The detector was removed and the definition saved. No body.")
    @ApiResponse(responseCode = "304", description = "No detector of that name on the resolved definition. Nothing was saved.")
    public Response deleteDetector(@Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Detector name as it appears in the definition.", example = "SNMP") @PathParam("detector") final String detector) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            List<PluginConfig> detectors = fs.getDetectors();
            PluginConfig removed = removeEntry(detectors, detector);
            if (removed != null) {
                fs.updateDateStamp();
                fs.setDetectors(detectors);
                m_pendingForeignSourceRepository.save(fs);
                return Response.accepted().build();
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
    @Operation(
            summary = "Remove a policy from a foreign source definition",
            description = """
                    Removes the first policy whose `name` matches and saves the definition to the pending
                    repository. If no policy matched, nothing is saved and the response is 304 with no body.""",
            operationId = "deleteForeignSourcePolicy")
    @ApiResponse(responseCode = "202", description = "The policy was removed and the definition saved. No body.")
    @ApiResponse(responseCode = "304", description = "No policy of that name on the resolved definition. Nothing was saved.")
    public Response deletePolicy(@Parameter(required = true, description = "Foreign source name.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Policy name as it appears in the definition.", example = "no-link-local") @PathParam("policy") final String policy) {
        writeLock();
        try {
            ForeignSource fs = getActiveForeignSource(foreignSource);
            List<PluginConfig> policies = fs.getPolicies();
            PluginConfig removed = removeEntry(policies, policy);
            if (removed != null) {
                fs.updateDateStamp();
                fs.setPolicies(policies);
                m_pendingForeignSourceRepository.save(fs);
                return Response.accepted().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    private static PluginConfig removeEntry(List<PluginConfig> plugins, String name) {
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

    private ForeignSource getActiveForeignSource(final String foreignSourceName) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSourceName);
        if (fs.isDefault()) {
            return m_deployedForeignSourceRepository.getForeignSource(foreignSourceName);
        }
        return fs;
    }

}
