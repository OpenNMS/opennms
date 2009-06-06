// Here are defined all the global variables used in OpenNMS map application
var appContext = "/opennms/";
var suffix = "map";

// IMAGES FOLDERS
var IMAGES_FOLDER = "images";
var IMAGES_BACKGROUND_FOLDER = IMAGES_FOLDER+"/background/";
var IMAGES_ELEMENTS_FOLDER = IMAGES_FOLDER+"/elements/";

// ACTIONS
var ADDNODES_ACTION = "AddNodes";
var ADDRANGE_ACTION = "AddRange";
var ADDMAPS_ACTION = "AddMaps";
var REFRESH_ACTION = "Refresh";
var RELOAD_ACTION = "Reload";
var ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";
var ADDMAPS_WITH_NEIG_ACTION = "AddMapsWithNeig";
var ADDNODES_BY_CATEGORY_ACTION = "AddNodesByCategory";
var ADDNODES_BY_LABEL_ACTION = "AddNodesByLabel";
var ADDNODES_NEIG_ACTION = "AddNodesNeig";
var ADDMAPS_NEIG_ACTION = "AddMapsNeig";
var DELETENODES_ACTION = "DeleteNodes";
var DELETEMAPS_ACTION = "DeleteMaps";
var CLEAR_ACTION = "Clear";
var DELETEMAP_ACTION = "DeleteMap";
var SWITCH_MODE_ACTION = "SwitchMode";
var LOADDEFAULTMAP_ACTION = "LoadDefaultMap";
var LOADMAPS_ACTION = "LoadMaps";
var LOADNODES_ACTION = "LoadNodes";
var NEWMAP_ACTION = "NewMap";
var OPENMAP_ACTION = "OpenMap";
var CLOSEMAP_ACTION = "CloseMap";
var SAVEMAP_ACTION = "SaveMap";

// General fonts setting
var titleFontSize = 12;
var textFontSize = 10;
var textFamily = "Arial,Helvetica";

// Constants used by SVG Link Object 
var deltaLink=2;
var borderPercentage=5;

//global factors for spacing between elements
//suggested factor:   xfactor/yfactor = 4/3 used by ApplicationMapElemPosition.js functions
var X_FACTOR = 3.5;
var Y_FACTOR = 2.625;

// Menu ObjectStyle
//Costants used for Menu style

var menuDownColor = "blue";
var menuUpColor =  "black";
var menuStyle = {"stroke":"navy","fill":"black","stroke-width":1};
var menuTextStyle = {"font-size": titleFontSize,"font-family":textFamily,"fill":"white"};
var menuMouseStyle = {"fill":"green","fill-opacity":0};
var menuWidth = 90;
var menuHeight = 20;
var mapMenuX = 0;
var mapMenuY = 21;
var nodeMenuX= menuWidth;
var nodeMenuY= mapMenuY;
var viewMenuX= menuWidth;
var viewMenuY = mapMenuY;
var refreshMenuX = 2 * menuWidth;
var refreshMenuY = mapMenuY;
var menuDeltaX = 0;
var menuDeltaY = menuHeight;

// Global variable constants identifing the menu
var mapMenuId = "MapMenu";
var nodeMenuId = "NodeMenu";
var viewMenuId = "ViewMenu";
var refreshMenuId = "RefreshMenu";

// These are constants used to manage button style
var buttonx = 180;
var buttony = 0;
var buttonwidth = 60;
var buttonheight = 20;
var buttonTextStyles = {"font-family":textFamily, "fill":"white","font-size":titleFontSize};
var buttonStyles = {"fill":"blue","fill-opacity":0};
var shadeLightStyles = {"fill":"blue","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var shadeDarkStyles = {"fill":"blue","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var shadowOffset = 0;

// These are constants used to manage textbox style
var textboxmaxChars = 32;
var textboxx = 3; 
var textboxy = 22; 
var textboxWidth = 237;
var textboxHeight = 22;
var textYOffset = 14;
var textStyles = {"font-family":textFamily,"font-size":titleFontSize,"fill":"black"};
var boxStyles = {"fill":"white","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var cursorStyles = {"stroke":"dimgray","stroke-width":1.5};
var seltextBoxStyles = {"fill":"white","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};

// Selection Boxes style constants
var selBoxwidth = 238;
var selBoxxOffset =2;
var selBoxyOffset = 23;
var selBoxCellHeight = 18;
var selBoxTextpadding = 3;
var selBoxheightNrElements=5;
var selBoxtextStyles = {"font-family":textFamily,"font-size":titleFontSize,"fill":"dimgray"};
var selBoxStyles = {"stroke":"dimgray","stroke-width":1,"fill":"white"};
var selBoxScrollbarStyles = {"stroke":"dimgray","stroke-width":1,"fill":"whitesmoke"};
var selBoxSmallrectStyles = {"stroke":"dimgray","stroke-width":1,"fill":"lightgray"};
var selBoxHighlightStyles = {"fill":"dimgray","fill-opacity":0.3};
var selBoxTriangleStyles = {"fill":"dimgray"}; 
var selBoxpreSelect = 0;
// ContextMenu---Styles
var cmdelta= 20;
var cmwidth = 100;
var cmmenuStyle={"fill":"grey"};
var cmmenuElementStyle = {"fill":"grey"};
var cmmenuElementTextStyle={"font-size": titleFontSize,"font-family":textFamily,"fill":"black"};
var cmmenuElementMouseStyle={"fill":"grey","fill-opacity":0};

// Windows Objects styles
/*
var winPlaceholderStyles = {"fill":"none","stroke":"dimgray","stroke-width":1.5};
var windowStyles = {"fill":"aliceblue","stroke":"dimgray","stroke-width":1};
var wintitlebarStyles = {"fill":"gainsboro","stroke":"dimgray","stroke-width":1};
var winstatusbarStyles = {"fill":"aliceblue","stroke":"dimgray","stroke-width":1};
var wintitletextStyles = {"font-family":"Arial,Helvetica","font-size":titleFontSize,"fill":"dimgray"};
var winstatustextStyles = {"font-family":"Arial,Helvetica","font-size":textFontSize,"fill":"dimgray"};
var winbuttonStyles = {"fill":"none","stroke":"dimgray","stroke-width":1};
var wintitlebarHeight = 17;
var winstatusbarHeight = 13;

var winwidth = 200;
var winheight = 200;
var winconstrXmin = 0;
var winconstrYmin = 80;
var winconstrXmax = 1024;
var winconstrYmax = 700;
*/
