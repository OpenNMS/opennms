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

package org.opennms.m2e.castor.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;

public class CastorGenerationTest
    extends AbstractMavenProjectTestCase
{
    public void test_p001_simple()
        throws Exception
    {
        ResolverConfiguration configuration = new ResolverConfiguration();
        IProject project1 = importProject( "projects/castor/castor-p001/pom.xml", configuration );
        waitForJobsToComplete();

        project1.build( IncrementalProjectBuilder.FULL_BUILD, monitor );
        project1.build( IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor );
        waitForJobsToComplete();

        assertNoErrors( project1 );

        IJavaProject javaProject1 = JavaCore.create( project1 );
        IClasspathEntry[] cp1 = javaProject1.getRawClasspath();

        assertEquals( new Path( "/castor-p001/target/generated-sources/castor" ), cp1[3].getPath() );

        assertTrue( project1.getFile( "target/generated-sources/castor/test/Tags.java" ).isSynchronized( IResource.DEPTH_ZERO ) );
        assertTrue( project1.getFile( "target/generated-sources/castor/test/Tags.java" ).isAccessible() );
    }

    public void test_p002_simple()
            throws Exception
        {
            ResolverConfiguration configuration = new ResolverConfiguration();
            IProject project1 = importProject( "projects/castor/castor-p002/pom.xml", configuration );
            waitForJobsToComplete();

            project1.build( IncrementalProjectBuilder.FULL_BUILD, monitor );
            project1.build( IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor );
            waitForJobsToComplete();

            assertNoErrors( project1 );

            IJavaProject javaProject1 = JavaCore.create( project1 );
            IClasspathEntry[] cp1 = javaProject1.getRawClasspath();

            assertEquals( new Path( "/castor-p002/target/generated-sources/castor" ), cp1[3].getPath() );

            assertTrue( project1.getFile( "target/generated-sources/castor/test/Tags.java" ).isSynchronized( IResource.DEPTH_ZERO ) );
            assertTrue( project1.getFile( "target/generated-sources/castor/test/Tags.java" ).isAccessible() );
        }

}
