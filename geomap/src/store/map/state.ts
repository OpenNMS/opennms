import { Node, Alarm } from '@/types'

export interface State {
    nodesWithCoordinates : Node[]
    alarms: Alarm[]
    interestedNodesID: number[]
    edges: [number, number][]
}

const state: State = {
    nodesWithCoordinates: [],
    alarms: [],
    interestedNodesID: [],
    edges: []
}

export default state