package vstools;

import java.util.TreeSet;

public class SEQ extends Data {

    public SEQ(SHP shp, ByteArray data) {
	super(data);
	this.shp = shp;
    }
    
    public SEQ(ByteArray data) {
	super(data);
    }

    public void header() {
	numSlots = u16();
	numJoints = u8();
	skip(1);
	size = u32();
	h3 = u32();
	slotPtr = u32() + 8;
	dataPtr = slotPtr + numSlots;

	log("numSlots: " + numSlots);
	log("numJoints: " + numJoints);
	log("dataPtr " + hex(dataPtr));
    }

    public void data() {
	numAnimations = (dataPtr - 16) / (numJoints * 4 + 10);
	log(numAnimations);

	animations = new SEQAnimation[numAnimations];
	for (int i = 0; i < numAnimations; ++i) {
	    animations[i] = new SEQAnimation(this, data);
	    animations[i].read(this, i);
	}

	slots = new int[numSlots];
	for (int i = 0; i < numSlots; ++i) {
	    slots[i] = s8();
	}

	for (int i = 0; i < numAnimations; ++i) {
	    animations[i].compute();
	}
    }

    public void read() {
	header();
	data();
    }

    public int ptrData(int i) {
	return i + dataPtr;
    }
    
    public int ptrDataRam(int i) {
	return i + 0x1275d2;
    }

    public void opcodes() {
	TreeSet<Integer> ptrs = new TreeSet<Integer>();
	SEQAnimation[] test = { animations[0] };
	test = animations;
	for (SEQAnimation p : test) {
	    if (p.idOtherPose == -1) {
		ptrs.add(p.ptr1);
		ptrs.add(p.ptrMove);
		ptrs.add(p.ptrTranslation);
		for (int i = 0; i < numJoints; ++i) {
		    ptrs.add(p.ptrJoints[i]);
		}
	    }
	}

	log(ptrs);

	int t = 0;
	for (int ptr : ptrs) {
	    int length = ptr - t;
	    log(hex(t) + "(" + t + ")" + ": " + length);
	    t = ptr;
	}
    }
    
    public void ptr1s() {
	SEQAnimation[] test = { animations[0] };
	test = animations;
	for (SEQAnimation p : test) {
	    data.seek(ptrData(p.ptr1));
	    log(u8() + " " +hex( u8())+ " " +hex( u8()));
	}
    }
    
    public static void single() {
	try {
	    SHP shp = new SHP(Util.read("OBJ/00.SHP"));
	    shp.all();
	    SEQ seq = new SEQ(shp, Util.read("OBJ/00_COM.SEQ"));
	    seq.read();
	    seq.opcodes();
	    seq.ptr1s();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public static void main(String[] args) {
	single();
    }
    
    public SHP shp;

    public int numSlots;
    public int numJoints;
    public int size;
    public int h3;
    public int slotPtr;
    public int dataPtr;
    public int numAnimations;

    public int slots[];

    public SEQAnimation[] animations;
    public int[] block;
}
