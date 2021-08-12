import axios from 'axios'

const v2 = axios.create({
  baseURL: process.env.VITE_BASE_V2_URL || '/opennms/api/v2',
  withCredentials: true
})

const rest = axios.create({
  baseURL: process.env.VITE_BASE_REST_URL || '/opennms/rest',
  withCredentials: true
})

export { v2, rest }
