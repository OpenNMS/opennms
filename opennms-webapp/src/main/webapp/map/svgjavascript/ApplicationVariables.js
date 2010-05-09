// The suffix to be used in URLs in mapDisplatcher-servlet.xml
var suffix = "map";

// IMAGES FOLDERS
var IMAGES_FOLDER = "images";
var IMAGES_BACKGROUND_FOLDER = IMAGES_FOLDER+"/background/";
var IMAGES_ELEMENTS_FOLDER = IMAGES_FOLDER+"/elements/";


// General Variables used for fonts setting
var titleFontSize = 12;
var textFontSize = 10;
var textFamily = "Arial,Helvetica";

// Variables used by "Link" SVG Object 
var deltaLink=6;
var borderPercentage=5;

// Variables used by "Summary Link" SVG Object
var summaryLinkId = -1;
var summaryLinkColor;
//global factors for spacing between elements
//suggested factor:   xfactor/yfactor = 4/3 
var X_FACTOR = 3.5;
var Y_FACTOR = 2.625;

//TabGroup Object Style
var tabStyles = {"fill":"navy","stroke":"navy","stroke-width":1,"cursor":"pointer"};
var tabwindowStyles = {"fill":"white","stroke":"navy","stroke-width":1};
var tabtextStyles = {"font-family":textFamily,"font-size":titleFontSize,"fill":"white","font-weight":"normal"};
var tabactivetabBGColor = "blue";

// Menu ObjectStyle
//Variables used for Menu style
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

// These are variables used to set <button> object style
var buttonx = 180;
var buttony = 0;
var buttonwidth = 60;
var buttonheight = 20;
var buttonTextStyles = {"font-family":textFamily, "fill":"white","font-size":titleFontSize};
var buttonStyles = {"fill":"blue","fill-opacity":0};
var shadeLightStyles = {"fill":"blue","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var shadeDarkStyles = {"fill":"blue","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var shadowOffset = 0;

// These are variables used to set <textbox> object style
var textboxmaxChars = 32;
var textboxx = 2; 
var textboxy = 0; 
var textboxWidth = 170;
var textboxHeight = 20;
var textYOffset = 14;
var textStyles = {"font-family":textFamily,"font-size":titleFontSize,"fill":"black"};
var boxStyles = {"fill":"white","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};
var cursorStyles = {"stroke":"dimgray","stroke-width":1.5};
var seltextBoxStyles = {"fill":"white","stroke":"pink","stroke-width":1,"fill-opacity":0.5,"stroke-opacity":0.9};

// These are variables used to set <Selection Box> object style
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

// These are variables to set <ContextMenu> object style
var cmdelta= 20;
var cmwidth = 100;
var cmmenuStyle={"fill":"grey"};
var cmmenuElementStyle = {"fill":"grey"};
var cmmenuElementTextStyle={"font-size": titleFontSize,"font-family":textFamily,"fill":"black"};
var cmmenuElementMouseStyle={"fill":"grey","fill-opacity":0};

// These are variables to set <ToolTip> object style
var tooltipTextStyles = {"font-family":"Arial,Helvetica","fill":"dimgray","font-size":titleFontSize};
var tooltipRectStyles = {"fill":"white","stroke":"dimgray"};


//********* Variables to Manage Application ************

//say if the menu is been opened or not
//color of the component of the menu
// The SVG node for menu in Map.svg
var menuSvgElement;
// The SVG Node for MapPanel in Map.svg
var mapSvgElement;
// The SVG Node for TabPanel in Map.svg
var tabSvgElement;
//the SVG map object to work on.
var map;
//The SVG tabGroup object to work on.
var mapTabGroub;

// The Javascript var Object for menus!
var mapMenu;
var nodeMenu;
var viewMenu;
var refreshMenu;

// Global variables/constants identify the menus object
var mapMenuId = "MapMenu";
var nodeMenuId = "NodeMenu";
var viewMenuId = "ViewMenu";
var refreshMenuId = "RefreshMenu";

// Needed by ECMAScripts
var myMapApp = new mapApp(false,undefined);

// Windows Objects used now when double click on MapElements
myMapApp.Windows = new Array();

//WorkPanel data
var workPanelWidth = 3*menuWidth;
var workPanelHeight = 600;
var workPanelDelta = 2;

// The Map width and height definition
var mapWidth;
var mapHeight;

//string containing a string form of the current map saved. this is used to test if the map is changed
var savedMapString=new String(); 

// variable for setting refresh time
var refreshNodesIntervalInSec=300; 

// variable for setting element dimension
var mapElemDimension=25;	

// Variable that states the current mode of the map. If true the user can modify the maps
var isAdminMode = false;
	
// int containing the number of loading at the moment (it is increased when an httprequest is done until handled) 
var loading=0;

// Variables used to define the state of the map aplication
var refreshingMapElems=false;
var deletingMapElem=false;
var addingMapElemNeighbors=false;
var settingMapElemIcon=false;
var stopCountdown=false;
var countdownStarted=false;
// ************ TopInfo variables ************

// Variable used to identify a Button SVG Object in TopInfo SVG box
var button1;
 
// Variable used to identify a TextBox SVG Object in TopInfo SVG box
var textbox1;

// *********** Variables used by selectionList SVG Object in TopInfo SVG box ******

// variables for NODE --- nodes are loaded by LoadNodes()
var selectedMapElemInList=0;
var selNodes; 

var nodeLabels = [" "];
var nodeSortAss;
var nodeidSortAss;
var mynodesResult;
var nodesLoaded = false;

//variables for Hide NODE
var hideNodesIds = "";
var hasHideNodes = false;

// variables for MAP ---maps are loaded by LoadMaps()
var mapLabels = [" "];
var mapSortAss;
var mapidSortAss;
var mymapsResult;		
var mapsLoaded = false;
//variables for tabGroup
var mapTabTitles = new Array();
// variables for search Map
var nodeLabelMap =new Array();

// variable to support the default map
var defaultMap;

//variables for Hide Maps
var hideMapsIds = "";
var hasHideMaps = false;

// variables for CATEGORIES --- categories are loaded by init()
var selectedCategoryInList=0;
var selCategories; 

var categories = [" "];
var categorySorts = [null]; 
var categorySortAss;
var mycategoriesResult;
	
//variables for MAP BACKGROUND IMAGES ---- backgroun image are laoded by init()
var selectedBGImageInList=0;
var selBGImages; 

var BGImages=[""];
var BGImagesSorts=[null]; 
var BGImagesSortAss;
var myBGImagesResult;

//variables for ICONS
var selectedMEIconInList=0;
var selMEIcons;
 
var MEIcons=[""];
var MEIconsSorts=[null]; 
var MEIconsSortAss;
var myMEIconsResult;		


//variables for NODE DIMENSION --- setted by init()
var selectedMapElemDimInList;
var selMapElemDim; 

var MapElemDim = [""];
var MapElemDimSorts = [null]; 
var MapElemDimSortAss;
var myMapElemDimResult;

// variable for setting refresh time --- setted in init()
var selectedRefreshTimeList="30 seconds";
var selRefreshTimeMins;

var refreshTimeMins = [""];
var refreshTimeMinsSorts= [null];
var refreshTimeMinsSortAss;
var myRefreshTimeMinsResult;		

//********* Finished TopInfo variables ***********

// ********Variables set during application init **************

// variable that stores if the user is Admin Role.
var isUserAdmin;

// variable to display semaphore or icon inline color
var useSemaphore;

// Define known statuses
var STATUSES_TEXT= new Array();
var STATUSES_COLOR= new Array();
var STATUSES_UEI= new Array();

// Define known severities
var SEVERITIES_LABEL= new Array();
var SEVERITIES_COLOR= new Array();
var SEVERITIES_FLASH= new Array();

// Define known avails
var AVAIL_COLOR = new Array();
var AVAIL_MIN = new Array();
var AVAIL_FLASH= new Array();

// Define known links
var LINK_SPEED = new Array();
var LINK_TEXT = new Array();
var LINK_WIDTH = new Array();
var LINK_DASHARRAY = new Array();	
var LINKSTATUS_COLOR = new Array();	
var LINKSTATUS_FLASH = new Array();	

// Context Menu 
var CM_COMMANDS = new Array();
var CM_LINKS = new Array();
var CM_PARAMS = new Array();

// Global Variables for defaults  
var DEFAULT_ICON;
var DEFAULT_MAP_ICON;
var DEFAULT_BG_COLOR;

// Global variable to define the color of semaphore
var colorSemaphoreBy='S';

// Variable for the map history
var mapHistory = new Array();
var mapHistoryName = new Array();
var mapHistoryIndex = 0;

// a currentMapId = MAP_NOT_OPENED indicates that no Maps are opened. 
// current map variables
var currentMapId;
var currentMapBackGround="";
var currentMapAccess="", currentMapName="", currentMapOwner="", currentMapUserlast="", currentMapCreatetime="", currentMapLastmodtime="", currentMapType="";

//vars for set background color
var x_picker,pick_begin,pick_color,pick_appui,comp_rouge=128,comp_vert=128,comp_bleu=128,pick_prefixe="pickColor",node,cible;
	

// Context menu height
var cmheight;

// Max number of links between two mapElements
var maxLinks;