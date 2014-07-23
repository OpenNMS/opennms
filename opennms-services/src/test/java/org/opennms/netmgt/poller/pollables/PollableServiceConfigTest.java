package org.opennms.netmgt.poller.pollables;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.scheduler.Timer;

@RunWith(MockitoJUnitRunner.class)
public class PollableServiceConfigTest {
    @Test
    public void testPollableServiceConfig() throws Exception {
        final FilterDao fd = mock(FilterDao.class);
        FilterDaoFactory.setInstance(fd);

        InputStream is = new FileInputStream(new File("src/test/resources/etc/psm-poller-configuration.xml"));
        PollerConfigFactory factory = new PollerConfigFactory(0, is, "localhost", false);
        PollerConfigFactory.setInstance(factory);        
        IOUtils.closeQuietly(is);

        final PollContext context = mock(PollContext.class);
        final PollableNetwork network = new PollableNetwork(context);
        final PollableNode node = network.createNodeIfNecessary(1, "foo");
        final PollableInterface iface = new PollableInterface(node, InetAddressUtils.addr("127.0.0.1"));
        final PollableService svc = new PollableService(iface, "MQ_API_DirectRte_v2");
        final PollOutagesConfig pollOutagesConfig = mock(PollOutagesConfig.class);
        final Package pkg = factory.getPackage("MapQuest");
        final Timer timer = mock(Timer.class);
        final PollableServiceConfig psc = new PollableServiceConfig(svc, factory, pollOutagesConfig, pkg, timer);

        final ServiceMonitor sm = mock(ServiceMonitor.class);
        psc.setServiceMonitor(sm);

        psc.poll();
    }
}