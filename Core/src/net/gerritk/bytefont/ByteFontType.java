package net.gerritk.bytefont;

import java.awt.*;

public enum ByteFontType {
	PLAIN(Font.PLAIN),
	BOLD(Font.BOLD),
	ITALIC(Font.ITALIC);

	public final int value;

	ByteFontType(int value) {
		this.value = value;
	}

	public static ByteFontType valueOf(int value) {
		for(ByteFontType type : values()) {
			if(type.value == value) {
				return type;
			}
		}
		return null;
	}
}
