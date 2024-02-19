package hengine.game.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Test {

	public Test(final String fileName) {
		final String pathR = "resources/sky/";

		final BufferedImage img = new BufferedImage(2048 * 3, 2048 * 2, BufferedImage.TYPE_4BYTE_ABGR);

		final String[] name = { "_left", "_back", "_right", "_front", "_top", "_bottom" };
		final Graphics g = img.getGraphics();

		try {
			for (int i = 0; i < name.length; i++) {
				final File file = new File(pathR + fileName + name[i] + ".png");
				System.out.println(file);
				final BufferedImage part = ImageIO.read(file);

				if (i > 2)
					g.drawImage(part, (i - 3) * 2048, 2048, null);
				else
					g.drawImage(part, i * 2048, 0, null);
			}

			ImageIO.write(img, "PNG", new File("/Users/danielesenigout/Desktop/.etienne/game/3DGame/ab"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		Matrix4f tmp = new Matrix4f();
		
		Quaternionf quat = new Quaternionf();
		
		tmp.translationRotateScale(new Vector3f(0, 0, 0), quat, 1);
		
		System.out.println(tmp);
		System.out.println(Files.exists(Paths.get("resources/hole.png")));
	}

	public static List<Integer> getLists() {
		return List.of();
	}
}
