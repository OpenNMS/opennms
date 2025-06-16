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
package org.opennms.netmgt.poller.shell;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.daemon.DaemonReloadEnum;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(scope = "opennms", name = "poller-benchmark", description = "Benchmark pollerd by creating N nodes/services and waiting for them to be polled.")
@Service
public class Benchmark implements Action {

    @Reference
    private SessionUtils sessionUtils;
    @Reference
    private MonitoringLocationDao monitoringLocationDao;
    @Reference
    private NodeDao nodeDao;
    @Reference
    private IpInterfaceDao ipInterfaceDao;
    @Reference
    private IpInterfaceDao ipIfaceDao;
    @Reference
    private MonitoredServiceDao monitoredServiceDao;
    @Reference
    private ServiceTypeDao serviceTypeDao;
    @Reference
    private EventForwarder eventForwarder;

    private final int batchSize = 100;

    @Option(name = "-n", aliases = "--nodes", description = "Number of nodes and services to create. A single service per interface per node is created.")
    int numNodes = 100;

    @Option(name = "-r", aliases = "--recycle", description = "Recycle nodes created by a previous run instead of deleting/recreating them.")
    boolean recycleNetwork = false;

    private final Set<Integer> allMonitoredServiceIds = new HashSet<>();

    @Override
    public Object execute() {
        System.out.println(new Date() + ": Benchmarking started...");

        long startOfPoll;
        if (!recycleNetwork) {
            System.out.println(new Date() + ": Destroying old network...");
            List<Event> eventsToSend = destroyOldNetwork();
            System.out.println(new Date() + ": Notifying pollerd of deleted services...");
            for (Event e : eventsToSend) {
                eventForwarder.sendNow(e);
            }
            System.out.println(new Date() + ": Building new network...");
            eventsToSend = buildNewNetwork();
            System.out.println(new Date() + ": Notifying pollerd of new services...");
            // Start the timeout after the entities have been pushed to the DB and *before* we send the events
            startOfPoll = System.currentTimeMillis();
            for (Event e : eventsToSend) {
                eventForwarder.sendNow(e);
            }
        } else {
            System.out.println(new Date() + ": Recycling network from previous run...");
            recycleNetwork();
            numNodes = allMonitoredServiceIds.size();
            System.out.printf("%s: Tracking %d services\n", new Date(), numNodes);
            startOfPoll = System.currentTimeMillis();
        }

        System.out.println(new Date() + ": Waiting for services to be polled...");
        waitForServices(startOfPoll);
        long endOfPoll = System.currentTimeMillis();
        System.out.println(new Date() + ": Benchmarking done.");
        System.out.println(new Date() + ": Services polled in: " + getDurationBreakdown(endOfPoll - startOfPoll));

        return null;
    }

    private void waitForServices(long startOfPoll) {
        Date lastPollLimit = new Date(startOfPoll);
        long totalNumServices = allMonitoredServiceIds.size();
        while(true) {
            long numServicesMatching = sessionUtils.withTransaction(() -> {
                final Criteria criteria = criteriaForBenchmarkServices();
                criteria.addRestriction(new AnyRestriction(
                        new GeRestriction("lastGood", lastPollLimit),
                        new GeRestriction("lastFail", lastPollLimit)));
                return monitoredServiceDao.countMatching(criteria);
            });

            if (numServicesMatching >= totalNumServices) {
                System.out.printf("All %d services have been polled.\n", totalNumServices);
                break;
            } else {
                System.out.printf("Still waiting for %d (out of %d) services...\n", (totalNumServices - numServicesMatching), totalNumServices);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted. Aborting.");
                    Thread.interrupted();
                    break;
                }
            }
        }
    }

    private Criteria criteriaForBenchmarkServices() {
        final Criteria criteria = new Criteria(OnmsMonitoredService.class);
        criteria.addRestriction(new InRestriction("status", Arrays.asList("A", "N")));
        criteria.setAliases(Arrays.asList(new Alias("ipInterface", "ipInterface", Alias.JoinType.LEFT_JOIN),
                new Alias("ipInterface.node", "node", Alias.JoinType.LEFT_JOIN)));
        criteria.addRestriction(new EqRestriction("node.operatingSystem", Benchmark.class.getCanonicalName()));
        return criteria;
    }

    private void recycleNetwork() {
        sessionUtils.withTransaction(() -> {
            final Criteria criteria = criteriaForBenchmarkServices();
            for (OnmsMonitoredService svc : monitoredServiceDao.findMatching(criteria)) {
                allMonitoredServiceIds.add(svc.getId());
            }
        });
    }

    private List<Event> destroyOldNetwork() {
        List<Event> eventsToSend = new LinkedList<>();

        AtomicBoolean nodesRemaining = new AtomicBoolean(true);
        while (nodesRemaining.get()) {
            sessionUtils.withTransaction(() -> {
                final Criteria criteria = new Criteria(OnmsNode.class);
                criteria.addRestriction(new EqRestriction("operatingSystem", Benchmark.class.getCanonicalName()));
                criteria.setLimit(batchSize);
                List<OnmsNode> nodes = nodeDao.findMatching(criteria);
                nodes.forEach(node -> {
                    nodeDao.delete(node);

                    EventBuilder eventBuilder = new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, Benchmark.class.getCanonicalName())
                            .setNode(node);
                    eventBuilder.addParam(EventConstants.PARM_NODE_LABEL, node.getLabel());
                    eventsToSend.add(eventBuilder.getEvent());
                });
                nodesRemaining.set(!nodes.isEmpty());
            });
        }

        return eventsToSend;
    }

    private List<Event> buildNewNetwork() {
        List<Event> eventsToSend = new LinkedList<>();
        InetAddress loopbackAddress = InetAddressUtils.addr("127.0.0.1");
        final var icmpServiceType = serviceTypeDao.findByName("ICMP");

        for (int i = 0; i < numNodes; i++) {
            final int k = i;
            sessionUtils.withTransaction(() -> {
                final var node = new OnmsNode();
                node.setLabel("bench-n" + k);
                node.setLocation(this.monitoringLocationDao.getDefaultLocation());
                node.setOperatingSystem(Benchmark.class.getCanonicalName());
                nodeDao.saveOrUpdate(node);

                final var iface = new OnmsIpInterface();
                iface.setNode(node);
                iface.setIpAddress(loopbackAddress);
                node.addIpInterface(iface);
                ipIfaceDao.saveOrUpdate(iface);

                final var svcICMP = new OnmsMonitoredService();
                svcICMP.setIpInterface(iface);
                svcICMP.setServiceType(icmpServiceType);
                svcICMP.setStatus("A");
                iface.addMonitoredService(svcICMP);
                monitoredServiceDao.saveOrUpdate(svcICMP);
                allMonitoredServiceIds.add(svcICMP.getId());

                EventBuilder eventBuilder = new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "benchmark")
                        .setNode(node)
                        .setInterface(iface.getIpAddress())
                        .setService(svcICMP.getServiceName());
                eventBuilder.addParam(EventConstants.PARM_NODE_LABEL, node.getLabel());
                eventsToSend.add(eventBuilder.getEvent());
            });

            if ((i+1) % batchSize == 0) {
                System.out.printf("Created %d/%d nodes\n", (i+1), numNodes);
            }
        }
        return eventsToSend;
    }

    /**
     * Pulled form https://stackoverflow.com/a/7663966
     * Convert a millisecond duration to a string format
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }


}
