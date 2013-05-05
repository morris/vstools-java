package vstools;

/**
 * Backup of (standard) TIM
 * VS uses custom TIM
 * @author morris
 *
 */
public class BackupTim extends Data {

    public BackupTim(ByteArray data) {
	super(data);
    }

    public void read() {
	log("tim begin");

	// 12 byte header
	skip(4); // magic 0x1000000
	bpp = u32();
	imgLen = u32();

	dataLen = imgLen - 12;

	// frame buffer positioning
	f1 = u16();
	f2 = u16();
	width = u16();
	height = u16();

	log("bpp: " + bpp);
	log("dim: " + width + "x" + height);
	log("org: " + f1 + "," + f2);

	// image data
	buffer = new byte[dataLen * 2];
	for (int i = 0; i < dataLen * 2; i += 4) {
	    colorBytes(buffer, i);
	}

	log("tim done");
    }

    public void colorBytes(byte[] buffer, int i) {
	// int bits = is.readShort();
	byte b1 = byt();
	byte b2 = byt();

	int a, b, g, r;

	a = (b1 & 0x80) >> 7;
	b = (b1 & 0x78) >> 2;
	g = ((b1 & 0x03) << 3) | ((b2 & 0xD0) >> 5);
	r = b2 & 0x1F;
	
	if (i < logColors) {
	    log(a + " " + r + " " + g + " " + b);
	}

	// converting from 5bit to 8bit is factor 2^3 = 8
	b = b * 8;
	g = g * 8;
	r = r * 8;

	buffer[i] = (byte) 255;
	buffer[i + 1] = (byte) b;
	buffer[i + 2] = (byte) g;
	buffer[i + 3] = (byte) r;
	
	buffer[i] = (byte) 255;
	buffer[i + 1] = (byte) b;
	buffer[i + 2] = (byte) g;
	buffer[i + 3] = (byte) r;
    }

    public String dimensions(int x) {
	String t = x + "";
	for (int i = 1; i < Math.sqrt(x); ++i) {
	    int y = x / i;
	    if (y * i == x) {
		t += " = " + i + "*" + y;
	    }
	}
	return t;
    }

    public int len;
    public int bpp;
    public int imgLen;
    public int dataLen;

    public int f1;
    public int f2;

    public int width;
    public int height;

    public byte[] buffer;
    
    public int logColors = 0;
}
