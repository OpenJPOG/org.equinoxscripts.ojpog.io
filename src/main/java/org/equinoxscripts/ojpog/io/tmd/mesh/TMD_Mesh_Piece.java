package org.equinoxscripts.ojpog.io.tmd.mesh;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.vecmath.Vector3f;

import org.equinoxscripts.ojpog.io.IOUtils;
import org.equinoxscripts.ojpog.io.tmd.TMD_File;
import org.equinoxscripts.ojpog.io.tmd.TMD_IO;

public class TMD_Mesh_Piece extends TMD_IO {
	// this seems to be a bone mapping, NOT a nodes-with-this-mesh mapping
	public final int[] meshParents;
	public int[] meshParentInverse;
	public final Vector3f boundingCenter, boundingExtents;

	private boolean loadedData;
	private final ByteBuffer vertex, index;

	public final TMD_Vertex[] verts;
	public final short[] tri_strip;
	public final int vertsRequired;

	public TMD_Mesh_Piece(TMD_File file, TMD_Vertex[] verts, short[] tris, int[] meshParents) {
		super(file);
		this.meshParents = meshParents;

		this.boundingCenter = new Vector3f();
		this.boundingExtents = new Vector3f();

		this.loadedData = true;
		this.vertex = this.index = null;

		this.verts = verts;
		this.tri_strip = tris;
		this.vertsRequired = this.verts.length;
	}

	public TMD_Mesh_Piece(TMD_File file, int requiredVerts, short[] tris, int[] meshParents) {
		super(file);
		this.meshParents = meshParents;

		this.boundingCenter = new Vector3f();
		this.boundingExtents = new Vector3f();

		this.loadedData = true;
		this.vertex = this.index = null;

		this.verts = new TMD_Vertex[0];
		this.tri_strip = tris;
		this.vertsRequired = requiredVerts;
	}

	private void computeBB(TMD_Vertex[] vtable, Vector3f center, Vector3f extent) {
		Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		for (short i : this.tri_strip) {
			Vector3f pos = vtable[i].position;
			min.x = Math.min(min.x, pos.x);
			min.y = Math.min(min.y, pos.y);
			min.z = Math.min(min.z, pos.z);
			max.x = Math.max(max.x, pos.x);
			max.y = Math.max(max.y, pos.y);
			max.z = Math.max(max.z, pos.z);
		}
		center.add(min, max);
		center.scale(0.5f);
		extent.sub(max, this.boundingCenter);
	}

	public void computeBB(TMD_Vertex[] vtable) {
		computeBB(vtable, this.boundingCenter, this.boundingExtents);
	}

	public TMD_Mesh_Piece(TMD_Mesh root, ByteBuffer b) {
		super(root.file);
		int tris = b.getInt();
		int verts = b.getInt();
		int num_nodes = b.getInt();
		vertsRequired = b.getInt();

		boundingCenter = IOUtils.readV3(b);
		boundingExtents = IOUtils.readV3(b);

		if (num_nodes > 10e6 || tris > 10e6 || verts > 10e6)
			throw new RuntimeException("Bad mesh size " + num_nodes + ", " + tris + ", " + verts);
		meshParents = new int[num_nodes];
		for (int j = 0; j < meshParents.length; j++)
			meshParents[j] = b.getInt();

		this.verts = new TMD_Vertex[verts];
		this.tri_strip = new short[tris];

		int origLim = b.limit();

		int vertexOffset = b.position();
		int indexOffset = vertexOffset + verts * TMD_Vertex.SIZEOF;
		int indexEnd = indexOffset + tris * 2;
		b.position(vertexOffset);
		b.limit(indexOffset);
		this.vertex = b.slice();
		this.vertex.order(b.order());
		b.position(indexOffset);
		b.limit(indexEnd);
		this.index = b.slice();
		this.index.order(b.order());
		b.position(indexEnd);

		b.limit(origLim);
	}

	@Override
	public void write(ByteBuffer b) {
		b.putInt(this.tri_strip.length);
		b.putInt(this.verts.length);
		b.putInt(this.meshParents.length);
		b.putInt(vertsRequired);
		IOUtils.writeV3(b, boundingCenter);
		IOUtils.writeV3(b, boundingExtents);
		for (int j : meshParents)
			b.putInt(j);
		for (TMD_Vertex v : this.verts)
			v.write(b);
		for (short s : tri_strip)
			b.putShort(s);
	}

	@Override
	public int length() {
		int len = 4 + 4 + 4 + 4 + 12 + 12;
		len += 4 * meshParents.length;
		for (TMD_Vertex v : this.verts)
			len += v.length();
		len += 2 * tri_strip.length;
		return len;
	}

	public boolean isSkinned() {
		return true;
	}

	public void loadVtxAndTri() {
		if (loadedData)
			return;
		vertex.position(0);
		for (int i = 0; i < verts.length; i++)
			verts[i] = new TMD_Vertex(vertex);
		index.position(0);
		shorts(index, tri_strip);
		loadedData = true;
	}

	public int maxBindingsPerVertex;

	@Override
	public void link() {
		meshParentInverse = new int[file.nodes.nodes.length];
		Arrays.fill(meshParentInverse, -1);
		for (int i = 0; i < meshParents.length; i++)
			meshParentInverse[meshParents[i]] = i;
		loadVtxAndTri();

		if (isSkinned()) {
			maxBindingsPerVertex = 0;
			for (TMD_Vertex v : verts) {
				maxBindingsPerVertex = Math.max(maxBindingsPerVertex, v.weightsBoneID().size());
			}
		}
	}

	public void checkBoundingBox() {
		// Vector3f center = new Vector3f();
		// Vector3f extent = new Vector3f();
		// computeBB(verts, center, extent);
		// if (verts.length == 0)
		// extent.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
		// Float.NEGATIVE_INFINITY);
		// if (!center.epsilonEquals(boundingCenter, 1e-4f))
		// System.err.println("Piece bounding centers inequal: " + center + " vs
		// " + boundingCenter);
		// if (!extent.epsilonEquals(boundingExtents, 1e-4f))
		// System.err.println("Piece bounding extents inequal: " + extent + " vs
		// " + boundingExtents);
	}
}
