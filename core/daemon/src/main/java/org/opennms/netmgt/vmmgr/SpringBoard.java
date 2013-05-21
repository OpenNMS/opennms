/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vmmgr;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringBoard implements SpringBoardMBean {
    
    private File contextDir;
    private FileSystemXmlApplicationContext m_context;
    
    /**
     * <p>Getter for the field <code>contextDir</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getContextDir() {
        return (contextDir == null ? null : contextDir.getAbsolutePath());
    }

    /** {@inheritDoc} */
    @Override
    public void setContextDir(String contextDir) {
        // TODO Auto-generated method stub
        
    }

    /**
     * <p>start</p>
     */
    @Override
    public void start() {
        String appContext = System.getProperty("opennms.appcontext", "opennms-appContext.xml");
        File contextFile = new File(contextDir, appContext);
        System.err.println(contextFile.getPath());
        m_context = new FileSystemXmlApplicationContext(contextFile.getPath());
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> status() {
        return Collections.singletonList(m_context.toString());
    }

    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        m_context.close();
    }


}
