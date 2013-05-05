package vstools;

import java.awt.Color;

import com.jme3.math.Vector2f;

/**
 * Base class for all data structures
 * @author morris
 *
 */
public class Data {

    public Data(ByteArray data) {
	this.data = data;
	verbose = true;
    }

    public void read() {
	
    }

    public void seek(int i) {
	data.seek(i);
    }

    public void skip(int i) {
	data.skip(i);
    }

    public byte byt() {
	return data.byt();
    }

    public int s8() {
	return data.s8();
    }

    public int u8() {
	return data.u8();
    }

    public int s32() {
	return data.s32();
    }

    public int u32() {
	return data.u32();
    }

    public int s16() {
	return data.s16();
    }

    public int s16big() {
	return data.s16big();
    }

    public int u16() {
	return data.u16();
    }

    public int[] buf(int len) {
	return data.buf(len);
    }

    public String text(int width) {
	return Text.convert(buf(width), width, true);
    }

    public Color color() {
	int bits = data.s16();
	int a = (bits & 0x8000) >> 15;
	int b = (bits & 0x7C00) >> 10;
	int g = (bits & 0x03E0) >> 5;
	int r = bits & 0x1F;

	if (a == 0 && b == 0 && g == 0 && r == 0) {
	    // 0,0,0 is defined as transparent
	    return new Color(0, 0, 0, 0);
	} else if (a == 0) {
	    // 5bit -> 8bit is factor 2^3 = 8
	    // TODO different conversions were suggested, investigate
	    return new Color(r * 8, g * 8, b * 8);
	} else {
	    return new Color(0, 0, 0, 0);
	}
    }

    /**
     * Unsigned byte
     * @param b
     * @return
     */
    public static int u(byte b) {
	if (b < 0) {
	    return 256 + ((int) b);
	}
	return (int) b;
    }
    
    public void log(Object t) {
	if (verbose) {
	    System.out.println(t);
	}
    }

    public void logpos() {
	log(hex(data.pos));
    }

    public static String hex(int i) {
	return Util.hex(i);
    }
    
    public static String hex(int i, int width) {
	return Util.hex(i, width);
    }
    
    public static String hex(int[] is) {
	return Util.hex(is);
    }
    
    public static String hex(int[] is, int width) {
	return Util.hex(is, width);
    }

    public static String bin(int i) {
	return Util.bin(i);
    }
    
    /**
     * Convert (x,y) coordinates on an image to real UV coordinates.
     * @param x
     * @param y
     * @param width image width
     * @param height image height
     * @return
     */
    public Vector2f uvf(int x, int y, int width, int height) {
	return new Vector2f((float) x / (float) width, (float) y / (float) height);
    }

    public void logDimensions(int size) {
	String t = size + "";
	int max = (int) Math.sqrt(size);
	for (int x = 1; x <= max; ++x) {
	    if (size % x == 0) {
		t += " = " + x + "*" + size / x;
	    }
	}
	log(t);
    }

    public void logDimensionsHex(int size) {
	String t = hex(size) + "";
	int max = (int) Math.sqrt(size);
	for (int x = 1; x <= max; ++x) {
	    if (size % x == 0) {
		t += " = " + hex(x) + "*" + hex(size / x);
	    }
	}
	log(t);
    }

    public ByteArray data;
    public boolean verbose;
}
