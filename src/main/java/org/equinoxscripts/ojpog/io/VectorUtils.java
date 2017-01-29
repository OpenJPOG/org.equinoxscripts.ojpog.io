package org.equinoxscripts.ojpog.io;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

public class VectorUtils {
	private static float mag(float x, float y, float z) {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Sourced from libgdx.
	 * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java
	 * 
	 * @param mat
	 * @return
	 */
	public static Quat4f matrixToQuat(Matrix4f mat) {

		float xx = mat.m00, xy = mat.m01, xz = mat.m02;
		float yx = mat.m10, yy = mat.m11, yz = mat.m12;
		float zx = mat.m20, zy = mat.m21, zz = mat.m22;

		float x, y, z, w;

		if (true) {
			final float lx = 1f / mag(xx, xy, xz);
			final float ly = 1f / mag(yx, yy, yz);
			final float lz = 1f / mag(zx, zy, zz);
			xx *= lx;
			xy *= lx;
			xz *= lx;
			yx *= ly;
			yy *= ly;
			yz *= ly;
			zx *= lz;
			zy *= lz;
			zz *= lz;
		}
		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		final float t = xx + yy + zz;

		// we protect the division by s by ensuring that s>=1
		if (t >= 0) { // |w| >= .5
			float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5f * s;
			s = 0.5f / s; // so this division isn't bad
			x = (zy - yz) * s;
			y = (xz - zx) * s;
			z = (yx - xy) * s;
		} else if ((xx > yy) && (xx > zz)) {
			float s = (float) Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
			x = s * 0.5f; // |x| >= .5
			s = 0.5f / s;
			y = (yx + xy) * s;
			z = (xz + zx) * s;
			w = (zy - yz) * s;
		} else if (yy > zz) {
			float s = (float) Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
			y = s * 0.5f; // |y| >= .5
			s = 0.5f / s;
			x = (yx + xy) * s;
			z = (zy + yz) * s;
			w = (xz - zx) * s;
		} else {
			float s = (float) Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
			z = s * 0.5f; // |z| >= .5
			s = 0.5f / s;
			x = (xz + zx) * s;
			y = (zy + yz) * s;
			w = (yx - xy) * s;
		}

		return new Quat4f(x, y, z, w);
	}
}
