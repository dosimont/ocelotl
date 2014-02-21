/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni;

public class OLPAggregWrapper {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected OLPAggregWrapper(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OLPAggregWrapper obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @Override
protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        lpaggregJNI.delete_OLPAggregWrapper(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OLPAggregWrapper(int dimension) {
    this(lpaggregJNI.new_OLPAggregWrapper(dimension), true);
  }

  public int getPart(int index) {
    return lpaggregJNI.OLPAggregWrapper_getPart(swigCPtr, this, index);
  }

  public int getPartNumber() {
    return lpaggregJNI.OLPAggregWrapper_getPartNumber(swigCPtr, this);
  }

  public float getParameter(int index) {
    return lpaggregJNI.OLPAggregWrapper_getParameter(swigCPtr, this, index);
  }

  public int getParameterNumber() {
    return lpaggregJNI.OLPAggregWrapper_getParameterNumber(swigCPtr, this);
  }

  public double getGainByIndex(int index) {
    return lpaggregJNI.OLPAggregWrapper_getGainByIndex(swigCPtr, this, index);
  }

  public double getGainByParameter(float parameter) {
    return lpaggregJNI.OLPAggregWrapper_getGainByParameter(swigCPtr, this, parameter);
  }

  public double getLossByIndex(int index) {
    return lpaggregJNI.OLPAggregWrapper_getLossByIndex(swigCPtr, this, index);
  }

  public double getLossByParameter(float parameter) {
    return lpaggregJNI.OLPAggregWrapper_getLossByParameter(swigCPtr, this, parameter);
  }

  public void computeQualities(boolean normalization) {
    lpaggregJNI.OLPAggregWrapper_computeQualities(swigCPtr, this, normalization);
  }

  public void computeParts(float parameter) {
    lpaggregJNI.OLPAggregWrapper_computeParts(swigCPtr, this, parameter);
  }

  public void computeDichotomy(float threshold) {
    lpaggregJNI.OLPAggregWrapper_computeDichotomy(swigCPtr, this, threshold);
  }

  public void setValue(int i, double value) {
    lpaggregJNI.OLPAggregWrapper_setValue__SWIG_0(swigCPtr, this, i, value);
  }

  public void push_back(double value) {
    lpaggregJNI.OLPAggregWrapper_push_back__SWIG_0(swigCPtr, this, value);
  }

  public void addVector() {
    lpaggregJNI.OLPAggregWrapper_addVector__SWIG_0(swigCPtr, this);
  }

  public void setValue(int i, int j, double value) {
    lpaggregJNI.OLPAggregWrapper_setValue__SWIG_1(swigCPtr, this, i, j, value);
  }

  public void push_back(int i, double value) {
    lpaggregJNI.OLPAggregWrapper_push_back__SWIG_1(swigCPtr, this, i, value);
  }

  public void addMatrix() {
    lpaggregJNI.OLPAggregWrapper_addMatrix(swigCPtr, this);
  }

  public void setValue(int i, int j, int k, double value) {
    lpaggregJNI.OLPAggregWrapper_setValue__SWIG_2(swigCPtr, this, i, j, k, value);
  }

  public void addVector(int i) {
    lpaggregJNI.OLPAggregWrapper_addVector__SWIG_1(swigCPtr, this, i);
  }

  public void push_back(int i, int j, double value) {
    lpaggregJNI.OLPAggregWrapper_push_back__SWIG_2(swigCPtr, this, i, j, value);
  }

}
