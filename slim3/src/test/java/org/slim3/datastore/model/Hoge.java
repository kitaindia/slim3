/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.slim3.datastore.model;

import java.math.BigDecimal;
import java.util.Date;

import org.slim3.datastore.Blob;
import org.slim3.datastore.Model;
import org.slim3.datastore.PrimaryKey;
import org.slim3.datastore.Text;
import org.slim3.datastore.Version;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.ShortBlob;

/**
 * @author higa
 * 
 */
@Model
public class Hoge {

    @PrimaryKey
    private Key key;

    private short myShort;

    private Short myShortWrapper;

    private int myInt;

    private Integer myIntWrapper;

    private long myLong;

    private Long myLongWrapper;

    private float myFloat;

    private Float myFloatWrapper;

    private double myDouble;

    private Double myDoubleWrapper;

    private String myString;

    private boolean myBoolean;

    private Boolean myBooleanWrapper;

    private Date myDate;

    @Text
    private String myStringText;

    private com.google.appengine.api.datastore.Text myText;

    private byte[] myBytes;

    @Blob
    private byte[] myBytesBlob;

    private MySerializable mySerializable;

    @Blob
    private MySerializable mySerializableBlob;

    private com.google.appengine.api.datastore.Blob myBlob;

    private ShortBlob myShortBlob;

    private BigDecimal myBigDecimal;

    @Version
    private Long version;

    /**
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * @return the myShort
     */
    public short getMyShort() {
        return myShort;
    }

    /**
     * @param myShort
     *            the myShort to set
     */
    public void setMyShort(short myShort) {
        this.myShort = myShort;
    }

    /**
     * @return the myShortWrapper
     */
    public Short getMyShortWrapper() {
        return myShortWrapper;
    }

    /**
     * @param myShortWrapper
     *            the myShortWrapper to set
     */
    public void setMyShortWrapper(Short myShortWrapper) {
        this.myShortWrapper = myShortWrapper;
    }

    /**
     * @return the myInt
     */
    public int getMyInt() {
        return myInt;
    }

    /**
     * @param myInt
     *            the myInt to set
     */
    public void setMyInt(int myInt) {
        this.myInt = myInt;
    }

    /**
     * @return the myIntWrapper
     */
    public Integer getMyIntWrapper() {
        return myIntWrapper;
    }

    /**
     * @param myIntWrapper
     *            the myIntWrapper to set
     */
    public void setMyIntWrapper(Integer myIntWrapper) {
        this.myIntWrapper = myIntWrapper;
    }

    /**
     * @return the myLong
     */
    public long getMyLong() {
        return myLong;
    }

    /**
     * @param myLong
     *            the myLong to set
     */
    public void setMyLong(long myLong) {
        this.myLong = myLong;
    }

    /**
     * @return the myLongWrapper
     */
    public Long getMyLongWrapper() {
        return myLongWrapper;
    }

    /**
     * @param myLongWrapper
     *            the myLongWrapper to set
     */
    public void setMyLongWrapper(Long myLongWrapper) {
        this.myLongWrapper = myLongWrapper;
    }

    /**
     * @return the myFloat
     */
    public float getMyFloat() {
        return myFloat;
    }

    /**
     * @param myFloat
     *            the myFloat to set
     */
    public void setMyFloat(float myFloat) {
        this.myFloat = myFloat;
    }

    /**
     * @return the myFloatWrapper
     */
    public Float getMyFloatWrapper() {
        return myFloatWrapper;
    }

    /**
     * @param myFloatWrapper
     *            the myFloatWrapper to set
     */
    public void setMyFloatWrapper(Float myFloatWrapper) {
        this.myFloatWrapper = myFloatWrapper;
    }

    /**
     * @return the myDouble
     */
    public double getMyDouble() {
        return myDouble;
    }

    /**
     * @param myDouble
     *            the myDouble to set
     */
    public void setMyDouble(double myDouble) {
        this.myDouble = myDouble;
    }

    /**
     * @return the myDoubleWrapper
     */
    public Double getMyDoubleWrapper() {
        return myDoubleWrapper;
    }

    /**
     * @param myDoubleWrapper
     *            the myDoubleWrapper to set
     */
    public void setMyDoubleWrapper(Double myDoubleWrapper) {
        this.myDoubleWrapper = myDoubleWrapper;
    }

    /**
     * @return the myString
     */
    public String getMyString() {
        return myString;
    }

    /**
     * @param myString
     *            the myString to set
     */
    public void setMyString(String myString) {
        this.myString = myString;
    }

    /**
     * @return the myBoolean
     */
    public boolean isMyBoolean() {
        return myBoolean;
    }

    /**
     * @param myBoolean
     *            the myBoolean to set
     */
    public void setMyBoolean(boolean myBoolean) {
        this.myBoolean = myBoolean;
    }

    /**
     * @return the myBooleanWrapper
     */
    public Boolean getMyBooleanWrapper() {
        return myBooleanWrapper;
    }

    /**
     * @param myBooleanWrapper
     *            the myBooleanWrapper to set
     */
    public void setMyBooleanWrapper(Boolean myBooleanWrapper) {
        this.myBooleanWrapper = myBooleanWrapper;
    }

    /**
     * @return the myDate
     */
    public Date getMyDate() {
        return myDate;
    }

    /**
     * @param myDate
     *            the myDate to set
     */
    public void setMyDate(Date myDate) {
        this.myDate = myDate;
    }

    /**
     * @return the myText
     */
    public String getMyStringText() {
        return myStringText;
    }

    /**
     * @param myText
     *            the myText to set
     */
    public void setMyStringText(String myText) {
        this.myStringText = myText;
    }

    /**
     * @return the myText
     */
    public com.google.appengine.api.datastore.Text getMyText() {
        return myText;
    }

    /**
     * @param myText
     *            the myText to set
     */
    public void setMyText(com.google.appengine.api.datastore.Text myText) {
        this.myText = myText;
    }

    /**
     * @return the myBytes
     */
    public byte[] getMyBytes() {
        return myBytes;
    }

    /**
     * @param myBytes
     *            the myBytes to set
     */
    public void setMyBytes(byte[] myBytes) {
        this.myBytes = myBytes;
    }

    /**
     * @return the myBytesBlob
     */
    public byte[] getMyBytesBlob() {
        return myBytesBlob;
    }

    /**
     * @param myBytesBlob
     *            the myBytesBlob to set
     */
    public void setMyBytesBlob(byte[] myBytesBlob) {
        this.myBytesBlob = myBytesBlob;
    }

    /**
     * @return the mySerializable
     */
    public MySerializable getMySerializable() {
        return mySerializable;
    }

    /**
     * @param mySerializable
     *            the mySerializable to set
     */
    public void setMySerializable(MySerializable mySerializable) {
        this.mySerializable = mySerializable;
    }

    /**
     * @return the mySerializableBlob
     */
    public MySerializable getMySerializableBlob() {
        return mySerializableBlob;
    }

    /**
     * @param mySerializableBlob
     *            the mySerializableBlob to set
     */
    public void setMySerializableBlob(MySerializable mySerializableBlob) {
        this.mySerializableBlob = mySerializableBlob;
    }

    /**
     * @return the myBlob
     */
    public com.google.appengine.api.datastore.Blob getMyBlob() {
        return myBlob;
    }

    /**
     * @param myBlob
     *            the myBlob to set
     */
    public void setMyBlob(com.google.appengine.api.datastore.Blob myBlob) {
        this.myBlob = myBlob;
    }

    /**
     * @return the myShortBlob
     */
    public ShortBlob getMyShortBlob() {
        return myShortBlob;
    }

    /**
     * @param myShortBlob
     *            the myShortBlob to set
     */
    public void setMyShortBlob(ShortBlob myShortBlob) {
        this.myShortBlob = myShortBlob;
    }

    /**
     * @return the myBigDecimal
     */
    public BigDecimal getMyBigDecimal() {
        return myBigDecimal;
    }

    /**
     * @param myBigDecimal
     *            the myBigDecimal to set
     */
    public void setMyBigDecimal(BigDecimal myBigDecimal) {
        this.myBigDecimal = myBigDecimal;
    }

    /**
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}
