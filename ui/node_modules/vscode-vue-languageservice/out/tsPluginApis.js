"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.register = void 0;
const shared = require("@volar/shared");
function register({ sourceFiles, scriptTsLsRaw, templateTsLsRaw }) {
    return {
        getCompletionsAtPosition,
        getDefinitionAtPosition,
        getDefinitionAndBoundSpan,
        getTypeDefinitionAtPosition,
        getImplementationAtPosition,
        findRenameLocations,
        getReferencesAtPosition,
        findReferences,
    };
    // apis
    function getCompletionsAtPosition(fileName, position, options) {
        const finalResult = scriptTsLsRaw.getCompletionsAtPosition(fileName, position, options);
        if (finalResult) {
            finalResult.entries = finalResult.entries.filter(entry => entry.name.indexOf('__VLS_') === -1);
        }
        return finalResult;
    }
    function getReferencesAtPosition(fileName, position) {
        return findLocations(['script', 'template'], fileName, position, 'references');
    }
    function getDefinitionAtPosition(fileName, position) {
        return findLocations(['script'], fileName, position, 'definition');
    }
    function getTypeDefinitionAtPosition(fileName, position) {
        return findLocations(['script'], fileName, position, 'typeDefinition');
    }
    function getImplementationAtPosition(fileName, position) {
        return findLocations(['script', 'template'], fileName, position, 'implementation');
    }
    function findRenameLocations(fileName, position, findInStrings, findInComments, providePrefixAndSuffixTextForRename) {
        return findLocations(['script', 'template'], fileName, position, 'rename', findInStrings, findInComments, providePrefixAndSuffixTextForRename);
    }
    function findLocations(lsTypes, fileName, position, mode, findInStrings = false, findInComments = false, providePrefixAndSuffixTextForRename) {
        return lsTypes.map(lsType => worker(lsType)).flat();
        function worker(lsType) {
            const tsLs = lsType === 'script' ? scriptTsLsRaw : templateTsLsRaw;
            const loopChecker = new Set();
            let symbols = [];
            withTeleports(fileName, position);
            return symbols.map(s => transformDocumentSpanLike(lsType, s)).filter(shared.notEmpty);
            function withTeleports(fileName, position) {
                if (loopChecker.has(fileName + ':' + position))
                    return;
                loopChecker.add(fileName + ':' + position);
                const _symbols = mode === 'definition' ? tsLs.getDefinitionAtPosition(fileName, position)
                    : mode === 'typeDefinition' ? tsLs.getTypeDefinitionAtPosition(fileName, position)
                        : mode === 'references' ? tsLs.getReferencesAtPosition(fileName, position)
                            : mode === 'implementation' ? tsLs.getImplementationAtPosition(fileName, position)
                                : mode === 'rename' ? tsLs.findRenameLocations(fileName, position, findInStrings, findInComments, providePrefixAndSuffixTextForRename)
                                    : undefined;
                if (!_symbols)
                    return;
                symbols = symbols.concat(_symbols);
                for (const ref of _symbols) {
                    loopChecker.add(ref.fileName + ':' + ref.textSpan.start);
                    const teleport = sourceFiles.getTsTeleports(lsType).get(shared.fsPathToUri(ref.fileName));
                    if (!teleport)
                        continue;
                    if (!teleport.allowCrossFile
                        && sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(ref.fileName)) !== sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(fileName)))
                        continue;
                    for (const teleRange of teleport.findTeleports2(ref.textSpan.start, ref.textSpan.start + ref.textSpan.length)) {
                        if ((mode === 'definition' || mode === 'typeDefinition' || mode === 'implementation') && !teleRange.sideData.capabilities.definitions)
                            continue;
                        if ((mode === 'references') && !teleRange.sideData.capabilities.references)
                            continue;
                        if ((mode === 'rename') && !teleRange.sideData.capabilities.rename)
                            continue;
                        if (loopChecker.has(ref.fileName + ':' + teleRange.start))
                            continue;
                        withTeleports(ref.fileName, teleRange.start);
                    }
                }
            }
        }
    }
    function getDefinitionAndBoundSpan(fileName, position) {
        return worker('script');
        function worker(lsType) {
            const tsLs = lsType === 'script' ? scriptTsLsRaw : templateTsLsRaw;
            const loopChecker = new Set();
            let textSpan;
            let symbols = [];
            withTeleports(fileName, position);
            if (!textSpan)
                return;
            return {
                textSpan: textSpan,
                definitions: symbols === null || symbols === void 0 ? void 0 : symbols.map(s => transformDocumentSpanLike(lsType, s)).filter(shared.notEmpty),
            };
            function withTeleports(fileName, position) {
                if (loopChecker.has(fileName + ':' + position))
                    return;
                loopChecker.add(fileName + ':' + position);
                const _symbols = tsLs.getDefinitionAndBoundSpan(fileName, position);
                if (!_symbols)
                    return;
                if (!textSpan) {
                    textSpan = _symbols.textSpan;
                }
                if (!_symbols.definitions)
                    return;
                symbols = symbols.concat(_symbols.definitions);
                for (const ref of _symbols.definitions) {
                    loopChecker.add(ref.fileName + ':' + ref.textSpan.start);
                    const teleport = sourceFiles.getTsTeleports(lsType).get(shared.fsPathToUri(ref.fileName));
                    if (!teleport)
                        continue;
                    if (!teleport.allowCrossFile
                        && sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(ref.fileName)) !== sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(fileName)))
                        continue;
                    for (const teleRange of teleport.findTeleports2(ref.textSpan.start, ref.textSpan.start + ref.textSpan.length)) {
                        if (!teleRange.sideData.capabilities.definitions)
                            continue;
                        if (loopChecker.has(ref.fileName + ':' + teleRange.start))
                            continue;
                        withTeleports(ref.fileName, teleRange.start);
                    }
                }
            }
        }
    }
    function findReferences(fileName, position) {
        const scriptResult = worker('script');
        const templateResult = worker('template');
        return [
            ...scriptResult,
            ...templateResult,
        ];
        function worker(lsType) {
            const tsLs = lsType === 'script' ? scriptTsLsRaw : templateTsLsRaw;
            const loopChecker = new Set();
            let symbols = [];
            withTeleports(fileName, position);
            return symbols.map(s => transformReferencedSymbol(lsType, s)).filter(shared.notEmpty);
            function withTeleports(fileName, position) {
                if (loopChecker.has(fileName + ':' + position))
                    return;
                loopChecker.add(fileName + ':' + position);
                const _symbols = tsLs.findReferences(fileName, position);
                if (!_symbols)
                    return;
                symbols = symbols.concat(_symbols);
                for (const symbol of _symbols) {
                    for (const ref of symbol.references) {
                        loopChecker.add(ref.fileName + ':' + ref.textSpan.start);
                        const teleport = sourceFiles.getTsTeleports(lsType).get(shared.fsPathToUri(ref.fileName));
                        if (!teleport)
                            continue;
                        if (!teleport.allowCrossFile
                            && sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(ref.fileName)) !== sourceFiles.getSourceFileByTsUri(lsType, shared.fsPathToUri(fileName)))
                            continue;
                        for (const teleRange of teleport.findTeleports2(ref.textSpan.start, ref.textSpan.start + ref.textSpan.length)) {
                            if (!teleRange.sideData.capabilities.references)
                                continue;
                            if (loopChecker.has(ref.fileName + ':' + teleRange.start))
                                continue;
                            withTeleports(ref.fileName, teleRange.start);
                        }
                    }
                }
            }
        }
    }
    // transforms
    function transformReferencedSymbol(lsType, symbol) {
        const definition = transformDocumentSpanLike(lsType, symbol.definition);
        const references = symbol.references.map(r => transformDocumentSpanLike(lsType, r)).filter(shared.notEmpty);
        if (definition) {
            return {
                definition,
                references,
            };
        }
        else if (references.length) { // TODO: remove patching
            return {
                definition: {
                    ...symbol.definition,
                    fileName: references[0].fileName,
                    textSpan: references[0].textSpan,
                },
                references,
            };
        }
    }
    function transformDocumentSpanLike(lsType, documentSpan) {
        const textSpan = transformSpan(lsType, documentSpan.fileName, documentSpan.textSpan);
        if (!textSpan)
            return;
        const contextSpan = transformSpan(lsType, documentSpan.fileName, documentSpan.contextSpan);
        const originalTextSpan = transformSpan(lsType, documentSpan.originalFileName, documentSpan.originalTextSpan);
        const originalContextSpan = transformSpan(lsType, documentSpan.originalFileName, documentSpan.originalContextSpan);
        return {
            ...documentSpan,
            fileName: textSpan.fileName,
            textSpan: textSpan.textSpan,
            contextSpan: contextSpan === null || contextSpan === void 0 ? void 0 : contextSpan.textSpan,
            originalFileName: originalTextSpan === null || originalTextSpan === void 0 ? void 0 : originalTextSpan.fileName,
            originalTextSpan: originalTextSpan === null || originalTextSpan === void 0 ? void 0 : originalTextSpan.textSpan,
            originalContextSpan: originalContextSpan === null || originalContextSpan === void 0 ? void 0 : originalContextSpan.textSpan,
        };
    }
    function transformSpan(lsType, fileName, textSpan) {
        if (!fileName)
            return;
        if (!textSpan)
            return;
        for (const vueLoc of sourceFiles.fromTsLocation2(lsType, shared.fsPathToUri(fileName), textSpan.start, textSpan.start + textSpan.length)) {
            return {
                fileName: shared.uriToFsPath(vueLoc.uri),
                textSpan: {
                    start: vueLoc.range.start,
                    length: vueLoc.range.end - vueLoc.range.start,
                },
            };
        }
    }
}
exports.register = register;
//# sourceMappingURL=tsPluginApis.js.map