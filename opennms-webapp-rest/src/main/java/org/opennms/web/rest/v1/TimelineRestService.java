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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("timelineRestService")
@Path("timeline")
@Tag(name = "Timeline", description = """
        Server-rendered outage strips.

        Every operation takes `start`, `end` and `width` as path segments. **`start` and `end` are epoch
        seconds, not milliseconds**: passing milliseconds produces a strip covering the year 58000 with no
        drawn outages.

        `width` is the pixel width of the image; the height is always 20. `width` also divides the time
        range, so it must be at least 1 and the range must be at least `width` seconds wide. Passing 0, or a
        range narrower than the width, fails with 500 on the division.

        Three of the four operations return a PNG. `html` returns an `<img>` element plus the client-side
        image map of the outage rectangles.

        Query parameters other than the path segments are applied as extra criteria on the outage query, the
        same way as elsewhere in v1.""")
public class TimelineRestService extends OnmsRestService {

    private static class TimescaleDescriptor {
        /**
         * The divisor to use for calculating the number of labels
         */
        private int m_divisor;
        /**
         * The calendar field type to be used
         */
        private int m_type;
        /**
         * The calendar field types to be zeroed
         */
        private int[] m_typesToZero;
        /**
         * The increment for the calendar field
         */
        private int m_increment;
        /**
         * The date format to be used
         */
        private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        private static final SimpleDateFormat SIMPLE_TIME_FORMAT = new SimpleDateFormat("HH:mm");

        /**
         * Red color
         */
        private static final Color ONMS_RED = Color.decode("#e35d5b");
        /**
         * Green color
         */
        private static final Color ONMS_GREEN = Color.decode("#99bf73");
        /**
         * Green color
         */
        private static final Color ONMS_GRAY = Color.decode("#999999");

        /**
         * Constructor for an instance.
         *
         * @param divisor     the divisor to be used
         * @param type        the calendar field type
         * @param increment   the increment for the calendar field type
         * @param typesToZero the calendar field types to be zeroed
         */
        public TimescaleDescriptor(int divisor, int type, int increment, int... typesToZero) {
            this.m_divisor = divisor;
            this.m_type = type;
            this.m_increment = increment;
            this.m_typesToZero = typesToZero;
        }

        public int getDivisor() {
            return m_divisor;
        }

        public int getType() {
            return m_type;
        }

        public int[] getTypesToZero() {
            return m_typesToZero;
        }

        public int getIncrement() {
            return m_increment;
        }

        /**
         * Checks whether this instance matches the required number of labels.
         *
         * @param delta the delta used
         * @param num   the number of labels
         * @return true, if matches, false otherwise
         */
        public boolean match(long delta, int num) {
            return (delta / m_divisor < num);
        }

        /**
         * Draws the header an a given graphics context.
         *
         * @param graphics2D the graphics context to be used
         * @param delta      the delta
         * @param start      the start value
         * @param width      the width of the header
         */
        public void drawHeader(Graphics2D graphics2D, long delta, long start, int width) {
            Calendar calendar = GregorianCalendar.getInstance();

            calendar.setTimeInMillis(start * 1000);

            for (int typeToZero : getTypesToZero()) {
                calendar.set(typeToZero, calendar.getActualMinimum(typeToZero));
            }

            calendar.add(getType(), -getIncrement());

            graphics2D.setColor(Color.BLACK);

            while (calendar.getTimeInMillis() / 1000 - getDivisor() < (start + delta)) {
                int n = (int) ((calendar.getTimeInMillis() / 1000 - start) / (delta / width));
                graphics2D.setColor(ONMS_GRAY);
                graphics2D.drawLine(n, 16, n, 19);
                graphics2D.drawLine(n, 0, n, 4);
                String d;
                if (getDivisor() <= 3600 * 24) {
                    d = SIMPLE_TIME_FORMAT.format(calendar.getTime());
                } else {
                    d = SIMPLE_DATE_FORMAT.format(calendar.getTime());
                }
                graphics2D.setColor(Color.BLACK);
                graphics2D.drawString(d, n - graphics2D.getFontMetrics().stringWidth(d) / 2, 15);
                calendar.add(getType(), getIncrement());
            }
        }

        /**
         * Draws vertical lines on a given graphics context.
         *
         * @param graphics2D the graphics context
         * @param delta      the delta
         * @param start      the start value
         * @param width      the width of the graphic
         */
        public void drawLine(Graphics2D graphics2D, long delta, long start, int width) {
            Calendar calendar = GregorianCalendar.getInstance();

            calendar.setTimeInMillis(start * 1000);

            for (int typeToZero : getTypesToZero()) {
                calendar.set(typeToZero, calendar.getActualMinimum(typeToZero));
            }

            calendar.add(getType(), -getIncrement());

            graphics2D.setColor(ONMS_GRAY);

            while (calendar.getTimeInMillis() / 1000 - getDivisor() < (start + delta)) {
                int n = (int) ((calendar.getTimeInMillis() / 1000 - start) / (delta / width));
                graphics2D.drawLine(n, 0, n, 19);
                calendar.add(getType(), getIncrement());
            }
        }

        /**
         * Draws an outage on a given graphics context.
         *
         * @param graphics2D the graphics context
         * @param timeDelta  the amount of time between the start and end of the graphic
         * @param startTime  the time at the start of the graphic
         * @param width      the width of the graphic
         * @param onmsOutage the outage to be drawn
         * @return true, if no resolved yet
         */
        public boolean drawOutage(Graphics2D graphics2D, long timeDelta, long startTime, int width, OnmsOutage onmsOutage) {
            long outageStartTime = onmsOutage.getIfLostService().getTime() / 1000;
            long outageEndTime = startTime + timeDelta;

            if (onmsOutage.getIfRegainedService() != null) {
                outageEndTime = onmsOutage.getIfRegainedService().getTime() / 1000;
            }

            graphics2D.setColor(ONMS_RED);
            int graphicStart = (int) ((outageStartTime - startTime) / (timeDelta / width));
            int graphicEnd = (int) ((outageEndTime - startTime) / (timeDelta / width));
            graphics2D.fillRect(graphicStart, 2, (graphicEnd - graphicStart > 0 ? graphicEnd - graphicStart : 1), 16);

            return onmsOutage.getIfRegainedService() == null;
        }

        /**
         * Draws a solid green bar for a node on a given graphics context, beginning at the createTime of the node.
         *
         * @param graphics2D the graphics context
         * @param timeDelta  the amount of time between the start and end of the graphic
         * @param startTime  the time at the start of the graphic
         * @param width      the width of the graphic
         * @param node       the node to be drawn
         */
        public void drawNode(Graphics2D graphics2D, long timeDelta, long startTime, int width, OnmsNode node) {
            long nodeCreateTime = node.getCreateTime().getTime() / 1000;

            if (nodeCreateTime < startTime) {
                nodeCreateTime = startTime;
            }

            graphics2D.setColor(ONMS_GREEN);
            int graphicStart = (int) ((nodeCreateTime - startTime) / (timeDelta / width));
            graphics2D.fillRect(graphicStart, 2, width - graphicStart, 16);
        }

        /**
         * Computes the number of labels to be used for the timeline
         *
         * @param graphics2D the graphics context
         * @param delta      the delta
         * @param width      the width of the timeline header
         * @return the number of labels
         */
        public static int computeNumberOfLabels(Graphics2D graphics2D, long delta, int width) {
            if (delta <= 3600 * 24) {
                return width / graphics2D.getFontMetrics().stringWidth("XX:XX");
            } else {
                return width / graphics2D.getFontMetrics().stringWidth("XXXX-XX-XX XX:XX");
            }
        }

        /**
         * Returns the HTML map entry for a given outage instance.
         *
         * @param graphics2D the graphics context
         * @param timeDelta  the amount of time between the start and end of the graphic
         * @param startTime  the time at the start of the graphic
         * @param width      the width of the graphic
         * @param onmsOutage the outage to be used
         * @return the HTML map entry
         */
        public String getMapEntry(Graphics2D graphics2D, long timeDelta, long startTime, int width, OnmsOutage onmsOutage) {
            long outageStartTime = onmsOutage.getIfLostService().getTime() / 1000;
            long outageEndTime = startTime + timeDelta;

            if (onmsOutage.getIfRegainedService() != null) {
                outageEndTime = onmsOutage.getIfRegainedService().getTime() / 1000;
            }

            graphics2D.setColor(ONMS_RED);
            int graphicStart = (int) ((outageStartTime - startTime) / (timeDelta / width));
            int graphicEnd = (int) ((outageEndTime - startTime) / (timeDelta / width));
            final StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("<area shape=\"rect\" coords=\"");
            stringBuffer.append(graphicStart);
            stringBuffer.append(",2,");
            stringBuffer.append(graphicEnd);
            stringBuffer.append(",18\" ");
            stringBuffer.append("href=\"/opennms/outage/detail.htm?id=");
            stringBuffer.append(onmsOutage.getId());
            stringBuffer.append("\" alt=\"Id " + onmsOutage.getId() + "\" title=\"" + onmsOutage.getIfLostService() + "\">");
            return stringBuffer.toString();
        }
    }

    /**
     * The static list of timescales
     */
    public static final ArrayList<TimescaleDescriptor> TIMESCALE_DESCRIPTORS = new ArrayList<>();

    /**
     * Initialization of the timescale list
     */
    static {
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 1, Calendar.MINUTE, 1, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 2, Calendar.MINUTE, 2, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 3, Calendar.MINUTE, 3, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 4, Calendar.MINUTE, 4, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 5, Calendar.MINUTE, 5, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 10, Calendar.MINUTE, 10, Calendar.SECOND));
        TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(60 * 30, Calendar.MINUTE, 30, Calendar.SECOND));

        for (int i = 1; i <= 10; i++) {
            TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(3600 * i, Calendar.HOUR, i, Calendar.SECOND, Calendar.MINUTE));
        }

        for (int i = 1; i <= 10; i++) {
            TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(3600 * 24 * i, Calendar.DAY_OF_MONTH, i, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR));
        }

        for (int i = 1; i <= 10; i++) {
            TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(3600 * 24 * 30 * i, Calendar.MONTH, i, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR, Calendar.DAY_OF_MONTH));
        }

        for (int i = 1; i <= 10; i++) {
            TIMESCALE_DESCRIPTORS.add(new TimescaleDescriptor(3600 * 24 * 360 * i, Calendar.YEAR, i, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR, Calendar.DAY_OF_MONTH, Calendar.MONTH));
        }
    }

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private NodeDao m_nodeDao;

    private OnmsOutageCollection queryOutages(final UriInfo uriInfo, final int nodeId, final String ipAddress, final int serviceId, final long start, final long end) {
        OnmsOutageCollection onmsOutageCollection;

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
        builder.eq("node.id", nodeId);

        final Date startDate = new Date();
        startDate.setTime(start * 1000l);

        final Date endDate = new Date();
        endDate.setTime(end * 1000l);

        builder.or(Restrictions.isNull("ifRegainedService"), Restrictions.gt("ifRegainedService", startDate)); 

        builder.le("ifLostService", endDate);

        builder.eq("serviceType.id", serviceId);
        builder.eq("ipInterface.ipAddress", InetAddressUtils.addr(ipAddress));
        builder.isNull("perspective");

        builder.alias("monitoredService", "monitoredService");
        builder.alias("monitoredService.ipInterface", "ipInterface");
        builder.alias("monitoredService.ipInterface.node", "node");
        builder.alias("monitoredService.serviceType", "serviceType");

        applyQueryFilters(uriInfo.getQueryParameters(), builder, null);

        builder.orderBy("id").desc();

        onmsOutageCollection = new OnmsOutageCollection(m_outageDao.findMatching(builder.toCriteria()));

        return onmsOutageCollection;
    }

    @GET
    @Produces("image/png")
    @Transactional
    @Path("header/{start}/{end}/{width}")
    @Operation(
            summary = "Render the timeline time axis",
            description = """
        Render the labelled time axis: tick marks and date or time labels for the given window. The label
        granularity is chosen from the window length and the width.

        `start` and `end` are epoch seconds.""",
            operationId = "getTimelineHeader"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A 20-pixel-high PNG of the time axis.",
                    content = @Content(mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "A path segment was not a number.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "`width` was 0, or the window was narrower than `width` seconds.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response header(
            @Parameter(description = "Start of the window, in epoch **seconds**.", required = true, example = "1787011200")
            @PathParam("start") final long start,
            @Parameter(description = "End of the window, in epoch **seconds**. Must be at least `width` seconds after `start`.",
                    required = true, example = "1787097600")
            @PathParam("end") final long end,
            @Parameter(description = "Image width in pixels. Must be at least 1.", required = true, example = "800")
            @PathParam("width") final int width) throws IOException {
        long delta = end - start;

        BufferedImage bufferedImage = new BufferedImage(width, 20, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics2D.setColor(Color.BLACK);

        int numLabels = TimescaleDescriptor.computeNumberOfLabels(graphics2D, delta, width);

        for (TimescaleDescriptor desc : TIMESCALE_DESCRIPTORS) {
            if (desc.match(delta, numLabels)) {
                desc.drawHeader(graphics2D, delta, start, width);
                break;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageData = baos.toByteArray();

        return Response.ok(imageData).build();
    }

    @GET
    @Produces("text/html")
    @Transactional
    @Path("html/{nodeId}/{ipAddress}/{serviceId}/{start}/{end}/{width}")
    @Operation(
            summary = "Render the HTML wrapper and image map for a service timeline",
            description = """
        Return an `<img>` element pointing at the matching `timeline/image/...` URL, together with the
        client-side image map that turns each drawn outage into a link to its outage detail page. Each `area`
        carries the outage id in its `alt` and the lost-service timestamp in its `title`.

        A node id that does not exist is not an error: the map comes back empty.

        `start` and `end` are epoch seconds.""",
            operationId = "getTimelineHtml"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The `<img>` element and its image map. The map is empty when no outage falls in the window.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                    <img src="/opennms/rest/timeline/image/1/127.0.0.4/2/1787011200/1787097600/800" usemap="#1-127.0.0.4-2"><map name="1-127.0.0.4-2"><area shape="rect" coords="585,2,586,18" href="/opennms/outage/detail.htm?id=3543" alt="Id 3543" title="2026-08-18 13:34:47.697"></map>"""))),
            @ApiResponse(responseCode = "404", description = "A numeric path segment was not a number.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "`width` was 0, or the window was narrower than `width` seconds.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response html(@Context final UriInfo uriInfo,
            @Parameter(description = "Database id of the node.", required = true, example = "1")
            @PathParam("nodeId") final int nodeId,
            @Parameter(description = "IP address of the interface the service runs on.", required = true, example = "127.0.0.4")
            @PathParam("ipAddress") final String ipAddress,
            @Parameter(description = "Service type id from the `service` table, not the monitored-service id.",
                    required = true, example = "2")
            @PathParam("serviceId") final int serviceId,
            @Parameter(description = "Start of the window, in epoch **seconds**.", required = true, example = "1787011200")
            @PathParam("start") final long start,
            @Parameter(description = "End of the window, in epoch **seconds**. Must be at least `width` seconds after `start`.",
                    required = true, example = "1787097600")
            @PathParam("end") final long end,
            @Parameter(description = "Image width in pixels. Must be at least 1.", required = true, example = "800")
            @PathParam("width") final int width) throws IOException {
        long delta = end - start;

        OnmsOutageCollection onmsOutageCollection = queryOutages(uriInfo, nodeId, ipAddress, serviceId, start, end);

        BufferedImage bufferedImage = new BufferedImage(width, 20, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics2D.setColor(Color.BLACK);

        int numLabels = TimescaleDescriptor.computeNumberOfLabels(graphics2D, delta, width);

        final String encodedIpAddress = URLEncoder.encode(ipAddress, "UTF-8");

        final StringBuffer htmlBuffer = new StringBuffer();

        htmlBuffer.append("<img src=\"/opennms/rest/timeline/image/");
        htmlBuffer.append(nodeId);
        htmlBuffer.append("/");
        htmlBuffer.append(encodedIpAddress);
        htmlBuffer.append("/");
        htmlBuffer.append(serviceId);
        htmlBuffer.append("/");
        htmlBuffer.append(start);
        htmlBuffer.append("/");
        htmlBuffer.append(end);
        htmlBuffer.append("/");
        htmlBuffer.append(width);
        htmlBuffer.append("\" usemap=\"#");
        htmlBuffer.append(nodeId);
        htmlBuffer.append("-");
        htmlBuffer.append(encodedIpAddress);
        htmlBuffer.append("-");
        htmlBuffer.append(serviceId);
        htmlBuffer.append("\"><map name=\"");
        htmlBuffer.append(nodeId);
        htmlBuffer.append("-");
        htmlBuffer.append(encodedIpAddress);
        htmlBuffer.append("-");
        htmlBuffer.append(serviceId);
        htmlBuffer.append("\">");

        for (TimescaleDescriptor desc : TIMESCALE_DESCRIPTORS) {
            if (desc.match(delta, numLabels)) {
                for (OnmsOutage onmsOutage : onmsOutageCollection) {
                    htmlBuffer.append(desc.getMapEntry(graphics2D, delta, start, width, onmsOutage));
                }
                break;
            }
        }

        htmlBuffer.append("</map>");

        return Response.ok(htmlBuffer.toString()).build();
    }

    @GET
    @Produces("image/png")
    @Transactional
    @Path("image/{nodeId}/{ipAddress}/{serviceId}/{start}/{end}/{width}")
    @Operation(
            summary = "Render the outage strip for one monitored service",
            description = """
        Render a 20-pixel-high strip for one monitored service over the window: a green bar from the node's
        creation time onward, red blocks for the outages that overlap the window, and the vertical grid lines.
        An outage that has not been resolved is drawn to the right-hand edge.

        The service is addressed by node id, interface IP address and service type id. Only outages with no
        perspective (that is, from the core poller rather than a remote perspective) are drawn.

        An unknown node id fails with 500, because the node is drawn before it is checked. `start` and `end`
        are epoch seconds.""",
            operationId = "getTimelineImage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A 20-pixel-high PNG of the outage strip.",
                    content = @Content(mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "A numeric path segment was not a number.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, `width` was 0, or the window was narrower than `width` seconds.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response image(@Context final UriInfo uriInfo,
            @Parameter(description = "Database id of the node.", required = true, example = "1")
            @PathParam("nodeId") final int nodeId,
            @Parameter(description = "IP address of the interface the service runs on.", required = true, example = "127.0.0.4")
            @PathParam("ipAddress") final String ipAddress,
            @Parameter(description = "Service type id from the `service` table, not the monitored-service id.",
                    required = true, example = "2")
            @PathParam("serviceId") final int serviceId,
            @Parameter(description = "Start of the window, in epoch **seconds**.", required = true, example = "1787011200")
            @PathParam("start") final long start,
            @Parameter(description = "End of the window, in epoch **seconds**. Must be at least `width` seconds after `start`.",
                    required = true, example = "1787097600")
            @PathParam("end") final long end,
            @Parameter(description = "Image width in pixels. Must be at least 1.", required = true, example = "800")
            @PathParam("width") final int width) throws IOException {
        long delta = end - start;

        OnmsOutageCollection onmsOutageCollection = queryOutages(uriInfo, nodeId, ipAddress, serviceId, start, end);
        OnmsNode node = m_nodeDao.get(nodeId);

        BufferedImage bufferedImage = new BufferedImage(width, 20, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics2D.setColor(Color.BLACK);

        int numLabels = TimescaleDescriptor.computeNumberOfLabels(graphics2D, delta, width);

        for (TimescaleDescriptor desc : TIMESCALE_DESCRIPTORS) {
            if (desc.match(delta, numLabels)) {
                desc.drawNode(graphics2D, delta, start, width, node);

                for (OnmsOutage onmsOutage : onmsOutageCollection) {
                    desc.drawOutage(graphics2D, delta, start, width, onmsOutage);
                }

                desc.drawLine(graphics2D, delta, start, width);

                break;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageData = baos.toByteArray();

        return Response.ok(imageData).build();
    }

    @GET
    @Produces("image/png")
    @Transactional
    @Path("empty/{start}/{end}/{width}")
    @Operation(
            summary = "Render an empty timeline strip",
            description = """
        Render a strip with only the vertical grid lines and no outage or node bar. Nothing is read from the
        database.

        `start` and `end` are epoch seconds. This operation narrows them to `int` before subtracting, so a
        window that does not fit in a signed 32-bit number of seconds produces a wrong or negative range.""",
            operationId = "getTimelineEmpty"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A 20-pixel-high PNG containing only grid lines.",
                    content = @Content(mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "A path segment was not a number.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "`width` was 0, or the window was narrower than `width` seconds.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response empty(
            @Parameter(description = "Start of the window, in epoch **seconds**.", required = true, example = "1787011200")
            @PathParam("start") final long start,
            @Parameter(description = "End of the window, in epoch **seconds**. Must be at least `width` seconds after `start`.",
                    required = true, example = "1787097600")
            @PathParam("end") final long end,
            @Parameter(description = "Image width in pixels. Must be at least 1.", required = true, example = "800")
            @PathParam("width") final int width) throws IOException {
        int delta = (int) end - (int) start;

        BufferedImage bufferedImage = new BufferedImage(width, 20, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics();

        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics2D.setColor(Color.BLACK);

        int numLabels = TimescaleDescriptor.computeNumberOfLabels(graphics2D, delta, width);

        for (TimescaleDescriptor desc : TIMESCALE_DESCRIPTORS) {
            if (desc.match(delta, numLabels)) {
                desc.drawLine(graphics2D, delta, start, width);

                break;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageData = baos.toByteArray();

        return Response.ok(imageData).build();
    }
}

