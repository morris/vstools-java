package vstools;

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Properties;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class Util {

    public static String hex(int i) {
	return "0x" + Integer.toHexString(i);
    }

    public static String hex(int i, int width) {
	String h = Integer.toHexString(i);
	if (i >= 0) {
	    h = String.format("%" + width + "s", h).replace(' ', '0');
	}

	return h.substring(h.length() - width);
    }

    public static String hex(int[] is) {
	String s = "";
	for (int i : is) {
	    s += hex(i) + " ";
	}
	return s;
    }

    public static String hex(int[] is, int width) {
	String s = "";
	for (int i : is) {
	    s += hex(i, width) + " ";
	}
	return s;
    }

    public static String bin(int i) {
	return Integer.toBinaryString(i) + "b";
    }

    public static String bin(int i, int width) {
	String h = Integer.toBinaryString(i);
	if (i >= 0) {
	    h = String.format("%" + width + "s", h).replace(' ', '0');
	}

	return h.substring(h.length() - width);
    }

    public static String bin(int[] is) {
	String s = "";
	for (int i : is) {
	    s += bin(i) + " ";
	}
	return s;
    }

    public static String bin(int[] is, int width) {
	String s = "";
	for (int i : is) {
	    s += bin(i, width) + " ";
	}
	return s;
    }

    /**
     * Read a file completely into a byte array. Looks for the file in the
     * dataPath set in the config.ini
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public static ByteArray read(String filename) throws IOException {
	return read(file(filename));
    }

    public static ByteArray read(File file) throws IOException {
	RandomAccessFile fis = new RandomAccessFile(file, "r");
	byte[] data = new byte[(int) file.length()];
	fis.readFully(data);
	fis.close();
	return new ByteArray(data);
    }

    /**
     * Finds a file in the dataPath set in the config.ini
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public static File file(String filename) {
	File file = new File(filename);
	if (!file.exists()) {
	    file = new File(getConfig().getProperty("dataPath") + "/"
		    + filename);
	}
	return file;
    }

    public static String ext(File file) {
	return ext(file.getAbsolutePath());
    }

    public static String ext(String path) {
	int i = path.lastIndexOf('.');
	int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

	if (i > p) {
	    return path.substring(i + 1).toLowerCase();
	} else {
	    return "";
	}
    }

    public static class ExtensionFilter implements FileFilter {
	public ExtensionFilter(String ext) {
	    this.ext = ext;
	}

	public boolean accept(File file) {
	    return ext.equals(ext(file.getAbsolutePath()));
	}

	String ext;
    }
    
    /**
     * Convert absolute coordinates on an image to relative UV coordinates.
     * @param x
     * @param y
     * @param width image width
     * @param height image height
     * @return
     */
    public static Vector2f abs2uv(int x, int y, int width, int height) {
	return new Vector2f((float) x / (float) width, (float) y / (float) height);
    }
    
    public static Color color(int bits) {
	int a = (bits & 0x8000) >> 15;
	int b = (bits & 0x7C00) >> 10;
	int g = (bits & 0x03E0) >> 5;
	int r = bits & 0x1F;
	
	if (a == 0 && b == 0 && g == 0 && r == 0) {
	    // 0,0,0 is defined as transparent
	    return new Color(0, 0, 0, 0);
	} else if (a == 0) {
	    // 5bit -> 8bit is factor 2^3 = 8
	    // TODO different conversions were suggested, investigate
	    return new Color(r * 8, g * 8, b * 8);
	} else {
	    return new Color(0, 0, 0, 0);
	}
    }

    public static Quaternion quat(float u, float v, float w) {
	Quaternion qu = new Quaternion();
	qu.fromAngleAxis(u, Vector3f.UNIT_X);
	Quaternion qv = new Quaternion();
	qv.fromAngleAxis(v, Vector3f.UNIT_Y);
	Quaternion qw = new Quaternion();
	qw.fromAngleAxis(w, Vector3f.UNIT_Z);

	return qw.mult(qv.mult(qu));
    }

    public static Quaternion quatd(float u, float v, float w) {
	return quat(u * FastMath.DEG_TO_RAD, v * FastMath.DEG_TO_RAD, w
		* FastMath.DEG_TO_RAD);
    }

    public static void quattest() {
	qtv(100, 0, 0, 45, 0, 0); // 100 0 0 (no changes)

	qtv(100, 0, 0, 0, 45, 0); // 70 0 -70
	qtv(100, 0, 0, 0, 0, 45); // 70 70 0

	qtv(100, 0, 0, 45, 45, 0); // 70 0 -70
	qtv(100, 0, 0, 45, 0, 45); // 70 -70 0
	qtv(100, 0, 0, 0, 10, 10); // 97 17 -17
	qtv(100, 0, 0, 0, 45, 90); // 0 70 -70

	qtv(100, 0, 0, 0, 0, 270); // 0 -100 0

	qtv(100, 0, 0, 0, 290, 60); // near 100 0 0
    }

    public static void qtv(float x, float y, float z, float u, float v, float w) {
	Quaternion q = quatd(u, v, w);
	Vector3f p = new Vector3f(x, y, z);
	q.multLocal(p);
	log(p);
    }

    public static void log(Object t) {
	System.out.println(t);
    }

    public static Properties getConfig() {
	if (config == null) {
	    Properties defaults = new Properties();
	    defaults.put("dataPath", ".");
	    defaults.put("currentDirectory", ".");
	    defaults.put("open", "");
	    defaults.put("openSEQ", "");

	    config = new Properties(defaults);

	    File configFile = new File("config.ini");
	    if (configFile.exists()) {
		try {
		    config.load(new FileInputStream(configFile));
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}

	return config;
    }

    public static void storeConfig() {
	try {
	    config.store(new FileOutputStream("config.ini"), "");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Utility function for automatic testing
     * 
     * @param all
     * @param file
     * @param extension
     * @param type
     * @return
     */
    public static boolean test(boolean all, File file, String extension,
	    Class<?> type) {
	try {
	    Constructor<?> ctor = type.getDeclaredConstructor(ByteArray.class);
	    ctor.setAccessible(true);
	    if (file.isFile()) {
		log("-- scanning " + file.getCanonicalPath());
		Data t = (Data) ctor.newInstance(read(file));
		try {
		    t.verbose = false;
		    t.read();
		} catch (Exception e) {
		    e.printStackTrace();
		    return all;
		}

		return true;
	    } else if (file.isDirectory()) {
		for (File f : file
			.listFiles(new Util.ExtensionFilter(extension))) {
		    if (!test(all, f, extension, type)) {
			return all;
		    }
		}
		return true;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    private static Properties config;
}
