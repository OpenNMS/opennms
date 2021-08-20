import * as CompilerDOM from '@vue/compiler-dom';
export declare function generate(node: CompilerDOM.RootNode): {
    text: string;
    tags: Set<string>;
};
