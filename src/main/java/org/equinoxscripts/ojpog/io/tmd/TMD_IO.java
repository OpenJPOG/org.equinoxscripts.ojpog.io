package org.equinoxscripts.ojpog.io.tmd;

import org.equinoxscripts.ojpog.io.Gen_IO;

public abstract class TMD_IO extends Gen_IO {
	public TMD_File file;

	public TMD_IO(TMD_File file) {
		this.file = file;
	}

	public TMD_IO() {
	}

	public void link() {
	}
}
