import API from "@/services"
import { Node, QueryParameters, VuexContext } from '@/types'
import { State } from './state'

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

    context.commit("SAVE_ALARMS", resp.alarm)

}

const resetInterestedNodesID = (context: VuexContext, state: State) => {
    context.commit("SET_INTERESTED_NODES_ID", state.nodesWithCoordinates.map(node => node.id))
}

// const getNodesGraph = ({ commit }) => {
//     return API.getNodesGraph()
//         .then(response => {
//             let edges = []
//             response.data.edges.forEach((e) => {
//                 let edge = []
//                 edge.push(e.source.id)
//                 edge.push(e.target.id)
//                 edges.push(edge)
//             });
//             commit("SET_NODE_EDGES", edges)
//         })
//         .catch(error => {
//             throw (error)
//         })
// }

const setInterestedNodesId = ( context: VuexContext , ids: number[]) => {
    context.commit("SAVE_INTERESTED_NODES_ID", ids)
}


export default {
    getNodes,
    getAlarms,
    resetInterestedNodesID,
    // getNodesGraph,
    setInterestedNodesId
}