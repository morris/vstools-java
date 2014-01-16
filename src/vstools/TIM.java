package vstools;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class TIM extends Data {

	public TIM(ByteArray data) {
		super(data);
	}

	public void read() {
		log("-- TIM header");

		// 12 byte header

		// magic 10 00 00 00
		int[] magic = buf(4);
		assert Arrays.equals(magic, new int[] { 0x10, 0, 0, 0 });

		bpp = u32();
		imgLen = u32();

		dataLen = imgLen - 12;

		// frame buffer positioning
		fx = u16();
		fy = u16();
		width = u16(); // width IN FRAME BUFFER
		height = u16(); // height IN FRAME BUFFER

		dataPtr = data.pos;

		log("bpp: " + bpp);
		log("position: " + fx + "," + fy);
		log("size: " + width + "x" + height);

		// skip data as we don't know what kind of texture this is
		// will read data on build
		skip(dataLen);
	}

	public void copyToFrameBuffer(FrameBuffer fb) {
		seek(dataPtr);

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				Color c = color();
				fb.setPixel(fx + x, fy + y, c);
			}
		}
	}

	public void markFrameBuffer(FrameBuffer fb) {
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				Color c = Color.red;
				fb.setPixel(fx + x, fy + y, c);
			}
		}
	}

	public byte[] buildCLUT(int x, int y) {
		int ox = x - fx;
		int oy = y - fy;

		log("clut");
		log(ox + ", " + oy);

		seek(dataPtr + (oy * width + ox) * 2);

		byte[] buffer = new byte[64];
		for (int i = 0; i < 64; i += 4) {
			Color c = color();

			log(c);

			buffer[i] = (byte) c.getAlpha();
			buffer[i + 1] = (byte) c.getBlue();
			buffer[i + 2] = (byte) c.getGreen();
			buffer[i + 3] = (byte) c.getRed();
		}

		return buffer;
	}

	public Material buildTexture4(byte[] clut) {
		seek(dataPtr);

		int size = width * height * 16;

		byte[] buffer = new byte[size];
		for (int i = 0; i < size; i += 8) {
			byte c = byte_();

			int l = ((c & 0xF0) >> 4) * 4;
			int r = (c & 0x0F) * 4;

			buffer[i + 0] = clut[r + 0];
			buffer[i + 1] = clut[r + 1];
			buffer[i + 2] = clut[r + 2];
			buffer[i + 3] = clut[r + 3];

			buffer[i + 4] = clut[l + 0];
			buffer[i + 5] = clut[l + 1];
			buffer[i + 6] = clut[l + 2];
			buffer[i + 7] = clut[l + 3];
		}

		// build material
		ByteBuffer bb = BufferUtils.createByteBuffer(buffer);

		Image image = new Image(Image.Format.ABGR8, width * 4, height, bb);

		Texture2D tex = new Texture2D(image);
		tex.setImage(image);
		tex.setMagFilter(Texture2D.MagFilter.Nearest);

		Material mat = app.unshaded();
		mat.setTexture("ColorMap", tex);
		mat.setBoolean("VertexColor", true);
		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		mat.getAdditionalRenderState().setAlphaTest(true);

		return mat;
	}

	public Material buildGreyTexture4() {
		seek(dataPtr);

		int size = width * height * 16;

		byte[] buffer = new byte[size];
		for (int i = 0; i < size; i += 8) {
			byte c = byte_();

			byte l = (byte) ((c & 0xF0) >> 4);
			byte r = (byte) (c & 0x0F);

			buffer[i + 0] = (byte) 255;
			buffer[i + 1] = r;
			buffer[i + 2] = r;
			buffer[i + 3] = r;

			buffer[i + 4] = (byte) 255;
			buffer[i + 5] = l;
			buffer[i + 6] = l;
			buffer[i + 7] = l;
		}

		// build material
		ByteBuffer bb = BufferUtils.createByteBuffer(buffer);

		Image image = new Image(Image.Format.ABGR8, width * 4, height, bb);

		Texture2D tex = new Texture2D(image);
		tex.setImage(image);
		tex.setMagFilter(Texture2D.MagFilter.Bilinear);

		Material mat = app.unshaded();
		mat.setTexture("ColorMap", tex);
		// mat.getAdditionalRenderState().setBlendMode(BlendMode.Additive);

		return mat;
	}

	public App app;

	public int len;
	public int bpp;
	public int imgLen;
	public int dataLen;

	public int fx;
	public int fy;

	public int width;
	public int height;

	public int dataPtr;

	public int logColors = 0;
}
