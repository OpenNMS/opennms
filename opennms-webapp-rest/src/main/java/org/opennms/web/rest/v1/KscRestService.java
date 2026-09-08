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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.web.svclayer.api.KscReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("kscRestService")
@Path("ksc")
@Tag(name = "Ksc", description = """
        KSC (Key SNMP Customized) report API, backed by `ksc-performance-reports.xml`.

        A KSC report is a numbered collection of graph definitions. The id is chosen by the caller, not
        assigned by the server, and every other operation addresses the report by it.

        `timespan` on a graph is validated against a fixed list and silently replaced with `7_day` when it
        does not match, so an unrecognised timespan is accepted rather than rejected.

        Writes go straight to `ksc-performance-reports.xml`. There is no delete operation here: removing a
        report means editing that file and calling `PUT /ksc/reloadConfig`.""")
public class KscRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(KscRestService.class);

    @Autowired
    private KscReportService m_kscReportService;

    @Autowired
    private KSC_PerformanceReportFactory m_kscReportFactory;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "List KSC reports",
            description = """
        List every KSC report in `ksc-performance-reports.xml`.

        This listing is terse: only `id` and `label` are populated. `show_timespan_button`,
        `show_graphtype_button` and `graphs_per_line` come back null and `kscGraph` comes back empty even
        for reports that set them.""",
            operationId = "getKscReports"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The KSC reports, in terse form.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = KscReportCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "kscReport": [
                        {
                          "id": 8100,
                          "label": "Response time overview",
                          "show_timespan_button": null,
                          "show_graphtype_button": null,
                          "graphs_per_line": null,
                          "kscGraph": []
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = KscReportCollection.class))
                    })
    })
    public KscReportCollection getReports() throws ParseException {
        final KscReportCollection reports = new KscReportCollection(m_kscReportService.getReportMap(), true);
        reports.setTotalCount(reports.size());
        return reports;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{reportId}")
    @Transactional
    @Operation(
            summary = "Get one KSC report",
            description = """
        Return a single KSC report with all of its graph definitions.

        `show_timespan_button`, `show_graphtype_button`, `graphs_per_line` and the `kscGraph` array are all
        populated. `graphs_per_line` is 0 when the report does not set it.""",
            operationId = "getKscReport"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The KSC report.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = KscReport.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 8100,
                      "label": "Response time overview",
                      "show_timespan_button": true,
                      "show_graphtype_button": true,
                      "graphs_per_line": 2,
                      "kscGraph": [
                        {
                          "title": "Response time, loopback-001",
                          "timespan": "7_day",
                          "graphtype": "http-8080",
                          "resourceId": "node[loopback-lab:lb-001].responseTime[127.0.0.1]",
                          "nodeId": null,
                          "nodeSource": null,
                          "domain": null,
                          "interfaceId": null,
                          "extlink": null
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = KscReport.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No report with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No such report id 8100.")))
    })
    public KscReport getReport(
            @Parameter(description = "Report id, as assigned when the report was created.", required = true, example = "8100")
            @PathParam("reportId") final Integer reportId) {
        final Map<Integer, Report> reportList = m_kscReportService.getReportMap();
        final Report report = reportList.get(reportId);
        if (report == null) {
            throw getException(Status.NOT_FOUND, "No such report id {}.", Integer.toString(reportId));
        }
        return new KscReport(report);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    @Operation(
            summary = "Count KSC reports",
            description = """
        Return the number of KSC reports as a plain-text integer.

        This operation only produces `text/plain`. A request with `Accept: application/json` does not match
        it and falls through to `GET /ksc/{reportId}`, which then answers 404 for the literal id `count`.""",
            operationId = "getKscReportCount"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The number of reports.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "1")))
    })
    public String getCount() {
        return Integer.toString(m_kscReportService.getReportList().size());
    }

    @PUT
    @Path("reloadConfig")
    @Transactional
    @Operation(
            summary = "Reload the KSC report configuration",
            description = """
        Re-read `ksc-performance-reports.xml` from disk and replace the in-memory report set with what it
        contains.""",
            operationId = "reloadKscConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was reloaded."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be reloaded, for instance because the file no longer parses.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response reloadConfiguration() {
        writeLock();
        try {
            KSC_PerformanceReportFactory.getInstance().reload();
            return Response.noContent().build();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{kscReportId}")
    @Transactional
    @Operation(
            summary = "Add a graph to a KSC report",
            description = """
        Append one graph to an existing KSC report. The graph is described entirely by query parameters;
        this operation takes no body. The report is written back to `ksc-performance-reports.xml`
        immediately.

        `reportName` is a prefabricated graph name (see `GET /graphs`) and `resourceId` is a resource id
        (see `GET /resources`). Both are required and neither is checked for existence, so a graph naming a
        resource that does not exist is stored.

        Graphs are only appended; there is no operation to replace or remove one.""",
            operationId = "addGraphToKscReport"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The graph was appended and the configuration saved."),
            @ApiResponse(responseCode = "400", description = "`reportName` or `resourceId` was missing. The empty-string check compares by reference, so an empty parameter is not reliably rejected.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid request: reportName and resourceId cannot be empty!"))),
            @ApiResponse(responseCode = "404", description = "No report with that id exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid request: No KSC report found with ID: 99999."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written back.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot save report with Id 8100 : <cause>")))
    })
    public Response addGraph(
            @Parameter(description = "Report id to append to.", required = true, example = "8100")
            @PathParam("kscReportId") final Integer kscReportId,
            @Parameter(description = "Title shown above the graph. Optional; the graph is stored without a title when omitted.",
                    example = "Response time, loopback-001")
            @QueryParam("title") final String title,
            @Parameter(description = "Name of the prefabricated graph to render, as listed by `GET /graphs`.",
                    required = true, example = "http-8080")
            @QueryParam("reportName") final String reportName,
            @Parameter(description = "Resource id the graph is drawn from, as listed by `GET /resources`.",
                    required = true, example = "node[loopback-lab:lb-001].responseTime[127.0.0.1]")
            @QueryParam("resourceId") final String resourceId,
            @Parameter(description = "Time window for the graph. An unrecognised value is replaced with `7_day` without an error.",
                    example = "7_day",
                    schema = @Schema(allowableValues = {"1_hour", "2_hour", "4_hour", "6_hour", "8_hour", "12_hour", "1_day", "2_day", "7_day", "1_month", "3_month", "6_month", "1_year", "Today", "Yesterday", "Yesterday 9am-5pm", "Yesterday 5pm-10pm", "This Week", "Last Week", "This Month", "Last Month", "This Quarter", "Last Quarter", "This Year", "Last Year"}))
            @QueryParam("timespan") String timespan) {
        writeLock();

        try {
            if (kscReportId == null || reportName == null || reportName == "" || resourceId == null || resourceId == "") {
                throw getException(Status.BAD_REQUEST, "Invalid request: reportName and resourceId cannot be empty!");
            }
            final Report report = m_kscReportFactory.getReportByIndex(kscReportId);
            if (report == null) {
                throw getException(Status.NOT_FOUND, "Invalid request: No KSC report found with ID: {}.", Integer.toString(kscReportId));
            }
            final Graph graph = new Graph();
            if (title != null) {
                graph.setTitle(title);
            }

            boolean found = false;
            for (final String valid : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
                if (valid.equals(timespan)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.debug("invalid timespan ('{}'), setting to '7_day' instead.", timespan);
                timespan = "7_day";
            }

            graph.setGraphtype(reportName);
            graph.setResourceId(resourceId);
            graph.setTimespan(timespan);
            report.addGraph(graph);
            m_kscReportFactory.setReport(kscReportId, report);
            try {
                m_kscReportFactory.saveCurrent();
            } catch (final Exception e) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot save report with Id {} : {} ", kscReportId.toString(), e.getMessage());
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            summary = "Create a KSC report",
            description = """
        Create a KSC report and write it to `ksc-performance-reports.xml`. The report id is taken from the
        body, not assigned by the server, and an id that is already in use is rejected with 409.

        This operation consumes XML only; a JSON body is rejected by the container with 415. The body maps
        onto XML attributes rather than elements, and the field names are underscored
        (`show_timespan_button`, `graphs_per_line`).

        `Location` on the 201 points at `GET /ksc/{reportId}` for the new report. Graphs may be supplied
        inline as `kscGraph` elements, or added afterwards with `PUT /ksc/{kscReportId}`.""",
            operationId = "addKscReport"
    )
    @RequestBody(
            required = true,
            description = "The report to create, including any graphs it starts with.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = KscReport.class),
                    examples = @ExampleObject(value = """
                    <kscReport id="8100" label="Response time overview"
                               show_timespan_button="true" show_graphtype_button="true" graphs_per_line="2">
                      <kscGraph title="Response time, loopback-001"
                                timespan="7_day"
                                graphtype="http-8080"
                                resourceId="node[loopback-lab:lb-001].responseTime[127.0.0.1]"/>
                    </kscReport>"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The report was created. `Location` carries its URI."),
            @ApiResponse(responseCode = "400", description = "The configuration could not be written back.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A report with that id already exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid request: Existing KSC report found with ID: 8100."))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/xml`, or no `Content-Type` was sent. The response has no body.")
    })
    public Response addKscReport(@Context final UriInfo uriInfo, final KscReport kscReport) {
        writeLock();
        try {
            LOG.debug("addKscReport: Adding KSC Report {}", kscReport);
            Report report = m_kscReportFactory.getReportByIndex(kscReport.getId());
            if (report != null) {
                throw getException(Status.CONFLICT, "Invalid request: Existing KSC report found with ID: {}.", Integer.toString(kscReport.getId()));
            }
            report = new Report();
            report.setId(kscReport.getId());
            report.setTitle(kscReport.getLabel());
            if (kscReport.getShowGraphtypeButton() != null) {
                report.setShowGraphtypeButton(kscReport.getShowGraphtypeButton());
            }
            if (kscReport.getShowTimespanButton() != null) {
                report.setShowTimespanButton(kscReport.getShowTimespanButton());
            }
            if (kscReport.getGraphsPerLine() != null) {
                report.setGraphsPerLine(kscReport.getGraphsPerLine());
            }
            if (kscReport.hasGraphs()) {
                for (KscGraph kscGraph : kscReport.getGraphs()) {
                    final Graph graph = kscGraph.buildGraph();
                    report.addGraph(graph);
                }
            }

            m_kscReportFactory.addReport(report);
            try {
                m_kscReportFactory.saveCurrent();
            } catch (final Exception e) {
                throw getException(Status.BAD_REQUEST, e.getMessage());
            }
            return Response.created(getRedirectUri(uriInfo, kscReport.getId())).build();
        } finally {
            writeUnlock();
        }
    }

    @XmlRootElement(name = "kscReports")
    public static final class KscReportCollection extends JaxbListWrapper<KscReport> {

        private static final long serialVersionUID = 1L;

        public KscReportCollection() {
            super();
        }

        public KscReportCollection(Collection<? extends KscReport> reports) {
            super(reports);
        }

        public KscReportCollection(final Map<Integer, Report> reportList, boolean terse) {
            super();
            for (final Report report : reportList.values()) {
                if (terse) {
                    add(new KscReport(report.getId(), report.getTitle()));
                } else {
                    add(new KscReport(report));
                }
            }
        }

        @XmlElement(name = "kscReport")
        public List<KscReport> getObjects() {
            return super.getObjects();
        }
    }

    @XmlRootElement(name = "kscReport")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class KscReport {

        @Schema(description = "Report id. Chosen by the caller when the report is created.", example = "8100", required = true)
        @XmlAttribute(name = "id", required = true)
        private Integer m_id;

        @Schema(description = "Report title, shown in the KSC report list.", example = "Response time overview", required = true)
        @XmlAttribute(name = "label", required = true)
        private String m_label;

        @Schema(description = "Whether the report page offers a timespan selector. Null in the terse listing.", example = "true")
        @XmlAttribute(name = "show_timespan_button", required = false)
        private Boolean m_show_timespan_button;

        @Schema(description = "Whether the report page offers a graph-type selector. Null in the terse listing.", example = "true")
        @XmlAttribute(name = "show_graphtype_button", required = false)
        private Boolean m_show_graphtype_button;

        @Schema(description = "Graphs to lay out per row. 0 when the report does not set it, null in the terse listing.", example = "2")
        @XmlAttribute(name = "graphs_per_line", required = false)
        private Integer m_graphs_per_line;

        @Schema(description = "Graph definitions in the report. Always empty in the terse listing.")
        @XmlElement(name = "kscGraph")
        private List<KscGraph> m_graphs = new ArrayList<>();

        public KscReport() {
        }

        public KscReport(final Integer reportId, final String label) {
            m_id = reportId;
            m_label = label;
        }

        public KscReport(Report report) {
            m_id = report.getId();
            m_label = report.getTitle();
            m_show_timespan_button = report.getShowTimespanButton().orElse(null);
            m_show_graphtype_button = report.getShowGraphtypeButton().orElse(null);
            m_graphs_per_line = report.getGraphsPerLine().orElse(0);
            m_graphs.clear();

            for(Graph graph : report.getGraphs()) {
                m_graphs.add(new KscGraph(graph));
            }
        }

        public Integer getId() {
            return m_id;
        }

        public void setId(final Integer id) {
            m_id = id;
        }

        public String getLabel() {
            return m_label;
        }

        public void setLabel(final String label) {
            m_label = label;
        }

        public Boolean getShowTimespanButton() {
            return m_show_timespan_button;
        }

        public void setShowTimespanButton(final Boolean show) {
            m_show_timespan_button = show;
        }

        public Boolean getShowGraphtypeButton() {
            return m_show_graphtype_button;
        }

        public void setShowGraphtypeButton(final Boolean show) {
            m_show_graphtype_button = show;
        }

        public Integer getGraphsPerLine() {
            return m_graphs_per_line;
        }

        public void setGraphsPerLine(final Integer graphs) {
            m_graphs_per_line = graphs;
        }

        public boolean hasGraphs() {
            return !m_graphs.isEmpty();
        }

        public List<KscGraph> getGraphs() {
            return m_graphs;
        }
    }

    @XmlRootElement(name = "kscGraph")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class KscGraph {

        @Schema(description = "Title shown above the graph.", example = "Response time, loopback-001")
        @XmlAttribute(name = "title", required = true)
        private String m_title;

        @Schema(description = "Time window for the graph. An unrecognised value is stored as `7_day`.", example = "7_day",
                allowableValues = {"1_hour", "2_hour", "4_hour", "6_hour", "8_hour", "12_hour", "1_day", "2_day", "7_day", "1_month", "3_month", "6_month", "1_year", "Today", "Yesterday", "Yesterday 9am-5pm", "Yesterday 5pm-10pm", "This Week", "Last Week", "This Month", "Last Month", "This Quarter", "Last Quarter", "This Year", "Last Year"})
        @XmlAttribute(name = "timespan", required = true)
        private String m_timespan;

        @Schema(description = "Prefabricated graph name, as listed by `GET /graphs`.", example = "http-8080", required = true)
        @XmlAttribute(name = "graphtype", required = true)
        private String m_graphtype;

        @Schema(description = "Resource id the graph is drawn from, as listed by `GET /resources`.",
                example = "node[loopback-lab:lb-001].responseTime[127.0.0.1]")
        @XmlAttribute(name = "resourceId", required = false)
        private String m_resourceId;

        @Schema(description = "Legacy way of addressing a node graph, superseded by `resourceId`.", example = "1")
        @XmlAttribute(name = "nodeId", required = false)
        private String m_nodeId;

        @Schema(description = "Legacy foreignSource:foreignId form of `nodeId`.", example = "loopback-lab:lb-001")
        @XmlAttribute(name = "nodeSource", required = false)
        private String m_nodeSource;

        @Schema(description = "Legacy domain name for a domain-scoped graph, superseded by `resourceId`.", example = "example.com")
        @XmlAttribute(name = "domain", required = false)
        private String m_domain;

        @Schema(description = "Legacy interface identifier for an interface-scoped graph, superseded by `resourceId`.", example = "127.0.0.1")
        @XmlAttribute(name = "interfaceId", required = false)
        private String m_interfaceId;

        @Schema(description = "URL the graph links out to when clicked.", example = "https://example.org/dashboard")
        @XmlAttribute(name = "extlink", required = false)
        private String m_extlink;

        public KscGraph() {

        }

        public KscGraph(Graph graph) {
            m_title = graph.getTitle();
            m_timespan = graph.getTimespan();
            m_graphtype = graph.getGraphtype();
            m_resourceId = graph.getResourceId().orElse(null);
            m_nodeId = graph.getNodeId().orElse(null);
            m_nodeSource = graph.getNodeSource().orElse(null);
            m_domain = graph.getDomain().orElse(null);
            m_interfaceId = graph.getInterfaceId().orElse(null);
            m_extlink = graph.getExtlink().orElse(null);
        }

        public Graph buildGraph() {
            boolean found = false;
            for (final String valid : KSC_PerformanceReportFactory.TIMESPAN_OPTIONS) {
                if (valid.equals(m_timespan)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.debug("invalid timespan ('{}'), setting to '7_day' instead.", m_timespan);
                m_timespan = "7_day";
            }

            final Graph graph = new Graph();
            graph.setTitle(m_title);
            graph.setTimespan(m_timespan);
            graph.setGraphtype(m_graphtype);
            graph.setResourceId(m_resourceId);
            graph.setNodeId(m_nodeId);
            graph.setNodeSource(m_nodeSource);
            graph.setDomain(m_domain);
            graph.setInterfaceId(m_interfaceId);
            graph.setExtlink(m_extlink);

            return graph;
        }
    }
}
