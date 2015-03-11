package net.gerritk.bytefont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class ByteFontReader {
	public static ByteFont loadByteFont(File src) {
		if (!src.exists() && !src.isFile()) {
			return null;
		}

		ByteFont result = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(src));
			String line;
			int lineCount = 0;

			HashMap<String, Object> meta = null;

			Character c = null;
			byte[][] bytes = null;

			while ((line = reader.readLine()) != null) {
				lineCount++;
				line = line.trim();

				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}

				if (result == null) {
					if(line.equals("META_BEGIN")) {
						meta = new HashMap<>();
					} else if(line.equals("META_END")) {
						ByteFont tmpResult = ByteFont.fromMeta(meta);
						if(tmpResult != null) {
							result = tmpResult;
						} else {
							System.err.println("failed to load byte font from meta!");
							break;
						}
					} else if(meta != null) {
						String[] kv = line.split(" ", 2);
						if(kv.length >= 2) {
							meta.put(kv[0].toLowerCase(), kv[1]);
						}
					}
				} else {
					if (line.startsWith("BEGIN ")) {
						try {
							int cInt = Integer.parseInt(line.replace("BEGIN ", ""));
							c = (char) cInt;
							bytes = new byte[0][0];
						} catch(NumberFormatException e) {
							System.err.println("could not read begin at line " + lineCount + " '" + line + "'!");
						}
					} else if (line.equals("END")) {
						if(c != null) {
							result.addChar(c, bytes);
						}
						c = null;
						bytes = null;
					} else if (c != null && bytes != null) {
						try {
							String[] strBytes = line.split(";");
							byte[] lineBytes = new byte[strBytes.length];
							for (int i = 0; i < strBytes.length; i++) {
								int cInt = Integer.parseInt(strBytes[i], 16);
								lineBytes[i] = (byte) cInt;
							}

							bytes = Arrays.copyOf(bytes, bytes.length + 1);
							bytes[bytes.length - 1] = lineBytes;
						} catch(NumberFormatException e) {
							System.err.println("could not read bytes at line " + lineCount + " '" + line + "' for char '" + c + "'!");
							c = null;
							bytes = null;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}

		return result;
	}
}
