package org.equinoxscripts.ojpog.io.mod;

public class ModVersion implements Comparable<ModVersion> {
	public final int major;
	public final int minor;
	public final int patch;

	public ModVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	@Override
	public int compareTo(ModVersion v) {
		if (major != v.major)
			return Integer.compare(major, v.major);
		if (minor != v.minor)
			return Integer.compare(minor, v.minor);
		return Integer.compare(patch, v.patch);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ModVersion) {
			return major == ((ModVersion) o).major && minor == ((ModVersion) o).minor
					&& patch == ((ModVersion) o).patch;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (major << 8) ^ (minor << 4) ^ (patch << 0);
	}
}
