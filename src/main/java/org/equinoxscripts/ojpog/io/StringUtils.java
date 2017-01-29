package org.equinoxscripts.ojpog.io;

import java.lang.reflect.Array;

public class StringUtils {
	public static String divide(String s, int n) {
		StringBuilder out = new StringBuilder(s.length() + (s.length() / n) + 10);
		for (int i = 0; i < s.length(); i += n) {
			out.append(s.substring(i, Math.min(s.length(), i + n)));
			out.append(' ');
		}
		return out.toString();
	}

	public static String hex(byte[] d) {
		StringBuilder sb = new StringBuilder();
		for (byte f : d) {
			String s = Integer.toHexString(f & 0xFF);
			if (s.length() < 2)
				sb.append("0");
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String pad(String s, int n) {
		while (s.length() < n)
			s = "0" + s;
		return s;
	}

	public static String rpad(String s, int n) {
		while (s.length() < n)
			s = s + " ";
		return s;
	}

	public static String toHexString(Object... f) {
		if (f.length == 1)
			return toHexStringA(f[0]);
		return toHexStringA(f);
	}

	public static String toHexStringA(Object f) {
		StringBuilder s = new StringBuilder("[");
		for (int i = 0; i < Array.getLength(f); i++) {
			Object o = Array.get(f, i);
			long v = ((Number) o).longValue();
			int pad = 16;
			if (o instanceof Long)
				pad = 16;
			else if (o instanceof Integer)
				pad = 8;
			else if (o instanceof Short)
				pad = 4;
			else if (o instanceof Byte)
				pad = 2;
			if (i > 0)
				s.append(", ");
			s.append(pad(Long.toHexString(v), pad));
		}
		return s.append("]").toString();
	}
}
