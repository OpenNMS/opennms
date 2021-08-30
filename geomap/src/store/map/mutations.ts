import { Node, Alarm } from '@/types'
import { State } from './state'

const SAVE_NODES_TO_STATE = (state: State, nodes: Node[]) =>{
    state.nodesWithCoordinates = [...nodes]
}

const SAVE_ALARMS_TO_STATE = (state: State, alarms: Alarm[]) => {
    state.alarms = [...alarms]
}

const SAVE_INTERESTED_NODES_ID = (state: State, ids: number[]) => {
    state.interestedNodesID = [...ids]
}

const SAVE_NODE_EDGES = (state: State, edges: [number, number][]) => {
    state.edges = [...edges]
}

export default {
    SAVE_NODES_TO_STATE,
    SAVE_ALARMS_TO_STATE,
    SAVE_INTERESTED_NODES_ID,
    SAVE_NODE_EDGES
}