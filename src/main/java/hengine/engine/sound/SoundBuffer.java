package hengine.engine.sound;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import hengine.engine.utils.Utils;

public class SoundBuffer {

	private final int bufferId;

	private ShortBuffer pcm;

	private ByteBuffer vorbis;

	public SoundBuffer(final String file) throws Exception {
		bufferId = alGenBuffers();
		try (final STBVorbisInfo info = STBVorbisInfo.malloc()) {
			readVorbis(file, info);

			// Copy to buffer
			alBufferData(bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm,
					info.sample_rate());
		}
	}

	private void readVorbis(final String resource, final STBVorbisInfo info) throws Exception {
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			vorbis = Utils.ioResourceToByteBuffer(resource);

			final IntBuffer error = stack.mallocInt(1);
			final long decoder = stb_vorbis_open_memory(vorbis, error, null);
			if (decoder == NULL)
				throw new RuntimeException("Erreur lors du chargement du fichier " + resource + " : " + error.get(0));

			stb_vorbis_get_info(decoder, info);

			final int channels = info.channels();

			final int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

			pcm = MemoryUtil.memAllocShort(lengthSamples);

			pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
			stb_vorbis_close(decoder);
		}
	}

	public int getBufferId() {
		return bufferId;
	}

	public void cleanup() {
		alDeleteBuffers(bufferId);

		if (pcm != null)
			MemoryUtil.memFree(pcm);
		if (vorbis != null)
			MemoryUtil.memFree(vorbis);
	}
}
