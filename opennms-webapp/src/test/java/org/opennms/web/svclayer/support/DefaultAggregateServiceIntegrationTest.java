package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.AggregateStatusColor;
import org.opennms.web.svclayer.AggregateStatusDefinition;
import org.opennms.web.svclayer.AggregateStatusService;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class DefaultAggregateServiceIntegrationTest extends AbstractTransactionalDataSourceSpringContextTests {
    
    private AggregateStatusService m_aggregateService;
    
    
    /**
     * This parm gets autowired from the application context by TDSCT (the base class for this test)
     * pretty cool Spring Framework trickery
     * @param svc
     */
    public void setAggregateStatusService(AggregateStatusService svc) {
        m_aggregateService = svc;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "META-INF/opennms/applicationContext-dao.xml",
                "org/opennms/web/svclayer/applicationContext-svclayer.xml" };
    }
    
    public void testCreateAggregateStatusUsingBuilding() {
        
        Collection<AggregateStatus> aggrStati;
        Collection<AggregateStatusDefinition> defs = new ArrayList<AggregateStatusDefinition>();
        
        AggregateStatusDefinition definition;
        definition = new AggregateStatusDefinition("LB/Router", new ArrayList<String>(Arrays.asList(new String[]{ "DEV_ROUTER", "DEV_LOADBAL" })));
        defs.add(definition);        
        definition = new AggregateStatusDefinition("Access Controller", new ArrayList<String>(Arrays.asList(new String[]{ "DEV_AC" })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Switches", new ArrayList<String>(Arrays.asList(new String[]{ "DEV_SWITCH" })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("Access Points", new ArrayList<String>(Arrays.asList(new String[]{ "DEV_AP" })));
        defs.add(definition);
        definition = new AggregateStatusDefinition("BCPC", new ArrayList<String>(Arrays.asList(new String[]{ "DEV_BCPC" })));
        defs.add(definition);
        
        String assetColumn = "building";
        String buildingName = "HAT102706";
        
        aggrStati = m_aggregateService.createAggregateStatusUsingAssetColumn(assetColumn, buildingName, defs);
        
        AggregateStatus status;
        status = (AggregateStatus)((ArrayList)aggrStati).get(0);
        assertEquals(status.getColor(), AggregateStatusColor.NODES_ARE_DOWN);
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(1);
        assertEquals(status.getColor(), AggregateStatusColor.ALL_NODES_UP);
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(2);
        assertEquals(status.getColor(), AggregateStatusColor.NODES_ARE_DOWN);

        status = (AggregateStatus)((ArrayList)aggrStati).get(3);
        assertEquals(status.getColor(), AggregateStatusColor.NODES_ARE_DOWN);
        assertEquals(new Integer(6), status.getDownEntityCount());
        
        status = (AggregateStatus)((ArrayList)aggrStati).get(4);
        assertEquals(status.getColor(), AggregateStatusColor.ALL_NODES_UP);


    }

}
