package vstools;

import java.io.File;
import java.io.IOException;

public class Scan extends Data {

    public Scan(ByteArray data) {
	super(data);
    }

    public static void main(String[] args) {
	try {
	    Scan.scan(Util.file("MAP"));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void scan(File file) throws IOException {
	if (file.isFile()) {
	    //Util.log("-- scanning " + file.getCanonicalPath());
	    Util.log("\n*\n");
	    Scan scan = new Scan(Util.read(file));
	    try {
		scan.scanDialog();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	} else if (file.isDirectory()) {
	    for (File f : file.listFiles(new Util.ExtensionFilter("MPD"))) {
		scan(f);
	    }
	}
    }
    
    

    public void scanDialog() throws IOException {
	try {
	    buffer = new int[512];
	    index = 0;
	    while (true) {
		int b = u8();
		if (b == 0xFB) {
		    skip(1); // TODO unused id
		    b = u8();

		    if (b == 0xFA) {
			skip(1);
			index = 0;

			while (true) {
			    if (index >= 512) {
				break;
			    }
			    b = u8();
			    if (b == 0xE7) {
				log("--");
				log(Text.convert(buffer, index));
				break;
			    }
			    buffer[index] = b;
			    ++index;
			}
		    }
		}
	    }
	} catch (Exception e) {
	    
	}
    }

    public int buffer[];
    public int index;

}
