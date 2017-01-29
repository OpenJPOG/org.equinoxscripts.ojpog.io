package org.equinoxscripts.ojpog.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class IOUtils {
	public static void verifyMagic(ByteBuffer b, byte[] magic) {
		byte[] tmp = new byte[magic.length];
		b.get(tmp);
		for (int i = 0; i < tmp.length; i++)
			if (tmp[i] != magic[i])
				throw new RuntimeException("Bad magic: " + Arrays.toString(tmp));
	}

	public static ByteBuffer read(File f) {
		byte[] buffer = new byte[1024];
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream fin = new FileInputStream(f);
			while (true) {
				int s = fin.read(buffer);
				if (s > 0)
					bos.write(buffer, 0, s);
				else
					break;
			}
			fin.close();
			bos.close();
			return ByteBuffer.wrap(bos.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String makePermitted(String s) {
		char[] c = new char[s.length()];
		for (int i = 0; i < s.length(); i++)
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				c[i] = '_';
			else
				c[i] = s.charAt(i);
		return new String(c);
	}

	public static boolean isPermitted(String s) {
		for (int i = 0; i < s.length(); i++)
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		return true;
	}

	public static Vector3f nearestSegmentPoint(Vector3f s0, Vector3f s1, Vector3f p) {
		Vector3f v = new Vector3f(s1);
		v.sub(s0);
		Vector3f w = new Vector3f(p);
		w.sub(s0);
		float c1 = v.dot(w);
		if (c1 <= 0)
			return s0;
		float c2 = v.dot(v);
		if (c2 <= c1)
			return s1;
		float b = c1 / c2;

		w.set(s0);
		v.scale(b);
		w.add(v);
		return w;
	}

	public static Vector3f readV3(ByteBuffer b) {
		return new Vector3f(b.getFloat(), b.getFloat(), b.getFloat());
	}

	public static void writeV3(ByteBuffer b, Vector3f v) {
		b.putFloat(v.x);
		b.putFloat(v.y);
		b.putFloat(v.z);
	}

	public static Quat4f readQ(ByteBuffer b) {
		return new Quat4f(b.getFloat(), b.getFloat(), b.getFloat(), b.getFloat());
	}

	public static void writeQ(ByteBuffer b, Quat4f q) {
		b.putFloat(q.x);
		b.putFloat(q.y);
		b.putFloat(q.z);
		b.putFloat(q.w);
	}

	public static Matrix4f readM4(ByteBuffer b) {
		Matrix4f m = new Matrix4f();
		for (int c = 0; c < 4; c++)
			for (int r = 0; r < 4; r++)
				m.setElement(r, c, b.getFloat());
		return m;
	}

	public static void writeM4(ByteBuffer b, Matrix4f mat) {
		for (int c = 0; c < 4; c++)
			for (int r = 0; r < 4; r++)
				b.putFloat(mat.getElement(r, c));
	}

	public static void write(File file, ByteBuffer output) throws IOException {
		byte[] data = new byte[output.remaining()];
		output.get(data);
		FileOutputStream fio = new FileOutputStream(file);
		fio.write(data);
		fio.close();
	}
}
