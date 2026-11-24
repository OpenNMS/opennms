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
package org.opennms.netmgt.alarmd.northbounder.syslog;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * NMS-20382: the northbounder filter rules are evaluated as SpEL. They must run in a restricted
 * context so a stored rule cannot execute arbitrary code (type references, constructors, reflection);
 * ordinary property/instance-method filters must still evaluate. A regression to an unrestricted
 * StandardEvaluationContext would make the "blocked" assertions below fail (those expressions would
 * evaluate to true), so this test locks the sandbox in place.
 */
public class SyslogFilterSandboxTest {

    private NorthboundAlarm alarm() {
        final OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setUei("uei.opennms.org/junit/test");
        return new NorthboundAlarm(onmsAlarm);
    }

    private boolean evaluate(final String rule) {
        return new SyslogFilter("test", rule, "localhost").passFilter(alarm());
    }

    /** A legitimate property-based filter must still evaluate normally. */
    @Test
    public void benignPropertyRuleStillWorks() {
        Assert.assertTrue(evaluate("uei == 'uei.opennms.org/junit/test'"));
        Assert.assertFalse(evaluate("uei == 'uei.opennms.org/other'"));
    }

    /** A legitimate instance-method filter must still evaluate (withInstanceMethods()). */
    @Test
    public void benignInstanceMethodRuleStillWorks() {
        Assert.assertTrue(evaluate("uei.startsWith('uei.opennms.org')"));
    }

    /**
     * Each of these is a code-execution vector that evaluates to true under an unrestricted context.
     * Under the sandbox they are rejected, so passFilter swallows the error and returns false.
     * assertFalse therefore means "blocked". If any returns true, the sandbox has regressed.
     */
    @Test
    public void typeReferenceIsBlocked() {
        Assert.assertFalse(evaluate("T(java.lang.System).getProperty('user.dir') != null"));
        Assert.assertFalse(evaluate("T(java.lang.Runtime).getRuntime() != null"));
    }

    @Test
    public void constructorIsBlocked() {
        Assert.assertFalse(evaluate("new java.lang.String('x') == 'x'"));
    }

    @Test
    public void reflectionChainIsBlocked() {
        // getClass() is an instance method, but forName(...) is static -> rejected by the sandbox.
        Assert.assertFalse(evaluate("''.getClass().forName('java.lang.Runtime') != null"));
    }
}
