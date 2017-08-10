/*
 * Common Groovey script used to access properties file and add properties into maven build for 
 * subscription-checker and productkeys projects
 */
	log.info("Starting gmaven-plugin Groovy script to import properties");
	import java.io.FileInputStream;
	import java.io.File;
	
	String PRODUCT_KEYS_FILENAME_PROPERTY = "productKeysFile";

	java.util.Properties newProperties = new java.util.Properties();

	String p = project.properties.getProperty(PRODUCT_KEYS_FILENAME_PROPERTY);

	if (p == null) {
	 log.error("Property not defined in pom: " + PRODUCT_KEYS_FILENAME_PROPERTY);
	} else {
	 File f = new File(p);
	 if (!f.exists()) {
	   log.warn("Properties File " + f.getAbsolutePath()
		   + " does not exist. Not loading extra properties. Using defaults");

	   String defaultSubscriptionName = project.properties.getProperty("defaultSubscriptionName");
	   if (defaultSubscriptionName != null) {
		   log.info("   adding default maven property: subscriptionName ="+ defaultSubscriptionName);
		   project.properties.setProperty("subscriptionName", defaultSubscriptionName);
	   }
	   
	   String defaultSubscriptionVersion= project.properties.getProperty("defaultSubscriptionVersion");
	   if (defaultSubscriptionVersion != null) {
		   log.info("   adding default maven property: subscriptionVersion ="+ defaultSubscriptionVersion);
		   project.properties.setProperty("subscriptionVersion", defaultSubscriptionVersion);
	   }
	   
	   String defaultSubscriptionGroupId= project.properties.getProperty("defaultSubscriptionGroupId");
	   if (defaultSubscriptionGroupId != null) {
		   log.info("   adding default maven property: subscriptionGroupId ="+ defaultSubscriptionGroupId);
		   project.properties.setProperty("subscriptionGroupId", defaultSubscriptionGroupId);
	   }
	   
	   String defaultSubscriptionCheckEnabled= project.properties.getProperty("defaultSubscriptionCheckEnabled");
	   if (defaultSubscriptionCheckEnabled != null) {
		   log.info("   adding default maven property: subscriptionCheckEnabled ="+ defaultSubscriptionCheckEnabled);
		   project.properties.setProperty("subscriptionCheckEnabled", defaultSubscriptionCheckEnabled);
	   }
	   
	   String defaultRegenerateSubscription= project.properties.getProperty("defaultRegenerateSubscription");
	   if (defaultRegenerateSubscription != null) {
		   log.info("   adding default maven property: regenerateSubscription ="+ defaultRegenerateSubscription);
		   project.properties.setProperty("regenerateSubscription", defaultRegenerateSubscription);
	   }

	  } else {
	   log.info("Loading additional build properties from "+ f.getAbsolutePath());
	   FileInputStream input;
	   try {
		 input = new FileInputStream(p);
		 newProperties.load(input);
	   } catch (Exception e) {
		 log.error("problem loading properties file ",e);
	   }

	   for (String key : newProperties.stringPropertyNames()) {
		 String value = newProperties.getProperty(key);
		 log.info("   adding new maven property: " + key + "="
			 + value + " replacing: "
			 + project.properties.setProperty(key, value));
	   }

	 }
	 
	 List<String> goals = session.getGoals();

	 String goalStr="";
	 if(goals.contains("install")) goalStr="install";
	 if(goals.contains("deploy")) goalStr="deploy";

	 log.info("   adding new maven property: goalStr="
			 + goalStr + " replacing: "
			 + project.properties.setProperty("goalStr", goalStr));
	 
	 String userPropertiesStr="";
	 for (String key : session.getUserProperties().stringPropertyNames()) {
	   userVariablesStr=userPropertiesStr+" -D"+key+"="+session.getUserProperties().getProperty(key);
	 }
	 log.info("   adding new maven property: userPropertiesStr="
			 + userPropertiesStr + " replacing: "
			 + project.properties.setProperty("userPropertiesStr", userPropertiesStr));
	 
	 String systemPropertiesStr="";
	 for (String key : session.getSystemProperties().stringPropertyNames()) {
	   userVariablesStr=systemPropertiesStr+" -D"+key+"="+session.getUserProperties().getProperty(key);
	 }
	 log.info("   adding new maven property: systemPropertiesStr="
			 + systemPropertiesStr + " replacing: "
			 + project.properties.setProperty("systemPropertiesStr", systemPropertiesStr));
	 
	}