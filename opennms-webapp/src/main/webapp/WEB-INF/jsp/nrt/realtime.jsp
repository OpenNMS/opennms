<html>
  <head>
    <title>NearRealTime</title>
    <link type="text/css" href="../../../css/jquery-ui-1.7.3.custom.css" rel="Stylesheet" />	
    <!-- <script type="text/javascript" src="../../../js/jquery-1.4.2.min.js"></script> -->
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.9/jquery-ui.min.js"></script>
    <script type="text/javascript" src="../../../js/amq_jquery_adapter.js"></script>
    <script type="text/javascript" src="../../../js/amq.js"></script>
    <script type="text/javascript" src="../../../js/d3.v2.js"></script>
    <style type="text/css">

svg {
  border: 1px solid #ccc;
}

path {
  stroke: steelblue;
  stroke-width: 1;
  fill: none;
}

line {
  stroke-width: 1;
  stroke: #ececec;
}
    </style>

    <!-- Receive inbound JavaScript variables from the controller. -->
    <script type="text/javascript">
        var collectionTaskId = "${collectionTask}";
    </script>

    <!-- Create and initialize the AMQ object and listener -->
    <script type="text/javascript">
// Create AMQ object.
var amq = org.activemq.Amq;

// L
amq.init({
  uri: '/amq', 
  logging: true, 
  timeout: 45, 
  clientId:(new Date()).getTime().toString() 
});

var realTimeHandler = {
  receiveMessage: function(message) {
    if (message != null) {
      var arr = message.textContent.split(";");
      line.addPoint(parseFloat(arr[1]));
      updateGraphValueList(arr[0], arr[1]);
      //document.getElementById("outputDiv").innerText=arr[1];
    }
  }
};

amq.addListener('RealtimeHandler', collectionTaskId, realTimeHandler.receiveMessage);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																													
    </script>

    <!-- Create AJAX timer. -->
    <script type="text/javascript">
// The time in seconds to request a new job.
var refreshIntervalInSeconds = 5;

var refreshTimerId = setInterval(function() {
	$.get("/nrt/starter.htm",
		"collectionTask="+collectionTaskId,
		function(data) { $("#errorDiv").text(data); },
		"html");
}, refreshIntervalInSeconds*1000);
    </script>

  </head>
  <body>
    <h1>Realtime Graph</div>
    <div id="content"></div>
    <div id="outputDiv"></div>
    <div id="errorDiv"></div>

    <!-- Create AJAX timer. -->
    <script type="text/javascript">
// The time in seconds to request a new job.
var refreshIntervalInSeconds = 10;

var refreshTimerId = setInterval(function() {
	alert("refreshing collection job.");
}, refreshIntervalInSeconds*1000);

function refreshCollectionJob() {
	alert("refreshing collection job.");
}
    </script>
    
    <script type="text/javascript">
// Define graph defaults.
var width = 600;
var height = 200;

// JQuery to facilitate the resize functionality.
$(function() {
	// Flag the graph container as resizable.
	$( "#resizable" ).resizable();

	// Catch that the container has been resized and alter the size of the graph.
	$( "#resizable" ).resize(function() {
		var elem = $( "#resizable" );
		height = elem.height();
		width = elem.width();
		
		// Call the functions to resize the graph and adjust the scale.
		resizeGraph();
		refreshGraphScale();
	});
});

function resizeGraph() {
	vis.attr("height", height);
	vis.attr("width", width);
	graphSvgLine.attr("y1", height / 2);
	graphSvgLine.attr("y2", height / 2);
	graphSvgLine.attr("x2", width)
}

function refreshGraphScale() {
	y.domain([0, maxGraphScale]).range([height, 0]);
	x.domain([0, width]).range([0, width]);
}

function updateGraphValueList(graphKey, graphValue) {
	$("#outputDiv").text("Key: " + graphKey + " Value: " + graphValue);
}

// Create the d3 graph.
var vis = d3.select("#content").append("svg");
var g = vis.append("svg:g").attr("class", "sparkline");

var graphSvgLine = g.append("svg:line")
    .attr("x1", 0)
  g.append("svg:path")

// Call the function to resize the visible graph space.
resizeGraph();

var maxGraphScale = 1000;
var y = d3.scale.linear()
    .domain([0, maxGraphScale])
    .range([height, 0]);

var x = d3.scale.linear()
    .domain([0, width])
    .range([0, width]);

var plot = d3.svg.line()
    .x(function(d,i) { return x(i)})
    .y(function(d) { return y(d)})
    .interpolate("linear")

  line = {
    'name': "",
    'data': [],
    'addPoint': function (datapoint) {
      this.data.push(datapoint)
      maxx = x(this.data.length-1)
      if (maxx >= width) {
        this.graph.select("g.sparkline")
          .select("path")
            .attr("d", plot(this.data))
            .attr("transform", "translate(" + x(1) + ")")
            .transition().duration(100).ease("linear")
            .attr("transform", "translate(" + x(0) + ")");
        this.data.shift()
      } else {
        // this.graph.attr("width", maxx)
        this.graph.select("g.sparkline")
          .select("path")
            .attr("d", plot(this.data))
      }
    }
  }
  line.graph = vis;
  
    </script>
  </body>
</html>

