package org.equinoxscripts.ojpog.io.tmd;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.equinoxscripts.ojpog.io.IOUtils;
import org.equinoxscripts.ojpog.io.tkl.TKL_File;
import org.equinoxscripts.ojpog.io.tkl.TKL_Resolver;
import org.equinoxscripts.ojpog.io.tkl.TKL_Resolver_Basic;
import org.equinoxscripts.ojpog.io.tmd.anim.TMD_Animation;
import org.equinoxscripts.ojpog.io.tmd.anim.TMD_Animation_Block;
import org.equinoxscripts.ojpog.io.tmd.mesh.TMD_DLoD_Block;
import org.equinoxscripts.ojpog.io.tmd.mesh.TMD_Mesh;
import org.equinoxscripts.ojpog.io.tmd.mesh.TMD_Mesh_Piece;
import org.equinoxscripts.ojpog.io.tmd.scene.TMD_Node_Block;

public class TMD_File extends TMD_IO {
	public final TMD_DLoD_Block dLoD;

	public final String source;

	public final TMD_Header_Block header;
	public final TMD_Node_Block nodes;
	public final TMD_Animation_Block animations;

	public TMD_File(File f) throws IOException {
		this(f, IOUtils.read(f));
	}

	public TMD_File(String source, ByteBuffer data, TKL_Resolver resolver) throws IOException {
		super(null);
		this.source = source;
		this.file = this;
		this.header = new TMD_Header_Block(this, data);
		this.nodes = new TMD_Node_Block(this, data);
		this.animations = new TMD_Animation_Block(this, data);
		this.dLoD = new TMD_DLoD_Block(this, data);
		this.tklRepo = resolver.resolve(this.header.category);
		if (this.tklRepo == null)
			throw new IOException("Failed to resolve the animation repository");
		this.link();
	}

	public TMD_File(File f, ByteBuffer data) throws IOException {
		this(f.getName().substring(0, f.getName().length() - 4), data, new TKL_Resolver_Basic(f.getParentFile()));
	}

	@Override
	public int length() throws IOException {
		int size = header.length();
		size = Math.max(size, header.nodeArrayOffset + nodes.length());
		size = Math.max(size, header.animationDataOffset + animations.length());
		size = Math.max(size, header.meshBlockOffset() + dLoD.length());
		return size;
	}

	@Override
	public void write(ByteBuffer data) throws IOException {
		header.write(data);
		data.position(header.nodeArrayOffset);
		nodes.write(data);
		data.position(header.animationDataOffset);
		animations.write(data);
		data.position(header.meshBlockOffset());
		dLoD.write(data);
	}

	public String summary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nodes: ").append("\n-").append(this.nodes.sceneGraph(a -> "").replace("\n", "\n-")).append("\n");
		sb.append("Animations:").append("\n");
		for (TMD_Animation a : animations.animations)
			sb.append("-" + a.name + ": " + a.length + " sec").append("\n");
		sb.append("Meshes:").append("\n");
		for (TMD_Mesh m : dLoD.levels[0].members) {
			sb.append("-Mesh mat=" + m.material_name + ", v=" + m.verts.length + ", t=" + m.totalTriStripLength
					+ ", pieces=" + m.pieces.length).append("\n");
			for (TMD_Mesh_Piece p : m.pieces) {
				int[] kl = new int[p.tri_strip.length];
				for (int k = 0; k < kl.length; k++)
					kl[k] = p.tri_strip[k] & 0xFFFF;
				sb.append("--Piece contains " + p.verts.length + " v, uses " + p.vertsRequired + " v, t="
						+ p.tri_strip.length + ", bones=" + p.meshParents.length).append("\n");
			}
		}
		return sb.toString();
	}

	public void updateIntegrity() throws IOException {
		header.fileLength = length() - 12;
	}

	public final TKL_File tklRepo;

	@Override
	public void link() {
		this.header.link();
		this.nodes.link();
		this.animations.link();
		this.dLoD.link();
	}
}
