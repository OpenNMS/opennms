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
package org.opennms.netmgt.junit.runner;

import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class SeleniumComputer extends Computer{
    private String m_baseUrl = "";
    private int m_timeout = 3;
    
    public SeleniumComputer() {
        
    }
    
    public SeleniumComputer(String baseUrl) {
        this(baseUrl, 3);
    }
    
    public SeleniumComputer(String baseUrl, int timeoutInSeconds) {
        setBaseUrl(baseUrl);
        setTimeout(timeoutInSeconds);
    }
    
    @Override
    protected Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        return new TestClassRunnerForSelenium(testClass, getBaseUrl(), getTimeout());
    }

    public String getBaseUrl() {
        return m_baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        m_baseUrl = baseUrl;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    
}