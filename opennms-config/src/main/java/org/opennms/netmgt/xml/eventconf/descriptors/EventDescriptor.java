/*
 * This class was automatically generated with
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.eventconf.descriptors;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.opennms.netmgt.xml.eventconf.Event;

/**
 * Class EventDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class EventDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field _elementDefinition.
	 */
	private boolean _elementDefinition;

	/**
	 * Field _nsPrefix.
	 */
	private java.lang.String _nsPrefix;

	/**
	 * Field _nsURI.
	 */
	private java.lang.String _nsURI;

	/**
	 * Field _xmlName.
	 */
	private java.lang.String _xmlName;

	/**
	 * Field _identity.
	 */
	private org.exolab.castor.xml.XMLFieldDescriptor _identity;


	//----------------/
	//- Constructors -/
	//----------------/

	public EventDescriptor() {
		super();
		_nsURI = "http://xmlns.opennms.org/xsd/eventconf";
		_xmlName = "event";
		_elementDefinition = true;

		//-- set grouping compositor
		setCompositorAsSequence();
		org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
		org.exolab.castor.mapping.FieldHandler             handler        = null;
		org.exolab.castor.xml.FieldValidator               fieldValidator = null;
		//-- initialize attribute descriptors

		//-- initialize element descriptors

		//-- _mask
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Mask.class, "_mask", "mask", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getMask();
			}
			@Override

			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Mask();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setMask( (org.opennms.netmgt.xml.eventconf.Mask) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Mask");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _mask
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _uei
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_uei", "uei", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getUei();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setUei( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _uei
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _eventLabel
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_eventLabel", "event-label", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getEventLabel();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setEventLabel( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _eventLabel
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _snmp
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Snmp.class, "_snmp", "snmp", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getSnmp();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Snmp();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setSnmp( (org.opennms.netmgt.xml.eventconf.Snmp) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Snmp");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _snmp
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _descr
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_descr", "descr", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getDescr();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setDescr( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _descr
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _logmsg
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Logmsg.class, "_logmsg", "logmsg", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getLogmsg();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Logmsg();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setLogmsg( (org.opennms.netmgt.xml.eventconf.Logmsg) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Logmsg");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _logmsg
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _severity
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_severity", "severity", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getSeverity();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setSeverity( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setRequired(true);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _severity
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(1);
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _correlation
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Correlation.class, "_correlation", "correlation", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getCorrelation();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Correlation();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setCorrelation( (org.opennms.netmgt.xml.eventconf.Correlation) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Correlation");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _correlation
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _operinstruct
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_operinstruct", "operinstruct", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getOperinstruct();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setOperinstruct( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _operinstruct
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _autoactionList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Autoaction.class, "_autoactionList", "autoaction", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getAutoaction();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Autoaction();
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllAutoaction();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addAutoaction( (org.opennms.netmgt.xml.eventconf.Autoaction) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Autoaction");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _autoactionList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _varbindsdecodeList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Varbindsdecode.class, "_varbindsdecodeList", "varbindsdecode", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getVarbindsdecode();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Varbindsdecode();
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllVarbindsdecode();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addVarbindsdecode( (org.opennms.netmgt.xml.eventconf.Varbindsdecode) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Varbindsdecode");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _varbindsdecodeList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _operactionList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Operaction.class, "_operactionList", "operaction", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getOperaction();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Operaction();
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllOperaction();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addOperaction( (org.opennms.netmgt.xml.eventconf.Operaction) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Operaction");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _operactionList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _autoacknowledge
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Autoacknowledge.class, "_autoacknowledge", "autoacknowledge", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getAutoacknowledge();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Autoacknowledge();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setAutoacknowledge( (org.opennms.netmgt.xml.eventconf.Autoacknowledge) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Autoacknowledge");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _autoacknowledge
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _loggroupList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_loggroupList", "loggroup", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getLoggroup();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllLoggroup();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addLoggroup( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _loggroupList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _tticket
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Tticket.class, "_tticket", "tticket", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getTticket();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Tticket();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setTticket( (org.opennms.netmgt.xml.eventconf.Tticket) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Tticket");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _tticket
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _forwardList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Forward.class, "_forwardList", "forward", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getForward();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Forward();
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllForward();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addForward( (org.opennms.netmgt.xml.eventconf.Forward) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Forward");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _forwardList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _scriptList
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.Script.class, "_scriptList", "script", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getScript();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.Script();
			}
			@Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
				try {
					Event target = (Event) object;
					target.removeAllScript();
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.addScript( (org.opennms.netmgt.xml.eventconf.Script) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.Script");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(true);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _scriptList
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		fieldValidator.setMinOccurs(0);
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
		//-- _mouseovertext
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_mouseovertext", "mouseovertext", org.exolab.castor.xml.NodeType.Element);
		desc.setImmutable(true);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getMouseovertext();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return null;
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setMouseovertext( (java.lang.String) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("string");
		desc.setHandler(handler);
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _mouseovertext
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
			org.exolab.castor.xml.validators.StringValidator typeValidator;
			typeValidator = new org.exolab.castor.xml.validators.StringValidator();
			fieldValidator.setValidator(typeValidator);
			typeValidator.setWhiteSpace("preserve");
		}
		desc.setValidator(fieldValidator);
		//-- _alarmData
		desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.opennms.netmgt.xml.eventconf.AlarmData.class, "_alarmData", "alarm-data", org.exolab.castor.xml.NodeType.Element);
		handler = new org.exolab.castor.xml.XMLFieldHandler() {
			@Override
			public java.lang.Object getValue( java.lang.Object object )
			throws IllegalStateException
			{
				Event target = (Event) object;
				return target.getAlarmData();
			}
			@Override
			public java.lang.Object newInstance(java.lang.Object parent) {
				return new org.opennms.netmgt.xml.eventconf.AlarmData();
			}
			@Override
			public void setValue( java.lang.Object object, java.lang.Object value)
			throws IllegalStateException, IllegalArgumentException
			{
				try {
					Event target = (Event) object;
					target.setAlarmData( (org.opennms.netmgt.xml.eventconf.AlarmData) value);
				} catch (java.lang.Exception ex) {
					throw new IllegalStateException(ex.toString());
				}
			}
		};
		desc.setSchemaType("org.opennms.netmgt.xml.eventconf.AlarmData");
		desc.setHandler(handler);
		desc.setNameSpaceURI("http://xmlns.opennms.org/xsd/eventconf");
		desc.setMultivalued(false);
		addFieldDescriptor(desc);
		addSequenceElement(desc);

		//-- validation code for: _alarmData
		fieldValidator = new org.exolab.castor.xml.FieldValidator();
		{ //-- local scope
		}
		desc.setValidator(fieldValidator);
	}


	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method getAccessMode.
	 * 
	 * @return the access mode specified for this class.
	 */
	@Override()
	public org.exolab.castor.mapping.AccessMode getAccessMode(
	) {
		return null;
	}

	/**
	 * Method getIdentity.
	 * 
	 * @return the identity field, null if this class has no
	 * identity.
	 */
	@Override()
	public org.exolab.castor.mapping.FieldDescriptor getIdentity(
	) {
		return _identity;
	}

	/**
	 * Method getJavaClass.
	 * 
	 * @return the Java class represented by this descriptor.
	 */
	@Override()
	public java.lang.Class<?> getJavaClass(
	) {
		return org.opennms.netmgt.xml.eventconf.Event.class;
	}

	/**
	 * Method getNameSpacePrefix.
	 * 
	 * @return the namespace prefix to use when marshaling as XML.
	 */
	@Override()
	public java.lang.String getNameSpacePrefix(
	) {
		return _nsPrefix;
	}

	/**
	 * Method getNameSpaceURI.
	 * 
	 * @return the namespace URI used when marshaling and
	 * unmarshaling as XML.
	 */
	@Override()
	public java.lang.String getNameSpaceURI(
	) {
		return _nsURI;
	}

	/**
	 * Method getValidator.
	 * 
	 * @return a specific validator for the class described by this
	 * ClassDescriptor.
	 */
	@Override()
	public org.exolab.castor.xml.TypeValidator getValidator(
	) {
		return this;
	}

	/**
	 * Method getXMLName.
	 * 
	 * @return the XML Name for the Class being described.
	 */
	@Override()
	public java.lang.String getXMLName(
	) {
		return _xmlName;
	}

	/**
	 * Method isElementDefinition.
	 * 
	 * @return true if XML schema definition of this Class is that
	 * of a global
	 * element or element with anonymous type definition.
	 */
	@Override
	public boolean isElementDefinition(
	) {
		return _elementDefinition;
	}

}
