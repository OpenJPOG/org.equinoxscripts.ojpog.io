package org.equinoxscripts.ojpog.io.tmd.scene;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.equinoxscripts.ojpog.io.IOUtils;
import org.equinoxscripts.ojpog.io.VectorUtils;
import org.equinoxscripts.ojpog.io.tmd.TMD_IO;

public class TMD_Node extends TMD_IO {
	private final TMD_Node_Block scene;

	public TMD_Node(TMD_Node_Block block, ByteBuffer b, int id) throws UnsupportedEncodingException {
		super(block.file);
		this.scene = block;
		this.id = id;
		Quat4f rotation = IOUtils.readQ(b);
		this.worldSkinningMatrix = IOUtils.readM4(b);
		this.worldSkinningMatrix_Inv = IOUtils.readM4(b);

		@SuppressWarnings("unused")
		byte len = b.get(); // length of node_name (in theory)
		this.node_name = read(b, 15);
		this.parent = b.getShort();
		this.noMesh = bool(b.getShort());
		// unknown
		this.matrix2 = new Matrix4f();
		this.matrix2.setIdentity();
		this.matrix2.setRotation(rotation);
		Vector3f tra = IOUtils.readV3(b);
		this.matrix2.setTranslation(tra);
	}

	@Override
	public int length() {
		int len = 4 * 4;
		len += 4 * 16;
		len += 4 * 16;
		len += 1 + 15;
		len += 4;
		len += 4 * 3;
		return len;
	}

	@Override
	public void write(ByteBuffer b) {
		Quat4f rot = VectorUtils.matrixToQuat(matrix2);
		IOUtils.writeQ(b, rot);
		IOUtils.writeM4(b, worldSkinningMatrix);
		IOUtils.writeM4(b, worldSkinningMatrix_Inv);
		b.put((byte) node_name.length());
		write(b, 15, node_name);
		b.putShort(this.parent);
		b.putShort((short) (this.noMesh ? 1 : 0));
		Vector3f tra = new Vector3f(matrix2.m03, matrix2.m13, matrix2.m23);
		IOUtils.writeV3(b, tra);
	}

	public final int id;
	/**
	 * Written to file
	 */
	public final Matrix4f worldSkinningMatrix, worldSkinningMatrix_Inv;
	/**
	 * Written to file
	 */
	public final Matrix4f matrix2;

	public final String node_name;
	public final short parent;
	/**
	 * <em>1</em> if there is no mesh attached to me or any decendents.
	 */
	public final boolean noMesh;

	@Override
	public String toString() {
		return node_name;
	}

	public Matrix4f localPosition() {
		TMD_Node parent = parentRef();
		Matrix4f tmp = new Matrix4f();
		if (parent != null)
			tmp.mul(parent.worldSkinningMatrix_Inv, worldSkinningMatrix);
		else
			tmp.set(worldSkinningMatrix);
		return tmp;
	}

	public TMD_Node parentRef() {
		return parent >= 0 ? this.scene.nodes[parent & 0xFF] : null;
	}

	public Set<TMD_Node> children() {
		Set<TMD_Node> ss = new HashSet<>();
		for (TMD_Node n : this.scene.nodes)
			if (n.parent == this.id)
				ss.add(n);
		return Collections.unmodifiableSet(ss);
	}

	@Override
	public void link() {
	}
}
