package org.equinoxscripts.ojpog.io.tml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.equinoxscripts.ojpog.io.Gen_IO;

public class TML_File extends Gen_IO {
	public int unknown;
	public TML_Texture[] textures;
	public String[] stringTable;

	public class TML_Material extends Gen_IO {
		public String name;
		public TML_Texture[] textures;

		public static final short UNKNOWN_FOR_BACKGROUND = 16388;

		// 16388 == background,
		// oother values...
		public short unknown;

		public TML_Material(ByteBuffer b) {
			this.name = stringTable[b.getInt()];
			this.unknown = b.getShort();
			this.textures = new TML_Texture[b.getShort()];
			for (int i = 0; i < textures.length; i++) {
				int key = b.getInt();
				for (TML_Texture t : TML_File.this.textures)
					if (t.textureID == key) {
						this.textures[i] = t;
						break;
					}
			}
		}

		public TML_Material(String nam) {
			this.name = nam;
			this.unknown = 0;
			// basic logic to choose a better unknown value:
			if (this.name.toLowerCase().startsWith("bkg"))
				this.unknown = UNKNOWN_FOR_BACKGROUND;
			this.textures = new TML_Texture[0];
		}

		public int key() {
			int key = -1;
			for (int i = 0; i < stringTable.length; i++)
				if (name.equals(stringTable[i])) {
					key = i;
					break;
				}
			return key;
		}

		@Override
		public void write(ByteBuffer b) throws IOException {
			int key = key();
			if (key == -1)
				throw new IOException();
			b.putInt(key);
			b.putShort(unknown);
			b.putShort((short) this.textures.length);
			for (TML_Texture t : textures)
				b.putInt(t.textureID);
		}

		@Override
		public int length() throws IOException {
			return 4 + 2 + 2 + 4 * textures.length;
		}

	}

	public final Map<String, TML_Material> stringMapping;

	public TML_File(ByteBuffer data) throws UnsupportedEncodingException, IOException {
		data.position(0);
		if (!read(data, 4).equals("TML1"))
			throw new IOException("Bad magic");
		this.unknown = data.getInt();
		this.textures = new TML_Texture[data.getInt()];
		for (int i = 0; i < this.textures.length; i++)
			this.textures[i] = new TML_Texture(data);
		if (this.textures.length != 0)
			stringTable = new String[data.getInt()];
		else
			stringTable = new String[0];
		for (int i = 0; i < stringTable.length; i++)
			stringTable[i] = read(data, 32);
		this.stringMapping = new LinkedHashMap<>();
		for (int i = 0; i < stringTable.length; i++) {
			TML_Material ref = new TML_Material(data);
			stringMapping.put(ref.name, ref);
		}
	}

	public TML_File() {
		this.unknown = 0;
		this.textures = new TML_Texture[0];
		this.stringTable = new String[0];
		this.stringMapping = new HashMap<>();
	}

	private static final Comparator<String> COMP = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}
	};

	@Override
	public void write(ByteBuffer b) throws IOException {
		Arrays.sort(stringTable, COMP);
		// Reorder the texture table and cleans it up. Scary operation.
		{
			List<Object[]> tex = new ArrayList<>();
			for (TML_Material m : stringMapping.values())
				for (int i = 0; i < m.textures.length; i++)
					tex.add(new Object[] { m.name + "_" + i, m.textures[i] });
			tex.sort(new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return COMP.compare((String) o1[0], (String) o2[0]);
				}
			});
			Set<TML_Texture> add = new HashSet<>();
			int texid = 0;
			for (Object[] o : tex) {
				TML_Texture t = (TML_Texture) o[1];
				if (add.add(t)) {
					TML_Texture out = new TML_Texture(texid);
					out.set(t);
					for (TML_Material m : stringMapping.values())
						for (int i = 0; i < m.textures.length; i++)
							if (m.textures[i] == t)
								m.textures[i] = out;
					this.textures[texid++] = out;
				}
			}
			this.textures = Arrays.copyOf(this.textures, texid);
		}

		write(b, 4, "TML1");
		b.putInt(this.unknown);
		b.putInt(this.textures.length);
		for (int i = this.textures.length - 1; i >= 0; i--)
			this.textures[i].write(b);
		b.putInt(stringTable.length);
		for (int i = 0; i < stringTable.length; i++)
			write(b, 32, stringTable[i]);
		List<Object[]> bas = new ArrayList<>();
		for (TML_Material r : stringMapping.values())
			bas.add(new Object[] { r.key(), r });
		bas.sort((a, bf) -> ((Integer) a[0]).compareTo((Integer) bf[0]));
		for (Object[] f : bas)
			((TML_Material) f[1]).write(b);
	}

	@Override
	public int length() throws IOException {
		int len = 4 + 4 + 4;
		for (TML_Texture t : this.textures)
			len += t.length();
		len += 4 + 32 * stringTable.length;
		for (TML_Material r : stringMapping.values())
			len += r.length();
		return len;
	}

	public int addOrGetString(String s) {
		for (int i = 0; i < stringTable.length; i++)
			if (stringTable[i].equals(s))
				return i;
		stringTable = Arrays.copyOf(stringTable, stringTable.length + 1);
		stringTable[stringTable.length - 1] = s;
		return stringTable.length - 1;
	}

	public TML_Material createOrGetMaterial(String name) {
		if (stringMapping.containsKey(name))
			return stringMapping.get(name);
		addOrGetString(name);
		TML_Material out = new TML_Material(name);
		stringMapping.put(name, out);
		return out;
	}

	public TML_Texture getFreeTexture() {
		// find a free texture id.
		List<Integer> usedIDs = new ArrayList<>();
		for (TML_Texture t : textures)
			usedIDs.add(t.textureID);
		usedIDs.sort(null);
		int id;
		if (usedIDs.isEmpty())
			id = 0;
		else
			id = usedIDs.get(usedIDs.size() - 1) + 1;
		for (int i = 0; i < usedIDs.size() - 1; i++)
			if (usedIDs.get(i) + 1 != usedIDs.get(i + 1)) {
				id = usedIDs.get(i + 1);
				break;
			}
		textures = Arrays.copyOf(textures, textures.length + 1);
		textures[textures.length - 1] = new TML_Texture(id);
		return textures[textures.length - 1];
	}
}
