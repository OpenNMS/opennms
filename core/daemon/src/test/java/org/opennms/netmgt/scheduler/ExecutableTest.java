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
package org.opennms.netmgt.scheduler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutableTest extends Executable {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutableTest.class);

    private final String m_name;

    public ExecutableTest(String name, int priority) {
        super(priority);
        m_name=name;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public void runExecutable() {
        LOG.info("Started: {}", m_name);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Ended: {}", m_name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExecutableTest that = (ExecutableTest) o;

        return Objects.equals(m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecutableTest{" +
                "m_name='" + m_name + '\'' +
                " m_priority=" + getPriority()
                +
                '}';
    }

    @Override
    public boolean isReady() {
        return true;
    }

}

