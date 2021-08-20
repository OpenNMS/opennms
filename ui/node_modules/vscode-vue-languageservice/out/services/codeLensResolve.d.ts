import type { ApiLanguageServiceContext } from '../types';
import type * as vscode from 'vscode-languageserver';
export declare function register({ sourceFiles, getTsLs }: ApiLanguageServiceContext): (codeLens: vscode.CodeLens, canShowReferences?: boolean | undefined) => vscode.CodeLens;
