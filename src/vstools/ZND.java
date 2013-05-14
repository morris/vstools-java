package vstools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class ZND extends Data {

    public ZND(ByteArray data) {
	super(data);
	materials = new HashMap<String, Material>();
    }

    public void read() {
	header();
	data();
    }

    public void header() {
	log("-- ZND header");

	mpdPtr = u32();
	mpdLen = u32();
	mpdNum = mpdLen / 8;
	enemyPtr = u32();
	enemyLen = u32();
	timPtr = u32();
	timLen = u32();
	wave = u8();
	skip(7);

	log("mpdNum: " + mpdNum);
	log("timLen: " + hex(timLen));
	log("tim section: " + hex(timPtr) + "-" + hex(timPtr + timLen));
    }

    public void data() {
	log("-- ZND data");

	mpdSection();
	enemiesSection();
	timSection();
    }

    public void mpdSection() {
	mpdLBAs = new int[mpdNum];
	mpdSizes = new int[mpdNum];
	for (int i = 0; i < mpdNum; ++i) {
	    mpdLBAs[i] = u32();
	    mpdSizes[i] = u32();
	}
    }

    public void enemiesSection() {
	skip(enemyLen);
    }

    public void timSection() {
	timLen2 = u32();
	skip(12);
	timNum = u32();

	log("timLen2: " + hex(timLen2));
	log("timNum: " + timNum);

	frameBuffer = new FrameBuffer();
	tims = new TIM[timNum];
	for (int i = 0; i < timNum; ++i) {
	    // not technically part of tim
	    @SuppressWarnings("unused")
	    int timlen = u32();

	    tims[i] = new TIM(data);
	    tims[i].read();
	    tims[i].app = app;
	    tims[i].copyToFrameBuffer(frameBuffer);
	}
	
	byte[] clut = tims[tims.length - 3].buildCLUT(0, 0);
	for(int i = 0; i < clut.length; ++i) {
	    frameBuffer.buffer[i] = clut[i];
	}
    }

    public Material getMaterial(int textureId, int clutId) {
	String id = textureId + "" + clutId;

	if (textureId - 5 >= tims.length) {
	    return app.debug();
	}

	if (materials.containsKey(id)) {
	    return materials.get(id);
	} else {
	    // find texture
	    TIM texture = tims[textureId - 5];

	    // find CLUT
	    int x = (clutId * 16) % 1024;
	    int y = (clutId * 16) / 1024;

	    byte[] clut = null;
	    for (TIM tim : tims) {
		if (tim.fx <= x && tim.fx + tim.width > x && tim.fy <= y
			&& tim.fy + tim.height > y) {
		    // we found the CLUT
		    clut = tim.buildCLUT(x, y);
		    break;
		}
	    }

	    // build texture
	    Material mat = texture.buildTexture4(clut);
	    //mat = texture.buildGreyTexture4();

	    // store
	    materials.put(id, mat);

	    return mat;
	}
    }

    public static void main(String[] args) {
	ZND t;
	try {
	    t = new ZND(Util.read("MAP/ZONE009.ZND"));
	    t.read();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public int mpdPtr;
    public int mpdLen;
    public int mpdNum;
    public int enemyPtr;
    public int enemyLen;
    public int timPtr;
    public int timLen;
    public int wave;

    public int[] mpdLBAs;
    public int[] mpdSizes;

    public int timLen2;
    public int timNum;

    public FrameBuffer frameBuffer;

    public TIM[] tims;

    public HashMap<String, Material> materials;

    public App app;
}
