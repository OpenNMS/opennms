/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.opennms.m2e.castor.internal;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

public class CastorBuildParticipant
    extends MojoExecutionBuildParticipant
{

    public CastorBuildParticipant( MojoExecution execution )
    {
        super( execution, true );
    }

    @Override
    public Set<IProject> build( int kind, IProgressMonitor monitor )
        throws Exception
    {
        IMaven maven = MavenPlugin.getMaven();
        BuildContext buildContext = getBuildContext();

        // check if any of the grammar files changed
        File source = maven.getMojoParameterValue(getSession(), getMojoExecution(), "schemaDirectory", File.class);
        Scanner ds = buildContext.newScanner( source ); // delta or full scanner
        ds.scan();
        String[] includedFiles = ds.getIncludedFiles();
        if (includedFiles == null || includedFiles.length <= 0 )
        {
            return null;
        }

        // execute mojo
        Set<IProject> result = super.build( kind, monitor );

        // tell m2e builder to refresh generated files
        File generated = maven.getMojoParameterValue(getSession(), getMojoExecution(), "dest", File.class);
        if (generated != null) {
            buildContext.refresh( generated );
        }

        return result;
    }
}
