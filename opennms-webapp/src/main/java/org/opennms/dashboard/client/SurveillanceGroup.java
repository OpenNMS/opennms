/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>SurveillanceGroup class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceGroup extends SurveillanceSet implements IsSerializable {
    
    private String m_label;
    private String m_id;
    private boolean m_column;
    
    /**
     * <p>Constructor for SurveillanceGroup.</p>
     */
    public SurveillanceGroup() {
        this(null, null, false);
    }
    
    /**
     * <p>Constructor for SurveillanceGroup.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param isColumn a boolean.
     */
    public SurveillanceGroup(String id, String label, boolean isColumn) {
        m_id = id;
        m_label = label;
        m_column = isColumn;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return m_id;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * <p>isColumn</p>
     *
     * @return a boolean.
     */
    public boolean isColumn() {
        return m_column;
    }

    /**
     * <p>setColumn</p>
     *
     * @param isColumn a boolean.
     */
    public void setColumn(boolean isColumn) {
        m_column = isColumn;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_label;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(Visitor v) {
        v.visitGroup(this);
    }
}
