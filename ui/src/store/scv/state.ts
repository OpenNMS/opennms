import { SCVCredentials } from '@/types/scv'

export interface State {
  aliases: string[]
  credentials: SCVCredentials
  dbCredentials: SCVCredentials // used to track changes
  isEditing: boolean
}

const state: State = {
  aliases: [],
  credentials: {
    alias: '',
    username: '',
    password: '',
    attributes: {}
  },
  dbCredentials: {} as SCVCredentials,
  isEditing: false
}

export default state
