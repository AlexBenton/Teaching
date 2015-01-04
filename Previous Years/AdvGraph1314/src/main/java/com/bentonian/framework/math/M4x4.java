package com.bentonian.framework.math;

import java.util.Arrays;

import com.google.common.base.Objects;

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
 * $Id: m4x4.cpp,v 1.1 2005/09/09 13:42:14 pb355 Exp $
 */
public class M4x4 {

  // Data stored [row][col]
  double data[][] = new double[4][4];

  public M4x4() {
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        data[row][col] = (row == col) ? 1 : 0;
      }
    }
  }

  public M4x4(double[][] A) {
    setData(A);
  }

  public M4x4(float[] A) {
    int i = 0;
    for (int col = 0; col < 4; col++) {
      for (int row = 0; row < 4; row++) {
        data[row][col] = A[i++];
      }
    }
  }

  public M4x4(M4x4 A) {
    setData(A);
  }

  public double[][] getData() {
    return data;
  }

  public float[] asFloats() {
    float[] buffer = new float[16];
    int i = 0;
    for (int col = 0; col < 4; col++) {
      for (int row = 0; row < 4; row++) {
        buffer[i++] = (float) data[row][col];
      }
    }
    return buffer;
  }

  public float[] as3x3Floats() {
    float[] buffer = new float[9];
    int i = 0;
    for (int col = 0; col < 3; col++) {
      for (int row = 0; row < 3; row++) {
        buffer[i++] = (float) data[row][col];
      }
    }
    return buffer;
  }

  public M4x4 setData(double[][] A) {
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        data[row][col] = A[row][col];
      }
    }
    return this;
  }

  public M4x4 setData(M4x4 A) {
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        data[row][col] = A.data[row][col];
      }
    }
    return this;
  }

  public static M4x4 fromString(String source) {
    M4x4 M = new M4x4();
    
    source = source.replace("[", " ");
    source = source.replace("]", " ");
    source = source.replace("\n", " ");
    source = source.replace(",", " ");
    source = source.trim();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        int n = source.indexOf(" ");
        n = (n > 0) ? n : source.length();
        M.data[row][col] = Double.valueOf(source.substring(0, n));
        source = source.substring(n).trim();
      }
    }
    return M;
  }
  
  public static M4x4 identity() {
    return new M4x4();
  }

  public M4x4 setIdentity() {
    setData(new M4x4().getData());
    return this;
  }

  public boolean isIdentity() {
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        if (data[row][col] != ((row == col) ? 1 : 0)) {
          return false;
        }
      }
    }
    return true;
  }

  public M4x4 plus(M4x4 A) {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = data[row][col] + A.data[row][col];
      }
    }
    return M;
  }

  public M4x4 minus(M4x4 A) {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = data[row][col] - A.data[row][col];
      }
    }
    return M;
  }

  public M4x4 neg() {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = -data[row][col];
      }
    }
    return M;
  }

  public M4x4 times(double k) {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = data[row][col] * k;
      }
    }
    return M;
  }

  public M3d times(M3d C) {
    double a, b, c, d;

    a = getRow(0).dot(C) + data[0][3];
    b = getRow(1).dot(C) + data[1][3];
    c = getRow(2).dot(C) + data[2][3];
    d = getRow(3).dot(C) + data[3][3];
    return new M3d(a / d, b / d, c / d);
  }

  public M4x4 times(M4x4 A) {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = 0;
        for (int k = 0; k < 4; k++) {
          M.data[row][col] += data[row][k] * A.data[k][col];
        }
      }
    }
    return M;
  }

  public M4x4 transposed() {
    M4x4 M = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        M.data[row][col] = data[col][row];
      }
    }
    return M;
  }

  public static M4x4 translationMatrix(M3d t) {
    M4x4 M = new M4x4();
    for (int i = 0; i < 3; i++) {
      M.data[i][3] = t.get(i);
    }
    return M;
  }

  public static M4x4 scaleMatrix(M3d t) {
    M4x4 M = new M4x4();
    for (int i = 0; i < 3; i++) {
      M.data[i][i] = t.get(i);
    }
    return M;
  }

  public static M4x4 rotationMatrix(M3d axis, double phi) {
    M4x4 M = new M4x4();
    double data[][] = M.getData();
    double c = Math.cos(phi);
    double s = Math.sin(phi);
    double t = 1-c;
    double x, y, z;

    axis = axis.normalized();
    x = axis.getX();
    y = axis.getY();
    z = axis.getZ();
    data[0][0] = t*x*x+c;   data[0][1] = t*x*y+s*z; data[0][2] = t*x*z-s*y; data[0][3] = 0;
    data[1][0] = t*x*y-s*z; data[1][1] = t*y*y+c;   data[1][2] = t*y*z+s*x; data[1][3] = 0;
    data[2][0] = t*x*z+s*y; data[2][1] = t*y*z-s*x; data[2][2] = t*z*z+c;   data[2][3] = 0;
    data[3][0] = 0;         data[3][1] = 0;         data[3][2] = 0;         data[3][3] = 1;
    return M;
  }

  public M4x4 translated(M3d t) {
    return translationMatrix(t).times(this);
  }

  public M4x4 scaled(M3d t) {
    return scaleMatrix(t).times(this);
  }

  public M4x4 rotated(M3d axis, double phi) {
    return rotationMatrix(axis, phi).times(this);
  }

  public static M4x4 perspective(double aspectRatioWidthOverHeight) {
    return perspective(50.0 * 2.0 * Math.PI / 360.0, aspectRatioWidthOverHeight, 0.01, 20);
  }

  public static M4x4 perspective(double fovY, double aspect, double zNear, double zFar) {
    M4x4 M = new M4x4();
    double f = 1.0 / Math.tan(fovY / 2.0);
    M.data[0][0] = f / aspect;
    M.data[1][1] = f;
    M.data[2][2] = (zNear + zFar) / (zNear - zFar);
    M.data[2][3] = (2 * zNear * zFar) / (zNear - zFar);
    M.data[3][2] = -1;
    M.data[3][3] = 0;
    return M;
  }

  public M3d getCol(int col) {
    return new M3d(data[0][col], data[1][col], data[2][col]);
  }

  public M3d getRow(int row) {
    return new M3d(data[row][0], data[row][1], data[row][2]);
  }

  public M4x4 extract3x3() {
    M4x4 M = new M4x4();
    for (int col = 0; col < 3; col++) {
      for (int row = 0; row < 3; row++) {
        M.data[row][col] = data[row][col];
      }
    }
    return M;
  }

  public M4x4 inverted() {
    double determinant = determinant(data);
    M4x4 inverted = cofactor().transposed().times(1.0 / determinant);

    M4x4 check = this.times(inverted);
    check = check.minus(M4x4.identity());
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        if (Math.abs(check.data[row][col]) > 0.00001) {
          throw new IllegalArgumentException(
              "Invert() implementation bug while attempting to invert matrix.\nSource =\n"
              + this + "\nInverted = " + inverted + "\nProduct = " + this.times(inverted));
        }
      }
    }

    return inverted;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    int i = 0;
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        if (i % 4 == 0) {
          builder.append("[ ");
        }
        builder.append(data[row][col]);
        if ((i+1) % 4 == 0) {
          builder.append("]" + ((i < 15) ? "\n" : ""));
        } else if (i < 15) {
          builder.append(", ");
        }
        i++;
      }
    }
    return builder.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        data[0][0], data[0][1], data[0][2], data[0][3],
        data[1][0], data[1][1], data[1][2], data[1][3],
        data[2][0], data[2][1], data[2][2], data[2][3],
        data[3][0], data[3][1], data[3][2], data[3][3]);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof M4x4) ? Arrays.deepEquals(data, ((M4x4) obj).data) : false;
  }

  /////////////////////////////////////////////////////////////////////////////

  private M4x4 cofactor() {
    double[][] source = data;
    M4x4 cofactor = new M4x4();
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        cofactor.data[row][col] =
            flipSign(row) * flipSign(col) * determinant(extractSubMatrix(source, row, col));
      }
    }

    return cofactor;
  }

  private static double determinant(double[][] matrix) {
    if (matrix.length == 1) {
      return matrix[0][0];
    }
    if (matrix.length == 2) {
      return (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]);
    }
    double sum = 0.0;
    for (int i = 0; i < matrix.length; i++) {
      sum += flipSign(i) * matrix[0][i] * determinant(extractSubMatrix(matrix, 0, i));
    }
    return sum;
  }

  private static double[][] extractSubMatrix(double[][] source, int excludeRow, int excludeCol) {
    double[][] dest = new double[source.length - 1][source.length - 1];
    int destRow = 0, destCol = 0;
    for (int srcRow = 0; srcRow < source.length; srcRow++) {
      if (srcRow != excludeRow) {
        for (int srcCol = 0; srcCol < source.length; srcCol++) {
          if (srcCol != excludeCol) {
            dest[destRow][destCol] = source[srcRow][srcCol];
            destCol++;
          }
        }
        destRow++;
        destCol = 0;
      }
    }
    return dest;
  }

  private static int flipSign(int i) {
    return ((i & 1) == 1) ? -1 : 1;
  }
}
