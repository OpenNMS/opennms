//********* Variables to Manage Application ************

//say if the menu is been opened or not
//color of the component of the menu
// The SVG node for menu in Map.svg
var menuSvgElement;
// The SVG Node for MapPanel in Map.svg
var mapSvgElement;
//the SVG map object to work on.
var map;

// The Javascript Object for menus!
var mapMenu;
var nodeMenu;
var viewMenu;
var refreshMenu;

// Needed by ECMAScripts
var myMapApp = new mapApp();

// Windows Objects used now when double click on MapElements
myMapApp.Windows = new Array();

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

//variables for NODE --- nodes are loaded by LoadNodes()
var hideNodesIds = "";
var hasHideNodes = false;
// variables for MAP ---maps are loaded by LoadMaps()
var selectedMapInList=0;
var selMaps;

var mapLabels = [" "];
var mapSortAss;
var mymapsResult;		
var mapsLoaded = false;

// variable to support the default map
var defaultMap;

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
var NODE_TYPE;
var MAP_TYPE;
var MAP_NOT_OPENED;
var NEW_MAP;


// Global variable to define the color of semaphore
var colorSemaphoreBy;

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
