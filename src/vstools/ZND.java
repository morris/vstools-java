package vstools;

import java.io.IOException;

public class ZND extends Data {

    public ZND(ByteArray data) {
	super(data);
    }

    public void read() {
	header();
	mpdSection();
	enemiesSection();
	timSection();
    }

    public void header() {
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

	tims = new TIM[timNum];
	palNum = 4; // TODO check this default
	for (int i = 0; i < timNum - palNum; ++i) {
	    log("texture tim at " + hex(this.data.pos));

	    // not technically part of tim
	    @SuppressWarnings("unused")
	    int timlen = u32();

	    tims[i] = new TIM(data);
	    tims[i].logColors = 0;
	    tims[i].read(1);
	}

	for (int i = timNum - palNum; i < timNum; ++i) {
	    log("palette tim at " + hex(this.data.pos));

	    // not technically part of tim
	    @SuppressWarnings("unused")
	    int timlen = u32();

	    tims[i] = new TIM(data);
	    tims[i].read(0);
	}

	// TODO set colors from palettes
	/*
	TIM palette = tims[timNum - palNum];
	for (int i = 0; i < timNum - palNum; ++i) {
	    TIM t = tims[i];
	    for (int j = 0; j < t.buffer.length; j += 4) {
		
	    }
	}
	*/
    }

    public static void main(String[] args) {
	ZND t;
	try {
	    t = new ZND(Util.read("MAP/ZONE001.ZND"));
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
    public int palNum;

    public TIM[] tims;
}
