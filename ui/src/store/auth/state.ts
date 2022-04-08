import { WhoAmIResponse } from '@/types'

export interface State {
  whoAmi: WhoAmIResponse
}

const state: State = {
  whoAmi: {
    roles: [] as string[]
  } as WhoAmIResponse
}

export default state
