	function getBaseHref() {
		return document.getElementsByTagName('base')[0].href;
	}

	function setLocation(url) {
		window.location.href = getBaseHref() + url;
	}
	
	// Show the progress bar
	function getProgressBar(){
		showTransBackground();
		$("#progressBar").show();
	}
	
	// Hide progress bar for export action
	function hideProgressBar(){
		$("#progressBar").hide();
		hideTransBackground();
	}
	
	// Popup for export action
	function showPopup(confirmText) {
		showTransBackground();
		$("#alertText").text(confirmText);
		$("#exportConfirmation").show();
	}
	
	// Close the export popup
	function hideTransBackground() {
		$("#exportConfirmation").hide();
		$("#backgroundPopup").hide();
		$("body").css("overflow-y","scroll");
	}
	
	// Call the export action
	function callExportAction() {
		var timerId = 0;
		var radios = document.getElementsByName("format");
		for (var i = 0; i < radios.length; i++) {       
			if (radios[i].checked) {
	                    document.alarm_action_form.format.value = radios[i].value;
			    if(radios[i].value == "CSV") {
				document.alarm_action_form.reportId.value = "local_alarm-report-csv";
			    }else{
				document.alarm_action_form.reportId.value = "local_alarm-report";
			    }
			    break;
			}
		}
		hideTransBackground();
		document.alarm_action_form.submit();
		//getProgressBar();
		//timerId = setInterval(function(){toCheck()},1000);
	}
	
	// Display the transparent background
	function showTransBackground(){
		$("#backgroundPopup").css("opacity", "0.3");
		$("#backgroundPopup").fadeIn(0001); 
		$("body").css("overflow-y","hidden");
	}
	// Call event export action
	function callEventExportAction() {
		var radios = document.getElementsByName("format");
		for (var i = 0; i < radios.length; i++) {       
			if (radios[i].checked) {
			    document.acknowledge_form.format.value = radios[i].value;
                           if(radios[i].value == "CSV") {
				document.acknowledge_form.reportId.value = "local_event-report-csv";
			    }else{
				document.acknowledge_form.reportId.value = "local_event-report";
			    }
			    break;
			}
		}
	hideTransBackground();
	document.acknowledge_form.submit();
	//getProgressBar();
	//timerId = setInterval(function(){exportStatus()},1000);
	}
