package org.opennms.plugins.elasticsearch.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.Version;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticTemplateLoadTest {
	
	@Test
	public void loadTemplate() throws IOException {
		DefaultTemplateLoader defaultTemplateLoader = new DefaultTemplateLoader();

		String template = defaultTemplateLoader.load(new Version(6,4,1) , "/eventsIndexTemplate");

		final JsonElement json = new JsonParser().parse(template);
		if (!json.isJsonObject()) {
			throw new IllegalArgumentException("Provided template is not a valid json object");
		}
		JsonObject templateJson = json.getAsJsonObject();
		assertNotNull(templateJson);
		assertTrue(!templateJson.toString().isEmpty());
	}


}
