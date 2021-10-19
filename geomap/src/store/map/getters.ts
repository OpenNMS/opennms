import { Node, Alarm } from '@/types'
import { State } from './state'

const getInterestedNodesID = (state: State) => {
    return state.interestedNodesID;
}

const getInterestedNodes = (state: State) => {
    return state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id));
}

const getAlarmsFromSelectedNodes = (state: State) => {
    let selectedNodesId = state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id)).map(
        (node: Node) => node.id
    );
    return state.alarms.filter((alarm: Alarm) =>
        selectedNodesId.includes(alarm.nodeId.toString())
    )
}

const getEdges = (state: State) => {
    return state.edges;
}

export default {
    getInterestedNodesID,
    getInterestedNodes,
    getAlarmsFromSelectedNodes,
    getEdges
}