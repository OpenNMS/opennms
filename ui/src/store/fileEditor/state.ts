export interface State {
  fileNames: string[],
  file: string,
  snippets: string,
  searchValue: string,
  contentModified: boolean,
  selectedFileName: string,
  modifiedFileString: string
}

const state: State = {
  fileNames: [],
  file: '',
  snippets: '',
  searchValue: '',
  contentModified: false,
  selectedFileName: '',
  modifiedFileString: ''
}

export default state
