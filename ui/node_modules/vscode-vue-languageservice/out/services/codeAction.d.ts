import * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register({ sourceFiles, getCssLs, getTsLs }: ApiLanguageServiceContext): (uri: string, range: vscode.Range, context: vscode.CodeActionContext) => Promise<vscode.CodeAction[]>;
