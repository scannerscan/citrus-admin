/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.admin.model.spring;

import org.springframework.context.annotation.Bean;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entryType", propOrder = {
    "entryTypeKey",
    "description",
    "beenAndRevesAndIdreves"
})
@XmlRootElement(name = "entry")
public class Entry {

    @XmlElement(name = "key")
    protected Key entryTypeKey;
    protected Description description;
    @XmlElementRefs({
        @XmlElementRef(name = "value", namespace = "http://www.springframework.org/schema/beans", type = Value.class),
        @XmlElementRef(name = "bean", namespace = "http://www.springframework.org/schema/beans", type = SpringBean.class),
        @XmlElementRef(name = "map", namespace = "http://www.springframework.org/schema/beans", type = Map.class),
        @XmlElementRef(name = "list", namespace = "http://www.springframework.org/schema/beans", type = List.class),
        @XmlElementRef(name = "ref", namespace = "http://www.springframework.org/schema/beans", type = Ref.class),
        @XmlElementRef(name = "props", namespace = "http://www.springframework.org/schema/beans", type = Props.class)
    })
    @XmlAnyElement(lax = true)
    protected java.util.List<Object> beenAndRevesAndIdreves;
    @XmlAttribute(name = "key")
    protected String key;
    @XmlAttribute(name = "key-ref")
    protected String keyRef;
    @XmlAttribute(name = "value")
    protected String value;
    @XmlAttribute(name = "value-ref")
    protected String valueRef;

    /**
     * Gets the value of the entryTypeKey property.
     * 
     * @return
     *     possible object is
     *     {@link Key }
     *     
     */
    public Key getEntryTypeKey() {
        return entryTypeKey;
    }

    /**
     * Sets the value of the entryTypeKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link Key }
     *     
     */
    public void setEntryTypeKey(Key value) {
        this.entryTypeKey = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the beenAndRevesAndIdreves property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the beenAndRevesAndIdreves property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBeenAndRevesAndIdreves().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Value }
     * {@link Array }
     * {@link Bean }
     * {@link Map }
     * {@link Object }
     * {@link Set }
     * {@link Null }
     * {@link Ref }
     * {@link com.consol.citrus.model.spring.beans.List }
     * {@link Idref }
     * {@link Props }
     * 
     * 
     */
    public java.util.List<Object> getBeenAndRevesAndIdreves() {
        if (beenAndRevesAndIdreves == null) {
            beenAndRevesAndIdreves = new ArrayList<Object>();
        }
        return this.beenAndRevesAndIdreves;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the keyRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyRef() {
        return keyRef;
    }

    /**
     * Sets the value of the keyRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyRef(String value) {
        this.keyRef = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the valueRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueRef() {
        return valueRef;
    }

    /**
     * Sets the value of the valueRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueRef(String value) {
        this.valueRef = value;
    }

}
