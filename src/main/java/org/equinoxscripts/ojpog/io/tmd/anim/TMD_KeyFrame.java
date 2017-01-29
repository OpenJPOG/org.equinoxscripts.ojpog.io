package org.equinoxscripts.ojpog.io.tmd.anim;

import java.nio.ByteBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.equinoxscripts.ojpog.io.tmd.TMD_IO;

public class TMD_KeyFrame extends TMD_IO {
	public final float time;
	public final int posKey, rotKey;

	public final Vector3f pos = new Vector3f();
	public final Quat4f rot = new Quat4f();

	public final Vector3f localPos = new Vector3f();
	public final Quat4f localRot = new Quat4f();

	public TMD_KeyFrame(TMD_Channel channel, ByteBuffer data) {
		super(channel.file);
		time = data.getFloat();
		posKey = data.getShort() & 0xFFFF;
		rotKey = data.getShort() & 0xFFFF;
	}

	@Override
	public void link() {
		if (posKey == 0 && file.tklRepo.positions.length == 0)
			this.pos.set(0, 0, 0);
		else
			this.pos.set(file.tklRepo.positions[posKey]);
		if (rotKey == 0 && file.tklRepo.rotations.length == 0)
			this.rot.set(0, 0, 0, 1);
		else
			this.rot.set(file.tklRepo.rotations[rotKey]);
	}

	@Override
	public String toString() {
		return "{" + time + ": " + pos.toString() + ", " + rot.toString() + "}";
	}

	@Override
	public void write(ByteBuffer b) {
		b.putFloat(time);
		b.putShort((short) posKey);
		b.putShort((short) rotKey);
	}

	@Override
	public int length() {
		return 4 + 2 + 2;
	}
}