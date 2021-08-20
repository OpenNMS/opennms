import type * as vscode from 'vscode-languageserver';
import { TextDocument } from 'vscode-languageserver-textdocument';
import type { LanguageServiceHost } from 'vscode-typescript-languageservice';
import type { HtmlLanguageServiceContext } from '../types';
export declare function register(context: HtmlLanguageServiceContext, getPreferences: LanguageServiceHost['getPreferences'], getFormatOptions: LanguageServiceHost['getFormatOptions']): (document: TextDocument, positions: vscode.Position[]) => vscode.SelectionRange[];
