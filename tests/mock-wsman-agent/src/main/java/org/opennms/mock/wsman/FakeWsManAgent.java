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
package org.opennms.mock.wsman;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A WS-Man agent for tests: speaks enough of the protocol for OpenNMS's
 * detector (Identify), monitor (Transfer Get) and collector (optimized
 * Enumerate, Pull) to work against it over plain HTTP, serves configurable
 * metric values per WMI class instance, and checks HTTP basic credentials.
 *
 * Values can be set at start-up ({@code Class.Property=value},
 * {@code Class[1].Property=value}) and changed while running through
 * {@code PUT /__fake/metrics} with the same one-per-line syntax, so an
 * end-to-end test can drive what the next collection sees.
 *
 * It is a test tool, not a WS-Man implementation: WQL filters are honoured
 * only for their FROM class and SELECT projection, selectors match on
 * equality, and enumerations are never paged unless a Pull is requested.
 */
public class FakeWsManAgent implements AutoCloseable {

    public static final String DEFAULT_VENDOR = "Microsoft Corporation";
    public static final String DEFAULT_VERSION = "OS: 10.0.20348 SP: 0.0 Stack: 3.0";

    private static final String SOAP_NS = "http://www.w3.org/2003/05/soap-envelope";
    private static final String WSEN_NS = "http://schemas.xmlsoap.org/ws/2004/09/enumeration";
    private static final String WSMAN_NS = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";
    private static final String WSMID_NS = "http://schemas.dmtf.org/wbem/wsman/identity/1/wsmanidentity.xsd";
    private static final String CONTROL_PATH = "/__fake/metrics";
    private static final Pattern WQL = Pattern.compile("^\\s*select\\s+(.+?)\\s+from\\s+([A-Za-z0-9_]+)(\\s+where\\s+.*)?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ASSIGNMENT = Pattern.compile("^([A-Za-z0-9_]+)(?:\\[(\\d+)\\])?\\.([A-Za-z0-9_]+)=(.*)$");

    private final HttpServer server;
    private final String username;
    private final String password;
    private volatile String vendor = DEFAULT_VENDOR;
    private volatile String version = DEFAULT_VERSION;
    // class name -> instances -> property values (insertion order kept for stable output)
    private final Map<String, List<Map<String, String>>> classes = new ConcurrentHashMap<>();
    private final Map<String, Enumeration> enumerations = new ConcurrentHashMap<>();
    // a long-running agent (smoke tests poll for hours) must not grow without bound
    private static final int MAX_REQUEST_LOG = 1000;
    private final List<String> requestLog = new ArrayList<>();

    private static final class Enumeration {
        private final List<Element> items;
        private Enumeration(final List<Element> items) {
            this.items = items;
        }
    }

    public FakeWsManAgent(final String bindAddress, final int port, final String username, final String password) throws IOException {
        this.username = username;
        this.password = password;
        server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        loadWindowsDefaults();
    }

    /** Binds an ephemeral port on the loopback address. */
    public static FakeWsManAgent onLoopback(final String username, final String password) throws IOException {
        return new FakeWsManAgent("127.0.0.1", 0, username, password);
    }

    public FakeWsManAgent start() {
        server.start();
        return this;
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public String getUrl() {
        return "http://127.0.0.1:" + getPort() + "/wsman";
    }

    @Override
    public void close() {
        server.stop(0);
    }

    public FakeWsManAgent withIdentity(final String vendor, final String version) {
        this.vendor = vendor;
        this.version = version;
        return this;
    }

    /** Replaces the instances of a class. */
    public FakeWsManAgent withInstances(final String className, final List<Map<String, String>> instances) {
        final List<Map<String, String>> copy = new ArrayList<>();
        for (final Map<String, String> instance : instances) {
            copy.add(new LinkedHashMap<>(instance));
        }
        classes.put(className, copy);
        return this;
    }

    /** Applies {@code Class.Property=value} or {@code Class[index].Property=value}; a missing instance is created. */
    public FakeWsManAgent set(final String assignment) {
        final Matcher m = ASSIGNMENT.matcher(assignment.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Expected Class.Property=value or Class[index].Property=value, got: " + assignment);
        }
        final List<Map<String, String>> instances = classes.computeIfAbsent(m.group(1), k -> new ArrayList<>());
        final int index = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        while (instances.size() <= index) {
            instances.add(new LinkedHashMap<>());
        }
        instances.get(index).put(m.group(3), m.group(4));
        return this;
    }

    public String get(final String className, final int index, final String property) {
        final List<Map<String, String>> instances = classes.get(className);
        return instances == null || instances.size() <= index ? null : instances.get(index).get(property);
    }

    /** The WS-Addressing actions served so far, oldest first. */
    public synchronized List<String> getRequestLog() {
        return new ArrayList<>(requestLog);
    }

    // --- the shipped Windows data collection groups, with plausible values ---

    private void loadWindowsDefaults() {
        set("Win32_OperatingSystem.Caption=Microsoft Windows Server 2022 Standard");
        set("Win32_OperatingSystem.Name=Microsoft Windows Server 2022 Standard|C:\\\\Windows|\\\\Device\\\\Harddisk0\\\\Partition3");
        set("Win32_OperatingSystem.SerialNumber=00454-40000-00001-AA123");
        set("Win32_OperatingSystem.TotalSwapSpaceSize=0");
        set("Win32_OperatingSystem.TotalVirtualMemorySize=19922944");
        set("Win32_OperatingSystem.TotalVisibleMemorySize=16776692");
        set("Win32_OperatingSystem.FreePhysicalMemory=9123456");
        set("Win32_OperatingSystem.FreeSpaceInPagingFiles=3145728");
        set("Win32_OperatingSystem.FreeVirtualMemory=11223344");
        set("Win32_OperatingSystem.LastBootUpTime=20260901081500.500000-240");
        set("Win32_PerfFormattedData_PerfOS_Memory.AvailableBytes=9342418944");
        set("Win32_PerfFormattedData_PerfOS_Memory.CacheBytes=1258291200");
        set("Win32_PerfFormattedData_PerfOS_Memory.CacheFaultsPersec=1200");
        set("Win32_PerfFormattedData_PerfOS_Memory.PageFaultsPersec=8400");
        set("Win32_PerfFormattedData_PerfOS_Memory.CommitLimit=34359738368");
        set("Win32_PerfFormattedData_PerfOS_Memory.CommittedBytes=9126805504");
        set("Win32_PerfFormattedData_PerfOS_Memory.PoolNonpagedAllocs=141000");
        set("Win32_PerfFormattedData_PerfOS_Memory.PoolPagedAllocs=232000");
        set("Win32_PerfFormattedData_PerfOS_Objects.Processes=187");
        set("Win32_PerfFormattedData_PerfOS_Objects.Threads=2412");
        set("Win32_PerfFormattedData_PerfOS_Objects.Events=41230");
        set("Win32_PerfFormattedData_PerfOS_Objects.Mutexes=1860");
        set("Win32_PerfFormattedData_PerfOS_Objects.Sections=9012");
        set("Win32_PerfFormattedData_PerfOS_Objects.Semaphores=3344");
        for (int cpu = 0; cpu < 2; cpu++) {
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].Name=" + cpu);
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].InterruptsPersec=" + (1200 + cpu * 100));
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].PercentProcessorTime=" + (12 + cpu * 5));
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].PercentDPCTime=1");
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].PercentInterruptTime=0");
            set("Win32_PerfFormattedData_PerfOS_Processor[" + cpu + "].PercentUserTime=" + (8 + cpu * 3));
        }
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.Name=0 C:");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.PercentDiskReadTime=3");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.PercentDiskWriteTime=2");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.SplitIOPerSec=0");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.DiskTransfersPersec=45");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.AvgDisksecPerTransfer=0");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.CurrentDiskQueueLength=0");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.AvgDiskQueueLength=0");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.PercentDiskTime=5");
        set("Win32_PerfFormattedData_PerfDisk_PhysicalDisk.PercentIdleTime=95");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.Name=C:");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.FreeMegabytes=51200");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.PercentDiskReadTime=3");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.PercentDiskWriteTime=2");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.PercentFreeSpace=40");
        set("Win32_PerfFormattedData_PerfDisk_LogicalDisk.SplitIOPerSec=0");
        set("Win32_PerfFormattedData_LocalSessionManager_TerminalServices.ActiveSessions=1");
        set("Win32_PerfFormattedData_LocalSessionManager_TerminalServices.InactiveSessions=0");
        set("Win32_PerfFormattedData_LocalSessionManager_TerminalServices.TotalSessions=1");
        set("Win32_PerfFormattedData_PerfNet_Server.ServerSessions=3");
        set("Win32_PerfFormattedData_PerfNet_Server.SessionsErroredOut=0");
        set("Win32_PerfFormattedData_PerfNet_Server.SessionsForcedOff=0");
        set("Win32_PerfFormattedData_PerfNet_Server.SessionsLoggedOff=12");
        set("Win32_PerfFormattedData_PerfNet_Server.SessionsTimedOut=1");
        set("Win32_PerfFormattedData_PerfNet_Server.ErrorsSystem=0");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.Name=Ethernet0");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.CurrentBandwidth=1000000000");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.BytesTotalPersec=123456789");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.BytesReceivedPersec=98765432");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.BytesSentPersec=24691357");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsPersec=555555");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsReceivedPersec=333333");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsReceivedNonUnicastPersec=1111");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsReceivedUnicastPersec=332222");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsSentPersec=222222");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsSentNonUnicastPersec=999");
        set("Win32_PerfRawData_Tcpip_NetworkInterface.PacketsSentUnicastPersec=221223");
    }

    // --- HTTP ---------------------------------------------------------------

    private void handle(final HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestURI().getPath().startsWith(CONTROL_PATH)) {
                handleControl(exchange);
                return;
            }
            if (!authorized(exchange)) {
                exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"wsman\"");
                respond(exchange, 401, "text/plain", "Unauthorized");
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, "text/plain", "WS-Man requests are POSTed SOAP envelopes");
                return;
            }
            final Document request = parse(exchange.getRequestBody());
            final String action = headerText(request, "Action");
            final String messageId = headerText(request, "MessageID");
            final String wsaNs = addressingNamespace(request);
            final boolean identify = action != null && action.endsWith("/Identify") || firstBodyElement(request, WSMID_NS, "Identify") != null;
            synchronized (this) {
                if (requestLog.size() >= MAX_REQUEST_LOG) {
                    requestLog.remove(0);
                }
                requestLog.add(identify ? WSMID_NS + "/Identify" : action == null ? "(no action)" : action);
            }
            final String body;
            if (identify) {
                body = identifyResponse();
            } else if (action != null && action.endsWith("/enumeration/Enumerate")) {
                body = enumerateResponse(request);
            } else if (action != null && action.endsWith("/enumeration/Pull")) {
                body = pullResponse(request);
            } else if (action != null && action.endsWith("/enumeration/Release")) {
                body = "";
            } else if (action != null && action.endsWith("/transfer/Get")) {
                body = getResponse(request);
            } else {
                respond(exchange, 500, "application/soap+xml;charset=UTF-8", fault(wsaNs, "wsa:ActionNotSupported", "Unsupported action: " + action));
                return;
            }
            if (body == null) {
                respond(exchange, 500, "application/soap+xml;charset=UTF-8", fault(wsaNs, "wsman:DestinationUnreachable", "No such resource"));
                return;
            }
            respond(exchange, 200, "application/soap+xml;charset=UTF-8", envelope(wsaNs, action == null ? "" : action + "Response", messageId, body));
        } catch (final RuntimeException e) {
            respond(exchange, 500, "text/plain", "fake agent error: " + e);
        }
    }

    private boolean authorized(final HttpExchange exchange) {
        if (username == null) {
            return true;
        }
        final String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            return false;
        }
        final String expected = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return expected.equals(header.substring("Basic ".length()).trim());
    }

    private void handleControl(final HttpExchange exchange) throws IOException {
        if ("PUT".equals(exchange.getRequestMethod()) || "POST".equals(exchange.getRequestMethod())) {
            final String text = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int applied = 0;
            for (final String line : text.split("\\r?\\n")) {
                if (!line.isBlank() && !line.trim().startsWith("#")) {
                    set(line);
                    applied++;
                }
            }
            respond(exchange, 200, "text/plain", applied + " value(s) applied\n");
            return;
        }
        final StringBuilder out = new StringBuilder();
        for (final Map.Entry<String, List<Map<String, String>>> e : classes.entrySet()) {
            for (int i = 0; i < e.getValue().size(); i++) {
                for (final Map.Entry<String, String> prop : e.getValue().get(i).entrySet()) {
                    out.append(e.getKey()).append('[').append(i).append("].").append(prop.getKey()).append('=').append(prop.getValue()).append('\n');
                }
            }
        }
        respond(exchange, 200, "text/plain", out.toString());
    }

    private static void respond(final HttpExchange exchange, final int status, final String contentType, final String body) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    // --- protocol -----------------------------------------------------------

    private String identifyResponse() {
        return "<wsmid:IdentifyResponse xmlns:wsmid=\"" + WSMID_NS + "\">"
                + "<wsmid:ProtocolVersion>" + WSMAN_NS + "</wsmid:ProtocolVersion>"
                + "<wsmid:ProductVendor>" + escape(vendor) + "</wsmid:ProductVendor>"
                + "<wsmid:ProductVersion>" + escape(version) + "</wsmid:ProductVersion>"
                + "</wsmid:IdentifyResponse>";
    }

    private String enumerateResponse(final Document request) {
        final String resourceUri = headerText(request, "ResourceURI");
        final Element enumerate = firstBodyElement(request, WSEN_NS, "Enumerate");
        String dialect = null;
        String filter = null;
        boolean optimized = false;
        if (enumerate != null) {
            final NodeList children = enumerate.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                final Node child = children.item(i);
                if (!(child instanceof Element)) {
                    continue;
                }
                if ("Filter".equals(child.getLocalName())) {
                    dialect = ((Element) child).getAttribute("Dialect");
                    filter = child.getTextContent();
                } else if ("OptimizeEnumeration".equals(child.getLocalName())) {
                    optimized = true;
                }
            }
        }
        final List<Element> items = itemsFor(resourceUri, dialect, filter);
        if (items == null) {
            return null;
        }
        if (optimized) {
            return "<wsen:EnumerateResponse xmlns:wsen=\"" + WSEN_NS + "\" xmlns:wsman=\"" + WSMAN_NS + "\">"
                    + "<wsen:EnumerationContext/>"
                    + "<wsman:Items>" + serialize(items) + "</wsman:Items>"
                    + "<wsman:EndOfSequence/>"
                    + "</wsen:EnumerateResponse>";
        }
        final String context = "uuid:" + UUID.randomUUID();
        enumerations.put(context, new Enumeration(items));
        return "<wsen:EnumerateResponse xmlns:wsen=\"" + WSEN_NS + "\">"
                + "<wsen:EnumerationContext>" + context + "</wsen:EnumerationContext>"
                + "</wsen:EnumerateResponse>";
    }

    private String pullResponse(final Document request) {
        final Element pull = firstBodyElement(request, WSEN_NS, "Pull");
        final String context = pull == null ? null : childText(pull, "EnumerationContext");
        final Enumeration enumeration = context == null ? null : enumerations.remove(context.trim());
        if (enumeration == null) {
            return null;
        }
        return "<wsen:PullResponse xmlns:wsen=\"" + WSEN_NS + "\">"
                + "<wsen:Items>" + serialize(enumeration.items) + "</wsen:Items>"
                + "<wsen:EndOfSequence/>"
                + "</wsen:PullResponse>";
    }

    private String getResponse(final Document request) {
        final String resourceUri = headerText(request, "ResourceURI");
        if (resourceUri == null || resourceUri.endsWith("*")) {
            return null;
        }
        final Map<String, String> selectors = new LinkedHashMap<>();
        final Element selectorSet = firstHeaderElement(request, "SelectorSet");
        if (selectorSet != null) {
            final NodeList selectorNodes = selectorSet.getChildNodes();
            for (int i = 0; i < selectorNodes.getLength(); i++) {
                if (selectorNodes.item(i) instanceof Element && "Selector".equals(selectorNodes.item(i).getLocalName())) {
                    selectors.put(((Element) selectorNodes.item(i)).getAttribute("Name"), selectorNodes.item(i).getTextContent().trim());
                }
            }
        }
        final String className = className(resourceUri);
        final List<Map<String, String>> instances = classes.get(className);
        if (instances == null) {
            return null;
        }
        for (final Map<String, String> instance : instances) {
            boolean matches = true;
            for (final Map.Entry<String, String> selector : selectors.entrySet()) {
                if (!selector.getValue().equals(instance.get(selector.getKey()))) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return serialize(List.of(element(resourceUri, className, instance, null)));
            }
        }
        return null;
    }

    // Resolve which class a request means: the resource URI's last segment, or
    // for a wildcard URI the FROM clause of the WQL filter.
    private List<Element> itemsFor(final String resourceUri, final String dialect, final String filter) {
        if (resourceUri == null) {
            return null;
        }
        String className;
        List<String> projection = null;
        String namespaceUri = resourceUri;
        if (filter != null && !filter.isBlank()) {
            final Matcher m = WQL.matcher(filter.trim());
            if (!m.matches()) {
                return null;
            }
            className = m.group(2);
            if (!"*".equals(m.group(1).trim())) {
                projection = new ArrayList<>();
                for (final String column : m.group(1).split(",")) {
                    projection.add(column.trim());
                }
            }
            namespaceUri = resourceUri.endsWith("*") ? resourceUri.substring(0, resourceUri.length() - 1) + className : resourceUri;
        } else {
            if (resourceUri.endsWith("*")) {
                return null;
            }
            className = className(resourceUri);
        }
        final List<Map<String, String>> instances = classes.get(className);
        if (instances == null) {
            return List.of();
        }
        final List<Element> items = new ArrayList<>();
        for (final Map<String, String> instance : instances) {
            items.add(element(namespaceUri, className, instance, projection));
        }
        return items;
    }

    private static String className(final String resourceUri) {
        return resourceUri.substring(resourceUri.lastIndexOf('/') + 1);
    }

    private static Element element(final String namespaceUri, final String className, final Map<String, String> instance, final List<String> projection) {
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Element el = doc.createElementNS(namespaceUri, "p:" + className);
            for (final Map.Entry<String, String> prop : instance.entrySet()) {
                if (projection != null && projection.stream().noneMatch(p -> p.equalsIgnoreCase(prop.getKey()))) {
                    continue;
                }
                final Element child = doc.createElementNS(namespaceUri, "p:" + prop.getKey());
                child.setTextContent(prop.getValue());
                el.appendChild(child);
            }
            return el;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // --- XML helpers ------------------------------------------------------------

    private static Document parse(final InputStream in) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(in);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Unparseable SOAP request: " + e.getMessage(), e);
        }
    }

    private static Element part(final Document request, final String localName) {
        final NodeList list = request.getElementsByTagNameNS(SOAP_NS, localName);
        return list.getLength() == 0 ? null : (Element) list.item(0);
    }

    private static Element firstHeaderElement(final Document request, final String localName) {
        final Element header = part(request, "Header");
        if (header == null) {
            return null;
        }
        final NodeList children = header.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element && localName.equals(children.item(i).getLocalName())) {
                return (Element) children.item(i);
            }
        }
        return null;
    }

    private static String headerText(final Document request, final String localName) {
        final Element el = firstHeaderElement(request, localName);
        return el == null ? null : el.getTextContent().trim();
    }

    private static String addressingNamespace(final Document request) {
        final Element action = firstHeaderElement(request, "Action");
        return action == null || action.getNamespaceURI() == null ? "http://www.w3.org/2005/08/addressing" : action.getNamespaceURI();
    }

    private static Element firstBodyElement(final Document request, final String namespace, final String localName) {
        final Element body = part(request, "Body");
        if (body == null) {
            return null;
        }
        final NodeList children = body.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element && localName.equals(child.getLocalName()) && namespace.equals(child.getNamespaceURI())) {
                return (Element) child;
            }
        }
        return null;
    }

    private static String childText(final Element parent, final String localName) {
        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element && localName.equals(children.item(i).getLocalName())) {
                return children.item(i).getTextContent();
            }
        }
        return null;
    }

    private static String serialize(final List<Element> elements) {
        final StringBuilder sb = new StringBuilder();
        for (final Element el : elements) {
            serialize(el, sb);
        }
        return sb.toString();
    }

    private static void serialize(final Element el, final StringBuilder sb) {
        sb.append('<').append(el.getTagName());
        if (el.getNamespaceURI() != null && (el.getParentNode() == null || !(el.getParentNode() instanceof Element))) {
            sb.append(" xmlns:").append(el.getPrefix()).append("=\"").append(escape(el.getNamespaceURI())).append('"');
        }
        sb.append('>');
        final NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element) {
                serialize((Element) child, sb);
            } else if (child.getNodeValue() != null) {
                sb.append(escape(child.getNodeValue()));
            }
        }
        sb.append("</").append(el.getTagName()).append('>');
    }

    private static String envelope(final String wsaNs, final String action, final String relatesTo, final String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<s:Envelope xmlns:s=\"" + SOAP_NS + "\" xmlns:wsa=\"" + wsaNs + "\">"
                + "<s:Header>"
                + "<wsa:Action>" + escape(action) + "</wsa:Action>"
                + "<wsa:MessageID>uuid:" + UUID.randomUUID() + "</wsa:MessageID>"
                + (relatesTo == null ? "" : "<wsa:RelatesTo>" + escape(relatesTo) + "</wsa:RelatesTo>")
                + "<wsa:To>" + wsaNs + "/anonymous</wsa:To>"
                + "</s:Header>"
                + "<s:Body>" + body + "</s:Body>"
                + "</s:Envelope>";
    }

    private static String fault(final String wsaNs, final String code, final String reason) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<s:Envelope xmlns:s=\"" + SOAP_NS + "\" xmlns:wsa=\"" + wsaNs + "\" xmlns:wsman=\"" + WSMAN_NS + "\">"
                + "<s:Header><wsa:Action>" + wsaNs + "/fault</wsa:Action></s:Header>"
                + "<s:Body><s:Fault><s:Code><s:Value>s:Sender</s:Value><s:Subcode><s:Value>" + code + "</s:Value></s:Subcode></s:Code>"
                + "<s:Reason><s:Text xml:lang=\"en\">" + escape(reason) + "</s:Text></s:Reason></s:Fault></s:Body></s:Envelope>";
    }

    private static String escape(final String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Runs an agent from the command line, for smoke tests: {@code FakeWsManAgent [--bind ADDR] [--port N]
     * [--user U --password P] [--vendor V] [--version V] [Class.Property=value ...]}.
     */
    public static void main(final String[] args) throws Exception {
        String bind = "127.0.0.1";
        int port = 5985;
        String user = null;
        String password = null;
        String vendor = DEFAULT_VENDOR;
        String version = DEFAULT_VERSION;
        final List<String> assignments = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "--bind": bind = args[++i]; break;
                case "--port": port = Integer.parseInt(args[++i]); break;
                case "--user": user = args[++i]; break;
                case "--password": password = args[++i]; break;
                case "--vendor": vendor = args[++i]; break;
                case "--version": version = args[++i]; break;
                default: assignments.add(args[i]);
            }
        }
        final FakeWsManAgent agent = new FakeWsManAgent(bind, port, user, Objects.requireNonNullElse(password, "")).withIdentity(vendor, version);
        assignments.forEach(agent::set);
        agent.start();
        System.out.println("fake WS-Man agent listening on " + bind + ":" + agent.getPort() + " (user " + user + ")");
        Thread.currentThread().join();
    }

    /** Reads a document from a string; exposed for tests that build requests by hand. */
    static Document parse(final String xml) {
        return parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
