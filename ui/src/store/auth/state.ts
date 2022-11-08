import { WhoAmIResponse } from '@/types'

export interface State {
  whoAmi: WhoAmIResponse
  loaded: boolean
}

const state: State = {
  whoAmi: {
    roles: [] as string[]
  } as WhoAmIResponse,
  loaded: false
}

export default state
