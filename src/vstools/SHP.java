package vstools;

public class SHP extends WEP {

    public SHP(ByteArray data) {
	super(data);
    }

    public void header() {
	
	log("SHP header");

	header1();

	for (int i = 0; i < 8; ++i) {
	    overlayX[i] = u8();
	    overlayY[i] = u8();
	    width[i] = u8();
	    height[i] = u8();
	}

	skip(0x24); // unknown

	skip(0x6); // collision, not sure about this
	menuPositionY = s16();
	skip(0xc); // u
	shadowRadius = s16();
	shadowSizeIncrease = s16();
	shadowSizeDecrease = s16();
	skip(4);

	menuScale = s16();
	skip(2);
	targetSpherePositionY = s16();
	skip(8);

	animLBAs = new int[0xC];
	for (int i = 0; i < 0xC; ++i) {
	    animLBAs[i] = u32();
	}

	chainIds = new int[0xC];
	for (int i = 0; i < 0xC; ++i) {
	    chainIds[i] = u16();
	}

	specialLBAs = new int[4];
	for (int i = 0; i < 4; ++i) {
	    specialLBAs[i] = u32();
	}

	skip(0x20); // u, more lbas?

	magicPtr = u32() + 0xF8;
	log("magicPtr: " + hex(magicPtr));

	skip(0x18 * 2);

	akaoPtr = u32() + 0xF8;
	log("akaoPtr: " + hex(akaoPtr));

	groupPtr = u32() + 0xF8;
	log("groupPtr: " + hex(groupPtr));

	vertexPtr = u32() + 0xF8;
	log("vertexPtr: " + hex(vertexPtr));

	polygonPtr = u32() + 0xF8;
	log("polygonPtr: " + hex(polygonPtr));

	// static, unused
	jointPtr = 0x138;
    }

    public void data() {
	log("SHP data");
	
	logpos();
	jointSection();
	logpos();
	groupSection();
	logpos();
	vertexSection();
	logpos();
	polygonSection();
	logpos();
	/*
	 * akao = is.readInt(); log("akao: " + akao);
	 * 
	 * akaoTable = new int[akao]; for(int i = 0; i < akao; ++i) {
	 * akaoTable[i] = is.readInt(); log(hex(akaoTable[i])); }
	 */
	// skip akao
	logpos();
	skip(magicPtr - akaoPtr);

	// skip magic section
	logpos();
	skip(4);
	int length = u32();
	log("magicSectionLength: " + hex(length));
	skip(length);

	log(hex(data.pos));

	log("textureMapPtr should be " + hex(length + magicPtr + 8));

	// texture section
	textureSection(2);
    }

    public void read() {
	header();
	data();
    }
    
    public void setSEQ(SEQ seq) {
	this.activeSeq = seq;

	// remove current animations
	for (String animName : control.getAnimationNames()) {
	    control.removeAnim(control.getAnim(animName));
	}

	// set new animations
	for (int i = 0; i < seq.animations.length; ++i) {
	    control.addAnim(seq.animations[i].animation);
	}
    }

    public static void single() {
	try {
	    SHP t = new SHP(Util.read("OBJ/00.SHP"));
	    t.read();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	single();
	// Util.test(true, Util.file("OBJ"), "SHP", SHP.class);
    }

    public int[] overlayX = new int[8];
    public int[] overlayY = new int[8];
    public int[] width = new int[8];
    public int[] height = new int[8];

    public byte[] u2c = new byte[0x24];
    public byte[] collision = new byte[6];

    public int menuPositionY;

    public byte[] u58 = new byte[0xC];

    public int shadowRadius;
    public int shadowSizeIncrease;
    public int shadowSizeDecrease;
    public int u6a;
    public int menuScale;
    public int u70;
    public int targetSpherePositionY;
    public int[] animLBAs;
    public int[] chainIds;
    public int[] specialLBAs;
    public long u74;
    public int magicPtr;
    public int akaoPtr;

    public int akao;
    public int[] akaoTable;
    
    public SEQ[] seqs;
    public SEQ activeSeq;
}
