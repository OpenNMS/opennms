/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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