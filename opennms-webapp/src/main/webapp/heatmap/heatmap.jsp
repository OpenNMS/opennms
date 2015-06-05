<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8"/>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min.js"></script>
    <script type="text/javascript" src="js/jquery.ui.treemap.js"></script>
</head>
<body>
<div id="treemap" style="width:900px;height:900px"></div>
<script type="text/javascript">
    var mouseclickHandler = function (e, data) {
        var nodes = data.nodes;
        var ids = data.ids;
        alert('you clicked node with id ' + nodes[0].id);
    };

    var url = "/opennms/rest/heatmap/nodesByForeignSource/My-Foreign-Source-1";

    $.getJSON(url, function (data) {
        console.log(data);

        $(document).ready(function () {
            $("#treemap").treemap({
                "colorStops": [
                    {"val": 1.0, "color": "#CC0000"},
                    {"val": 0.5, "color": "#FFCC00"},
                    {"val": 0.0, "color": "#336600"}
                ],
                "labelsEnabled": true,
                "dimensions": [500, 500],
                "nodeData": {
                    "id": "2fc414e2",
                    "children": data.children
                }

            }).bind('treemapclick', mouseclickHandler);
        });
    });
</script>
</body>
</html>