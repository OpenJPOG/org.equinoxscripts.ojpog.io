package org.equinoxscripts.ojpog.io.sav;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.equinoxscripts.ojpog.io.Gen_IO;
import org.equinoxscripts.ojpog.io.IOUtils;

public class S00_File extends Gen_IO {

	public static void main(String[] args) throws IOException {
		S00_File test = new S00_File(IOUtils.read(new File(
				"E:/Users/westin/Programming/ojpog-workspace/data/original/Data/SaveGame/Site B - 7 Jan 10-28 AM.s00")));
		BufferedImage tmp = new BufferedImage(test.binOffset.length * 4, test.binOffset[0].length * 4,
				BufferedImage.TYPE_INT_RGB);
		float minH = Float.MAX_VALUE;
		float maxH = -Float.MAX_VALUE;
		for (int x = 0; x < tmp.getWidth(); x++)
			for (int y = 0; y < tmp.getHeight(); y++) {
				float h = test.height(x, y);
				minH = Math.min(minH, h);
				maxH = Math.max(maxH, h);
			}
		for (int x = 0; x < tmp.getWidth(); x++)
			for (int y = 0; y < tmp.getHeight(); y++) {
				float h = test.height(x, y);
				h = (h - minH) / (maxH - minH);
				int r = (int) (h * 0xFF);
				tmp.setRGB(x, y, (r << 16) | (r << 8) | (r << 0));
			}
		ImageIO.write(tmp, "PNG", new File("C:/Users/westin/Downloads/test.png"));
	}

	private final int unknown1;
	private final float[][] binOffset;

	public S00_File(ByteBuffer data) {
		int binsW = data.getInt();
		int binsH = data.getInt();
		this.unknown1 = data.getInt();
		this.binOffset = new float[binsW][binsH];
		for (int x = 0; x < binsW; x++)
			for (int y = 0; y < binsH; y++)
				binOffset[x][y] = data.getFloat();
	}

	private float height(int x, int y) {
		int bx = x / 4;
		int bi = x % 4;
		int by = y / 4;
		int bj = y % 4;
		return binOffset[bx][by];
	}

	@Override
	public void write(ByteBuffer b) throws IOException {
	}

	@Override
	public int length() throws IOException {
		return 0;
	}
}
