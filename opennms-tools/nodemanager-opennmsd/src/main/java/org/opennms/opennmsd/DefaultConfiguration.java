/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.opennmsd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class DefaultConfiguration implements Configuration {

    private File m_configFile;
    
    public void setConfigFile(File configFile) {
        m_configFile = configFile;
    }
    
    public void load() throws IOException {
        Assert.notNull(m_configFile, "The configuration file must not be null");
        
        if (!m_configFile.exists()) {
            throw new FileNotFoundException("file "+m_configFile+" does not exist.");
        }
        
    }

    public FilterChain getFilterChain() {
        FilterChainBuilder bldr = new FilterChainBuilder();
        bldr.newFilter();
        bldr.setAction(Filter.ACCEPT);
        return bldr.getChain();
    }
    
	

}
