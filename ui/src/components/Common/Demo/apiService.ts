import axios from "axios";

let url = "src/components/Common/Demo/nodeData.json";

const nodeData = axios.get(url);

export default nodeData
