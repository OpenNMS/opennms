/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer;

/**
 * <p>PaletteItem class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class PaletteItem {
	
	/** Constant <code>SPACER</code> */
	public static final PaletteItem SPACER = new PaletteItem(null, null, true);
	
	private String m_label;
	private String m_id;
	private boolean m_spacer = false;
	
	/**
	 * <p>Constructor for PaletteItem.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 * @param spacer a boolean.
	 */
	protected PaletteItem(String id, String label, boolean spacer) {
		m_id = id;
		m_label = label;
		m_spacer = spacer;
	}
	
	/**
	 * <p>Constructor for PaletteItem.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public PaletteItem(String id, String label) {
		this(id, label, false);
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
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * <p>isSpacer</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSpacer() {
		return m_spacer;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		if (isSpacer()) {
			return "SPACER";
		}
		else {
			return m_label+"<"+m_id+">";
		}
	}
	
}
