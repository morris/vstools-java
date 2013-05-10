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
	numSlots = u16(); // "slots" is just some random name, purpose unknown
	numJoints = u8();
	skip(1); // padding
	size = u32(); // file size, useless for this tools
	h3 = u32(); // unknown
	slotPtr = u32() + 8; // ptr to slots
	dataPtr = slotPtr + numSlots; // ptr to rotation and opcode data

	log("numSlots: " + numSlots);
	log("numJoints: " + numJoints);
	log("dataPtr " + hex(dataPtr));
    }

    public void data() {
	// number of animations has to be computed
	numAnimations = (dataPtr - 16) / (numJoints * 4 + 10);
	log("numAnimations: " + numAnimations);

	// read all headers
	animations = new SEQAnimation[numAnimations];
	for (int i = 0; i < numAnimations; ++i) {
	    animations[i] = new SEQAnimation(this, data);
	    animations[i].header(i);
	}

	// read "slots"
	// these are animation ids, can be used as in this.animations[id].
	// purpose unknown
	slots = new int[numSlots];
	for (int i = 0; i < numSlots; ++i) {
	    slots[i] = s8();
	}

	// compute animations
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

    /**
     * Utility method to display the (possible) lengths of opcode information.
     * It takes all pointers from all animations to the data section, sorts them
     * and prints the length between them.
     */
    public void opcodes() {
	TreeSet<Integer> ptrs = new TreeSet<Integer>();
	SEQAnimation[] test = { animations[0] };
	test = animations;  // comment this to only print for animation 0
	for (SEQAnimation p : test) {
	    if (p.idOtherAnimation == -1) {
		ptrs.add(p.ptr1);
		ptrs.add(p.ptrMove);
		ptrs.add(p.ptrTranslation);
		for (int i = 0; i < numJoints; ++i) {
		    ptrs.add(p.ptrJoints[i]);
		}
	    }
	}

	int t = 0;
	for (int ptr : ptrs) {
	    int length = ptr - t;
	    log(hex(t, 4) + "(" + t + ")" + ": " + length);
	    t = ptr;
	}
    }

    /**
     * Utility method to print all ptr1s.
     */
    public void ptr1s() {
	SEQAnimation[] test = { animations[0] };
	test = animations; // comment this to only print for animation 0
	for (SEQAnimation p : test) {
	    data.seek(ptrData(p.ptr1));
	    log(u8() + " " + hex(u8()) + " " + hex(u8()));
	}
    }

    /**
     * Read ashley and common seq and print debug
     */
    public static void single() {
	try {
	    SHP shp = new SHP(Util.read("OBJ/00.SHP"));
	    shp.read();
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
