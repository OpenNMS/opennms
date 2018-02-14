/* Copyright (c) 2006-2010 by OpenLayers Contributors (see authors.txt for 
 * full list of contributors). Published under the Clear BSD license.  
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Format/XML.js
 */

/**
 * Class: OpenLayers.Format.WMSCapabilities
 * Read WMS Capabilities.
 * 
 * Inherits from:
 *  - <OpenLayers.Format.XML>
 */
OpenLayers.Format.WMSCapabilities = OpenLayers.Class(OpenLayers.Format.XML, {
    
    /**
     * APIProperty: defaultVersion
     * {String} Version number to assume if none found.  Default is "1.1.1".
     */
    defaultVersion: "1.1.1",
    
    /**
     * APIProperty: version
     * {String} Specify a version string if one is known.
     */
    version: null,
    
    /**
     * Property: parser
     * {<OpenLayers.Format>} A cached versioned format used for reading.
     */
    parser: null,

    /**
     * Constructor: OpenLayers.Format.WMSCapabilities
     * Create a new parser for WMS capabilities.
     *
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     this instance.
     */
    initialize: function(options) {
        OpenLayers.Format.XML.prototype.initialize.apply(this, [options]);
        this.options = options;
    },

    /**
     * APIMethod: read
     * Read capabilities data from a string, and return a list of layers. 
     * 
     * Parameters: 
     * data - {String} or {DOMElement} data to read/parse.
     *
     * Returns:
     * {Array} List of named layers.
     */
    read: function(data) {
        if(typeof data == "string") {
            data = OpenLayers.Format.XML.prototype.read.apply(this, [data]);
        }
        var root = data.documentElement;
        var version = this.version || root.getAttribute("version") || this.defaultVersion;
        if(!this.parser || this.parser.version !== version) {
            var constr = OpenLayers.Format.WMSCapabilities[
                "v" + version.replace(/\./g, "_")
            ];
            if(!constr) {
                throw "Can't find a WMS capabilities parser for version " + version;
            }
            this.parser = new constr(this.options);
        }
        var capabilities = this.parser.read(data);
        capabilities.version = version;
        return capabilities;
    },
    
    CLASS_NAME: "OpenLayers.Format.WMSCapabilities" 

});