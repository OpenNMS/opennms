import * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export declare function register({ documentContext, sourceFiles, htmlLs, pugLs, getCssLs }: ApiLanguageServiceContext): (uri: string) => Promise<vscode.DocumentLink[] | undefined>;
