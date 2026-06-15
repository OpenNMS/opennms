import { UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import {
  isDuplicateFile,
  MAX_FILES_UPLOAD,
  validateGroupElement,
  validateMibObjElement,
  validateParameterElement,
  validateResourceTypeElement,
  validateSnmpDataCollectionSourceFile,
  validateSystemDefElement
} from '@/components/SnmpDataCollection/snmpDataCollectionSourceXmlValidator'
import { beforeEach, describe, expect, it } from 'vitest'

describe('snmpDataCollectionSourceXmlValidator', () => {
  // Valid XML templates for testing
  const validXmlTemplate = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <resourceType name="testResource" label="Test Resource">
    <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/>
    <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/>
  </resourceType>
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="sysDescr" type="string"/>
  </group>
  <systemDef name="testSystem">
    <sysoid>.1.3.6.1.4.1.9</sysoid>
    <collect>
      <includeGroup>testGroup</includeGroup>
    </collect>
  </systemDef>
</datacollection-group>`

  const createMockFile = (content: string, name: string = 'test.xml'): File => {
    return new File([content], name, { type: 'application/xml' })
  }

  describe('MAX_FILES_UPLOAD constant', () => {
    it('should be defined', () => {
      expect(MAX_FILES_UPLOAD).toBeDefined()
    })

    it('should equal 100', () => {
      expect(MAX_FILES_UPLOAD).toBe(100)
    })
  })

  describe('validateSnmpDataCollectionSourceFile', () => {
    describe('Valid Files', () => {
      it('should validate a valid XML file', async () => {
        const file = createMockFile(validXmlTemplate)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should validate file with multiple resourceTypes', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <resourceType name="resource1" label="Resource 1">
    <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/>
    <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/>
  </resourceType>
  <resourceType name="resource2" label="Resource 2">
    <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/>
    <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/>
  </resourceType>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should validate file with multiple groups', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <group name="group1" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="sysDescr" type="string"/>
  </group>
  <group name="group2" ifType="ignore">
    <mibObj oid=".1.3.6.1.2.1.1.2" instance="0" alias="sysObjectID" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should validate file with multiple systemDefs', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <systemDef name="system1">
    <sysoid>.1.3.6.1.4.1.9</sysoid>
    <collect>
      <includeGroup>group1</includeGroup>
    </collect>
  </systemDef>
  <systemDef name="system2">
    <sysoidMask>.1.3.6.1.4.1.10.</sysoidMask>
    <collect>
      <includeGroup>group2</includeGroup>
    </collect>
  </systemDef>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should validate file with empty collect element', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <systemDef name="system1">
    <sysoid>.1.3.6.1.4.1.9</sysoid>
    <collect/>
  </systemDef>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should validate all valid MIB object types', async () => {
        const types = ['counter', 'counter32', 'counter64', 'gauge', 'gauge32', 'gauge64', 'integer', 'integer32', 'timeticks', 'string', 'octetstring', 'opaque']

        for (const type of types) {
          const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="${type}"/>
  </group>
</datacollection-group>`
          const file = createMockFile(xml, `test-${type}.xml`)
          const result = await validateSnmpDataCollectionSourceFile(file)

          expect(result.isValid).toBe(true)
          expect(result.errors).toHaveLength(0)
        }
      })
    })

    describe('Empty and Invalid File Content', () => {
      it('should reject empty file', async () => {
        const file = createMockFile('')
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('File is empty')
      })

      it('should reject file with only whitespace', async () => {
        const file = createMockFile('   \n\t  ')
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('File is empty')
      })

      it('should reject file without .xml extension', async () => {
        const file = createMockFile(validXmlTemplate, 'test.txt')
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('File must have .xml extension')
      })

      it('should reject file with invalid XML syntax', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <group name="testGroup" ifType="all"
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors.some(e => e.includes('Invalid XML format'))).toBe(true)
      })

      it('should reject file with malformed XML', async () => {
        const xml = 'not xml content at all'
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors.some(e => e.includes('Invalid XML format'))).toBe(true)
      })
    })

    describe('Root Element Validation', () => {
      it('should reject file with neither datacollection-group nor datacollection-config root', async () => {
        const xml = `<?xml version="1.0"?>
<root>
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</root>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('Expected <datacollection-group> or <datacollection-config> as root element')
      })

      it('should allow file with missing namespace', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group name="test-group">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(true)
        expect(result.errors).toHaveLength(0)
      })

      it('should reject file with invalid namespace', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://wrong.namespace.org" name="test-group">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('Invalid OpenNMS namespace in <datacollection-group> element')
      })

      it('should reject datacollection-group without name attribute', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('<datacollection-group> element must have a non-empty "name" attribute')
      })

      it('should reject datacollection-group with empty name attribute', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('<datacollection-group> element must have a non-empty "name" attribute')
      })

      it('should reject datacollection-group with whitespace-only name', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="   ">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('<datacollection-group> element must have a non-empty "name" attribute')
      })
    })

    describe('Child Elements Validation', () => {
      it('should reject file with no child elements', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors).toContain('No <resourceType>, <group>, or <systemDef> entries found within <datacollection-group> element')
      })

      it('should reject file with invalid child elements only', async () => {
        const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <invalidElement>content</invalidElement>
</datacollection-group>`
        const file = createMockFile(xml)
        const result = await validateSnmpDataCollectionSourceFile(file)

        expect(result.isValid).toBe(false)
        expect(result.errors.some(e => e.includes('but no <resourceType>, <group>, or <systemDef> elements'))).toBe(true)
      })
    })
  })

  describe('validateResourceTypeElement', () => {
    let parser: DOMParser
    let xmlDoc: Document

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('should validate a valid resourceType element', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('')
    })

    it('should reject null or undefined element', () => {
      const result = validateResourceTypeElement(null as any, 1)
      expect(result).toBe('ResourceType 1: invalid element')
    })

    it('should reject element without querySelector method', () => {
      const result = validateResourceTypeElement({} as any, 1)
      expect(result).toBe('ResourceType 1: invalid element')
    })

    it('should reject resourceType without name attribute', () => {
      const xml = `<resourceType label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType 1: missing "name" attribute')
    })

    it('should reject resourceType with empty name', () => {
      const xml = `<resourceType name="" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType 1: missing "name" attribute')
    })

    it('should reject resourceType without label attribute', () => {
      const xml = `<resourceType name="test">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": missing "label" attribute')
    })

    it('should reject resourceType with empty label', () => {
      const xml = `<resourceType name="test" label="">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": missing "label" attribute')
    })

    it('should reject resourceType without persistenceSelectorStrategy', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": missing <persistenceSelectorStrategy>')
    })

    it('should reject persistenceSelectorStrategy without class attribute', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": <persistenceSelectorStrategy> missing "class" attribute')
    })

    it('should reject persistenceSelectorStrategy with empty class', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class=""/>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": <persistenceSelectorStrategy> missing "class" attribute')
    })

    it('should reject resourceType without storageStrategy', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": missing <storageStrategy>')
    })

    it('should reject storageStrategy without class attribute', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": <storageStrategy> missing "class" attribute')
    })

    it('should reject storageStrategy with empty class', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
        <storageStrategy class=""/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('ResourceType "test": <storageStrategy> missing "class" attribute')
    })

    it('should validate resourceType with parameters in strategies', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy">
          <parameter key="key1" value="value1"/>
        </persistenceSelectorStrategy>
        <storageStrategy class="org.opennms.StorageStrategy">
          <parameter key="key2" value="value2"/>
        </storageStrategy>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toBe('')
    })

    it('should reject invalid parameter in persistenceSelectorStrategy', () => {
      const xml = `<resourceType name="test" label="Test Label">
        <persistenceSelectorStrategy class="org.opennms.TestStrategy">
          <parameter value="value1"/>
        </persistenceSelectorStrategy>
        <storageStrategy class="org.opennms.StorageStrategy"/>
      </resourceType>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('resourceType')

      const result = validateResourceTypeElement(element, 1)
      expect(result).toContain('persistenceSelectorStrategy parameter 1')
    })
  })

  describe('validateParameterElement', () => {
    let parser: DOMParser
    let xmlDoc: Document

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('should validate a valid parameter element', () => {
      const xml = '<parameter key="testKey" value="testValue"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('parameter')

      const result = validateParameterElement(element, 'Test context')
      expect(result).toBe('')
    })

    it('should validate parameter with empty value', () => {
      const xml = '<parameter key="testKey" value=""/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('parameter')

      const result = validateParameterElement(element, 'Test context')
      expect(result).toBe('')
    })

    it('should reject null parameter', () => {
      const result = validateParameterElement(null as any, 'Test context')
      expect(result).toBe('Test context: invalid element')
    })

    it('should reject parameter without key attribute', () => {
      const xml = '<parameter value="testValue"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('parameter')

      const result = validateParameterElement(element, 'Test context')
      expect(result).toBe('Test context: missing "key" attribute')
    })

    it('should reject parameter with empty key', () => {
      const xml = '<parameter key="" value="testValue"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('parameter')

      const result = validateParameterElement(element, 'Test context')
      expect(result).toBe('Test context: missing "key" attribute')
    })

    it('should reject parameter without value attribute', () => {
      const xml = '<parameter key="testKey"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('parameter')

      const result = validateParameterElement(element, 'Test context')
      expect(result).toBe('Test context: missing "value" attribute')
    })
  })

  describe('validateGroupElement', () => {
    let parser: DOMParser
    let xmlDoc: Document

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('should validate a valid group element', () => {
      const xml = `<group name="testGroup" ifType="all">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('')
    })

    it('should validate group with ifType="ignore"', () => {
      const xml = `<group name="testGroup" ifType="ignore">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('')
    })

    it('should validate group with numeric ifType', () => {
      const xml = `<group name="testGroup" ifType="6">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('')
    })

    it('should reject null or invalid element', () => {
      const result = validateGroupElement(null as any, 1)
      expect(result).toBe('Group 1: invalid element')
    })

    it('should reject element without querySelector method', () => {
      const result = validateGroupElement({} as any, 1)
      expect(result).toBe('Group 1: invalid element')
    })

    it('should reject group without name attribute', () => {
      const xml = `<group ifType="all">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('Group 1: missing "name" attribute')
    })

    it('should reject group with empty name', () => {
      const xml = `<group name="" ifType="all">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('Group 1: missing "name" attribute')
    })

    it('should reject group without ifType attribute', () => {
      const xml = `<group name="testGroup">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('Group "testGroup": missing "ifType" attribute')
    })

    it('should reject group with empty ifType', () => {
      const xml = `<group name="testGroup" ifType="">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('Group "testGroup": missing "ifType" attribute')
    })

    describe('Invalid ifType Values', () => {
      const invalidIfTypes = [
        { value: 'invalid', description: 'invalid string value' },
        { value: 'some', description: 'non-standard string' },
        { value: 'abc', description: 'alphabetic string' },
        { value: '1.5', description: 'decimal number' },
        { value: '-1', description: 'negative number' }
      ]

      it.each(invalidIfTypes)(
        'should reject group with ifType $description',
        ({ value }) => {
          const xml = `<group name="testGroup" ifType="${value}">
            <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
          </group>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('group')

          const result = validateGroupElement(element!, 1)
          expect(result).toContain(`invalid "ifType" value "${value}"`)
        }
      )
    })

    it('should reject group without mibObj elements', () => {
      const xml = `<group name="testGroup" ifType="all">
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('Group "testGroup": missing <mibObj> elements')
    })

    it('should validate group with multiple mibObj elements', () => {
      const xml = `<group name="testGroup" ifType="all">
        <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test1" type="string"/>
        <mibObj oid=".1.3.6.1.2.1.1.2" instance="0" alias="test2" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toBe('')
    })

    it('should reject group with invalid mibObj', () => {
      const xml = `<group name="testGroup" ifType="all">
        <mibObj instance="0" alias="test" type="string"/>
      </group>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('group')

      const result = validateGroupElement(element!, 1)
      expect(result).toContain('mibObj 1')
    })
  })

  describe('validateMibObjElement', () => {
    let parser: DOMParser
    let xmlDoc: Document

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('should validate a valid mibObj element', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="sysDescr" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('')
    })

    it('should validate OID without leading dot', () => {
      const xml = '<mibObj oid="1.3.6.1.2.1.1.1" instance="0" alias="sysDescr" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('')
    })

    it('should reject null element', () => {
      const result = validateMibObjElement(null as any, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: invalid element')
    })

    it('should reject mibObj without oid attribute', () => {
      const xml = '<mibObj instance="0" alias="test" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "oid"')
    })

    it('should reject mibObj with empty oid', () => {
      const xml = '<mibObj oid="" instance="0" alias="test" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "oid"')
    })

    describe('Invalid OID Formats', () => {
      const invalidOids = [
        { oid: 'abc.def', description: 'alphabetic characters' },
        { oid: '1.2.3.a', description: 'mixed alphanumeric' },
        { oid: '1..2.3', description: 'double dots' },
        { oid: '1.2.', description: 'trailing dot (invalid)' },
        { oid: '.', description: 'single dot' },
        { oid: '1 2 3', description: 'spaces instead of dots' }
      ]

      it.each(invalidOids)(
        'should reject OID with $description',
        ({ oid }) => {
          const xml = `<mibObj oid="${oid}" instance="0" alias="test" type="string"/>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('mibObj')

          const result = validateMibObjElement(element!, 'testGroup', 1)
          expect(result).toContain(`invalid OID format "${oid}"`)
        }
      )
    })

    it('should reject mibObj without instance attribute', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" alias="test" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "instance"')
    })

    it('should reject mibObj with empty instance', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="" alias="test" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "instance"')
    })

    it('should reject mibObj without alias attribute', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "alias"')
    })

    it('should reject mibObj with empty alias', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="" type="string"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "alias"')
    })

    it('should reject mibObj without type attribute', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test"/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "type"')
    })

    it('should reject mibObj with empty type', () => {
      const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type=""/>'
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('mibObj')

      const result = validateMibObjElement(element!, 'testGroup', 1)
      expect(result).toBe('Group "testGroup" mibObj 1: missing "type"')
    })

    describe('Invalid MIB Object Types', () => {
      const invalidTypes = [
        { type: 'invalid', description: 'unknown type' },
        { type: 'int', description: 'short form' },
        { type: 'str', description: 'abbreviated form' },
        { type: 'float', description: 'float type' },
        { type: 'boolean', description: 'boolean type' }
      ]

      it.each(invalidTypes)(
        'should reject type "$type" ($description)',
        ({ type }) => {
          const xml = `<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="${type}"/>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('mibObj')

          const result = validateMibObjElement(element!, 'testGroup', 1)
          expect(result).toContain(`invalid type "${type}"`)
        }
      )
    })

    describe('Valid MIB Object Types', () => {
      const validTypes = [
        'counter', 'counter32', 'counter64',
        'gauge', 'gauge32', 'gauge64',
        'integer', 'integer32',
        'timeticks', 'string', 'octetstring', 'opaque'
      ]

      it.each(validTypes)(
        'should validate type "%s"',
        (type) => {
          const xml = `<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="${type}"/>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('mibObj')

          const result = validateMibObjElement(element!, 'testGroup', 1)
          expect(result).toBe('')
        }
      )

      it('should accept case-insensitive types (Counter becomes counter)', () => {
        const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="Counter"/>'
        xmlDoc = parser.parseFromString(xml, 'application/xml')
        const element = xmlDoc.querySelector('mibObj')

        const result = validateMibObjElement(element!, 'testGroup', 1)
        expect(result).toBe('')
      })

      it('should accept case-insensitive types (String becomes string)', () => {
        const xml = '<mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="String"/>'
        xmlDoc = parser.parseFromString(xml, 'application/xml')
        const element = xmlDoc.querySelector('mibObj')

        const result = validateMibObjElement(element!, 'testGroup', 1)
        expect(result).toBe('')
      })
    })
  })

  describe('validateSystemDefElement', () => {
    let parser: DOMParser
    let xmlDoc: Document

    beforeEach(() => {
      parser = new DOMParser()
    })

    it('should validate systemDef with sysoid', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect>
          <includeGroup>testGroup</includeGroup>
        </collect>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    it('should validate systemDef with sysoidMask', () => {
      const xml = `<systemDef name="testSystem">
        <sysoidMask>.1.3.6.1.4.1.9.</sysoidMask>
        <collect>
          <includeGroup>testGroup</includeGroup>
        </collect>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    it('should validate systemDef with empty collect element', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    it('should reject null or invalid element', () => {
      const result = validateSystemDefElement(null as any, 1)
      expect(result).toBe('SystemDef 1: invalid element')
    })

    it('should reject element without querySelector method', () => {
      const result = validateSystemDefElement({} as any, 1)
      expect(result).toBe('SystemDef 1: invalid element')
    })

    it('should reject systemDef without name attribute', () => {
      const xml = `<systemDef>
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('SystemDef 1: missing "name" attribute')
    })

    it('should reject systemDef with empty name', () => {
      const xml = `<systemDef name="">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('SystemDef 1: missing "name" attribute')
    })

    it('should reject systemDef without sysoid or sysoidMask', () => {
      const xml = `<systemDef name="testSystem">
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('SystemDef "testSystem": missing <sysoid> or <sysoidMask>')
    })

    describe('Invalid Sysoid Formats', () => {
      const invalidSysoids = [
        { sysoid: 'abc', description: 'alphabetic' },
        { sysoid: '1.2.a.4', description: 'mixed alphanumeric' },
        { sysoid: '1..2.3', description: 'double dots' },
        { sysoid: '1 2 3', description: 'spaces' },
        { sysoid: '1.2.3.', description: 'trailing dot with no number' }
      ]

      it.each(invalidSysoids)(
        'should reject sysoid with $description format',
        ({ sysoid }) => {
          const xml = `<systemDef name="testSystem">
            <sysoid>${sysoid}</sysoid>
            <collect/>
          </systemDef>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('systemDef')

          const result = validateSystemDefElement(element!, 1)
          expect(result).toContain(`invalid <sysoid> format "${sysoid}"`)
        }
      )
    })

    it('should validate sysoid without leading dot', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>1.3.6.1.4.1.9</sysoid>
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    it('should validate sysoidMask with trailing dot', () => {
      const xml = `<systemDef name="testSystem">
        <sysoidMask>.1.3.6.1.4.1.9.</sysoidMask>
        <collect/>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    describe('Invalid SysoidMask Formats', () => {
      const invalidMasks = [
        { mask: 'abc.', description: 'alphabetic' },
        { mask: '1.2.a.', description: 'mixed alphanumeric' },
        { mask: '1..2.', description: 'double dots' }
      ]

      it.each(invalidMasks)(
        'should reject sysoidMask with $description format',
        ({ mask }) => {
          const xml = `<systemDef name="testSystem">
            <sysoidMask>${mask}</sysoidMask>
            <collect/>
          </systemDef>`
          xmlDoc = parser.parseFromString(xml, 'application/xml')
          const element = xmlDoc.querySelector('systemDef')

          const result = validateSystemDefElement(element!, 1)
          expect(result).toContain(`invalid <sysoidMask> format "${mask}"`)
        }
      )
    })

    it('should reject systemDef without collect element', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('SystemDef "testSystem": missing <collect>')
    })

    it('should validate systemDef with multiple includeGroups', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect>
          <includeGroup>group1</includeGroup>
          <includeGroup>group2</includeGroup>
        </collect>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toBe('')
    })

    it('should reject systemDef with empty includeGroup', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect>
          <includeGroup></includeGroup>
        </collect>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toContain('<includeGroup> 1 is empty')
    })

    it('should reject systemDef with whitespace-only includeGroup', () => {
      const xml = `<systemDef name="testSystem">
        <sysoid>.1.3.6.1.4.1.9</sysoid>
        <collect>
          <includeGroup>   </includeGroup>
        </collect>
      </systemDef>`
      xmlDoc = parser.parseFromString(xml, 'application/xml')
      const element = xmlDoc.querySelector('systemDef')

      const result = validateSystemDefElement(element!, 1)
      expect(result).toContain('<includeGroup> 1 is empty')
    })
  })

  describe('isDuplicateFile', () => {
    let existingFiles: UploadSnmpDataCollectionFileType[]

    beforeEach(() => {
      existingFiles = [
        {
          file: new File(['content1'], 'file1.xml', { type: 'application/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: new File(['content2'], 'file2.xml', { type: 'application/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: new File(['content3'], 'Test-File.xml', { type: 'application/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
    })

    it('should return true for duplicate file name', () => {
      const result = isDuplicateFile('file1.xml', existingFiles)
      expect(result).toBe(true)
    })

    it('should return false for non-duplicate file name', () => {
      const result = isDuplicateFile('file3.xml', existingFiles)
      expect(result).toBe(false)
    })

    it('should be case-insensitive', () => {
      const result = isDuplicateFile('FILE1.XML', existingFiles)
      expect(result).toBe(true)
    })

    it('should handle mixed case comparison', () => {
      const result = isDuplicateFile('test-file.xml', existingFiles)
      expect(result).toBe(true)
    })

    it('should handle empty existing files array', () => {
      const result = isDuplicateFile('file1.xml', [])
      expect(result).toBe(false)
    })

    it('should handle undefined existing files array', () => {
      const result = isDuplicateFile('file1.xml', undefined as any)
      expect(result).toBe(false)
    })

    it('should handle null existing files array', () => {
      const result = isDuplicateFile('file1.xml', null as any)
      expect(result).toBe(false)
    })

    describe('Parametrized Duplicate Detection Tests', () => {
      const testCases = [
        { fileName: 'file1.xml', expected: true, description: 'exact match' },
        { fileName: 'FILE1.XML', expected: true, description: 'uppercase' },
        { fileName: 'File1.Xml', expected: true, description: 'mixed case' },
        { fileName: 'file2.xml', expected: true, description: 'second file exact' },
        { fileName: 'file3.xml', expected: false, description: 'non-existent file' },
        { fileName: 'new-file.xml', expected: false, description: 'new file name' }
      ]

      it.each(testCases)(
        'should return $expected for $description',
        ({ fileName, expected }) => {
          const result = isDuplicateFile(fileName, existingFiles)
          expect(result).toBe(expected)
        }
      )
    })
  })

  describe('Integration Tests', () => {
    it('should validate complex file with all element types', async () => {
      const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="complex-group">
  <resourceType name="resource1" label="Resource 1">
    <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy">
      <parameter key="param1" value="value1"/>
    </persistenceSelectorStrategy>
    <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy">
      <parameter key="param2" value=""/>
    </storageStrategy>
  </resourceType>
  <group name="group1" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="sysDescr" type="string"/>
    <mibObj oid="1.3.6.1.2.1.1.3" instance="0" alias="sysUpTime" type="timeticks"/>
  </group>
  <group name="group2" ifType="6">
    <mibObj oid=".1.3.6.1.2.1.2.1.0" instance="0" alias="ifNumber" type="integer32"/>
  </group>
  <systemDef name="system1">
    <sysoid>.1.3.6.1.4.1.9</sysoid>
    <collect>
      <includeGroup>group1</includeGroup>
      <includeGroup>group2</includeGroup>
    </collect>
  </systemDef>
  <systemDef name="system2">
    <sysoidMask>.1.3.6.1.4.1.10.</sysoidMask>
    <collect/>
  </systemDef>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
      expect(result.errors).toHaveLength(0)
    })

    it('should detect first error in file with multiple errors', async () => {
      const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <resourceType label="Missing Name">
    <persistenceSelectorStrategy class="org.opennms.TestStrategy"/>
    <storageStrategy class="org.opennms.StorageStrategy"/>
  </resourceType>
  <group ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(false)
      expect(result.errors.length).toBeGreaterThan(0)
      expect(result.errors[0]).toContain('ResourceType 1: missing "name" attribute')
    })

    it('should handle file reading errors gracefully', async () => {
      const mockFile = {
        name: 'test.xml',
        text: () => Promise.reject(new Error('Read error'))
      } as any

      const result = await validateSnmpDataCollectionSourceFile(mockFile)

      expect(result.isValid).toBe(false)
      expect(result.errors[0]).toContain('Error reading file content')
    })
  })

  describe('Edge Cases', () => {
    it('should handle very long OID', async () => {
      const longOid = '.1.3.6.1.4.1.9999.' + Array(100).fill('1').join('.')
      const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <group name="testGroup" ifType="all">
    <mibObj oid="${longOid}" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
    })

    it('should handle very long attribute values', async () => {
      const longName = 'a'.repeat(1000)
      const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="${longName}">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
    })

    it('should handle XML with comments', async () => {
      const xml = `<?xml version="1.0"?>
<!-- This is a comment -->
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group">
  <!-- Another comment -->
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
    })

    it('should handle special characters in attribute values', async () => {
      const xml = `<?xml version="1.0"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-&amp;-group">
  <group name="test&lt;Group&gt;" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test&quot;alias" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
    })

    it('should handle Unicode characters in content', async () => {
      const xml = `<?xml version="1.0" encoding="UTF-8"?>
<datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="test-group-日本語">
  <group name="testGroup" ifType="all">
    <mibObj oid=".1.3.6.1.2.1.1.1" instance="0" alias="test-中文" type="string"/>
  </group>
</datacollection-group>`
      const file = createMockFile(xml)
      const result = await validateSnmpDataCollectionSourceFile(file)

      expect(result.isValid).toBe(true)
    })
  })
})
