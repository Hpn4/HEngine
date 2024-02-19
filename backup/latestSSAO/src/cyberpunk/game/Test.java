package hengine.game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

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
		System.getProperties().entrySet().stream().forEach(System.out::println);
	}

	public static List<Integer> getLists() {
		return List.of();
	}
}
