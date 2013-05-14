package vstools;

import java.awt.Color;

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

    public byte byte_() {
	return data.byte_();
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
	int bits = data.u16();
	return Util.color(bits);
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
    
    public static String bin(int i, int width) {
	return Util.bin(i, width);
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
