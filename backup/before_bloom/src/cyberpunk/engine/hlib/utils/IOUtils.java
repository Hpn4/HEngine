package hengine.engine.hlib.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryUtil;

public class IOUtils {

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
			try (final InputStream source = IOUtils.class.getClassLoader().getResourceAsStream(resource);
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
