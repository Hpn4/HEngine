package hengine.engine.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

public class Utils {

	public static final String rscPath = "resources/";

	public static String loadResource(final String fileName) throws Exception {
		final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getPath(fileName))));
		String ligne, contenue = "";

		while ((ligne = br.readLine()) != null)
			contenue += ligne + "\n";

		br.close();
		return contenue;
	}

	public static String getPath(final String fileName) {
		return rscPath + fileName;
	}
	
	public static float toRadians(final double angle) {
		return (float) Math.toRadians(angle);
	}

	public static float[] listToArray(List<Float> list) {
		final int size = list != null ? list.size() : 0;
		final float[] floatArr = new float[size];
		for (int i = 0; i < size; i++)
			floatArr[i] = list.get(i);

		return floatArr;
	}
	
    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

	public static ByteBuffer ioResourceToByteBuffer(final String resource, final int bufferSize) throws IOException {
		ByteBuffer buffer;

		final Path path = Paths.get(resource);
		if (Files.isReadable(path)) {
			try (SeekableByteChannel fc = Files.newByteChannel(path)) {
				buffer = MemoryUtil.memAlloc((int) fc.size() + 1);
				while (fc.read(buffer) != -1) {
					;
				}
			}
		} else {
			try (final InputStream source = Utils.class.getClassLoader().getResourceAsStream(resource);
					final ReadableByteChannel rbc = Channels.newChannel(source)) {
				buffer = MemoryUtil.memAlloc(bufferSize);

				while (true) {
					int bytes = rbc.read(buffer);
					if (bytes == -1) {
						break;
					}
					if (buffer.remaining() == 0) {
						buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
					}
				}
			}
		}

		buffer.flip();
		return buffer.slice();
	}

	private static ByteBuffer resizeBuffer(final ByteBuffer buffer, final int newCapacity) {
		final ByteBuffer newBuffer = MemoryUtil.memAlloc(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
}
