package vstools;

import java.nio.ByteBuffer;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class WEPTextureMap extends Data {

    public WEPTextureMap(ByteArray data) {
	super(data);
    }

    public void read(int numberOfPalettes) {
	log(hex(data.pos));
	size = u32();

	skip(1); // unknown, always 1?
	width = u8() * 2;
	height = u8() * 2;

	colorsPerPalette = u8();

	log("textureMap.width: " + width);
	log("textureMap.height: " + height);
	log("colorsPerPalette: " + colorsPerPalette);

	log(hex(data.pos));
	palettes = new WEPPalette[numberOfPalettes];
	for (int i = 0; i < numberOfPalettes; ++i) {
	    palettes[i] = new WEPPalette(data);
	    palettes[i].read(colorsPerPalette);
	}

	map = new int[width][height];
	for (int y = 0; y < height; ++y) {
	    for (int x = 0; x < width; ++x) {
		map[x][y] = u8();
	    }
	}

	log("textureMap done");
    }

    public void build(int palette) {
	buffer = new byte[width * height * 4];
	int i = 0;
	for (int y = 0; y < height; ++y) {
	    for (int x = 0; x < width; ++x) {
		int c = map[x][y];
		// TODO sometimes c >= colorsPerPalette?? setting black
		if (c < colorsPerPalette) {
		    buffer[i] = (byte) palettes[palette].colors[c].getAlpha();
		    buffer[i + 1] = (byte) palettes[palette].colors[c]
			    .getBlue();
		    buffer[i + 2] = (byte) palettes[palette].colors[c]
			    .getGreen();
		    buffer[i + 3] = (byte) palettes[palette].colors[c].getRed();
		} else {
		    buffer[i] = 0;
		    buffer[i + 1] = 0;
		    buffer[i + 2] = 0;
		    buffer[i + 3] = 0;
		}
		i += 4;
	    }
	}
    }

    public void setTexture(Material mat) {
	ByteBuffer bb = BufferUtils.createByteBuffer(buffer);

	Image image = new Image(Image.Format.ABGR8, width, height, bb);

	Texture2D texture = new Texture2D(image);
	texture.setImage(image);
	texture.setMagFilter(Texture2D.MagFilter.Nearest);
	mat.setTexture("ColorMap", texture);
	mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    }

    public int size;
    public int width;
    public int height;
    public int colorsPerPalette;
    public WEPPalette[] palettes;
    public int[][] map;
    public byte[] buffer;
}
