//global factors for spacing between elements
//suggested factor:   xfactor/yfactor = 4/3
	var X_FACTOR = 3.5;
	var Y_FACTOR = 2.625;

//reloads the grid of nodes
function reloadGrid(){
	gridRectWidth=parseInt(mapElemDimension*X_FACTOR);	
	gridRectHeight=parseInt(mapElemDimension*Y_FACTOR);
	var numCols = parseInt(map.getWidth()/gridRectWidth);
	if(numCols==0) numCols=1;
	//alert("numcols="+numCols+" mapWidth/gridRectWidth " +map.getWidth()+"/"+gridRectWidth);
	var numRows = parseInt(map.getHeight()/gridRectHeight);
	if(numRows==0) numRows=1;
	//alert("numrows="+numRows+" mapHeight/gridRectHeight " +map.getHeight()+"/"+gridRectHeight);

	maxNumOfElements = numCols*numRows;
	var nodeGrid=new Array(numCols);
	for(i = 0; i< numCols; i++){
		nodeGrid[i]=new Array(numRows);
	}
	//alert(nodeGrid);
	var nodes =	map.mapElements;
	for(n in nodes){
		var i = parseInt(nodes[n].x / gridRectWidth);
		//if map dimension are not in 4/3 format
		if(i>=numCols) i=numCols-1;
		var j =	parseInt(nodes[n].y / gridRectHeight);
		if(j>=numRows) j=numRows-1;
		//alert(nodes[n].x+" "+nodes[n].y+" - "+i+" "+j);
		if(	nodeGrid[i][j] == undefined)
			nodeGrid[i][j]=1;
		else nodeGrid[i][j]++;
	}
	
	//alert(nodeGrid);
	return nodeGrid;
}


//gets the first point (Point2D) free of the grid
function getFirstFreePoint(){
	//first, reload grid
	var nodeGrid=reloadGrid();
	//loop first on cols, after on rows
	for(j=0; j<nodeGrid[0].length; j++){
		for(i=0; i<nodeGrid.length; i++){//grid is a 'rectangle'
		  		if(	nodeGrid[i][j]==undefined   || nodeGrid[i][j] == 0){
		  			//alert("grid element "+i+" "+j);
		  			return new Point2D(i*gridRectWidth+gridRectWidth/2, j*gridRectHeight+gridRectHeight/2+(i%2*gridRectHeight/4));	
		  			}
		}
	}
	return null;
}


//gets all free points (Array of Point2D) of the grid
function getFreePoints(){
	//first, reload grid
	var nodeGrid=reloadGrid();
	var freePoints = new Array();
	//loop first on cols, after on rows
	for(j=0; j<nodeGrid[0].length; j++){
		for(i=0; i<nodeGrid.length; i++){//grid is a 'rectangle'
		  		if(	nodeGrid[i][j] == undefined  || nodeGrid[i][j] == 0){
		  			//alert("grid element "+i+" "+j);
		  			var np = new Point2D(i*gridRectWidth+gridRectWidth/2, j*gridRectHeight+gridRectHeight/2+(i%2*gridRectHeight/4));	
		  			freePoints.push(np);
		  			}
		}
	}
	return freePoints;
}