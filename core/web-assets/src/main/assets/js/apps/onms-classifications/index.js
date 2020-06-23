const angular = require('vendor/angular-js');
const elementList = require('../onms-elementList/lib/elementList');
require('../../lib/onms-pagination');
require('../../lib/onms-http');
require('angular-bootstrap-confirm');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');
require('angular-ui-sortable');

const indexTemplate  = require('./views/index.html');
const configTemplate = require('./views/config.html');
const groupTemplate  = require('./views/group.html');

const newRuleModalTemplate = require('./views/modals/new-rule-modal.html');
const newGroupModalTemplate = require('./views/modals/new-group-modal.html');
const importModalTemplate  = require('./views/modals/import-modal.html');
const exportModalTemplate  = require('./views/modals/export-modal.html');

const confirmTopoverTemplate = require('./views/modals/popover.html');

const handleErrorResponse = function(response, $scope) {
    if (response && response.data) {
        var error = response.data;
        $scope.error = {};
        $scope.error[error.context] = error.message;
    }
};

(function() {
    'use strict';

    var MODULE_NAME = 'onms.classifications';

    angular.module(MODULE_NAME, [
            'angular-loading-bar',
            'ngResource',
            'ui.router',
            'ui.bootstrap',
            'ui.checkbox',
            'ui.toggle',
            'ui.sortable',
            'onms.http',
            'onms.elementList',
            'mwl.confirm',
            'onms.pagination'
        ])
        .run(function(confirmationPopoverDefaults) {
            confirmationPopoverDefaults.templateUrl = confirmTopoverTemplate;
        })

        .config( ['$locationProvider', function ($locationProvider) {
            $locationProvider.hashPrefix('!');
            $locationProvider.html5Mode(false);
        }])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('classifications', {
                    url: '/classifications',
                    controller: 'ClassificationController',
                    templateUrl: indexTemplate
                })
                .state('classifications.config', {
                    templateUrl: configTemplate,
                    url: '/config',
                    controller: 'ClassificationConfigController'
                })
                .state('classifications.group', {
                    templateUrl: groupTemplate,
                    url: '/:id',
                    controller: 'ClassificationGroupController'
                });
            $urlRouterProvider.otherwise('classifications/config');
        }])

        .filter('capitalize', function() {
            return function(input) {
                return input ? input.charAt(0).toUpperCase() + input.substr(1).toLowerCase() : '';
            }
        })

        .factory('ClassificationService', /* @ngInject */ function($resource) {
            return $resource('rest/classifications/', {},
                {
                    'classify': { method: 'POST', url:'rest/classifications/classify'}
                }
            );
        })
        .factory('ClassificationGroupService', /* @ngInject */ function($resource) {
            return $resource('rest/classifications/groups/:id', {id: '@id'},
                {
                    'get': {method: 'GET'},
                    'update': {method: 'PUT'},
                    'query': {method: 'GET', isArray: true},
                    'delete': {method: 'DELETE'},
                }
            );
        })
        .factory('ClassificationRuleService', /* @ngInject */ function($resource) {
            return $resource('rest/classifications/:id', {id: '@id'},
                {
                    'get': {method: 'GET'},
                    'save': {method: 'POST'},
                    'update': {method: 'PUT'},
                    'query': {method: 'GET', isArray: true},
                    'delete': {method: 'DELETE'}
                }
            );
        })

        .factory('ProtocolService', /* @ngInject */ function($resource) {
            return $resource('rest/classifications/protocols');
        })
        .controller('ClassificationController', ['$scope', '$state', 'ClassificationService', 'ClassificationGroupService', 'ProtocolService', function($scope, $state, ClassificationService, ClassificationGroupService, ProtocolService) {
            $scope.groups = [];
            $scope.classificationRequest = {};
            $scope.classificationResponse = undefined;
            $scope.isClassificationCollapsed = true;
            $scope.error = undefined;

            $scope.classify = function (classificationRequest) {
                ClassificationService.classify(classificationRequest, function (result) {
                    $scope.classifyError = undefined;
                    $scope.classificationResponse = result.classification === undefined ? 'No mapping found' : result.classification;
                }, function (response) {
                    $scope.classificationResponse = undefined;
                    if (response.status === 400 && response.data && response.data.context && response.data.message) {
                        $scope.classifyError = {};
                        $scope.classifyError[response.data.context] = response.data.message;
                    } else {
                        $scope.classifyError['entity'] = 'Cannot perform the request.';
                    }
                });
            };

            $scope.refreshTabs = function(navigateToFirstGroup) {
                return ClassificationGroupService.query({
                    limit: 1000, // override default limit (we want to show as many groups as possible)
                    orderBy: 'position',
                }, function(response) {
                    // Remove disabled groups
                    $scope.groups = response.filter(function(group) {
                        return group.enabled === true
                    });
                    // Sort by position (lowest first)
                    $scope.groups = $scope.groups.sort(function(l, r) {
                        return r.position - l.position;
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
                var fullyDefined = $scope.classificationRequest
                    && $scope.classificationRequest.protocol
                    && $scope.classificationRequest.dstPort && $scope.classificationRequest.dstAddress
                    && $scope.classificationRequest.srcPort && $scope.classificationRequest.srcAddress
                    && $scope.classificationRequest.exporterAddress;
                return fullyDefined;
            };

            $scope.toggleClassificationMode = function() {
                $scope.classificationMode = ($scope.classificationMode === 'simple' ? 'complex' : 'simple');
            };

            $scope.loadProtocols();
            $scope.refreshTabs(true);

        }])
        .controller('ClassificationConfigController', ['$scope', '$rootScope', '$uibModal', '$location', '$log', 'ClassificationGroupService', function($scope, $rootScope, $uibModal, $location, $log, ClassificationGroupService) {
            $scope.groups = [];
            $scope.query = {
                page: 1,
                limit: 20,
                totalItems: 0,
            };
            $scope.updateGroup = function(group) {
                group.$update({}, function() {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };
            $scope.deleteGroup = function(group) {
                group.$delete().then(function() {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };
            var openModal = function(group) {
                return $uibModal.open({
                    backdrop: false,
                    controller: 'GroupModalController',
                    templateUrl: newGroupModalTemplate,
                    size: 'lg',
                    resolve: {
                        group: function() {
                            return group;
                        },
                        groups: function () {
                            return $scope.groups;
                        },
                        groupsTotalAmount: function () {
                            return $scope.query.totalItems;
                        }
                    }
                });
            };
            $scope.editGroup = function(group) {
                var modalInstance = openModal(group);
                modalInstance.closed.then(function () {
                    $scope.refreshTabs();
                    $scope.refresh();
                }, function() {
                    // modal was dismissed
                    $scope.refresh();
                });
            };
            $scope.addGroup = function(group) {
                var modalInstance = openModal(group);
                modalInstance.closed.then(function () {
                    $scope.refreshTabs();
                    $scope.refresh();
                });
            };
            $scope.refresh = function() {
                var parameters = $scope.query || {};
                ClassificationGroupService.query({
                    limit: 20,
                    offset: (parameters.page -1) * parameters.limit || 0,
                    orderBy: 'position',
                    order: 'asc'
                }, function(result, headers) {
                    $scope.groups = result;
                    var contentRange = elementList.parseContentRange(headers('Content-Range'));
                    $scope.query.totalItems = contentRange.total;
                });
            };
            // for drag and drop of groups (redefining position)
            $scope.sortableGroups = {
                start: function(e, ui) {
                    // remember old index before moving
                    angular.element(ui.item).data('oldIndex', ui.item.index());
                },
                stop: function(e, ui) {

                    // Check Precondition:  item was actually moved
                    var oldIndex =  angular.element(ui.item).data().oldIndex;
                    var newIndex =  ui.item.index();
                    if(oldIndex !== newIndex) {
                        // Calculate and set new position (index + offset)
                        var parameters = $scope.query || {};
                        var offset = (parameters.page - 1) * parameters.limit || 0;
                        var group = $scope.groups[newIndex];
                        var position;
                        if (newIndex - 1 < 0) {
                            // we are already at the beginning of the visible paged list
                            position = offset;
                        } else {
                            var previousGroup = $scope.groups[newIndex - 1];
                            position = (newIndex > oldIndex) ? previousGroup.position : previousGroup.position + 1;
                        }
                        group.position = position;

                        // Update backend
                        var refreshCallback = function () {
                            $scope.refreshTabs();
                            $scope.refresh();
                        };
                        var errorCallback = function(response) {
                            handleErrorResponse(response, $scope);
                        };

                        ClassificationGroupService.update(group, refreshCallback, errorCallback);
                    }
                },
                items: "tr:not(.unsortable)"
            };
            $scope.refresh();
        }])
        .controller('ClassificationGroupController', ['$scope', '$stateParams', '$uibModal', '$log', '$http', '$window','ClassificationRuleService', 'ClassificationGroupService', function($scope, $stateParams, $uibModal, $log, $http, $window,ClassificationRuleService, ClassificationGroupService) {
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
                    $scope.refresh();
                });
            };

            $scope.showExportRulesDialog = function() {
                var modalInstance = $uibModal.open({
                    backdrop: false,
                    controller: 'ClassificationExportController',
                    templateUrl: exportModalTemplate,
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

            $scope.refresh = function() {
                var parameters = $scope.query || {};
                var editPositionOfRuleEnabled = !($scope.group.readOnly) && ($scope.query.orderBy === 'position' && $scope.query.order === 'asc');
                var sortable =  angular.element( '.ui-sortable' );
                if(editPositionOfRuleEnabled === true) {
                    sortable.sortable('enable');
                } else {
                    sortable.sortable('disable');
                }
                return ClassificationRuleService.query( {
                    limit: parameters.limit || 20,
                    offset: (parameters.page -1) * parameters.limit || 0,
                    orderBy: parameters.orderBy,
                    order: parameters.order,
                    groupFilter: parameters.groups || [],
                    query: parameters.search
                }, function (result, headers) {
                    $scope.rules = result;
                    var contentRange = elementList.parseContentRange(headers('Content-Range'));
                    $scope.query.totalItems = contentRange.total;
                });
            };

            // In some cases the currently selected group needs to be refreshed, this method finds the group from
            // $scope.groups and updates $scope.group accordingly, in order to reflect updates in $scope.groups
            $scope.refreshGroup = function() {
                for (var i = 0; i<$scope.groups.length; i++) {
                    var group = $scope.groups[i];
                    if (group.id === $scope.group.id) {
                        $scope.group = group;
                        return;
                    }
                }
            };

            $scope.refreshAll = function() {
                var result = $scope.refreshTabs();
                result.$promise.then(function() {
                    $scope.refreshGroup()
                    $scope.refresh();
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
                    $scope.refreshAll();
                });
            };

            $scope.deleteAllRules = function() {
                ClassificationRuleService.delete({groupId: $scope.group.id}, function() {
                    $scope.refreshAll();
                });
            };

            var openModal = function(classification, group) {
                return $uibModal.open({
                    backdrop: false,
                    controller: 'ClassificationModalController',
                    templateUrl: newRuleModalTemplate,
                    size: 'lg',
                    resolve: {
                        classification: function() {
                            return classification;
                        },
                        group: function() {
                            return group;
                        },
                        groups: function () {
                            return $scope.groups;
                        }
                    }
                });
            };

            $scope.editRule = function(rule) {
                var modalInstance = openModal(rule, rule.group);
                modalInstance.closed.then(function () {
                    $scope.refreshAll();
                }, function() {
                    // modal was dismissed
                    $scope.refresh();
                });
            };

            $scope.addRule = function(group) {
                var modalInstance = openModal(null, group);
                modalInstance.closed.then(function () {
                    $scope.refreshAll();
                });
            };

            // for drag and drop of rules (redefining position)
            $scope.sortableRules = {
                start: function(e, ui) {
                    // remember old index before moving
                    angular.element(ui.item).data('oldIndex', ui.item.index());
                },
                stop: function(e, ui) {
                    // Check Precondition: item was actually moved
                    var oldIndex =  angular.element(ui.item).data().oldIndex;
                    var newIndex =  ui.item.index();
                    if(oldIndex !== newIndex) {
                        // Calculate and set new position (index + offset)
                        var parameters = $scope.query || {};
                        var offset = (parameters.page - 1) * parameters.limit || 0;
                        var rule = $scope.rules[newIndex];
                        var position;
                        if (newIndex - 1 < 0) {
                            // we are already at the beginning of the visible paged list
                            position = offset;
                        } else {
                            var previousRule = $scope.rules[newIndex - 1];
                            position = (newIndex > oldIndex) ? previousRule.position : previousRule.position + 1;
                        }
                        rule.position = position;

                        // Update backend
                        var refreshCallback = function () {
                            $scope.refreshAll();
                        };
                        var errorCallback = function(response) {
                            handleErrorResponse(response, $scope);
                        };

                        ClassificationRuleService.update(rule, refreshCallback, errorCallback);
                    }
                }
            };

            $scope.importRules = function() {
                var modalInstance = $uibModal.open({
                    size: 'lg',
                    backdrop: false,
                    controller: 'ClassificationImportController',
                    templateUrl: importModalTemplate,
                    resolve: {
                        group: function () {
                            return $scope.group;
                        }
                    }
                });
                modalInstance.closed.then(function () {
                    $scope.refreshAll();
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
                $scope.deleteExistingRules = false;
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
                        url: 'rest/classifications/groups/'+group.id,
                        method: 'POST',
                        data: reader.result,
                        params: {'hasHeader': $scope.containsHeader, 'deleteExistingRules' : $scope.deleteExistingRules},
                        headers: {'Content-Type': 'text/comma-separated-values'}
                    }).success(function (response) {
                        $uibModalInstance.close();
                    }).error(function (response, status) {
                        if (status === 500) {
                            $scope.error = 'An unexpected error occurred.';
                        }
                        if (status === 400) {
                            // General error
                            if (response.error) {
                                $scope.error = response.error.message;
                                return;
                            }
                            // Report failed rows
                            if (response.errors) {
                                $scope.error = 'The rules could not be imported. Please fix the errors shown below and retry.';
                                // Persist locally
                                var errorKeys = Object.getOwnPropertyNames(response.errors);
                                for (var i = 0; i<errorKeys.length; i++) {
                                    var index = errorKeys[i];
                                    var rowIndex = $scope.containsHeader ? parseInt(index, 10) + 1 : index; // increase row index if csv contains header
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
        .controller('ClassificationExportController', ['$scope', '$http', '$uibModalInstance', 'group', '$window',
            function($scope, $http, $uibModalInstance, group, $window) {
                $scope.group = group;
                $scope.export = {};
                $scope.export.requestedFileName = group.name + '_rules.csv';
                $scope.exportGroup = function() {
                    var requestedFileName = $scope.export.requestedFileName.trim();
                    $window.location = 'rest/classifications/groups/' + $scope.group.id +'?filename='
                        +requestedFileName+'&format=csv';
                    $uibModalInstance.close();
            };

        }])
        .controller('ClassificationModalController', ['$scope', '$uibModalInstance', 'ProtocolService', 'ClassificationRuleService', 'classification', 'group', 'groups', function($scope, $uibModalInstance, ProtocolService, ClassificationRuleService, classification, group, groups) {
            $scope.classification = classification || {group:group};
            $scope.protocols = [];
            $scope.currentSelection = undefined;
            $scope.selectedProtocols = [];
            $scope.buttonName = $scope.classification.id ? 'Update' : 'Create';
            $scope.group = group;
            $scope.maxPosition = (classification === null) ? group.ruleCount : group.ruleCount-1;
            $scope.selectableGroups = groups.filter((group) => group.readOnly === false);

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

            $scope.save = function() {
                // Close modal afterwards
                var closeCallback = function() {
                    $uibModalInstance.close();
                };
                var errorCallback = function(response) {
                    handleErrorResponse(response, $scope);
                };
                $scope.classification.protocols = convertProtocolsArrayToStringArray($scope.selectedProtocols);
                if ($scope.classification.id) {
                    ClassificationRuleService.update($scope.classification, closeCallback, errorCallback);
                } else {
                    ClassificationRuleService.save($scope.classification, closeCallback, errorCallback);
                }
            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('Cancelled by User');
            };

            $scope.insertProtocol = function(item) {
                if (item && $scope.selectedProtocols.indexOf(item) === -1) {
                    $scope.currentSelection = '';
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
        .controller('GroupModalController', ['$scope', '$uibModalInstance', 'ClassificationGroupService', 'group', 'groups', 'groupsTotalAmount', function($scope, $uibModalInstance, ClassificationGroupService, group, groups, groupsTotalAmount) {
            $scope.group = group || {enabled:true};
            $scope.currentSelection = undefined;
            $scope.buttonName = $scope.group.id ? 'Update' : 'Create';
            $scope.groups = groups;
            $scope.groupsTotalAmount = groupsTotalAmount;
            $scope.maxPosition = (group === undefined) ? groupsTotalAmount-1 : groupsTotalAmount-2; // pre-defined group has always the last position

            $scope.save = function() {
                // Close modal afterwards
                var closeCallback = function() {
                    $uibModalInstance.close();
                };
                var errorCallback = function(response) {
                    handleErrorResponse(response, $scope);
                };
                if ($scope.group.id) {
                    ClassificationGroupService.update($scope.group, closeCallback, errorCallback);
                } else {
                    ClassificationGroupService.save($scope.group, closeCallback, errorCallback);
                }
            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('Cancelled by User');
            };
        }])
    ;
}());
