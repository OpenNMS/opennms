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

import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class TestClassRunnerForSelenium extends BlockJUnit4ClassRunner{
    
    private int m_timeout;
    private String m_baseUrl;
    
    TestClassRunnerForSelenium(Class<?> type, String baseUrl, int timeoutInSeconds) throws InitializationError {
        super(type);
        setBaseUrl(baseUrl);
        setTimeout(timeoutInSeconds);
    }
    
    
    
    @Override
    public Object createTest() throws Exception{
        return getTestClass().getOnlyConstructor().newInstance(getBaseUrl(), getTimeout());
    }
    
    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }



    public int getTimeout() {
        return m_timeout;
    }



    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }



    public String getBaseUrl() {
        return m_baseUrl;
    }



    public void setBaseUrl(String baseUrl) {
        m_baseUrl = baseUrl;
    }
    
}