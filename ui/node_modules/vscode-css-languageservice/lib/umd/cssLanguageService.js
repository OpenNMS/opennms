var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "./parser/cssParser", "./services/cssCompletion", "./services/cssHover", "./services/cssNavigation", "./services/cssCodeActions", "./services/cssValidation", "./parser/scssParser", "./services/scssCompletion", "./parser/lessParser", "./services/lessCompletion", "./services/cssFolding", "./languageFacts/dataManager", "./languageFacts/dataProvider", "./services/cssSelectionRange", "./services/scssNavigation", "./data/webCustomData", "./cssLanguageTypes"], factory);
    }
})(function (require, exports) {
    /*---------------------------------------------------------------------------------------------
     *  Copyright (c) Microsoft Corporation. All rights reserved.
     *  Licensed under the MIT License. See License.txt in the project root for license information.
     *--------------------------------------------------------------------------------------------*/
    'use strict';
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.getLESSLanguageService = exports.getSCSSLanguageService = exports.getCSSLanguageService = exports.newCSSDataProvider = exports.getDefaultCSSDataProvider = void 0;
    var cssParser_1 = require("./parser/cssParser");
    var cssCompletion_1 = require("./services/cssCompletion");
    var cssHover_1 = require("./services/cssHover");
    var cssNavigation_1 = require("./services/cssNavigation");
    var cssCodeActions_1 = require("./services/cssCodeActions");
    var cssValidation_1 = require("./services/cssValidation");
    var scssParser_1 = require("./parser/scssParser");
    var scssCompletion_1 = require("./services/scssCompletion");
    var lessParser_1 = require("./parser/lessParser");
    var lessCompletion_1 = require("./services/lessCompletion");
    var cssFolding_1 = require("./services/cssFolding");
    var dataManager_1 = require("./languageFacts/dataManager");
    var dataProvider_1 = require("./languageFacts/dataProvider");
    var cssSelectionRange_1 = require("./services/cssSelectionRange");
    var scssNavigation_1 = require("./services/scssNavigation");
    var webCustomData_1 = require("./data/webCustomData");
    __exportStar(require("./cssLanguageTypes"), exports);
    function getDefaultCSSDataProvider() {
        return newCSSDataProvider(webCustomData_1.cssData);
    }
    exports.getDefaultCSSDataProvider = getDefaultCSSDataProvider;
    function newCSSDataProvider(data) {
        return new dataProvider_1.CSSDataProvider(data);
    }
    exports.newCSSDataProvider = newCSSDataProvider;
    function createFacade(parser, completion, hover, navigation, codeActions, validation, cssDataManager) {
        return {
            configure: function (settings) {
                validation.configure(settings);
                completion.configure(settings === null || settings === void 0 ? void 0 : settings.completion);
                hover.configure(settings === null || settings === void 0 ? void 0 : settings.hover);
            },
            setDataProviders: cssDataManager.setDataProviders.bind(cssDataManager),
            doValidation: validation.doValidation.bind(validation),
            parseStylesheet: parser.parseStylesheet.bind(parser),
            doComplete: completion.doComplete.bind(completion),
            doComplete2: completion.doComplete2.bind(completion),
            setCompletionParticipants: completion.setCompletionParticipants.bind(completion),
            doHover: hover.doHover.bind(hover),
            findDefinition: navigation.findDefinition.bind(navigation),
            findReferences: navigation.findReferences.bind(navigation),
            findDocumentHighlights: navigation.findDocumentHighlights.bind(navigation),
            findDocumentLinks: navigation.findDocumentLinks.bind(navigation),
            findDocumentLinks2: navigation.findDocumentLinks2.bind(navigation),
            findDocumentSymbols: navigation.findDocumentSymbols.bind(navigation),
            doCodeActions: codeActions.doCodeActions.bind(codeActions),
            doCodeActions2: codeActions.doCodeActions2.bind(codeActions),
            findDocumentColors: navigation.findDocumentColors.bind(navigation),
            getColorPresentations: navigation.getColorPresentations.bind(navigation),
            doRename: navigation.doRename.bind(navigation),
            getFoldingRanges: cssFolding_1.getFoldingRanges,
            getSelectionRanges: cssSelectionRange_1.getSelectionRanges
        };
    }
    var defaultLanguageServiceOptions = {};
    function getCSSLanguageService(options) {
        if (options === void 0) { options = defaultLanguageServiceOptions; }
        var cssDataManager = new dataManager_1.CSSDataManager(options);
        return createFacade(new cssParser_1.Parser(), new cssCompletion_1.CSSCompletion(null, options, cssDataManager), new cssHover_1.CSSHover(options && options.clientCapabilities, cssDataManager), new cssNavigation_1.CSSNavigation(options && options.fileSystemProvider), new cssCodeActions_1.CSSCodeActions(cssDataManager), new cssValidation_1.CSSValidation(cssDataManager), cssDataManager);
    }
    exports.getCSSLanguageService = getCSSLanguageService;
    function getSCSSLanguageService(options) {
        if (options === void 0) { options = defaultLanguageServiceOptions; }
        var cssDataManager = new dataManager_1.CSSDataManager(options);
        return createFacade(new scssParser_1.SCSSParser(), new scssCompletion_1.SCSSCompletion(options, cssDataManager), new cssHover_1.CSSHover(options && options.clientCapabilities, cssDataManager), new scssNavigation_1.SCSSNavigation(options && options.fileSystemProvider), new cssCodeActions_1.CSSCodeActions(cssDataManager), new cssValidation_1.CSSValidation(cssDataManager), cssDataManager);
    }
    exports.getSCSSLanguageService = getSCSSLanguageService;
    function getLESSLanguageService(options) {
        if (options === void 0) { options = defaultLanguageServiceOptions; }
        var cssDataManager = new dataManager_1.CSSDataManager(options);
        return createFacade(new lessParser_1.LESSParser(), new lessCompletion_1.LESSCompletion(options, cssDataManager), new cssHover_1.CSSHover(options && options.clientCapabilities, cssDataManager), new cssNavigation_1.CSSNavigation(options && options.fileSystemProvider), new cssCodeActions_1.CSSCodeActions(cssDataManager), new cssValidation_1.CSSValidation(cssDataManager), cssDataManager);
    }
    exports.getLESSLanguageService = getLESSLanguageService;
});
