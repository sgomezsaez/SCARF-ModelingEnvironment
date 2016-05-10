//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.08 at 08:30:12 PM GMT 
//


package org.eclipse.winery.model.tosca;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for tExportedOperation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tExportedOperation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="NodeOperation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="nodeRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *                 &lt;attribute name="interfaceName" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *                 &lt;attribute name="operationName" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RelationshipOperation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="relationshipRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *                 &lt;attribute name="interfaceName" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *                 &lt;attribute name="operationName" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Plan">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="planRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tExportedOperation", propOrder = {
    "nodeOperation",
    "relationshipOperation",
    "plan"
})
public class TExportedOperation {

    @XmlElement(name = "NodeOperation")
    protected TExportedOperation.NodeOperation nodeOperation;
    @XmlElement(name = "RelationshipOperation")
    protected TExportedOperation.RelationshipOperation relationshipOperation;
    @XmlElement(name = "Plan")
    protected TExportedOperation.Plan plan;
    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * Gets the value of the nodeOperation property.
     * 
     * @return
     *     possible object is
     *     {@link TExportedOperation.NodeOperation }
     *     
     */
    public TExportedOperation.NodeOperation getNodeOperation() {
        return nodeOperation;
    }

    /**
     * Sets the value of the nodeOperation property.
     * 
     * @param value
     *     allowed object is
     *     {@link TExportedOperation.NodeOperation }
     *     
     */
    public void setNodeOperation(TExportedOperation.NodeOperation value) {
        this.nodeOperation = value;
    }

    /**
     * Gets the value of the relationshipOperation property.
     * 
     * @return
     *     possible object is
     *     {@link TExportedOperation.RelationshipOperation }
     *     
     */
    public TExportedOperation.RelationshipOperation getRelationshipOperation() {
        return relationshipOperation;
    }

    /**
     * Sets the value of the relationshipOperation property.
     * 
     * @param value
     *     allowed object is
     *     {@link TExportedOperation.RelationshipOperation }
     *     
     */
    public void setRelationshipOperation(TExportedOperation.RelationshipOperation value) {
        this.relationshipOperation = value;
    }

    /**
     * Gets the value of the plan property.
     * 
     * @return
     *     possible object is
     *     {@link TExportedOperation.Plan }
     *     
     */
    public TExportedOperation.Plan getPlan() {
        return plan;
    }

    /**
     * Sets the value of the plan property.
     * 
     * @param value
     *     allowed object is
     *     {@link TExportedOperation.Plan }
     *     
     */
    public void setPlan(TExportedOperation.Plan value) {
        this.plan = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="nodeRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *       &lt;attribute name="interfaceName" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
     *       &lt;attribute name="operationName" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NodeOperation {

        @XmlAttribute(name = "nodeRef", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object nodeRef;
        @XmlAttribute(name = "interfaceName", required = true)
        @XmlSchemaType(name = "anyURI")
        protected String interfaceName;
        @XmlAttribute(name = "operationName", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String operationName;

        /**
         * Gets the value of the nodeRef property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getNodeRef() {
            return nodeRef;
        }

        /**
         * Sets the value of the nodeRef property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setNodeRef(Object value) {
            this.nodeRef = value;
        }

        /**
         * Gets the value of the interfaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInterfaceName() {
            return interfaceName;
        }

        /**
         * Sets the value of the interfaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInterfaceName(String value) {
            this.interfaceName = value;
        }

        /**
         * Gets the value of the operationName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOperationName() {
            return operationName;
        }

        /**
         * Sets the value of the operationName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOperationName(String value) {
            this.operationName = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="planRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Plan {

        @XmlAttribute(name = "planRef", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object planRef;

        /**
         * Gets the value of the planRef property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getPlanRef() {
            return planRef;
        }

        /**
         * Sets the value of the planRef property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setPlanRef(Object value) {
            this.planRef = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="relationshipRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
     *       &lt;attribute name="interfaceName" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
     *       &lt;attribute name="operationName" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class RelationshipOperation {

        @XmlAttribute(name = "relationshipRef", required = true)
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object relationshipRef;
        @XmlAttribute(name = "interfaceName", required = true)
        @XmlSchemaType(name = "anyURI")
        protected String interfaceName;
        @XmlAttribute(name = "operationName", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String operationName;

        /**
         * Gets the value of the relationshipRef property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getRelationshipRef() {
            return relationshipRef;
        }

        /**
         * Sets the value of the relationshipRef property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setRelationshipRef(Object value) {
            this.relationshipRef = value;
        }

        /**
         * Gets the value of the interfaceName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInterfaceName() {
            return interfaceName;
        }

        /**
         * Sets the value of the interfaceName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInterfaceName(String value) {
            this.interfaceName = value;
        }

        /**
         * Gets the value of the operationName property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOperationName() {
            return operationName;
        }

        /**
         * Sets the value of the operationName property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOperationName(String value) {
            this.operationName = value;
        }

    }

}
