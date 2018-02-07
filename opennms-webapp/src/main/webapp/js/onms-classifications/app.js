(function() {
    'use strict';

    var MODULE_NAME = 'onms.classifications';


    angular.module(MODULE_NAME, [ 'angular-loading-bar', 'ngResource', 'ui.router', 'ui.bootstrap', 'ui.checkbox', 'ui.toggle', 'onms.elementList', 'mwl.confirm', 'onms.pagination'])
        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('classifications', {
                    url: '/classifications',
                    controller: 'ClassificationController',
                    templateUrl: 'js/onms-classifications/views/index.html',
                })
                .state('classifications.config', {
                    templateUrl: 'js/onms-classifications/views/config.html',
                    url: '/config',
                    controller: 'ClassificationConfigController'
                })
                .state('classifications.group', {
                    templateUrl: 'js/onms-classifications/views/group.html',
                    url: '/:id',
                    controller: 'ClassificationGroupController'
                });
            $urlRouterProvider.otherwise('classifications/config');
        }])

        .filter('capitalize', function() {
            return function(input) {
                return (!!input) ? input.charAt(0).toUpperCase() + input.substr(1).toLowerCase() : '';
            }
        })

        .factory('ClassificationService', function($resource) {
            return $resource('rest/classifications/', {},
                {
                    'classify': { method: 'POST', url:'rest/classifications/classify'}
                }
            );
        })
        .factory('ClassificationGroupService', function($resource) {
            return $resource('rest/classifications/groups/:id', {id: '@id'},
                {
                    'get': {method: 'GET'},
                    'update': {method: 'PUT'},
                    'query': {method: 'GET', isArray: true},
                    'delete': {method: 'DELETE', params: {id: -1 /*force to -1 to prevent accidentally deleting all groups*/}}
                }
            );
        })
        .factory('ClassificationRuleService', function($resource) {
            return $resource('rest/classifications/:id', {id: '@id'},
                {
                    'get': {method: 'GET'},
                    'save': {method: 'POST'},
                    'update': {method: 'PUT'},
                    'query': {method: 'GET', isArray: true},
                    'delete': {method: 'DELETE'},
                }
            );
        })

        .factory('ProtocolService', function($resource) {
            return $resource('rest/classifications/protocols');
        })
        .controller('ClassificationController', ['$scope', '$state', 'ClassificationService', 'ClassificationGroupService', 'ProtocolService', function($scope, $state, ClassificationService, ClassificationGroupService, ProtocolService) {
            $scope.groups = [];
            $scope.classificationRequest = {}
            $scope.isClassificationCollapsed = true;

            $scope.classify = function(classificationRequest) {
                ClassificationService.classify(classificationRequest, function(result) {
                    $scope.classificationResponse = result.classification === undefined ? 'No mapping found' : result.classification;
                }, function(response) {
                    console.log(response);
                    if (response.status === 400 && response.data && response.data.message) {
                        $scope.error = response.data.message;
                    } else {
                        $scope.error = "Cannot perform the request.";
                    }
                });
            };

            $scope.refreshTabs = function(navigateToFirstGroup) {
                return ClassificationGroupService.query({}, function(response) {
                    // Remove disabled groups
                    $scope.groups = response.filter(function(group) {
                        return group.enabled === true
                    });
                    // Sort by priority (highest first)
                    $scope.groups = $scope.groups.sort(function(l, r) {
                        return l.priority - r.priority;
                    });
                    $scope.groups = $scope.groups.reverse();

                    // Select first group if available
                    if (navigateToFirstGroup && $scope.groups.length !== 0) {
                        $state.go('classifications.group', {id: $scope.groups[0].id});
                    }
                });
            };

            $scope.loadProtocols = function() {
                ProtocolService.query(function(response, headers) {
                    $scope.protocols = response;
                });
            };

            $scope.fullyDefined = function() {
                return $scope.classificationRequest && $scope.classificationRequest.protocol && $scope.classificationRequest.port && $scope.classificationRequest.ipAddress;
            };

            $scope.loadProtocols();
            $scope.refreshTabs(true);

        }])
        .controller('ClassificationConfigController', ['$scope', '$rootScope', '$location', '$log', 'ClassificationGroupService', function($scope, $rootScope, $location, $log, ClassificationGroupService) {
            $scope.groups = [];
            $scope.query = {
                page: 1,
                limit: 20,
                totalItems: 0,
                orderBy: 'priority',
                order: 'desc'
            };
            $scope.updateGroup = function(group) {
                group.$update({}, function() {
                    $scope.refreshTabs();
                });
            };
            $scope.refresh = function() {
                ClassificationGroupService.query({}, function(result, headers) {
                    $scope.groups = result;
                    var contentRange = parseContentRange(headers('Content-Range'));
                    $scope.query.totalItems = contentRange.total;
                });
            };
            $scope.refresh();
        }])
        .controller('ClassificationGroupController', ['$scope', '$stateParams', '$uibModal', '$log', 'ClassificationRuleService', 'ClassificationGroupService', function($scope, $stateParams, $uibModal, $log, ClassificationRuleService, ClassificationGroupService) {
            // Defaults
            $scope.rules = [];
            $scope.query = {
                page: 1,
                limit: 20,
                totalItems: 0,
                orderBy: 'position',
                order: 'asc',
                groups: [$stateParams.id],
                search: undefined
            };
            $scope.findGroup = function(groupId) {
                return ClassificationGroupService.get({id: groupId}, function(response) {
                    $scope.group = response;
                    $scope.csv_link = 'rest/classifications/groups/' + $scope.group.id;
                    $scope.refresh();
                });
            };

            $scope.refresh = function() {
                var parameters = $scope.query || {};
                return ClassificationRuleService.query( {
                    limit: parameters.limit || 20,
                    offset: (parameters.page -1) * parameters.limit || 0,
                    orderBy: parameters.orderBy,
                    order: parameters.order,
                    groupFilter: parameters.groups || [],
                    query: parameters.search
                }, function (result, headers) {
                    $scope.rules = result;
                    var contentRange = parseContentRange(headers('Content-Range'));
                    $scope.query.totalItems = contentRange.total;
                });
            };

            $scope.changeOrderBy = function(column) {
                if ($scope.query.orderBy === column) {
                    $scope.query.order = $scope.query.order === 'asc' ? 'desc' : 'asc';
                } else {
                    $scope.query.orderBy = column;
                    $scope.query.order = 'asc';
                }
                $scope.refresh();
            };

            $scope.deleteRule = function(rule) {
                rule.$delete().then(function() {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };

            $scope.deleteAllRules = function() {
                ClassificationRuleService.delete({groupId: $scope.group.id}, function() {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };

            var openModal = function(classification) {
                return $uibModal.open({
                    backdrop: false,
                    controller: 'ClassificationModalController',
                    templateUrl: 'js/onms-classifications/views/modals/new-rule-modal.html',
                    size: 'md',
                    resolve: {
                        classification: function() {
                            return classification;
                        }
                    }
                });
            };

            $scope.editRule = function(rule) {
                var modalInstance = openModal(rule);
                modalInstance.result.then(function () {
                    $scope.refreshTabs();
                    $scope.refresh();
                }, function() {
                    // modal was dismissed
                    $scope.refresh();
                });
            };

            $scope.addRule = function() {
                var modalInstance = openModal();
                modalInstance.result.then(function () {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };

            $scope.importRules = function() {
                var modalInstance = $uibModal.open({
                    backdrop: false,
                    controller: 'ClassificationImportController',
                    templateUrl: 'js/onms-classifications/views/modals/import-modal.html',
                    resolve: {
                        group: function () {
                            return $scope.group;
                        }
                    }
                });
                modalInstance.result.then(function () {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };

            $scope.clearSearch = function() {
                $scope.query.search = undefined;
                $scope.refresh();
            };

            $scope.findGroup($stateParams.id);
        }])
        .controller('ClassificationImportController', ['$scope', '$http', '$uibModalInstance', 'group', function($scope, $http, $uibModalInstance, group) {
            $scope.group = group;
            $scope.setFile = function(element) {
                $scope.$apply(function(scope) {
                    scope.fileToUpload = element.files[0];
                });
            };
            $scope.openFileDialogue = function() {
                setTimeout(function() {
                    document.getElementById('fileToImport').click();
                }, 0);
            };
            $scope.resetInput = function() {
                $scope.fileToUpload = undefined;
                $scope.error = undefined;
                $scope.failedRows = [];
                $scope.containsHeader = true;
                $scope.confirmImport = false;
                $scope.pagination = {
                    page: 1,
                    totalItems: 0,
                    limit: 5
                };
            };

            $scope.navigateWithinErrors = function() {
                var limit = $scope.pagination.limit;
                var offset = ($scope.pagination.page - 1) * limit;
                var endIndex = Math.min(offset + limit, $scope.errors.length);
                $scope.failedRows = $scope.errors.slice(offset, endIndex);
            };

            $scope.uploadFile = function() {
                var reader = new FileReader();
                reader.onload = function(e) {
                    $scope.error = undefined;
                    $scope.errors = [];
                    $scope.failedRows = [];
                    $http({
                        url: 'rest/classifications',
                        method: "POST",
                        data: reader.result,
                        params: {"hasHeader": $scope.containsHeader},
                        headers: {'Content-Type': 'text/comma-separated-values'}
                    }).success(function (response) {
                        $uibModalInstance.close();
                    }).error(function (response, status) {
                        if (status === 500) {
                            $scope.error = "An unexpected error occurred.";
                        }
                        if (status === 400) {
                            // General error
                            if (response.error) {
                                $scope.error = response.error.message;
                                return;
                            }
                            // Report failed rows
                            if (response.errors) {
                                $scope.error = "The rules could not be imported. Please fix the errors shown below and retry.";
                                // Persist locally
                                var errorKeys = Object.getOwnPropertyNames(response.errors);
                                for (var i = 0; i<errorKeys.length; i++) {
                                    var index = errorKeys[i];
                                    var rowIndex = $scope.containsHeader ? parseInt(index) + 1 : index; // increase row index if csv contains header
                                    $scope.errors.push({index: rowIndex, message: response.errors[index].message});
                                }
                                // Update pagination settings
                                $scope.pagination.totalItems = $scope.errors.length;
                                $scope.navigateWithinErrors();
                            }
                        }
                    });
                };
                reader.readAsText($scope.fileToUpload);
            };
            $scope.resetInput();
        }])
        .controller('ClassificationModalController', ['$scope', '$uibModalInstance', 'ProtocolService', 'ClassificationRuleService', 'classification', function($scope, $uibModalInstance, ProtocolService, ClassificationRuleService, classification) {
            $scope.classification = classification || {};
            $scope.protocols = [];
            $scope.currentSelection = undefined;
            $scope.selectedProtocols = [];
            $scope.buttonName = $scope.classification.id ? "Update" : "Create";

            var convertStringArrayToProtocolsArray = function(string) {
                return string.map(function(protocol) {
                    return {keyword: protocol};
                })
            };

            var convertProtocolsArrayToStringArray = function(protocols) {
                return protocols.map(function(protocol) {
                    return protocol.keyword;
                });
            };

            var handleErrorResponse = function(response) {
                if (response && response.data) {
                    var error = response.data;
                    $scope.error = {};
                    $scope.error[error.context.toLowerCase()] = error.message;
                }
            };

            $scope.save = function() {
                // Close modal afterwards
                var closeCallback = function() {
                    $uibModalInstance.close();
                };
                $scope.classification.protocols = convertProtocolsArrayToStringArray($scope.selectedProtocols);
                if ($scope.classification.id) {
                    ClassificationRuleService.update($scope.classification, closeCallback, handleErrorResponse);
                } else {
                    ClassificationRuleService.save($scope.classification, closeCallback, handleErrorResponse);
                }
            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('Cancelled by User');
            };

            $scope.insertProtocol = function(item) {
                if (item && $scope.selectedProtocols.indexOf(item) == -1) {
                    $scope.currentSelection = "";
                    $scope.selectedProtocols.push(item);
                }
            };

            $scope.removeProtocol = function(protocol) {
                var index = $scope.selectedProtocols.indexOf(protocol);
                if (index !== -1) {
                    $scope.selectedProtocols.splice(index, 1);
                }
            };

            $scope.loadProtocols = function() {
                ProtocolService.query(function(response, headers) {
                    $scope.protocols = response;
                });
            };

            if ($scope.classification.id) {
                $scope.selectedProtocols = convertStringArrayToProtocolsArray($scope.classification.protocols);
            }

            $scope.loadProtocols();

        }])
    ;
}());
