import type * as ts from 'typescript/lib/tsserverlibrary';
import * as vscode from 'vscode-languageserver';
import { TextDocument } from 'vscode-languageserver-textdocument';
import type { LanguageServiceHost } from '../';
export declare function register(languageService: ts.LanguageService, getTextDocument: (uri: string) => TextDocument | undefined, getTextDocument2: (uri: string) => TextDocument | undefined, host: LanguageServiceHost): (item: vscode.CompletionItem, newPosition?: vscode.Position | undefined) => Promise<vscode.CompletionItem>;
