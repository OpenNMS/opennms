import { SCVCredentials } from '@/types/scv'

export interface State {
  aliases: string[]
  credentials: SCVCredentials
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
  isEditing: false
}

export default state
