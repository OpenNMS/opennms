import { IpInterface } from '@/types'

export interface State {
  ipInterfaces: IpInterface[]
}

const state: State = {
  ipInterfaces: []
}

export default state
