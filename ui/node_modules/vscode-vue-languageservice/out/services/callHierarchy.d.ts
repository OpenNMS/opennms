import type * as vscode from 'vscode-languageserver';
import type { ApiLanguageServiceContext } from '../types';
export interface Data {
    lsType: 'script' | 'template';
    tsData: any;
}
export declare function register({ sourceFiles, getTsLs }: ApiLanguageServiceContext): {
    doPrepare: (uri: string, position: vscode.Position) => vscode.CallHierarchyItem[];
    getIncomingCalls: (item: vscode.CallHierarchyItem) => vscode.CallHierarchyIncomingCall[];
    getOutgoingCalls: (item: vscode.CallHierarchyItem) => vscode.CallHierarchyOutgoingCall[];
};
