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
package org.opennms.features.jmxconfiggenerator.jmxconfig;

import org.junit.Ignore;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
@Ignore("this is used by other tests, but is not a test itself")
public class JmxTestDummy implements JmxTestDummyMBean {

    private int writable = 0;

    @Override
    public String getName() {
        return "JmxTest";
    }

    @Override
    public int getX() {
        return 42;
    }

    @Override
    public Integer getInteger() {
        return getX();
    }

    @Override
    public Long getLong() {
        return Long.valueOf(getX());
    }

    @Override
    public void setWritableY(int writable) {
        this.writable = writable;
    }

    @Override
    public int getWritableY() {
        return writable;
    }
}
