import type { ApiLanguageServiceContext } from './types';
import type * as ts from 'typescript/lib/tsserverlibrary';
export declare function register({ sourceFiles, scriptTsLsRaw, templateTsLsRaw }: ApiLanguageServiceContext): {
    getCompletionsAtPosition: (fileName: string, position: number, options: ts.GetCompletionsAtPositionOptions | undefined) => ReturnType<ts.LanguageService['getCompletionsAtPosition']>;
    getDefinitionAtPosition: (fileName: string, position: number) => ReturnType<ts.LanguageService['getDefinitionAtPosition']>;
    getDefinitionAndBoundSpan: (fileName: string, position: number) => ReturnType<ts.LanguageService['getDefinitionAndBoundSpan']>;
    getTypeDefinitionAtPosition: (fileName: string, position: number) => ReturnType<ts.LanguageService['getDefinitionAtPosition']>;
    getImplementationAtPosition: (fileName: string, position: number) => ReturnType<ts.LanguageService['getImplementationAtPosition']>;
    findRenameLocations: (fileName: string, position: number, findInStrings: boolean, findInComments: boolean, providePrefixAndSuffixTextForRename?: boolean | undefined) => ReturnType<ts.LanguageService['findRenameLocations']>;
    getReferencesAtPosition: (fileName: string, position: number) => ReturnType<ts.LanguageService['getReferencesAtPosition']>;
    findReferences: (fileName: string, position: number) => ReturnType<ts.LanguageService['findReferences']>;
};
