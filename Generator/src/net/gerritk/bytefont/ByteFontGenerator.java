package net.gerritk.bytefont;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ByteFontGenerator {
    public static final String VERSION = "0.0.1";
    public static final int LOG_DEBUG = 3;
    public static final int LOG_MIDDLE = 2;
    public static final int LOG_LOW = 1;

    private static Font font;
    private static File out;
    private static int rangeStart = 0;
    private static int rangeEnd = 256;
    private static boolean comments;
    private static int verbose = LOG_LOW;

    public static void main(String[] args) throws IOException {
        log("ByteFontGenerator V" + VERSION + " by K.Design", LOG_LOW);

        boolean parsed = parseArgs(args);
        if(!parsed) {
            error("failed parsing arguments.");
            System.exit(-1);
        }


        log(String.format("generating byte font for '%s' with size '%d' and style '%d'.", font.getFontName(), font.getSize(), font.getStyle()), LOG_LOW);
        long start = System.currentTimeMillis();
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));
        BufferedImage img = new BufferedImage(font.getSize() * 2, font.getSize() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("name", font.getName());
		meta.put("size", font.getSize());
		meta.put("style", font.getStyle());
		meta.put("height", g.getFontMetrics().getHeight());

		writer.write("META_BEGIN");
		writer.newLine();
		for (Map.Entry<String, Object> metaEntry : meta.entrySet()) {
			writer.write("  " + metaEntry.getKey().toUpperCase() + " " + metaEntry.getValue().toString());
			writer.newLine();
		}
		writer.write("META_END");
		writer.newLine();

		writer.newLine();
        for(int i = rangeStart; i < rangeEnd; i++) {
            byte[][] byteChar = getByteChar((char) i, g, img);


            if(byteChar.length > 0) {
                if(comments) {
                    writer.write(String.format("# char '%c' with decimal value '%d'", (char) i, i));
                    writer.newLine();
                }
                writer.write("BEGIN " + i);
                writer.newLine();

                for(int y = 0; y < byteChar[0].length; y++) {
					writer.write("  ");
                    for(int x = 0; x < byteChar.length; x++) {
                        writer.write(Integer.toHexString(Byte.toUnsignedInt(byteChar[x][y])));
                        if(x < byteChar.length - 1) {
                            writer.write(";");
                        }
                    }
                    if(y < byteChar[0].length - 1) {
                        writer.newLine();
                    }
                }

                writer.newLine();
                writer.write("END");
                writer.newLine();
            }
        }

        writer.flush();
        writer.close();

        log("finished generating byte font (" + (System.currentTimeMillis() - start) + "ms).", LOG_LOW);
    }

    public static byte[][] getByteChar(char c, Graphics2D g, BufferedImage img) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds("" + c, g);

        int bh = metrics.getHeight() % 8 > 0 ? (metrics.getHeight() + 4) / 8 + 1 : metrics.getHeight() / 8;
        byte[][] result = new byte[(int) bounds.getWidth()][bh];

        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(String.format("'%c' | %d : {", c, (int) c));
        builder.append("\n");

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setColor(Color.BLACK);
        g.drawString("" + c, 0, metrics.getMaxAscent());

        for (int y = 0; y < metrics.getHeight(); y++) {
            builder.append(String.format("%d:\t", y));
            for (int x = 0; x < bounds.getWidth(); x++) {
                Color color = new Color(img.getRGB(x, y));
                if (color.equals(Color.BLACK)) {
                    builder.append("#");
                    result[x][y >> 3] |= (1 << (y & 7));
                } else {
                    builder.append("-");
                }
            }
            builder.append("\n");
        }
        builder.append("}");
        log(builder.toString(), LOG_DEBUG);
        return result;
    }

    private static void log(String log, int level) {
        if(level <= verbose) {
            System.out.println(log);
        }
    }

    private static void error(String log) {
        if(LOG_LOW <= verbose) {
            System.err.println(log);
        }
    }

    private static boolean parseArgs(String[] args) {
        log("parsing arguments...", LOG_MIDDLE);
        long start = System.currentTimeMillis();

        String fontName = "Arial";
        int fontStyle = Font.PLAIN;
        int fontSize = 10;

        for(int i = 0; i < args.length; i++) {
            try {
                if (args[i].startsWith("-")) {
                    switch (args[i]) {
                        case "-c":
                        case "--comments":
                            comments = true;
                            log("file comments switched on.", LOG_DEBUG);
                            break;
                        case "-v":
                        case "--verbose":
                            if (args.length > i + 1) {
                                verbose = Integer.parseInt(args[++i]);
                                log("verbosity level set to '" + verbose + "'.", LOG_DEBUG);
                            }
                            break;
                        case "-f":
                        case "--font":
                            if (args.length > i + 1) {
                                fontName = args[++i];
                                log("font switched to '" + fontName + "'.", LOG_DEBUG);
                            }
                            break;
                        case "-s":
                        case "--size":
                            if (args.length > i + 1) {
                                fontSize = Integer.parseInt(args[++i]);
                                log("font size switched to '" + fontSize + "'.", LOG_DEBUG);
                            }
                            break;
                        case "-y":
                        case "--style":
                            if (args.length > i + 1) {
                                try {
                                    int tmpStyle = Integer.parseInt(args[++i]);
                                    if (fontStyle == Font.PLAIN || fontStyle == Font.BOLD || fontStyle == Font.ITALIC) {
                                        fontStyle = tmpStyle;
                                    }
                                } catch (NumberFormatException e) {
                                    if (args[i].equalsIgnoreCase("PLAIN") || args[i].equalsIgnoreCase("P")) {
                                        fontStyle = Font.PLAIN;
                                    } else if (args[i].equalsIgnoreCase("BOLD") || args[i].equalsIgnoreCase("B")) {
                                        fontStyle = Font.BOLD;
                                    } else if (args[i].equalsIgnoreCase("ITALIC") || args[i].equalsIgnoreCase("I")) {
                                        fontStyle = Font.ITALIC;
                                    }
                                }
                                log("font style switched to '" + fontStyle + "'.", LOG_DEBUG);
                            }
                            break;
                        case "-r":
                        case "--range":
                            if (args.length > i + 1) {
                                rangeStart = Integer.parseInt(args[++i]);
                                log("char range start switched to '" + rangeStart + "'.", LOG_DEBUG);
                                if(args.length > i + 1) {
                                    rangeEnd = Integer.parseInt(args[++i]);
                                    log("char range end switched to '" + rangeEnd + "'.", LOG_DEBUG);
                                }
                            }
                            break;
                        case "-o":
                        case "--out":
                            if (args.length > i + 1) {
                                File tmpOut = new File(args[++i]);
                                if (!tmpOut.isDirectory()) {
                                    out = tmpOut;
                                    log("output switched to '" + out.getPath() + "'.", LOG_DEBUG);
                                }
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                error("parsing error: " + e.getMessage());
            }
        }
        log("finished parsing arguments (" + (System.currentTimeMillis() - start) + "ms).", LOG_MIDDLE);

        font = new Font(fontName, fontStyle, fontSize);
        if(out == null) {
            out = new File(String.format("%s_%s_%d.bff", fontName.toLowerCase().replace(" ", ""), fontStyle == Font.PLAIN ? "p" : fontStyle == Font.BOLD ? "b" : "i", fontSize));
        }

        try {
            if(out.getParentFile() != null) {
                boolean mkDirs = out.getParentFile().mkdirs();
                if(mkDirs) {
                    log("created parent directories for output file.", LOG_DEBUG);
                }
            }
            boolean createdFile = out.createNewFile();
            if(createdFile) {
                log("created output file.", LOG_DEBUG);
            }
        } catch (IOException e) {
            error("error: " + e.getMessage());
            return false;
        }

        return true;
    }
}
