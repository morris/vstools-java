package vstools;

import java.io.File;
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
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

/**
 * WEP data structure reader and builder
 * @author morris
 *
 */
public class WEP extends Data {

    public WEP(ByteArray data) {
	super(data);
    }

    public void all() {
	read();
	build(0);
	buildMesh();
	buildSkeleton();
	buildNode();
    }

    public void read() {
	header();
	data();
	build(0);
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

    public void header() {

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

    public void data() {

	jointSection();
	groupSection();
	vertexSection();
	polygonSection();
	textureSection(5);

	// we are done
	log("done");
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
	    if (j.parentJoint < joints.length) {
		j.parentObject = joints[j.parentJoint];
	    }

	    log("joint " + i + ": s=" + j.length + " p=" + j.parentJoint + " " + j.x
		    + " " + j.y + " " + j.z + " " + j.mode);
	}
    }

    public void groupSection() {
	groups = new WEPGroup[numGroups];
	for (int i = 0; i < numGroups; ++i) {
	    groups[i] = new WEPGroup(data);
	    groups[i].read();
	    groups[i].jointObject = joints[groups[i].joint];
	    log("group " + i + ": joint=" + groups[i].joint + " lv="
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
	    vertices[i].group = g;
	    vertices[i].groupObject = groups[g];
	    vertices[i].joint = groups[g].joint;
	}
    }

    public void polygonSection() {
	polygons = new WEPPolygon[numAllPolygons];
	for (int i = 0; i < numAllPolygons; ++i) {
	    polygons[i] = new WEPPolygon(data);
	    polygons[i].read();
	}
    }

    public void textureSection(int palettes) {
	textureMap = new WEPTextureMap(data);
	textureMap.read(palettes);
    }

    public void build(int palette) {
	log("building...");

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
		vertices3[jv] = vec3(p.vertex1);
		vertices3[jv + 1] = vec3(p.vertex2);
		vertices3[jv + 2] = vec3(p.vertex3);
		vertices3[jv + 3] = vec3(p.vertex4);

		verticesMap[jv] = p.vertex1;
		verticesMap[jv + 1] = p.vertex2;
		verticesMap[jv + 2] = p.vertex3;
		verticesMap[jv + 3] = p.vertex4;

		uv[jv] = uvf(p.u1, p.v1, textureMap.width, textureMap.height);
		uv[jv + 1] = uvf(p.u2, p.v2, textureMap.width,
			textureMap.height);
		uv[jv + 2] = uvf(p.u3, p.v3, textureMap.width,
			textureMap.height);
		uv[jv + 3] = uvf(p.u4, p.v4, textureMap.width,
			textureMap.height);

		boneIndices[jv * 4] = bone(p.vertex1);
		boneIndices[jv * 4 + 4] = bone(p.vertex2);
		boneIndices[jv * 4 + 8] = bone(p.vertex3);
		boneIndices[jv * 4 + 12] = bone(p.vertex4);

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
		vertices3[jv] = vec3(p.vertex1);
		vertices3[jv + 1] = vec3(p.vertex2);
		vertices3[jv + 2] = vec3(p.vertex3);

		// uv is correctly reordered
		uv[jv] = uvf(p.u2, p.v2, textureMap.width, textureMap.height);
		uv[jv + 1] = uvf(p.u3, p.v3, textureMap.width,
			textureMap.height);
		uv[jv + 2] = uvf(p.u1, p.v1, textureMap.width,
			textureMap.height);

		boneIndices[jv * 4] = bone(p.vertex1);
		boneIndices[jv * 4 + 4] = bone(p.vertex2);
		boneIndices[jv * 4 + 8] = bone(p.vertex3);

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

	textureMap.build(palette);

	log("building done");
    }

    public Vector3f vec3(int i) {
	return new Vector3f(vertices[i].x, vertices[i].y, vertices[i].z);
    }

    public byte bone(int i) {
	return (byte) (vertices[i].joint);
    }

    public void seperate(int g, int x, int y, int z) {
	for (int i = 0; i < vertices.length; ++i) {
	    if (vertices[i].group == g) {
		vertices[i].x += x;
		vertices[i].y += y;
		vertices[i].z += z;
	    }
	}
    }

    public void buildMesh() {
	if (vertices3 != null && uv != null && indices != null
		&& boneWeights != null && boneIndices != null) {
	    // mesh creation
	    mesh = new Mesh();

	    // faces
	    mesh.setBuffer(Type.Position, 3,
		    BufferUtils.createFloatBuffer(vertices3));
	    mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(uv));
	    mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
	    mesh.setBuffer(Type.Normal, 3,
		    BufferUtils.createFloatBuffer(vertices3));

	    FloatBuffer weights = BufferUtils.createFloatBuffer(boneWeights);
	    VertexBuffer weightsBuf = new VertexBuffer(Type.BoneWeight);
	    weightsBuf.setupData(Usage.CpuOnly, 4, Format.Float, weights);
	    mesh.setBuffer(weightsBuf);

	    ByteBuffer indices = BufferUtils.createByteBuffer(boneIndices);
	    VertexBuffer indicesBuf = new VertexBuffer(Type.BoneIndex);
	    indicesBuf
		    .setupData(Usage.CpuOnly, 4, Format.UnsignedByte, indices);
	    mesh.setBuffer(indicesBuf);

	    // Create bind pose buffers
	    mesh.generateBindPose(true);

	    mesh.setMaxNumWeights(1);
	}
    }

    public void buildSkeleton() {
	if (joints != null) {
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
		if (joints[i].parentJoint < joints.length) {
		    bones[joints[i].parentJoint + numJoints].addChild(bones[i]);
		}
	    }

	    skeleton = new Skeleton(bones);
	    skeleton.updateWorldVectors();
	    skeleton.setBindingPose();
	}
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

    public void buildNode() {
	node = new Node();

	if (mesh != null) {
	    // create and attach node
	    meshGeom = new Geometry("Mesh", mesh);
	    mesh.updateBound();

	    node.attachChild(meshGeom);
	}

	if (skeleton != null) {
	    // create and attach controls
	    control = new AnimControl(skeleton);
	    node.addControl(control);
	    // adding the control to the geom has no effect
	    // ogre mesh loader adds it to the node as well

	    // additional skeleton control inspired by ogre mesh loader
	    skeletonControl = new SkeletonControl(skeleton);
	    node.addControl(skeletonControl);
	    // trying to add the skeletonControl to the geom gives an exception
	}

	node.scale(0.1f); // TODO this is a random default?
	node.rotate(FastMath.PI, 0, 0);
	node.setCullHint(CullHint.Never); // TODO fix bounding box for culling
    }

    public void buildMaterial(Material base) {
	if (meshGeom != null && textureMap != null) {
	    textureMap.setTexture(base);
	    meshGeom.setMaterial(base);
	}
    }

    public void setSEQ(SEQ seq) {
	this.seq = seq;
	for (int i = 0; i < seq.animations.length; ++i) {
	    control.addAnim(seq.animations[i].animation);
	}
    }

    public static void single() {
	try {
	    WEP t = new WEP(Util.read("OBJ/7F.WEP"));
	    t.header();
	    t.data();
	    t.build(0);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	// single();
	Util.test(false, new File("OBJ"), "WEP", WEP.class);
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
    
    public SEQ seq;

    public Geometry meshGeom;
    public Node node;
    public AnimControl control;
    public SkeletonControl skeletonControl;
    public AnimChannel channel;

}
