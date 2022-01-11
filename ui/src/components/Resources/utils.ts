import { GraphMetricsResponse } from '@/types'
import { fromUnixTime, format } from 'date-fns'

const formatXLabels = (metrics: GraphMetricsResponse, format: string): GraphMetricsResponse => {
  const timestamps = metrics.timestamps
  metrics.formattedTimestamps = timestamps.map((timestamp) => formatTimestamp(timestamp, format))
  return metrics
}

const formatTimestamp = (timestamp: number, formatStr: string) => {
  const date = fromUnixTime(timestamp / 1000)

  switch(formatStr) {
    case 'minutes':
      return format(date, 'HH:mm')
    case 'hours':
      return format(date, 'HH:mm')
    case 'days':
      return format(date, 'dd/MMM HH:mm')
    default:
      return format(date, 'dd/MMM :HH:mm')
  }
}

export {
  formatXLabels
}
