package org.opennms.secret.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jrobin.core.RrdException;
import org.opennms.secret.model.GraphDefinition;

/**
 * returns a rendered version of the graph definition supplied
 * @author mhuot
 *
 */
public interface GraphRenderer {
	
	
	public  ByteArrayInputStream getPNG(GraphDefinition gdef) throws IOException, RrdException;
	
}
