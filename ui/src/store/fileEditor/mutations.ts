import { State } from './state'

const SAVE_FILE_NAMES_TO_STATE = (state: State, fileNames: string[]) => {
  state.fileNames = fileNames
}

const SAVE_FILE_TO_STATE = (state: State, file: string) => {
  state.file = file
}

export default {
  SAVE_FILE_NAMES_TO_STATE,
  SAVE_FILE_TO_STATE
}
