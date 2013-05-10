package vstools;

public class TIM extends Data {

    public TIM(ByteArray data) {
	super(data);
    }

    public void read(int mode) {
	log("TIM");

	// 12 byte header
	skip(4); // magic 10 00 00 00
	bpp = u32();
	imgLen = u32();

	dataLen = imgLen - 12;

	// frame buffer positioning
	fx = u16();
	fy = u16();
	width = u16();
	height = u16();

	if (mode == 1) {
	    width *= 4;
	}

	log("bpp: " + bpp);
	log("position: " + fx + "," + fy);
	log("size: " + width + "x" + height);

	// image data

	if (mode == 0) {
	    // standard tim, used for palettes
	    buffer = new byte[dataLen * 2];
	    for (int i = 0; i < dataLen * 2; i += 4) {
		colorBytes(buffer, i);
	    }
	} else if (mode == 1) {
	    // 4 bit texture maps
	    buffer = new byte[width * height * 4];
	    for (int i = 0; i < dataLen * 8; i += 8) {
		byte c = byt();
		byte l = (byte) ((c & 0xF0) >> 4);
		byte r = (byte) (c & 0x0F);
		buffer[i] = (byte) 0xFF;
		buffer[i + 1] = r;
		buffer[i + 2] = r;
		buffer[i + 3] = r;
		buffer[i + 4] = (byte) 0xFF;
		buffer[i + 5] = l;
		buffer[i + 6] = l;
		buffer[i + 7] = l;
		
		if(i < logColors) {
		    log("  color index " + l + " " + r);
 		}
	    }
	}

	log("TIM done");
    }

    public void colorBytes(byte[] buffer, int i) {
	// int bits = is.readShort();
	byte b2 = byt();
	byte b1 = byt();

	int a, b, g, r;

	a = (b1 & 0x80) >> 7;
	b = (b1 & 0x78) >> 2;
	g = ((b1 & 0x03) << 3) | ((b2 & 0xE0) >> 5);
	r = b2 & 0x1F;

	// converting from 5bit to 8bit is factor 2^3 = 8
	b = b * 8;
	g = g * 8;
	r = r * 8;
	
	if (i < logColors) {
	    log(a + " " + r + " " + g + " " + b);
	}

	buffer[i] = (byte) 255;
	buffer[i + 1] = (byte) b;
	buffer[i + 2] = (byte) g;
	buffer[i + 3] = (byte) r;
    }

    public int len;
    public int bpp;
    public int imgLen;
    public int dataLen;

    public int fx;
    public int fy;

    public int width;
    public int height;

    public byte[] buffer;

    public int logColors = 0;
}
