package vstools;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Random;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class FrameBuffer {

    public void build(App app) {
	ByteBuffer bb = BufferUtils.createByteBuffer(buffer);

	Image image = new Image(Image.Format.ABGR8, width, height, bb);

	Texture2D texture = new Texture2D(image);
	texture.setImage(image);
	texture.setMagFilter(Texture2D.MagFilter.Nearest);

	Material mat = app.unshaded();
	mat.setTexture("ColorMap", texture);
	mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

	Geometry g = new Geometry("FrameBuffer", new Box(50, 25, 1));
	g.setMaterial(mat);

	node = new Node("FrameBuffer");
	node.attachChild(g);
    }

    public void markCLUT(int id) {
	Random random = new Random();
	byte a = (byte) 255;
	byte b = (byte) 0;
	byte g = (byte) 0;
	byte r = (byte) 255;

	int ilo = id * 64;
	int ihi = ilo + 64;
	for (int i = ilo; i < ihi; i += 4) {
	    buffer[i + 0] = a;
	    buffer[i + 1] = b;
	    buffer[i + 2] = g;
	    buffer[i + 3] = r;
	}
    }

    public void markPixel(int x, int y) {
	Random random = new Random();
	byte a = (byte) 255;
	byte b = (byte) random.nextInt(256);
	byte g = (byte) random.nextInt(256);
	byte r = (byte) random.nextInt(256);

	int i = (y * 1024 + x) * 4;
	buffer[i] = a;
	buffer[i + 1] = b;
	buffer[i + 2] = g;
	buffer[i + 3] = r;
    }
    
    public void setPixel(int x, int y, Color c) {
	int i = (y * 1024 + x) * 4;
	buffer[i + 0] = (byte) c.getAlpha();
	buffer[i + 1] = (byte) c.getBlue();
	buffer[i + 2] = (byte) c.getGreen();
	buffer[i + 3] = (byte) c.getRed();
    }

    public byte[] buffer = new byte[width * height * 4];

    public static final int width = 1024;
    public static final int height = 512;

    public Node node;
}
