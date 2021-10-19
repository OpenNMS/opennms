import API from "@/services"
import { QueryParameters, AlarmModificationQueryVariable, VuexContext, Node } from '@/types'

const getNodes = async (context: VuexContext, queryParameters?: QueryParameters) => {
    const resp = await API.getNodes(queryParameters)
    if (resp) {
        let nodes = resp.node.filter(
            (node) =>
                !(
                    node.assetRecord.latitude == null ||
                    node.assetRecord.latitude.length === 0
                ) &&
                !(
                    node.assetRecord.longitude == null ||
                    node.assetRecord.longitude.length === 0
                )
        )
        context.commit("SAVE_NODES_TO_STATE", nodes),
        context.commit("SAVE_INTERESTED_NODES_ID", nodes.map(node => node.id))
    }
}

const getAlarms = async (context: VuexContext, queryParameters?: QueryParameters) => {
    const resp = await API.getAlarms(queryParameters)
    if (resp) {
        context.commit("SAVE_ALARMS_TO_STATE", resp.alarm)
    }
}

const resetInterestedNodesID = ({ commit, state }) => {
    commit("SAVE_INTERESTED_NODES_ID", state.nodesWithCoordinates.map((node: Node) => node.id))
}

const getNodesGraphEdges = async (context: VuexContext, queryParameters?: QueryParameters) => {
    const resp = await API.getGraphNodesNodes(queryParameters)
    if (resp) {
        let edges: [number, number][] = [];
        resp.edges.forEach((e) => {
            let edge: [number, number]
            edge = [e.source.id, e.target.id]
            edges.push(edge)
        });

        context.commit("SAVE_NODE_EDGES", edges)
    }
}

const setInterestedNodesId = (context: VuexContext, ids: number[]) => {
    context.commit("SAVE_INTERESTED_NODES_ID", ids)
}

const modifyAlarm = async (context: VuexContext, alarmQueryVariable: AlarmModificationQueryVariable) => {
    const resp = await API.modifyAlarm(alarmQueryVariable.pathVariable, alarmQueryVariable.queryParameters)
    return resp;
}


export default {
    getNodes,
    getAlarms,
    resetInterestedNodesID,
    getNodesGraphEdges,
    setInterestedNodesId,
    modifyAlarm
}