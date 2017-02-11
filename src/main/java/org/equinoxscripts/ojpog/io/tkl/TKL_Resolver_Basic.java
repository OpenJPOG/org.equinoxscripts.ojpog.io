package org.equinoxscripts.ojpog.io.tkl;

import java.io.File;
import java.io.IOException;

import org.equinoxscripts.ojpog.io.IOUtils;

public class TKL_Resolver_Basic implements TKL_Resolver {
	private final File repository;

	public TKL_Resolver_Basic(File repo) {
		this.repository = repo;
	}

	@Override
	public TKL_File resolve(String name) {
		try {
			return new TKL_File(IOUtils.read(new File(repository, name + ".tkl")));
		} catch (IOException e) {
			return null;
		}
	}

}
