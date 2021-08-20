import type * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register({ sourceFiles, getTsLs, htmlLs, pugLs, getCssLs }: ApiLanguageServiceContext): (uri: string, position: vscode.Position) => vscode.DocumentHighlight[] | undefined;
