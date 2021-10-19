import { Node, Alarm } from '@/types'
import { State } from './state'

const getInterestedNodesID = (state: State) => {
    return state.interestedNodesID;
}

const getInterestedNodes = (state: State) => {
    return state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id));
}

const getAlarmsFromSelectedNodes = (state: State) => {
    let selectedNodesLabel = state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id)).map(
        (node: Node) => node.label
    );
    return state.alarms.filter((alarm: Alarm) =>
        selectedNodesLabel.includes(alarm.nodeLabel)
    )
}

const getEdges = (state: State) => {
    return state.edges;
}

const getMapCenter = (state: State) => {
    return state.mapCenter;
}

export default {
    getInterestedNodesID,
    getInterestedNodes,
    getAlarmsFromSelectedNodes,
    getEdges,
    getMapCenter,
}