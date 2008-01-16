package org.opennms.netmgt.statsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.castor.statsd.PackageReport;
import org.opennms.netmgt.dao.castor.statsd.StatsdPackage;
import org.opennms.netmgt.dao.support.BottomNAttributeStatisticVisitor;
import org.opennms.netmgt.dao.support.MockResourceType;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

public class ReportDefinitionTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private ResourceDao m_resourceDao = m_mocks.createMock(ResourceDao.class);
    private RrdDao m_rrdDao = m_mocks.createMock(RrdDao.class);
    private FilterDao m_filterDao = m_mocks.createMock(FilterDao.class);
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        m_mocks.verifyAll();
    }

    @SuppressWarnings("unchecked")
    public void testBogusReportClass() {
        // Not replaying anything, but need to do it before verifyAll() happens
        m_mocks.replayAll();
        
        ReportDefinition def = new ReportDefinition();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            def.setReportClass((Class<? extends AttributeStatisticVisitorWithResults>) String.class); 
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSet() {
        // Not replaying anything, but need to do it before verifyAll() happens
        m_mocks.replayAll();
        
        createReportDefinition();
    }
    
    public void testReportWalking() throws Exception {
        EasyMock.expect(m_resourceDao.findTopLevelResources()).andReturn(new ArrayList<OnmsResource>(0));
        
        ReportDefinition def = createReportDefinition();
        def.setResourceAttributeKey("ifSpeed");
        def.setResourceAttributeValueMatch("100000000");
        ReportInstance report = def.createReport(m_resourceDao, m_rrdDao, m_filterDao);

        m_mocks.replayAll();
        
        report.walk();

        assertEquals("results size", 0, report.getResults().size());
    }

    public void testUnfilteredResourceAttributeFilteringWithNoMatch() throws Exception {
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("IfInOctets", "something", "something else");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute));

        EasyMock.expect(m_resourceDao.findTopLevelResources()).andReturn(Collections.singletonList(resource));
        
        ReportDefinition def = createReportDefinition();
        def.setResourceAttributeKey("ifSpeed");
        def.setResourceAttributeValueMatch("100000000");
        ReportInstance report = def.createReport(m_resourceDao, m_rrdDao, m_filterDao);

        m_mocks.replayAll();
        
        report.walk();
        
        assertEquals("results size", 0, report.getResults().size());
    }

    public void testUnfilteredResourceAttributeFilteringWithMatch() throws Exception {
        OnmsAttribute rrdAttribute = new RrdGraphAttribute("IfInOctets", "something", "something else");
        ExternalValueAttribute externalValueAttribute = new ExternalValueAttribute("ifSpeed", "100000000");

        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>();
        attributes.add(rrdAttribute);
        attributes.add(externalValueAttribute);

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsResource resource = new OnmsResource("1", "Node One", resourceType, attributes);

        EasyMock.expect(m_resourceDao.findTopLevelResources()).andReturn(Collections.singletonList(resource));
        
        ReportDefinition def = createReportDefinition();
        def.setResourceAttributeKey(externalValueAttribute.getName());
        def.setResourceAttributeValueMatch(externalValueAttribute.getValue());
        ReportInstance report = def.createReport(m_resourceDao, m_rrdDao, m_filterDao);

        EasyMock.expect(m_rrdDao.getPrintValue(rrdAttribute, def.getConsolidationFunction(), report.getStartTime(), report.getEndTime())).andReturn(1.0);

        m_mocks.replayAll();
        
        report.walk();
        
        m_mocks.verifyAll();

        assertEquals("results size", 1, report.getResults().size());
        
        m_mocks.replayAll();
    }

    public void testFilteredResourceAttributeFilteringWithNoMatch() throws Exception {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("Node One");
        
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("IfInOctets", "something", "something else");
        OnmsResource resource = new OnmsResource(node.getId().toString(), node.getLabel(), resourceType, Collections.singleton(attribute));

        ReportDefinition def = createReportDefinition();
        def.getReport().getPackage().setFilter("");
        def.setResourceAttributeKey("ifSpeed");
        def.setResourceAttributeValueMatch("100000000");
        ReportInstance report = def.createReport(m_resourceDao, m_rrdDao, m_filterDao);

        m_filterDao.walkMatchingNodes(EasyMock.eq(""), EasyMock.isA(EntityVisitor.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                ((EntityVisitor) EasyMock.getCurrentArguments()[1]).visitNode(node);
                return null;
            }
        });
        
        EasyMock.expect(m_resourceDao.getResourceForNode(node)).andReturn(resource);

        m_mocks.replayAll();
        
        report.walk();
        
        assertEquals("results size", 0, report.getResults().size());
    }


    public void testFilteredResourceAttributeFilteringWithMatch() throws Exception {
        OnmsAttribute rrdAttribute = new RrdGraphAttribute("IfInOctets", "something", "something else");
        ExternalValueAttribute externalValueAttribute = new ExternalValueAttribute("ifSpeed", "100000000");

        Set<OnmsAttribute> attributes = new HashSet<OnmsAttribute>();
        attributes.add(rrdAttribute);
        attributes.add(externalValueAttribute);

        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("Node One");
        
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsResource resource = new OnmsResource(node.getId().toString(), node.getLabel(), resourceType, attributes);

        ReportDefinition def = createReportDefinition();
        def.getReport().getPackage().setFilter("");
        def.setResourceAttributeKey(externalValueAttribute.getName());
        def.setResourceAttributeValueMatch(externalValueAttribute.getValue());
        ReportInstance report = def.createReport(m_resourceDao, m_rrdDao, m_filterDao);

        m_filterDao.walkMatchingNodes(EasyMock.eq(""), EasyMock.isA(EntityVisitor.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                ((EntityVisitor) EasyMock.getCurrentArguments()[1]).visitNode(node);
                return null;
            }
        });
        
        EasyMock.expect(m_resourceDao.getResourceForNode(node)).andReturn(resource);

        EasyMock.expect(m_rrdDao.getPrintValue(rrdAttribute, def.getConsolidationFunction(), report.getStartTime(), report.getEndTime())).andReturn(1.0);

        m_mocks.replayAll();
        
        report.walk();
        
        assertEquals("results size", 1, report.getResults().size());
    }

    private ReportDefinition createReportDefinition() {
        ReportDefinition def;
        def = new ReportDefinition();
        def.setReport(createPackageReport());
        def.setCount(10);
        def.setConsolidationFunction("AVERAGE");
        def.setRelativeTime(RelativeTime.LASTHOUR);
        def.setResourceTypeMatch("interfaceSnmp");
        def.setAttributeMatch("IfInOctets");
        def.setReportClass(BottomNAttributeStatisticVisitor.class);
        def.afterPropertiesSet();
        return def;
    }

    private PackageReport createPackageReport() {
        PackageReport packageReport;
        packageReport = new PackageReport();
        packageReport.setDescription("a package!");
        packageReport.setEnabled(true);
        packageReport.setPackage(new StatsdPackage());
        packageReport.setReport(null);
        packageReport.setRetainInterval(new Long(86400 * 1000));
        packageReport.setSchedule("hmm");
        return packageReport;
    }
}
