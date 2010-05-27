try{
   var testCommons = new MQObject();
   testCommons = null;
}catch(error){
   throw "You must include mqcommon.js or toolkit api script prior to mqobjects.js.";
}
/**
 * @fileoverview This file contains the bulk of the class that are used to communicate
 * with the server.
 */
// utility function to wrap null and class type
/**
 * Utility function to wrap null and class type errors
 * @type Boolean
 * @throws InvalidClassException This will throw an exception if the newObj is not the same type as
 * this object.
 * @throws NullPointerException This will throw an exception if the newObj is null and the allowNull
 * is false.
 */
function mqIsClass(strClassName, newObj, allowNull){
   if (newObj!==null){
      try{
         newObj.getClassName();
      } catch (error){
         throw "InvalidClassException";// " This class is not of type " + strClassName;
      }
      if(newObj.getClassName()===strClassName){
         return true;
      } else {
         throw "InvalidClassException";// " This class " + newObj.getClassName() + " is not of type " + strClassName;
      }
   } else if(allowNull) {
      return true;
   }
   throw "NullPointerException";// " This class is not allowed to be set to null";
};
// Begin class MQCONSTANTS
/**
 * Constructs a new MQConstants object.
 * @class Class that will contain all the constants for the
 * js api.
 *
 */
function MQConstants(){
   /**
    * Constant to hold a value for MQDistanceUnits<br>
    * MILES = 0
    * @type int
    */
   this.MQDISTANCEUNITS_MILES = 0;
   /**
    * Constant to hold a value for MQDistanceUnits<br>
    * KILOMETERS = 1
    * @type int
    */
   this.MQDISTANCEUNITS_KILOMETERS = 1;
   /**
    * Constant to hold value for radians conversion<br>
    * RADIANS = 0.01745329251994
    * @type Double
    */
   this.MQLATLNG_RADIANS = 0.01745329251994;
   /**
    * Constant to hold a value for validation check<br>
    * INVALID = 314159.265358
    * @type Double
    */
   this.MQLATLNG_INVALID = 314159.265358;
   /**
    * Constant to hold a value for validation check<br>
    * TOLERANCE = 314159.265358
    * @type Double
    */
   this.MQLATLNG_TOLERANCE = 0.000001;
   /**
    * Constant to hold a value for validation check<br>
    * INVALID = 32767
    * @type int
    */
   this.MQPOINT_INVALID = 32767;
   /**
    * Constant to hold a value for PI<br>
    * PI   = 3.14159265358979323846
    * @type Double
    */
   this.PI = 3.14159265358979323846;
   /**
    * Constant to hold a value for MQSearchCriteria<br>
    * MILES_PER_DEGREE_LAT = 68.9
    * @type Double
    */
   this.MQSEARCHCRITERIA_MILES_PER_DEGREE_LAT = 68.9;
   /**
    * Constant to hold a value for MQSearchCriteria<br>
    * DEGREES_LAT_PER_MILE = ( 1 / this.MILES_PER_DEGREE_LAT )
    * @type Double
    */
   this.MQSEARCHCRITERIA_DEGREES_LAT_PER_MILE = ( 1 / this.MQSEARCHCRITERIA_MILES_PER_DEGREE_LAT );
   /**
    * Constant to hold a value for calculations<br>
    * MILES_PER_LATITUDE = 69.170976
    * @type Double
    */
   this.DISTANCEAPPROX_MILES_PER_LATITUDE = 69.170976;
   /**
    * Constant to hold a value for calculations<br>
    * KILOMETERS_PER_MILE = 1.609347
    * @type Double
    */
   this.DISTANCEAPPROX_KILOMETERS_PER_MILE = 1.609347;
   /**
    * Constant to validate MQRouteTypes <br>
    * FASTEST = 0
    * @type int
    */
   this.MQROUTETYPE_FASTEST = 0;
   /**
    * Constant to validate MQRouteTypes <br>
    * SHORTEST = 1
    * @type int
    */
   this.MQROUTETYPE_SHORTEST = 1;
   /**
    * Constant to validate MQRouteTypes <br>
    * PEDESTRIAN = 2
    * @type int
    */
   this.MQROUTETYPE_PEDESTRIAN = 2;
   /**
    * Constant to validate MQRouteTypes <br>
    * OPTIMIZED = 3
    * @type int
    */
   this.MQROUTETYPE_OPTIMIZED = 3;
   /**
    * Constant to validate MQRouteTypes <br>
    * SELECT_DATASET_ONLY = 4
    * @type int
    */
   this.MQROUTETYPE_SELECT_DATASET_ONLY = 4;
   /**
    * Constant to validate MQNarrativeTypes <br>
    * DEFAULT = 0
    * @type int
    */
   this.MQNARRATIVETYPE_DEFAULT  = 0;
   /**
    * Constant to validate MQNarrativeTypes <br>
    * HTML = 1
    * @type int
    */
   this.MQNARRATIVETYPE_HTML = 1;
   /**
    * Constant to validate MQNarrativeTypes <br>
    * NONE = -1
    * @type int
    */
   this.MQNARRATIVETYPE_NONE = -1;
   /**
    * Constant for MQRouteOptions <br>
    * @type String
    */
   this.MQROUTEOPTIONS_AVOID_ATTRIBUTE_LIMITED_ACCESS = "Limited Access";
   /**
    * Constant for MQRouteOptions <br>
    * @type String
    */
   this.MQROUTEOPTIONS_AVOID_ATTRIBUTE_TOLL_ROAD = "Toll Road";
   /**
    * Constant for MQRouteOptions <br>
    * @type String
    */
   this.MQROUTEOPTIONS_AVOID_ATTRIBUTE_FERRY = "Ferry";
   /**
    * Constant for MQRouteOptions <br>
    * @type String
    */
   this.MQROUTEOPTIONS_AVOID_ATTRIBUTE_UNPAVED_ROAD = "Unpaved";
   /**
    * Constant for MQRouteOptions <br>
    * @type String
    */
   this.MQROUTEOPTIONS_AVOID_ATTRIBUTE_SEASONAL = "Approximate seasonal closure";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_ENGLISH = "English";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_FRENCH = "French";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_GERMAN = "German";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_ITALIAN = "Italian";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_SPANISH = "Spanish";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_DANISH = "Danish";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_DUTCH = "Dutch";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_NORWEGIAN = "Norwegian";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_SWEDISH = "Swedish";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_IBERIAN_SPANISH = "Iberian Spanish";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_BRITISH_ENGLISH = "British English";
   /**
    * Constant for MQRouteOption Language <br>
    * @type String
    */
   this.MQROUTEOPTIONS_LANGUAGE_IBERIAN_PORTUGUESE ="Iberian Portuguese";
   /**
    * Constant for MQRouteResultsCode type validation <br>
    * @type int
    */
   this.MQROUTERESULTSCODE_NOT_SPECIFIED    = -1;
   /**
    * Constant for MQRouteResultsCode type validation <br>
    * @type int
    */
   this.MQROUTERESULTSCODE_SUCCESS          = 0;
   /**
    * Constant for MQRouteResultsCode type validation <br>
    * @type int
    */
   this.MQROUTERESULTSCODE_INVALID_LOCATION = 1;
   /**
    * Constant for MQRouteResultsCode type validation <br>
    * @type int
    */
   this.MQROUTERESULTSCODE_ROUTE_FAILURE    = 2;
   /**
    * Constant for MQRouteResultsCode type validation <br>
    * @type int
    */
   this.MQROUTERESULTSCODE_NO_DATASET_FOUND = 3;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_NOT_SPECIFIED          = -1;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_SUCCESS                = 0;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_INVALID_LOCATION       = 1;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_ROUTE_FAILURE          = 2;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_NO_DATASET_FOUND       = 3;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_INVALID_OPTION         = 4;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_PARTIAL_SUCCESS        = 5;
   /**
    * Constant for MQRouteMatrixResultsCode type validation <br>
    * @type int
    */
   this.MQROUTEMATRIXRESULTSCODE_EXCEEDED_MAX_LOCATIONS = 6;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_NULL       = 0;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_NORTH      = 1;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_NORTH_WEST = 2;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_NORTH_EAST = 3;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_SOUTH      = 4;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_SOUTH_EAST = 5;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_SOUTH_WEST = 6;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_WEST       = 7;
   /**
    * Constant for MQManeuver heading values <br>
    * @type int
    */
   this.MQMANEUVER_HEADING_EAST       = 8;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_STRAIGHT       = 0;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_SLIGHT_RIGHT   = 1;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT          = 2;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_SHARP_RIGHT    = 3;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_REVERSE        = 4;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_SHARP_LEFT     = 5;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT           = 6;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_SLIGHT_LEFT    = 7;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT_UTURN    = 8;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT_UTURN     = 9;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT_MERGE    = 10;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT_MERGE     = 11;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT_ON_RAMP  = 12;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT_ON_RAMP   = 13;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT_OFF_RAMP = 14;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT_OFF_RAMP  = 15;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_RIGHT_FORK     = 16;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_LEFT_FORK      = 17;
   /**
    * Constant for MQManeuver turning values <br>
    * @type int
    */
   this.MQMANEUVER_TURN_TYPE_STRAIGHT_FORK  = 18;
   /**
    * Constant for MQManeuver attribute values <br>
    * @type int
    */
   this.MQMANEUVER_ATTRIBUTE_PORTIONS_TOLL                  = 1;
   /**
    * Constant for MQManeuver attribute values <br>
    * @type int
    */
   this.MQMANEUVER_ATTRIBUTE_PORTIONS_UNPAVED               = 2;
   /**
    * Constant for MQManeuver attribute values <br>
    * @type int
    */
   this.MQMANEUVER_ATTRIBUTE_POSSIBLE_SEASONAL_ROAD_CLOSURE = 4;
   /**
    * Constant for MQManeuver attribute values <br>
    * @type int
    */
   this.MQMANEUVER_ATTRIBUTE_GATE                           = 8;
   /**
    * Constant for MQManeuver attribute values <br>
    * @type int
    */
   this.MQMANEUVER_ATTRIBUTE_FERRY                          = 16;
   /**
    * Constant for MQCoordinateType attribute values <br>
    * @type int
    */
   this.MQCOORDINATETYPE_GEOGRAPHIC = 1;
   /**
    * Constant for MQCoordinateType attribute values <br>
    * @type int
    */
   this.MQCOORDINATETYPE_DISPLAY = 2;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_BEFORE_POLYGONS        = 3585;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_AFTER_POLYGONS         = 3586;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_BEFORE_TEXT            = 3588;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_AFTER_TEXT             = 3618;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_BEFORE_ROUTE_HIGHLIGHT = 3616;
   /**
    * Constant for MQDrawTrigger attribute values <br>
    * @type int
    */
   this.MQDRAWTRIGGER_AFTER_ROUTE_HIGHLIGHT  = 3617;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_SOLID        = 0;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_DASH         = 1;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_DOT          = 2;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_DASH_DOT     = 3;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_DASH_DOT_DOT = 4;
   /**
    * Constant for MQPenStyle attribute values
    * @type int
    */
   this.MQPENSTYLE_NONE         = 5;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_INVALID    = 0xffffffff;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_BLACK      = 0;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_BLUE       = 16711680;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_CYAN       = 16776960;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_DARK_GRAY  = 4210752;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_GRAY       = 8421504;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_GREEN      = 65280;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_LIGHT_GRAY = 12632256;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_MAGENTA    = 16711935;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_ORANGE     = 51455;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_PINK       = 11513855;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_RED        = 255;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_WHITE      = 16777215;
   /**
    * Constant for MQColorStyle attribute values
    * @type int
    */
   this.MQCOLORSTYLE_YELLOW     = 65535;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_SOLID       = 0;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_BDIAGONAL   = 1;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_CROSS       = 2;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_DIAG_CROSS  = 3;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_FDIAGONAL   = 4;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_HORIZONTAL  = 5;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_VERTICAL    = 6;
   /**
    * Constant for MQFillStyle attribute values
    * @type int
    */
   this.MQFILLSTYLE_NONE        = 7;
   /**
    * Constant for MQSymbolType attribute values
    * @type int
    */
   this.MQSYMBOLTYPE_RASTER = 0;
   /**
    * Constant for MQSymbolType attribute values
    * @type int
    */
   this.MQSYMBOLTYPE_VECTOR = 1;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_CENTER     = 1;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_LEFT       = 2;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_RIGHT      = 4;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_BASELINE   = 8;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_BOTTOM     = 16;
   /**
    * Constant for TextAlignment attribute values
    * @type int
    * @see MQTextPrimitive
    */
   this.MQTEXTALIGNMENT_TOP        = 32;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_INVALID    = -1;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_NORMAL     = 0;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_BOLD       = 1;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_BOXED      = 2;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_OUTLINED   = 4;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_ITALICS    = 8;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_UNDERLINE  = 16;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_STRIKEOUT  = 32;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_THIN       = 64;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_SEMIBOLD   = 128;
   /**
    * Constant for MQFontStyle attribute values
    * @type int
    */
   this.MQFONTSTYLE_MAX_VALUE  = 256;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_DT_NULL = 65532; // Null DT (meaning no DT specified)
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_ROAD              =  0;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_LINE              =  1;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_POLYGON           =  2;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_POINT             =  3;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_POI               =  4;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_SEED              =  5;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_DISPLAYLIST       =  6;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_APP               =  7;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_CT_XA                =  8;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_BT_LINE              =  0;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_BT_POLYGON           =  1;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_BT_POINT             =  2;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_BT_OTHER             =  3;
   /**
    * Constant for MQBaseDTStyle attribute values
    * @type int
    */
   this.MQBASEDTSTYLE_BT_XA                =  4;
   /**
    * Constant for MQFeatureSpeciferAttributeType attribute values
    * @type int
    */
   this.MQFEATURESPECIFERATTRIBUTETYPE_GEFID       = 0;
   /**
    * Constant for MQFeatureSpeciferAttributeType attribute values
    * @type int
    */
   this.MQFEATURESPECIFERATTRIBUTETYPE_NAME        = 1;
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_LOC        = 0;  // Location
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_INTR       = 1;  // Intersection
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_NEARBLK    = 2;  // Block - Nearest numbered
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_REPBLK     = 3;  // Block - Representative (Centroid)
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_BLOCK      = 4;  // Block
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA1        = 5;  // Admin - Country
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA2        = 6;  // Admin - Division
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA3        = 7;  // Admin - State
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA4        = 8;  // Admin - County
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA5        = 9;  // Admin - City
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA6        = 10; // Admin - Division (rarely used)
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_AA7        = 11; // Admin - smallest division (rarely used)
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_PC1        = 12; // Postal - Zip
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_PC2        = 13; // Postal -
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_PC3        = 14; // Postal -
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_PC4        = 15; // Postal -
   /**
    * Constant for MQMatchType attribute values
    * @type int
    */
   this.MQMATCHTYPE_POI        = 16; // POI
   /**
    * Constant for MQQualityType attribute values
    * @type int
    */
   this.MQQUALITYTYPE_EXACT  = 0;  // Exact
   /**
    * Constant for MQQualityType attribute values
    * @type int
    */
   this.MQQUALITYTYPE_GOOD   = 1;  // Good
   /**
    * Constant for MQQualityType attribute values
    * @type int
    */
   this.MQQUALITYTYPE_APPROX = 2;  // Approximate
}
var MQCONSTANT = new MQConstants();
// End class MQCONSTANTS

// Begin class MQERRORS
function MQErrors(){
   /** @type String
    */
   this.RECORDSET_GETFIELD_1 = "failure in getField -- m_curRec is not Pointing to an existing Record";
   /** @type String
    */
   this.RECORDSET_GETFIELD_2 = "failure in getField -- could not find strFieldName";
   /** @type String
    */
   this.RECORDSET_MOVEFIRST_1 = "failure in moveFirst -- Error Moving Cursor, RecordSet is Empty.";
   /** @type String
    */
   this.RECORDSET_MOVELAST_1 = "Error Moving Cursor, RecordSet is Empty.";
   /** @type String
    */
   this.RECORDSET_MOVENEXT_1 = "Error Moving Cursor, EOF was true.";
   /** @type String
    */
   this.RECORDSET_MOVENEXT_2 = "Error Moving Cursor, Unknown Error.";
   /** @type String
    */
   this.RECORDSET_MOVENEXT_3 = "Error Moving Cursor, RecordSet is Empty.";
}
var MQERROR = new MQErrors();
// End class MQERRORS

// Begin Class MQSign
/* Inheirit from MQObject */
MQSign.prototype = new MQObject();
MQSign.prototype.constructor = MQSign;
/**
 * Constructs a new MQSign object.
 * @class Contains information for geocoding and routing to and from
 * addresses.
 * @extends MQObject
 */
function MQSign () {
   MQObject.call(this);
   this.setM_Xpath("Sign");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSIGN()));
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSign.prototype.getClassName = function(){
      return "MQSign";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSign.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQSign.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {XML Node} node of the document.
    * @type void
    * @private
    */
   MQSign.prototype.loadXmlFromNode = function (node) {
      this.setM_XmlDoc(mqCreateXMLDocImportNode(node));
   };

   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQSign.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    *   initializes the object to its defaults.
    */
   MQSign.prototype.clear = function (){
      this.setType(0);
      this.setText("");
      this.setExtraText("");
      this.setDirection(MQCONSTANT.MQMANEUVER_HEADING_NULL);
   };
   /**
     * Sets Type.
     * @param {int} type The Type
     * @type void
     */
   MQSign.prototype.setType = function(type){
      this.setProperty("Type",type);
   };
   /**
    * Returns the type.
    *
    * <pre>
    * LIST OF VALUES:
    *    0    No sign
    *
    *    United States Road Shield Tokens
    *    1    Interstate
    *    2    US Highway
    *    3    State Route
    *    4    County Route / Parish Route
    *    5    Interstate Business
    *    10   Farm to Market (FM)
    *    11   Bureau of Indian Affairs (BIA)
    *
    *    Puerto Rico Road Shield Tokens
    *    15   Carr
    *    16   Carretera
    *
    *    US Virgin Islands Road Shield Tokens
    *    19   National Roads
    *
    *    Canada Road Shield Tokens
    *    20   Trans Canada Highway
    *    21   Autoroute
    *    22   Primary Provincial Route
    *    23   Provincial Secondary Route
    *    24   District Route / Regional Route
    *    25   Yellowhead Highway
    *
    *    United Kingdom Road Shield Tokens
    *    30   Motorway / M Road
    *    31   National / A Road
    *    33   Regional / B Road
    *    34   C Road
    *
    *    Brazil Road Shield Tokens
    *    35  National
    *    36  State
    *
    *    Mexico Road Shield Tokens
    *    40   Federal Route
    *    41   Route Type 2
    *    42   State Route
    *
    *    Argentina Road Shield Tokens
    *    45  National
    *    46  Provincial
    *
    *    European Road Shield Tokens
    *    50   European / E Road
    *
    *    Andorra Road Shield Tokens
    *    52   Carretera General
    *    53   Carretera Secundaria
    *
    *    Austria Road Shield Tokens
    *    55   Autobahnen
    *    56   Schnellstrassen
    *    57   Bundersstrassen
    *    58   Landestrassen / Bezirkstrassen / Privatstrassen des Bundes
    *
    *    Belgium Road Shield Tokens
    *    60   Autosnelwegen
    *    61   Gewestwegen
    *    62   Bretelles / Ring / Regionaalwegen
    *
    *    Denmark Road Shield Tokens
    *    65   Primaerrute
    *    66   Sekendaerrute
    *
    *    Finland Road Shield Tokens
    *    70   Valtatie
    *    71   Kantatie
    *    72   Seututie
    *    73   Muun Yleisen Tie
    *
    *    France / Monaco Road Shield Tokens
    *    75   Autoroutes
    *    76   Routes Nationales
    *    77   Departmentale Strategique / Routes Departmentales
    *    78   Voie Communale, Chemin Rural / Vicinal / Communal
    *    79   Local
    *
    *    Germany Road Shield Tokens
    *    80   Autobahnen
    *    81   Bundesstrassen
    *
    *    Italy / San Marino / Vatican City State Road Shield Tokens
    *    90   Autostrada
    *    91   Strada Statale
    *    92   Strada Provinciale / Strada Regionale
    *
    *    Luxembourg Road Shield Tokens
    *    95   Autoroute A
    *    96   Routes Nationales
    *    97   Chemins Repris
    *    98   Autoroute B
    *
    *    The Netherlands (Holland) Road Shield Tokens
    *    100  Autosnelwegen
    *    101  Nationale wegen
    *    102  Stadsroutenummmers
    *
    *    Norway Road Shield Tokens
    *    105  Riksveg
    *    106  Fylkeveg
    *
    *    Portugal Road Shield Tokens
    *    110  Autostrada
    *    111  Itinerario Principal
    *    112  Itinerario Complementar
    *    113  Estrada Nacional
    *    114  Estrada Municipal
    *
    *    Spain Road Shield Tokens
    *    115  Autopista
    *    116  Nacional
    *    117  Autonomica 1st order
    *    118  Autonomica 2nd order / Autonomica Local
    *    119  Autonomica Local
    *
    *    Sweden Road Shield Tokens
    *    120  Riksvag
    *    121  Lansvag
    *
    *    Switzerland / Liechtenstein Road Shield Tokens
    *    125  Autostrassen
    *    126  Haupstrassen
    *
    *    Czech Republic Road Shield Tokens
    *    130  Dalnice
    *    131  Silnice
    *
    *    Greece Road Shield Tokens
    *    140  National Roads
    *
    *    Hungary Road Shield Tokens
    *    150  Autopalya (Motorways)
    *    151  Orszagut (Country Roads)
    *
    *    Bosnia and Herzegovina Road Shield Tokens
    *    152  Autoput
    *    153  Magistralni Put
    *    154  Regionalni Put
    *
    *    Estonia Road Shield Tokens
    *    155  Pohimaantee  (Motorway)
    *    156  Tugimaantee  (National)
    *    157  Korvalmaantee (Regional)
    *
    *    Poland Road Shield Tokens
    *    160  Autostrada (Motorways
    *    161  Drogo Ekspresowa (Expressway)
    *    162  Drogo Krajowa (National Road)
    *    163  Drogo Wojewodzka (Regional Road)
    *
    *    Latvia Road Shield Tokens
    *    165  Galvenie Autoceii (Motorway)
    *    166  Skiras Autoceii  (1st class road)
    *    167  Skiras Autoceii  (2nd class road)
    *
    *    Bulgaria Road Shield Tokens
    *    168 Magistrala (Motorway)
    *    169 Put (National Road)
    *
    *    Slovak Republic Road Shield Tokens
    *    170  Dialnice
    *    171  Cesty
    *
    *    Albania Road Shield Tokens
    *    172  Route Type 2
    *    173  Route Type 3
    *
    *    Lithuania Road Shield Tokens
    *    175  Magistraliniai Keliai
    *    176  Krasto Keliai
    *    177  Rajoniniai Keliai
    *
    *    Slovenia Road Shield Tokens
    *    180  Avtocesta
    *    181  Hitra Cesta
    *    182  Glavna Cesta
    *    183  Regionalna Cesta
    *
    *    Croatia Road Shield Tokens
    *    185  Autocesta
    *    186  Drzavna
    *    187  Zupanijska Cesta
    *
    *    Romania Road Shield Tokens
    *    190  Class 2 (A1, A2)
    *    191  Class 3
    *    192  Class 4
    *
    *    United Arab Emirates Road Shield Tokens
    *    200  Federal
    *    201  Emirate
    *
    *    Oman Road Shield Tokens
    *    205  Class 1
    *    206  Class 2
    *
    *    Bahrain Road Shield Tokens
    *    208  Highway Class 2
    *
    *    Kuwait Road Shield Tokens
    *    210  Ring Road
    *    211  2 Digit State Route
    *
    *    Saudi Arabia Road Shield Tokens
    *    212  Class 1
    *    213  Class 2
    *    214  Class 3
    *
    *    Hong Kong Road Shield Tokens
    *    215  Highway
    *
    *    Taiwan Road Shield Tokens
    *    220  National Freeway
    *    221  Provincial Highway
    *    222  County Highway
    *
    *    Singapore Road Shield Tokens
    *    225  Level 1 Roads
    *    226  Level 3 Roads
    *
    *    Malaysia Road Shield Tokens
    *    230  Level 1 Roads
    *    231  Level 3 Roads
    *
    *    Autralia Road Shield Tokens
    *    235  Freeway
    *    236  Highway/ National
    *    237  State
    *
    *    New Zealand Road Shield Tokens
    *    240  State
    *
    *    South Africa Road Shield Tokens
    *    245  National
    *    246  Regional
    *    247  Metro
    *
    *    Exit sign information
    *    1001 Exit number
    * </pre>
    *
     * @return The Type
     * @type int
     */
   MQSign.prototype.getType = function(){
      return this.getProperty("Type");
   };
   /**
     * Sets Text.
     * @param {String} text The Text
     * @type void
     */
   MQSign.prototype.setText = function(text){
      this.setProperty("Text",text);
   };
   /**
     * Gets Text.
     * @return The Text
     * @type String
     */
   MQSign.prototype.getText = function(){
      return this.getProperty("Text");
   };
   /**
     * Sets ExtraText.
     * @param {String} extraText The ExtraText
     * @type void
     */
   MQSign.prototype.setExtraText = function(extraText){
      this.setProperty("ExtraText",extraText);
   };
   /**
     * Gets ExtraText.
     * @return The ExtraText
     * @type String
     */
   MQSign.prototype.getExtraText = function(){
      return this.getProperty("ExtraText");
   };
   /**
     * Sets Direction.
     * @param {Long} direction The Direction
     * @type void
     */
   MQSign.prototype.setDirection = function(direction){
      this.setProperty("Direction",direction);
   };
   /**
     * Gets Direction.
     * @return The Direction
     * @type Long
     */
   MQSign.prototype.getDirection = function(){
      return this.getProperty("Direction");
   };
// End class MQSign

// Begin class MQFeature
/* Inheirit from MQFeature */
MQFeature.prototype = new MQObject();
MQFeature.prototype.constructor = MQFeature;
/**
 * Constructs a new MQFeature object.
 * @class
 * @extends MQObject
 * @see MQPointFeature
 * @see MQPolygonFeature
 * @see MQLineFeature
 */
function MQFeature () {
   MQObject.call(this);
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQFeature.prototype.getClassName = function(){
      return "MQFeature";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFeature.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
     * Gets Distance.
     * @return The Distance value
     * @type String
     */
   MQFeature.prototype.getDistance = function(){
      return this.getProperty("Distance");
   };
   /**
     * Sets Distance.
     * @param {Double} dblDistance The Distance value
     * @type void
     */
   MQFeature.prototype.setDistance = function(dblDistance){
      this.setProperty("Distance",dblDistance);
   };
   /**
     * Gets name.
     * @return The name value
     * @type String
     */
   MQFeature.prototype.getName = function(){
      return this.getProperty("Name");
   };
   /**
     * Sets name.
     * @param {String} strName The name value
     * @type void
     */
   MQFeature.prototype.setName = function(strName){
      this.setProperty("Name",strName);
   };
   /**
     * Gets sourceLayerName.
     * @return The sourceLayerName value
     * @type String
     */
   MQFeature.prototype.getSourceLayerName = function(){
      return this.getProperty("SourceLayerName");
   };
   /**
     * Sets sourceLayerName.
     * @param {String} strSourceLayerName The sourceLayerName value
     * @type void
     */
   MQFeature.prototype.setSourceLayerName = function(strSourceLayerName){
      this.setProperty("SourceLayerName",strSourceLayerName);
   };
   /**
     * Gets key.
     * @return The key value
     * @type String
     */
   MQFeature.prototype.getKey = function(){
      return this.getProperty("Key");
   };
   /**
     * Sets key.
     * @param {String} strKey The key value
     * @type void
     */
   MQFeature.prototype.setKey = function(strKey){
      this.setProperty("Key",strKey);
   };
   /**
     * Sets GEFID.
     * @param {int} intGEFID the value to set GEFID to
     * @type void
     */
   MQFeature.prototype.setGEFID = function(intGEFID){
      this.setProperty("GEFID",intGEFID);
   };
   /**
     * Gets GEFID.
     * @return The GEFID value
     * @type int
     */
   MQFeature.prototype.getGEFID = function(){
      return this.getProperty("GEFID");
   };
   /**
     * Sets DT.
     * @param {int} intDT the value to set DT to
     * @type void
     */
   MQFeature.prototype.setDT = function(intDT){
      this.setProperty("DT",intDT);
   };
   /**
     * Gets DT.
     * @return The DT value
     * @type int
     */
   MQFeature.prototype.getDT = function(){
      return this.getProperty("DT");
   };
// End class MQFeature

// Begin class MQPointFeature
/* Inheirit from MQPointFeature */
MQPointFeature.prototype = new MQFeature();
MQPointFeature.prototype.constructor = MQPointFeature;
/**
 * Constructs a new MQPointFeature object.
 * @class
 * @extends MQFeature
 * @see MQLatLng
 * @see MQPoint
 */
function MQPointFeature () {
   MQObject.call(this);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPOINTFEATURE()));
   this.setM_Xpath("PointFeature");
   /**
    * Value to represent the MQLatLng
    * @type MQLatLng
    */
   this.m_CenterLatLng = new MQLatLng("CenterLatLng");
   /**
    * Value to represent the MQPoint
    * @type MQPoint
    */
   this.m_CenterPoint = new MQPoint("CenterPoint");
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPointFeature.prototype.getClassName = function(){
      return "MQPointFeature";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPointFeature.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPointFeature.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getCenterLatLng();
      var point = this.getCenterPoint();
      var lnode = mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng");
      var pnode = mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint");
      if(lnode !== null)
         latlng.loadXmlFromNode(lnode);
      if(pnode !== null)
         point.loadXmlFromNode(pnode);
   };

    /**
    * Assigns the xml that relates to this object.
    * @param node {XML Node} of the document.
    * @type void
    * @private
    */
   MQPointFeature.prototype.loadXmlFromNode = function (node) {
      this.setM_XmlDoc(mqCreateXMLDocImportNode(node));
      this.getCenterLatLng().setLatLng(this.getProperty("CenterLatLng/Lat"), this.getProperty("CenterLatLng/Lng"));
      var x = this.getProperty("CenterPoint/X");
      if(x!=="")
         this.getCenterPoint().setXY(x, this.getProperty("CenterPoint/Y"));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPointFeature.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenterLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      newNode = MQA.createXMLDoc(this.getCenterPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Returns the m_CenterLatLng object.
    * @return The m_CenterLatLng object.
    * @type MQLatLng
    */
   MQPointFeature.prototype.getCenterLatLng = function() {
      return this.m_CenterLatLng;
   };
   /**
    * Sets the m_CenterLatLng object.
    * @param {MQLatLng} latLng the MQLatLng to set m_CenterLatLng to.
    * @type void
    */
   MQPointFeature.prototype.setCenterLatLng = function(latLng) {
      this.m_CenterLatLng.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Returns the m_CenterPoint object.
    * @return The m_CenterPoint object.
    * @type MQPoint
    */
   MQPointFeature.prototype.getCenterPoint = function() {
      return this.m_CenterPoint;
   };
   /**
    * Sets the m_CenterPoint object.
    * @param {MQPoint} Point the MQPoint to set m_CenterPoint to.
    * @type void
    */
   MQPointFeature.prototype.setCenterPoint = function(Point) {
      this.m_CenterPoint.setXY(Point.getX(), Point.getY());
   };
// End class MQPointFeature

// Begin class MQPolygonFeature
/* Inheirit from MQPolygonFeature */
MQPolygonFeature.prototype = new MQPointFeature();
MQPolygonFeature.prototype.constructor = MQPolygonFeature;
/**
 * Constructs a new MQPolygonFeature object.
 * @class
 * @extends MQFeature
 * @see MQLatLng
 * @see MQLatLngCollection
 * @see MQPoint
 * @see MQPointCollection
 */
function MQPolygonFeature () {
   MQPointFeature.call(this);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPOLYGONFEATURE()));
   this.setM_Xpath("PolygonFeature");
   /**
    * Value to represent the MQLatLngCollection
    * @type MQLatLngCollection
    */
   var m_LatLngs = new MQLatLngCollection();
   m_LatLngs.setM_Xpath("LatLngs");
   /**
    * Returns the m_LatLngs object.
    * @return The m_LatLngs object.
    * @type MQLatLngCollection
    */
   this.getLatLngs = function() {
      return m_LatLngs;
   };
   /**
    * Sets the m_LatLngs object.
    * @param {MQLatLngCollection} latLngs the MQLatLngCollection to set m_LatLngs to.
    * @type void
    */
   this.setLatLngs = function(latLngs) {
      if (latLngs.getClassName()==="MQLatLngCollection"){
         m_LatLngs.removeAll();
         m_LatLngs.append(latLngs);
      } else {
         alert("failure in setLatLngs");
         throw "failure in setLatLngs";
      }
   };
   /**
    * Value to represent the MQPointCollection
    * @type MQPointCollection
    */
   var m_Points = new MQPointCollection();
   m_Points.setM_Xpath("Points");
   /**
    * Returns the m_Points object.
    * @return The m_Points object.
    * @type MQPointCollection
    */
   this.getPoints = function() {
      return m_Points;
   };
   /**
    * Sets the m_Points object.
    * @param {MQPointCollection} pts the MQPointCollection to set m_Points to.
    * @type void
    */
   this.setPoints = function(pts) {
      m_Points.removeAll();
      m_Points.append(pts);
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPolygonFeature.prototype.getClassName = function(){
      return "MQPolygonFeature";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPolygonFeature.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPolygonFeature.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getCenterLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")));
      var point = this.getCenterPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
      var latlngs = this.getLatLngs();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         latlngs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
      var points = this.getPoints();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")!==null)
         points.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPolygonFeature.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenterLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      newNode = MQA.createXMLDoc(this.getCenterPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      newNode = MQA.createXMLDoc(this.getPoints().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Points"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQPolygonFeature

// Begin class MQLineFeature
/* Inheirit from MQLineFeature */
MQLineFeature.prototype = new MQPolygonFeature();
MQLineFeature.prototype.constructor = MQLineFeature;
/**
 * Constructs a new MQLineFeature object.
 * @class
 * @extends MQFeature
 * @see MQLatLng
 * @see MQLatLngCollection
 * @see MQPoint
 * @see MQPointCollection
 */
function MQLineFeature () {
   MQPolygonFeature.call(this);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getLINEFEATURE()));
   this.setM_Xpath("LineFeature");
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQLineFeature.prototype.getClassName = function(){
      return "MQLineFeature";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQLineFeature.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQLineFeature.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getCenterLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")));
      var point = this.getCenterPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
      var latlngs = this.getLatLngs();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         latlngs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
      var points = this.getPoints();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")!==null)
         points.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQLineFeature.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenterLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      newNode = MQA.createXMLDoc(this.getCenterPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      newNode = MQA.createXMLDoc(this.getPoints().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Points"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
     * Gets LeftPostalCode.
     * @return The LeftPostalCode value
     * @type String
     */
   MQLineFeature.prototype.getLeftPostalCode = function(){
      return this.getProperty("LeftPostalCode");
   };
   /**
     * Sets LeftPostalCode.
     * @param {String} strVal The LeftPostalCode value
     * @type void
     */
   MQLineFeature.prototype.setLeftPostalCode = function(strVal){
      this.setProperty("LeftPostalCode",strVal);
   };
   /**
     * Gets RightPostalCode.
     * @return The RightPostalCode value
     * @type String
     */
   MQLineFeature.prototype.getRightPostalCode = function(){
      return this.getProperty("RightPostalCode");
   };
   /**
     * Sets RightPostalCode.
     * @param {String} strVal The RightPostalCode value
     * @type void
     */
   MQLineFeature.prototype.setRightPostalCode = function(strVal){
      this.setProperty("RightPostalCode",strVal);
   };
   /**
     * Gets LeftAddressHi.
     * @return The LeftAddressHi value
     * @type String
     */
   MQLineFeature.prototype.getLeftAddressHi = function(){
      return this.getProperty("LeftAddressHi");
   };
   /**
     * Sets LeftAddressHi.
     * @param {String} strVal The LeftAddressHi value
     * @type void
     */
   MQLineFeature.prototype.setLeftAddressHi = function(strVal){
      this.setProperty("LeftAddressHi",strVal);
   };
   /**
     * Gets RightAddressHi.
     * @return The RightAddressHi value
     * @type String
     */
   MQLineFeature.prototype.getRightAddressHi = function(){
      return this.getProperty("RightAddressHi");
   };
   /**
     * Sets RightAddressHi.
     * @param {String} strVal The RightAddressHi value
     * @type void
     */
   MQLineFeature.prototype.setRightAddressHi = function(strVal){
      this.setProperty("RightAddressHi",strVal);
   };
   /**
     * Gets LeftAddressLo.
     * @return The LeftAddressLo value
     * @type String
     */
   MQLineFeature.prototype.getLeftAddressLo = function(){
      return this.getProperty("LeftAddressLo");
   };
   /**
     * Sets LeftAddressLo.
     * @param {String} strVal The LeftAddressLo value
     * @type void
     */
   MQLineFeature.prototype.setLeftAddressLo = function(strVal){
      this.setProperty("LeftAddressLo",strVal);
   };
   /**
     * Gets RightAddressLo.
     * @return The RightAddressLo value
     * @type String
     */
   MQLineFeature.prototype.getRightAddressLo = function(){
      return this.getProperty("RightAddressLo");
   };
   /**
     * Sets RightAddressLo.
     * @param {String} strVal The RightAddressLo value
     * @type void
     */
   MQLineFeature.prototype.setRightAddressLo = function(strVal){
      this.setProperty("RightAddressLo",strVal);
   };
// End class MQLineFeature

// Begin class MQLocation
/* Inheirit from MQLocation */
MQLocation.prototype = new MQObject();
MQLocation.prototype.constructor = MQLocation;
/**
 * Constructs a new MQLocation object.
 * @class Contains properties common to MQAddress
 * objects. A MQLocation can also be geocoded, but only to the city
 * level.
 * @extends MQObject
 */
function MQLocation () {
   MQObject.call(this);
   this.setM_Xpath("Location");
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQLocation.prototype.getClassName = function(){
      return "MQLocation";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQLocation.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQLocation.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQLocation.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
     * Gets admin area.
     * @param  {int} intIndex Which admin area (1 - 7)
     * @return The admin area value for intIndex
     * @type String
     */

// End class MQLocation

// Begin Class MQAddress
/* Inheirit from MQLocation */
MQAddress.prototype = new MQLocation();
MQAddress.prototype.constructor = MQAddress;
/**
 * Constructs a new MQAddress object.
 * @class Contains information for geocoding and routing to and from
 * addresses.
 * @extends MQLocation
 */
function MQAddress () {
   MQLocation.call(this);
   this.setM_Xpath("Address");
   if(this.getClassName() === "MQAddress"){
      this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getADDRESS()));
   }
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQAddress.prototype.getClassName = function(){
      return "MQAddress";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQAddress.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQAddress.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQAddress.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
     * Sets street.
     * @param {String} strStreet  street Address
     * @type void
     */

   MQAddress.prototype.getAdminArea = function(intIndex){
   return this.getProperty("AdminArea" + intIndex);
   };
   /**
   * Sets admin area.
   * @param  {int} intIndex Which admin area (1 - 7)
   * @param {String} strAdminArea The admin area value for intIndex
   * @type void
   */
   MQAddress.prototype.setAdminArea = function(intIndex, strAdminArea){
   this.setProperty("AdminArea" + intIndex,strAdminArea);
   };
   /**
   * Gets country.
   * @return The country value
   * @type String
   */
   MQAddress.prototype.getCountry = function(){
   return this.getProperty("AdminArea1");
   };
   /**
   * Sets country.
   * @param {String} strCountry The country value
   * @type void
   */
   MQAddress.prototype.setCountry = function(strCountry){
   this.setProperty("AdminArea1",strCountry);
   };
   /**
   * Gets county.
   * @return The county value
   * @type String
   */
   MQAddress.prototype.getCounty = function(){
   return this.getProperty("AdminArea4");
   };
   /**
   * Sets county.
   * @param {String} strCounty The county value
   * @type void
   */
   MQAddress.prototype.setCounty = function(strCounty){
   this.setProperty("AdminArea4",strCounty);
   };
   /**
   * Gets city.
   * @return The city value
   * @type String
   */
   MQAddress.prototype.getCity = function(){
   return this.getProperty("AdminArea5");
   };
   /**
   * Sets city.
   * @param {String} strCity The city value
   * @type void
   */
   MQAddress.prototype.setCity = function(strCity){
   this.setProperty("AdminArea5",strCity);
   };
   /**
   * Gets postal code.
   * @return The postal code value
   * @type String
   */
   MQAddress.prototype.getPostalCode = function(){
   return this.getProperty("PostalCode");
   };
   /**
   * Sets postal code.
   * @param {String} strPostalCode The postal code value
   * @type void
   */
   MQAddress.prototype.setPostalCode = function(strPostalCode){
   this.setProperty("PostalCode",strPostalCode);
   };
   /**
   * Gets state.
   * @return The state value
   * @type String
   */
   MQAddress.prototype.getState = function(){
   return this.getProperty("AdminArea3");
   };
   /**
   * Sets state.
   * @param {String} strState The state value
   * @type void
   */
   MQAddress.prototype.setState = function(strState){
   this.setProperty("AdminArea3",strState);
   };

   MQAddress.prototype.setStreet = function(strStreet){
      this.setProperty("Street",strStreet);
   };
   /**
     * Gets street.
     * @return The street
     * @type String
     */
   MQAddress.prototype.getStreet = function(){
      return this.getProperty("Street");
   };
// End class MQAddress


// Begin Class MQSingleLineAddress
/* Inheirit from MQLocation */
MQSingleLineAddress.prototype = new MQLocation();
MQSingleLineAddress.prototype.constructor = MQSingleLineAddress;

/**
 * Constructs a new MQSingleLineAddress object.
 * @class Contains information for Singleline geocoding and routing to and from
 * addresses.
 * @extends MQLocation
 */
function MQSingleLineAddress () {
   MQLocation.call(this);
   this.setM_Xpath("SingleLineAddress");
   if(this.getClassName() === "MQSingleLineAddress"){
      this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSINGLELINEADDRESS()));
   }
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSingleLineAddress.prototype.getClassName = function(){
      return "MQSingleLineAddress";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSingleLineAddress.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQSingleLineAddress.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQSingleLineAddress.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
     * Sets Address.
     * @param {String} strAddress  Singleline Address
     * @type void
     */
   MQSingleLineAddress.prototype.setAddress = function(strAddress){
      this.setProperty("Address",strAddress);
   };
   /**
     * Gets Country.
     * @return The Country
     * @type String
     */
   MQSingleLineAddress.prototype.getAddress = function(){
      return this.getProperty("Address");
   };
   MQSingleLineAddress.prototype.setCountry = function(strCountry){
      this.setProperty("Country",strCountry);
   };
   /**
     * Gets Country.
     * @return The Country
     * @type String
     */
   MQSingleLineAddress.prototype.getCountry = function(){
      return this.getProperty("Country");
   };
// End class MQSingleLineAddress


// Begin Class MQGeoAddress
/* Inheirit from MQAddress */
MQGeoAddress.prototype = new MQAddress();
MQGeoAddress.prototype.constructor = MQGeoAddress;
/**
 * Constructs a new MQGeoAddress object.
 * @class Contains the results of geocoding an address.
 * @extends MQAddress
 * @see MQLocation
 * @see MQAddress
 * @see MQLatLng
 */
function MQGeoAddress () {
   MQAddress.call(this);
   this.setM_Xpath("GeoAddress");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getGEOADDRESS()));
   /**
    * Value to represent the MQLatLng
    * @type MQLatLng
    *
    */
   var m_MQLatLng = new MQLatLng();
   /**
    * Returns the m_MQLatLng object.
    * @return The m_MQLatLng object.
    * @type MQLatLng
    *
    */
   this.getMQLatLng = function() {
      return m_MQLatLng;
   };
   /**
    * Sets the m_MQLatLng object.
    * @param {MQLatLng} latLng the Document to set m_MQLatLng to.
    * @type void
    *
    */
   this.setMQLatLng = function(latLng) {
      m_MQLatLng = latLng;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQGeoAddress.prototype.getClassName = function(){
      return "MQGeoAddress";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQGeoAddress.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQGeoAddress.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var lat = this.getProperty("LatLng/Lat");
      var lng = this.getProperty("LatLng/Lng");
      this.getMQLatLng().setLatLng(lat,lng);
   };
   /**
    * Build an xml string that represents this object. Because this is
    * a complex object we need to completely rebuild the xml string.
    * @return The xml string.
    * @type String
    *
    */
   MQGeoAddress.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getMQLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLng"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
     * Sets distAlong.
     * @param {Double} dblDistAlong the value to set distAlong to
     * @type void
     */
   MQGeoAddress.prototype.setDistAlong = function(dblDistAlong){
      this.setProperty("DistAlong",dblDistAlong);
   };
   /**
     * Gets distAlong.
     * @return The distAlong value
     * @type Double
     */
   MQGeoAddress.prototype.getDistAlong = function(){
      return this.getProperty("DistAlong");
   };
   /**
     * Sets GEFID.
     * @param {int} intGEFID the value to set GEFID to
     * @type void
     */
   MQGeoAddress.prototype.setGEFID = function(intGEFID){
      this.setProperty("GEFID",intGEFID);
   };
   /**
     * Gets GEFID.
     * @return The GEFID value
     * @type int
     */
   MQGeoAddress.prototype.getGEFID = function(){
      return this.getProperty("GEFID");
   };
   /**
     * Sets ResultCode.
     * @param {String} strResultCode the value to set ResultCode to
     * @type void
     */
   MQGeoAddress.prototype.setResultCode = function(strResultCode){
      this.setProperty("ResultCode",strResultCode);
   };
   /**
     * Gets ResultCode.
     * @return The ResultCode value
     * @type String
     */
   MQGeoAddress.prototype.getResultCode = function(){
      return this.getProperty("ResultCode");
   };
   /**
     * Sets SourceId.
     * @param {String} strSourceId the value to set SourceId to
     * @type void
     */
   MQGeoAddress.prototype.setSourceId = function(strSourceId){
      this.setProperty("SourceId",strSourceId);
   };
   /**
     * Gets SourceId.
     * @return The SourceId value
     * @type String
     */
   MQGeoAddress.prototype.getSourceId = function(){
      return this.getProperty("SourceId");
   };
// End Class MQGeoAddress

// Begin Class MQManeuver
/* Inheirit from MQObject */
MQManeuver.prototype = new MQObject();
MQManeuver.prototype.constructor = MQManeuver;
/**
 * Constructs a new MQManeuver object.
 * @class This object will hold each maneuver of a trekroute returned from a route.
 * @extends MQObject
 * @see MQLatLngCollection
 * @see MQSignCollection
 * @see MQStringCollection
 */
function MQManeuver () {
   MQObject.call(this);
   this.setM_Xpath("Maneuver");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getMANEUVER()));
   /**
    * Value to represent the collection of streets.
    * @type MQStringCollection
    */
   var m_Streets = new MQStringCollection("Item");
   m_Streets.setM_Xpath("Streets");
   /**
    * Returns the m_Streets collection.
    * @return The m_Streets collection.
    * @type MQStringCollection
    */
   this.getStreets = function() {
      return m_Streets;
   };
   /**
    * Sets the m_Streets collection.
    * @param {MQStringCollection} streets The collection to set m_Streets to.
    * @type void
    */
   this.setStreets = function(streets) {
      m_Streets.removeAll();
      m_Streets.append(streets);
   };
   /**
    * Value to represent the collection of shape points.
    * @type MQLatLngCollection
    */
   var m_ShapePoints = new MQLatLngCollection();
   m_ShapePoints.setM_Xpath("ShapePoints");
   /**
    * Returns the m_ShapePoints collection.
    * @return The m_ShapePoints collection.
    * @type MQLatLngCollection
    */
   this.getShapePoints = function() {
      return m_ShapePoints;
   };
   /**
    * Sets the m_ShapePoints collection.
    * @param {MQLatLngCollection} shapePoints The collection to set m_ShapePoints to.
    * @type void
    */
   this.setShapePoints = function(shapePoints) {
      if (shapePoints.getClassName()==="MQLatLngCollection"){
         m_ShapePoints.removeAll();
         m_ShapePoints.append(shapePoints);
      }
      else {
         alert("failure in setShapePoints");
         throw "failure in setShapePoints";
      }
   };
   /**
    * Value to represent the collection of GEFIDs
    * @type MQIntCollection
    */
   var m_GEFIDs = new MQIntCollection("Item");
   m_GEFIDs.setM_Xpath("GEFIDs");
   /**
    * Returns the m_GEFIDs collection.
    * @return The m_GEFIDs collection.
    * @type MQIntCollection
    */
   this.getGEFIDs = function() {
      return m_GEFIDs;
   };
   /**
    * Sets the m_GEFIDs collection.
    * @param {MQIntCollection} GEFIDs The collection to set m_GEFIDs to.
    * @type void
    */
   this.setGEFIDs = function(GEFIDs) {
      m_GEFIDs.removeAll();
      m_GEFIDs.append(GEFIDs);
   };
   /**
    * Value to represent the collection of signs
    * @type MQSignCollection
    */
   var m_Signs = new MQSignCollection("Sign");
   m_Signs.setM_Xpath("Signs");
   /**
    * Returns the m_Signs collection.
    * @return The m_Signs collection.
    * @type MQSignCollection
    */
   this.getSigns = function() {
      return m_Signs;
   };
   /**
    * Sets the m_Signs collection.
    * @param {MQSignCollection} signs The collection to set m_Signs to.
    * @type void
    */
   this.setSigns = function(signs) {
      m_Signs.removeAll();
      m_Signs.append(signs);
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQManeuver.prototype.getClassName = function(){
      return "MQManeuver";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQManeuver.prototype.getObjectVersion = function(){
      return 1;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQManeuver.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var streets = this.getStreets();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Streets")!==null)
         streets.loadXmlFromNode(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Streets"));
      var shapes = this.getShapePoints();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ShapePoints")!==null)
         shapes.loadXmlFromNode(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ShapePoints"));
      var gefids = this.getGEFIDs();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/GEFIDs")!==null)
         gefids.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/GEFIDs")));
      var signs = this.getSigns();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Signs")!==null)
        signs.loadXmlFromNode(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Signs"));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQManeuver.prototype.saveXml = function () {
      // get inner object nodes
        var newNode = MQA.createXMLDoc(this.getStreets().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Streets"));
        newNode = MQA.createXMLDoc(this.getShapePoints().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "ShapePoints"));
        newNode = MQA.createXMLDoc(this.getGEFIDs().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "GEFIDs"));
        newNode = MQA.createXMLDoc(this.getSigns().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Signs"));
        return mqXmlToStr(this.getM_XmlDoc());

   };
 /**
   * Sets the turn type of this Maneuver.
   * @param {int} type that is a valid turn type
   * Valid types are the constants starting with K_
   * @type void
   */
   MQManeuver.prototype.setTurnType = function(type){
      this.setProperty("TurnType",type);
   };

 /**
   * Returns the turn type of this Maneuver.
   * @return The turn type of this Maneuver.
   * @type int
   */
   MQManeuver.prototype.getTurnType = function(){
      return this.getProperty("TurnType");
   };

   /**
   * Sets the distance of this Maneuver.
   * @param {Float} distance The distance
   * @type void
   */
   MQManeuver.prototype.setDistance = function(distance){
      this.setProperty("Distance",distance);
   };

   /**
   * Returns the distance of this Maneuver.
   * @return The distance of this Maneuver.
   * @type Float
   */
   MQManeuver.prototype.getDistance = function(){
      return this.getProperty("Distance");
   };

   /**
   * Sets the seconds to travel this Maneuver.
   * @param {int} seconds The seconds to travel this Maneuver.
   * @type void
   */
   MQManeuver.prototype.setTime = function(seconds){
      this.setProperty("Time",seconds);
   };

   /**
   * Returns the seconds to travel this Maneuver.
   * @return The seconds to travel this Maneuver.
   * @type int
   */
   MQManeuver.prototype.getTime = function(){
      return this.getProperty("Time");
   };
   /**
   * Sets an integer representing the direction for this Maneuver.
   * @param {int} direction The direction for this Maneuver
   * @type void
   */
   MQManeuver.prototype.setDirection = function(direction){
      this.setProperty("Direction",direction);
   };
   /**
   * Returns an integer representing the direction for this Maneuver.
   * @return The integer representing the direction for this Maneuver.
   * @type int
   */
   MQManeuver.prototype.getDirection = function(){
      return this.getProperty("Direction");
   };
   /**
   * Returns a string representing the direction for this Maneuver.
   * @return The string representing the direction for this Maneuver.
   * @type String
   */
   MQManeuver.prototype.getDirectionName = function(){
      switch (parseInt(this.getDirection()))
      {
      case MQCONSTANT.MQMANEUVER_HEADING_NORTH:      return "North";
      case MQCONSTANT.MQMANEUVER_HEADING_NORTH_WEST: return "Northwest";
      case MQCONSTANT.MQMANEUVER_HEADING_NORTH_EAST: return "Northeast";
      case MQCONSTANT.MQMANEUVER_HEADING_SOUTH:      return "South";
      case MQCONSTANT.MQMANEUVER_HEADING_SOUTH_EAST: return "Southeast";
      case MQCONSTANT.MQMANEUVER_HEADING_SOUTH_WEST: return "Southwest";
      case MQCONSTANT.MQMANEUVER_HEADING_WEST:       return "West";
      case MQCONSTANT.MQMANEUVER_HEADING_EAST:       return "East";
      default:                                       return "";
      }

   };
   /**
   * Set the attributes associated with this maneuver.
   * @param {int} attributes attributes associated with this maneuver.
   * @type void
   */
   MQManeuver.prototype.setAttributes = function(attributes){
      this.setProperty("Attributes",attributes);
   };
   /**
   * Get the attributes associated with this maneuver.
   * @return The attributes associated with this maneuver.
   * @type int
   */
   MQManeuver.prototype.getAttributes = function(){
      return this.getProperty("Attributes");
   };
   /**
   * Sets a string representing the narrative for this Maneuver.
   * @param {String} narrative a string representing the narrative for this Maneuver
   * @type void
   */
   MQManeuver.prototype.setNarrative = function(narrative){
      this.setProperty("Narrative",narrative);
   };
   /**
   * Returns a string representing the narrative for this Maneuver.
   * @returns a string representing the narrative for this Maneuver
   * @type String
   */
   MQManeuver.prototype.getNarrative = function(){
      return this.getProperty("Narrative");
   };
// End class MQManeuver

// Begin Class MQTrekRoute
/* Inheirit from MQObject */
MQTrekRoute.prototype = new MQObject();
MQTrekRoute.prototype.constructor = MQTrekRoute;
/**
 * Constructs a new MQTrekRoute object.
 * @class This object will hold each maneuver of a trekroute returned from a route.
 * @extends MQObject
 * @see MQLatLngCollection
 * @see MQSignCollection
 * @see MQStringCollection
 */
function MQTrekRoute () {
   MQObject.call(this);
   this.setM_Xpath("TrekRoute");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getTREKROUTE()));
   /**
    * Value to represent the collection of streets.
    * @type MQManeuverCollection
    *
    */
   var maneuverList = new MQManeuverCollection("Maneuver");
   maneuverList.setM_Xpath("Maneuvers");
   /**
    * Returns the maneuverList collection.
    * @return The maneuverList collection.
    * @type MQManeuverCollection
    */
   this.getManeuvers = function() {
      return maneuverList;
   };
   /**
    * Value to represent the collection of streets.
    * @type MQManeuverCollection
    *
    */
   var shapePoints = null;
   /**
    * Returns the shapePoints collection made by appending all the maneuver shape point collections.
    * @return The shapePoints collection.
    * @type MQLatLngCollection
    */
   this.getShapePoints = function() {
      // calc and set if null
      if (shapePoints === null){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var man = 0; man < this.getManeuvers().getSize(); man++) {
            shapePoints.append(this.getManeuvers().get(man).getShapePoints());
            distance += parseFloat(this.getManeuvers().get(man).getDistance());
            time += parseInt(this.getManeuvers().get(man).getTime());
         }
      }
      return shapePoints;
   };
   /**
    * Value of the total distance for this maneuver.
    * @type Float
    */
   var distance = null;
   /**
    * Returns the total distance for this maneuver.
    * @return The total distance for this maneuver.
    * @type Float
    */
   this.getDistance = function() {
      // calc and set if null
      if(distance === null){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var man = 0; man < this.getManeuvers().getSize(); man++) {
            shapePoints.append(this.getManeuvers().get(man).getShapePoints());
            distance += parseFloat(this.getManeuvers().get(man).getDistance());
            time += parseInt(this.getManeuvers().get(man).getTime());
         }
      }
      return distance;
   };
   /**
    * Value of the total time for this maneuver.
    * @type int
    */
   var time = null;
   /**
    * Returns the total time for this maneuver.
    * @return The total time for this maneuver.
    * @type int
    */
   this.getTime = function(){
      // calc and set if null
      if(time === null){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var man = 0; man < this.getManeuvers().getSize(); man++) {
            shapePoints.append(this.getManeuvers().get(man).getShapePoints());
            distance += parseFloat(this.getManeuvers().get(man).getDistance());
            time += parseInt(this.getManeuvers().get(man).getTime());
         }
      }
      return time;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQTrekRoute.prototype.getClassName = function(){
      return "MQTrekRoute";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQTrekRoute.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQTrekRoute.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
     var maneuvers = this.getManeuvers();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Maneuvers")!==null)
         maneuvers.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Maneuvers")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQTrekRoute.prototype.saveXml = function () {
      // get inner object nodes
        var newNode = MQA.createXMLDoc(this.getManeuvers().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Maneuvers"));
        return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQTrekRoute

// Begin class MQGeocodeOptions
/* Inheirit from MQObject */
MQGeocodeOptions.prototype = new MQObject();
MQGeocodeOptions.prototype.constructor = MQGeocodeOptions;
/**
 * @class Class for specifying geocode parameters.
 * @extends MQObject
 * @see MQMatchType
 * @see MQQualityType
 */
function MQGeocodeOptions (){
   MQObject.call(this);
   this.setM_Xpath("GeocodeOptions");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getGEOCODEOPTIONS()));
   /**
    * Value to represent the MQMatchType
    * @type MQMatchType
    *
    */
   var m_MatchType = new MQMatchType(0);
   /**
    * Returns the m_MatchType object.
    * @return The m_MatchType object.
    * @type MQMatchType
    *
    */
   this.getMatchType = function(){
      return m_MatchType;
   };
   /**
    * Sets the m_MatchType.
    * @param {MQMatchType} MatchType The object to set m_MatchType to.
    * @type void
    *
    */
   this.setMatchType = function(MatchType){
      m_MatchType = MatchType;
   };
   /**
    * Value to represent the MQQualityType
    * @type MQQualityType
    *
    */
   var m_QualityType = new MQQualityType(0);
   /**
    * Returns the m_QualityType object.
    * @return The m_QualityType object.
    * @type MQQualityType
    *
    */
   this.getQualityType = function(){
      return m_QualityType;
   };
   /**
    * Sets the m_QualityType.
    * @param {MQQualityType} QualityType The object to set m_QualityType to.
    * @type void
    *
    */
   this.setQualityType = function(QualityType){
      m_QualityType = QualityType;
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQGeocodeOptions.prototype.getClassName = function(){
      return "MQGeocodeOptions";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQGeocodeOptions.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQGeocodeOptions.prototype.loadXml = function (strXml){
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      this.setMatchType(new MQMatchType(Math.floor(this.getProperty("MatchType"))));
      this.getQualityType(new MQQualityType(Math.floor(this.getProperty("QualityType"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQGeocodeOptions.prototype.saveXml = function (){
        this.setProperty("MatchType", this.getMatchType().intValue());
        this.setProperty("QualityType", this.getQualityType().intValue());
        return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the name of the coverage that this option applies to
    * @param {String} newCoverageName The coverage name
    * @type void
    */
   MQGeocodeOptions.prototype.setCoverageName = function(newCoverageName){
      this.setProperty("CoverageName", newCoverageName);
   };
   /**
    * Gets the name of the coverage that this option applies to
    * @return The name of the coverage that this option applies to
    * @type String
    */
   MQGeocodeOptions.prototype.getCoverageName = function(){
      return this.getProperty("CoverageName");
   };
   /**
    * Sets the maximum matches to be returned for this object.
    * @param {int} nMax The maximum number of matches that are to be returned by
    * the search.
    * @type void
    */
   MQGeocodeOptions.prototype.setMaxMatches = function(nMax){
      this.setProperty("MaxMatches", nMax);
   };
   /**
    * Gets the maximum matches to be returned for this object.
    * @return The maximum number of matches that are to be returned by
    * the search
    * @type int
    */
   MQGeocodeOptions.prototype.getMaxMatches = function(){
      return this.getProperty("MaxMatches");
   };
// End class MQGeocodeOptions

// Begin class MQRouteOptions
/* Inheirit from MQObject */
MQRouteOptions.prototype = new MQObject();
MQRouteOptions.prototype.constructor = MQRouteOptions;
/**
 * @class This class is used to set various options that will affect the
 * type of route result returned from the DoRoute function.
 * @extends MQObject
 * @see MQAutoRouteCovSwitch
 * @see MQIntCollection
 * @see MQDistanceUnits
 * @see MQNarrativeType
 * @see MQRouteType
 * @see MQStringCollection
 */
function MQRouteOptions (){
   MQObject.call(this);
   this.setM_Xpath("RouteOptions");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getROUTEOPTIONS()));
   /**
    * Value to represent the Avoid Attribute List
    * @type MQStringCollection
    *
    */
   var m_AvoidAttrList = new MQStringCollection();
   m_AvoidAttrList.setM_Xpath("AvoidAttributeList");
   /**
    * Returns the m_AvoidAttrList collection.
    * @return The m_AvoidAttrList collection.
    * @type MQStringCollection
    *
    */
   this.getAvoidAttrList = function(){
      return m_AvoidAttrList;
   };
   /**
    * Sets the m_AvoidAttrList collection.
    * @param {MQStringCollection} AvoidAttrList The collection to set m_AvoidAttrList to.
    * @type void
    *
    */
   this.setAvoidAttrList = function(avoidAttrList){
      m_AvoidAttrList.removeAll();
        m_AvoidAttrList.append(avoidAttrList);
   };
   /**
    * Value to represent the Avoid Gef Id List
    * @type MQIntCollection
    *
    */
   var m_AvoidGefIdList = new MQIntCollection();
   m_AvoidGefIdList.setM_Xpath("AvoidGefIdList");
   /**
    * Returns the m_AvoidGefIdList collection.
    * @return The m_AvoidGefIdList collection.
    * @type MQIntCollection
    *
    */
   this.getAvoidGefIdList = function(){
      return m_AvoidGefIdList;
   };
   /**
    * Sets the m_AvoidGefIdList collection.
    * @param {MQIntCollection} AvoidGefIdList The collection to set m_AvoidGefIdList to.
    * @type void
    *
    */
   this.setAvoidGefIdList = function(avoidGefIdList){
      m_AvoidGefIdList.removeAll();
        m_AvoidGefIdList.append(avoidGefIdList);
   };
   /**
    * Value to represent the Avoid Absolute Gef Id List
    * @type MQIntCollection
    *
    */
   var m_AvoidAbsGefIdList = new MQIntCollection();
   m_AvoidAbsGefIdList.setM_Xpath("AvoidAbsoluteGefIdList");
   /**
    * Returns the m_AvoidAbsGefIdList collection.
    * @return The m_AvoidAbsGefIdList collection.
    * @type MQIntCollection
    *
    */
   this.getAvoidAbsGefIdList = function(){
      return m_AvoidAbsGefIdList;
   };
   /**
    * Sets the m_AvoidAbsGefIdList collection.
    * @param {MQIntCollection} AvoidAbsGefIdList The collection to set m_AvoidAbsGefIdList to.
    * @type void
    *
    */
   this.setAvoidAbsGefIdList = function(avoidAbsGefIdList){
      m_AvoidAbsGefIdList.removeAll();
      m_AvoidAbsGefIdList.append(avoidAbsGefIdList);
   };
   /**
    * Value to represent the MQAutoRouteCovSwitch
    * @type MQAutoRouteCovSwitch
    *
    */
   var m_AutoRouteCovSwitch = new MQAutoRouteCovSwitch("CovSwitcher");
   /**
    * Returns the m_AutoRouteCovSwitch object.
    * @return The m_AutoRouteCovSwitch object.
    * @type MQAutoRouteCovSwitch
    *
    */
   this.getAutoRouteCovSwitch = function(){
      return m_AutoRouteCovSwitch;
   };
   /**
    * Sets the m_AutoRouteCovSwitch collection.
    * @param {MQAutoRouteCovSwitch} autoRouteCovSwitch The object to set m_AutoRouteCovSwitch to.
    * @type void
    *
    */
   this.setAutoRouteCovSwitch = function(autoRouteCovSwitch){
      m_AutoRouteCovSwitch = autoRouteCovSwitch;
   };
   /**
    * Value to represent the MQRouteType
    * @type MQRouteType
    *
    */
   var m_RouteType = new MQRouteType(0);
   /**
    * Returns the m_RouteType object.
    * @return The m_RouteType object.
    * @type MQRouteType
    *
    */
   this.getRouteType = function(){
      return m_RouteType;
   };
   /**
    * Sets the m_RouteType collection.
    * @param {MQRouteType} routeType The object to set m_RouteType to.
    * @type void
    *
    */
   this.setRouteType = function(routeType){
      m_RouteType = routeType;
   };
   /**
    * Value to represent the MQNarrativeType
    * @type MQNarrativeType
    *
    */
   var m_NarrativeType = new MQNarrativeType(0);
   /**
    * Returns the m_NarrativeType object.
    * @return The m_NarrativeType object.
    * @type MQNarrativeType
    *
    */
   this.getNarrativeType = function(){
      return m_NarrativeType;
   };
   /**
    * Sets the m_NarrativeType collection.
    * @param {MQNarrativeType} narrativeType The object to set m_NarrativeType to.
    * @type void
    *
    */
   this.setNarrativeType = function(narrativeType){
      m_NarrativeType = narrativeType;
   };
   /**
    * Value to represent the MQDistanceUnits
    * @type MQDistanceUnits
    *
    */
   var m_DistanceUnits = new MQDistanceUnits(0);
   /**
    * Returns the m_DistanceUnits object.
    * @return The m_DistanceUnits object.
    * @type MQDistanceUnits
    *
    */
   this.getDistanceUnits = function(){
      return m_DistanceUnits;
   };
   /**
    * Sets the m_DistanceUnits collection.
    * @param {MQDistanceUnits} distanceUnits The object to set m_DistanceUnits to.
    * @type void
    *
    */
   this.setDistanceUnits = function(distanceUnits){
      m_DistanceUnits = distanceUnits;
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteOptions.prototype.getClassName = function(){
      return "MQRouteOptions";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteOptions.prototype.getObjectVersion = function(){
      return 3;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRouteOptions.prototype.loadXml = function (strXml){
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var avoidAttributeList = this.getAvoidAttrList();
      avoidAttributeList.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/AvoidAttributeList")));
      var avoidGefIdList = this.getAvoidGefIdList();
      avoidGefIdList.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/AvoidGefIdList")));
      var avoidAbsoluteGefIdList = this.getAvoidAbsGefIdList();
      avoidAbsoluteGefIdList.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/AvoidAbsoluteGefIdList")));
      var covSwitcher = this.getAutoRouteCovSwitch();
      covSwitcher.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CovSwitcher")));
      this.setRouteType(new MQRouteType(Math.floor(this.getProperty("RouteType"))));
      this.getNarrativeType(new MQNarrativeType(Math.floor(this.getProperty("NarrativeType"))));
      this.getDistanceUnits(new MQDistanceUnits(Math.floor(this.getProperty("NarrativeDistanceUnitType"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRouteOptions.prototype.saveXml = function (){
        // get inner object nodes
        var newNode = null;
        this.setProperty("RouteType", this.getRouteType().intValue());
        this.setProperty("NarrativeType", this.getNarrativeType().intValue());
        this.setProperty("NarrativeDistanceUnitType", this.getDistanceUnits().getValue());
        newNode = MQA.createXMLDoc(this.getAutoRouteCovSwitch().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CovSwitcher"));
        newNode = MQA.createXMLDoc(this.getAvoidAttrList().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "AvoidAttributeList"));
        newNode = MQA.createXMLDoc(this.getAvoidGefIdList().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "AvoidGefIdList"));
        newNode = MQA.createXMLDoc(this.getAvoidAbsGefIdList().saveXml());
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "AvoidAbsoluteGefIdList"));
        return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the maximum number of shape points for the server to
    * return from the route. The smaller the number the less
    * data gets returned from the server response on a route.
    * @param {int} nCount with the maximum number of shape points.
    * @type void
    */
   MQRouteOptions.prototype.setMaxShapePointsPerManeuver = function(nCount){
      this.setProperty("MaxShape", nCount);
   };
   /**
    * Returns the maximum number of shape points for the route options.
    * @return The maximum number of shape points
    * @type int
    */
   MQRouteOptions.prototype.getMaxShapePointsPerManeuver = function(){
      return this.getProperty("MaxShape");
   };
   /**
    * Sets the maximum number of GEFIDs for the server to
    * return from the route. The smaller the number the less
    * data gets returned from the server response on a route.
    * @param {int} nCount with the maximum number of GEFIDs.
    * @type void
    */
   MQRouteOptions.prototype.setMaxGEFIDsPerManeuver = function(nCount){
      this.setProperty("MaxGEFID", nCount);
   };
   /**
    * Returns the maximum number of GEFIDs for the route options.
    * @return The maximum number of GEFIDs
    * @type int
    */
   MQRouteOptions.prototype.getMaxGEFIDsPerManeuver = function(){
      return this.getProperty("MaxGEFID");
   };
   /**
    * Sets the language for the server to use on a route narrative.
    * Valid values for languages are defined by the members of this class
    * starting with the prefix szLanguage_.
    * @param {String} strLanguage containing a valid language
    * @type void
    */
   MQRouteOptions.prototype.setLanguage = function(strLanguage){
      this.setProperty("Language", strLanguage);
   };
   /**
    * Returns the language set for this RouteOptions object.
    * @return The language set
    * @type String
    */
   MQRouteOptions.prototype.getLanguage = function(){
      return this.getProperty("Language");
   };
   /**
    * Set the coverage name of the routing data to
    * use takes precedence over CovSwitcher.
    * @param {String} newCoverageName The coverage name
    * @type void
    */
   MQRouteOptions.prototype.setCoverageName = function(newCoverageName){
      this.setProperty("CoverageName", newCoverageName);
   };
   /**
    * Returns the coverage name of the routing data to use
    * takes precedence over CovSwitcher.
    * @return The coverage name
    * @type String
    */
   MQRouteOptions.prototype.getCoverageName = function(){
      return this.getProperty("CoverageName");
   };
   /**
    * Sets the state boundary display flag.
    * @param {Boolean} bFlag The state boundary display flag
    * @type void
    */
   MQRouteOptions.prototype.setStateBoundaryDisplay = function(bFlag){
      this.setProperty("StateBoundaryDisplay", (bFlag===true)?1:0);
   };
   /**
    * Gets the state boundary display flag
    * @return true if a state boundary text should be displayed, false otherwise.
    * @type boolean
    */
   MQRouteOptions.prototype.getStateBoundaryDisplay = function(){
      return (this.getProperty("StateBoundaryDisplay")==1)? true : false;
   };
   /**
    * Sets the country boundary display flag.
    * @param  {Boolean} bFlag The country boundary display flag
    * @type void
    */
   MQRouteOptions.prototype.setCountryBoundaryDisplay = function(bFlag){
      this.setProperty("CountryBoundaryDisplay", (bFlag===true)?1:0);
   };
   /**
    * Gets the country boundary display flag
    * @return true if a country boundary text should be displayed, false otherwise.
    * @type boolean
    */
   MQRouteOptions.prototype.getCountryBoundaryDisplay = function(){
      return (this.getProperty("CountryBoundaryDisplay")==1)? true : false;
   };
// End class MQRouteOptions

// Begin Class MQRouteResults
/* Inheirit from MQObject */
MQRouteResults.prototype = new MQObject();
MQRouteResults.prototype.constructor = MQRouteResults;
/**
 * Constructs a new MQRouteResults object.
 * @class Contains the results of a routing request.
 * @extends MQObject
 * @see MQLatLngCollection
 * @see MQLocationCollection
 * @see MQRouteResultsCode
 * @see MQStringCollection
 * @see MQTrekRouteCollection
 */
function MQRouteResults () {
   MQObject.call(this);
   this.setM_Xpath("RouteResults");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getROUTERESULTS()));
   /**
    * Value to represent the collection of locations.
    * @type MQLocationCollection
    *
    */
   var m_Locations = new MQLocationCollection();
   m_Locations.setM_Xpath("Locations");
   /**
    * Returns all Locations used in the route.  LatLng may be altered to closest
    * point on found road segment.  Locations may be re-ordered in the case
    * of an optimized route
    * @return All Locations used in the route.
    * @type MQLocationCollection
    */
   this.getLocations = function() {
      return m_Locations;
   };
   /**
    * Value to represent the collection of MQTrekRoutes.
    * @type MQTrekRouteCollection
    *
    */
   var m_TrekRoutes = new MQTrekRouteCollection("TrekRoute");
   m_TrekRoutes.setM_Xpath("TrekRoutes");
   /**
    * Returns the TrekRouteCollection member of this object.
    * @return All TrekRoutes used in the route.
    * @type MQTrekRouteCollection
    */
   this.getTrekRoutes = function() {
      return m_TrekRoutes;
   };
      /**
    * Value to represent the collection of locations.
    * @type MQRouteResultsCode
    *
    */
   var m_ResultCode = new MQRouteResultsCode(MQCONSTANT.MQROUTERESULTSCODE_NOT_SPECIFIED);
   /**
    * Gets the result code member of this object.
    * @return The result code member of this object.
    * @type MQRouteResultsCode
    */
   this.getResultCode = function() {
      return m_ResultCode;
   };
   /**
    * Sets the result code member of this object.
    * @param {MQRouteResultsCode} resultCode The MQRouteResultsCode for this MQRouteResults object.
    * @type void
    */
   this.setResultCode = function(resultCode) {
      m_ResultCode = resultCode;
   };
   /**
    * Value to represent the collection of result messages.
    * @type MQStringCollection
    *
    */
   var m_ResultMessages = new MQStringCollection("Item");
   m_ResultMessages.setM_Xpath("ResultMessages");
   /**
    * Returns the messages returned by Server.
    * Each result message should contain a unique number followed by
    * a text description.  Ex: "200 Unable to calculate route."
    * Messages for invalid locations should be numbered 100..199
    * Messages for route failures should be numbered 200..99
    * @return An MQStringCollection of messages.
    * @type MQStringCollection
    */
   this.getResultMessages = function() {
      return m_ResultMessages;
   };
   /**
    * Value to represent the collection of streets.
    * @type MQManeuverCollection
    *
    */
   var shapePoints = null;
   /**
    * Returns the shapePoints collection made by appending all the maneuver shape point collections.
    * @return The shapePoints collection.
    * @type MQLatLngCollection
    */
   this.getShapePoints = function() {
      // calc and set if null
      if (shapePoints === null){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var tr = 0; tr < this.getTrekRoutes().getSize(); tr++) {
            var trek = this.getTrekRoutes().get(tr);
            for(var man = 0; man < trek.getManeuvers().getSize(); man++) {
               shapePoints.append(trek.getManeuvers().get(man).getShapePoints());
               distance += parseFloat(trek.getManeuvers().get(man).getDistance());
               time += parseInt(trek.getManeuvers().get(man).getTime());
            }
         }
      }
      return shapePoints;
   };
   /**
    * Value of the total distance for this maneuver.
    * @type Float
    */
   var distance = -1.0;
   /**
    * Returns the total distance for this maneuver.
    * @return The total distance for this maneuver.
    * @type Float
    */
   this.getDistance = function() {
      // calc and set if null
      if(distance === -1.0){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var tr = 0; tr < this.getTrekRoutes().getSize(); tr++) {
            var trek = this.getTrekRoutes().get(tr);
            for(var man = 0; man < trek.getManeuvers().getSize(); man++) {
               shapePoints.append(trek.getManeuvers().get(man).getShapePoints());
               distance += parseFloat(trek.getManeuvers().get(man).getDistance());
               time += parseInt(trek.getManeuvers().get(man).getTime());
            }
         }
      }
      return distance;
   };
   /**
    * Value of the total time for this maneuver.
    * @type int
    */
   var time = -1;
   /**
    * Returns the total time for this maneuver.
    * @return The total time for this maneuver.
    * @type int
    */
   this.getTime = function(){
      // calc and set if null
      if(time === -1){
         distance = 0.0;
         time = 0;
         shapePoints = new MQLatLngCollection();
         shapePoints.setM_Xpath("ShapePoints");
         for(var tr = 0; tr < this.getTrekRoutes().getSize(); tr++) {
            var trek = this.getTrekRoutes().get(tr);
            for(var man = 0; man < trek.getManeuvers().getSize(); man++) {
               shapePoints.append(trek.getManeuvers().get(man).getShapePoints());
               distance += parseFloat(trek.getManeuvers().get(man).getDistance());
               time += parseInt(trek.getManeuvers().get(man).getTime());
            }
         }
      }
      return time;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteResults.prototype.getClassName = function(){
      return "MQRouteResults";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteResults.prototype.getObjectVersion = function(){
      return 1;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRouteResults.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var locs = this.getLocations();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Locations")!==null)
         locs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Locations")));
      var trek = this.getTrekRoutes();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/TrekRoutes")!==null)
         trek.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/TrekRoutes")));
      var mesgs = this.getResultMessages();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ResultMessages")!==null)
         mesgs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ResultMessages")));
      this.setResultCode(new MQRouteResultsCode(Math.floor(this.getProperty("ResultCode"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRouteResults.prototype.saveXml = function () {
      // get inner object nodes
      this.setProperty("ResultCode", this.getResultCode().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the Coverage name used to perform this route.
    * @param {String} strName Name of coverage.
    * @type void
    */
   MQRouteResults.prototype.setCoverageName = function(strName){
      this.setProperty("CoverageName",strName);
   };
   /**
    * Gets the Coverage name used to perform this route.
    * @return The value of CoverageName
    * @type String
    */
   MQRouteResults.prototype.getCoverageName = function(){
      return this.getProperty("CoverageName");
   };
// End class MQRouteResults

// Begin Class MQRouteMatrixResults
/* Inheirit from MQObject */
MQRouteMatrixResults.prototype = new MQObject();
MQRouteMatrixResults.prototype.constructor = MQRouteMatrixResults;
/**
 * Constructs a new MQRouteMatrixResults object.
 * @class Contains the results of a route matrix request.
 * @extends MQObject
 * @see MQRouteMatrixResultsCode
 * @see MQStringCollection
 * @see MQIntCollection
 */
function MQRouteMatrixResults () {
   MQObject.call(this);
   this.setM_Xpath("RouteMatrixResults");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getROUTEMATRIXRESULTS()));
   /**
    * Value to represent the count of locations
    * @type int
    */
   var m_LocationCount = -1;
   /**
    * Value to represent the distance matrix
    * @type MQIntCollection
    */
   var m_Distance = null;
   /**
    * Sets the distance MQIntCollection. Should be used only for loadXml method.
    * @param {MQIntCollection} col The distance MQIntCollection.
    * @type void
    */
    this.setDistance = function(col){
        if (col){
           if (col.getClassName()==="MQIntCollection"){
               if (m_Distance!==null){
                  m_Distance.removeAll();
                  m_Distance.append(col);
               }else{
                  m_Distance = col;
               }
           }else{
            alert("failure in setDistance -- col is not MQIntCollection type");
            throw "failure in setDistance -- col is not MQIntCollection type";
           }
       }else{
            alert("failure in setDistance -- col is null");
            throw "failure in setDistance -- col is null";
       }
    };
   /**
    * Returns the calculated distance of the route.
    * @return The calculated total distance of the route.
    * @param {int} from The route to start with
    * @param {int} to The route to end with
    * @type Double
    */
   this.getDistance = function(from, to){
       if(m_LocationCount===-1){
          m_LocationCount = this.getProperty("LocationCount");
       }
       var pos = ((from * m_LocationCount) + to);
       return (m_Distance.get(pos)/1000.0).toFixed(6);
   };
   /**
    * Value to represent the time matrix
    * @type MQIntCollection
    */
   var m_Time = null;
   /**
    * Sets the time MQIntCollection. Should be used only for loadXml method.
    * @param {MQIntCollection} col The time MQIntCollection.
    * @type void
    *
    */
    this.setTime = function(col){
        if (col){
           if (col.getClassName()==="MQIntCollection"){
              if (m_Time!==null){
                 m_Time.removeAll();
                 m_Time.append(col);
              }else{
                 m_Time = col;
              }
           }else{
              alert("failure in setTime -- col is not MQIntCollection type");
              throw "failure in setTime -- col is not MQIntCollection type";
           }
       }else{
          alert("failure in setTime -- col is null");
          throw "failure in setTime -- col is null";
       }
    };
   /**
    * Returns the calculated elapsed time in seconds for the route.
    * @return The calculated elapsed time in seconds for the route.
    * @param {int} from The route to start with
    * @param {int} to The route to end with
    * @type int
    */
   this.getTime = function(from, to){
       if(m_LocationCount===-1){
          m_LocationCount = this.getProperty("LocationCount");
       }
       var pos = ((from * m_LocationCount) + to);
       return m_Time.get(pos);
   };
   /**
    * Value to represent the result code
    * @type MQRouteMatrixResultsCode
    */
   var m_ResultCode = new MQRouteMatrixResultsCode(MQCONSTANT.MQROUTEMATRIXRESULTSCODE_NOT_SPECIFIED);
   /**
    * Gets the result code member of this object.
    * @returns The MQRouteMatrixResultsCode for this MQRouteMatrixResults object.
    * @type MQRouteMatrixResultsCode
    */
   this.getResultCode = function(){
       return m_ResultCode;
   };
   /**
    * Sets the result code member of this object.
    * @param {MQRouteMatrixResultsCode} resultCode The MQRouteMatrixResultsCode for this MQRouteMatrixResults object.
    * @throws Some Exception for invalid type or null/missing parameters
    * @type void
    */
   this.setResultCode = function(rc){
       if (rc){
           if (rc.getClassName()==="MQRouteMatrixResultsCode"){
               m_ResultCode = rc;
           }else{
            alert("failure in setResultsCode -- rc is not MQRouteMatrixResultsCode type");
            throw "failure in setResultsCode -- rc is not MQRouteMatrixResultsCode type";
           }
       }else{
            alert("failure in setResultsCode -- rc is null");
            throw "failure in setResultsCode -- rc is null";
       }
   };
   /**
    * Value to represent the result messages
    * @type MQStringCollection
    */
   var m_ResultMessages = null;
   /**
    * Sets the ResultMessages MQStringCollection. Should be used only for loadXml method.
    * @param {MQStringCollection} col The ResultMessages MQStringCollection.
    * @type void
    *
    */
    this.setResultMessages = function(col){
        if (col){
           if (col.getClassName()==="MQStringCollection"){
              if (m_ResultMessages!==null){
                 m_ResultMessages.removeAll();
                 m_ResultMessages.append(col);
              }else{
                 m_ResultMessages = col;
              }
           }else{
            alert("failure in setResultMessages -- col is not MQStringCollection type");
            throw "failure in setResultMessages -- col is not MQStringCollection type";
           }
       }else{
         alert("failure in setResultMessages -- col is null");
         throw "failure in setResultMessages -- col is null";
       }
    };
   /**
    * Gets the result messages of this object.
    * @returns The MQStringCollection for this object.
    * @type MQStringCollection
    */
   this.getResultsMessages = function(){
       return m_ResultMessages;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteMatrixResults.prototype.getClassName = function(){
      return "MQRouteMatrixResults";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteMatrixResults.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRouteMatrixResults.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var dis = new MQIntCollection();
      dis.setM_Xpath("DistanceMatrix");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DistanceMatrix")!==null)
         dis.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DistanceMatrix")));
      this.setDistance(dis);
      var tim = new MQIntCollection();
      tim.setM_Xpath("TimeMatrix");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/TimeMatrix")!==null)
         tim.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/TimeMatrix")));
      this.setTime(tim);
      var mes = new MQStringCollection();
      mes.setM_Xpath("ResultMessages");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ResultMessages")!==null)
         mes.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/ResultMessages")));
      this.setResultMessages(mes);
      this.setResultCode(new MQRouteMatrixResultsCode(Math.floor(this.getProperty("ResultCode"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRouteMatrixResults.prototype.saveXml = function () {
      // get inner object nodes
      this.setProperty("ResultCode", this.getResultCode().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the Coverage name used to perform this route.
    * @param {String} newCoverageName the Coverage name used to perform this route.
    * @type void
    */
   MQRouteMatrixResults.prototype.setCoverageName = function(newCoverageName){
      this.setProperty("CoverageName",newCoverageName);
   };
   /**
    * Gets the Coverage name used to perform this route.
    * @return The value of CoverageName
    * @type String
    */
   MQRouteMatrixResults.prototype.getCoverageName = function(){
      return this.getProperty("CoverageName");
   };
   /**
    * Gets the all-to-all flag.
    * @return Returns the allToAll flag. True if the matrix is from each
    * location to each other, False if from location 0 to all others.
    * @type Boolean
    */
   MQRouteMatrixResults.prototype.getAllToAllFlag = function(){
      return (this.getProperty("AllToAll")==1)? true : false;
   }
// End Class MQRouteMatrixResults

// Begin Class MQRecordSet
/* Inheirit from MQObject */
MQRecordSet.prototype = new MQObject();
MQRecordSet.prototype.constructor = MQRecordSet;
/**
 * Constructs a new MQRecordSet object.
 * @class Class for retrieving record attribute information.
 * @extends MQObject
 * @see MQStringCollection
 */
function MQRecordSet() {
   MQObject.call(this);
   this.setM_Xpath("RecordSet");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getRECORDSET()));
   /**
    * Value to represent end of file indicator
    * @type Boolean
    */
   var m_EOF = true;
   /**
    * Value to represent beginning of file indicator
    * @type Boolean
    */
   var m_BOF = true;
   /**
    * Value to represent total record count
    * @type int
    */
   var m_recCnt = 0;
   /**
    * Value to represent current record
    * @type int
    */
   var m_curRec = -1;
   /**
    * Value to represent the field names of the columns
    * @type MQStringCollection
    */
   var m_Fields = new MQStringCollection();
   m_Fields.setM_Xpath("Fields");
   /**
    * Value to represent the record set retrieved for the user
    * @type MQStrColCollection
    */
   var m_Records = new MQStrColCollection("Record");
   m_Records.setM_Xpath("Records");
   m_Records.setValidClassName("MQStringCollection");
   /**
    * Sets the cursor to the first record in the record set.
    * @type void
    */
   this.moveFirst = function(){
      if(m_Records.getSize() !== 0){
         m_EOF = false;
         m_BOF = false;
         m_curRec = 0;
      }else{
         alert(MQERROR.RECORDSET_MOVEFIRST_1);
         throw MQERROR.RECORDSET_MOVEFIRST_1;
      }
   };
   /**
    * Sets the cursor to the last record in the record set.
    * @type void
    */
   this.moveLast = function(){
      if(m_Records.getSize() !== 0){
         m_EOF = false;
         m_BOF = false;
         m_curRec = m_Records.getSize()-1;
      }else{
         alert(MQERROR.RECORDSET_MOVELAST_1);
         throw MQERROR.RECORDSET_MOVELAST_1;
      }
   };
   /**
    * Sets the cursor to the next record in the record set.
    * If the cursor is currently set to the last record,
    * A call to MoveNext sets EOF to true
    * @type void
    */
   this.moveNext = function(){
      var numRecs = m_Records.getSize();
      if( numRecs!== 0){
         if(m_curRec < numRecs-1){
            m_curRec++;
            m_BOF = false;
            m_EOF = false;
         }else if(m_curRec === numRecs-1){
            m_curRec++;
            m_BOF = false;
            m_EOF = true;
         }else if(m_EOF){
            alert(MQERROR.RECORDSET_MOVENEXT_1);
            throw MQERROR.RECORDSET_MOVENEXT_1;
         }else{
            alert(MQERROR.RECORDSET_MOVENEXT_2);
            throw MQERROR.RECORDSET_MOVENEXT_2;
         }
      }else{
         alert(MQERROR.RECORDSET_MOVENEXT_3);
         throw MQERROR.RECORDSET_MOVENEXT_3;
      }
   };
   /**
    * Set to true when the cursor is before the beginning of the record set
    * @return beginning record set flag
    * @type Boolean
    */
   this.isBOF = function(){
      return m_BOF;
   };
   /**
    * Set to true when the cursor is passed the end of the record set
    * @return end record set flag
    * @type Boolean
    */
   this.isEOF = function(){
      return m_EOF;
   };
   /**
    * Get the list of available field names
    * @return the list of field names
    * @type MQStringCollection
    */
   this.getFieldNames = function(){
      return m_Fields;
   };
   /**
    * Get the field value by field name
    * @param {String} strFieldName the field name
    * @type String
    */
   this.getField = function(strFieldName){
      // validate that 0<= m_curRec<m_recCnt
      if (!(0 <= m_curRec && m_curRec < m_recCnt)){
         alert(MQERROR.RECORDSET_GETFIELD_1);
         throw MQERROR.RECORDSET_GETFIELD_1;
      }
      // find the position of the field in the fieldNames array
      var pos = -1;
      for(var i = 0; i < m_Fields.getSize(); i++){
         if(m_Fields.get(i)===strFieldName){
            pos = i;
            break;
         }
      }
      // check pos not equal to -1
      if (pos===-1){
         alert(MQERROR.RECORDSET_GETFIELD_2);
         throw MQERROR.RECORDSET_GETFIELD_2;
      }
      // find the var in the current record
      return m_Records.get(m_curRec).get(pos);
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   this.loadXml = function (strXml) {
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      m_Fields.loadXml(mqXmlToStr(mqGetNode(xmlDoc,"/" + this.getM_Xpath() + "/Fields")));
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var col = null;
         m_Records.removeAll();
         // iterate through xml and create stringcollections to add to the strcolCollection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="Record"){
               col = new MQStringCollection();
               col.setM_Xpath("Record");
               col.loadXml(mqXmlToStr(nodes[count]));
               m_Records.add(col);
            }
         }
      }
      m_recCnt = this.getProperty("RecordCount");
      if(m_recCnt > 0){
         m_curRec = 0;
         m_BOF = false;
         m_EOF = false;
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRecordSet.prototype.getClassName = function(){
      return "MQRecordSet";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRecordSet.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRecordSet.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End Class MQRecordSet

// Begin Class MQMapState
/* Inheirit from MQObject */
MQMapState.prototype = new MQObject();
MQMapState.prototype.constructor = MQMapState;
/**
 * Constructs a new MQMapState object.
 * @class Defines the characteristics of a map. It includes such information
 * as the map name, the coverage name, the latitude/longitude of the
 * map Center, the map scale, and the image size.
 * @extends MQObject
 * @see MQLatLng
 * @see MQPoint
 */
function MQMapState(){
   MQObject.call(this);
   this.setM_Xpath("MapState");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getMAPSTATE()));
   /**
    * Value to represent the Center point of the map
    * @type MQLatLng
    */
   var m_MQLatLng = new MQLatLng("Center");
   /**
    * Returns the Center point of the map associated with this MapState object.
    * @return The Center point of this MapState object.
    * @type MQLatLng
    */
   this.getCenter = function() {
      return m_MQLatLng;
   };
   /**
    * Sets the Center point of the map associated with this MapState object.
    * @param {MQLatLng} latLng The Center point for the map associated with this
    * MapState object.
    * @type void
    */
   this.setCenter = function(latLng) {
      m_MQLatLng.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQMapState.prototype.getClassName = function(){
      return "MQMapState";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQMapState.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Initializes object.
    * @type void
    */
   MQMapState.prototype.initObject = function(){
      this.setProperty("Scale", 0);
      this.setCenter(new MQLatLng(0,0));
      this.setProperty("Width", -1.0);
      this.setProperty("Height", -1.0);
      this.setProperty("MapName", "");
      this.setProperty("CoverageName", "");
   }
   /**
    * Compares this object to the specified object. The result is true if and
    * only if the argument is not null and is a MapState object that has
    * same attributes as this object.
    * @param   {MQMapState} comparator the object to compare this MapState against.
    * @return  true if the MapState objects are equal; false otherwise.
    * @type Boolean
    */
   MQMapState.prototype.equals = function(comparator){
      if(comparator){
         try{
         if(comparator.getClassName()==="MQMapState"){
            // Test whether the attributes have identical values
            return m_nScale == other.m_nScale
            && m_strMapName === other.m_strMapName
            && m_strCoverageName === other.m_strCoverageName
            && m_llCenter.equals(other.m_llCenter)
            && m_dMapWidth === other.m_dMapWidth
            && m_dMapHeight === other.m_dMapHeight;
         }}catch(error){}
      }
      return false;
   };
   /**
    * Sets the map name of the map associated with this MapState object.
    * @param {String} strName The map name for the map associated with this
    * MapState object.
    * @type void
    */
   MQMapState.prototype.setMapName = function(strName){
      this.setProperty("MapName", strName);
   }
   /**
    * Returns the map name of the map associated with this MapState object.
    * @return The map name of the map associated with this MapState object.
    * @type void
    */
   MQMapState.prototype.getMapName = function(){
      this.getProperty("MapName");
   }
   /**
    * Sets the coverage name for the map associated with this MapState object.
    * @param {String} strCovId The coverage name of the map associated with this
    * MapState object.
    * @type void
    */
   MQMapState.prototype.setCoverageName = function(strCovId){
      this.setProperty("CoverageName", strCovId);
   }
   /**
    * Returns the coverage name of the map associated with this MapState object.
    * @return The coverage name of this MapState object.
    * @type void
    */
   MQMapState.prototype.getCoverageName = function(){
      this.getProperty("CoverageName");
   }
   /**
    * Sets the width of the generated map in inches.
    * @param {Double} dWidth double value specifying the width
    * of the map in inches.
    * @type void
    */
   MQMapState.prototype.setWidthInches = function(dWidth){
      this.setProperty("Width", dWidth);
   }
   /**
    * Returns the width of the generated map in inches.
    * @return the width of the generated map in inches.
    * @type = int
    */
   MQMapState.prototype.getWidthInches = function(){
      return this.getProperty("Width");
   }
   /**
    * Sets the height of the generated map in inches.
    * @param {Double} dHeight double value specifying the height
    * of the map in inches.
    * @type void
    */
   MQMapState.prototype.setHeightInches = function(dHeight){
      this.setProperty("Height", dHeight);
   }
   /**
    * Returns the height of the generated map in inches.
    * @return the height of the generated map in inches.
    * @type = int
    */
   MQMapState.prototype.getHeightInches = function(){
      return this.getProperty("Height");
   }
   /**
    * Sets the width of the generated map in pixels.
    * @param {int} iWidth int value specifying the width of the
    * map in pixels.
    * @param {int} dpi int value specifying the DPI of the image, used
    * to convert from inches to pixels.
    * @type void
    */
   MQMapState.prototype.setWidthPixels = function(iWidth, dpi){
      if(dpi){
         this.setProperty("Width", parseFloat(iWidth)/parseFloat(dpi));
      } else {
         this.setProperty("Width", parseFloat(iWidth)/parseFloat(72));
      }
   }
   /**
    * Returns the width of the generated map in pixels.
    * @param {int} dpi int value specifying the DPI used to convert
    * image size from inches to pixels.
    * @return the width of the generated map in pixels.
    * @type void
    */
   MQMapState.prototype.getWidthPixels = function(dpi){
      if (dpi){
         return Math.ceil(this.getProperty("Width")*dpi);
      } else {
         return Math.ceil(this.getProperty("Width")*72);
      }
    }
   /**
    * Sets the height of the generated map in pixels.
    * @param {int} iHeight int value specifying the height
    * of the map in pixels.
    * @param {int} dpi int value specifying the DPI of the image, used to
    * convert from inches to pixels.
    * @type void
    */
   MQMapState.prototype.setHeightPixels = function(iHeight, dpi){
      if(dpi){
         this.setProperty("Height", parseFloat(iHeight)/parseFloat(dpi));
      } else {
         this.setProperty("Height", parseFloat(iHeight)/parseFloat(72));
      }
   }
   /**
    * Returns the height of the generated map in pixels.
    * @param {int} dpi int value specifying the DPI used to convert
    * image size from inches to pixels.
    * @type void
    */
   MQMapState.prototype.getHeightPixels = function(dpi){
      if (dpi){
         return Math.ceil(this.getProperty("Height")*dpi);
      } else {
         return Math.ceil(this.getProperty("Height")*72);
      }
    }
   /**
    * Sets the scale of the map associated with this MapState object.
    * @param {int} nNewScale The new scale value for the map associated with this
    * MapState object.
    * @type void
    */
   MQMapState.prototype.setMapScale = function(nNewScale){
      this.setProperty("Scale",nNewScale);
   }
    /**
     * Returns the scale of the map associated with this MapState object.
     * @return      The scale of this MapState object.
     */
    MQMapState.prototype.getMapScale = function(){
       return this.getProperty("Scale");
    }
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQMapState.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var lat = this.getProperty("Center/Lat");
      var lng = this.getProperty("Center/Lng");
      this.getCenter().setLatLng(lat,lng);
   };
   /**
    * Build an xml string that represents this object. Because this is
    * a complex object we need to completely rebuild the xml string.
    * @return The xml string.
    * @type String
    *
    */
   MQMapState.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Center"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End Class MQMapState

// Begin class MQSearchCriteria
/* Inheirit from MQObject */
MQSearchCriteria.prototype = new MQObject();
MQSearchCriteria.prototype.constructor = MQSearchCriteria;
/**
 * Constructs a new MQSearchCriteria object.
 * @class Base class to define the criteria for a search. This class is not to be
 * directly instantiated. Use one of its child classes instead.
 * @extends MQObject
 */
function MQSearchCriteria () {
   MQObject.call(this);
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSearchCriteria.prototype.getClassName = function(){
      return "MQSearchCriteria";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSearchCriteria.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Sets the data member of this object which indicates the maximum number
    * of matches that are to be returned by the search.
    * @param {int} nMax The maximum number of matches that are to be returned by
    * the search.
    * @type void
    */
   MQSearchCriteria.prototype.setMaxMatches = function(nMax){
      this.setProperty("MaxMatches", nMax);
   };
   /**
    * Gets the data member of this object which indicates the maximum number
    * of matches that are to be returned by the search.
    * @return The maximum number of matches that are to be returned by
    * the search
    * @type int
    */
   MQSearchCriteria.prototype.getMaxMatches = function(){
      return this.getProperty("MaxMatches");
   };
// End class MQSearchCriteria

// Begin class MQRadiusSearchCriteria
/* Inheirit from MQSearchCriteria */
MQRadiusSearchCriteria.prototype = new MQSearchCriteria();
MQRadiusSearchCriteria.prototype.constructor = MQRadiusSearchCriteria;
/**
 * Constructs a new MQRadiusSearchCriteria object.
 * @class Defines the criteria for a radius search. The radius to be searched
 * is defined by the latitude/longitude of the Center and the radius
 * of the circle.
 * @extends MQSearchCriteria
 * @see MQLatLng
 * @see MQDistanceUnits
 */
function MQRadiusSearchCriteria () {
   MQObject.call(this);
   this.setM_Xpath("RadiusSearchCriteria");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getRADIUSSEARCHCRITERIA()));
   /**
    * Value to represent the MQLatLng
    * @type MQLatLng
    *
    */
   var m_MQLatLng = new MQLatLng("CenterLatLng");
   /**
    * Returns the Center (latitude/longitude) of the circle which defines
    * the extent of the search area.
    * @return The m_MQLatLng object.
    * @type MQLatLng
    *
    */
   this.getCenter = function() {
      return m_MQLatLng;
   };
   /**
    * Sets the latitude and longitude values of the Center point of the
    * circle which defines the extent of the search area.
    * @param {MQLatLng} latLng the Document to set m_MQLatLng to.
    * @type void
    *
    */
   this.setCenter = function(latLng) {
      m_MQLatLng.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRadiusSearchCriteria.prototype.getClassName = function(){
      return "MQRadiusSearchCriteria";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRadiusSearchCriteria.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRadiusSearchCriteria.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var lat = this.getProperty("CenterLatLng/Lat");
      var lng = this.getProperty("CenterLatLng/Lng");
      this.getCenter().setLatLng(lat, lng);
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRadiusSearchCriteria.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the radius of the circle which defines the extent of the search
    * area.
    * @param {Double} radius The radius that this object is to be set to.
    * @param {MQDistanceUnits} lUnits DistanceUnits Miles or KiloMeters
    * @type void
    */
   MQRadiusSearchCriteria.prototype.setRadius = function(radius, lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         radius = radius / MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      this.setProperty("Radius", radius);
   };
   /**
    * Returns the radius of the circle which defines the extent of the search
    * area.
    * @param {MQDistanceUnits} lUnits DistanceUnits Miles or KiloMeters
    * @return The radius of the circle which defines the extent of
    * the search area.
    * @type Double
    */
   MQRadiusSearchCriteria.prototype.getRadius = function(lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      var radius = this.getProperty("Radius");
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         radius = radius * MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      return radius;
   };
// End class MQRadiusSearchCriteria

// Begin class MQRectSearchCriteria
/* Inheirit from MQSearchCriteria */
MQRectSearchCriteria.prototype = new MQSearchCriteria();
MQRectSearchCriteria.prototype.constructor = MQRectSearchCriteria;
/**
 * Constructs a new MQRectSearchCriteria object.
 * @class Defines the criteria for a rectangle search. The rectangle to be
 * searched is defined by the upper left and lower right corners of
 * the rectangle.
 * @param {MQRectLL} mqRectLL An object provided by the TileMap ToolKit that
 * is used to preset the upper left and lower right latlngs.
 * This parameter is optional.
 * @extends MQSearchCriteria
 * @see MQLatLng
 */
function MQRectSearchCriteria (mqRectLL) {
   MQObject.call(this);
   this.setM_Xpath("RectSearchCriteria");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getRECTSEARCHCRITERIA()));
   /**
    * Value to represent the latitude/longitude of the upper left corner
    * @type MQLatLng
    */
   var m_UpperLeft = new MQLatLng("UpperLeftLatLng");
   /**
    * Gets the latitude/longitude of the upper left corner
    * of this RectSearchCriteria object.
    * @return The m_UpperLeft object.
    * @type MQLatLng
    */
   this.getUpperLeft = function() {
      return m_UpperLeft;
   };
   /**
    * Sets the latitude/longitude of the upper left corner
    * of this RectSearchCriteria object.
    * @param {MQLatLng} latLng the MQLatLng to set m_UpperLeft to.
    * @type void
    */
   this.setUpperLeft = function(latLng) {
      m_UpperLeft.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Value to represent the latitude/longitude of the lower right corner
    * @type MQLatLng
    */
   var m_LowerRight = new MQLatLng("LowerRightLatLng");
   /**
    * Gets the latitude/longitude of the lower right corner
    * of this RectSearchCriteria object.
    * @return The m_LowerRight object.
    * @type MQLatLng
    */
   this.getLowerRight = function() {
      return m_LowerRight;
   };
   /**
    * Sets the latitude/longitude of the lower right corner
    * of this RectSearchCriteria object.
    * @param {MQLatLng} latLng the MQLatLng to set m_LowerRight to.
    * @type void
    */
   this.setLowerRight = function(latLng) {
      m_LowerRight.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   if (mqRectLL){
      m_UpperLeft.setLatLng(mqRectLL.getUpperLeft().getLat(),mqRectLL.getUpperLeft().getLng());
      m_LowerRight.setLatLng(mqRectLL.getLowerRight().getLat(),mqRectLL.getLowerRight().getLng());
   }
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRectSearchCriteria.prototype.getClassName = function(){
      return "MQRectSearchCriteria";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRectSearchCriteria.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRectSearchCriteria.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var lat = this.getProperty("UpperLeftLatLng/Lat");
      var lng = this.getProperty("UpperLeftLatLng/Lng");
      this.getUpperLeft().setLatLng(lat, lng);
      lat = this.getProperty("LowerRightLatLng/Lat");
      lng = this.getProperty("LowerRightLatLng/Lng");
      this.getLowerRight().setLatLng(lat, lng);
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRectSearchCriteria.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getUpperLeft().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftLatLng"));
      newNode = MQA.createXMLDoc(this.getLowerRight().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LowerRightLatLng"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQRectSearchCriteria

// Begin class MQPolySearchCriteria
/* Inheirit from MQSearchCriteria */
MQPolySearchCriteria.prototype = new MQSearchCriteria();
MQPolySearchCriteria.prototype.constructor = MQPolySearchCriteria;
/**
 * Constructs a new MQPolySearchCriteria object.
 * @class Defines the criteria for a poly search based on a collection of
 * MQLatLngs
 * @extends MQSearchCriteria
 * @see MQLatLngCollection
 */
function MQPolySearchCriteria () {
   MQObject.call(this);
   this.setM_Xpath("PolySearchCriteria");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPOLYSEARCHCRITERIA()));
   /**
    * Value to represent the MQLatLngCollection
    * @type MQLatLngCollection
    */
   var m_LatLngs = new MQLatLngCollection();
   m_LatLngs.setM_Xpath("LatLngs");
   /**
    * Returns the m_LatLngs object.
    * @return The m_LatLngs object.
    * @type MQLatLngCollection
    */
   this.getShapePoints = function() {
      return m_LatLngs;
   };
   /**
    * Sets the m_LatLngs object.
    * @param {MQLatLngCollection} latLngs the MQLatLngCollection to set m_LatLngs to.
    * @type void
    */
   this.setShapePoints = function(latLngs) {
      if (latLngs.getClassName()==="MQLatLngCollection"){
         m_LatLngs.removeAll();
         m_LatLngs.append(latLngs);
      }
      else {
         alert("failure in setShapePoints");
         throw "failure in setShapePoints";
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPolySearchCriteria.prototype.getClassName = function(){
      return "MQPolySearchCriteria";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPolySearchCriteria.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    */
   MQPolySearchCriteria.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlngs = this.getShapePoints();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         latlngs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    */
   MQPolySearchCriteria.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getShapePoints().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQPolySearchCriteria

// Begin class MQCorridorSearchCriteria
/* Inheirit from MQPolySearchCriteria */
MQCorridorSearchCriteria.prototype = new MQPolySearchCriteria();
MQCorridorSearchCriteria.prototype.constructor = MQCorridorSearchCriteria;
/**
 * Constructs a new MQCorridorSearchCriteria object.
 * @class Defines the criteria for a Corridor search. The corridor to be searched
 * is defined by the latitude/longitude collection
 * @extends MQPolySearchCriteria
 * @see MQDistanceUnits
 * @see MQSearchCriteria
 */
function MQCorridorSearchCriteria () {
   MQPolySearchCriteria.call(this);
   this.setM_Xpath("CorridorSearchCriteria");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getCORRIDORSEARCHCRITERIA()));
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQCorridorSearchCriteria.prototype.getClassName = function(){
      return "MQCorridorSearchCriteria";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQCorridorSearchCriteria.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Sets exactLinks-whether to use generalization in determining corridor
    * @param {Boolean} bExactLinks Enables/disables generalization in the server.
    * @type void
    */
   MQCorridorSearchCriteria.prototype.setCorrExactLinks = function(bExactLinks){
      this.setProperty("ExactLinks", (bExactLinks===true)?1:0);
   };
   /**
    * Returns the exactLinks-whether to use generalization in determining corridor
    * @return The exactLinks-whether to use generalization in determining corridor
    * @type Boolean
    */
   MQCorridorSearchCriteria.prototype.getCorrExactLinks = function(){
      return (this.getProperty("ExactLinks")==1)? true : false;
   };
   /**
    * Sets the Corridor width (miles) which defines the extent of the search
    * area. Returns the width it was set to.
    * @param  {Double} dCorrWidth The corridor width that this object is to be set to.
    * @param  {MQDistanceUnits} lUnits DistanceUnits Miles or KiloMeters.
    * @type void
    */
   MQCorridorSearchCriteria.prototype.setCorridorWidth = function(dCorrWidth, lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         dCorrWidth = dCorrWidth / MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      this.setProperty("CorridorWidth", dCorrWidth);
   };
   /**
    * Returns the width of the corridor which defines the extent of the search
    * area.
    * @param {MQDistanceUnits} lUnits The DistanceUnits Miles or KiloMeters
    * @return The width of the corridor which defines the extent of
    * the search area.
    * @type Double
    */
   MQCorridorSearchCriteria.prototype.getCorridorWidth = function(lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      var width = this.getProperty("CorridorWidth");
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         width = width * MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      return width;
   };
   /**
    * Sets the buffer width which defines the amount to generalize.
    * Returns the width it was set to.
    * @param {Double} dBufferWidth The corridor buffer that this object is to be
    * set to.
    * @param {MQDistanceUnits} lUnits The DistanceUnits Miles or KiloMeters.
    * @type void
    */
   MQCorridorSearchCriteria.prototype.setCorridorBufferWidth = function(dBufferWidth, lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         dBufferWidth = dBufferWidth / MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      this.setProperty("CorridorBufferWidth", dBufferWidth);
   };
   /**
    * Returns the width of the corridor buffer which defines the extent of the
    * generalization.
    * @param {MQDistanceUnits} lUnits DistanceUnits Miles or KiloMeters
    * @return The width of the corridor buffer which defines the extent of
    * the generalization.
    * @type Double
    */
   MQCorridorSearchCriteria.prototype.getCorridorBufferWidth = function(lUnits){
      if(lUnits){
         mqIsClass("MQDistanceUnits", lUnits, false);
      } else {
         lUnits = new MQDistanceUnits(MQCONSTANT.MQDISTANCEUNITS_MILES);
      }
      var width = this.getProperty("CorridorBufferWidth");
      if (lUnits.getValue() === MQCONSTANT.MQDISTANCEUNITS_KILOMETERS)
         width = width * MQCONSTANT.DISTANCEAPPROX_KILOMETERS_PER_MILE;
      return width;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    */
/*   MQCorridorSearchCriteria.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlngs = this.getLatLngs();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         latlngs.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
   };*/
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    */
/*   MQCorridorSearchCriteria.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      return mqXmlToStr(this.getM_XmlDoc());
   };*/
// End class MQCorridorSearchCriteria

// Begin Class MQDBLayerQuery
/* Inheirit from MQObject */
MQDBLayerQuery.prototype = new MQObject();
MQDBLayerQuery.prototype.constructor = MQDBLayerQuery;
/**
 * Constructs a new MQDBLayerQuery object.
 * @class Stores geocode coverage switching parameters.
 * @extends MQObject
 */
function MQDBLayerQuery () {
   MQObject.call(this);
   this.setM_Xpath("DBLayerQuery");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDBLAYERQUERY()));
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDBLayerQuery.prototype.getClassName = function(){
      return "MQDBLayerQuery";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDBLayerQuery.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQDBLayerQuery.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQDBLayerQuery.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Contains Secondary SQL Query.
    *   Remarks:
    * Optional Secondary SQL query (used in conjunction with spatial search criteria)
    * Allows user to add further SQL restrictions on the results that would have been returned.
    * This SQL fragment will be appended to the WHERE clause with an "AND" operator.
    * @param {String} strName The string which is to be stored as the database layer
    * name in this object.
    * @type void
    */
   MQDBLayerQuery.prototype.setDBLayerName = function(strName){
      this.setProperty("LayerName",strName);
   };
   /**
    * Returns the database layer name of this object.
    * @return The database layer name of this object.
    * @type String
    */
   MQDBLayerQuery.prototype.getDBLayerName = function(){
      return this.getProperty("LayerName");
   };
   /**
    * Sets the query string to the specified string.
    * @param {String} strQuery The query string which is to be stored in this object.
    * @type void
    */
   MQDBLayerQuery.prototype.setExtraCriteria = function(strQuery){
      this.setProperty("ExtraCriteria",strQuery);
   };
   /**
    * Contains Secondary SQL Query.
    *   Remarks:
    * Optional Secondary SQL query (used in conjunction with spatial search criteria)
    * Allows user to add further SQL restrictions on the results that would have been returned.
    * This SQL fragment will be appended to the WHERE clause with an "AND" operator.
    * @return   The query string of this object.
    * @type Long
    */
   MQDBLayerQuery.prototype.getExtraCriteria = function(){
      return this.getProperty("ExtraCriteria");
   };
// End class MQDBLayerQuery

// Begin Class MQPrimitive
/* Inheirit from MQObject */
MQPrimitive.prototype = new MQObject();
MQPrimitive.prototype.constructor = MQPrimitive;
/**
 * Constructs a new MQPrimitive object.
 * @class The base class for drawing objects that can be added to the map.
 * @extends MQObject
 * @see MQCoordinateType
 * @see MQDrawTrigger
 */
function MQPrimitive () {
   MQObject.call(this);
   /**
    * Value to represent the draw trigger to be used
    * @type MQDrawTrigger
    */
   var m_DrawTrigger = new MQDrawTrigger(MQCONSTANT.MQDRAWTRIGGER_AFTER_TEXT);
   /**
    * Gets the DrawTrigger
    * @return The m_DrawTrigger object.
    * @type MQDrawTrigger
    */
   this.getDrawTrigger = function() {
      return m_DrawTrigger;
   };
   /**
    * Sets the DrawTrigger
    * @param {MQDrawTrigger} dt the MQDrawTrigger to set m_DrawTrigger to.
    * @type void
    */
   this.setDrawTrigger = function(dt) {
      if(dt){
         if(dt.getClassName() === "MQDrawTrigger"){
            m_DrawTrigger = dt;
         }
      }
   };
   /**
    * Value to represent the coordinate type to be used
    * @type MQCoordinateType
    */
   var m_CoordinateType = new MQCoordinateType(MQCONSTANT.MQCOORDINATETYPE_GEOGRAPHIC);
   /**
    * Gets the CoordinateType
    * @return The m_CoordinateType object.
    * @type MQCoordinateType
    */
   this.getCoordinateType = function() {
      return m_CoordinateType;
   };
   /**
    * Sets the CoordinateType
    * @param {MQCoordinateType} dt the MQCoordinateType to set m_CoordinateType to.
    * @type void
    */
   this.setCoordinateType = function(dt) {
      if(dt){
         if(dt.getClassName() === "MQCoordinateType"){
            m_CoordinateType = dt;
         }
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPrimitive.prototype.getClassName = function(){
      return "MQPrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPrimitive.prototype.getObjectVersion = function(){
      return 2;
   };
   /**
    * Sets the key.
    * @param {String} strKey The new key to be assigned to this object.
    * @type void
    */
   MQPrimitive.prototype.setKey = function(strKey){
      this.setProperty("Key",strKey);
   };
   /**
    * Gets the key.
    * @return The key of this object.
    * @type String
    */
   MQPrimitive.prototype.getKey = function(){
      return this.getProperty("Key");
   }
   /**
    * Sets the Opacity.
    * @param {String} strOpacity The new Opacity to be assigned to this object.
    * @type void
    */
   MQPrimitive.prototype.setOpacity = function(strOpacity){
      this.setProperty("Opacity",strOpacity);
   };
   /**
    * Gets the Opacity.
    * @return The Opacity of this object.
    * @type String
    */
   MQPrimitive.prototype.getOpacity = function(){
      return this.getProperty("Opacity");
   }
// Begin Class MQPrimitive

// Begin Class MQLinePrimitive
/* Inheirit from MQPrimitive */
MQLinePrimitive.prototype = new MQPrimitive();
MQLinePrimitive.prototype.constructor = MQLinePrimitive;
/**
 * Constructs a new MQLinePrimitive object.
 * @class Describes linear annotations to be placed on the map. The line is
 * converted into latitude/longitude points so it can be placed in the
 * same geographical location after the map is manipulated.
 * @extends MQPrimitive
 * @see MQColorStyle
 * @see MQPenStyle
 * @see MQPointCollection
 * @see MQLatLngCollection
 */
function MQLinePrimitive () {
   MQPrimitive.call(this);
   this.setM_Xpath("LinePrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getLINEPRIMITIVE()));
   /**
    * Value to represent the color style to be used
    * @type MQColorStyle
    */
   var m_Color = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_BLACK);
   /**
    * Gets the m_Color
    * @return The m_Color object.
    * @type MQColorStyle
    */
   this.getColor = function() {
      return m_Color;
   };
   /**
    * Sets the m_Color
    * @param {MQColorStyle} obj the MQColorStyle to set m_Color to.
    * @type void
    */
   this.setColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_Color = obj;
         }
      }
   };
   /**
    * Value to represent the pen style to be used
    * @type MQPenStyle
    */
   var m_Style = new MQPenStyle(MQCONSTANT.MQPENSTYLE_SOLID);
   /**
    * Gets the m_Style
    * @return The m_Style object.
    * @type MQPenStyle
    */
   this.getStyle = function() {
      return m_Style;
   };
   /**
    * Sets the m_Style
    * @param {MQPenStyle} obj the MQPenStyle to set m_Style to.
    * @type void
    */
   this.setStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQPenStyle"){
            m_Style = obj;
         }
      }
   };
   /**
    * The MQPointCollection of coordinates of the Line.
    * @type MQPointCollection
    */
   var m_XYArray = new MQPointCollection();
   m_XYArray.setM_Xpath("Points");
   /**
    * Gets the m_XYArray
    * @return The m_XYArray object.
    * @type MQPointCollection
    */
   this.getPoints = function() {
      return m_XYArray;
   };
   /**
    * The MQLatLngCollection of coordinates of the Line.
    * @type MQLatLngCollection
    */
   var m_LLArray = new MQLatLngCollection();
   m_LLArray.setM_Xpath("LatLngs");
   /**
    * Gets the m_LLArray
    * @return The m_LLArray object.
    * @type MQLatLngCollection
    */
   this.getLatLngs = function() {
      return m_LLArray;
   };
   /**
    * Sets the m_LLArray
    * @param {MQLatLngCollection} obj the MQLatLngCollection to set m_LLArray to.
    * @type void
    */
   this.setLatLngs = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQLatLngCollection"){
            m_LLArray.removeAll();
            m_LLArray.append(obj);
         }
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQLinePrimitive.prototype.getClassName = function(){
      return "MQLinePrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQLinePrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQLinePrimitive.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var ll = new MQLatLngCollection();
      ll.setM_Xpath("LatLngs");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         ll.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
      this.setLatLngs(ll);
      var pt = new MQPointCollection();
      pt.setM_Xpath("Points");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")!==null)
         pt.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")));
      this.setPoints(ll);
      this.setDrawTrigger(new MQDrawTrigger(Math.floor(this.getProperty("DrawTrigger"))));
      this.setCoordinateType(new MQCoordinateType(Math.floor(this.getProperty("CoordinateType"))));
      this.setColor(new MQColorStyle(Math.floor(this.getProperty("Color"))));
      this.setStyle(new MQPenStyle(Math.floor(this.getProperty("Style"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQLinePrimitive.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      newNode = MQA.createXMLDoc(this.getPoints().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Points"));
      this.setProperty("DrawTrigger", this.getDrawTrigger().intValue());
      this.setProperty("CoordinateType", this.getCoordinateType().intValue());
      this.setProperty("Color", this.getColor().intValue());
      this.setProperty("Style", this.getStyle().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the value representing the width of this object.
    * @param {int} width The new Width to be assigned to this object.
    * @type void
    */
   MQLinePrimitive.prototype.setWidth = function(width){
      this.setProperty("Width",width);
   };
   /**
    * Returns the value representing the width of this object.
    * @return The Width of this object.
    * @type int
    */
   MQLinePrimitive.prototype.getWidth = function(){
      return this.getProperty("Width");
   }
// Begin Class MQLinePrimitive

// Begin Class MQPolygonPrimitive
/* Inheirit from MQLinePrimitive */
MQPolygonPrimitive.prototype = new MQLinePrimitive();
MQPolygonPrimitive.prototype.constructor = MQPolygonPrimitive;
/**
 * Constructs a new MQPolygonPrimitive object.
 * @class Describes polygons to be placed on a map. The polygon is initially
 * defined with a collection of points. These points are the X,Y
 * coordinates of the vertices of the polygon. The coordinates are
 * converted into a latitude/longitude points so the annotation can be
 * placed in the same geographical location after the map is
 * manipulated.
 * @extends MQLinePrimitive
 * @see MQColorStyle
 * @see MQFillStyle
 */
function MQPolygonPrimitive () {
   MQLinePrimitive.call(this);
   this.setM_Xpath("LinePrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPOLYGONPRIMITIVE()));
   /**
    * Value to represent the fill color style to be used
    * @type MQColorStyle
    */
   var m_FillColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_RED);
   /**
    * Gets the m_FillColor
    * @return The m_FillColor object.
    * @type MQColorStyle
    */
   this.getFillColor = function() {
      return m_FillColor;
   };
   /**
    * Sets the m_FillColor
    * @param {MQColorStyle} obj the MQColorStyle to set m_FillColor to.
    * @type void
    */
   this.setFillColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FillColor = obj;
         }
      }
   };
   /**
    * Value to represent the color style to be used
    * @type MQFillStyle
    */
   var m_FillStyle = new MQFillStyle(MQCONSTANT.MQFILLSTYLE_SOLID);
   /**
    * Gets the m_FillStyle
    * @return The m_FillStyle object.
    * @type MQFillStyle
    */
   this.getFillStyle = function() {
      return m_FillStyle;
   };
   /**
    * Sets the m_FillStyle
    * @param {MQFillStyle} obj the MQFillStyle to set m_FillStyle to.
    * @type void
    */
   this.setFillStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQFillStyle"){
            m_FillStyle = obj;
         }
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPolygonPrimitive.prototype.getClassName = function(){
      return "MQPolygonPrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPolygonPrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPolygonPrimitive.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var ll = new MQLatLngCollection();
      ll.setM_Xpath("LatLngs");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         ll.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
      this.setLatLngs(ll);
      var pt = new MQPointCollection();
      pt.setM_Xpath("Points");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")!==null)
         pt.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/Points")));
      this.setPoints(ll);
      this.setDrawTrigger(new MQDrawTrigger(Math.floor(this.getProperty("DrawTrigger"))));
      this.setCoordinateType(new MQCoordinateType(Math.floor(this.getProperty("CoordinateType"))));
      this.setColor(new MQColorStyle(Math.floor(this.getProperty("Color"))));
      this.setStyle(new MQPenStyle(Math.floor(this.getProperty("Style"))));
      this.setFillColor(new MQColorStyle(Math.floor(this.getProperty("FillColor"))));
      this.setFillStyle(new MQFillStyle(Math.floor(this.getProperty("FillStyle"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPolygonPrimitive.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      newNode = MQA.createXMLDoc(this.getPoints().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "Points"));
      this.setProperty("DrawTrigger", this.getDrawTrigger().intValue());
      this.setProperty("CoordinateType", this.getCoordinateType().intValue());
      this.setProperty("Color", this.getColor().intValue());
      this.setProperty("Style", this.getStyle().intValue());
      this.setProperty("FillColor", this.getFillColor().intValue());
      this.setProperty("FillStyle", this.getFillStyle().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
// Begin Class MQPolygonPrimitive

// Begin Class MQRectanglePrimitive
/* Inheirit from MQPrimitive */
MQRectanglePrimitive.prototype = new MQPrimitive();
MQRectanglePrimitive.prototype.constructor = MQRectanglePrimitive;
/**
 * Constructs a new MQRectanglePrimitive object.
 * @class Describes linear annotations to be placed on the map. The line is
 * converted into latitude/longitude points so it can be placed in the
 * same geographical location after the map is manipulated.
 * @extends MQPrimitive
 * @see MQColorStyle
 * @see MQPenStyle
 * @see MQPointCollection
 * @see MQLatLngCollection
 */
function MQRectanglePrimitive () {
   MQPrimitive.call(this);
   this.setM_Xpath("RectanglePrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getRECTANGLEPRIMITIVE()));
   /**
    * Value to represent the color style to be used
    * @type MQColorStyle
    */
   var m_Color = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_BLACK);
   /**
    * Gets the m_Color
    * @return The m_Color object.
    * @type MQColorStyle
    */
   this.getColor = function() {
      return m_Color;
   };
   /**
    * Sets the m_Color
    * @param {MQColorStyle} obj the MQColorStyle to set m_Color to.
    * @type void
    */
   this.setColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_Color = obj;
         }
      }
   };
   /**
    * Value to represent the pen style to be used
    * @type MQPenStyle
    */
   var m_Style = new MQPenStyle(MQCONSTANT.MQPENSTYLE_SOLID);
   /**
    * Gets the m_Style
    * @return The m_Style object.
    * @type MQPenStyle
    */
   this.getStyle = function() {
      return m_Style;
   };
   /**
    * Sets the m_Style
    * @param {MQPenStyle} obj the MQPenStyle to set m_Style to.
    * @type void
    */
   this.setStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQPenStyle"){
            m_Style = obj;
         }
      }
   };
   /**
    * Value to represent the fill color style to be used
    * @type MQColorStyle
    */
   var m_FillColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_RED);
   /**
    * Gets the m_FillColor
    * @return The m_FillColor object.
    * @type MQColorStyle
    */
   this.getFillColor = function() {
      return m_FillColor;
   };
   /**
    * Sets the m_FillColor
    * @param {MQColorStyle} obj the MQColorStyle to set m_FillColor to.
    * @type void
    */
   this.setFillColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FillColor = obj;
         }
      }
   };
   /**
    * Value to represent the color style to be used
    * @type MQFillStyle
    */
   var m_FillStyle = new MQFillStyle(MQCONSTANT.MQFILLSTYLE_SOLID);
   /**
    * Gets the m_FillStyle
    * @return The m_FillStyle object.
    * @type MQFillStyle
    */
   this.getFillStyle = function() {
      return m_FillStyle;
   };
   /**
    * Sets the m_FillStyle
    * @param {MQFillStyle} obj the MQFillStyle to set m_FillStyle to.
    * @type void
    */
   this.setFillStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQFillStyle"){
            m_FillStyle = obj;
         }
      }
   };
   /**
    * Value to represent the latitude/longitude of the upper left corner
    * @type MQLatLng
    */
   var m_UpperLeftLL = new MQLatLng("UpperLeftLatLng");
   /**
    * Gets the latitude/longitude of the upper left corner.
    * @return The m_UpperLeftLL object.
    * @type MQLatLng
    */
   this.getUpperLeftLatLng = function() {
      return m_UpperLeftLL;
   };
   /**
    * Sets the latitude/longitude of the upper left corner.
    * @param {MQLatLng} latLng the MQLatLng to set m_UpperLeftLL to.
    * @type void
    */
   this.setUpperLeftLatLng = function(latLng) {
      m_UpperLeftLL.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Value to represent the latitude/longitude of the lower right corner
    * @type MQLatLng
    */
   var m_LowerRightLL = new MQLatLng("LowerRightLatLng");
   /**
    * Gets the latitude/longitude of the lower right corner
    * @return The m_LowerRightLL object.
    * @type MQLatLng
    */
   this.getLowerRightLatLng = function() {
      return m_LowerRightLL;
   };
   /**
    * Sets the latitude/longitude of the lower right corner
    * @param {MQLatLng} latLng the MQLatLng to set m_LowerRightLL to.
    * @type void
    */
   this.setLowerRightLatLng = function(latLng) {
      m_LowerRightLL.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Value to represent the x/y of the upper left corner
    * @type MQPoint
    */
   var m_UpperLeftPT = new MQPoint("UpperLeftPoint");
   /**
    * Gets the x/y of the upper left corner.
    * @return The m_UpperLeftPT object.
    * @type MQPoint
    */
   this.getUpperLeftPoint = function() {
      return m_UpperLeftPT;
   };
   /**
    * Sets the x/y of the upper left corner.
    * @param {MQPoint} point the MQPoint to set m_UpperLeftPT to.
    * @type void
    */
   this.setUpperLeftPoint = function(point) {
      m_UpperLeftPT.setXY(point.getX(), point.getY());
   };
   /**
    * Value to represent the x/y of the lower right corner
    * @type MQPoint
    */
   var m_LowerRightPT = new MQPoint("LowerRightPoint");
   /**
    * Gets the x/y of the lower right corner
    * @return The m_LowerRightPT object.
    * @type MQPoint
    */
   this.getLowerRightPoint = function() {
      return m_LowerRightPT;
   };
   /**
    * Sets the x/y of the lower right corner
    * @param {MQPoint} point the MQPoint to set m_LowerRightPT to.
    * @type void
    */
   this.setLowerRightPoint = function(point) {
      m_LowerRightPT.setXY(point.getX(), point.getY());
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRectanglePrimitive.prototype.getClassName = function(){
      return "MQRectanglePrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRectanglePrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQRectanglePrimitive.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getUpperLeftLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")));
      latlng = this.getLowerRightLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightLatLng")));
      var point = this.getUpperLeftPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")));
      point = this.getLowerRightPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightPoint")));

      this.setDrawTrigger(new MQDrawTrigger(Math.floor(this.getProperty("DrawTrigger"))));
      this.setCoordinateType(new MQCoordinateType(Math.floor(this.getProperty("CoordinateType"))));
      this.setColor(new MQColorStyle(Math.floor(this.getProperty("Color"))));
      this.setStyle(new MQPenStyle(Math.floor(this.getProperty("Style"))));
      this.setFillColor(new MQColorStyle(Math.floor(this.getProperty("FillColor"))));
      this.setFillStyle(new MQFillStyle(Math.floor(this.getProperty("FillStyle"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQRectanglePrimitive.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getUpperLeftLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftLatLng"));
      newNode = MQA.createXMLDoc(this.getLowerRightLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LowerRightLatLng"));
      newNode = MQA.createXMLDoc(this.getUpperLeftPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftPoint"));
      newNode = MQA.createXMLDoc(this.getLowerRightPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LowerRightPoint"));

      this.setProperty("DrawTrigger", this.getDrawTrigger().intValue());
      this.setProperty("CoordinateType", this.getCoordinateType().intValue());
      this.setProperty("Color", this.getColor().intValue());
      this.setProperty("Style", this.getStyle().intValue());
      this.setProperty("FillColor", this.getFillColor().intValue());
      this.setProperty("FillStyle", this.getFillStyle().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the value representing the width of this object.
    * @param {int} width The new Width to be assigned to this object.
    * @type void
    */
   MQRectanglePrimitive.prototype.setWidth = function(width){
      this.setProperty("Width",width);
   };
   /**
    * Returns the value representing the width of this object.
    * @return The Width of this object.
    * @type int
    */
   MQRectanglePrimitive.prototype.getWidth = function(){
      return this.getProperty("Width");
   }
// End Class MQRectanglePrimitive

// Begin Class MQEllipsePrimitive
/* Inheirit from MQRectanglePrimitive */
MQEllipsePrimitive.prototype = new MQRectanglePrimitive();
MQEllipsePrimitive.prototype.constructor = MQEllipsePrimitive;
/**
 * Constructs a new MQEllipsePrimitive object.
 * @class Describes circle annotations to be placed on the map. The ellipse
 * is positioned by the Upper Left and Lower Right coordinates of a
 * bounding rectangle. The coordinates are converted into
 * latitude/longitude points so the annotation can be placed in the
 * same geographical location after the map is manipulated.
 * @extends MQRectanglePrimitive
 */
function MQEllipsePrimitive () {
   MQRectanglePrimitive.call(this);
   this.setM_Xpath("EllipsePrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getELLIPSEPRIMITIVE()));
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQEllipsePrimitive.prototype.getClassName = function(){
      return "MQEllipsePrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQEllipsePrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
// End Class MQEllipsePrimitive

// Begin Class MQSymbolPrimitive
/* Inheirit from MQPrimitive */
MQSymbolPrimitive.prototype = new MQPrimitive();
MQSymbolPrimitive.prototype.constructor = MQSymbolPrimitive;
/**
 * Constructs a new MQSymbolPrimitive object.
 * @class Describes a SymbolPrimitive to be placed on a map. The SymbolPrimitive is initially
 * located by an X/Y coordinate. The coordinate is converted into a
 * latitude/longitude point so the SymbolPrimitive can be placed in the same
 * geographical location after the map is manipulated.
 * @extends MQPrimitive
 * @see MQPoint
 * @see MQLatLng
 * @see MQSymbolType
 */
function MQSymbolPrimitive () {
   MQPrimitive.call(this);
   this.setM_Xpath("SymbolPrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSYMBOLPRIMITIVE()));
   /**
    * Value to represent the x/y of the center of the map
    * @type MQPoint
    */
   var m_pt = new MQPoint("CenterPoint");
   /**
    * Gets the x/y of the center of the map
    * @return The m_pt object.
    * @type MQPoint
    */
   this.getCenterPoint = function() {
      return m_pt;
   };
   /**
    * Sets the x/y of the center of the map
    * @param {MQPoint} point the MQPoint to set m_pt to.
    * @type void
    */
   this.setCenterPoint = function(point) {
      m_pt.setXY(point.getX(), point.getY());
   };
   /**
    * Value to represent the latitude/longitude of the center of the map
    * @type MQLatLng
    */
   var m_ll = new MQLatLng("CenterLatLng");
   /**
    * Gets the latitude/longitude of the center of the map
    * @return The m_ll object.
    * @type MQLatLng
    */
   this.getCenterLatLng = function() {
      return m_ll;
   };
   /**
    * Sets the latitude/longitude of the center of the map
    * @param {MQLatLng} latLng the MQLatLng to set m_ll to.
    * @type void
    */
   this.setCenterLatLng = function(latLng) {
      m_ll.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Value to for symbol type
    * @type MQSymbolType
    */
   var m_SymbolType = new MQSymbolType(MQCONSTANT.MQSYMBOLTYPE_RASTER);
   /**
    * Gets the m_SymbolType
    * @return The m_SymbolType object.
    * @type MQSymbolType
    */
   this.getSymbolType = function() {
      return m_SymbolType;
   };
   /**
    * Sets the m_SymbolType
    * @param {MQSymbolType} obj the MQSymbolType to set m_SymbolType to.
    * @type void
    */
   this.setSymbolType = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQSymbolType"){
            m_SymbolType = obj;
         }
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSymbolPrimitive.prototype.getClassName = function(){
      return "MQSymbolPrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSymbolPrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQSymbolPrimitive.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getCenterLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")));
      var point = this.getCenterPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));

      this.setDrawTrigger(new MQDrawTrigger(Math.floor(this.getProperty("DrawTrigger"))));
      this.setCoordinateType(new MQCoordinateType(Math.floor(this.getProperty("CoordinateType"))));
      this.setSymbolType(new MQSymbolType(Math.floor(this.getProperty("SymbolType"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQSymbolPrimitive.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenterLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      newNode = MQA.createXMLDoc(this.getCenterPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));

      this.setProperty("DrawTrigger", this.getDrawTrigger().intValue());
      this.setProperty("CoordinateType", this.getCoordinateType().intValue());
      this.setProperty("SymbolType", this.getSymbolType().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the value representing the symbolName of this object.
    * @param {String} symbolName The new SymbolName to be assigned to this object.
    * @type void
    */
   MQSymbolPrimitive.prototype.setSymbolName = function(symbolName){
      this.setProperty("SymbolName",symbolName);
   };
   /**
    * Returns the value representing the symbolName of this object.
    * @return The SymbolName of this object.
    * @type String
    */
   MQSymbolPrimitive.prototype.getSymbolName = function(){
      return this.getProperty("SymbolName");
   }
// End Class MQSymbolPrimitive

// Begin Class MQTextPrimitive
/* Inheirit from MQPrimitive */
MQTextPrimitive.prototype = new MQPrimitive();
MQTextPrimitive.prototype.constructor = MQTextPrimitive;
/**
 * Constructs a new MQTextPrimitive object.
 * @class Describes text to be placed on the map. The text is initially
 * located by the X/Y coordinate. The coordinate is converted into a
 * latitude/longitude point so the annotation can be placed in the
 * same geographical location after the map is manipulated.
 * @extends MQPrimitive
 * @see MQColorStyle
 * @see MQFontStyle
 * @see MQLatLng
 * @see MQPoint
 * @see MQConstants
 */
function MQTextPrimitive () {
   MQPrimitive.call(this);
   this.setM_Xpath("TextPrimitive");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getTEXTPRIMITIVE()));
   /**
    * Value to represent the x/y of the upper left of the map
    * @type MQPoint
    */
   var m_pt = new MQPoint("UpperLeftPoint");
   /**
    * Gets the x/y of the upper left of the map
    * @return The m_pt object.
    * @type MQPoint
    */
   this.getUpperLeftPoint = function() {
      return m_pt;
   };
   /**
    * Sets the x/y of the upper left of the map
    * @param {MQPoint} point The MQPoint to set m_pt to.
    * @type void
    */
   this.setUpperLeftPoint = function(point) {
      m_pt.setXY(point.getX(), point.getY());
   };
   /**
    * Value to represent the latitude/longitude of the upper left of the map
    * @type MQLatLng
    */
   var m_ll = new MQLatLng("UpperLeftLatLng");
   /**
    * Gets the latitude/longitude of the upper left of the map
    * @return The m_ll object.
    * @type MQLatLng
    */
   this.getUpperLeftLatLng = function() {
      return m_ll;
   };
   /**
    * Sets the latitude/longitude of the upper left of the map
    * @param {MQLatLng} latLng The MQLatLng to set m_ll to.
    * @type void
    */
   this.setUpperLeftLatLng = function(latLng) {
      m_ll.setLatLng(latLng.getLatitude(), latLng.getLongitude());
   };
   /**
    * Value to represent the color style to be used
    * @type MQColorStyle
    */
   var m_color = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_BLACK);
   /**
    * Gets the color.
    * @return The m_color type.
    * @type MQColorStyle
    */
   this.getColor = function() {
      return m_color;
   };
   /**
    * Sets the color.
    * @param {MQColorStyle} obj The MQColorStyle to set m_color type to.
    * @type void
    */
   this.setColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_color = obj;
         }
      }
   };
   /**
    * Value to represent the font style to be used
    * @type MQFontStyle
    */
   var m_nStyle = new MQFontStyle(MQCONSTANT.MQFONTSTYLE_BOXED);
   /**
    * Gets the font style.
    * @return The m_nStyle.
    * @type MQFontStyle
    */
   this.getStyle = function() {
      return m_nStyle;
   };
   /**
    * Sets the font style.
    * @param {MQFontStyle} obj The MQFontStyle to set m_nStyle type to.
    * @type void
    */
   this.setStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQFontStyle"){
            m_nStyle = obj;
         }
      }
   };
   /**
    * Value to represent the background color style to be used
    * @type MQColorStyle
    */
   var m_bgdColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_WHITE);
   /**
    * Gets the background color.
    * @return The m_bgdColor type.
    * @type MQColorStyle
    */
   this.getBkgdColor = function() {
      return m_bgdColor;
   };
   /**
    * Sets the background color.
    * @param {MQColorStyle} obj The MQColorStyle to set m_bgdColor type to.
    * @type void
    */
   this.setBkgdColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_bgdColor = obj;
         }
      }
   };
   /**
    * Value to represent the box outline color style to be used
    * @type MQColorStyle
    */
   var m_lBoxOutlineColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the box outline color.
    * @return The m_lBoxOutlineColor type.
    * @type MQColorStyle
    */
   this.getBoxOutlineColor = function() {
      return m_lBoxOutlineColor;
   };
   /**
    * Sets the box outline color.
    * @param {MQColorStyle} obj The MQColorStyle to set m_lBoxOutlineColor type to.
    * @type void
    */
   this.setBoxOutlineColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_lBoxOutlineColor = obj;
         }
      }
   };
   /**
    * Value to represent the outline color style to be used
    * @type MQColorStyle
    */
   var m_lOutlineColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the outline color.
    * @return The m_lOutlineColor type.
    * @type MQColorStyle
    */
   this.getOutlineColor = function() {
      return m_lOutlineColor;
   };
   /**
    * Sets the outline color.
    * @param {MQColorStyle} obj The MQColorStyle to set m_lOutlineColor type to.
    * @type void
    */
   this.setOutlineColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_lOutlineColor = obj;
         }
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQTextPrimitive.prototype.getClassName = function(){
      return "MQTextPrimitive";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQTextPrimitive.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQTextPrimitive.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var latlng = this.getUpperLeftLatLng();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")!==null)
         latlng.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")));
      var point = this.getUpperLeftPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")!==null)
         point.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")));

      this.setDrawTrigger(new MQDrawTrigger(Math.floor(this.getProperty("DrawTrigger"))));
      this.setCoordinateType(new MQCoordinateType(Math.floor(this.getProperty("CoordinateType"))));
      this.setColor(new MQColorStyle(Math.floor(this.getProperty("Color"))));
      this.setStyle(new MQFontStyle(Math.floor(this.getProperty("Style"))));
      this.setBkgdColor(new MQColorStyle(Math.floor(this.getProperty("BkgdColor"))));
      this.setBoxOutlineColor(new MQColorStyle(Math.floor(this.getProperty("BoxOutlineColor"))));
      this.setOutlineColor(new MQColorStyle(Math.floor(this.getProperty("OutlineColor"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQTextPrimitive.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getUpperLeftLatLng().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftLatLng"));
      newNode = MQA.createXMLDoc(this.getUpperLeftPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftPoint"));

      this.setProperty("DrawTrigger", this.getDrawTrigger().intValue());
      this.setProperty("CoordinateType", this.getCoordinateType().intValue());
      this.setProperty("Color", this.getColor().intValue());
      this.setProperty("Style", this.getStyle().intValue());
      this.setProperty("BkgdColor", this.getBkgdColor().intValue());
      this.setProperty("BoxOutlineColor", this.getBoxOutlineColor().intValue());
      this.setProperty("OutlineColor", this.getOutlineColor().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the Text of this object.
    * @param {String} str The new Text of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setText = function(str){
      this.setProperty("Text",str);
   };
   /**
    * Returns the Text of this object.
    * @return The new Text of this object.
    * @type String
    */
   MQTextPrimitive.prototype.getText = function(){
      return this.getProperty("Text");
   }
   /**
    * Sets the FontName of this object.
    * @param {String} str The new FontName of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setFontName = function(str){
      this.setProperty("FontName",str);
   };
   /**
    * Returns the FontName of this object.
    * @return The new FontName of this object.
    * @type String
    */
   MQTextPrimitive.prototype.getFontName = function(){
      return this.getProperty("FontName");
   }
   /**
    * Sets the Width of this object.
    * @param {int} val The new Width of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setWidth = function(val){
      this.setProperty("Width",val);
   };
   /**
    * Returns the Width of this object.
    * @return The new Width of this object.
    * @type int
    */
   MQTextPrimitive.prototype.getWidth = function(){
      return this.getProperty("Width");
   }
   /**
    * Sets the FontSize of this object.
    * @param {int} val The new FontSize of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setFontSize = function(val){
      this.setProperty("FontSize",val);
   };
   /**
    * Returns the FontSize of this object.
    * @return The new FontSize of this object.
    * @type int
    */
   MQTextPrimitive.prototype.getFontSize = function(){
      return this.getProperty("FontSize");
   }
   /**
    * Sets the Margin of this object.
    * @param {int} val The new Margin of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setMargin = function(val){
      this.setProperty("Margin",val);
   };
   /**
    * Returns the Margin of this object.
    * @return The new Margin of this object.
    * @type int
    */
   MQTextPrimitive.prototype.getMargin = function(){
      return this.getProperty("Margin");
   }
   /**
    * Sets the TextAlignment of this object.
    * @param {int} val The new TextAlignment of this object.
    * @type void
    */
   MQTextPrimitive.prototype.setTextAlignment = function(val){
      this.setProperty("TextAlignment",val);
   };
   /**
    * Returns the TextAlignment of this object.
    * @return The new TextAlignment of this object.
    * @type int
    */
   MQTextPrimitive.prototype.getTextAlignment = function(){
      return this.getProperty("TextAlignment");
   }
// End Class MQTextPrimitive

// Begin class MQFeatureSpecifier
/* Inheirit from MQObject */
MQFeatureSpecifier.prototype = new MQObject();
MQFeatureSpecifier.prototype.constructor = MQFeatureSpecifier;
/**
 * Constructs a new MQFeatureSpecifier object.
 * @class Describes FeatureSpecifier annotations to be placed on the map. The FeatureSpecifier is
 * converted into latitude/longitude points so it can be placed in the
 * same geographical location after the map is manipulated.
 * @extends MQObject
 * @see MQFeatureSpeciferAttributeType
 */
function MQFeatureSpecifier(){
   MQObject.call(this);
   this.setM_Xpath("FeatureSpecifier");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getFEATURESPECIFIER()));
   /**
    * Value to represent the Feature Specifer Attribute Type to be used
    * @type MQFeatureSpeciferAttributeType
    */
   var m_Type = new MQFeatureSpeciferAttributeType(MQCONSTANT.MQFEATURESPECIFERATTRIBUTETYPE_GEFID);
   /**
    * Gets the Feature Specifer Attribute Type.
    * @return The m_Type.
    * @type MQFeatureSpeciferAttributeType
    */
   this.getAttributeType = function() {
      return m_Type;
   };
   /**
    * Sets the Feature Specifer Attribute Type.
    * @param {MQFeatureSpeciferAttributeType} obj The MQFeatureSpeciferAttributeType to set m_Type to.
    * @type void
    */
   this.setAttributeType = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQFeatureSpeciferAttributeType"){
            m_Type = obj;
         }
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQFeatureSpecifier.prototype.getClassName = function(){
      return "MQFeatureSpecifier";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFeatureSpecifier.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Set attribute value member of the FeatureSpecifier.
    * @param {String} val The value member of the FeatureSpecifier.
    * @type void
    */
   MQFeatureSpecifier.prototype.setAttributeValue = function(val){
      this.setProperty("AttributeValue",val);
   };
   /**
    * Get attribute value member of the FeatureSpecifier.
    * @return The new value member of the FeatureSpecifier.
    * @type String
    */
   MQFeatureSpecifier.prototype.getAttributeValue = function(){
      return this.getProperty("AttributeValue");
   };
// End class MQFeatureSpecifier

// Begin class MQBaseDTStyle
/* Inheirit from MQObject */
MQBaseDTStyle.prototype = new MQObject();
MQBaseDTStyle.prototype.constructor = MQBaseDTStyle;
/**
 * Constructs a new MQBaseDTStyle object.
 * @class This is the base class for the DTStyle and DTStyleEx objects.
 * @extends MQObject
 * @see MQDTFeatureStyleEx
 * @see MQDTStyle
 * @see MQDTStyleEx
 */
function MQBaseDTStyle(){
   MQObject.call(this);
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQBaseDTStyle.prototype.getClassName = function(){
      return "MQBaseDTStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQBaseDTStyle.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Sets the DT of this object.
    * @param {int} val The new DT of this object.
    * @type void
    */
   MQBaseDTStyle.prototype.setDT = function(val){
      this.setProperty("DT",val);
   };
   /**
    * Returns the DT of this object.
    * @return The new DT of this object.
    * @type int
    */
   MQBaseDTStyle.prototype.getDT = function(){
      return this.getProperty("DT");
   }
   /**
    * Sets the HighScale of this object.
    * @param {int} val The new HighScale of this object.
    * @type void
    */
   MQBaseDTStyle.prototype.setHighScale = function(val){
      this.setProperty("HighScale",val);
   };
   /**
    * Returns the HighScale of this object.
    * @return The new HighScale of this object.
    * @type int
    */
   MQBaseDTStyle.prototype.getHighScale = function(){
      return this.getProperty("HighScale");
   }
   /**
    * Sets the LowScale of this object.
    * @param {int} val The new LowScale of this object.
    * @type void
    */
   MQBaseDTStyle.prototype.setLowScale = function(val){
      this.setProperty("LowScale",val);
   };
   /**
    * Returns the LowScale of this object.
    * @return The new LowScale of this object.
    * @type int
    */
   MQBaseDTStyle.prototype.getLowScale = function(){
      return this.getProperty("LowScale");
   };
   /**
    * Checks the equality of the values of two MQBaseDTStyle objects.
    * @return True if values are equal and false otherwise.
    * @type Boolean
    */
    MQBaseDTStyle.prototype.equals = function(type){
      if(type){
         try{
            var className = type.getClassName();
         } catch (error){
            alert("Invalid type for this function!");
            throw "Invalid type for this function!";
         }
         if(className === this.getClassName()){
            return (   this.getDT() === type.getDT() &&
                     this.getHighScale() === type.getHighScale() &&
                     this.getLowScale() === type.getLowScale());
         } else {
            alert("Invalid type for this function!");
            throw "Invalid type for this function!";
         }
      } else {
         alert("An MQBaseDTStyle parameter must be provided for this function!");
         throw "An MQBaseDTStyle parameter must be provided for this function!";
      }
   };
// End class MQBaseDTStyle

// Begin class MQDTStyle
/* Inheirit from MQBaseDTStyle */
MQDTStyle.prototype = new MQBaseDTStyle();
MQDTStyle.prototype.constructor = MQDTStyle;
/**
 * Constructs a new MQDTStyle object.
 * @class Contains the appearance characteristics of all features for a given
 * display type. These characteristics include the icon name, icon type
 * (Raster or Metafile), icon visible, label visible, low scale index,
 * and high scale index. The scale indexes indicate the range of map
 * scales at which the display type style applies. If the user desires
 * to create his own icon, it must be a .BMP, which is a raster image.
 *
 * In order to modify feature types other than points or use complex
 * style descriptions, use the Style File Editor application to design
 * complex styles to use with the DTStyleEx class. The Style File
 * Editor application can export complex styles as map style strings.
 * The DTStyleEx class has all the functionality described for DTStyle,
 * and additionally can modify or add styles for point, line, and
 * polygon map features using map style strings.
 * @extends MQBaseDTStyle
 */
function MQDTStyle(){
   MQBaseDTStyle.call(this);
   this.setM_Xpath("DTStyle");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDTSTYLE()));
   /**
    * Value to represent the font color style to be used
    * @type MQColorStyle
    */
   var m_FontColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the color.
    * @return The m_FontColor type.
    * @type MQColorStyle
    */
   this.getFontColor = function() {
      return m_FontColor;
   };
   /**
    * Sets the color.
    * @param {MQColorStyle} obj The MQColorStyle to set m_FontColor type to.
    * @type void
    */
   this.setFontColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FontColor = obj;
         }
      }
   };
   /**
    * Value to represent the font outline color style to be used
    * @type MQColorStyle
    */
   var m_FontOutlineColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the FontOutlineColor.
    * @return The m_FontOutlineColor type.
    * @type MQColorStyle
    */
   this.getFontOutlineColor = function() {
      return m_FontOutlineColor;
   };
   /**
    * Sets the FontOutlineColor.
    * @param {MQColorStyle} obj The MQColorStyle to set m_FontOutlineColor type to.
    * @type void
    */
   this.setFontOutlineColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FontOutlineColor = obj;
         }
      }
   };
   /**
    * Value to represent the font box background color style to be used
    * @type MQColorStyle
    */
   var m_FontBoxBkgdColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the FontBoxBkgdColor.
    * @return The m_FontBoxBkgdColor type.
    * @type MQColorStyle
    */
   this.getFontBoxBkgdColor = function() {
      return m_FontBoxBkgdColor;
   };
   /**
    * Sets the FontBoxBkgdColor.
    * @param {MQColorStyle} obj The MQColorStyle to set m_FontBoxBkgdColor type to.
    * @type void
    */
   this.setFontBoxBkgdColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FontBoxBkgdColor = obj;
         }
      }
   };
   /**
    * Value to represent the font box outline color style to be used
    * @type MQColorStyle
    */
   var m_FontBoxOutlineColor = new MQColorStyle(MQCONSTANT.MQCOLORSTYLE_INVALID);
   /**
    * Gets the FontBoxOutlineColor.
    * @return The m_FontBoxOutlineColor type.
    * @type MQColorStyle
    */
   this.getFontBoxOutlineColor = function() {
      return m_FontBoxOutlineColor;
   };
   /**
    * Sets the FontBoxOutlineColor.
    * @param {MQColorStyle} obj The MQColorStyle to set m_FontBoxOutlineColor type to.
    * @type void
    */
   this.setFontBoxOutlineColor = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQColorStyle"){
            m_FontBoxOutlineColor = obj;
         }
      }
   };
   /**
    * Value to represent the font style to be used
    * @type MQFontStyle
    */
   var m_FontStyle = new MQFontStyle(MQCONSTANT.MQFONTSTYLE_INVALID);
   /**
    * Gets the font.
    * @return The m_FontStyle type.
    * @type MQFontStyle
    */
   this.getFontStyle = function() {
      return m_FontStyle;
   };
   /**
    * Sets the font.
    * @param {MQFontStyle} obj The MQFontStyle to set m_FontStyle type to.
    * @type void
    */
   this.setFontStyle = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQFontStyle"){
            m_FontStyle = obj;
         }
      }
   };
   /**
    * Value to represent the symbol type to be used
    * @type MQSymbolType
    */
   var m_SymbolType = new MQSymbolType(MQCONSTANT.MQSYMBOLTYPE_RASTER);
   /**
    * Gets the color.
    * @return The m_SymbolType type.
    * @type MQSymbolType
    */
   this.getSymbolType = function() {
      return m_SymbolType;
   };
   /**
    * Sets the color.
    * @param {MQSymbolType} obj The MQSymbolType to set m_SymbolType type to.
    * @type void
    */
   this.setSymbolType = function(obj) {
      if(obj){
         if(obj.getClassName() === "MQSymbolType"){
            m_SymbolType = obj;
         }
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDTStyle.prototype.getClassName = function(){
      return "MQDTStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDTStyle.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQDTStyle.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      this.setFontColor(new MQColorStyle(Math.floor(this.getProperty("FontColor"))));
      this.setFontOutlineColor(new MQColorStyle(Math.floor(this.getProperty("FontOutlineColor"))));
      this.setFontBoxBkgdColor(new MQColorStyle(Math.floor(this.getProperty("FontBoxBkgdColor"))));
      this.setFontBoxOutlineColor(new MQColorStyle(Math.floor(this.getProperty("FontBoxOutlineColor"))));
      this.setFontStyle(new MQFontStyle(Math.floor(this.getProperty("FeatureSpeciferAttributeType"))));
      this.setSymbolType(new MQSymbolType(Math.floor(this.getProperty("SymbolType"))));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQDTStyle.prototype.saveXml = function () {
      this.setProperty("FontColor", this.getFontColor().intValue());
      this.setProperty("FontOutlineColor", this.getFontOutlineColor().intValue());
      this.setProperty("FontBoxBkgdColor", this.getFontBoxBkgdColor().intValue());
      this.setProperty("FontBoxOutlineColor", this.getFontBoxOutlineColor().intValue());
      this.setProperty("FontStyle", this.getFontStyle().intValue());
      this.setProperty("SymbolType", this.getSymbolType().intValue());
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the SymbolName of this object.
    * @param {String} val The new SymbolName of this object.
    * @type void
    */
   MQDTStyle.prototype.setSymbolName = function(val){
      this.setProperty("SymbolName",val);
   };
   /**
    * Returns the SymbolName of this object.
    * @return The new SymbolName of this object.
    * @type String
    */
   MQDTStyle.prototype.getSymbolName = function(){
      return this.getProperty("SymbolName");
   };
   /**
    * Sets the FontName of this object.
    * @param {String} val The new FontName of this object.
    * @type void
    */
   MQDTStyle.prototype.setFontName = function(val){
      this.setProperty("FontName",val);
   };
   /**
    * Returns the FontName of this object.
    * @return The new FontName of this object.
    * @type String
    */
   MQDTStyle.prototype.getFontName = function(){
      return this.getProperty("FontName");
   };
   /**
    * Sets the Visible flag.
    * @param {Boolean} bFlag The Visible flag
    * @type void
    */
   MQDTStyle.prototype.setVisible = function(bFlag){
      this.setProperty("Visible", (bFlag===true)?1:0);
   };
   /**
    * Gets the Visible flag
    * @return true if a Visible = 1, false otherwise.
    * @type boolean
    */
   MQDTStyle.prototype.getVisible = function(){
      return (this.getProperty("Visible")==1)? true : false;
   };
   /**
    * Sets the LabelVisible flag.
    * @param {Boolean} bFlag The LabelVisible flag
    * @type void
    */
   MQDTStyle.prototype.setLabelVisible = function(bFlag){
      this.setProperty("LabelVisible", (bFlag===true)?1:0);
   };
   /**
    * Gets the LabelVisible flag
    * @return true if a LabelVisible = 1, false otherwise.
    * @type boolean
    */
   MQDTStyle.prototype.getLabelVisible = function(){
      return (this.getProperty("LabelVisible")==1)? true : false;
   };
   /**
    * Sets the FontSize of this object.
    * @param {int} val The new FontSize of this object.
    * @type void
    */
   MQDTStyle.prototype.setFontSize = function(val){
      this.setProperty("FontSize",val);
   };
   /**
    * Returns the FontSize of this object.
    * @return The new FontSize of this object.
    * @type int
    */
   MQDTStyle.prototype.getFontSize = function(){
      return this.getProperty("FontSize");
   }
   /**
    * Sets the FontBoxMargin of this object.
    * @param {int} val The new FontBoxMargin of this object.
    * @type void
    */
   MQDTStyle.prototype.setFontBoxMargin = function(val){
      this.setProperty("FontBoxMargin",val);
   };
   /**
    * Returns the FontBoxMargin of this object.
    * @return The new FontBoxMargin of this object.
    * @type int
    */
   MQDTStyle.prototype.getFontBoxMargin = function(){
      return this.getProperty("FontBoxMargin");
   }
// End class MQDTStyle

// Begin class MQDTStyleEx
/* Inheirit from MQBaseDTStyle */
MQDTStyleEx.prototype = new MQBaseDTStyle();
MQDTStyleEx.prototype.constructor = MQDTStyleEx;
/**
 * Constructs a new MQDTStyleEx object.
 * @class This is the base class for the DTStyle and DTStyleEx objects.
 * @extends MQBaseDTStyle
 */
function MQDTStyleEx(){
   MQBaseDTStyle.call(this);
   this.setM_Xpath("DTStyleEx");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDTSTYLEEX()));
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDTStyleEx.prototype.getClassName = function(){
      return "MQDTStyleEx";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDTStyleEx.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQDTStyleEx.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQDTStyleEx.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the StyleString of this object.
    * @param {String} val The new StyleString of this object.
    * @type void
    */
   MQDTStyleEx.prototype.setStyleString = function(val){
      this.setProperty("StyleString",val);
   };
   /**
    * Returns the StyleString of this object.
    * @return The new StyleString of this object.
    * @type String
    */
   MQDTStyleEx.prototype.getStyleString = function(){
      return this.getProperty("StyleString");
   };
// End class MQDTStyleEx

// Begin class MQDTFeatureStyleEx
/* Inheirit from MQDTStyle */
MQDTFeatureStyleEx.prototype = new MQDTStyleEx();
MQDTFeatureStyleEx.prototype.constructor = MQDTFeatureStyleEx;
/**
 * Constructs a new MQDTFeatureStyleEx object.
 * @class Contains the appearance characteristics of all features for a given
 * display type. These characteristics include the icon name, icon type
 * (Raster or Metafile), icon visible, label visible, low scale index,
 * and high scale index. The scale indexes indicate the range of map
 * scales at which the display type style applies. If the user desires
 * to create his own icon, it must be a .BMP, which is a raster image.
 *
 * The MQDTFeatureStyleEx class is very similar to DTStyleEx, but lets
 * developers specify what map features to override by providing its
 * name or unique ID (GEF ID) within a map data set.
 * @extends MQDTStyleEx
 * @see MQFeatureSpecifierCollection
 */
function MQDTFeatureStyleEx(){
   MQBaseDTStyle.call(this);
   this.setM_Xpath("DTStyleEx");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDTFEATURESTYLEEX()));
   /**
    * Value to represent Feature Specifier Collection
    * @type MQFeatureSpecifierCollection
    */
   var m_Col = new MQFeatureSpecifierCollection("FeatureSpecifier");
   m_Col.setM_Xpath("FeatureSpecifierCollection");
   /**
    * Returns the m_Col object.
    * @return The m_Col object.
    * @type MQFeatureSpecifierCollection
    */
   this.getFeatureSpecifiers = function() {
      return m_Col;
   };
   /**
    * Sets the m_Col object.
    * @param {MQFeatureSpecifierCollection} col the MQFeatureSpecifierCollection to set m_Col to.
    * @type void
    */
   this.setFeatureSpecifiers = function(col) {
      if (mqIsClass(m_Col.getClassName(), col, false)){
         m_Col.removeAll();
         m_Col.append(col);
      }
   };
};
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDTFeatureStyleEx.prototype.getClassName = function(){
      return "MQDTFeatureStyleEx";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDTFeatureStyleEx.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQDTFeatureStyleEx.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQDTFeatureStyleEx.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQDTStyleEx

// Begin Class MQMapCommand
/* Inheirit from MQObject */
MQMapCommand.prototype = new MQObject();
MQMapCommand.prototype.constructor = MQMapCommand;
/**
 * Constructs a new MQMapCommand object.
 * @class Allows actions (such as panning, zooming in, and zooming out) to be
 * performed on a map.
 * @extends MQObject
 */
function MQMapCommand () {
   MQObject.call(this);
   this.setM_Xpath("DTStyleEx");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDTFEATURESTYLEEX()));
   /**
    * Value to represent Feature Specifier Collection
    * @type MQFeatureSpecifierCollection
    */
   var m_Col = new MQFeatureSpecifierCollection("FeatureSpecifier");
   m_Col.setM_Xpath("FeatureSpecifierCollection");
   /**
    * Returns the m_Col object.
    * @return The m_Col object.
    * @type MQFeatureSpecifierCollection
    */
   this.getFeatureSpecifiers = function() {
      return m_Col;
   };
   /**
    * Sets the m_Col object.
    * @param {MQFeatureSpecifierCollection} col the MQFeatureSpecifierCollection to set m_Col to.
    * @type void
    */
   this.setFeatureSpecifiers = function(col) {
      if (col.getClassName()==="MQFeatureSpecifierCollection")
         m_Col = col;
      else {
         alert("failure in setFeatureSpecifiers");
         throw "failure in setFeatureSpecifiers";
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQMapCommand.prototype.getClassName = function(){
      return "MQMapCommand";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQMapCommand.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQMapCommand

// Begin Class MQBestFit
/* Inheirit from MQMapCommand */
MQBestFit.prototype = new MQMapCommand();
MQBestFit.prototype.constructor = MQBestFit;
/**
 * Constructs a new MQBestFit object.
 * @class Calculates the best scale and center point of a map for the given
 * set of features in the Session. For example, if you place 20
 * points in a FeatureCollection object in your Session, adding a
 * BestFit object to the Session adjusts the MapState object so that
 * the map optimally displays all 20 points.
 * @extends MQMapCommand
 * @see MQDTCollection
 */
function MQBestFit () {
   MQMapCommand.call(this);
   this.setM_Xpath("BestFit");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getBESTFIT()));
   /**
    * Value to represent DT Collection
    * @type MQDTCollection
    */
   var m_DisplayTypes = new MQDTCollection();
   m_DisplayTypes.setM_Xpath("DisplayTypes");
   /**
    * Returns the m_DisplayTypes collection.
    * @return The m_DisplayTypes collection.
    * @type MQDTCollection
    *
    */
   this.getDisplayTypes = function(){
      return m_DisplayTypes;
   };
   /**
    * Sets the m_DisplayTypes collection.
    * @param {MQDTCollection} col The collection to set m_DisplayTypes to.
    * @type void
    *
    */
   this.setDisplayTypes = function(col){
      m_DisplayTypes.removeAll();
      m_DisplayTypes.append(col);
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQBestFit.prototype.getClassName = function(){
      return "MQBestFit";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQBestFit.prototype.getObjectVersion = function(){
      return 2;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQBestFit.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getDisplayTypes();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DisplayTypes")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DisplayTypes")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQBestFit.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getDisplayTypes().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "DisplayTypes"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the IncludePrimitives flag.
    * @param {Boolean} bFlag The IncludePrimitives flag
    * @type void
    */
   MQBestFit.prototype.setIncludePrimitives = function(bFlag){
      this.setProperty("IncludePrimitives", (bFlag===true)?1:0);
   };
   /**
    * Gets the IncludePrimitives flag
    * @return true if a IncludePrimitives = 1, false otherwise.
    * @type boolean
    */
   MQBestFit.prototype.getIncludePrimitives = function(){
      return (this.getProperty("IncludePrimitives")==1)? true : false;
   };
   /**
    * Sets the KeepCenter flag.
    * @param {Boolean} bFlag The KeepCenter flag
    * @type void
    */
   MQBestFit.prototype.setKeepCenter = function(bFlag){
      this.setProperty("KeepCenter", (bFlag===true)?1:0);
   };
   /**
    * Gets the KeepCenter flag
    * @return true if a KeepCenter = 1, false otherwise.
    * @type boolean
    */
   MQBestFit.prototype.getKeepCenter = function(){
      return (this.getProperty("KeepCenter")==1)? true : false;
   };
   /**
    * Sets the SnapToZoomLevel flag.
    * @param {Boolean} bFlag The SnapToZoomLevel flag
    * @type void
    */
   MQBestFit.prototype.setSnapToZoomLevel = function(bFlag){
      this.setProperty("SnapToZoomLevel", (bFlag===true)?1:0);
   };
   /**
    * Gets the SnapToZoomLevel flag
    * @return true if a SnapToZoomLevel = 1, false otherwise.
    * @type boolean
    */
   MQBestFit.prototype.getSnapToZoomLevel = function(){
      return (this.getProperty("SnapToZoomLevel")==1)? true : false;
   };
   /**
    * Sets the ScaleAdjustmentFactor of this object.
    * @param {Double} val The new ScaleAdjustmentFactor of this object.
    * @type void
    */
   MQBestFit.prototype.setScaleAdjustmentFactor = function(val){
      this.setProperty("ScaleAdjFactor",val);
   };
   /**
    * Returns the ScaleAdjustmentFactor of this object.
    * @return The new ScaleAdjustmentFactor of this object.
    * @type Double
    */
   MQBestFit.prototype.getScaleAdjustmentFactor = function(){
      return this.getProperty("ScaleAdjFactor");
   }
// End class MQBestFit

// Begin Class MQBestFitLL
/* Inheirit from MQMapCommand */
MQBestFitLL.prototype = new MQMapCommand();
MQBestFitLL.prototype.constructor = MQBestFitLL;
/**
 * Constructs a new MQBestFitLL object.
 * @class Calculates the best scale and center point of a map for the given
 * collection Lat/Lngs.  Adding a BestFitLL object to the Session
 * adjusts the MapState object so that the map optimally displays all
 * Lat/Lngs in the LatLngCollection set in the BestFitLL object.
 * @extends MQMapCommand
 * @see MQLatLngCollection
 */
function MQBestFitLL () {
   MQMapCommand.call(this);
   this.setM_Xpath("BestFitLL");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getBESTFITLL()));
   /**
    * Value to represent LatLng Collection
    * @type MQLatLngCollection
    */
   var m_LatLng = new MQLatLngCollection();
   m_LatLng.setM_Xpath("LatLngs");
   /**
    * Returns the m_LatLng collection.
    * @return The m_LatLng collection.
    * @type MQLatLngCollection
    *
    */
   this.getLatLngs = function(){
      return m_LatLng;
   };
   /**
    * Sets the m_LatLng collection.
    * @param {MQLatLngCollection} col The collection to set m_LatLng to.
    * @type void
    *
    */
   this.setLatLngs = function(col){
      m_LatLng.removeAll();
      m_LatLng.append(col);
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQBestFitLL.prototype.getClassName = function(){
      return "MQBestFitLL";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQBestFitLL.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQBestFitLL.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getLatLngs();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LatLngs")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQBestFitLL.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getLatLngs().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LatLngs"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the KeepCenter flag.
    * @param {Boolean} bFlag The KeepCenter flag
    * @type void
    */
   MQBestFitLL.prototype.setKeepCenter = function(bFlag){
      this.setProperty("KeepCenter", (bFlag===true)?1:0);
   };
   /**
    * Gets the KeepCenter flag
    * @return true if a KeepCenter = 1, false otherwise.
    * @type boolean
    */
   MQBestFitLL.prototype.getKeepCenter = function(){
      return (this.getProperty("KeepCenter")==1)? true : false;
   };
   /**
    * Sets the SnapToZoomLevel flag.
    * @param {Boolean} bFlag The SnapToZoomLevel flag
    * @type void
    */
   MQBestFitLL.prototype.setSnapToZoomLevel = function(bFlag){
      this.setProperty("SnapToZoomLevel", (bFlag===true)?1:0);
   };
   /**
    * Gets the SnapToZoomLevel flag
    * @return true if a SnapToZoomLevel = 1, false otherwise.
    * @type boolean
    */
   MQBestFitLL.prototype.getSnapToZoomLevel = function(){
      return (this.getProperty("SnapToZoomLevel")==1)? true : false;
   };
   /**
    * Sets the ScaleAdjustmentFactor of this object.
    * @param {Double} val The new ScaleAdjustmentFactor of this object.
    * @type void
    */
   MQBestFitLL.prototype.setScaleAdjustmentFactor = function(val){
      this.setProperty("ScaleAdjFactor",val);
   };
   /**
    * Returns the ScaleAdjustmentFactor of this object.
    * @return The new ScaleAdjustmentFactor of this object.
    * @type Double
    */
   MQBestFitLL.prototype.getScaleAdjustmentFactor = function(){
      return this.getProperty("ScaleAdjFactor");
   }
// End class MQBestFitLL

// Begin Class MQCenter
/* Inheirit from MQMapCommand */
MQCenter.prototype = new MQMapCommand();
MQCenter.prototype.constructor = MQCenter;
/**
 * Constructs a new MQCenter object.
 * @class Centers a map based on a mouse click. The new center point is
 * calculated in pixels and stored in the X and Y coordinates of this
 * object.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQCenter () {
   MQMapCommand.call(this);
   this.setM_Xpath("Center");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getCENTER()));
   /**
    * Value to represent center of the map
    * @type MQPoint
    */
   var m_Center = new MQPoint("CenterPoint");
   /**
    * Returns the m_Center.
    * @return The m_Center.
    * @type MQPoint
    *
    */
   this.getCenter = function(){
      return m_Center;
   };
   /**
    * Sets the m_Center.
    * @param {MQPoint} pt The point to set m_Center to.
    * @type void
    *
    */
   this.setCenter = function(pt){
      if (mqIsClass(m_Center.getClassName(), pt, false)){
         m_Center = pt.internalCopy(m_Center);
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQCenter.prototype.getClassName = function(){
      return "MQCenter";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQCenter.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQCenter.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getCenter();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQCenter.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQCenter

// Begin Class MQCenterLatLng
/* Inheirit from MQMapCommand */
MQCenterLatLng.prototype = new MQMapCommand();
MQCenterLatLng.prototype.constructor = MQCenterLatLng;
/**
 * Constructs a new MQCenterLatLng object.
 * @class Stores data to center the maps on a given point specified with
 * a latitude/longitude point.
 * @extends MQMapCommand
 * @see  MQLatLng
 */
function MQCenterLatLng () {
   MQMapCommand.call(this);
   this.setM_Xpath("CenterLatLng");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getCENTERLATLNG()));
   /**
    * Value to represent center of the map
    * @type MQLatLng
    */
   var m_Center = new MQLatLng("CenterLatLng");
   /**
    * Returns the m_Center.
    * @return The m_Center.
    * @type MQLatLng
    *
    */
   this.getCenter = function(){
      return m_Center;
   };
   /**
    * Sets the m_Center.
    * @param {MQLatLng} ll The LatLng to set m_Center to.
    * @type void
    *
    */
   this.setCenter = function(ll){
      if (mqIsClass(m_Center.getClassName(), ll, false)){
         m_Center = ll.internalCopy(m_Center);
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQCenterLatLng.prototype.getClassName = function(){
      return "MQCenterLatLng";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQCenterLatLng.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQCenterLatLng.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getCenter();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterLatLng")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQCenterLatLng.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterLatLng"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQCenterLatLng

// Begin Class MQPan
/* Inheirit from MQMapCommand */
MQPan.prototype = new MQMapCommand();
MQPan.prototype.constructor = MQPan;
/**
 * Constructs a new MQPan object.
 * @class Stores data to center the map on a given point without changing
 * its scale. Setting the X,Y coordinates moves the map viewport that number of
 * pixels left or right and up or down.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQPan () {
   MQMapCommand.call(this);
   this.setM_Xpath("Pan");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPAN()));
   /**
    * Value to represent center of the map
    * @type MQPoint
    */
   var m_Point = new MQPoint("DeltaPoint");
   /**
    * Returns the m_Point.
    * @return The m_Point.
    * @type MQPoint
    *
    */
   this.getPoint = function(){
      return m_Point;
   };
   /**
    * Sets the Delta X and Delta Y variables, which are the number of
    * pixels that the map viewport is to be moved in the horizontal and
    * vertical direction respectively.
    * @param {Double} dblX The number of pixels that the map viewport is to be moved in
    * the horizontal direction.  Negative indicates movement to the left,
    * positive to the right.
    * @param {Double} dblY The number of pixels that the map viewport is to be moved in
    * the vertical direction.  Negative indicates movement upward, positive downward.
    * @type void
    */
   this.setDeltaXY = function(dblX, dblY){
      m_Point.setXY(dblX, dblY);
   };
   /**
    * Sets the Delta Y variable, which is the number of pixels that the
    * map viewport is to be moved in the vertical direction.
    * @param {Double} dblY  The number of pixels that the map viewport is to be moved in
    * the vertical direction.  Negative indicates movement upward,
    * positive downward.
    * @type void
    */
   this.setDeltaY = function(dblY){
      m_Point.setY(dblY);
   };
   /**
    * Sets the Delta X variable, which is the number of pixels that the
    * map viewport is to be moved in the horizontal direction.
    * @param {Double} dblX The number of pixels that the map viewport is to be moved in
    * the horizontal direction.  Negative indicates movement to the
    * left, positive to the right.
    * @type void
    */
   this.setDeltaX = function(dblX){
      m_Point.setX(dblX);
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPan.prototype.getClassName = function(){
      return "MQPan";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPan.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPan.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getPoint();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DeltaPoint")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DeltaPoint")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPan.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getPoint().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "DeltaPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQPan

// Begin Class MQZoomIn
/* Inheirit from MQMapCommand */
MQZoomIn.prototype = new MQMapCommand();
MQZoomIn.prototype.constructor = MQZoomIn;
/**
 * Constructs a new MQZoomIn object.
 * @class Stores data to zoom in one level and center the map on a given
 * point. Setting the X,Y coordinates to the invalid Point results in a zoom
 * without changing the Center point.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQZoomIn () {
   MQMapCommand.call(this);
   this.setM_Xpath("ZoomIn");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getZOOMIN()));
   /**
    * Value to represent center of the map
    * @type MQPoint
    */
   var m_Center = new MQPoint("CenterPoint");
   /**
    * Returns the m_Center.
    * @return The m_Center.
    * @type MQPoint
    *
    */
   this.getCenter = function(){
      return m_Center;
   };
   /**
    * Sets the m_Center.
    * @param {MQPoint} pt The point to set m_Center to.
    * @type void
    *
    */
   this.setCenter = function(pt){
      m_Center = pt;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQZoomIn.prototype.getClassName = function(){
      return "MQZoomIn";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQZoomIn.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQZoomIn.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getCenter();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQZoomIn.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQZoomIn

// Begin Class MQZoomOut
/* Inheirit from MQMapCommand */
MQZoomOut.prototype = new MQMapCommand();
MQZoomOut.prototype.constructor = MQZoomOut;
/**
 * Constructs a new MQZoomOut object.
 * @class Stores data to zoom out one level and center the map on a given
 * point. Setting the X,Y coordinates to the invalid Point results in a zoom
 * without changing the Center point.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQZoomOut () {
   MQMapCommand.call(this);
   this.setM_Xpath("ZoomOut");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getZOOMOUT()));
   /**
    * Value to represent center of the map
    * @type MQPoint
    */
   var m_Center = new MQPoint("CenterPoint");
   /**
    * Returns the m_Center.
    * @return The m_Center.
    * @type MQPoint
    *
    */
   this.getCenter = function(){
      return m_Center;
   };
   /**
    * Sets the m_Center.
    * @param {MQPoint} pt The point to set m_Center to.
    * @type void
    *
    */
   this.setCenter = function(pt){
      m_Center = pt;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQZoomOut.prototype.getClassName = function(){
      return "MQZoomOut";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQZoomOut.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQZoomOut.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getCenter();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQZoomOut.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQZoomOut

// Begin Class MQZoomTo
/* Inheirit from MQMapCommand */
MQZoomTo.prototype = new MQMapCommand();
MQZoomTo.prototype.constructor = MQZoomTo;
/**
 * Constructs a new MQZoomTo object.
 * @class Stores data to zoom out one level and center the map on a given
 * point. Setting the X,Y coordinates to the invalid Point results in a zoom
 * without changing the Center point.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQZoomTo () {
   MQMapCommand.call(this);
   this.setM_Xpath("ZoomTo");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getZOOMTO()));
   /**
    * Value to represent center of the map
    * @type MQPoint
    */
   var m_Center = new MQPoint("CenterPoint");
   /**
    * Returns the m_Center.
    * @return The m_Center.
    * @type MQPoint
    */
   this.getCenter = function(){
      return m_Center;
   };
   /**
    * Sets the m_Center.
    * @param {MQPoint} pt The point to set m_Center to.
    * @type void
    */
   this.setCenter = function(pt){
      m_Center = pt;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQZoomTo.prototype.getClassName = function(){
      return "MQZoomTo";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQZoomTo.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    */
   MQZoomTo.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var obj = this.getCenter();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")!==null)
         obj.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/CenterPoint")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    */
   MQZoomTo.prototype.saveXml = function () {
      var newNode = MQA.createXMLDoc(this.getCenter().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "CenterPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Get zoom level index from the AutoMapCovswitch object ZoomLevels property.
    * Has no affect when not using map data selector.
    * @return The zoom level index.
    * @type Long
    */
   MQZoomTo.prototype.getZoomLevel = function () {
      return this.getProperty("ZoomLevel");
   };
   /**
    * Set zoom level index from the AutoMapCovswitch object ZoomLevels property.
    * Has no affect when not using map data selector.
    * @param {Long} val The zoom level index.
    * @type void
    */
   MQZoomTo.prototype.setZoomLevel = function (val) {
      this.setProperty("ZoomLevel", val);
   };
// End class MQZoomTo

// Begin Class MQZoomToRect
/* Inheirit from MQMapCommand */
MQZoomToRect.prototype = new MQMapCommand();
MQZoomToRect.prototype.constructor = MQZoomToRect;
/**
 * Constructs a new MQZoomToRect object.
 * @class Stores data to zoom to a rectangle specified with pixel
 * coordinates.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQZoomToRect () {
   MQMapCommand.call(this);
   this.setM_Xpath("ZoomToRect");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getZOOMTORECT()));
   /**
    * Value to represent upper left of the map
    * @type MQPoint
    */
   var m_UpperLeft = new MQPoint("UpperLeftPoint");
   /**
    * Value to represent lower right of the map
    * @type MQPoint
    */
   var m_LowerRight = new MQPoint("LowerRightPoint");
    /**
     * Returns the upper left and lower right corners of this rectangle.
     * @param {MQPoint} ulpt The upper left corner of this rectangular zoom area.
     * @param {MQPoint} lrpt The lower right corner of this rectangular zoom area.
     * @void
     */
   this.getRect = function(ulpt, lrpt){
      ulpt.loadXml(m_UpperLeft.copy().saveXml());
      lrpt.loadXml(m_LowerRight.copy().saveXml());
   };
   /**
    * Sets the upper left and lower right corners of the rectangle that
    * is to be zoomed to.
    * @param {MQPoint} ulpt The X,Y coordinates of the upper left corner of
    * this rectangular zoom area.
    * @param {MQPoint} lrpt The X,Y coordinates of the lower right corner
    * of this rectangular zoom area.
    * @type void
    *
    */
   this.setRect = function(ulpt, lrpt){
      if (mqIsClass(m_UpperLeft.getClassName(), ulpt, false) && mqIsClass(m_LowerRight.getClassName(), lrpt, false)){
         m_UpperLeft = ulpt.internalCopy(m_UpperLeft);
         m_LowerRight= lrpt.internalCopy(m_LowerRight);
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQZoomToRect.prototype.getClassName = function(){
      return "MQZoomToRect";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQZoomToRect.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQZoomToRect.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var ul = new MQPoint("UpperLeftPoint"), lr = new MQPoint("LowerRightPoint");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")!==null)
         ul.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftPoint")));
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightPoint")!==null)
         lr.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightPoint")));
      this.setRect(ul, lr);
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQZoomToRect.prototype.saveXml = function () {
      var ul = new MQPoint("UpperLeftPoint"), lr = new MQPoint("LowerRightPoint");
      this.getRect(ul, lr);
      var newNode = MQA.createXMLDoc(ul.saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftPoint"));
      newNode = MQA.createXMLDoc(lr.saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LowerRightPoint"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQZoomToRect

// Begin Class MQZoomToRectLatLng
/* Inheirit from MQMapCommand */
MQZoomToRectLatLng.prototype = new MQMapCommand();
MQZoomToRectLatLng.prototype.constructor = MQZoomToRectLatLng;
/**
 * Constructs a new MQZoomToRectLatLng object.
 * @class Stores data to zoom to a rectangle specified with
 * latitude/longitude points.
 * @extends MQMapCommand
 * @see  MQPoint
 */
function MQZoomToRectLatLng () {
   MQMapCommand.call(this);
   this.setM_Xpath("ZoomToRectLatLng");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getZOOMTORECTLATLNG()));
   /**
    * Value to represent upper left of the map
    * @type MQLatLng
    */
   var m_UpperLeft = new MQLatLng("UpperLeftLatLng");
   /**
    * Value to represent lower right of the map
    * @type MQLatLng
    */
   var m_LowerRight = new MQLatLng("LowerRightLatLng");
    /**
     * Returns the upper left and lower right corners of this rectangle.
     * @param {MQLatLng} ulll The upper left corner of this rectangular zoom area.
     * @param {MQLatLng} lrll The lower right corner of this rectangular zoom area.
     * @void
     */
   this.getRect = function(ulll, lrll){
      ulll.loadXml(m_UpperLeft.copy().saveXml());
      lrll.loadXml(m_LowerRight.copy().saveXml());
   };
   /**
    * Sets the upper left and lower right corners of the rectangle that
    * is to be zoomed to.
    * @param {MQLatLng} ulll The X,Y coordinates of the upper left corner of
    * this rectangular zoom area.
    * @param {MQLatLng} lrll The X,Y coordinates of the lower right corner
    * of this rectangular zoom area.
    * @type void
    *
    */
   this.setRect = function(ulll, lrll){
      if (mqIsClass(m_UpperLeft.getClassName(), ulll, false) && mqIsClass(m_LowerRight.getClassName(), lrll, false)){
         m_UpperLeft = ulll.internalCopy(m_UpperLeft);
         m_LowerRight= lrll.internalCopy(m_LowerRight);
      }
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQZoomToRectLatLng.prototype.getClassName = function(){
      return "MQZoomToRectLatLng";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQZoomToRectLatLng.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQZoomToRectLatLng.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var ul = new MQLatLng("UpperLeftLatLng"), lr = new MQLatLng("LowerRightLatLng");
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")!==null)
         ul.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/UpperLeftLatLng")));
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightLatLng")!==null)
         lr.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/LowerRightLatLng")));
      this.setRect(ul, lr);
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQZoomToRectLatLng.prototype.saveXml = function () {
      var ul = new MQLatLng("UpperLeftLatLng"), lr = new MQLatLng("LowerRightLatLng");
      this.getRect(ul, lr);
      var newNode = MQA.createXMLDoc(ul.saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "UpperLeftLatLng"));
      newNode = MQA.createXMLDoc(lr.saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "LowerRightLatLng"));
      return mqXmlToStr(this.getM_XmlDoc());
   };
// End class MQZoomToRectLatLng

// Begin class MQType
/**
 * Constructs a new MQType object.
 * @class Base Type to encapsulate any common functions of the enum type pattern objects.
 */
function MQType(){};
   /**
    * Checks the equality of the values of two MQType objects.
    * @return True if values are equal and false otherwise.
    * @type Boolean
    */
    MQType.prototype.equals = function(type){
      if(type){
         try{
            var className = type.getClassName();
         } catch (error){
            alert("Invalid type for this function!");
            throw "Invalid type for this function!";
         }
         if(className=== this.getClassName()){
            return (this.intValue() === type.intValue());
         } else {
            alert("Invalid type for this function!");
            throw "Invalid type for this function!";
         }
      } else {
         alert("An MQType parameter must be provided for this function!");
         throw "An MQType parameter must be provided for this function!";
      }
   };
// End class MQType

// Begin class MQRouteType
/* Inheirit from MQType */
MQRouteType.prototype = new MQType();
MQRouteType.prototype.constructor = MQRouteType;
/**
 * Constructs a new MQRouteType object.
 * @class Constants to specify the type of route wanted.
 * @extends MQType
 */
function MQRouteType(val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQROUTETYPE_FASTEST:             value = val; break;
      case MQCONSTANT.MQROUTETYPE_SHORTEST:          value = val; break;
      case MQCONSTANT.MQROUTETYPE_PEDESTRIAN:          value = val; break;
      case MQCONSTANT.MQROUTETYPE_OPTIMIZED:            value = val; break;
      case MQCONSTANT.MQROUTETYPE_SELECT_DATASET_ONLY:    value = val; break;
      default:
         alert(val + " is an invalid value for MQRouteType!");
         throw val + " invalid value for MQRouteType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteType.prototype.getClassName = function(){
      return "MQRouteType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQRouteType

// Begin class MQNarrativeType
/* Inheirit from MQType */
MQNarrativeType.prototype = new MQType();
MQNarrativeType.prototype.constructor = MQNarrativeType;
/**
 * Constructs a new MQNarrativeType object.
 * @class Constants to specify the type of route wanted.
 * @extends MQType
 */
function MQNarrativeType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    */
   var value = -2;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQNARRATIVETYPE_DEFAULT:   value = val; break;
      case MQCONSTANT.MQNARRATIVETYPE_HTML:    value = val; break;
      case MQCONSTANT.MQNARRATIVETYPE_NONE:    value = val; break;
      default:
         alert(val + " is an invalid value for MQNarrativeType!");
         throw val + " invalid value for MQNarrativeType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQNarrativeType.prototype.getClassName = function(){
      return "MQNarrativeType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQNarrativeType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQNarrativeType

// Begin class MQCoordinateType
/* Inheirit from MQType */
MQCoordinateType.prototype = new MQType();
MQCoordinateType.prototype.constructor = MQCoordinateType;
/**
 * Constructs a new MQCoordinateType object.
 * @class Constants to specify the type of coordinate the primitives should use.
 * @extends MQType
 */
function MQCoordinateType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -2;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQCOORDINATETYPE_DISPLAY:    value = val; break;
      case MQCONSTANT.MQCOORDINATETYPE_GEOGRAPHIC: value = val; break;
      default:
         alert(val + " is an invalid value for MQCoordinateType!");
         throw val + " invalid value for MQCoordinateType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQCoordinateType.prototype.getClassName = function(){
      return "MQCoordinateType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQCoordinateType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQCoordinateType

// Begin class MQFeatureSpeciferAttributeType
/* Inheirit from MQType */
MQFeatureSpeciferAttributeType.prototype = new MQType();
MQFeatureSpeciferAttributeType.prototype.constructor = MQFeatureSpeciferAttributeType;
/**
 * Constructs a new MQFeatureSpeciferAttributeType object.
 * @class Constants for the attribute type of a FeatureSpecifier object.
 * Used to select the attribute of a feature you want to match.
 * @extends MQType
 */
function MQFeatureSpeciferAttributeType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQFEATURESPECIFERATTRIBUTETYPE_GEFID:    value = val; break;
      case MQCONSTANT.MQFEATURESPECIFERATTRIBUTETYPE_NAME:    value = val; break;
      default:
         alert(val + " is an invalid value for MQFeatureSpeciferAttributeType!");
         throw val + " invalid value for MQFeatureSpeciferAttributeType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQFeatureSpeciferAttributeType.prototype.getClassName = function(){
      return "MQFeatureSpeciferAttributeType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFeatureSpeciferAttributeType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQFeatureSpeciferAttributeType

// Begin class MQSymbolType
/* Inheirit from MQType */
MQSymbolType.prototype = new MQType();
MQSymbolType.prototype.constructor = MQSymbolType;
/**
 * Constructs a new MQSymbolType object.
 * @class Constants for the symbol type in a DTStyle.
 * @extends MQType
 */
function MQSymbolType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQSYMBOLTYPE_RASTER:    value = val; break;
      case MQCONSTANT.MQSYMBOLTYPE_VECTOR:    value = val; break;
      default:
         alert(val + " is an invalid value for MQSymbolType!");
         throw val + " invalid value for MQSymbolType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSymbolType.prototype.getClassName = function(){
      return "MQSymbolType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSymbolType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQSymbolType

// Begin class MQMatchType
/* Inheirit from MQType */
MQMatchType.prototype = new MQType();
MQMatchType.prototype.constructor = MQMatchType;
/**
 * Constructs a new MQMatchType object.
 * @class Constants for the symbol type in a DTStyle.
 * @extends MQType
 */
function MQMatchType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQMATCHTYPE_LOC:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_INTR:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_NEARBLK:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_REPBLK:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_BLOCK:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA1:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA2:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA3:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA4:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA5:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA6:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_AA7:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_PC1:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_PC2:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_PC3:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_PC4:    value = val; break;
      case MQCONSTANT.MQMATCHTYPE_POI:    value = val; break;
      default:
         alert(val + " is an invalid value for MQMatchType!");
         throw val + " invalid value for MQMatchType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQMatchType.prototype.getClassName = function(){
      return "MQMatchType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQMatchType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQMatchType

// Begin class MQQualityType
/* Inheirit from MQType */
MQQualityType.prototype = new MQType();
MQQualityType.prototype.constructor = MQQualityType;
/**
 * Constructs a new MQQualityType object.
 * @class Constants for the symbol type in a DTStyle.
 * @extends MQType
 */
function MQQualityType (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQQUALITYTYPE_EXACT:    value = val; break;
      case MQCONSTANT.MQQUALITYTYPE_GOOD:    value = val; break;
      case MQCONSTANT.MQQUALITYTYPE_APPROX:    value = val; break;
      default:
         alert(val + " is an invalid value for MQQualityType!");
         throw val + " invalid value for MQQualityType!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQQualityType.prototype.getClassName = function(){
      return "MQQualityType";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQQualityType.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQQualityType

// Begin class MQDrawTrigger
/* Inheirit from MQType */
MQDrawTrigger.prototype = new MQType();
MQDrawTrigger.prototype.constructor = MQDrawTrigger;
/**
 * Constructs a new MQDrawTrigger object.
 * @class Constants for trigger Draw Triggers
 * @extends MQType
 */
function MQDrawTrigger (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQDRAWTRIGGER_AFTER_POLYGONS:    value = val; break;
      case MQCONSTANT.MQDRAWTRIGGER_AFTER_ROUTE_HIGHLIGHT: value = val; break;
      case MQCONSTANT.MQDRAWTRIGGER_AFTER_TEXT: value = val; break;
      case MQCONSTANT.MQDRAWTRIGGER_BEFORE_POLYGONS: value = val; break;
      case MQCONSTANT.MQDRAWTRIGGER_BEFORE_ROUTE_HIGHLIGHT: value = val; break;
      case MQCONSTANT.MQDRAWTRIGGER_BEFORE_TEXT: value = val; break;
      default:
         alert(val + " is an invalid value for MQDrawTrigger!");
         throw val + " invalid value for MQDrawTrigger!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDrawTrigger.prototype.getClassName = function(){
      return "MQDrawTrigger";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDrawTrigger.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQDrawTrigger

// Begin class MQPenStyle
/* Inheirit from MQType */
MQPenStyle.prototype = new MQType();
MQPenStyle.prototype.constructor = MQPenStyle;
/**
 * Constructs a new MQPenStyle object.
 * @class Pen styles for Rectangle and Line primitives.
 * @extends MQType
 */
function MQPenStyle (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQPENSTYLE_DASH:    value = val; break;
      case MQCONSTANT.MQPENSTYLE_DASH_DOT: value = val; break;
      case MQCONSTANT.MQPENSTYLE_DASH_DOT_DOT:    value = val; break;
      case MQCONSTANT.MQPENSTYLE_SOLID: value = val; break;
      case MQCONSTANT.MQPENSTYLE_DOT:    value = val; break;
      case MQCONSTANT.MQPENSTYLE_NONE: value = val; break;
      default:
         alert(val + " is an invalid value for MQPenStyle!");
         throw val + " invalid value for MQPenStyle!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQPenStyle.prototype.getClassName = function(){
      return "MQPenStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPenStyle.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQPenStyle

// Begin class MQFontStyle
/* Inheirit from MQType */
MQFontStyle.prototype = new MQType();
MQFontStyle.prototype.constructor = MQFontStyle;
/**
 * Constructs a new MQFontStyle object.
 * @class Constants for the font style in a DTStyle. When using DTStyles
 * you may reset the font style back to the its default style from
 * the style file state by specifying mqFontInvalid.
 * @extends MQType
 */
function MQFontStyle (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -2;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQFONTSTYLE_BOLD:    value = val; break;
      case MQCONSTANT.MQFONTSTYLE_BOXED: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_DOT:    value = val; break;
      case MQCONSTANT.MQFONTSTYLE_ITALICS: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_MAX_VALUE:    value = val; break;
      case MQCONSTANT.MQFONTSTYLE_NORMAL: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_OUTLINED: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_SEMIBOLD: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_STRIKEOUT: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_THIN: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_UNDERLINE: value = val; break;
      case MQCONSTANT.MQFONTSTYLE_INVALID: value = val; break;
      default:
         alert(val + " is an invalid value for MQFontStyle!");
         throw val + " invalid value for MQFontStyle!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQFontStyle.prototype.getClassName = function(){
      return "MQFontStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFontStyle.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQFontStyle

// Begin class MQColorStyle
/* Inheirit from MQType */
MQColorStyle.prototype = new MQType();
MQColorStyle.prototype.constructor = MQColorStyle;
/**
 * Constructs a new MQColorStyle object.
 * @class Pen styles for Rectangle and Line primitives.
 * @extends MQType
 */
function MQColorStyle (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = MQCONSTANT.MQCOLORSTYLE_INVALID;
   if(val!==null){
      value = val;
   }
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQColorStyle.prototype.getClassName = function(){
      return "MQColorStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQColorStyle.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Returns the RGB value representing the color in the default sRGB
    * ColorModel. (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are
    * blue). Alpha is defaulted to 255.
    * @return the RGB value representing this object.
    * @type int
    */
   MQColorStyle.prototype.getRGB = function(){
      var rgb = -16777216;
      var a = parseInt(parseInt(this.intValue() / 65536) % 256);
      var b = parseInt(parseInt(parseInt(this.intValue() / 256) % 256) * 256);
      var c = parseInt(parseInt(this.intValue() % 256) * 65536);
      return rgb + a + b + c;
   }
// End class MQColorStyle

// Begin class MQFillStyle
/* Inheirit from MQType */
MQFillStyle.prototype = new MQType();
MQFillStyle.prototype.constructor = MQFillStyle;
/**
 * Constructs a new MQFillStyle object.
 * @class Pen styles for Rectangle and Line primitives.
 * @extends MQType
 */
function MQFillStyle (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -1;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQFILLSTYLE_SOLID:    value = val; break;
      case MQCONSTANT.MQFILLSTYLE_BDIAGONAL: value = val; break;
      case MQCONSTANT.MQFILLSTYLE_CROSS:    value = val; break;
      case MQCONSTANT.MQFILLSTYLE_DIAG_CROSS: value = val; break;
      case MQCONSTANT.MQFILLSTYLE_FDIAGONAL:    value = val; break;
      case MQCONSTANT.MQFILLSTYLE_HORIZONTAL: value = val; break;
      case MQCONSTANT.MQFILLSTYLE_VERTICAL:    value = val; break;
      case MQCONSTANT.MQFILLSTYLE_NONE: value = val; break;
      default:
         alert(val + " is an invalid value for MQFillStyle!");
         throw val + " invalid value for MQFillStyle!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQFillStyle.prototype.getClassName = function(){
      return "MQFillStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFillStyle.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQFillStyle

// Begin class MQDistanceUnits
/* Inheirit from MQType */
MQDistanceUnits.prototype = new MQType();
MQDistanceUnits.prototype.constructor = MQDistanceUnits;
 /**
 * Constructs a new MQDistanceUnits object.
 * @class Constants to specify the type of units to use when calculating distance.
 * @param {int} val Used to determine a miles or kilometers unit. Miles is the
 * default type if nothing is handed into the constructor.
 * @see MQGeoAddress
 * @extends MQType
 */
function MQDistanceUnits (val) {

   // Default value is miles
   /**
    * Value to represent the type of units to use
    * @type int
    */
   var value = 0;
   val = val || 0;
   switch (val)
   {
      case MQCONSTANT.MQDISTANCEUNITS_MILES:      value = val; break;
      case MQCONSTANT.MQDISTANCEUNITS_KILOMETERS: value = val; break;
      default:
         alert(val + " is an invalid value for MQDistanceUnits!");
         throw val + " invalid value for MQDistanceUnist!";
   };
   /**
     * Gets value.
     * @return The value value
     * @type int
     */
   this.getValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQDistanceUnits.prototype.getClassName = function(){
      return "MQDistanceUnits";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDistanceUnits.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQDistanceUnits

// Begin class MQRouteResultsCode
/* Inheirit from MQType */
MQRouteResultsCode.prototype = new MQType();
MQRouteResultsCode.prototype.constructor = MQRouteResultsCode;
/**
 * Constructs a new MQRouteResultsCode object.
 * @class Constants to specify the type of route result.
 * @extends MQType
 */
function MQRouteResultsCode (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -2;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQROUTERESULTSCODE_NOT_SPECIFIED:    value = val; break;
      case MQCONSTANT.MQROUTERESULTSCODE_SUCCESS:          value = val; break;
      case MQCONSTANT.MQROUTERESULTSCODE_INVALID_LOCATION: value = val; break;
      case MQCONSTANT.MQROUTERESULTSCODE_ROUTE_FAILURE:    value = val; break;
      case MQCONSTANT.MQROUTERESULTSCODE_NO_DATASET_FOUND: value = val; break;
      default:
         alert(val + " is an invalid value for MQRouteResultsCode!");
         throw val + " invalid value for MQRouteResultsCode!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteResultsCode.prototype.getClassName = function(){
      return "MQRouteResultsCode";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteResultsCode.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQRouteResultsCode

// Begin class MQRouteMatrixResultsCode
/* Inheirit from MQType */
MQRouteMatrixResultsCode.prototype = new MQType();
MQRouteMatrixResultsCode.prototype.constructor = MQRouteMatrixResultsCode;
/**
 * Constructs a new MQRouteMatrixResultsCode object.
 * @class Constants to specify the type of route result.
 * @extends MQType
 */
function MQRouteMatrixResultsCode (val) {
   /**
    * Value to represent the type and preset to a invalid value
    * @type int
    *
    */
   var value = -2;
   // Prevent values except constants that are allowed
   switch (val)
   {
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_NOT_SPECIFIED:          value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_SUCCESS:                value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_INVALID_LOCATION:       value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_ROUTE_FAILURE:          value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_NO_DATASET_FOUND:       value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_INVALID_OPTION:         value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_PARTIAL_SUCCESS:        value = val; break;
      case MQCONSTANT.MQROUTEMATRIXRESULTSCODE_EXCEEDED_MAX_LOCATIONS: value = val; break;
      default:
         alert(val + " is an invalid value for MQRouteMatrixResultsCode!");
         throw val + " invalid value for MQRouteMatrixResultsCode!";
   };
   /**
    * Gets the integer value of this type.
    * @return The integer of value of this type.
    * @type int
    */
   this.intValue = function(){
      return value;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQRouteMatrixResultsCode.prototype.getClassName = function(){
      return "MQRouteMatrixResultsCode";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQRouteMatrixResultsCode.prototype.getObjectVersion = function(){
      return 0;
   };
// End class MQRouteMatrixResultsCode

// Begin class MQLocationCollection
/* Inheirit from MQObjectCollection */
MQLocationCollection.prototype = new MQObjectCollection(32678);
MQLocationCollection.prototype.constructor = MQLocationCollection;
/**
  * Constructs a new MQLocationCollection object.
  * @class Contains a collection of Location objects, usually Geocode results.
  * The Geocode results are in the form of <code><b>MQGeoAddress</b></code>
  * objects.
  * @param {String} strMQLocationType The type of MQLocation descedent to check for
  * @extends MQObjectCollection
  * @see MQLocation
  * @see MQAddress
  * @see MQGeoAddress
  * @see MQSingleLineAddress
  */
function MQLocationCollection() {
   MQObjectCollection.call(this, 32678);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getLOCATIONCOLLECTION()));
   this.setM_Xpath("LocationCollection");
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQLocationCollection.prototype.getClassName = function(){
      return "MQLocationCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQLocationCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQLocationCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var loc = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="Address"){
               loc = new MQAddress();
               loc.loadXml(mqXmlToStr(nodes[count]));
            }else if(nodes[count].nodeName==="GeoAddress"){
               loc = new MQGeoAddress();
               loc.loadXml(mqXmlToStr(nodes[count]));
            }else if(nodes[count].nodeName==="SingleLineAddress"){
               loc = new MQSingleLineAddress();
               loc.loadXml(mqXmlToStr(nodes[count]));
            }
            this.add(loc);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQLocationCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
   /**
    * Is this object an MQGeoAddress or an MQAddress or an MQSingleLineAddress
    * @return True if valid, false otherwise
    * @type MQLocation
    *
    */
   MQLocationCollection.prototype.isValidObject = function(obj){
      if(obj){
         if(obj.getClassName()==="MQGeoAddress" || obj.getClassName()==="MQAddress" || obj.getClassName()==="MQSingleLineAddress"){
            return true;
         }
      }
      return false;
   };
// End class MQLocationCollection



// Begin class MQLocationCollectionCollection
/* Inheirit from MQObjectCollection */
MQLocationCollectionCollection.prototype = new MQObjectCollection(32678);
MQLocationCollectionCollection.prototype.constructor = MQLocationCollectionCollection;
/**
  * Constructs a new MQLocationCollectionCollection object.
  * @class Contains a collection of Location Collection objects, usually Batch Geocode results.
  * The Geocode results are in the form of <code><b>MQGeoAddress</b></code>
  * objects.
  * @extends MQObjectCollection
  * @see MQLocationCollection
  */
function MQLocationCollectionCollection() {
   MQObjectCollection.call(this, 32678);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getLOCATIONCOLLECTIONCOLLECTION()));
   this.setM_Xpath("LocationCollectionCollection");
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQLocationCollectionCollection.prototype.getClassName = function(){
      return "MQLocationCollectionCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQLocationCollectionCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQLocationCollectionCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var loc = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            loc = new MQLocationCollection();
            loc.loadXml(mqXmlToStr(nodes[count]));
            this.add(loc);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQLocationCollectionCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
   /**
    * Is this object an MQLocationCollection
    * @return True if valid, false otherwise
    * @type MQLocation
    *
    */
   MQLocationCollectionCollection.prototype.isValidObject = function(obj){
      if(obj){
         if(obj.getClassName()==="MQLocationCollection"){
            return true;
         }
      }
      return false;
   };
// End class MQLocationCollectionCollection



// Begin class MQSignCollection
/* Inheirit from MQObjectCollection */
MQSignCollection.prototype = new MQObjectCollection(32678);
MQSignCollection.prototype.constructor = MQSignCollection;
/**
  * Constructs a new MQSignCollection object.
  * @class A collection of Sign objects.
  * @param {String} itemXpath The xpath of the items in the collection
  * @extends MQObjectCollection
  * @see MQSign
  */
function MQSignCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setValidClassName("MQSign");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSIGNCOLLECTION()));
   this.setM_Xpath("SignCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQSignCollection.prototype.getClassName = function(){
      return "MQSignCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSignCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQSignCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var sign = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQSign"){
            for (var count=minCount;count < maxCount; count++){
               sign = new MQSign();
               sign.setM_Xpath(this.getM_itemXpath());
               sign.loadXml(mqXmlToStr(nodes[count]));
               this.add(sign);
            }
            }
      }
   };

   /**
    * Assigns the xml that relates to this object.
    * @param {Node} node of the xml document to be imported.
    * @type void
    * @private
    *
    */
   MQSignCollection.prototype.loadXmlFromNode = function (node) {

      var xmlDoc = mqCreateXMLDocImportNode(node);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var sign = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQSign"){
            for (var count=minCount;count < maxCount; count++){
               sign = new MQSign();
                sign.setM_Xpath(this.getM_itemXpath());
                sign.loadXmlFromNode(nodes[count]);
               this.add(sign);
            }
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQSignCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQSignCollection

// Begin class MQPointCollection
/* Inheirit from MQObjectCollection */
MQPointCollection.prototype = new MQObjectCollection(32678);
MQPointCollection.prototype.constructor = MQPointCollection;
/**
  * Constructs a new MQPointCollection object.
  * @class A collection of Point objects.
  * @param {String} itemXpath The xpath of the items in the collection
  * @extends MQObjectCollection
  * @see MQPoint
  */
function MQPointCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setValidClassName("MQPoint");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPOINTCOLLECTION()));
   this.setM_Xpath("PointCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQPointCollection.prototype.getClassName = function(){
      return "MQPointCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPointCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPointCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var pnt = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQPoint"){
            for (var count=minCount;count < maxCount; count++){
							var x;
							var y;
							if(nodes[count].firstChild!==null){
								 x = nodes[count].firstChild.nodeValue;
							}
							count++;
							if(nodes[count].firstChild!==null){
								 y = nodes[count].firstChild.nodeValue;
							}
               pnt = new MQPoint(x,y);
               this.add(pnt);
            }
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPointCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQPointCollection


// Begin class MQDBLayerQueryCollection
/* Inheirit from MQObjectCollection */
MQDBLayerQueryCollection.prototype = new MQObjectCollection(32678);
MQDBLayerQueryCollection.prototype.constructor = MQDBLayerQueryCollection;
/**
  * Constructs a new MQDBLayerQueryCollection object.
  * @class A collection of MQDBLayerQuery objects.
  * @param {String} itemXpath The xpath of the items in the collection
  * @extends MQObjectCollection
  * @see MQDBLayerQuery
  */
function MQDBLayerQueryCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setValidClassName("MQDBLayerQuery");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSIGNCOLLECTION()));
   this.setM_Xpath("DBLayerQueryCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQDBLayerQueryCollection.prototype.getClassName = function(){
      return "MQDBLayerQueryCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDBLayerQueryCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQDBLayerQueryCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var sign = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQDBLayerQuery"){
            for (var count=minCount;count < maxCount; count++){
               sign = new MQDBLayerQuery();
               sign.setM_Xpath(this.getM_itemXpath());
               sign.loadXml(mqXmlToStr(nodes[count]));
               this.add(sign);
            }
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQDBLayerQueryCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQDBLayerQueryCollection

// Begin class MQManeuverCollection
/* Inheirit from MQObjectCollection */
MQManeuverCollection.prototype = new MQObjectCollection(32678);
MQManeuverCollection.prototype.constructor = MQManeuverCollection;
/**
  * Constructs a new MQManeuverCollection object.
  * @class A collection of Maneuver objects which are used to annotate the
  * map.
  * @param {String} itemXpath The xpath of the items in the collection
  * @extends MQObjectCollection
  * @see MQManeuver
  */
function MQManeuverCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setValidClassName("MQManeuver");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getMANEUVERCOLLECTION()));
   this.setM_Xpath("ManeuverCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQManeuverCollection.prototype.getClassName = function(){
      return "MQManeuverCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQManeuverCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQManeuverCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var maneuver = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQManeuver"){
            for (var count=minCount;count < maxCount; count++){
               maneuver = new MQManeuver();
               maneuver.setM_Xpath(this.getM_itemXpath());
               maneuver.loadXml(mqXmlToStr(nodes[count]));
               this.add(maneuver);
            }
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQManeuverCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQManeuverCollection

// Begin class MQTrekRouteCollection
/* Inheirit from MQObjectCollection */
MQTrekRouteCollection.prototype = new MQObjectCollection(32678);
MQTrekRouteCollection.prototype.constructor = MQTrekRouteCollection;
/**
  * Constructs a new MQTrekRouteCollection object.
  * @class A collection of TrekRoute objects which are used to annotate the
  * map.
  * @param {String} itemXpath The xpath of the items in the collection
  * @extends MQObjectCollection
  * @see MQTrekRoute
  */
function MQTrekRouteCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setValidClassName("MQTrekRoute");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getTREKROUTECOLLECTION()));
   this.setM_Xpath("TrekRouteCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQTrekRouteCollection.prototype.getClassName = function(){
      return "MQTrekRouteCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQTrekRouteCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQTrekRouteCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var trek = null;
         // iterate through xml and create objects for collection
         if(this.getValidClassName()==="MQTrekRoute"){
            for (var count=minCount;count < maxCount; count++){
               trek = new MQTrekRoute();
               trek.setM_Xpath(this.getM_itemXpath());
               trek.loadXml(mqXmlToStr(nodes[count]));
               this.add(trek);
            }
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQTrekRouteCollection.prototype.saveXml = function () {
  	  var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQTrekRouteCollection

// Begin class MQIntCollection
/* Inheirit from MQObjectCollection */
MQIntCollection.prototype = new MQObjectCollection(32678);
MQIntCollection.prototype.constructor = MQIntCollection;
/**
  * Constructs a new MQIntCollection object.
  * @class Contains a collection of ints.
  * @param {String} itemXpath The xpath to the item
  * @extends MQObjectCollection
  */
function MQIntCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("int");
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   if(this.getClassName() === "MQIntCollection"){
      this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getINTCOLLECTION()));
       this.setM_Xpath("IntCollection");
   }
}
  /**
   * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQIntCollection.prototype.getClassName = function(){
      return "MQIntCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQIntCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml The xml to be assigned.
    * @type void
    *
    */
   MQIntCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var str = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
              if(nodes[count].firstChild!==null){
                str = parseInt(nodes[count].firstChild.nodeValue);
              }else {
                 str = 0;
              }
            this.add(str);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQIntCollection.prototype.saveXml = function () {
   	  var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = "<" + this.getM_itemXpath() + ">" + this.get(i) + "</" + this.getM_itemXpath() + ">";
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQIntCollection

// Begin class MQDTCollection
/* Inheirit from MQIntCollection */
MQDTCollection.prototype = new MQIntCollection("Item");
MQDTCollection.prototype.constructor = MQDTCollection;
/**
  * Constructs a new MQDTCollection object.
  * @class Contains a collection of display type codes.
  * @param {String} itemXpath The xpath to the item
  * @extends MQIntCollection
  */
function MQDTCollection(itemXpath) {
   MQIntCollection.call(this, itemXpath);
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getDTCOLLECTION()));
   this.setM_Xpath("DTCollection");
};
  /**
   * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQDTCollection.prototype.getClassName = function(){
      return "MQDTCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQDTCollection.prototype.getObjectVersion = function(){
      return 1;
   };
// End class MQDTCollection

// Begin class MQFeatureCollection
/* Inheirit from MQObjectCollection */
MQFeatureCollection.prototype = new MQObjectCollection(32678);
MQFeatureCollection.prototype.constructor = MQFeatureCollection;
/**
  * Constructs a new MQFeatureCollection object.
  * @class A collection of Feature objects.
  * @extends MQObjectCollection
  * @see MQLineFeature
  * @see MQPointFeature
  * @see MQPolygonFeature
  */
function MQFeatureCollection() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("ALL");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getFEATURECOLLECTION()));
   this.setM_Xpath("FeatureCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQFeatureCollection.prototype.getClassName = function(){
      return "MQFeatureCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFeatureCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQFeatureCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var feat = null;
         var nodeName = "";
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            nodeName = nodes[count].nodeName;
            if(nodeName==="LineFeature"){
               feat = new MQLineFeature();
               feat.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodeName==="PointFeature"){
               feat = new MQPointFeature();
               feat.loadXmlFromNode(nodes[count]);
            } else if(nodeName==="PolygonFeature"){
               feat = new MQPolygonFeature();
               feat.loadXml(mqXmlToStr(nodes[count]));
            }
            this.add(feat);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQFeatureCollection.prototype.saveXml = function () {
   	  var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Version=\"" + this.getObjectVersion() + "\" Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQFeatureCollection

// Begin class MQFeatureSpecifierCollection
/* Inheirit from MQObjectCollection */
MQFeatureSpecifierCollection.prototype = new MQObjectCollection(32678);
MQFeatureSpecifierCollection.prototype.constructor = MQFeatureSpecifierCollection;
/**
  * Constructs a new MQFeatureSpecifierCollection object.
  * @class A collection of MQFeatureSpecifier objects.
  * @extends MQObjectCollection
  * @see MQFeatureSpecifier
  */
function MQFeatureSpecifierCollection() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("MQFeatureSpecifier");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getFEATURESPECIFIERCOLLECTION()));
   this.setM_Xpath("FeatureCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQFeatureSpecifierCollection.prototype.getClassName = function(){
      return "MQFeatureSpecifierCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQFeatureSpecifierCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQFeatureSpecifierCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var feat = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="FeatureSpecifier"){
               feat = new MQFeatureSpecifier();
               feat.loadXml(mqXmlToStr(nodes[count]));
            }
            this.add(feat);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQFeatureSpecifierCollection.prototype.saveXml = function () {
	  var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQFeatureSpecifierCollection

// Begin class MQGeocodeOptionsCollection
/* Inheirit from MQObjectCollection */
MQGeocodeOptionsCollection.prototype = new MQObjectCollection(32678);
MQGeocodeOptionsCollection.prototype.constructor = MQGeocodeOptionsCollection;
/**
  * Constructs a new MQGeocodeOptionsCollection object.
  * @class A collection of MQGeocodeOptions objects.
  * @extends MQObjectCollection
  * @see MQGeocodeOptions
  */
function MQGeocodeOptionsCollection() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("MQGeocodeOptions");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getGEOCODEOPTIONSCOLLECTION()));
   this.setM_Xpath("GeocodeOptionsCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQGeocodeOptionsCollection.prototype.getClassName = function(){
      return "MQGeocodeOptionsCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQGeocodeOptionsCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQGeocodeOptionsCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var geoO = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="GeocodeOptions"){
               geoO = new MQGeocodeOptions();
               geoO.loadXml(mqXmlToStr(nodes[count]));
            }
            this.add(geoO);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQGeocodeOptionsCollection.prototype.saveXml = function () {
	  var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQGeocodeOptionsCollection

// Begin class MQCoverageStyle
/* Inheirit from MQObjectCollection */
MQCoverageStyle.prototype = new MQObjectCollection(32678);
MQCoverageStyle.prototype.constructor = MQCoverageStyle;
/**
  * Constructs a new MQCoverageStyle object.
  * @class A collection of DTStyle objects. DTStyle objects contain
  * information necessary to display various features on a map.
  * @extends MQObjectCollection
  * @see MQBaseDTStyle
  */
function MQCoverageStyle() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("ALL");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getCOVERAGESTYLE()));
   this.setM_Xpath("CoverageStyle");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQCoverageStyle.prototype.getClassName = function(){
      return "MQCoverageStyle";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQCoverageStyle.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQCoverageStyle.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var dt = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="DTStyle"){
               dt = new MQDTStyle();
               dt.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="DTStyleEx"){
               dt = new MQDTStyleEx();
               dt.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="DTFeatureStyleEx"){
               dt = new MQDTFeatureStyleEx();
               dt.loadXml(mqXmlToStr(nodes[count]));
            }
            if(dt!==null){
               this.add(dt);
            }
            dt = null;
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQCoverageStyle.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<CoverageStyle Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "<Name>" + this.getProperty("Name") + "</Name>";
      strRetArray[strRetArray.length] = "</CoverageStyle>";
      var strRet = strRetArray.join("");
      return strRet;
   };
   /**
    * Sets the name of the CoverageStyle.
    * @param {String} strName Name of CoverageStyle.
    * @type void
    */
   MQCoverageStyle.prototype.setName = function(strName){
      this.setProperty("Name",strName);
   };
   /**
    * Returns the name of the CoverageStyle.
    * @return The value of Name
    * @type String
    */
   MQCoverageStyle.prototype.getName = function(){
      return this.getProperty("Name");
   };
// End class MQCoverageStyle

// Begin class MQPrimitiveCollection
/* Inheirit from MQObjectCollection */
MQPrimitiveCollection.prototype = new MQObjectCollection(32678);
MQPrimitiveCollection.prototype.constructor = MQPrimitiveCollection;
/**
  * Constructs a new MQPrimitiveCollection object.
  * @class A collection of Primitive objects which are used to annotate the
  * map. Some examples of Primitive objects are: Ellipse, Icon, Line,
  * Polygon, Rectangle, and TextAnnotation.
  * @extends MQObjectCollection
  * @see MQPrimitive
  * @see MQEllipsePrimitive
  * @see MQLinePrimitive
  * @see MQPolygonPrimitive
  * @see MQRectanglePrimitive
  * @see MQTextPrimitive
  * @see MQSymbolPrimitive
  */
function MQPrimitiveCollection() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("ALL");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getPRIMITIVECOLLECTION()));
   this.setM_Xpath("PrimitiveCollection");
}
   /**
    * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQPrimitiveCollection.prototype.getClassName = function(){
      return "MQPrimitiveCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQPrimitiveCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQPrimitiveCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var prim = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            if(nodes[count].nodeName==="EllipsePrimitive"){
               prim = new MQEllipsePrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="LinePrimitive"){
               prim = new MQLinePrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="PolygonPrimitive"){
               prim = new MQPolygonPrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="RectanglePrimitive"){
               prim = new MQRectanglePrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="TextPrimitive"){
               prim = new MQTextPrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="SymbolPrimitive"){
               prim = new MQSymbolPrimitive();
               prim.loadXml(mqXmlToStr(nodes[count]));
            }
            this.add(prim);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQPrimitiveCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQPrimitiveCollection

// Begin class MQStringCollection
/* Inheirit from MQObjectCollection */
MQStringCollection.prototype = new MQObjectCollection(32678);
MQStringCollection.prototype.constructor = MQStringCollection;
/**
  * Constructs a new MQStringCollection object.
  * @class Contains a collection of ints.
  * @param {String} itemXpath The xpath to the item
  * @extends MQObjectCollection
  */
function MQStringCollection(itemXpath) {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("String");
   if (itemXpath){
      this.setM_itemXpath(itemXpath);
   }
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSTRINGCOLLECTION()));
   this.setM_Xpath("StringCollection");
}
  /**
   * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQStringCollection.prototype.getClassName = function(){
      return "MQStringCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQStringCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml The xml to be assigned.
    * @type void
    *
    */
   MQStringCollection.prototype.loadXml = function (strXml) {
      this.removeAll();
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;

           var str = null;
         // iterate through xml and create objects for collection
           for (var count=minCount;count < maxCount; count++){
              if(nodes[count].firstChild!==null){
              str = nodes[count].firstChild.nodeValue;
              } else {
                 str = "";
              }
            this.add(str);
           }
      }
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {Node} node of the xml document to be imported.
    * @type void
    * @private
    */
   MQStringCollection.prototype.loadXmlFromNode = function (node) {
		this.setM_XmlDoc(mqCreateXMLDocImportNode(node));
		var xmlDoc = this.getM_XmlDoc();
		if (xmlDoc!==null){
			var root = xmlDoc.documentElement;
			var nodes = root.childNodes;
			var maxCount = nodes.length;
			maxCount = (maxCount < 32678) ? maxCount : 32678;
			var minCount = 0;           var str = null;
			// iterate through xml and create objects for collection
			for (var count=minCount;count < maxCount; count++){
				if(nodes[count].firstChild!==null){
				str = nodes[count].firstChild.nodeValue;
				} else {
					str = "";
				}
				this.add(str);
			}
		}
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQStringCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = "<" + this.getM_itemXpath() + ">" + this.get(i) + "</" + this.getM_itemXpath() + ">";
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
// End class MQStringCollection

// Begin class MQStrColCollection
/* Inheirit from MQObjectCollection */
MQStrColCollection.prototype = new MQObjectCollection(32678);
MQStrColCollection.prototype.constructor = MQStrColCollection;
/**
  * Constructs a new MQStrColCollection object.
  * @class Contains a collection of StringCollections.
  * @param {String} itemXpath The xpath to the item
  * @extends MQObjectCollection
  * @see MQStringCollection
  */
function MQStrColCollection() {
   MQObjectCollection.call(this, 32678);
   this.setValidClassName("MQStringCollection");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSTRCOLCOLLECTION()));
}
  /**
   * Returns the text name of this class.
   * @return The text name of this class.
   * @type String
   */
   MQStrColCollection.prototype.getClassName = function(){
      return "MQStrColCollection";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQStrColCollection.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQStrColCollection.prototype.saveXml = function () {
      var strRetArray = new Array();
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRet = strRetArray.join("");
      return strRet;
   };
// End class MQStrColCollection

// Begin Class MQAutoGeocodeCovSwitch
/* Inheirit from MQObject */
MQAutoGeocodeCovSwitch.prototype = new MQObject();
MQAutoGeocodeCovSwitch.prototype.constructor = MQAutoGeocodeCovSwitch;
/**
 * Constructs a new MQAutoGeocodeCovSwitch object.
 * @class Stores geocode coverage switching parameters.
 * @extends MQObject
 */
function MQAutoGeocodeCovSwitch () {
   MQObject.call(this);
   this.setM_Xpath("AutoGeocodeCovSwitch");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getAUTOGEOCODECOVSWITCH()));
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQAutoGeocodeCovSwitch.prototype.getClassName = function(){
      return "MQAutoGeocodeCovSwitch";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQAutoGeocodeCovSwitch.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQAutoGeocodeCovSwitch.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQAutoGeocodeCovSwitch.prototype.saveXml = function () {
      return mqXmlToStr(this.getM_XmlDoc());
   };
   /**
    * Sets the name of the Geocode coverage switching rules to use.  Names of
    * coverage switching rules are defined in the mqserver.ini file.
    * @param {String} strName Name of coverage switching rules to use.
    * @type void
    */
   MQAutoGeocodeCovSwitch.prototype.setName = function(strName){
      this.setProperty("Name",strName);
   };
   /**
    * Gets the name of the Geocode coverage switching rules being used.
    * @return The value of Name
    * @type String
    */
   MQAutoGeocodeCovSwitch.prototype.getName = function(){
      return this.getProperty("Name");
   };
   /**
    * Sets the maximum matches to be returned for this object.
    * Default = 1
    * @param {Long} lMaxMatches max matches for this object
    * @type void
    */
   MQAutoGeocodeCovSwitch.prototype.setMaxMatches = function(lMaxMatches){
      this.setProperty("MaxMatches",lMaxMatches);
   };
   /**
    * Gets the maximum matches to be returned for this object.
    * @return The max matches for this object
    * @type Long
    */
   MQAutoGeocodeCovSwitch.prototype.getMaxMatches = function(){
      return this.getProperty("MaxMatches");
   };
// End class MQAutoGeocodeCovSwitch

// Begin Class MQAutoRouteCovSwitch
/* Inheirit from MQObject */
MQAutoRouteCovSwitch.prototype = new MQObject();
MQAutoRouteCovSwitch.prototype.constructor = MQAutoRouteCovSwitch;
/**
 * Constructs a new MQAutoRouteCovSwitch object.
 * @class Stores route coverage switching parameters.
  * @param {String} strXpath The type of xPath for this class
 * @extends MQObject
 * @see MQIntCollection
 */
function MQAutoRouteCovSwitch (strXpath) {
   MQObject.call(this);
   if(this.getClassName() === "MQAutoRouteCovSwitch"){
      if (strXpath){
         this.setM_Xpath(strXpath);
         this.setM_XmlDoc(MQA.createXMLDoc("<" + strXpath + "><Name/><DataVendorCodeUsage>0</DataVendorCodeUsage><DataVendorCodes Count=\"0\"/></" + strXpath + ">"));
      } else {
         this.setM_Xpath("AutoRouteCovSwitch");
         this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getAUTOROUTECOVSWITCH()));
      }
   }
   /**
    * Value to represent the data vendor codes
    * @type MQIntCollection
    *
    */
   var m_DataVendorCodes = new MQIntCollection();
   m_DataVendorCodes.setM_Xpath("DataVendorCodes");
   /**
    * Returns the m_DataVendorCodes collection.
    * @return The m_DataVendorCodes collection.
    * @type MQIntCollection
    *
    */
   this.getDataVendorCodes = function() {
      return m_DataVendorCodes;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQAutoRouteCovSwitch.prototype.getClassName = function(){
      return "MQAutoRouteCovSwitch";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQAutoRouteCovSwitch.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQAutoRouteCovSwitch.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var dataVendorCodes = this.getDataVendorCodes();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DataVendorCodes")!==null)
         dataVendorCodes.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DataVendorCodes")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQAutoRouteCovSwitch.prototype.saveXml = function () {
      // get inner object nodes
        var newNode = MQA.createXMLDoc(this.getDataVendorCodes().saveXml());
        // replace nodes
        this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "DataVendorCodes"));
        return mqXmlToStr(this.getM_XmlDoc());

   };
   /**
    * Sets the name of the Route coverage switching rules to use.  Names of
    * coverage switching rules are defined in the mqserver.ini file.
    * @param {String} strName Name of coverage switching rules to use.
    * @type void
    */
   MQAutoRouteCovSwitch.prototype.setName = function(strName){
      this.setProperty("Name",strName);
   };
   /**
    * Gets the name of the Route coverage switching rules being used.
    * @return The value of Name
    * @type String
    */
   MQAutoRouteCovSwitch.prototype.getName = function(){
      return this.getProperty("Name");
   };
   /**
    * Sets the DataVendorCodeUsage for this object.
    * @param {int} lDataVendorCodeUsage DataVendorCodeUsage for this object
    * @type void
    */
   MQAutoRouteCovSwitch.prototype.setDataVendorCodeUsage = function(lDataVendorCodeUsage){
      this.setProperty("DataVendorCodeUsage",lDataVendorCodeUsage);
   };
   /**
    * Gets the DataVendorCodeUsage for this object.
    * @return The DataVendorCodeUsage for this object
    * @type int
    */
   MQAutoRouteCovSwitch.prototype.getDataVendorCodeUsage = function(){
      return this.getProperty("DataVendorCodeUsage");
   };
// End class MQAutoRouteCovSwitch

// Begin Class MQAutoMapCovSwitch
/* Inheirit from MQAutoRouteCovSwitch */
MQAutoMapCovSwitch.prototype = new MQAutoRouteCovSwitch();
MQAutoMapCovSwitch.prototype.constructor = MQAutoMapCovSwitch;
/**
 * Constructs a new MQAutoMapCovSwitch object.
 * @class Stores map coverage switching parameters. Add this object to a
 * Session to enable automatic map coverage switching for the session.
 * @param {String} strXpath The type of xPath for this class
 * @extends MQAutoRouteCovSwitch
 * @see MQIntCollection
 */
function MQAutoMapCovSwitch (strXpath) {
   MQAutoRouteCovSwitch.call(this);
   if (strXpath){
      this.setM_Xpath(strXpath);
      this.setM_XmlDoc(MQA.createXMLDoc("<" + strXpath + "><Name/><Style/><DataVendorCodeUsage>0</DataVendorCodeUsage><DataVendorCodes Count=\"0\"/><ZoomLevels Count=\"14\"><Item>6000</Item><Item>12000</Item><Item>24000</Item><Item>48000</Item><Item>96000</Item><Item>192000</Item><Item>400000</Item><Item>800000</Item><Item>1600000</Item><Item>3000000</Item><Item>6000000</Item><Item>12000000</Item><Item>24000000</Item><Item>48000000</Item></ZoomLevels></" + strXpath + ">"));
   } else {
      this.setM_Xpath("AutoMapCovSwitch");
      this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getAUTOMAPCOVSWITCH()));
   }
   /**
    * Value to represent the Zoom Levels
    * @type MQIntCollection
    */
   var m_ZoomLevels = new MQIntCollection();
   m_ZoomLevels.setM_Xpath("ZoomLevels");
   m_ZoomLevels.add(6000);
   m_ZoomLevels.add(12000);
   m_ZoomLevels.add(24000);
   m_ZoomLevels.add(48000);
   m_ZoomLevels.add(96000);
   m_ZoomLevels.add(192000);
   m_ZoomLevels.add(400000);
   m_ZoomLevels.add(800000);
   m_ZoomLevels.add(1600000);
   m_ZoomLevels.add(3000000);
   m_ZoomLevels.add(6000000);
   m_ZoomLevels.add(12000000);
   m_ZoomLevels.add(24000000);
   m_ZoomLevels.add(48000000);
   /**
    * Returns the m_ZoomLevels collection.
    * @return The m_ZoomLevels collection.
    * @type MQIntCollection
    */
   this.getZoomLevels = function() {
      return m_ZoomLevels;
   };
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQAutoMapCovSwitch.prototype.getClassName = function(){
      return "MQAutoMapCovSwitch";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQAutoMapCovSwitch.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQAutoMapCovSwitch.prototype.loadXml = function (strXml) {
      this.setM_XmlDoc(MQA.createXMLDoc(strXml));
      var dataVendorCodes = this.getDataVendorCodes();
      if(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DataVendorCodes")!==null)
         dataVendorCodes.loadXml(mqXmlToStr(mqGetNode(this.getM_XmlDoc(),"/" + this.getM_Xpath() + "/DataVendorCodes")));
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQAutoMapCovSwitch.prototype.saveXml = function () {
      // get inner object nodes and replace nodes
      var newNode = MQA.createXMLDoc(this.getDataVendorCodes().saveXml());
      this.setM_XmlDoc(mqReplaceElementNode(this.getM_XmlDoc(), newNode, "DataVendorCodes"));
      return mqXmlToStr(this.getM_XmlDoc());

   };
   /**
    * Sets the name of the style alias to be selected for the selected mapping
    * coverage or blank for default.
    * @param {String} strStyle Name of the style alias to use.
    * @type void
    */
   MQAutoMapCovSwitch.prototype.setStyle = function(strStyle){
      this.setProperty("Style",strStyle);
   };
   /**
    * Gets the name of the style alias to be selected for the selected mapping
    * coverage or blank for default.
    * @return The value of Style
    * @type String
    */
   MQAutoMapCovSwitch.prototype.getStyle = function(){
      return this.getProperty("Style");
   };
// End class MQAutoMapCovSwitch

// Begin Class MQSession
/* Inheirit from MQObject */
MQSession.prototype = new MQObjectCollection(32678);
MQSession.prototype.constructor = MQSession;
/**
 * @class Uniquely identifies a MQSession.
 * Maintains the state of the current user's requests. Contains zero
 * or one of the following objects: MQMapCommand, MQMapState,
 * MQDBLayerQueryCollection, MQFeatureCollection, and MQCoverageStyle.
 * @see MQMapCommand
 * @see MQMapState
 * @see MQDBLayerQueryCollection
 * @see MQFeatureCollection
 * @see MQCoverageStyle
 * @see MQPrimitiveCollection
 * @see MQAutoMapCovSwitch
 * @extends MQObject
 */
function MQSession(){
   MQObjectCollection.call(this, 32678);
   this.setM_Xpath("Session");
   this.setM_XmlDoc(MQA.createXMLDocFromNode(MQA.MQXML.getSESSION()));
}
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQSession.prototype.getClassName = function(){
      return "MQSession";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQSession.prototype.getObjectVersion = function(){
      return 0;
   };
   /**
    * Assigns the xml that relates to this object.
    * @param {String} strXml the xml to be assigned.
    * @type void
    *
    */
   MQSession.prototype.loadXml = function (strXml) {
      var xmlDoc = MQA.createXMLDoc(strXml);
      this.setM_XmlDoc(xmlDoc);
      if (xmlDoc!==null){
         var root = xmlDoc.documentElement;
         var nodes = root.childNodes;
         var maxCount = nodes.length;
         maxCount = (maxCount < 32678) ? maxCount : 32678;
         var minCount = 0;
         var obj = null;
         // iterate through xml and create objects for collection
         for (var count=minCount;count < maxCount; count++){
            obj = null;
            if(nodes[count].nodeName==="MapState"){
               obj = new MQMapState();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="CoverageStyle"){
               obj = new MQCoverageStyle();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="AutoMapCovSwitch"){
               obj = new MQAutoMapCovSwitch();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="DBLayerQueryCollection"){
               obj = new MQDBLayerQueryCollection();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="FeatureCollection"){
               obj = new MQFeatureCollection();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="PrimitiveCollection"){
               obj = new MQPrimitiveCollection();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="Center"){
               obj = new MQCenter();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="CenterLL"){
               obj = new MQCenterLL();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="ZoomIn"){
               obj = new MQZoomIn();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="ZoomOut"){
               obj = new MQZoomOut();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="ZoomTo"){
               obj = new MQZoomTo();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="ZoomToRect"){
               obj = new MQZoomToRect();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="ZoomToRectLL"){
               obj = new MQZoomToRectLL();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="Pan"){
               obj = new MQPan();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="BestFit"){
               obj = new MQBestFit();
               obj.loadXml(mqXmlToStr(nodes[count]));
            } else if(nodes[count].nodeName==="BestFitLL"){
               obj = new MQBestFitLL();
               obj.loadXml(mqXmlToStr(nodes[count]));
            }
           if (obj!==null)
              this.add(obj);
         }
      }
   };
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQSession.prototype.saveXml = function () {
      var strRetArray = new Array();
      strRetArray[strRetArray.length] = "<" + this.getM_Xpath() + " Count=\"" + this.getSize() + "\">";
      // iterate through objects and add them here
      var size = this.getSize();
      for(var i = 0; i < size; i++){
         strRetArray[strRetArray.length] = this.get(i).saveXml();
      }
      strRetArray[strRetArray.length] = "</" + this.getM_Xpath() + ">";
      var strRet = strRetArray.join("");
      return strRet;
   };
   /**
    * Adds a MQObject to the collection. Calls <code>addOne(MQObject, null)</code>.
    * @param {MQObject} obj The new MQObject to be added to the collection.
    * @see addOne(MQObject, null)
    * @type void
    */
   MQSession.prototype.add = function (obj){
      return this.addOne(obj, null);
   }
   /**
    * Returns true if the specified object is a MapCommand object, false otherwise.
    *    <UL><B>MQMapCommand objects</B>
    *       <LI>MQCenter
    *       <LI>MQCenterLatLng
    *       <LI>MQZoomIn
    *       <LI>MQZoomOut
    *       <LI>MQZoomTo
    *       <LI>MQZoomToRect
    *       <LI>MQZoomToRectLatLng
    *       <LI>MQPan
    *       <LI>MQBestFit
    *       <LI>MQBestFitLL
    *    </UL>
    * @param  {MQObject} obj The MQObject to be validated.
    * @return true if the specified object is a MapComand object, false otherwise.
    * @type Boolean
    */
   MQSession.prototype.isMapCommandObject = function(obj)
   {
      if(obj){
         var cls = obj.getClassName();
         if(cls==="MQCenter" || cls==="MQCenterLatLng" || cls==="MQZoomIn" ||
            cls==="MQZoomOut" || cls==="MQZoomTo" || cls==="MQZoomToRect" ||
            cls==="MQZoomToRectLatLng" || cls==="MQPan" || cls==="MQBestFit" ||
            cls==="MQBestFitLL"){
            return true;
         }else{
            return false;
         }
      }

   }
   /**
    * Adds an element to this Session object, ensuring that it is
    * the only one of its class type.  Validates element as being one
    * of object types that is permitted in this Session object.  If
    * it is, and there happens to be one of that type of object
    * already present, the new object will replace it.
    * @param  {MQObject} newElement The new element that is to be added to this MQSession object.
    * @param  {MQObject} replacedElement The element that was previously in this MQSession object.
    * @return index of element that is added.
    * @type void
    */
   MQSession.prototype.addOne = function(newElement, replacedElement){
      var collectionSize = this.getSize();
      var newTypeId = newElement.getClassName();
      var newIndex = 0;

      if (this.isValidObject(newElement)){
         if (this.isMapCommandObject(newElement)){
            // Check if we already have a MapCommand object
            for (newIndex = 0; newIndex < collectionSize; newIndex++){
               if (isMapCommandObject(get(newIndex)))
                  break;
            }
         } else {
            for (newIndex = 0; newIndex < collectionSize; newIndex++){
               if (get(newIndex).getClassId() == newTypeId)
                  break;
            }
         }
      } else {
         alert("Invalid object for this collection.");
         throw ("Invalid object for this collection.");
      }
      if (newIndex < collectionSize){
         replacedElement = this.set(newIndex, newElement);
      }else{
         m_collection.add(newElement);
      }
      return newIndex;
   }
   /**
    * Returns true if the specified object is a valid object for this
    * collection, false otherwise.
    *    <UL><B>Valid objects</B>
    *       <LI>MQCenter
    *       <LI>MQCenterLatLng
    *       <LI>MQZoomIn
    *       <LI>MQZoomOut
    *       <LI>MQZoomTo
    *       <LI>MQZoomToRect
    *       <LI>MQZoomToRectLatLng
    *       <LI>MQPan
    *       <LI>MQBestFit
    *       <LI>MQBestFitLL
    *       <LI>MQDBLayerQueryCollection
    *       <LI>MQCoverageStyle
    *       <LI>MQFeatureCollection
    *       <LI>MQAutoMapCovSwitch
    *       <LI>MQPrimitiveCollection
    *       <LI>MQMapState
    *    </UL>
    * @param  {MQObject} obj The MQObject to be validated.
    * @return Boolean true if the specified object is a valid object for this
    * collection, false otherwise.
    * @type Boolean
    */
   MQSession.prototype.isValidObject = function(obj){
      if(obj){
         var cls = obj.getClassName();
         if(cls==="MQCenter" || cls==="MQCenterLatLng" || cls==="MQZoomIn" || cls==="MQZoomOut" ||
            cls==="MQZoomTo" || cls==="MQZoomToRect" || cls==="MQZoomToRectLatLng" || cls==="MQPan" ||
            cls==="MQBestFit" || cls==="MQBestFitLL" || cls==="MQDBLayerQueryCollection" || cls==="MQCoverageStyle" ||
            cls==="MQFeatureCollection" || cls==="MQAutoMapCovSwitch" || cls==="MQPrimitiveCollection" || cls==="MQMapState"){
            return true;
         }else{
            return false;
         }
      }
   }
// End Class MQSession

// Begin Class MQAuthentication
function MQAuthentication(strInfo) {
   var strTransactionInfo = (strInfo!=null) ? strInfo: "";
   this.getInfo = function(){
   return strTransactionInfo;
   }

}
   /**
    * Build an xml string that represents this object.
    * @return The xml string.
    * @type String
    *
    */
   MQAuthentication.prototype.saveXml = function() {
      return "<Authentication Version=\"" + this.getObjectVersion() + "\">" +
            "<TransactionInfo>" + this.getInfo() + "</TransactionInfo>" +
            "</Authentication>";
   };
   /**
    * Returns the text name of this class.
    * @return The text name of this class.
    * @type String
    */
   MQAuthentication.prototype.getClassName = function(){
      return "MQAuthentication";
   };
   /**
    * Returns the version of this class.
    * @return The version of this class.
    * @type int
    */
   MQAuthentication.prototype.getObjectVersion = function(){
      return 2;
   };
// End Class MQAuthentication

// Begin class MQXmlNodeObject
function MQXmlNodeObject( strName, strValue ) {

   var nodeName = strName;
   var nodeValue = strValue;

   this.saveXml = function() {
      return "<" + nodeName + ">" + nodeValue + "</" + nodeName + ">";
   };
}