import type { ApiLanguageServiceContext } from '../types';
import type * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
import type { SourceMap } from '../utils/sourceMaps';
export declare function register({ sourceFiles }: ApiLanguageServiceContext): (uri: string, range: vscode.Range) => {
    language: string;
    document: TextDocument | undefined;
    range: vscode.Range;
    sourceMap: SourceMap | undefined;
} | undefined;
