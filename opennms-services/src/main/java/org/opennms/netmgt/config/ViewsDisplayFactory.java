/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
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
        InputStream stream = null;
        try {
            stream = getStream();
            unmarshal(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    private void unmarshal(InputStream stream) throws MarshalException, ValidationException {
        m_viewInfo = CastorUtils.unmarshal(Viewinfo.class, stream);
        updateViewsMap();
    }
    
    private void updateViewsMap() {
        Map<String, View> viewsMap = new HashMap<String,View>();

        for (View view : m_viewInfo.getViewCollection()) {
            viewsMap.put(view.getViewName(), view);
        }
        
        m_viewsMap = viewsMap;
    }
    
    private InputStream getStream() throws IOException {
        File viewsDisplayFile = getViewsDisplayFile();
        m_lastModified = viewsDisplayFile.lastModified();
        InputStream stream = new FileInputStream(viewsDisplayFile);
        return stream;
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
