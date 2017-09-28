/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.template.PutTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Initialises Elastic Search by sending a template
 * @author cgallen
 *
 */
public class ElasticSearchInitialiser {

	Logger LOG = LoggerFactory.getLogger(ElasticSearchInitialiser.class);

	// default time before retrying to connect to ES
	public static final long DEFAULT_RETRY_TIMER_MS=20000; // 20 seconds default retry

	private long retryTimer = DEFAULT_RETRY_TIMER_MS;

	// thread for running initialiser until initialisation completes
	// rest of ES client will not run until the initialisation has completed
	private ESInitialiser esInitialiser = new ESInitialiser();
	private Thread esInitialiserThread = new Thread(esInitialiser);

	// true if ElasticSearchInitialiser has completed initialisation
	private AtomicBoolean initialised= new AtomicBoolean(false);

	// set false to stop initialiser thread
	private AtomicBoolean initialiserIsRunning = new AtomicBoolean(false);

	// key template name, Value file containing template
	private Map<String,String> templateFilesMap=new LinkedHashMap<String, String>();

	private Map<String,StringBuffer> loadedFiles= new HashMap<String, StringBuffer>();

	private RestClientFactory restClientFactory=null;

	private JestClient jestClient = null;

	public long getRetryTimer() {
		return retryTimer;
	}

	public void setRetryTimer(long retryTimer) {
		this.retryTimer = retryTimer;
	}

	public Map<String, String> getTemplateFiles() {
		return templateFilesMap;
	}

	public void setTemplateFiles(Map<String, String> templateFiles) {
		templateFilesMap.clear();
		templateFilesMap.putAll(templateFiles);
	}

	public RestClientFactory getRestClientFactory() {
		return restClientFactory;
	}

	public void setRestClientFactory(RestClientFactory restClientFactory) {
		this.restClientFactory = restClientFactory;
	}


	/*
	 * returns true if ES has finished initialising
	 */
	public boolean isInitialised() {
		return initialised.get();
	}

	public void init(){

		if (getTemplateFiles().isEmpty()){
			LOG.info("ElasticSearcInitialiser started with no default template files");
			initialised.set(true);
			return;
		}

		// load all files and test if JSON is OK
		for (String templateName: getTemplateFiles().keySet() ){
			String fileName= getTemplateFiles().get(templateName);
			LOG.info("   elasticsearch intialiser loading index template '"+templateName+ "' from file "+fileName);
			try {
				StringBuffer body=new StringBuffer();
				BufferedReader is=new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fileName)));
				String l;
				while((l=is.readLine())!=null) {
					body.append(l);
				}
				JSONParser parser = new JSONParser();
				JSONObject jsonObject=null;
				try{
					Object obj = parser.parse(body.toString());
					jsonObject = (JSONObject) obj;
					loadedFiles.put(templateName, body);
					LOG.debug("Loaded Template File"
							+ "\n     fileName: '"+fileName
							+ "'\n     templateName: '"+templateName
							+ "'\n     contents: "+jsonObject.toJSONString());
				} catch (ParseException e1) {
					throw new RuntimeException("cannot parse json Elastic Search template mapping from file "+fileName, e1);
				}

			} catch (Exception e) {
				throw new RuntimeException("Problem reading Elastic Search template mapping fileName="+fileName, e);
			}
		}

		LOG.debug("ElasticSearcInitialiser loaded template files. Trying to send templates to to ES");

		// start initialiser thread
		initialiserIsRunning.set(true);
		esInitialiserThread.start();

	}


	/**
	 * returns a singleton jest client from factory for use by this class
	 * @return
	 */
	private JestClient getJestClient(){
		if (jestClient==null) {
			synchronized(this){
				if (jestClient==null){
					if (restClientFactory==null) throw new RuntimeException("JestClientFactory must be set");
					jestClient= restClientFactory.getJestClient();
				}
			}
		}
		return jestClient;
	}

	public void destroy(){

		// signal initialiser thread to stop if still running
		initialiserIsRunning.set(false);
		esInitialiserThread.interrupt();

		// shutdown jest client
		if (jestClient!=null)
			try{
				jestClient.shutdownClient();
			}catch (Exception e){}
		jestClient=null;
	}

	/*
	 * Class run in separate thread to initialise ES until all files have been sent
	 * This waits until ES is available
	 * queue
	 */
	private class ESInitialiser implements Runnable {

		@Override
		public void run() {
			LOG.debug("starting ESInitialiser thread");
			// try sending templates to ES
			// if no connection then retry until connection succeeds. 
			// This blocks sending of events until all templates sent and initialised
			while (! initialised.get() && initialiserIsRunning.get()){
				String templateName=null;
				try {

					Iterator<Entry<String, StringBuffer>> entries = loadedFiles.entrySet().iterator();
					while (entries.hasNext() && initialiserIsRunning.get() ) {

						Entry<String, StringBuffer> entry = entries.next();

						templateName=entry.getKey();
						String body= entry.getValue().toString();

						PutTemplate putTemplate = new PutTemplate.Builder(templateName, body).build();

						JestResult jestResult = getJestClient().execute(putTemplate);

						if (! jestResult.isSucceeded()){
							LOG.error("Error sending template '"+templateName+"' to Elastic Search"
									+ " received result: "+jestResult.getJsonString()
									+ "\n   response code:" +jestResult.getResponseCode() 
									+ "\n   error message: "+jestResult.getErrorMessage());
						} else {
							LOG.info("Sent template '"+templateName+"' to Elastic Search"
									+ " received result: "+jestResult.getJsonString()
									+ "\n   response code:" +jestResult.getResponseCode() 
									+ "\n   error message: "+jestResult.getErrorMessage());
						}

					}

					initialised.set(true); // will only be true if all json templates processed

				} catch (Exception ex) {
					LOG.error("could not send template '"+templateName+"' to Elastic Search",ex);
				}

				if ( initialiserIsRunning.get() && ! initialised.get()) {
					try {
						LOG.error("waiting "+retryTimer+" ms before retrying to sending all templates again to Elastic Search");
						synchronized (this){
							wait(retryTimer);
						}
					} catch (InterruptedException e) {}
				}
			}

			if (initialised.get()){
				LOG.info("ElasticSearcInitialiser initialisation complete");
			} else LOG.error("ElasticSearcInitialiser initialisation not complete. Initialiser shutting down.");

		}
	}






}
