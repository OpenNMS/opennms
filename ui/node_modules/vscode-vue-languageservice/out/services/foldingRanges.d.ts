import { TextDocument } from 'vscode-languageserver-textdocument';
import * as vscode from 'vscode-languageserver';
import type { HtmlLanguageServiceContext } from '../types';
import type { LanguageServiceHost } from 'vscode-typescript-languageservice';
export declare function register(context: HtmlLanguageServiceContext, getPreferences: LanguageServiceHost['getPreferences'], getFormatOptions: LanguageServiceHost['getFormatOptions']): (document: TextDocument) => vscode.FoldingRange[];
