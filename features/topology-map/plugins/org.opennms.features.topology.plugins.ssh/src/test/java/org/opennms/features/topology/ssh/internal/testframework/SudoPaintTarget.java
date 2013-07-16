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

package org.opennms.features.topology.ssh.internal.testframework;

import java.util.Map;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.VariableOwner;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class SudoPaintTarget implements PaintTarget {

	public SudoPaintTarget() {
		
	}
	
	@Override
	public void addSection(String sectionTagName, String sectionData)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTag(String tagName) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endTag(String tagName) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, boolean value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, int value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, Resource value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name,
			StreamVariable value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, long value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, float value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, double value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, String value) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String name, Map<?, ?> value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, String value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, int value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, long value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, float value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, double value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, boolean value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVariable(VariableOwner owner, String name, String[] value)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addUploadStreamVariable(VariableOwner owner, String name)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addXMLSection(String sectionTagName, String sectionData,
			String namespace) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addUIDL(String uidl) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addText(String text) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCharacterData(String text) throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAttribute(String string, Object[] keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTag(ClientConnector paintable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFullRepaint() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAttribute(String name, Component value)
			throws PaintException {
		// TODO Auto-generated method stub
	}

	@Override
	public void addVariable(VariableOwner owner, String name, Component value)
			throws PaintException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endPaintable(Component paintable) throws PaintException {
		// TODO Auto-generated method stub
	}

	@Override
	public PaintStatus startPaintable(Component paintable, String tag)
			throws PaintException {
		// TODO Auto-generated method stub
		return null;
	}

}
