package qa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
	public static void Write(String filename, String str) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(str);
			writer.close();
		} catch (Exception ex) {
		}
	}
	
	public static void Append(String filename, String str) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			writer.write(str);
			writer.close();
		} catch (Exception ex) {
		}
	}
}