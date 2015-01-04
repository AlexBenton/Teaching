package com.bentonian.framework.math;

import static java.lang.Math.max;
import static java.lang.Math.min;


/*
 * Math3d - The 3D Computer Graphics Math Library
 * Copyright (C) 1996-2000 by J.E. Hoffmann <je-h@gmx.net>
 * All rights reserved.
 *
 * This program is  free  software;  you can redistribute it and/or modify it
 * under the terms of the  GNU Lesser General Public License  as published by 
 * the  Free Software Foundation;  either version 2.1 of the License,  or (at 
 * your option) any later version.
 *
 * This  program  is  distributed in  the  hope that it will  be useful,  but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or  FITNESS FOR A  PARTICULAR PURPOSE.  See the  GNU Lesser General Public  
 * License for more details.
 *
 * You should  have received  a copy of the GNU Lesser General Public License
 * along with  this program;  if not, write to the  Free Software Foundation,
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: m3d.cpp,v 1.1 2005/09/09 13:42:14 pb355 Exp $
 */
public class M3d {
  
  static final double EPSILON = 0.00001;
  double vec[] = new double[3];  
  
  public M3d() {
    vec[0] = vec[1] = vec[2] = 0;
  }
  
  public M3d(double x, double y, double z) {
    set(x, y, z);
  }
  
  public M3d(M3d A) {
    set(A);
  }

  public M3d(double[] data) {
    set(data);
  }
  
  public M3d(int[] data) {
    set(data[0], data[1], data[2]);
  }
  
  public void set(double x, double y, double z) {
    vec[0] = x;
    vec[1] = y;
    vec[2] = z;
  }
  
  public void set(M3d A) {
    vec[0] = A.vec[0];
    vec[1] = A.vec[1];
    vec[2] = A.vec[2];
  }

  public void set(double[] data) {
    vec[0] = data[0];
    vec[1] = data[1];
    vec[2] = data[2];
  }

  public M3d neg() {
    return new M3d(-vec[0], -vec[1], -vec[2]);
  }

  public M3d plus(M3d B) {
    return new M3d(vec[0] + B.vec[0], vec[1] + B.vec[1], vec[2] + B.vec[2]); 
  }

  public M3d minus(M3d B) {
    return new M3d(vec[0] - B.vec[0], vec[1] - B.vec[1], vec[2] - B.vec[2]); 
  }

  public M3d times(double k) {
    return new M3d(k*vec[0], k*vec[1], k*vec[2]);
  }

  public M3d cross(M3d B) {
    return new M3d(vec[1]*B.vec[2] - vec[2]*B.vec[1], 
        vec[2]*B.vec[0] - vec[0]*B.vec[2],
        vec[0]*B.vec[1] - vec[1]*B.vec[0]);
  }

  public M3d lerp(M3d B, double t) {
    return new M3d(vec[0]+t*(B.vec[0]-vec[0]),
        vec[1]+t*(B.vec[1]-vec[1]),
        vec[2]+t*(B.vec[2]-vec[2]));
  }

  public M3d normalized() {
    double len = length();
    
    if (len > EPSILON) {
      return this.times(1.0 / len);
    } else {
      return new M3d(0,0,0);
    }
  }

  public double get(int i) {
    return vec[i];
  }

  public double[] get() { 
    return vec;
  }
  
  public float[] asFloats() { 
    return new float[]{ (float) vec[0], (float) vec[1], (float) vec[2] };
  }
  
  public int asRGBA() {
    int r = max(min((int) (vec[0] * 255), 255), 0);
    int g = max(min((int) (vec[1] * 255), 255), 0);
    int b = max(min((int) (vec[2] * 255), 255), 0);
    return (0xFF << 24) | (r << 16) | (g << 8) | (b << 0);
  }
  
  public void set(int i, double val) {
    vec[i] = val;
  }

  public double getX() {
    return vec[0];
  }

  public void setX(double val) {
    vec[0] = val;
  }

  public double getY() {
    return vec[1];
  }

  public void setY(double val) {
    vec[1] = val;
  }

  public double getZ() {
    return vec[2];
  }

  public void setZ(double val) {
    vec[2] = val;
  }

  public double dot(M3d A) {
    return vec[0]*A.vec[0] + vec[1]*A.vec[1] + vec[2]*A.vec[2];
  }

  public boolean cmp(M3d A, double epsilon) {
    return
      (Math.abs(vec[0]-A.vec[0])<epsilon) &&
      (Math.abs(vec[1]-A.vec[1])<epsilon) &&
      (Math.abs(vec[2]-A.vec[2])<epsilon);
  }

  public double length() {
    return (double) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
  }

  public double lengthSquared() {
    return vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2];
  }
  
  public boolean lessThan(M3d A) {
    return ((getX() < A.getX()) || 
            (getX() == A.getX() && getY() < A.getY()) || 
            (getX() == A.getX() && getY() == A.getY() && getZ() < A.getZ()));
  }
  
  public M3d toAxis() {
    double x = Math.abs(getX());
    double y = Math.abs(getY());
    double z = Math.abs(getZ());
    
    if (x >= y && x >= z) {
      return new M3d(getX() < 0 ? -1 : 1, 0, 0);
    } else if (y >= x && y >= z) {
      return new M3d(0, getY() < 0 ? -1 : 1, 0);
    } else if (z >= x && z >= y) {
      return new M3d(0, 0, getZ() < 0 ? -1 : 1);
    } else {
      return new M3d(0,0,0);
    }
  }
  
  @Override
  public String toString() {
    return getX() + ", " + getY() + ", " + getZ();
  }

  @Override
  public boolean equals(Object v) {
    return (v instanceof M3d) ? cmp((M3d) v, EPSILON) : false;
  }

  @Override
  public int hashCode() {
    long l = 991 * Double.doubleToLongBits(vec[0])
        ^ 997 * Double.doubleToLongBits(vec[1])
        ^ 1009 * Double.doubleToLongBits(vec[2]);
    return (int) ((l >> 32) ^ (l & 0xFFFFFFFF));
  }
}
