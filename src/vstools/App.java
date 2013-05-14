package vstools;

import com.jme3.animation.Skeleton;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

public class App extends SimpleApplication implements ActionListener {

    @Override
    public void simpleInitApp() {

	// register "assets" subdirectory
	assetManager.registerLocator("assets", FileLocator.class);
	
	initFlyCam();

	initKeys();

	initStuff();
    }

    protected void initFlyCam() {
	// setup flycam and light
	// flycam is enabled by default
	flyCam.setMoveSpeed(100);
	flyCamLight = new SpotLight();
	flyCamLight.setColor(ColorRGBA.White);
	flyCamLight.setSpotRange(1000);
	flyCamLight.setSpotInnerAngle(80f * FastMath.DEG_TO_RAD);
	flyCamLight.setSpotOuterAngle(90f * FastMath.DEG_TO_RAD);
	rootNode.addLight(flyCamLight);
    }

    protected void initStuff() {
	// debug box for testing
	Geometry geom = new Geometry("Box", new Box(0.5f, 0.5f, 0.5f));
	geom.setLocalScale(1, 1, 1);
	geom.setMaterial(wireframe(ColorRGBA.Green));
	getRootNode().attachChild(geom);
    }

    protected void initKeys() {

    }

    @Override
    public void simpleUpdate(float tpf) {
	// deterministic game loop
	acc += tpf;

	while (acc >= dt) {
	    update(dt);
	    acc -= dt;
	}
    }

    public void update(float dt) {
	if (flyCamLight != null) {
	    flyCamLight.setPosition(cam.getLocation());
	    flyCamLight.setDirection(cam.getDirection());
	}
    }

    public void onAction(String name, boolean keyPressed, float tpf) {
	
    }

    public void debugSkeleton(Skeleton skeleton, Node node) {
	if (skeleton != null && node != null) {
	    skeletonDebug = new SkeletonDebugger("skeleton", skeleton);
	    Material mat = unshaded();
	    mat.setColor("Color", ColorRGBA.Green);
	    mat.getAdditionalRenderState().setDepthTest(false);
	    skeletonDebug.setMaterial(mat);
	    node.attachChild(skeletonDebug);
	}
    }

    public Material wireframe(ColorRGBA c) {
	Material mat = unshaded();
	mat.getAdditionalRenderState().setWireframe(true);
	mat.setColor("Color", c);
	return mat;
    }

    public Material debug() {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Light/Lighting.j3md");

	Texture texture = assetManager.loadTexture("Textures/debug.png");
	texture.setMagFilter(Texture2D.MagFilter.Nearest);
	mat.setTexture("DiffuseMap", texture);
	return mat;
    }
    
    public Material lighting() {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Light/Lighting.j3md");
	return mat;
    }

    public Material unshaded() {
	return new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    }

    public Material flat(ColorRGBA c) {
	Material mat = new Material(assetManager,
		"Common/MatDefs/Light/Lighting.j3md");

	mat.setColor("Diffuse", c);
	mat.setColor("Ambient", c);
	mat.setColor("Specular", c);
	mat.setBoolean("UseMaterialColors", true);
	return mat;
    }

    private final float dt = 0.001f;
    private float acc = 0.0f;

    protected SpotLight flyCamLight;

    protected SkeletonDebugger skeletonDebug;
}
