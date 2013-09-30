'use strict';

var app = angular.module('RequisitionEditor',['ngResource']);
// provisioningGroupName is a global set in the script via the jsp
app
.value('groupName', provisioningGroupName)
.factory('Requisition', function($resource) {
        return $resource('/opennms/rest/requisitions/:groupName');
})
.controller("MainController", function($scope, groupName, Requisition){
	$scope.requisition = Requisition.get({groupName: groupName}, function(req) {
		console.log(req);
	}, function(err) {
		console.log("Error");
		console.log(err);
	});
	
	var edit = function(node) {
		node.editting = true;
	};
	
	
	$scope.edit = edit;
	
	console.log($scope);
});