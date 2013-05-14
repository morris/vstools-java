package vstools;

import java.util.HashMap;

public class MPDGroup extends Data {

    public MPDGroup(MPD mpd, ByteArray data) {
	super(data);
	this.mpd = mpd;
    }
    
    public void read() {
	header();
	data();
    }

    public void header() {
	header = new int[64];
	for (int i = 0; i < 64; ++i) {
	    header[i] = u8();
	}
	log(hex(header, 2));
	
	// the header is not well understood
	// it seems that the bits in the second byte are flag bits
	// the following fixes the scaling issues in maps 001 and 002
	if((header[1] & 0x08) > 0) {
	    scale = 1;
	} else {
	    scale = 8; // TODO is this the default?
	}
    }

    public void data() {
	numPoly3gts = u32();
	numPoly4gts = u32();
	numPoly = numPoly3gts + numPoly4gts;
	log("numPoly: " + numPoly);

	polygons = new MPDPolygon[numPoly];
	meshes = new HashMap<String, MPDMesh>();
	
	for (int i = 0; i < numPoly3gts; ++i) {
	    polygons[i] = new MPDPolygon(this, data);
	    polygons[i].readTriangle();
	    
	    getMesh(polygons[i].textureId, polygons[i].clutId).polygons.add(polygons[i]);
	}
	
	for (int i = numPoly3gts; i < numPoly; ++i) {
	    polygons[i] = new MPDPolygon(this, data);
	    polygons[i].readQuad();
	    
	    getMesh(polygons[i].textureId, polygons[i].clutId).polygons.add(polygons[i]);
	}
    }
    
    public void build() {
	for(MPDMesh mesh : meshes.values()) {
	    mesh.build();
	}
    }
    
    public MPDMesh getMesh(int textureId, int clutId) {
	String id = textureId + "" + clutId;
	
	if(meshes.containsKey(id)) {
	    return meshes.get(id);
	} else {
	    MPDMesh mpdMesh = new MPDMesh(this, textureId, clutId);
	    meshes.put(id, mpdMesh);
	    return mpdMesh;
	}
    }
    
    public MPD mpd;

    public int[] header;

    public int numPoly3gts;
    public int numPoly4gts;
    public int numPoly;
    public int scale;

    public MPDPolygon[] polygons;
    
    public HashMap<String, MPDMesh> meshes;
}
