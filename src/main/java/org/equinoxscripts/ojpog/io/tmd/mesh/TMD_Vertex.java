package org.equinoxscripts.ojpog.io.tmd.mesh;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.equinoxscripts.ojpog.io.IOUtils;
import org.equinoxscripts.ojpog.io.tmd.TMD_IO;

public class TMD_Vertex extends TMD_IO {
	public static final int SIZEOF = 4 * (3 + 3 + 2 + 2);

	public TMD_Vertex(ByteBuffer b) {
		position = IOUtils.readV3(b);
		normal = IOUtils.readV3(b);
		b.get(skinningInfo);
		texpos = new Vector2f(b.getFloat(), b.getFloat());
	}

	public TMD_Vertex(Vector3f pos, Vector3f nrm, Vector2f tex, Map<Integer, Float> weights) {
		this.position = pos;
		this.normal = nrm;
		this.texpos = tex;
		List<Entry<Integer, Float>> binds = new ArrayList<>(weights.entrySet());
		binds.sort((a, b) -> -Float.compare(a.getValue(), b.getValue()));
		weightsBoneID(weights);
	}

	@Override
	public void write(ByteBuffer b) {
		IOUtils.writeV3(b, position);
		IOUtils.writeV3(b, normal);
		b.put(skinningInfo);
		b.putFloat(texpos.x);
		b.putFloat(texpos.y);
	}

	@Override
	public int length() {
		return SIZEOF;
	}

	public final Vector3f position, normal;
	private final byte[] skinningInfo = new byte[8];
	public final Vector2f texpos;

	public TMD_Mesh_Piece user;

	public void usedBy(TMD_Mesh_Piece p) {
		if (user != null) {
			// check binding consistency
			Map<Integer, Float> ops = weightsBoneID();
			for (Entry<Integer, Float> o : ops.entrySet())
				if (o.getValue() > 0.01f)
					if (p.meshParents[o.getKey()] != user.meshParents[o.getKey()])
						System.err.println("Binding mismatch");
		}
		user = p;
	}

	public Map<Integer, Float> weightsBoneID() {
		Map<Integer, Float> weights = new HashMap<>();
		for (int i = 0; i < 4; i++) {
			int boneID = (skinningInfo[4 + i] & 0xFF) / 3;
			float boneWeight = (skinningInfo[i] & 0xFF) / 255f;
			if (boneWeight > 0)
				weights.put(boneID, boneWeight);
		}
		return Collections.unmodifiableMap(weights);
	}

	public Map<Integer, Float> weightsNodeID() {
		Map<Integer, Float> f = weightsBoneID();
		Map<Integer, Float> out = new HashMap<>();
		for (Entry<Integer, Float> e : f.entrySet())
			out.put(user.meshParents[e.getKey().intValue()], e.getValue());
		return Collections.unmodifiableMap(out);
	}

	public void weightsBoneID(Map<Integer, Float> store) {
		for (int i = 0; i < 8; i++)
			skinningInfo[i] = 0;
		if (store == null) {
			skinningInfo[4] = (byte) 0xFF;
			skinningInfo[0] = 0;
		} else {
			int i = 0;
			List<Entry<Integer, Float>> lst = new ArrayList<>(store.entrySet());
			lst.sort((a, b) -> -a.getValue().compareTo(b.getValue()));
			for (Entry<Integer, Float> e : lst) {
				if (i >= 4)
					break;
				skinningInfo[i] = (byte) (0xFF * e.getValue().floatValue());
				skinningInfo[i + 4] = (byte) (3 * e.getKey().intValue());
				i++;
			}
		}
	}

	public boolean weightsEqualEpsilon(TMD_Vertex other, float eps) {
		Map<Integer, Float> mwbi = weightsNodeID();
		Map<Integer, Float> owbi = other.weightsNodeID();
		if (!mwbi.keySet().equals(owbi.keySet()))
			return false;
		for (Entry<Integer, Float> ft : mwbi.entrySet())
			if (Math.abs(ft.getValue() - owbi.get(ft.getKey())) > eps)
				return false;
		return true;
	}
}