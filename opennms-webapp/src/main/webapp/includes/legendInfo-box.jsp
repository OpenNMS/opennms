<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">



<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en' xmlns:opennms='xsds/coreweb.xsd'>
<head>
  <title>
    
    OpenNMS Web Console
  </title>
  <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css"/>
  <meta http-equiv="Content-Script-Type" content="text/javascript"/>

  
  
    
        <base href="http://localhost:8980/opennms/" />
    
  
  <!--   -->
  
    
        <link rel="stylesheet" type="text/css" href="http://localhost:8980/opennms/css/styles.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="http://localhost:8980/opennms/css/print.css" media="print" />
        
        	
        		<link rel='stylesheet' type='text/css' href='http://localhost:8980/opennms/extJS/resources/css/ext-all.css' />
  				<link rel='stylesheet' type='text/css' href='http://localhost:8980/opennms/css/o-styles.css' media='screen' />
  				<link rel='stylesheet' type='text/css' href='http://localhost:8980/opennms/extJS/resources/css/opennmsGridTheme.css' />
        	
        
    
  
  
  <script type="text/javascript" src="http://localhost:8980/opennms/js/global.js"></script>

    
        <script type="text/javascript" src="http://localhost:8980/opennms/coreweb/coreweb.nocache.js"></script>
    
	
  		<script type='text/javascript' src='http://localhost:8980/opennms/extJS/source/core/Ext.js'></script>
  		<script type='text/javascript' src='http://localhost:8980/opennms/extJS/source/adapter/ext-base.js'></script>
  		<script type='text/javascript' src='http://localhost:8980/opennms/extJS/ext-all-debug.js'></script>
  		<script type='text/javascript'>Ext.BLANK_IMAGE_URL = 'http://localhost:8980/opennms/extJS/resources/images/default/s.gif'</script>
	

	

	




</head>



<TITLE>
Additional Legend Information
</TITLE>

<div id="header">
<h1><a href="http://localhost:8980/opennms/index.jsp"><img alt="OpenNMS Web Console Home" src="http://localhost:8980/opennms/images/logo.png"></a></h1>
</div>

<div id="content">
<BODY>
<h2>
<P>
Each status cell is an intersection of a Location and Application
</P>
<br/>
<P>
An Application is defined by a subset of the set of IP based services created in OpenNMS
</P>
<p>
</p>
<P>
A Location is an arbitrary entity defined through configuration by the OpenNMS user
</P>
<br/>
<P>
Each Location presents Availability as the best percentage possible based on the history of status<br/>
of services monitored from *all* remote pollers in that Location since midnight of the current day.<br/>
If there were 2 services being monitored by 2 remote pollers and each 1 service down, uniquely, then<br/>
 the availability would still be 100%.
</P>
<br/>
<P>
Each Location presents Status as the worst known status of all remote pollers in a Started state.
<P>
</h2>
</BODY>
</div>

</HTML>
