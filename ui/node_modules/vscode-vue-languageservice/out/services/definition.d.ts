import type * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register({ sourceFiles, getCssLs, getTsLs }: ApiLanguageServiceContext): {
    on: (uri: string, position: vscode.Position) => vscode.LocationLink[] | vscode.Location[];
    onType: (uri: string, position: vscode.Position) => vscode.LocationLink[];
};
