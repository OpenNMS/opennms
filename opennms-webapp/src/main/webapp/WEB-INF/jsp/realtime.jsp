<html>
  <head>
    <title>Realtime</title>
    <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="js/amq_jquery_adapter.js"></script>
    <script type="text/javascript" src="js/amq.js"></script>
    <script type="text/javascript" src="js/d3.v2.js"></script>
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
    <script type="text/javascript">

var amq = org.activemq.Amq;

amq.init({
  uri: 'amq', 
  logging: true, 
  timeout: 45, 
  clientId:(new Date()).getTime().toString() 
});

var realTimeHandler = {
  receiveMessage: function(message) {
    if (message != null) {
      var arr = message.textContent.split(";");
      line.addPoint(parseFloat(arr[1]));
      document.getElementById("outputDiv").innerText=arr[1];
    }
  }
};

amq.addListener('RealtimeHandler', 'NrtResults', realTimeHandler.receiveMessage);

    </script>
  </head>
  <body>
    <h1>Realtime Graph</div>
    <div id="content"></div>
    <div id="outputDiv"></div>
    
    <script type="text/javascript">

  var width = 300;
  var height = 100;

  vis = d3.select("#content")
    .append("svg")
      .attr("width", width)
      .attr("height", height);
  g = vis.append("svg:g")
    .attr("class", "sparkline");
  g.append("svg:line")
    .attr("x1", 0)
    .attr("y1", height / 2)
    .attr("x2", width)
    .attr("y2", height / 2)
  g.append("svg:path")


  var max = 100;
  y = d3.scale.linear()
    .domain([0, max])
    .range([height, 0]);
  x = d3.scale.linear()
    .domain([0, width])
    .range([0, width]);

  plot = d3.svg.line()
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

