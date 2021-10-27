import { State } from './state'

const getFilteredFileNames = (state: State) =>
  state.fileNames.filter((fileName) => !state.searchValue || (state.searchValue && fileName.includes(state.searchValue)))

export default {
  getFilteredFileNames
}
