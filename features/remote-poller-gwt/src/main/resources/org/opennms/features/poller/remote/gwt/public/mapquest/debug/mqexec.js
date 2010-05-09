try{
   var testCommons = new MQObject();
   testCommons = null;
}catch(error){
   throw "You must include mqcommon.js or toolkit api script prior to mqexec.js.";
}
/**
 * Constructs new MQExec Object
 *
 * @class Provides communication layer for requests to a server. Most, but
 * not all, of the functions generate actual requests to the MapQuest
 * Server. Within the context of this object, a Session object is used
 * to maintain information concerning the current client's requests.
 *
 * @param {String/MQExec} strServerNameORmqeObj MapQuest Server name (or) MQExec
 * object to copy the server information.
 * @param {String} strPathToServer Path to the Server
 * @param {int} nServerPort HTTP Port of the Server
 * @param {String} strProxyServerName Name of the server hosting the Proxy page
 * @param (String} strPathToProxyServerPage Path to the Proxy Server page
 * @param {int} nProxyServerPort Port of the Server hosting the Proxy page
 *
 *    strServerName, strPathToServer, and nServerPort are required properties
 *    that need to be set before the first request. If these properties are not
 *    specified, the defaults used for initialization are:
 *
 *    MapServer = localhost
 *    PathToServer = mq
 *    HTTPPort = 80
 *
 *    Please Note: These defaults are not likely to match your configuration.
 *
 *    If MQExec object is provided as the first parameter, remaining parameters
 *    will be ignored and server information will be copied from the provided object.
 *
 *
 * @see MQDBLayerQueryCollection
 * @see MQDTCollection
 * @see MQFeatureCollection
 * @see MQLocation
 * @see MQLocationCollection
 * @see MQMapState
 * @see MQLatLng
 * @see MQPoint
 * @see MQSession
 * @see MQRouteOptions
 * @see MQRouteResults
 * @see MQGeocodeOptionsCollection
 * @see MQStringCollection
 * @see MQRecordSet
 * @see MQLatLngCollection
 * @see MQPointCollection
 * @see MQSearchCriteria
 * @see MQMapCommand
 * @see MQQualityType
 * @see MQMatchType
 */
function MQExec ( strServerNameORmqeObj, strPathToServer, nServerPort,
                  strProxyServerName, strPathToProxyServerPage, nProxyServerPort )
{


   var m_strServerName;
   var m_strServerPath;
   var m_nServerPort;
   var m_strProxyServerPath ;
   var m_strProxyServerName;
   var m_nProxyServerPort;
   var m_lSocketTimeout;
   var m_strXInfo = "";

   if( typeof strServerNameORmqeObj == "string" ) {
      m_strServerName = strServerNameORmqeObj || "localhost";
      m_strServerPath = strPathToServer || "mq";
      m_nServerPort     = nServerPort || 80;
      m_strProxyServerPath = strPathToProxyServerPage || "";
      m_strProxyServerName = strProxyServerName || "";
      m_nProxyServerPort   = nProxyServerPort || 0;
      m_lSocketTimeout = 0;

   } else if(strServerNameORmqeObj.getClassName() &&
            strServerNameORmqeObj.getClassName() == "MQExec" ) {
      m_strServerName = strServerNameORmqeObj.getServerName();
      m_strServerPath = strServerNameORmqeObj.getServerPath();
      m_nServerPort   = strServerNameORmqeObj.getServerPort();
      m_strProxyServerName = strServerNameORmqeObj.getProxyServerName();
      m_nProxyServerPort   = strServerNameORmqeObj.getProxyServerPort();
      m_strProxyServerPath = strServerNameORmqeObj.getProxyServerPath();
      m_lSocketTimeout  = strServerNameORmqeObj.m_lSocketTimeout;
   }


   /**
    * Sets the name of the server which is to be used to satisfy this request.
    * @param {String} strServerName The name of the MapQuest Server which is to
    * be used to satisfy this request.
    * @type     void
    */

   this.setServerName = function(strServerName) {
      m_strServerName = strServerName;

   };

  /**
    * Returns the name of the server which will be used to satisfy this
    * request.
    *
    * @return   The name of the server which will be used to satisfy
    * this request.
    * @type      String
    */

   this.getServerName= function() {
      return m_strServerName;
   };


   /**
    * Sets the path to the server which is to be used to satisfy this request.
    *
    * @param   {String}   strServerPath  The path to the server which is to be used
    * to satisfy this request.
    * @type      void
    */

   this.setServerPath = function(strServerPath) {
      m_strServerPath = strServerPath;
   };


   /**
    * Returns the path to the server which will be used to satisfy this
    * request.
    *
    * @return  The path to the  server which will be used to satisfy this
    * request.
    * @type    String
    */

   this.getServerPath= function() {
      return m_strServerPath;
   };


   /**
    * Sets the port number to the server which is to be used to satisfy this
    * request.
    *
    * @param  {int}  nServerPort The port number of the server which is to be
    *                      used to satisfy this request.
    * @type    void
    */

   this.setServerPort = function(nServerPort) {
      m_nServerPort = nServerPort;

   };

   /**
    * Returns the port number of the server which will be used to satisfy
    * this request.
    *
    * @return    The port number of the server which will be used to satisfy
    *            this request.
    * @type    int
    */

   this.getServerPort= function() {
      return m_nServerPort;
   };



  /**
    * Sets the name or IP of the proxy server to connect to.
    * The name should not contain any leading or trailing slashes ("/").
    *
    * @param  {String} strProxyServerName The name of the proxy server.
    * @type    void
    */

   this.setProxyServerName = function(strProxyServerName) {
      m_strProxyServerName = strProxyServerName;

   };

  /**
    * Returns the name or IP of the proxy server to connect to.
    *
    * @return The name of the proxy server.
    * @type String
    */

   this.getProxyServerName = function() {

      return m_strProxyServerName;

   };

   /**
    * Sets the path to the proxy server which is to be used to satisfy this request.
    *
    * @param  (String} strProxyServerPath The path to the server which is to be used
    * to satisfy this request.
    * @type void
    */

   this.setProxyServerPath = function(strProxyServerPath) {
      m_strProxyServerPath = strProxyServerPath;

   };


   /**
    * Returns the path to the proxy server page which will be used to satisfy this
    * request.
    *
    * @return  The path to the proxy server page which will be used to satisfy this
    *          request.
    * @type    String
    */

   this.getProxyServerPath = function() {
      return m_strProxyServerPath;
   };

   /**
    * Sets the port to connect to the proxy server with.
    *
    * @param {int}  nProxyServerPort The proxy server port number.
    * @type void
    */

   this.setProxyServerPort = function(nProxyServerPort) {
      m_nProxyServerPort = nProxyServerPort;

   };

   /**
    * Returns the port number to connect to the proxy server with.
    *
    * @return The proxy server port number.
    * @type int
    */

   this.getProxyServerPort = function() {
      return m_nProxyServerPort;
   };


   /**
    * Sets data to be passed to the server to be logged with any
    * subsequent requests in the transaction log. (Max 8 characters)
    *
    * @param {String} strXInfo Transaction Info.
    *
    * @type void
    */

   this.setTransactionInfo = function(strXInfo) {
      if (strXInfo.length > 32)
         m_strXInfo = strXInfo.substring(0, 32);
      else
         m_strXInfo = strXInfo;

   };


   /**
    * Gets data to be passed to the server to be logged with any
    * subsequent requests in the transaction log.
    *
    * @return Transaction Information
    * @type String
    */

   this.getTransactionInfo = function() {
      return m_strXInfo;
   };


}

   /* Defined transaction versions */
   MQExec.prototype.ROUTE_VERSION = "2";
   MQExec.prototype.SEARCH_VERSION = "0";
   MQExec.prototype.GEOCODE_VERSION = "1";
   MQExec.prototype.ROUTEMATRIX_VERSION = "0";
   MQExec.prototype.GETRECORDINFO_VERSION = "0";
   MQExec.prototype.REVERSEGEOCODE_VERSION = "0";
   MQExec.prototype.GETSESSION_VERSION = "1";


   /**
    * Prepares request XML data to be sent to the server for a given transaction
    * and set of request objects.
    *
    * @param {String} strTransaction Name of the transaction
    *
    * @param {Array} arrRequest Array of request objects to form the XML
    *
    * @param {String} strVersion Version of the transaction implementation
    *
    * @type void
    *
    * @private
    */
   MQExec.prototype.getRequestXml = function(strTransaction, arrRequest, strVersion) {

      var arrXmlBuf = new Array();
      var version = strVersion || "0";
      arrXmlBuf.push("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
      arrXmlBuf.push("<" + strTransaction + " Version=\"" + version + "\">\n");
      for (var i=0; i < arrRequest.length; i++) {
         arrXmlBuf.push(arrRequest[i].saveXml());
         arrXmlBuf.push("\n");
      }
      arrXmlBuf.push("</" + strTransaction + ">");
      return arrXmlBuf.join("");

   };

  /**
    * Performs the given transaction by making AJAX call to the Proxy Server page.
    *
    * @param {String} strTransaction Name of the transaction
    *
    * @param {Array} arrRequest Array of request objects
    *
    * @type void
    *
    * @private
    */
   MQExec.prototype.doTransaction = function(strTransaction, arrRequest, strVersion ) {

      var xmlDoc;
      var strResXml;
      var http_request = mqXMLHttpRequest();
      var strUrl = "";
      arrRequest.push( new MQAuthentication(this.getTransactionInfo()));
      var strReqXml = this.getRequestXml(strTransaction, arrRequest, strVersion);

      if(this.getProxyServerName() != "") {
         strUrl += "http://" + this.getProxyServerName();
         if(this.getProxyServerPort() != 0) {
            strUrl += ":" + this.getProxyServerPort();
         }
         strUrl += "/";
      }

      strUrl += this.getProxyServerPath();
      strUrl += "?sname=" + this.getServerName();
      strUrl += "&spath=" + this.getServerPath();
      strUrl += "&sport=" + this.getServerPort();
      display("mqXmlLogs", "Request URL: ", strUrl, "rURL", "mqDisplay");
      display("mqXmlLogs", "Request XML: ", strReqXml, "", "mqDisplay");

      http_request.open("POST", strUrl, false);
      http_request.send(strReqXml);
      if (http_request.status == 200) {
          xmlDoc= http_request.responseXML;
      }
      else {
         alert(   "HTTP Status: " + http_request.status +
               " (" + http_request.statusText + ")\n" +
               "Details: \n" + http_request.responseText
              );
         xmlDoc = null;
      }
      display("mqXmlLogs", "Response XML: ", mqXmlToStr(xmlDoc), "resXML", "mqDisplay");
      return xmlDoc;
   };


   /**
    * Method to geocode an address or an intersection. Geocode options supply
    * the QualityType and MatchType.
    *
    * @param {MQAddress} mqaAddress  The Address object containing the necessary info for
    * the address.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQLocationCollection} mqlcLocations  The LocationCollection to hold the results of the
    * geocode.
    *
    * @param {MQAutoGeocodeCovSwitch/MQGeocodeOptionsCollection} theOptions
    * The AutoGeocodeCovSwitch or MQGeocodeOptionsCollection to select a set of
    * options stored on the server.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @type void
    */

   MQExec.prototype.geocode = function(mqaAddress, mqlcLocations, theOptions) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if(mqaAddress == null || (mqaAddress.getClassName() !== "MQAddress" && mqaAddress.getClassName() !== "MQSingleLineAddress")) {
           throw "Null or Illegal Argument passed for MQAddress";
      } else {
           arrRequest.push(mqaAddress);
      }

      if(mqlcLocations == null || mqlcLocations.getClassName() !== "MQLocationCollection") {
           throw "Null or Illegal Argument passed for MQLocationCollection";
      }

      if(theOptions != null) {
         if(theOptions.getClassName() !== "MQAutoGeocodeCovSwitch" &&
             theOptions.getClassName() !== "MQGeocodeOptionsCollection" ) {
            throw "Illegal Argument passed for Geocode Options";
         } else {
            arrRequest.push(theOptions);
         }
      }

      mqLogTime("MQExec.geocode: Transaction Start");
      xmlDoc = this.doTransaction("Geocode", arrRequest, this.GEOCODE_VERSION);
      mqLogTime("MQExec.geocode: Transaction End");

      mqLogTime("MQExec.geocode: Loading of GeocodeResponse Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/GeocodeResponse/LocationCollection"));
      mqlcLocations.loadXml(strXml);
      mqLogTime("MQExec.geocode: Loading of GeocodeResponse End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };



   /**
    * Method to batch geocode a collection of locations. Geocode options supply
    * the QualityType and MatchType.
    * 
    * @param {MQLocationCollection} mqlcLocations  The LocationCollection containing the
    * necessary info for the addresses to be geocode.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQLocationCollectionCollection} mqlccLocations  The LocationCollectionCollection
    * used to hold the results of the geocode.
    *
    * @param {MQAutoGeocodeCovSwitch/MQGeocodeOptionsCollection} theOptions
    * The AutoGeocodeCovSwitch or MQGeocodeOptionsCollection to select a set of
    * options stored on the server.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @type void
    */

   MQExec.prototype.batchGeocode = function(mqlcLocations, mqlccLocations, theOptions) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if(mqlcLocations == null || mqlcLocations.getClassName() !== "MQLocationCollection") {
           throw "Null or Illegal Argument passed for MQLocationCollection";
      } else {
           arrRequest.push(mqlcLocations);
      }

      if(mqlccLocations == null || mqlccLocations.getClassName() !== "MQLocationCollectionCollection") {
           throw "Null or Illegal Argument passed for MQLocationCollectionCollection";
      }

      if(theOptions != null) {
         if(theOptions.getClassName() !== "MQAutoGeocodeCovSwitch" &&
             theOptions.getClassName() !== "MQGeocodeOptionsCollection" ) {
            throw "Illegal Argument passed for Geocode Options";
         } else {
            arrRequest.push(theOptions);
         }
      }

      mqLogTime("MQExec.batchGeocode: Transaction Start");
      xmlDoc = this.doTransaction("BatchGeocode", arrRequest, this.GEOCODE_VERSION);
      mqLogTime("MQExec.batchGeocode: Transaction End");

      mqLogTime("MQExec.batchGeocode: Loading of GeocodeResponse Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/BatchGeocodeResponse/LocationCollectionCollection"));
      mqlccLocations.loadXml(strXml);
      mqLogTime("MQExec.batchGeocode: Loading of GeocodeResponse End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };



   /**
    * Calculates a route using a collection of locations as origin and
    * destination. These locations can be Address, GeoAddress,
    * Intersection, or GeoIntersection objects.
    *
    * @param {MQLocationCollection} mqlcLocations Collection of locations.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQRouteOptions} mqroOptions Route options object, to modify
    * behavior of route.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQRouteResults} mqrrResults The results of the route request.
    *
    * @param {String} strSessionUID The unique Session ID.
    *
    * @param {MQRectLL} mqRectLL The bounding box to provide to the TileMap
    * ToolKit. This parameter is optional
    *
    * @type void
    */

   MQExec.prototype.doRoute = function(mqlcLocations, mqroOptions, mqrrResults, strSessionUID, mqRectLL) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if(mqlcLocations == null || mqlcLocations.getClassName() !== "MQLocationCollection") {
         throw "Null or Illegal Argument passed for MQLocationCollection";
      } else {
         arrRequest.push(mqlcLocations);
      }
      if(mqroOptions == null || mqroOptions.getClassName() !== "MQRouteOptions") {
         throw "Null or Illegal Argument passed for MQRouteOptions";
      } else {
         arrRequest.push(mqroOptions);
      }
      if(mqrrResults == null || mqrrResults.getClassName() !== "MQRouteResults") {
         throw "Null or Illegal Argument passed for MQRouteResults";
      } else {
         var sessionId = strSessionUID || "";
         arrRequest.push(new MQXmlNodeObject("SessionID",sessionId) );
      }

      mqLogTime("MQExec.doRoute: Transaction Start");
      xmlDoc = this.doTransaction("DoRoute", arrRequest, this.ROUTE_VERSION);
      mqLogTime("MQExec.doRoute: Transaction End");

      mqLogTime("MQExec.doRoute: Loading of RouteResults Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/DoRouteResponse/RouteResults"));
      mqrrResults.loadXml(strXml);
      mqLogTime("MQExec.doRoute: Loading of RouteResults End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

      if(mqRectLL !== null && sessionId !== ""){
         this.getRouteBoundingBoxFromSessionResponse(sessionId, mqRectLL);
      }

   };

  /**
    * Generates a request to the MapQuest server (as specified by the
    * server name, path, and port number) to create a user Session.
    * The Session is assigned a unique identifier which is used to
    * access and update the Session information.
    *
    * @param  {MQSession} mqsSession Object containing objects for the session.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @return The unique Session identifier.
    *
    * @type String
    */

   MQExec.prototype.createSessionEx = function(mqsSession) {

      var xmlDoc;
      var strSessId;
      var arrRequest = new Array();
      if(mqsSession == null || mqsSession.getClassName() !== "MQSession") {
         throw "Null or Illegal Argument passed for MQSession";
      } else {
         arrRequest.push(mqsSession);
      }

      xmlDoc = this.doTransaction("CreateSession", arrRequest);
      strSessId = mqGetNodeText(mqGetNode(xmlDoc, "/CreateSessionResponse/SessionID"));

      return strSessId;
   };


   MQExec.prototype.getSession = function(strSessionID, mqObj) {

      // TO-DO: do class name validations.
      var xmlDoc;
      var strXml;
      var sessionId = strSessionID || "";
      var arrRequest = new Array();
      arrRequest.push(new MQXmlNodeObject("SessionID",sessionId) );

      xmlDoc = this.doTransaction("GetSession", arrRequest, this.GETSESSION_VERSION);
      if(mqObj.getClassName()==="MQMapState"){
         strXml = mqXmlToStr(mqGetNode(xmlDoc, "/GetSessionResponse/Session/MapState"));
         mqObj.loadXml(strXml);
      } else if(mqObj.getClassName()==="MQSession"){
         strXml = mqXmlToStr(mqGetNode(xmlDoc, "/GetSessionResponse/Session"));
         mqObj.loadXml(strXml);
      }
   };


   /**
    * Calculates a route matrix. Either a drive time (many-to-many) or a
    * multi-destination (one-to-many) route.
    *
    * @param  {MQLocationCollection} mqlcLocations Collection of location objects.
    * The first member of the collection is the origin. If allToAll is true then
    * the last member is the destination. Each location must be a GeoAddress
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param  {boolean} bAllToAll If true then a matrix of time and distance from
    * each location to all others is found. If false, time and distance from the
    * first location (origin) to all others is found.
    *
    * @param  {MQRouteOptions} mqroOptions Specifies options for the route (fastest vs.
    * shortest, coverage to use, roads to avoid, etc.).
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param  {MQRouteMatrixResults} mqrmrResults Contains the time and distance matrix as
    * well as the status of the call (success, failure, partial success)
    *
    * @type void
    *
    */

   MQExec.prototype.doRouteMatrix = function(mqlcLocations, bAllToAll, mqroOptions, mqrmrResults) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if(mqlcLocations == null || mqlcLocations.getClassName() !== "MQLocationCollection") {
           throw "Null or Illegal Argument passed for MQLocationCollection";
      } else {
           arrRequest.push(mqlcLocations);
      }
      if( bAllToAll == null || typeof bAllToAll != "boolean") {
           throw "Null or Illegal Argument passed for bAllToAll";
      } else {
           var iAllToAll = bAllToAll ? 1 : 0;
           arrRequest.push(new MQXmlNodeObject("AllToAll", iAllToAll));
      }
      if(mqroOptions == null || mqroOptions.getClassName() !== "MQRouteOptions") {
           throw "Null or Illegal Argument passed for MQRouteOptions";
      } else {
           arrRequest.push(mqroOptions);
      }
      if(mqrmrResults == null || mqrmrResults.getClassName() !== "MQRouteMatrixResults") {
           throw "Null or Illegal Argument passed for MQRouteMatrixResults";
      }

      mqLogTime("MQExec.doRoute: Transaction Start");
      xmlDoc = this.doTransaction("DoRouteMatrix", arrRequest, this.ROUTEMATRIX_VERSION);
      mqLogTime("MQExec.doRoute: Transaction End");

      mqLogTime("MQExec.doRoute: Loading of RouteResults Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/DoRouteMatrixResponse/RouteMatrixResults"));
      mqrmrResults.loadXml(strXml);
      mqLogTime("MQExec.doRoute: Loading of RouteResults End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };


   /**
    * Generates a request to the MapQuest server (as specified by the server
    * name, path, and port number) to perform a DB Search based on either the
    * recordIds received or the extraCriteria info of the dblayerquery. The
    * resulting recordset contains fields specified by the scFieldNames param.
    *
    * @param  {MQStringCollection} mqscFieldNames containing the names of the fields
    * to return, or blank for all fields.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param  {MQDBLayerQuery} mqdlqQuery  Contains the name of the dblayer/table to query.
    * Optionally contains ExtraCriteria.  Utilized only if RecordIds are empty.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQRecordSet}  mqrsResults  The Returned RecordSet containg the records and
    * fields matching the input parameters.
    *
    * @param  {MQStringCollection} mqscRecIds  RecordIdentifiers of the records to return
    * stored in a StringCollection.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @type void
    *
    */

   MQExec.prototype.getRecordInfo = function(mqscFieldNames, mqdlqQuery, mqrsResults, mqscRecIds) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if( mqscFieldNames == null || mqscFieldNames.getClassName() !== "MQStringCollection" ) {
           throw "Null or Illegal Argument passed for MQStringCollection";
      } else {
            var fields = new MQStringCollection();
            fields.setM_Xpath("Fields");
            fields.append(mqscFieldNames);
            arrRequest.push(fields);
      }
      if( mqdlqQuery == null || mqdlqQuery.getClassName() !== "MQDBLayerQuery" ) {
           throw "Null or Illegal Argument passed for MQDBLayerQuery";
      } else {
           arrRequest.push(mqdlqQuery);
      }
      if( mqrsResults == null || mqrsResults.getClassName() !== "MQRecordSet" ) {
           throw "Null or Illegal Argument passed for MQRecordSet";
      }
      if( mqscRecIds == null || mqscRecIds.getClassName() !== "MQStringCollection") {
           throw "Null or Illegal Argument passed for MQStringCollection";
      } else {
            var recordIds = new MQStringCollection();
            recordIds.setM_Xpath("RecordIds");
            recordIds.append(mqscRecIds);
            arrRequest.push(recordIds);
      }

      mqLogTime("MQExec.getRecordInfo: Transaction Start");
      xmlDoc = this.doTransaction("GetRecordInfo", arrRequest, this.GETRECORDINFO_VERSION);
      mqLogTime("MQExec.getRecordInfo: Transaction End");

      mqLogTime("MQExec.getRecordInfo: Loading of RecordSet Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/GetRecordInfoResponse/RecordSet"));
      mqrsResults.loadXml(strXml);
      mqLogTime("MQExec.getRecordInfo: Loading of RecordSet End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };


   /**
    * Finds the address at a given latitude/longitude position.
    *
    * @param {MQLatLng}  mqllLatLng The latitude/longitude position to use for
    * the reverse geocode.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQLocationCollection}  mqlcLocations A LocationCollection in
    * which to return the results of the reverse geocode.  It will contain
    * only GeoAddress objects.
    *
    * @param {String}  strMapCovName The name of the mapping coverage to be
    * used for the reverse geocode.
    *
    * @param {String}  strGeocodeCovName An optional parameter, specifies the
    * geocode coverage to use for the reverse geocode. If specified, using
    * this geocode coverage, a ZIP code lookup will be performed on the addresses
    * found in the mapping data to verify and fill in the city/state information
    * for the address. If not specified, only ZIP code and, if available, street
    * information found in the mapping data will be returned.
    *
    * @type void
    *
    */

   MQExec.prototype.reverseGeocode = function(mqllLatLng, mqlcLocations, strMapCovName, strGeocodeCovName) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if( mqllLatLng == null || mqllLatLng.getClassName() !== "MQLatLng" ) {
           throw "Null or Illegal Argument passed for MQLatLng";
      } else {
           arrRequest.push(mqllLatLng);
      }
      if( mqlcLocations == null || mqlcLocations.getClassName() !== "MQLocationCollection" ) {
           throw "Null or Illegal Argument passed for MQLocationCollection";
      }
      var mapPool = strMapCovName || "";
      arrRequest.push(new MQXmlNodeObject("MapPool", mapPool));

      var geocodePool = strGeocodeCovName || "";
      arrRequest.push(new MQXmlNodeObject("GeocodePool", geocodePool));

      mqLogTime("MQExec.reverseGeocode: Transaction Start");
      xmlDoc = this.doTransaction("ReverseGeocode", arrRequest, this.REVERSEGEOCODE_VERSION);
      mqLogTime("MQExec.reverseGeocode: Transaction End");

      mqLogTime("MQExec.reverseGeocode: Loading of Response Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/ReverseGeocodeResponse/LocationCollection"));
      mqlcLocations.loadXml(strXml);
      mqLogTime("MQExec.reverseGeocode: Loading of Response End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };


   /**
    * Generates a request to the MapQuest server (as specified by the
    * server name, path, and port number) to perform a search and
    * return the results in a FeatureCollection object.
    *
    * @param  {MQSearchCriteria} mqscCriteria  Spatial parameters for the search.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQFeatureCollection}  mqfcSearchResults Search results.
    *
    * @param {String}  strCoverageName  The name of the map data coverage if searching
    * for features in the map data.
    *
    * @param {MQDBLayerQueryCollection}  mqdlqcDbLayers  Database parameters for the search.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQFeatureCollection}  mqfcFeatures  A FeatureCollection to also be searched
    * based on the spatial criteria.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @param {MQDTCollection}  mqdcDisplayTypes  A DTCollection to narrow the search. Only those
    * features with display types corresponding to those in this collection will be returned.
    * (Please Note:  This object will not be updated, it is simply used for the request.)
    *
    * @type void
    */

   MQExec.prototype.search = function(mqscCriteria, mqfcSearchResults, strCoverageName,
                              mqdlqcDbLayers, mqfcFeatures, mqdcDisplayTypes)      {
      var xmlDoc;
      var strXml;
      var arrRequest = new Array();
      var strName = mqscCriteria ? mqscCriteria.getClassName(): null;

      if( strName == null || (strName !== "MQSearchCriteria" &&
            strName !== "MQRadiusSearchCriteria" && strName !== "MQRectSearchCriteria" &&
            strName !== "MQPolySearchCriteria" && strName !== "MQCorridorSearchCriteria" )) {
           throw "Null or Illegal Argument passed for Search Criteria";
      } else {
             arrRequest.push(mqscCriteria);
      }
      if(mqfcSearchResults == null || mqfcSearchResults.getClassName() !== "MQFeatureCollection") {
           throw "Null or Illegal Argument passed for MQFeatureCollection";
      }
      if( typeof strCoverageName !== "string" ) {
           throw "Illegal Argument passed for strCoverageName";
      } else {
            arrRequest.push(new MQXmlNodeObject("CoverageName", strCoverageName));
      }

      if(mqdlqcDbLayers != null && mqdlqcDbLayers.getClassName() !== "MQDBLayerQueryCollection") {
           throw "Illegal Argument passed for MQRouteOptions";
      } else if(mqdlqcDbLayers == null) {
            mqdlqcDbLayers = new MQDBLayerQueryCollection();
      }
      arrRequest.push(mqdlqcDbLayers);

      if(mqfcFeatures != null && mqfcFeatures.getClassName() !== "MQFeatureCollection") {
      throw "Illegal Argument passed for MQFeatureCollection";
      } else if( mqfcFeatures == null ) {
      mqfcFeatures = new MQFeatureCollection();
      }
      arrRequest.push(mqfcFeatures);

      if(mqdcDisplayTypes != null && mqdcDisplayTypes.getClassName() !== "MQDTCollection") {
          throw "Illegal Argument passed for MQDTCollection";
     } else if(mqdcDisplayTypes == null ) {
          mqdcDisplayTypes = new MQDTCollection();
      }
      arrRequest.push(mqdcDisplayTypes);

      mqLogTime("MQExec.Search: Transaction Start");
      xmlDoc = this.doTransaction("Search", arrRequest, this.SEARCH_VERSION);
      mqLogTime("MQExec.Search: Transaction End");

      mqLogTime("MQExec.Search: Loading of Search results Start");
      strXml = mqXmlToStr(mqGetNode(xmlDoc, "/SearchResponse/FeatureCollection"));
      mqfcSearchResults.loadXml(strXml);
      mqLogTime("MQExec.Search: Loading of Search results End");

      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");

   };

   /**
    * Sets the mqRectLL appropriately for tilemaps routing
    *
    * @param {String} strSessionUID The unique Session ID.
    *
    * @param {MQRectLL} mqRectLL The bounding box for the route in the session.
    *
    * @type void
    * @private
    */

   MQExec.prototype.getRouteBoundingBoxFromSessionResponse = function(sessionId, mqRectLL) {

      var xmlDoc;
      var strXml;
      var arrRequest = new Array();

      if(mqRectLL == null) {
         throw "Null or Illegal Argument passed for MQRectLL";
      }
      arrRequest.push(new MQXmlNodeObject("SessionID",sessionId) );

      xmlDoc = this.doTransaction("GetRouteBoundingBoxFromSession", arrRequest);

      mqLogTime("MQExec.doRoute: Loading of MQRectLL Start");
      var nodes = xmlDoc.documentElement.childNodes;
      var ul = new MQLatLng();
      ul.loadXml(mqXmlToStr(nodes[0]));
      var lr = new MQLatLng();
      lr.loadXml(mqXmlToStr(nodes[1]));
      mqRectLL.setUpperLeft(ul);
      mqRectLL.setLowerRight(lr);
      mqLogTime("MQExec.doRoute: Loading of MQRectLL End");
   };

   /**
    * Returns a value indicating whether or not this object has been
    * initialized to a map server and port number.
    *
    * @return <code><b>true</b></code> if the port number or the Server
    *         name is not equal to their uninitialized values, <code><b>
    *         false</b></code> otherwise.
    *
    * @type boolean
    *
    * @private
    */

   MQExec.prototype.isAlive = function() {

      if( this.getServerPort() == -1 || this.getServerName() == "" )
         return false;
      return true;

   };


   /**
    * Returns the string which specifies the coverage information requested
    * directly via an http request.
    *
    * @param {int}  lType Type of info to return,
    * 0=CoverageInfo, 1=MapDataSelectorInfo.
    *
    * @return The string which specifies the server information.
    *
    * @type Document
    */

   MQExec.prototype.getServerInfo = function( lType ) {

      if (!this.isAlive())
         return null;

      var strReqXml;
      var xmlDoc;
      var strXml;
      var type = lType || 0;
      var arrRequest = new Array();
      if( typeof type !== "number" ) {
         throw "Illegal Argument passed for lType";
      } else {
         arrRequest.push(new MQXmlNodeObject("Type", type));
      }

      mqLogTime("MQExec.GetServerInfo: Transaction Start");
      xmlDoc = this.doTransaction("GetServerInfo", arrRequest);
      mqLogTime("MQExec.GetServerInfo: Transaction End");
      display("results", "Response", mqXmlToStr(xmlDoc), "", "mqDisplay");
      return xmlDoc;

   };


