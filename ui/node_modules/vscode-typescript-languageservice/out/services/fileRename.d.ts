import type * as ts from 'typescript/lib/tsserverlibrary';
import type * as vscode from 'vscode-languageserver';
import type { TextDocument } from 'vscode-languageserver-textdocument';
import type { LanguageServiceHost } from '../';
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined, host: LanguageServiceHost): (oldUri: string, newUri: string) => Promise<vscode.WorkspaceEdit | undefined>;
