import * as CompilerDom from '@vue/compiler-dom';
import * as CompilerCore from '@vue/compiler-core';
export declare function compile(template: string, options?: CompilerDom.CompilerOptions): CompilerDom.CodegenResult;
export declare function baseCompile(template: string, options?: CompilerCore.CompilerOptions): CompilerCore.CodegenResult;
