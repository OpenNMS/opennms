package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.support.PropertiesGraphDao.PrefabGraphTypeDao;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.PrefabGraph;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.ObjectRetrievalFailureException;

public class PropertiesGraphDaoIT extends PropertiesGraphDaoTestCase {

	@Test
	public void testPrefabPropertiesReload() throws Exception {
	    File f = m_fileAnticipator.tempFile("snmp-graph.properties");
	    
	    m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    // Don't include mib2.discards in the reports line
	    String noDiscards = s_prefab.replace(", mib2.discards", "");
	    m_writer.write(noDiscards);
	    m_writer.close();
	    m_outputStream.close();
	    
	    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
	    perfConfig.put("performance", new FileSystemResource(f));
	    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
	    PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("performance");
	    assertNotNull("could not get performance prefab graph type", type);
	    
	    assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
	    assertNull("could get mib2.discards report, but shouldn't have been able to", type.getQuery("mib2.discards"));
	
	    /*
	     *  On UNIX, the resolution of the last modified time is 1 second,
	     *  so we need to wait at least that long before rewriting the
	     *  file to ensure that we have crossed over into the next second.
	     *  At least we're not crossing over with John Edward.
	     *  
	     *  This also happens to be long enough for 
	     *  FileReloadContainer.DEFAULT_RELOAD_CHECK_INTERVAL
	     *  to pass by.
	     */
	    Thread.sleep(1100);
	
	    m_outputStream = new FileOutputStream(f);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_prefab);
	    m_writer.close();
	    m_outputStream.close();
	    
	    type = dao.findPrefabGraphTypeDaoByName("performance");
	    assertNotNull("could not get performance prefab graph type after rewriting config file", type);
	    assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
	    assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
	}

	@Test
	public void testPrefabPropertiesReloadBad() throws Exception {
	    MockLogAppender.setupLogging(false, "DEBUG");
	    m_testSpecificLoggingTest = true;
	
	    File f = m_fileAnticipator.tempFile("snmp-graph.properties");
	
	    m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_prefab);
	    m_writer.close();
	    m_outputStream.close();
	
	    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
	    perfConfig.put("performance", new FileSystemResource(f));
	    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
	    PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("performance");
	    assertNotNull("could not get performance prefab graph type", type);
	    
	    assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
	    assertNotNull("could not get mib2.discards report", type.getQuery("mib2.discards"));
	
	    Thread.sleep(1100);
	
	    m_outputStream = new FileOutputStream(f);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    // Don't include the reports line at all so we get an error
	    String noReports = s_prefab.replace("reports=mib2.HCbits, mib2.bits, mib2.discards", "");
	    m_writer.write(noReports);
	    m_writer.close();
	    m_outputStream.close();
	    
	    type = dao.findPrefabGraphTypeDaoByName("performance");
	
	    assertNotNull("could not get performance prefab graph type after rewriting config file", type);
	    assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
	    assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
	    
	    MockLogAppender.assertLogMatched(Level.ERROR, "Could not reload configuration");
	}

	@Test
	public void testAdhocPropertiesReload() throws Exception {
	    File f = m_fileAnticipator.tempFile("snmp-adhoc-graph.properties");
	    
	    m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    // Set the image type to image/cheeesy
	    String cheesy = s_adhoc.replace("image/png", "image/cheesy");
	    m_writer.write(cheesy);
	    m_writer.close();
	    m_outputStream.close();
	    
	    HashMap<String, Resource> adhocConfig = new HashMap<String, Resource>();
	    adhocConfig.put("performance", new FileSystemResource(f));
	    PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, adhocConfig);
	    AdhocGraphType type = dao.findAdhocGraphTypeByName("performance");
	    assertNotNull("could not get performance adhoc graph type", type);
	    assertEquals("image type isn't correct", "image/cheesy", type.getOutputMimeType());
	
	    Thread.sleep(1100);
	
	    m_outputStream = new FileOutputStream(f);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_adhoc);
	    m_writer.close();
	    m_outputStream.close();
	    
	    type = dao.findAdhocGraphTypeByName("performance");
	    assertNotNull("could not get performance adhoc graph type", type);
	    assertEquals("image type isn't correct", "image/png", type.getOutputMimeType());
	}

	/**
	 * Test that an included single report per file properties config can override
	 * a report in the main properties file. 
	 * @throws IOException
	 */
	@Test
	public void testPrefabConfigDirectorySingleReportOverride()
			throws Exception {
			    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
			    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
			    
			    File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
			    File graphHCbits = m_fileAnticipator.tempFile(graphDirectory, "mib2.HCbits.properties");
			                
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_mib2bitsBasePrefab);
			    m_writer.close();
			    m_outputStream.close();
			                
			    graphDirectory.mkdir();
			    m_outputStream = new FileOutputStream(graphBits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateBitsGraph);
			    m_writer.close();
			    m_outputStream.close();
			    
			    m_outputStream = new FileOutputStream(graphHCbits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateHCBitsGraph);
			    m_writer.close();
			    m_outputStream.close();
			
			    HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
			    prefabConfigs.put("performance", new FileSystemResource(rootFile));
			
			    PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
			    
			    PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
			    assertNotNull(mib2Bits);
			    //The base properties file (s_mib2bitsBasePrefab) has the name=Wrong Name, and columns=wrongColumn1,wrongColumn2.
			    // We check that the overridden graph has the correct details in it
			    assertEquals("mib2.bits", mib2Bits.getName());
			    assertEquals("Bits In/Out", mib2Bits.getTitle());
			    String columns1[] = {"ifInOctets","ifOutOctets"};
			    Assert.assertArrayEquals(columns1, mib2Bits.getColumns());
			
			    PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
			    assertNotNull(mib2HCBits);
			    assertEquals("mib2.HCbits", mib2HCBits.getName());
			    assertEquals("Bits In/Out", mib2HCBits.getTitle());
			    String columns2[] = {"ifHCInOctets","ifHCOutOctets"};
			    Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
			    
			    //Now, having proven that the override works, rewrite the base file with the same data, thus updating the timestamp
			    // and forcing a reload.  The mib2.bits graph should still be the correct overridden one.  
			
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_mib2bitsBasePrefab);
			    m_writer.close();
			    m_outputStream.close();
			                
			    //Wait long enough to make the FileReloadContainers do their thing reliably
			    Thread.sleep(1100);
			
			    //Ensure that the override still applies and hasn't been "underridden" by the rewrite of the base file.
			    mib2Bits = dao.getPrefabGraph("mib2.bits");
			    assertNotNull(mib2Bits);
			    assertEquals("mib2.bits", mib2Bits.getName());
			    assertEquals("Bits In/Out", mib2Bits.getTitle());
			    String columns3[] = {"ifInOctets","ifOutOctets"};
			    Assert.assertArrayEquals(columns3, mib2Bits.getColumns());
			}

	@Test
	public void testPrefabPropertiesIncludeDirectoryReloadSingleReports()
			throws Exception {
			    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
			    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
			    File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
			
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_baseIncludePrefab);
			    m_writer.close();
			    m_outputStream.close();
			    
			    graphDirectory.mkdir();
			    m_outputStream = new FileOutputStream(graphBits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateBitsGraph);
			    m_writer.close();
			    m_outputStream.close();
			    
			    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
			    perfConfig.put("performance", new FileSystemResource(rootFile));
			    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
			                
			    PrefabGraph graph = dao.getPrefabGraph("mib2.bits");
			    assertNotNull("could not get mib2.bits report", graph);
			    assertEquals("ifSpeed", graph.getExternalValues()[0]);
			
			    Thread.sleep(1100);
			
			    m_outputStream = new FileOutputStream(graphBits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateBitsGraph.replace("ifSpeed", "anotherExternalValue"));
			    m_writer.close();
			    m_outputStream.close();
			    
			    graph = dao.getPrefabGraph("mib2.bits");
			    assertNotNull("could not get mib2.bits report after rewriting config file", graph);
			    assertEquals("anotherExternalValue", graph.getExternalValues()[0]);
			}

	/**
	 * Test that reloading a badly formatted single report doens't overwrite a previously functioning
	 * report.  
	 * 
	 * NB: It should still complain with an Error log.  Should there be an event as well?
	 * @throws Exception
	 */
	@Test
	public void testPrefabPropertiesIncludeDirectoryBadReloadSingleReport()
			throws Exception {
			    //We're expecting an ERROR log, and will be most disappointed if
			    // we don't get it.  Turn off the default check in runTest
			    m_testSpecificLoggingTest = true;
			    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
			    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
			    File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
			
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_baseIncludePrefab);
			    m_writer.close();
			    m_outputStream.close();
			    
			    graphDirectory.mkdir();
			    m_outputStream = new FileOutputStream(graphBits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateBitsGraph);
			    m_writer.close();
			    m_outputStream.close();
			       
			    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
			    perfConfig.put("performance", new FileSystemResource(rootFile));
			    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
			    
			    
			    PrefabGraph graph = dao.getPrefabGraph("mib2.bits");
			    assertNotNull("could not get mib2.bits report", graph);
			    assertEquals("ifSpeed", graph.getExternalValues()[0]);
			
			    Thread.sleep(1100);
			
			    m_outputStream = new FileOutputStream(graphBits);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    //Two changes:
			    // 1) Remove a required property; this should break the reading of the new file
			    // 2) Change the externalvalues attribute name; we shouldn't see that new name after the reload
			    m_writer.write(s_separateBitsGraph.replace("report.name", "report.fluggle").replace("ifSpeed", "anotherExternalValue"));
			    m_writer.close();
			    m_outputStream.close();
			    
			    graph = dao.getPrefabGraph("mib2.bits");
			    assertNotNull("could not get mib2.bits report after rewriting config file", graph);
			    assertEquals("ifSpeed", graph.getExternalValues()[0]);
			    
			}

	/**
	 * Test that adding a "include.directory" property to the main graph config file
	 * will cause included files to be read on reload of the main config file
	 * (early code didn't do this right)
	 * @throws Exception
	 */
	@Test
	public void testAddingIncludeDirectory() throws Exception {
	    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
	
	    m_outputStream = new FileOutputStream(rootFile);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_prefab);
	    m_writer.close();
	    m_outputStream.close();
	    
	    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
	    perfConfig.put("performance", new FileSystemResource(rootFile));
	    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
	
	    assertNotNull(dao.getPrefabGraph("mib2.bits"));
	    try {
	        PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
	        fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
	    } catch (ObjectRetrievalFailureException e) {
	        
	    }
	    
	    //Wait long enough to make the FileReloadContainers do their thing reliably
	    Thread.sleep(1100);
	
	    //Now create the new graph in a sub-directory, and rewrite the rootFile with an include.directory property 
	    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
	    File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
	    
	    graphDirectory.mkdir();
	    m_outputStream = new FileOutputStream(graphErrors);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_separateErrorsGraph);
	    m_writer.close();
	    m_outputStream.close();
	    
	    m_outputStream = new FileOutputStream(rootFile);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_prefab.replace("output.mime", "include.directory=snmp-graph.properties.d\n" +
	    		"output.mime"));
	    m_writer.close();
	    m_outputStream.close();
	    
	    assertNotNull(dao.getPrefabGraph("mib2.bits")); //Just checking the reload didn't lose existing graphs
	    assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
	}

	/**
	 * Test that adding a new properties file into an included directory
	 * will be picked up.  Requires the include.directory.rescan to be set low 
	 * @throws Exception
	 */
	@Test
	public void testIncludeDirectoryNewFile() throws Exception {
	    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
	
	    m_outputStream = new FileOutputStream(rootFile);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_baseIncludePrefab);
	    m_writer.close();
	    m_outputStream.close();
	    
	    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
	    graphDirectory.mkdir();
	
	    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
	    perfConfig.put("performance", new FileSystemResource(rootFile));
	    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
	
	    try {
	        PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
	        fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
	    } catch (ObjectRetrievalFailureException e) {
	        
	    }
	
	    //Now create the new graph in a sub-directory; see if it gets read
	    File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
	    m_outputStream = new FileOutputStream(graphErrors);
	    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
	    m_writer.write(s_separateErrorsGraph);
	    m_writer.close();
	    m_outputStream.close();
	
	    //Wait longer than the rescan timeout on the include directory
	    Thread.sleep(1100);
	    
	    assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
	}

	/**
	 * It would be nice if having found a new file in the include directory that was malformed, that
	 * when it is fixed, it is picked up immediately, rather than having to wait for the next rescan interval
	 */
	@Test
	public void testIncludeNewFileMalformedContentThenFixed()
			throws Exception {
			    //Don't do the normal checking of logging for worse than warning; we expect an error or two to be logged, and that's fine
			    m_testSpecificLoggingTest = true;
			
			    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
			
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_baseIncludePrefab);
			    m_writer.close();
			    m_outputStream.close();
			    
			    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
			    graphDirectory.mkdir();
			
			    HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
			    perfConfig.put("performance", new FileSystemResource(rootFile));
			    PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
			
			    try {
			        PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
			        fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
			    } catch (ObjectRetrievalFailureException e) {
			        
			    }
			
			    //Now create the new graph in a sub-directory but make it malformed; make sure it isn't loaded
			    File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
			    m_outputStream = new FileOutputStream(graphErrors);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_separateErrorsGraph.replace("report.id", "report.noid"));
			    m_writer.close();
			    m_outputStream.close();
			
			    //Wait longer than the rescan timeout on the include directory
			    Thread.sleep(1100);
			    
			    //Confirm that the graph still hasn't been loaded (because it was munted)
			    try {
			        PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
			        fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
			    } catch (ObjectRetrievalFailureException e) {
			        
			    }
			    
			    //Now set the include rescan interval to a large number, rewrite the graph correctly, and check
			    // that the file is loaded (and we don't have to wait for the rescan interval)
			   dao.findPrefabGraphTypeByName("performance").setIncludeDirectoryRescanInterval(300000); //5 minutes
			   m_outputStream = new FileOutputStream(graphErrors);
			   m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			   m_writer.write(s_separateErrorsGraph);
			   m_writer.close();
			   m_outputStream.close();
			
			   //Just make sure any timestamps will be at least 1 second old, just to be sure
			   Thread.sleep(1100);
			
			   //And now the graph should have loaded
			   try {
			       assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
			   } catch (Exception e) {
			       //Catch exceptions and fail explicitly, because that's a failure, not an "error"
			       fail("Should not have gotten an exception fetching the graph");
			   }
			}

	/**
	 * Test that when loading graphs from files in the include directory, that if one of
	 * the graphs defined in one of the multi-graph files is borked, the rest load correctly
	 * 
	 * Then also check that on setting the reload interval high, that the borked graph is 
	 * noticed immediately when we fix it
	 * @throws IOException
	 */
	@Test
	public void testPrefabConfigDirectoryPartlyBorkedMultiReports()
			throws Exception {
			    //Don't do the normal checking of logging for worse than warning; we expect an error or two to be logged, and that's fine
			    m_testSpecificLoggingTest = true;
			
			    File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
			    File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
			
			    File multiFile1 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits1.properties");
			    File multiFile2 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits2.properties");
			
			    m_outputStream = new FileOutputStream(rootFile);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_baseIncludePrefab);
			    m_writer.close();
			    m_outputStream.close();
			
			    graphDirectory.mkdir();
			    m_outputStream = new FileOutputStream(multiFile1);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    //Make mib2.errors incorrectly specified
			    m_writer.write(s_includedMultiGraph1.replace("report.mib2.errors.name", "report.mib2.errors.nmae"));
			    m_writer.close();
			    m_outputStream.close();
			    
			    m_outputStream = new FileOutputStream(multiFile2);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    m_writer.write(s_includedMultiGraph2);
			    m_writer.close();
			    m_outputStream.close();
			    
			    HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
			    prefabConfigs.put("performance", new FileSystemResource(rootFile));
			
			    PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
			
			    //Check the graphs, basically ensuring that a handful of unique but easily checkable 
			    // bits are uniquely what they should be.
			
			    //We check all 4 graphs
			    PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
			    assertNotNull(mib2Bits);
			    assertEquals("mib2.bits", mib2Bits.getName());
			    assertEquals("Bits In/Out", mib2Bits.getTitle());
			    String columns1[] = { "ifInOctets", "ifOutOctets" };
			    Assert.assertArrayEquals(columns1, mib2Bits.getColumns());
			
			    PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
			    assertNotNull(mib2HCBits);
			    assertEquals("mib2.HCbits", mib2HCBits.getName());
			    assertEquals("Bits In/Out", mib2HCBits.getTitle());
			    String columns2[] = { "ifHCInOctets", "ifHCOutOctets" };
			    Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
			
			    PrefabGraph mib2Discards = dao.getPrefabGraph("mib2.discards");
			    assertNotNull(mib2Discards);
			    assertEquals("mib2.discards", mib2Discards.getName());
			    assertEquals("Discards In/Out", mib2Discards.getTitle());
			    String columns3[] = { "ifInDiscards", "ifOutDiscards" };
			    Assert.assertArrayEquals(columns3, mib2Discards.getColumns());
			
			    try {
			        PrefabGraph mib2Errors = dao.getPrefabGraph("mib2.errors");
			        fail("Should have thrown an ObjectRetrievalFailureException retrieving graph "
			                + mib2Errors);
			    } catch (ObjectRetrievalFailureException e) {
			        //This is ok, and what should have happened
			    }
			
			    //Now set the include rescan interval to a large number, rewrite the multigraph file correctly, and check
			    // that the file is loaded (and we don't have to wait for the rescan interval)
			    dao.findPrefabGraphTypeByName("performance").setIncludeDirectoryRescanInterval(300000); //5 minutes
			
			    //Just make sure any timestamps will be at least 1 second old, just to be sure that the file timestamp
			    // will be 1 second in the past
			    Thread.sleep(1100);
			
			    m_outputStream = new FileOutputStream(multiFile1);
			    m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
			    //Correctly specified graph file now (error corrected)
			    m_writer.write(s_includedMultiGraph1);
			    m_writer.close();
			    m_outputStream.close();
			
			    //And now the graph should have loaded correctly
			    try {
			        assertNotNull(dao.getPrefabGraph("mib2.errors")); 
			    } catch (Exception e) {
			        //Catch exceptions and fail explicitly, because that's a failure, not an "error"
			        fail("Should not have gotten an exception fetching the graph");
			    }
			}

}
