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
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for tRequirementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tRequirementType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/tosca/ns/2011/12}tEntityType">
 *       &lt;attribute name="requiredCapabilityType" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tRequirementType")
public class TRequirementType
    extends TEntityType
{

    @XmlAttribute(name = "requiredCapabilityType")
    protected QName requiredCapabilityType;

    /**
     * Gets the value of the requiredCapabilityType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getRequiredCapabilityType() {
        return requiredCapabilityType;
    }

    /**
     * Sets the value of the requiredCapabilityType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setRequiredCapabilityType(QName value) {
        this.requiredCapabilityType = value;
    }

}
