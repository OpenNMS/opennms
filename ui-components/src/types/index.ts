export interface Plugin {
  extensionClass?: string
  extensionId: string
  menuEntry: string
  moduleFileName: string
  resourceRootPath: string
}

export interface SearchResultResponse {
  label?: string
  context: {
    name: string
    weight: number
  }
  empty: boolean
  more: boolean
  results: SearchResultMatch[]
}

export interface SearchResultMatch {
  label: string;
  value: string;
}

export interface SearchResultItem {
  identifier: string
  icon: string;
  label: string;
  url: string;
  properties: any;
  matches: SearchResultMatch[];
  weight: number;
}

export type SearchResultsByContext = Array<{label: string, results: SearchResultResponse[]}>
