package org.opennms.web.rest;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.Source;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Contains tests that can run irrespective of the fetch strategy.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public abstract class MeasurementsRestServiceTest {

    @Autowired
    protected MeasurementsRestService m_svc;

    @Autowired
    protected DefaultResourceDao m_resourceDao;

    @Autowired
    protected NodeDao m_nodeDao;

    public void setUp() {
        BeanUtils.assertAutowiring(this);

        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");
        m_nodeDao.save(node);
        m_nodeDao.flush();
    }

    @Test(expected=WebApplicationException.class)
    public void notFoundOnMissingResource() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);

        Source source = new Source();
        source.setResourceId("node[99].interfaceSnmp[eth0-04013f75f101]");
        source.setAttribute("ifInOctets");
        source.setAggregation("AVERAGE");
        source.setLabel("octetsIn");
        request.setSources(Lists.newArrayList(source));

        m_svc.query(request);
    }

    @Test(expected=WebApplicationException.class)
    public void notFoundOnMissingAttribute() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);

        Source source = new Source();
        source.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        source.setAttribute("n0tIfInOctets");
        source.setAggregation("AVERAGE");
        source.setLabel("octetsIn");
        request.setSources(Lists.newArrayList(source));

        m_svc.query(request);
    }
}
