#!/usr/bin/env groovy

import groovy.xml.DOMBuilder;
import groovy.xml.dom.DOMCategory;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import javax.xml.transform.OutputKeys;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;

import org.xml.sax.InputSource;


def pdb = ProvisioningDatabase.load(new File("config-ext.txt"), new File("config-int.txt"));

def capsdConfig = new CapsdConfiguration(new File("capsd-configuration.xml"));
def pollerConfig = new PollerConfiguration(new File("poller-configuration.xml"));

pdb.forEachService { svc -> capsdConfig.process(svc) }
pdb.forEachService { svc -> pollerConfig.process(svc) }

capsdConfig.save();
pollerConfig.save();


class ProvisioningDatabase {
    def comment = ~/^\s*#.*$/;
    def blank = ~/^\s*$/;
    def data = ~/^\s*(\S*)\s+(\S*)\s+(\S*)\s+(\S*)\s*$/;

    def services = [];

    public static ProvisioningDatabase load(externalFile, internalFile) {
        ProvisioningDatabase pdb = new ProvisioningDatabase();
        pdb.parse(false, externalFile);
        pdb.parse(true, internalFile);
        return pdb;
    }

    public void parse(internal, r) {
        r.eachLine { line ->
            if (line =~ blank) {
                return;
            }
            if (line =~ comment) {
                return;
            }
            def matcher = line =~ data;
            if (matcher) {
                def name = matcher.group(1);
                def url = new ServiceURL(url:matcher.group(2));
                def threshold = new ServiceThreshold(thresholdSpec:matcher.group(3));
                def email = matcher.group(4);
                def svc = new ProvisionedService(internal:internal, name:name, url:url, threshold:threshold, email:email);
                services.add(svc);
            }
        }
   }

   public void print() {
       for(svc in services) {
           println svc;
       }
   }

    public void forEachService(Closure c) {
        for(svc in services) {
            c(svc);
        }
    }
}

class ServiceThreshold {
    String thresholdSpec;

    public String toString() {
        return thresholdSpec;
    }
}

class ServiceURL {
    String url;
    private addr = null;
    

    public String toString() {
        return url;
    }

    public int getPort() {
        return new URL(url).getPort();
    }

    public String getPath() {
        return new URL(url).getPath();
    }

    public String getHost() {
        return new URL(url).getHost();
    }

    public String getQuery() {
        return new URL(url).getQuery();
    }

    public String getFile() {
        return new URL(url).getFile();
    }

    public String getAddress() {
        if (addr != null) {
            return addr;
        }

        String host = getHost();
        try {
            addr = InetAddress.getByName(host).getHostAddress();
        } catch(Exception e) {
            System.err.println e
            addr = host;
        }

        return addr;
    }
}

class ProvisionedService {
    boolean internal;
    String name;
    ServiceURL url;
    ServiceThreshold threshold;
    String email;

    public String getServiceName() {
        return name.replace('-','_');
    }

    public String getPollingPackageName() {
        return internal ? "internal" : "external";
    }

    public String toString() {
        return "S: $name U: $url T: $threshold E: $email";
    }
}

class CapsdConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }

    CapsdConfiguration(File file) {
        super(file);
        initialize();
    }

    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'capsd-configuration'();
    }

    public void process(ProvisionedService svc) {
        if (alreadyConfigured(svc)) {
            return;
        }
        def plugin = getExistingConfiguration(svc);
        if (plugin == null) {
            createNewConfiguration(svc);
        } else {
            addToExistingConfiguration(plugin, svc);
        }
    }

    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'protocol-plugin'.'protocol-configuration'.specific.find { it.text() == svc.url.address });
        }
        return result;
    }

    public Object getExistingConfiguration(ProvisionedService svc) {
        // handle a bug in groovy that doesnt properly return from 'use' cks
        def p;
        use(DOMCategory) {
            p = document.documentElement['protocol-plugin'].find { it['@protocol'] == svc.serviceName }
        }
        return p;
    }

    public void addToExistingConfiguration(Element plugin, ProvisionedService svc) {
        def protoConfig;
        use(DOMCategory) {
           protoConfig = plugin['protocol-configuration'][0];
        }
        addLeadingComment(plugin, comment(svc));
        def xml = new DomBuilder(document, protoConfig);
        xml.specific(svc.url.address);
    }

    
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.comment(comment(svc));
        xml.'protocol-plugin'(protocol:svc.serviceName, 'class-name':'org.opennms.netmgt.capsd.LoopPlugin', scan:'on', 'user-defined':false) {
            'protocol-configuration'(scan:'enabled', 'user-defined':false) {
                specific(svc.url.address)
            }
            'property'(key:'timeout', value:3000)
            'property'(key:'retry', value:1)
        }
    }
    
}

class PollerConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    PollerConfiguration(File file) {
        super(file);
        initialize();
    }

    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'poller-configuration'() {
            ['internal','external'].each {
                'package'(name:it) {
                    filter("IPADDR IPLIKE *.*.*.*")
                    'include-range'(begin:'1.1.1.1', end:'254.254.254.254')
                    rrd(step:300) {
                        rra('RRA:AVERAGE:0.5:1:2016')
                        rra('RRA:AVERAGE:0.5:12:4464')
                        rra('RRA:MIN:0.5:12:4464')
                        rra('RRA:MAX:0.5:12:4464')
                    }
                }
            }
        };
    }

    public void process(ProvisionedService svc) {
        if (!alreadyConfigured(svc)) {
            createNewConfiguration(svc);
        }
        if (!monitorConfigured(svc)) {
            createNewMonitor(svc);
        }
    }

    public boolean monitorConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.monitor.find{ it['@service'] == svc.serviceName });
        }
        return result;
    }

    public boolean createNewMonitor(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.monitor(service:svc.serviceName, 'class-name':'org.opennms.netmgt.poller.monitors.PageSequenceMonitor');
    }

    public boolean alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'package'.find{ it['@name'] == svc.pollingPackageName }?.service?.find{ it['@name'] == svc.serviceName })
        }
        return result;
    }

    public Node findPackage(ProvisionedService svc) {
        Node pkg;
        use (DOMCategory) {
            pkg = document.documentElement.'package'.find{ it['@name'] == svc.pollingPackageName };
        }
        return pkg;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def pkg = findPackage(svc);
        def agentInfo = svc.internal ? "Internal" : "External";
        def xml = new DomBuilder(document, pkg);
        xml.comment(comment(svc));
        xml.service(name:svc.serviceName, interval:300000, 'user-defined':true, status:'on') {
            parameter(key:'retry', value:1);
            parameter(key:'timeout', value:3000);
            parameter(key:'rrd-repository', value:'/opt/OpenNMS/share/rrd/response');
            parameter(key:'ds-name', value:svc.serviceName);
            parameter(key:'page-sequence') {
                'page-sequence' {
                    page('user-agent':"FASTMonitor/v1.3.2 (${agentInfo})", host:svc.url.address, port:svc.url.port, path:svc.url.file)
                }
            }
        }
    }
    
}

/**
 * This class reprsents a base class for XML configuration files that need to 
 * be loaded edited and saved.  
 */
abstract class XMLConfigurationFile {
    DocumentBuilder bldr;
    File file;
    Document document;

    XMLConfigurationFile(File file) {
        bldr = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.file = file;
    }

    public void initialize() {
        if (file.exists()) {
            document = bldr.parse(file);
        } else {
            document = bldr.newDocument();
            intializeNewDocument(document);
        }
    }

    public abstract void intializeNewDocument(Document document);

    public int nodeDepth(Node node) {
        Node n = node;
        int depth = 0;
        while(!n.isSameNode(document.documentElement)) {
            n = n.getParentNode();
            depth++;
        }
        return depth;
        
    }

    public void addLeadingComment(Node node, String comment) {
        DomBuilder.addLeadingComment(node, comment);
    }
    
    public void save() {
        def transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(file));
    }        
}


/**
 * This class is a DomBuilder similar to DOMBuilder provided with groovy but handles comments and
 * properly indents created DOM elements
 */
class DomBuilder extends BuilderSupport {
    private static final String START_TAG_INDENT = "start-tag-indent";
    private static final String END_TAG_INDENT = "end-tag-indent";

    Document document;

    public DomBuilder(Document doc) {
        this(doc, doc);
    }

    public DomBuilder(Document doc, Node node) {
        document = doc;
        setCurrent(node);
    }

    public static String addLeadingComment(Node node, String comment) {
        Document d = node.ownerDocument;
        Node parent = node.parentNode;
        Node insertionPoint = node;
        Node prev = node.previousSibling;
        if (DomBuilder.isIndent(prev)) {
            insertionPoint = prev;
        }
        parent.insertBefore(createIndent(d, nodeDepth(d, node)), insertionPoint);
        parent.insertBefore(d.createComment(comment), insertionPoint);
    }

    public static String indentString(int depth) {
        StringBuilder buf = new StringBuilder(depth*4 + 1);
        (depth*4).times { buf.append(' '); }
        return buf.toString();
    }

    protected void setParent(Object parent, Object child) {
        Node lastChild = parent.lastChild;
        if (DomBuilder.isIndent(lastChild)) {
            parent.insertBefore(child, lastChild);
        } else {
            parent.appendChild(child);
        }
    }

    private static boolean isIndent(Node lastChild) {
        if (lastChild == null) {
            return false;
        }

        if (lastChild.nodeType != Node.TEXT_NODE) {
            return false;
        }

        if (lastChild.textContent =~ /^\s*$/) {
            return true;
        }

        return false;
    }
    protected Object createNode(Object name) {
        if ("comment".equals(name)) {
            return document.createComment("");
        } else {
            return document.createElement(name);
        }
    }
    protected Object createNode(Object name, Object value) {
        if ("comment".equals(name)) {
            return document.createComment(value.toString());
        } else {
            def element = document.createElement(name);
            def text = document.createTextNode(value.toString());
            element.appendChild((Node)text);
            return element
        }
    }
    protected Object createNode(Object name, Map attributes) {
        def element = document.createElement(name);
        attributes.each { k,v -> element.setAttribute(k.toString(), v.toString()) }
        return element;
    }
    protected Object createNode(Object name, Map attributes, Object value) {
        def element = createNode(name, attributes);
        element.appendChild((Node)document.createTextNode(value.toString()));
        return element;
    }
    protected void nodeCompleted(Object parent, Object node) {
        int depth = nodeDepth(node);
        if (depth > 0) {
            parent.insertBefore(createIndentForStartTag(depth), node);
        }

        if (!isOneLiner(node)) {
            node.appendChild(document.createTextNode('\n'+indentString(depth)));
        }
    }

    public Node createIndentForStartTag(int depth) {
        createIndent(document, depth);
    }

    public static Node createIndent(Document d, int depth) {
        Node text = d.createTextNode('\n'+indentString(depth));
        return text;
    }


    public Node createIndentForEndTag(int depth) {
        createIndent(document, depth);
    }

    public int nodeDepth(Node node) {
        nodeDepth(document, node);
    }

    public static int nodeDepth(Document d, Node node) {
        Node n = node;
        int depth = 0;
        while(!n.isSameNode(d.documentElement)) {
            n = n.getParentNode();
            depth++;
        }
        return depth;
        
    }

    public boolean isOneLiner(Node node) {
        if (!node.hasChildNodes()) {
            return true;
        }

        NodeList children = node.getChildNodes();
        if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
            return true;
        }

        return false;
        
    }

}

