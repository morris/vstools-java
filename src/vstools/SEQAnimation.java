package vstools;

import com.jme3.animation.Animation;
import com.jme3.animation.BoneTrack;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * See http://datacrystal.romhacking.net/wiki/Vagrant_Story:SEQ_files
 * 
 * @author morris
 * 
 */
public class SEQAnimation extends Data {

    public SEQAnimation(SEQ seq, ByteArray data) {
	super(data);
	this.seq = seq;
    }

    public void header(int id) {
	this.id = id;
	numFrames = u16(); // not sure about this

	// some animations use a different animation as base
	idOtherAnimation = s8();
	assert idOtherAnimation >= -1 && idOtherAnimation < seq.numAnimations;

	mode = u8(); // unknown. has weird effects on character mesh

	// seems to point to an opcode block that controls looping
	ptr1 = u16(); 
	
	// points to a translation vector for the animated character
	ptrTranslation = u16();
	
	// points to an opcode block that controls movement
	ptrMove = u16();

	// just some logging
	log("animation " + id);
	log("numFrames: " + numFrames);
	log("idOtherPose: " + idOtherAnimation);
	log("mode: " + mode);
	log("ptr1: " + hex(seq.ptrData(ptr1)) + " (" + ptr1 + ")");
	log("ptrTranslation: " + hex(seq.ptrData(ptrTranslation)) + " ("
		+ ptrTranslation + ")");
	log("ptrMove: " + hex(seq.ptrData(ptrMove)) + " (" + ptrMove + ")");

	// read pointers to rotations and opcodes for individual joints
	ptrJoints = new int[seq.numJoints];
	for (int i = 0; i < seq.numJoints; ++i) {
	    ptrJoints[i] = u16();
	    log(i + " " + hex(seq.ptrData(ptrJoints[i])));
	}

	for (int i = 0; i < seq.numJoints; ++i) {
	    // TODO always zero?
	    skip(2);
	}
    }

    public void compute() {
	log("computing animation " + id);

	// read translation
	// big endian
	seek(seq.ptrData(ptrTranslation));

	x = s16big();
	y = s16big();
	z = s16big();

	// TODO implement move

	// initialize joint rotations
	quaternions = new Quaternion[seq.numJoints];

	// set base animation
	SEQAnimation base = this;
	if (idOtherAnimation != -1) {
	    base = seq.animations[idOtherAnimation];
	}

	// read rotation and opcodes
	for (int i = 0; i < seq.numJoints; ++i) {
	    seek(seq.ptrData(base.ptrJoints[i]));

	    rotation(i);
	    // opcodes(i); TODO doesnt work
	}

	log("translation " + x + " " + y + " " + z);

	// build animation
	buildAnimation();
    }

    public void rotation(int i) {
	int rx, ry, rz;

	// big endian! but... WHY?!
	rx = s16big();
	ry = s16big();
	rz = s16big();
	
	log("rotation " + i + ": " + rx + " " + ry + " " + rz);

	float fpitch = convert(rx);
	float fyaw = convert(ry);
	float froll = convert(rz);

	quaternions[i] = Util.quat(fpitch, fyaw, froll);
    }

    public void opcodes(int i) {
	// this mostly follows
	// http://datacrystal.romhacking.net/wiki/Vagrant_Story:SEQ_files

	int f = 0;
	while (true) {
	    int op = u8();
	    int op2 = op;

	    if (op == 0) {
		break;
	    }

	    // actual amount of rotation
	    int rx = 0;
	    int ry = 0;
	    int rz = 0;

	    if ((op & 0xe0) > 0) {
		int t = op & 0x1f;
		if (t == 0x1f) {
		    t = u8();
		    f += 0x20 + t;
		} else {
		    f += 1 + t;
		}

	    } else {
		int t = op & 0x3;
		if (t == 0x3) {
		    t = u8();
		    f += 0x4 + t;
		} else {
		    f += 1 + t;
		}

		op = op << 3;

		// half word rotation
		int h = s16big();

		if ((h & 0x4) > 0) {
		    rx = h >> 3;
		    op = op & 0x60;
		    if ((h & 0x2) > 0) {
			ry = s16big();
			op = op & 0xa0;
		    }
		    if ((h & 0x1) > 0) {
			rz = s16big();
			op = op & 0xc0;
		    }
		} else if ((h & 0x2) > 0) {
		    ry = h >> 3;
		    op = op & 0xa0;
		    if ((h & 0x1) > 0) {
			rz = s16big();
			op = op & 0xc0;
		    }
		} else if ((h & 0x1) > 0) {
		    rz = h >> 3;
		    op = op & 0xc0;
		}
	    }

	    // byte rotation
	    if ((op & 0x80) > 0) {
		rx = s8();
	    }
	    if ((op & 0x40) > 0) {
		ry = s8();
	    }
	    if ((op & 0x20) > 0) {
		rz = s8();
	    }

	    if (i == 1) {
		log(hex(op2) + " " + f + " " + rx + " " + ry + " " + rz);
	    }
	}
    }

    /**
     * builds the animation for jmonkey. every joint uses two jme bones, one for
     * rotation and one for translation.
     */
    public void buildAnimation() {
	if (seq.shp != null) {
	    animation = new Animation("Animation" + id, 0.5f);

	    for (int i = 0; i < seq.numJoints; ++i) {

		float[] times = { 0.0f };

		// rotation bone
		BoneTrack track = new BoneTrack(i);
		Vector3f[] translations = { Vector3f.ZERO };
		Quaternion[] rotations = { quaternions[i] };
		track.setKeyframes(times, translations, rotations);

		animation.addTrack(track);

		// translation bone
		// not for root
		if (i > 0) {
		    BoneTrack track2 = new BoneTrack(i + seq.numJoints);
		    Vector3f[] translations2 = { new Vector3f(
			    seq.shp.joints[i].length, 0, 0) };
		    Quaternion[] rotations2 = { Quaternion.IDENTITY };
		    track2.setKeyframes(times, translations2, rotations2);
		    animation.addTrack(track2);
		}
	    }
	}
    }

    public float convert(int angle) {
	return angle * CONVERT;
    }

    // conversion factor for rotations to radians
    public static final float CONVERT = (1.0f / 4096.0f) * 2.0f * FastMath.PI;

    public SEQ seq;

    public int id;
    public int numFrames;
    public int idOtherAnimation;
    public int mode;
    public int ptr1;
    public int ptrTranslation;
    public int ptrMove;
    public int ptrJoints[];

    public Quaternion quaternions[];
    public int x;
    public int y;
    public int z;

    public Animation animation;
}
