import type { ApiLanguageServiceContext } from '../types';
import type * as vscode from 'vscode-languageserver';
export declare function register({ sourceFiles, getCssLs, getTsLs }: ApiLanguageServiceContext): (uri: string, position: vscode.Position) => vscode.Location[];
