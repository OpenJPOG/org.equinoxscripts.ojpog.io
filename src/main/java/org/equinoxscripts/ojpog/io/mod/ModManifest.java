package org.equinoxscripts.ojpog.io.mod;

public class ModManifest {
	public static final String MANIFEST_FILE_NAME = "manifest.json";
	
	public final String name;
	public final String author;
	public final String url;
	public final ModVersion version;

	public ModManifest(String name, String author, String url, ModVersion version) {
		this.name = name;
		this.author = author;
		this.url = url;
		this.version = version;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ author.hashCode() ^ url.hashCode() ^ version.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ModManifest) {
			ModManifest v = (ModManifest) o;
			return v.name.equals(name) && v.author.equals(author) && v.url.equals(url) && v.version.equals(version);
		}
		return false;
	}
}
