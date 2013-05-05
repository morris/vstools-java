package vstools;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

public class ARM extends Data {

    public ARM(ByteArray data) {
	super(data);
    }

    public void read() {
	numRooms = u32();

	// headers
	rooms = new ARMRoom[numRooms];
	for (int i = 0; i < numRooms; ++i) {
	    rooms[i] = new ARMRoom(this, data);
	    rooms[i].read();
	    rooms[i].header();
	}

	// graphics
	for (int i = 0; i < numRooms; ++i) {
	    rooms[i].graphics();
	}

	// names
	for (int i = 0; i < numRooms; ++i) {
	    rooms[i].name();
	    log(rooms[i].name);
	}
    }

    public void build(Viewer viewer) {
	node = new Node();
	
	for (int i = 0; i < numRooms; ++i) {
	    rooms[i].build(viewer);
	    node.attachChild(rooms[i].geom);
	    node.attachChild(rooms[i].lineGeom);
	}
	
	node.updateModelBound();
	node.setCullHint(CullHint.Never); // TODO fix bounding box for culling
    }

    public static void single() {
	try {
	    ARM t = new ARM(Util.read("SMALL/SCEN001.ARM"));
	    t.build(null);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	single();
	Util.test(true, Util.file("SMALL"), "ARM", ARM.class);
    }

    public int numRooms;
    public ARMRoom[] rooms;

    public Node node;
}
