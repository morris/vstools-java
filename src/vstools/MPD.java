package vstools;

import com.jme3.material.Material;
import com.jme3.scene.Node;

/**
 * MPD data structure reader and builder
 * http://datacrystal.romhacking.net/wiki/Vagrant_Story:MPD_files
 * 
 * @author morris
 * 
 */
public class MPD extends Data {

    public MPD(ByteArray data) {
	super(data);
    }

    public void read() {
	read(null);
    }

    public void read(ZND znd) {
	this.znd = znd;

	header();
	roomHeader();
	roomSection();
	// clearedSection();
	// scriptSection();
    }

    public void header() {
	ptrRoomSection = u32();
	lenRoomSection = u32();
	ptrClearedSection = u32();
	lenClearedSection = u32();
	ptrScriptSection = u32();
	lenScriptSection = u32();
	ptrDoorSection = u32();
	lenDoorSection = u32();
	ptrEnemySection = u32();
	lenEnemySection = u32();
	ptrTreasureSection = u32();
	lenTreasureSection = u32();
    }

    public void roomHeader() {
	lenGeometrySection = u32();
	lenCollisionSection = u32();
	lenSubSection03 = u32();
	lenDoorSectionRoom = u32();
	lenLightingSection = u32();

	lenSubSection06 = u32();
	lenSubSection07 = u32();
	lenSubSection08 = u32();
	lenSubSection09 = u32();
	lenSubSection0A = u32();
	lenSubSection0B = u32();

	lenTextureEffectsSection = u32();

	lenSubSection0D = u32();
	lenSubSection0E = u32();
	lenSubSection0F = u32();
	lenSubSection10 = u32();
	lenSubSection11 = u32();
	lenSubSection12 = u32();
	lenSubSection13 = u32();

	lenAKAOSubSection = u32();

	lenSubSection15 = u32();
	lenSubSection16 = u32();
	lenSubSection17 = u32();
	lenSubSection18 = u32();
    }

    public void roomSection() {
	geometrySection();
	collisionSection();
	SubSection03();
	doorSectionRoom();
	lightingSection();
	SubSection06();
	SubSection07();
	SubSection08();
	SubSection09();
	SubSection0A();
	SubSection0B();
	textureEffectsSection();
	SubSection0D();
	SubSection0E();
	SubSection0F();
	SubSection10();
	SubSection11();
	SubSection12();
	SubSection13();
	akaoSubSection();
	SubSection15();
	SubSection16();
	SubSection17();
	SubSection18();
    }

    public void geometrySection() {
	numGroups = u32();
	log("numGroups: " + numGroups);
	groups = new MPDGroup[numGroups];
	for (int i = 0; i < numGroups; ++i) {
	    log("group " + i + " header");
	    groups[i] = new MPDGroup(this, data);
	    groups[i].header();
	}
	for (int i = 0; i < numGroups; ++i) {
	    log("group " + i + " data");
	    groups[i].data();
	}
    }

    public void collisionSection() {
	skip(lenCollisionSection);
    }

    public void SubSection03() {
	skip(lenSubSection03);
    }

    public void doorSectionRoom() {
	skip(lenDoorSectionRoom);
    }

    public void lightingSection() {
	skip(lenLightingSection);
    }

    public void SubSection06() {
	skip(lenSubSection06);
    }

    public void SubSection07() {
	skip(lenSubSection07);
    }

    public void SubSection08() {
	skip(lenSubSection08);
    }

    public void SubSection09() {
	skip(lenSubSection09);
    }

    public void SubSection0A() {
	skip(lenSubSection0A);
    }

    public void SubSection0B() {
	skip(lenSubSection0B);
    }

    public void textureEffectsSection() {
	skip(lenTextureEffectsSection);
    }

    public void SubSection0D() {
	skip(lenSubSection0D);
    }

    public void SubSection0E() {
	skip(lenSubSection0E);
    }

    public void SubSection0F() {
	skip(lenSubSection0F);
    }

    public void SubSection10() {
	skip(lenSubSection10);
    }

    public void SubSection11() {
	skip(lenSubSection11);
    }

    public void SubSection12() {
	skip(lenSubSection12);
    }

    public void SubSection13() {
	skip(lenSubSection13);
    }

    public void akaoSubSection() {
	skip(lenAKAOSubSection);
    }

    public void SubSection15() {
	skip(lenSubSection15);
    }

    public void SubSection16() {
	skip(lenSubSection16);
    }

    public void SubSection17() {
	skip(lenSubSection17);
    }

    public void SubSection18() {
	skip(lenSubSection18);
    }

    public void clearedSection() {
	skip(lenClearedSection);
    }

    public void scriptSection() {
	int len = u16();
	log(hex(lenScriptSection));
	log(hex(len));
	int ptrDialogText = u16();
	log(hex(ptrDialogText + ptrScriptSection));

	skip(ptrDialogText);
	int[] s = buf(700);
	log(Text.convert(s, 700));
    }

    public void build() {

	node = new Node("Map");

	for (int i = 0; i < numGroups; ++i) {
	    MPDGroup group = groups[i];
	    group.build();
	    
	    for(MPDMesh mesh : group.meshes.values()) {
		node.attachChild(mesh.geom);
	    }
	}
	
	for(MPDMesh m : groups[8].meshes.values()) {
	    //m.geom.setMaterial(znd.app.debug());
	}
    }
    
    public void setMaterial(Material mat) {
	for (int i = 0; i < numGroups; ++i) {
	    MPDGroup group = groups[i];
	    
	    for(MPDMesh mesh : group.meshes.values()) {
		mesh.geom.setMaterial(mat);
	    }
	}
    }

    public static void main(String[] args) {
	try {
	    MPD mpd = new MPD(Util.read("MAP/MAP009.MPD"));
	    mpd.read();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public ZND znd;

    public int ptrRoomSection;
    public int lenRoomSection;
    public int ptrClearedSection;
    public int lenClearedSection;
    public int ptrScriptSection;
    public int lenScriptSection;
    public int ptrDoorSection;
    public int lenDoorSection;
    public int ptrEnemySection;
    public int lenEnemySection;
    public int ptrTreasureSection;
    public int lenTreasureSection;

    public int lenGeometrySection;
    public int lenCollisionSection;
    public int lenSubSection03;
    public int lenDoorSectionRoom;
    public int lenLightingSection;
    public int lenSubSection06;
    public int lenSubSection07;
    public int lenSubSection08;
    public int lenSubSection09;
    public int lenSubSection0A;
    public int lenSubSection0B;
    public int lenTextureEffectsSection;
    public int lenSubSection0D;
    public int lenSubSection0E;
    public int lenSubSection0F;
    public int lenSubSection10;
    public int lenSubSection11;
    public int lenSubSection12;
    public int lenSubSection13;
    public int lenAKAOSubSection;
    public int lenSubSection15;
    public int lenSubSection16;
    public int lenSubSection17;
    public int lenSubSection18;

    public int numGroups;

    public MPDGroup[] groups;

    public Node node;
}
