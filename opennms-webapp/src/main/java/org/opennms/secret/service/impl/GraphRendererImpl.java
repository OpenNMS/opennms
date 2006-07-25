package org.opennms.secret.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import org.jrobin.core.RrdException;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.opennms.secret.model.GraphDataElement;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;
import org.opennms.secret.service.GraphRenderer;


/**
 * This is an implimentation of the GraphRender which uses jrobin 
 * to generate the PNG file from the supplied graph definition
 * @author mhuot
 *
 */
public class GraphRendererImpl implements GraphRenderer {
	
	
	public static final int GIF=1;
	public static final int JPG=2;
	public static final int PNG=3;
	
	//   public static String tmpDir="/tmp/";    // path must end with slash
	
	public ByteArrayInputStream getPNG(GraphDefinition gdef) throws IOException, RrdException {
		RrdGraph graph = getRrdGraph(gdef, "PNG");
		return new ByteArrayInputStream(graph.getRrdGraphInfo().getBytes());
	}

	public ByteArrayInputStream getJPEG(GraphDefinition gdef) throws IOException, RrdException {
		RrdGraph graph = getRrdGraph(gdef, "JPEG");
		return new ByteArrayInputStream(graph.getRrdGraphInfo().getBytes());
	}

	public ByteArrayInputStream getGIF(GraphDefinition gdef) throws IOException, RrdException {
		RrdGraph graph = getRrdGraph(gdef, "GIF");
		return new ByteArrayInputStream(graph.getRrdGraphInfo().getBytes());
	}


	/**
	 * @param gdef
	 * @return
	 * @throws RrdException
	 */
	private RrdGraph getRrdGraph(GraphDefinition gdef, String imageFormat) throws IOException, RrdException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef = getRrdGraphDef(gdef);
                graphDef.setImageFormat(imageFormat);
                graphDef.setImageQuality(1.0f);
		RrdGraph graph = new RrdGraph(graphDef);
		return graph;
	}
	
	
	
	public ByteArrayInputStream getGraphImage( GraphDefinition gdef ) {
		// TODO geneneralise the graph definition to RDD graphdefinition
		/*String reportname,
		 File rrdfile,
		 Date from,
		 Date to,
		 String imagefilename,
		 int imagetype) */
		
		try {
			RrdGraphDef graphDef = getRrdGraphDef(gdef);
                	graphDef.setImageFormat("PNG");
	                graphDef.setImageQuality(1.0f);
			RrdGraph graph = new RrdGraph(graphDef);
			
			return new ByteArrayInputStream(graph.getRrdGraphInfo().getBytes());
			
		} catch (Exception e) {
			System.err.println("ErrorGraphRenderImpl - problem creating graph ");
			e.printStackTrace();
		};
		return null ; 
	}



	/**
	 * @param gdef
	 * @return 
	 * @throws RrdException
	 */
	private RrdGraphDef getRrdGraphDef(GraphDefinition gdef) throws RrdException {
//		RrdGraphDef graphDef = new RrdGraphDef(gdef.getStartTime() / 1000, gdef.getEndTime() / 1000);
	        RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setTimeSpan(1086793506L, 1086879506L);
		graphDef.setTitle(gdef.getGraphTitle());
		
		LinkedList elist = gdef.getGraphDataElements();
		for (Iterator iter = elist.iterator(); iter.hasNext();) {
			GraphDataElement gde = (GraphDataElement) iter.next();
			graphDef.datasource(gde.getUniqueID(), // java.lang.String name,  name - Graph source name.
					gde.getDataSource().getSource(),  // java.lang.String file,  file - Path to RRD file.
					gde.getDataSource().getDataSource(),          // java.lang.String dsName,  dsName - Data source name defined in the RRD file.
			        "AVERAGE" );                //java.lang.String consolFunc  consolFunc - Consolidation function that will be used 
			// to extract data from the RRD file ("AVERAGE", "MIN", "MAX" or "LAST").
			
			if (gde instanceof GraphDataLine ) {
				// add GraphDataLine  to graphDef
				GraphDataLine gdl = (GraphDataLine)gde;
				graphDef.line(gdl.getUniqueID(), gdl.getColor(), gdl.getLegend(), gdl.getLineWidth());
				graphDef.gprint(gdl.getUniqueID(),"MIN","Min = @2@C");
				graphDef.gprint(gdl.getUniqueID(),"MAX","Max = @2@r");
			} 
			else {
				System.err.println("ErrorGraphRenderImpl - Printing other than GraphDataLine not defined");
			}
		}
		return graphDef;
	}
	
}

