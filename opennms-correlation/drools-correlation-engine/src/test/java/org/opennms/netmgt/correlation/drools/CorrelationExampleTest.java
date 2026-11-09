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
package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.io.ClassPathResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.utils.KieHelper;
import org.opennms.netmgt.correlation.drools.CorrelationExample.EventBean;
import org.opennms.netmgt.correlation.drools.CorrelationExample.Node;
import org.opennms.netmgt.correlation.drools.CorrelationExample.Outage;

/**
 * Pins the behavior of the rules in CorrelationExample.drl on the chain
 * opennms(1) &lt;- switch(2) &lt;- router1(3) &lt;- switchA(4) &lt;- hostA1(5).
 */
public class CorrelationExampleTest {

    private final Node opennms = new Node(1, "opennms", null);
    private final Node sw = new Node(2, "switch", opennms);
    private final Node router1 = new Node(3, "router1", sw);
    private final Node switchA = new Node(4, "switchA", router1);
    private final Node hostA1 = new Node(5, "hostA1", switchA);

    private KieSession session;

    @Before
    public void setUp() {
        final KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();
        // keeps the 10 s "causeTimeout" duration from firing in real time
        conf.setOption(ClockTypeOption.PSEUDO);
        session = new KieHelper()
                .addResource(new ClassPathResource("CorrelationExample.drl", CorrelationExample.class))
                .build()
                .newKieSession(conf, null);
    }

    @After
    public void tearDown() {
        session.dispose();
    }

    @Test
    public void parentDependencyProposesEachAncestorOnce() {
        final Outage outage = nodeDown(hostA1);
        assertEquals(List.of("1u", "2u", "3u", "4u", "5v"), possibleCauses(outage));
    }

    @Test
    public void ancestorWithOutageIsVerified() {
        nodeDown(switchA);
        final Outage outage = nodeDown(hostA1);
        assertEquals(List.of("1u", "2u", "3u", "4v", "5v"), possibleCauses(outage));
    }

    @Test
    public void parentlessNodeIsItsOwnRootCause() {
        assertEquals(opennms, nodeDown(opennms).getCause());
    }

    @Test
    public void nodeWithParentIsItsOwnRootCause() {
        // "no parent causes possible" (salience 200) runs before parents are proposed
        assertEquals(hostA1, nodeDown(hostA1).getCause());
    }

    @Test
    public void parentlessAncestorWithOutageIsRootCause() {
        nodeDown(opennms);
        assertEquals(opennms, nodeDown(hostA1).getCause());
    }

    private Outage nodeDown(final Node node) {
        session.insert(new EventBean("nodeDown", node));
        session.fireAllRules();
        return session.getObjects(o -> o instanceof Outage && ((Outage) o).getNode() == node).stream()
                .map(Outage.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no Outage for " + node));
    }

    /** Node id plus v(erified)/u(nverified) of each possible cause of the outage, sorted. */
    private List<String> possibleCauses(final Outage outage) {
        return session.getObjects(o -> o instanceof CorrelationExample.PossibleCause
                        && ((CorrelationExample.PossibleCause) o).getOutage() == outage).stream()
                .map(CorrelationExample.PossibleCause.class::cast)
                .map(pc -> pc.getNode().getId() + (pc.isVerified() ? "v" : "u"))
                .sorted()
                .collect(Collectors.toList());
    }
}
