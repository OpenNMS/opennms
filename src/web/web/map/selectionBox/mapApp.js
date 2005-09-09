//holds data on window size
function mapApp() {
	if (!document.documentElement.getScreenCTM) {
		//initialize ratio
		this.resetFactors();
		//add resize event to document element
		document.documentElement.addEventListener("SVGResize",this,false);
	}
}

mapApp.prototype.handleEvent = function(evt) {
	if (evt.type == "SVGResize") {
		this.resetFactors();
	}
}

mapApp.prototype.resetFactors = function() {
	if (!document.documentElement.getScreenCTM) {
		//case for viewers that don't support .getScreenCTM, such as ASV3
		//calculate ratio and offset values of app window
		var viewBoxArray = document.documentElement.getAttributeNS(null,"viewBox").split(" ");
		var myRatio = viewBoxArray[2]/viewBoxArray[3];
		if ((window.innerWidth/window.innerHeight) > myRatio) { //case window is more wide than myRatio
			this.scaleFactor = viewBoxArray[3] / window.innerHeight;
		}
		else { //case window is more tall than myRatio
			this.scaleFactor = viewBoxArray[2] / window.innerWidth;
		}
		this.offsetX = (window.innerWidth - viewBoxArray[2] * 1 / this.scaleFactor) / 2;
		this.offsetY = (window.innerHeight - viewBoxArray[3] * 1 / this.scaleFactor) / 2;
	}
}

mapApp.prototype.calcCoord = function(coordx,coordy) {
	var coords = new Array();
	if (!document.documentElement.getScreenCTM) {
		//case ASV3 a. Corel
		coords["x"] = (coordx  - this.offsetX) * this.scaleFactor;
		coords["y"] = (coordy - this.offsetY) * this.scaleFactor;
	}
	else {
		matrix=document.documentElement.getScreenCTM();
		coords["x"]= matrix.inverse().a*coordx+matrix.inverse().c*coordy+matrix.inverse().e;
		coords["y"]= matrix.inverse().b*coordx+matrix.inverse().d*coordy+matrix.inverse().f;
	}

	return coords;
}