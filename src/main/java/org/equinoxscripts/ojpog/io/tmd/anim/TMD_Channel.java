package org.equinoxscripts.ojpog.io.tmd.anim;

import java.nio.ByteBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.equinoxscripts.ojpog.io.tmd.TMD_IO;
import org.equinoxscripts.ojpog.io.tmd.scene.TMD_Node;

public class TMD_Channel extends TMD_IO {
	public final TMD_Animation animation;
	public TMD_Node nodeRef;

	public final boolean usePositionKeys;
	public final boolean ignoreThisChannel;
	public final TMD_KeyFrame[] frames;
	public int nodeID;
	public int nodeRemap;
	public final int offset;

	public TMD_Channel(TMD_Animation animation, ByteBuffer data) {
		super(animation.file);
		this.offset = data.position();
		this.animation = animation;
		short tmp = data.getShort();
		this.usePositionKeys = bool(tmp & 1);
		this.ignoreThisChannel = bool((tmp >> 1) & 1);
		if ((tmp & ~3) != 0)
			System.err.println("Channel has bad bitfield");
		this.frames = new TMD_KeyFrame[data.getShort() & 0xFFFF];
		for (int i = 0; i < this.frames.length; i++)
			this.frames[i] = new TMD_KeyFrame(this, data);
	}

	@Override
	public int length() {
		int j = 2 + 2;
		for (TMD_KeyFrame f : frames)
			j += f.length();
		return j;
	}

	@Override
	public void write(ByteBuffer b) {
		b.putShort((short) ((usePositionKeys ? 1 : 0) | (ignoreThisChannel ? 2 : 0)));
		b.putShort((short) frames.length);
		for (TMD_KeyFrame f : frames)
			f.write(b);
	}

	public boolean shouldIgnore() {
		// unk1==1 recursive include?
		// unk1==2 singular include?
		// if (!animation.shouldPruneChannels())
		// return false;
		// return unk1_M2;
		return ignoreThisChannel;
	}

	@Override
	public void link() {
		for (TMD_KeyFrame frame : frames)
			frame.link();
	}

	private int frameFor(float time) {
		if (frames[0].time > time)
			return 0;
		for (int i = 1; i < frames.length; i++)
			if (frames[i].time > time)
				return i - 1;
		return frames.length - 1;
	}

	public int value(float t, Vector3f tmp3, Quat4f tmpQ, boolean local) {
		int frame = frameFor(t);
		TMD_KeyFrame act = frames[frame];
		tmpQ.set(local ? act.localRot : act.rot);
		tmp3.set(local ? act.localPos : act.pos);
		if (frame < frames.length - 1) {
			TMD_KeyFrame ot = frames[frame + 1];
			float s = (t - act.time) / (ot.time - act.time);
			if (!Float.isFinite(s) || s < 0)
				s = 0;
			else if (s > 1)
				s = 1;
			tmpQ.interpolate(local ? ot.localRot : ot.rot, s);
			tmpQ.normalize();
			tmp3.interpolate(local ? ot.localPos : ot.pos, s);
		}
		return frame;
	}
}