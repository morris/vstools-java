package vstools;

import java.io.IOException;

import com.jme3.scene.Node;

public class ZUD extends Data {
    
    public ZUD(ByteArray data) {
	super(data);
    }

    public void header() {
	idCharacter = u8();
	idWeapon = u8();
	idWeaponCategory = u8();
	idWeaponMaterial = u8();
	idShield = u8();
	idShieldMaterial = u8();
	Unknown = u8();
	skip(1); // padding
	ptrCharacterSHP = u32();
	lenCharacterSHP = u32();
	ptrWeaponWEP = u32();
	lenWeaponWEP = u32();
	ptrShieldWEP = u32();
	lenShieldWEP = u32();
	ptrCommonSEQ = u32();
	lenCommonSEQ = u32();
	ptrBattleSEQ = u32();
	lenBattleSEQ = u32();

	log("ptrWeaponWEP: " + hex(ptrWeaponWEP));
	log("ptrShieldWEP: " + hex(ptrShieldWEP));
	log("ptrCommonSEQ: " + hex(ptrCommonSEQ));
	log("ptrBattleSEQ: " + hex(ptrBattleSEQ));
    }

    public void data() {
	shp = new SHP(data);
	shp.read();

	seek(ptrWeaponWEP);
	try {
	    weapon = new WEP(data);
	    weapon.read();
	} catch (Exception e) {
	    log("weapon failed");
	    e.printStackTrace();
	    weapon = null;
	}

	seek(ptrShieldWEP);
	try {
	    shield = new WEP(data);
	    shield.read();
	} catch (Exception e) {
	    log("shield failed");
	    e.printStackTrace();
	    shield = null;
	}

	seek(ptrCommonSEQ);
	try {
	    com = new SEQ(shp, data);
	    com.read();
	} catch (Exception e) {
	    log("common seq failed");
	    e.printStackTrace();
	    com = null;
	}

	seek(ptrBattleSEQ);
	try {
	    bt = new SEQ(shp, data);
	    bt.read();
	} catch (Exception e) {
	    log("battle seq failed");
	    e.printStackTrace();
	    bt = null;
	}
    }

    public void read() {
	header();
	data();
    }

    public static void main(String[] args) {
	try {
	    ZUD t = new ZUD(Util.read("MAP/Z001U01.ZUD"));
	    t.read();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public Node getNode() {
	return shp.node;
    }

    public int idCharacter; // (as used by ZND files)
    public int idWeapon; // (as per weapons list)
    public int idWeaponCategory; // (as per weapon categories)
    public int idWeaponMaterial; // (as per materials list)
    public int idShield; // (as per armours list)
    public int idShieldMaterial; // (as per materials list)
    public int Unknown; //
    public int ptrCharacterSHP;
    public int lenCharacterSHP;
    public int ptrWeaponWEP;
    public int lenWeaponWEP;
    public int ptrShieldWEP;
    public int lenShieldWEP;
    public int ptrCommonSEQ;
    public int lenCommonSEQ;
    public int ptrBattleSEQ;
    public int lenBattleSEQ;

    public SHP shp;
    public WEP weapon;
    public WEP shield;
    public SEQ com;
    public SEQ bt;
}
