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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

public class SeleniumJUnitRunner extends Suite{
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface BaseUrl{
        String url();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TimeoutInSeconds{
        int timeout();
    }
    
    private final List<Runner> m_runners = new ArrayList<>();
    
    public SeleniumJUnitRunner(Class<?> testClass) throws InitializationError 
    {
        super(testClass, Collections.<Runner>emptyList());
        m_runners.add(new TestClassRunnerForSelenium( getTestClass().getJavaClass(), getBaseUrlAnnotation( testClass ), getTimeoutAnnotation(testClass) ) );
    }
    

    private int getTimeoutAnnotation(Class<?> testClass) {
        SeleniumJUnitRunner.TimeoutInSeconds timeout = testClass.getAnnotation(SeleniumJUnitRunner.TimeoutInSeconds.class);
        if(timeout == null) {
            return 3;
        }else {
            return timeout.timeout();
        }
    }


    private String getBaseUrlAnnotation(Class<?> klass) {
        SeleniumJUnitRunner.BaseUrl baseUrl = klass.getAnnotation(SeleniumJUnitRunner.BaseUrl.class);
        if(baseUrl == null) {
            return "";
        }else {
            return baseUrl.url();
        }
    }


    @Override
    protected List<Runner> getChildren(){
        return m_runners;
    }
}