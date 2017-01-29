package org.equinoxscripts.ojpog.io.tmd.scene;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.function.Function;

import org.equinoxscripts.ojpog.io.tmd.TMD_File;
import org.equinoxscripts.ojpog.io.tmd.TMD_IO;

public class TMD_Node_Block extends TMD_IO {
	public final TMD_Node[] nodes;

	public TMD_Node_Block(TMD_File file, ByteBuffer data) throws UnsupportedEncodingException {
		super(file);
		data.position(file.header.nodeArrayOffset);

		nodes = new TMD_Node[file.header.numNodes];
		for (int i = 0; i < file.header.numNodes; i++) {
			nodes[i] = new TMD_Node(this, data, i);
		}
	}

	@Override
	public void link() {
		for (TMD_Node node : nodes) {
			node.link();
		}
	}

	@Override
	public int length() {
		int len = 0;
		for (int i = 0; i < len; i++)
			len += nodes[i].length();
		return len;
	}

	@Override
	public void write(ByteBuffer b) {
		for (TMD_Node n : nodes)
			n.write(b);
	}

	private int calcMaxLen(TMD_Node node, String indent) {
		String my = indent + " - " + node.node_name;
		int l = my.length();
		for (TMD_Node n : node.children())
			l = Math.max(calcMaxLen(n, indent + "  "), l);
		return l;
	}

	private void graph(StringBuilder out, TMD_Node node, Function<TMD_Node, String> map, String indent, int pad) {
		StringBuilder my = new StringBuilder(pad);
		my.append(indent).append(" - ").append(node.node_name);
		while (my.length() < pad)
			my.append(' ');
		out.append(my).append(map.apply(node));
		for (TMD_Node c : node.children()) {
			out.append("\n");
			graph(out, c, map, indent + "  ", pad);
		}
	}

	public String sceneGraph(Function<TMD_Node, String> map) {
		StringBuilder graph = new StringBuilder();
		int pad = 0;
		for (TMD_Node n : nodes)
			if (n.parent < 0)
				pad = Math.max(pad, calcMaxLen(n, ""));
		pad += 4;

		for (TMD_Node n : nodes)
			if (n.parent < 0) {
				graph(graph, n, map, "", pad);
				graph.append("\n");
			}
		return graph.toString();
	}

	public TMD_Node byName(String s) {
		for (TMD_Node n : nodes)
			if (n.node_name.equalsIgnoreCase(s))
				return n;
		return null;
	}
}
