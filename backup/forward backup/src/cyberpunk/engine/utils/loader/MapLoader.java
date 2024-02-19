package hengine.engine.utils.loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.joml.Vector4f;

import hengine.engine.graph.light.Material;
import hengine.engine.graph.mesh.InstancedMesh;
import hengine.engine.graph.mesh.Mesh;
import hengine.engine.item.GameItem;
import hengine.engine.utils.Utils;

public class MapLoader {

	public static GameItem[] loadMap(final String internalPath) throws Exception {
		final BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(Utils.getPath(internalPath))));
		String line;

		GameItem[] items = null;
		GameItem tmp = null;
		int index = 0;

		while ((line = br.readLine()) != null) {
			if (line.startsWith("nmbItem"))
				items = new GameItem[getInt(line, "nmbItem")];

			else if (line.startsWith("item")) {
				if (tmp != null)
					items[index++] = new GameItem(tmp);

				final String[] args = getStrings(line, "item");
				tmp = load(args[0], args[1]);

			} else if (line.startsWith("position")) {
				final float[] pos = getFloats(line, "position");
				tmp.setPosition(pos[0], pos[1], pos[2]);

			} else if (line.startsWith("rotation")) {
				final float[] rot = getFloats(line, "rotation");
				tmp.setRotation(rot[0], rot[1], rot[2]);

			} else if (line.startsWith("scale"))
				tmp.setScale(getFloat(line, "scale"));
			else if (line.startsWith("color")) {
				final float[] color = getFloats(line, "color");
				tmp.getMesh().setMaterial(new Material(new Vector4f(color[0], color[1], color[2], color[3]), 1));
			}
		}
		items[index] = tmp;

		br.close();
		return items;
	}

	private static int getInt(final String line, final String tag) {
		final int length = (tag + ":").length();
		return Integer.parseInt(line.substring(length));
	}

	private static float getFloat(final String line, final String tag) {
		final int length = (tag + ":").length();
		return Float.parseFloat(line.substring(length));
	}

	private static float[] getFloats(final String line, final String tag) {
		final int length = (tag + ":").length();
		final String[] part = line.substring(length).split(" ");

		final int count = part.length;
		final float[] floats = new float[count];

		for (int i = 0; i < count; i++)
			floats[i] = Float.parseFloat(part[i]);

		return floats;
	}

	private static String[] getStrings(final String line, final String tag) {
		final int length = (tag + ":").length();
		return line.substring(length).split(" ");
	}

	public static GameItem load(final String dir, final String extension) throws Exception {
		final long time = System.currentTimeMillis();
		final String path = "models/" + dir + "/";
		final Mesh[] mesh = StaticMeshesLoader.load(path + extension, path);

		System.out.println(path + extension + " charger en : " + (System.currentTimeMillis() - time)
				+ " ms\n\tNmb meshes : " + mesh.length);
		return new GameItem(mesh);
	}

	public static GameItem loadInstanced(final String dir, final String extension, final int instances) throws Exception {
		final long time = System.currentTimeMillis();
		final String path = "models/" + dir + "/";
		final InstancedMesh[] mesh = StaticMeshesLoader.loadInstanced(path + extension, path, instances);

		System.out.println(path + extension + " charger en : " + (System.currentTimeMillis() - time)
				+ " ms\n\tNmb meshes : " + mesh.length);
		return new GameItem(mesh);
	}
}
