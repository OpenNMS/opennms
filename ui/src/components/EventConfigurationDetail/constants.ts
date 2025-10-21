
export enum EventConfigurationDocType {
  Json = 'json',
  Xml = 'xml'
}

export const EventConfigurationDocTypes = [
  {
    name: EventConfigurationDocType.Json.toUpperCase(),
    value: EventConfigurationDocType.Json
  },
  {
    name: EventConfigurationDocType.Xml.toUpperCase(),
    value: EventConfigurationDocType.Xml
  }  
]

