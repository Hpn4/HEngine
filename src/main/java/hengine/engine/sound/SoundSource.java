package hengine.engine.sound;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_SOURCE_ABSOLUTE;
import static org.lwjgl.openal.AL10.AL_SOURCE_RELATIVE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import org.joml.Vector3f;

public class SoundSource {

	private final int sourceId;

	public SoundSource(final boolean loop, final boolean relative) {
		this.sourceId = alGenSources();

		if (loop)
			alSourcei(sourceId, AL_LOOPING, AL_TRUE);

		alSourcei(sourceId, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
		alSourcei(sourceId, AL_SOURCE_ABSOLUTE, relative ? AL_FALSE : AL_TRUE);
	}

	public void setBuffer(final int bufferId) {
		stop();
		alSourcei(sourceId, AL_BUFFER, bufferId);
	}

	/**
	 * Définie la position du son dans l'espace
	 * 
	 * @param position La position du son
	 */
	public void setPosition(final Vector3f position) {
		alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
	}

	/**
	 * Définie la vitesse du son
	 * 
	 * @param speed La vitesse du son
	 */
	public void setSpeed(final Vector3f speed) {
		alSource3f(sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
	}

	public void setGain(final float gain) {
		alSourcef(sourceId, AL_GAIN, gain);
	}

	public void setProperty(final int param, final float value) {
		alSourcef(sourceId, param, value);
	}

	public void play() {
		alSourcePlay(sourceId);
	}

	public boolean isPlaying() {
		return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	public void pause() {
		alSourcePause(sourceId);
	}

	public void stop() {
		alSourceStop(sourceId);
	}

	public void cleanup() {
		stop();
		alDeleteSources(sourceId);
	}
}
