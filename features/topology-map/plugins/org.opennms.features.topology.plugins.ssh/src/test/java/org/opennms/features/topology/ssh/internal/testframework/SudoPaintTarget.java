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

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.terminal.VariableOwner;

@SuppressWarnings("serial")
public class SudoPaintTarget implements PaintTarget{

	public SudoPaintTarget() {
		
	}
	
	@Override
	public void addSection(String sectionTagName, String sectionData)
			throws PaintException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean startTag(Paintable paintable, String tag)
			throws PaintException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Deprecated
	public void paintReference(Paintable paintable, String referenceName)
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
	public void addAttribute(String name, Paintable value)
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
	public void addVariable(VariableOwner owner, String name, Paintable value)
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
	public String getTag(Paintable paintable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFullRepaint() {
		// TODO Auto-generated method stub
		return false;
	}

}
