//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Sep 10: Support unit/integration testing. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.netmgt.config.viewsdisplay.Viewinfo;

/**
 * <p>ViewsDisplayFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ViewsDisplayFactory {
    /** The singleton instance. */
    private static ViewsDisplayFactory m_instance;

    /** File path of groups.xml. */
    protected File m_viewsDisplayFile;

    /** Boolean indicating if the init() method has been called. */
    protected boolean initialized = false;

    /** Timestamp of the viewDisplay file, used to know when to reload from disk. */
    protected long m_lastModified;

    /** Map of view objects by name. */
    protected Map<String,View> m_viewsMap;

    private Viewinfo m_viewInfo;

    /**
     * Empty private constructor so this class cannot be instantiated outside
     * itself.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws ValidationException 
     * @throws MarshalException 
     */
    private ViewsDisplayFactory() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        reload();
    }

    /**
     * <p>Constructor for ViewsDisplayFactory.</p>
     *
     * @param file a {@link java.lang.String} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public ViewsDisplayFactory(String file) throws MarshalException, ValidationException, FileNotFoundException, IOException {
        setViewsDisplayFile(new File(file));
        reload();
    }

    /**
     * Be sure to call this method before calling getInstance().
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (m_instance == null) {
            setInstance(new ViewsDisplayFactory());
        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * ViewsDisplayFactory
     *
     * @return the single views display factory instance
     * @throws java.lang.IllegalStateException
     *             if init has not been called
     */
    public static synchronized ViewsDisplayFactory getInstance() {
        if (m_instance == null) {
            throw new IllegalStateException("You must call ViewDisplay.init() before calling getInstance().");
        }

        return m_instance;
    }

    /**
     * Parses the viewsdisplay.xml via the Castor classes
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        Reader reader = getReader();
        try {
            unmarshal(reader);
        } finally {
            reader.close();
        }
    }
    
    private void unmarshal(Reader reader) throws MarshalException, ValidationException {
        m_viewInfo = (Viewinfo) Unmarshaller.unmarshal(Viewinfo.class, reader);
        Map<String, View> viewsMap = new HashMap<String,View>();

        Collection viewList = m_viewInfo.getViewCollection();
        Iterator i = viewList.iterator();

        while (i.hasNext()) {
            View view = (View) i.next();
            viewsMap.put(view.getViewName(), view);
        }
        
        m_viewsMap = viewsMap;
    }
    
    private Reader getReader() throws IOException, FileNotFoundException {
        File viewsDisplayFile = getViewsDisplayFile();

        Reader reader = new FileReader(viewsDisplayFile);
        m_lastModified = m_viewsDisplayFile.lastModified();
        return reader;
    }
    
    /**
     * <p>setViewsDisplayFile</p>
     *
     * @param viewsDisplayFile a {@link java.io.File} object.
     */
    public void setViewsDisplayFile(File viewsDisplayFile) {
        m_viewsDisplayFile = viewsDisplayFile;
    }

    /**
     * <p>getViewsDisplayFile</p>
     *
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public File getViewsDisplayFile() throws IOException {
        if (m_viewsDisplayFile == null) {
            m_viewsDisplayFile = ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_DISPLAY_CONF_FILE_NAME);
        }
        return m_viewsDisplayFile;
    }

    /**
     * Can be null
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.viewsdisplay.View} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public View getView(String viewName) throws IOException, MarshalException, ValidationException {
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        updateFromFile();

        View view = m_viewsMap.get(viewName);

        return view;
    }
    
    /**
     * <p>getDefaultView</p>
     *
     * @return a {@link org.opennms.netmgt.config.viewsdisplay.View} object.
     */
    public View getDefaultView() {
        return m_viewsMap.get(m_viewInfo.getDefaultView());
    }

    /**
     * Reload the viewsdisplay.xml file if it has been changed since we last
     * read it.
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected void updateFromFile() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_viewsDisplayFile.lastModified()) {
            reload();
        }
    }

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.ViewsDisplayFactory} object.
     */
    public static void setInstance(ViewsDisplayFactory instance) {
        m_instance = instance;
        m_instance.initialized = true;
    }

    /**
     * <p>getDisconnectTimeout</p>
     *
     * @return a int.
     */
    public int getDisconnectTimeout() {
        return m_viewInfo.getDisconnectTimeout();
    }
}
