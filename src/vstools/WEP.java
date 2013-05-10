package vstools;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

/**
 * WEP data structure reader and builder
 * 
 * @author morris
 * 
 */
public class WEP extends Data {

    public WEP(ByteArray data) {
	super(data);
    }

    public void read() {
	header();
	data();
    }

    public void header() {

	log("WEP header");

	header1();

	texturePtr = u32() + 0x10;
	log("texturePtr: " + hex(texturePtr));

	skip(0x30);

	texturePtr = u32() + 0x10;
	log("texturePtr (2): " + hex(texturePtr));

	groupPtr = u32() + 0x10;
	log("groupPtr: " + hex(groupPtr));

	vertexPtr = u32() + 0x10;
	log("vertexPtr: " + hex(vertexPtr));

	polygonPtr = u32() + 0x10;
	log("polygonPtr: " + hex(polygonPtr));

	// static, unused
	jointPtr = 0x4C + 0x4;
    }

    public void header1() {
	skip(4); // "H01 "

	numJoints = u8();
	numGroups = u8();
	numTriangles = u16();
	numQuads = u16();
	numPolygons = u16();
	numAllPolygons = numTriangles + numQuads + numPolygons;

	logHeader();
    }

    public void logHeader() {
	log("numberOfJoints: " + numJoints);
	log("numberOfGroups: " + numGroups);
	log("numberOfTriangles: " + numTriangles);
	log("numberOfQuads: " + numQuads);
	log("numberOfPolygons " + numPolygons);
	log("numberOfAllPolygons: " + numAllPolygons);
    }

    public void data() {

	log("WEP data");

	jointSection();
	groupSection();
	vertexSection();
	polygonSection();
	textureSection(5);
    }

    public void jointSection() {
	joints = new WEPJoint[numJoints];
	for (int i = 0; i < numJoints; ++i) {
	    joints[i] = new WEPJoint(data);
	    joints[i].read();
	}

	for (int i = 0; i < joints.length; ++i) {
	    WEPJoint j = joints[i];

	    // set parentObject
	    if (j.parentJointId < joints.length) {
		j.parentJoint = joints[j.parentJointId];
	    }

	    log("joint " + i + ": s=" + j.length + " p=" + j.parentJointId
		    + " " + j.x + " " + j.y + " " + j.z + " " + j.mode);
	}
    }

    public void groupSection() {
	groups = new WEPGroup[numGroups];
	for (int i = 0; i < numGroups; ++i) {
	    groups[i] = new WEPGroup(data);
	    groups[i].read();
	    groups[i].joint = joints[groups[i].jointId];
	    log("group " + i + ": joint=" + groups[i].jointId + " lv="
		    + groups[i].lastVertex);
	}
    }

    public void vertexSection() {
	numVertices = groups[numGroups - 1].lastVertex;
	log("numberOfVertices: " + numVertices);

	vertices = new WEPVertex[numVertices];
	int g = 0;
	for (int i = 0; i < numVertices; ++i) {
	    if (i >= groups[g].lastVertex) {
		++g;
	    }
	    vertices[i] = new WEPVertex(data);
	    vertices[i].read();
	    vertices[i].groupId = g;
	    vertices[i].group = groups[g];
	    vertices[i].jointId = groups[g].jointId;
	}
    }

    public void polygonSection() {
	polygons = new WEPPolygon[numAllPolygons];
	for (int i = 0; i < numAllPolygons; ++i) {
	    polygons[i] = new WEPPolygon(data);
	    polygons[i].read();
	}
    }

    public void textureSection(int numPalettes) {
	textureMap = new WEPTextureMap(data);
	textureMap.read(numPalettes);
    }

    public void build(Material material) {
	build(material, 0);
    }

    public void build(Material material, int paletteId) {
	buildMesh();
	buildMaterial(material, paletteId);
	buildSkeleton();
	buildNode();
    }

    public void buildMesh() {
	// count indices
	int ni = 0;
	int nv = 0;
	for (int i = 0; i < polygons.length; ++i) {
	    WEPPolygon p = polygons[i];
	    if (p.quad()) {
		nv += 4;
		ni += 6;
		if (p.doubl()) {
		    ni += 6;
		}
	    } else {
		nv += 3;
		ni += 3;
		if (p.doubl()) {
		    ni += 6;
		}
	    }
	}

	log(nv + " vertices, " + ni + " indices");

	// build vertices
	vertices3 = new Vector3f[nv];
	verticesMap = new int[nv];
	uv = new Vector2f[nv];
	indices = new int[ni];
	boneIndices = new byte[nv * 4];
	boneWeights = new float[nv * 4];

	for (int i = 0; i < nv * 4; ++i) {
	    boneIndices[i] = 0;
	    boneWeights[i] = 0.0f;
	}

	int jv = 0;
	int ji = 0;
	for (int i = 0; i < polygons.length; ++i) {
	    WEPPolygon p = polygons[i];
	    if (p.quad()) {
		vertices3[jv] = pos(p.vertex1);
		vertices3[jv + 1] = pos(p.vertex2);
		vertices3[jv + 2] = pos(p.vertex3);
		vertices3[jv + 3] = pos(p.vertex4);

		verticesMap[jv] = p.vertex1;
		verticesMap[jv + 1] = p.vertex2;
		verticesMap[jv + 2] = p.vertex3;
		verticesMap[jv + 3] = p.vertex4;

		uv[jv] = abs2uv(p.u1, p.v1, textureMap.width, textureMap.height);
		uv[jv + 1] = abs2uv(p.u2, p.v2, textureMap.width,
			textureMap.height);
		uv[jv + 2] = abs2uv(p.u3, p.v3, textureMap.width,
			textureMap.height);
		uv[jv + 3] = abs2uv(p.u4, p.v4, textureMap.width,
			textureMap.height);

		boneIndices[jv * 4] = boneId(p.vertex1);
		boneIndices[jv * 4 + 4] = boneId(p.vertex2);
		boneIndices[jv * 4 + 8] = boneId(p.vertex3);
		boneIndices[jv * 4 + 12] = boneId(p.vertex4);

		boneWeights[jv * 4] = 1.0f;
		boneWeights[jv * 4 + 4] = 1.0f;
		boneWeights[jv * 4 + 8] = 1.0f;
		boneWeights[jv * 4 + 12] = 1.0f;

		// 3, 2, 1
		indices[ji] = jv + 2;
		indices[ji + 1] = jv + 1;
		indices[ji + 2] = jv;

		// 2, 3, 4
		indices[ji + 3] = jv + 1;
		indices[ji + 4] = jv + 2;
		indices[ji + 5] = jv + 3;

		ji += 6;

		if (p.doubl()) {
		    // 1, 2, 3
		    indices[ji] = jv;
		    indices[ji + 1] = jv + 1;
		    indices[ji + 2] = jv + 2;

		    // 4, 3, 2
		    indices[ji + 3] = jv + 3;
		    indices[ji + 4] = jv + 2;
		    indices[ji + 5] = jv + 1;

		    ji += 6;
		}

		jv += 4;

	    } else {
		vertices3[jv] = pos(p.vertex1);
		vertices3[jv + 1] = pos(p.vertex2);
		vertices3[jv + 2] = pos(p.vertex3);

		// uv is correctly reordered
		uv[jv] = abs2uv(p.u2, p.v2, textureMap.width, textureMap.height);
		uv[jv + 1] = abs2uv(p.u3, p.v3, textureMap.width,
			textureMap.height);
		uv[jv + 2] = abs2uv(p.u1, p.v1, textureMap.width,
			textureMap.height);

		boneIndices[jv * 4] = boneId(p.vertex1);
		boneIndices[jv * 4 + 4] = boneId(p.vertex2);
		boneIndices[jv * 4 + 8] = boneId(p.vertex3);

		boneWeights[jv * 4] = 1.0f;
		boneWeights[jv * 4 + 4] = 1.0f;
		boneWeights[jv * 4 + 8] = 1.0f;

		// 3, 2, 1
		indices[ji] = jv + 2;
		indices[ji + 1] = jv + 1;
		indices[ji + 2] = jv;

		ji += 3;

		if (p.doubl()) {
		    // 1, 2, 3
		    indices[ji] = jv;
		    indices[ji + 1] = jv + 1;
		    indices[ji + 2] = jv + 2;

		    ji += 3;
		}

		jv += 3;

	    }
	}

	// mesh creation
	mesh = new Mesh();

	mesh.setBuffer(Type.Position, 3,
		BufferUtils.createFloatBuffer(vertices3));
	mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));
	mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
	mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(vertices3));

	FloatBuffer weights = BufferUtils.createFloatBuffer(boneWeights);
	VertexBuffer weightsBuf = new VertexBuffer(Type.BoneWeight);
	weightsBuf.setupData(Usage.CpuOnly, 4, Format.Float, weights);
	mesh.setBuffer(weightsBuf);

	ByteBuffer indices = BufferUtils.createByteBuffer(boneIndices);
	VertexBuffer indicesBuf = new VertexBuffer(Type.BoneIndex);
	indicesBuf.setupData(Usage.CpuOnly, 4, Format.UnsignedByte, indices);
	mesh.setBuffer(indicesBuf);

	// Create bind pose buffers
	mesh.generateBindPose(true);

	mesh.setMaxNumWeights(1);

	mesh.updateBound();
	
	meshGeom = new Geometry("Mesh", mesh);
    }

    public void buildMaterial(Material material, int paletteId) {
	textureMap.build(paletteId);
	textureMap.setTexture(material);
	meshGeom.setMaterial(material);
    }

    public void buildSkeleton() {
	// build bones
	bones = new Bone[numJoints * 2];

	// rotation bones
	for (int i = 0; i < numJoints; ++i) {
	    bones[i] = new Bone("RBone" + i);
	    bones[i].setBindTransforms(Vector3f.ZERO, Quaternion.IDENTITY,
		    Vector3f.UNIT_XYZ);
	}

	// translation bones
	for (int i = numJoints; i < numJoints * 2; ++i) {
	    bones[i] = new Bone("TBone" + i);
	    bones[i].setBindTransforms(Vector3f.ZERO, Quaternion.IDENTITY,
		    Vector3f.UNIT_XYZ);
	    bones[i - numJoints].addChild(bones[i]);
	}

	for (int i = 0; i < numJoints; ++i) {
	    if (joints[i].parentJointId < joints.length) {
		bones[joints[i].parentJointId + numJoints].addChild(bones[i]);
	    }
	}

	skeleton = new Skeleton(bones);
	skeleton.updateWorldVectors();
	skeleton.setBindingPose();
    }

    public void buildNode() {
	node = new Node();
	node.attachChild(meshGeom);

	// create and attach controls
	control = new AnimControl(skeleton);
	node.addControl(control);
	// adding the control to the geom has no effect
	// ogre mesh loader adds it to the node as well

	// additional skeleton control inspired by ogre mesh loader
	skeletonControl = new SkeletonControl(skeleton);
	node.addControl(skeletonControl);
	// trying to add the skeletonControl to the geom gives an exception

	node.scale(0.1f); // TODO this is a random default?
	node.rotate(FastMath.PI, 0, 0);
	node.updateModelBound();
    }

    public Vector3f pos(int i) {
	return new Vector3f(vertices[i].x, vertices[i].y, vertices[i].z);
    }

    public byte boneId(int i) {
	return (byte) (vertices[i].jointId);
    }
    
    public Node getNode() {
	return node;
    }

    public static void single() {
	try {
	    WEP t = new WEP(Util.read("OBJ/7F.WEP"));
	    t.read();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	single();
	// Util.test(true, new File("OBJ"), "WEP", WEP.class);
    }

    /**
     * @deprecated
     * @param pose
     */
    public void setPose(SEQAnimation pose) {
	if (pose != null && bones != null) {
	    for (int i = 1; i < numJoints; ++i) {
		bones[i].setUserControl(true);
		Quaternion q = pose.quaternions[i];
		bones[i].setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
	    }

	    for (int i = numJoints + 1; i < numJoints * 2; ++i) {

		bones[i].setUserControl(true);
		Vector3f v = new Vector3f(joints[i - numJoints].length, 0, 0);
		bones[i].setUserTransforms(v, Quaternion.IDENTITY,
			Vector3f.UNIT_XYZ);
	    }
	}
    }

    public int numJoints;
    public int numGroups;
    public int numTriangles;
    public int numQuads;
    public int numPolygons;
    public int numAllPolygons;
    public int numIndices;

    public int texturePtr;
    public int groupPtr;
    public int vertexPtr;
    public int polygonPtr;
    public int jointPtr;

    // joints
    public WEPJoint[] joints;

    // groups
    public WEPGroup[] groups;

    public int numVertices;

    // vertices
    public WEPVertex[] vertices;

    // polygons
    public WEPPolygon[] polygons;

    public WEPTextureMap textureMap;

    public Vector3f[] vertices3;
    public int[] verticesMap;
    public int[] indices;
    public Vector2f[] uv;
    public byte[] boneIndices;
    public float[] boneWeights;

    public Mesh mesh;
    public Skeleton skeleton;
    public Bone[] bones;

    public Geometry meshGeom;
    public Node node;
    public AnimControl control;
    public SkeletonControl skeletonControl;
    public AnimChannel channel;

}
