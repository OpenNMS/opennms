import type { ApiLanguageServiceContext } from '../types';
import type { TextDocument } from 'vscode-languageserver-textdocument';
import * as vscode from 'vscode-languageserver';
import type * as ts2 from 'vscode-typescript-languageservice';
import type * as ts from 'typescript/lib/tsserverlibrary';
export declare function register({ modules: { typescript: ts }, sourceFiles, getTsLs }: ApiLanguageServiceContext): (document: TextDocument, position: vscode.Position) => string | undefined | null;
export declare function isBlacklistNode(ts: typeof import('typescript/lib/tsserverlibrary'), node: ts.Node, pos: number): boolean;
export declare function isRefType(typeDefs: vscode.LocationLink[], tsLs: ts2.LanguageService): boolean;
