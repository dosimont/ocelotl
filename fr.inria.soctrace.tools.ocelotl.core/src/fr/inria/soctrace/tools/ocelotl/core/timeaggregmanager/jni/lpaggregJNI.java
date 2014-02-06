/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.jni;

public class lpaggregJNI {
  public final static native long new_LPAggregWrapper(int jarg1);
  public final static native void delete_LPAggregWrapper(long jarg1);
  public final static native int LPAggregWrapper_getPart(long jarg1, LPAggregWrapper jarg1_, int jarg2);
  public final static native int LPAggregWrapper_getPartNumber(long jarg1, LPAggregWrapper jarg1_);
  public final static native float LPAggregWrapper_getParameter(long jarg1, LPAggregWrapper jarg1_, int jarg2);
  public final static native int LPAggregWrapper_getParameterNumber(long jarg1, LPAggregWrapper jarg1_);
  public final static native double LPAggregWrapper_getGainByIndex(long jarg1, LPAggregWrapper jarg1_, int jarg2);
  public final static native double LPAggregWrapper_getGainByParameter(long jarg1, LPAggregWrapper jarg1_, float jarg2);
  public final static native double LPAggregWrapper_getLossByIndex(long jarg1, LPAggregWrapper jarg1_, int jarg2);
  public final static native double LPAggregWrapper_getLossByParameter(long jarg1, LPAggregWrapper jarg1_, float jarg2);
  public final static native void LPAggregWrapper_computeQualities(long jarg1, LPAggregWrapper jarg1_, boolean jarg2);
  public final static native void LPAggregWrapper_computeParts(long jarg1, LPAggregWrapper jarg1_, float jarg2);
  public final static native void LPAggregWrapper_computeDichotomy(long jarg1, LPAggregWrapper jarg1_, float jarg2);
  public final static native void LPAggregWrapper_setValue__SWIG_0(long jarg1, LPAggregWrapper jarg1_, int jarg2, double jarg3);
  public final static native void LPAggregWrapper_push_back__SWIG_0(long jarg1, LPAggregWrapper jarg1_, double jarg2);
  public final static native void LPAggregWrapper_addVector__SWIG_0(long jarg1, LPAggregWrapper jarg1_);
  public final static native void LPAggregWrapper_setValue__SWIG_1(long jarg1, LPAggregWrapper jarg1_, int jarg2, int jarg3, double jarg4);
  public final static native void LPAggregWrapper_push_back__SWIG_1(long jarg1, LPAggregWrapper jarg1_, int jarg2, double jarg3);
  public final static native void LPAggregWrapper_addMatrix(long jarg1, LPAggregWrapper jarg1_);
  public final static native void LPAggregWrapper_setValue__SWIG_2(long jarg1, LPAggregWrapper jarg1_, int jarg2, int jarg3, int jarg4, double jarg5);
  public final static native void LPAggregWrapper_addVector__SWIG_1(long jarg1, LPAggregWrapper jarg1_, int jarg2);
  public final static native void LPAggregWrapper_push_back__SWIG_2(long jarg1, LPAggregWrapper jarg1_, int jarg2, int jarg3, double jarg4);
}
