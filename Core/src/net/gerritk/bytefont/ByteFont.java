package net.gerritk.bytefont;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ByteFont {
	private String name;
	private int size;
	private int style;
	private int height;
	private HashMap<Character, byte[][]> byteChars;

	public ByteFont(String name, int size, int style, int height) {
		this.name = name;
		this.size = size;
		this.style = style;
		this.byteChars = new HashMap<>();
		this.height = height;
	}

	public ByteFont(String name, int size, int style) {
		this(name, size, style, -1);
	}

	public byte[][] getBytes(char c) {
		byte[][] original = byteChars.getOrDefault(c, null);
		byte[][] result;
		if(original != null) {
			result = new byte[original.length][original[0].length];
			for(int i = 0; i < result.length; i++) {
				result[i] = Arrays.copyOf(original[i], original[i].length);
			}
		} else {
			result = new byte[0][0];
		}
		return result;
	}

	public int getCharCount() {
		return byteChars.size();
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public int getStyle() {
		return style;
	}

	public int getHeight() {
		if(height < 0) {
			int maxHeight = 0;
			for(Map.Entry<Character, byte[][]> byteChar : byteChars.entrySet()) {
				if(byteChar.getValue().length > 0 && byteChar.getValue()[0].length > maxHeight) {
					maxHeight = byteChar.getValue()[0].length * 8;
				}
			}
			return maxHeight;
		} else {
			return height;
		}
	}

	private void setHeight(int height) {
		this.height = height;
	}

	protected boolean addChar(char c, byte[][] bytes) {
		return byteChars.putIfAbsent(c, bytes) == null;
	}

	public static ByteFont fromMeta(HashMap<String, Object> meta) {
		ByteFont result = null;

		try {
			String name = String.valueOf(meta.get("name"));
			int size = Integer.parseInt(String.valueOf(meta.get("size")));
			int style = Integer.parseInt(String.valueOf(meta.get("style")));

			if(!name.equals("null") && size > 0 && style > 0) {
				result = new ByteFont(name, size, style);

				if(meta.containsKey("height")) {
					result.setHeight(Integer.parseInt(String.valueOf(meta.getOrDefault("height", -1))));
				}
			}
		} catch (Exception e) {
			System.err.println("invalid meta file! (" + e.getMessage() + ")");
		}

		return result;
	}
}
