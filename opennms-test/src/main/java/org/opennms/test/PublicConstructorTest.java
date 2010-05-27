/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Test;

/**
 * This JUnit test simply checks for public constructors on the list of classes
 * that are returned by the {@link #getClasses()} method.
 * 
 * @author Seth
 */
public abstract class PublicConstructorTest {
	@Test
	public void testPublicConstructors() throws NoSuchMethodException, Exception {
		for (Class<? extends Object> clazz : getClasses()) {
			try {
				Constructor<? extends Object> constructor = clazz.getConstructor();
				assertNotNull(constructor);
				System.out.println("Found public constructor on class: " + clazz.getName());
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	/**
	 * This method should return a list of classes that you wish to inspect for public
	 * constructors.
	 */
	protected abstract List<Class<? extends Object>> getClasses() throws Exception;
}
