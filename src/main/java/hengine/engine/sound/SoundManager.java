package hengine.engine.sound;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import hengine.engine.graph.Camera;

public class SoundManager {

	private long device;

	private long context;

	private SoundListener listener;

	private final Map<String, SoundBuffer> soundBufferList;

	private final Map<String, SoundSource> soundSourceMap;

	public SoundManager() {
		soundBufferList = new HashMap<>();
		soundSourceMap = new HashMap<>();
	}

	public void init() throws Exception {
		device = alcOpenDevice((ByteBuffer) null);
		if (device == NULL)
			throw new IllegalStateException("Failed to open the default OpenAL device.");

		final ALCCapabilities deviceCaps = ALC.createCapabilities(device);

		context = alcCreateContext(device, (IntBuffer) null);
		if (context == NULL)
			throw new IllegalStateException("Failed to create OpenAL context.");

		alcMakeContextCurrent(context);
		AL.createCapabilities(deviceCaps);

		listener = new SoundListener();
	}

	/**
	 * Ajoute un sond d'ambiance. Le son est préparé, pour le jouer il suffit de
	 * faire : {@code playSoundSource(name)}
	 * 
	 * @param name Le nom sous lequel enregistrer le son
	 * @param file Le chemin du son
	 * 
	 * @throws Exception
	 * 
	 * @see {@link #playSoundSource(String)}
	 */
	public void addAmbientSound(final String name, final String file) throws Exception {
		addSound(name, file, new Vector3f(), false, true);
	}

	/**
	 * Ajoute un nouveau sond dans la liste (le son est chargé, enregistré et
	 * preparé).<br/>
	 * <p>
	 * Le sond est joué en 3D. Si la position du sond est relative, cela signifie
	 * qu'il sera joue en {@code position} dans un repère où l'origine est la caméra
	 * (ex: les bruits de pas). <br/>
	 * Si la position est absolue ({@code relative=false}) le sond sera joué dans
	 * l'espace mais cette fois ci, l'origine sera celui du monde (ex:une voiture
	 * qui roule)
	 * </p>
	 * Pour le jouer il suffit de faire : <br/>
	 * 
	 * {@code #playSoundSource(name)}
	 * 
	 * @param name     Le nom sous lequel enregistrer le son (ce qui permettra de le
	 *                 jouer plus tard)
	 * @param file     Le chemin du fichier du sond
	 * @param position La position en 3D du sond
	 * @param loop     Si le sond doit être repetté en boucle (dès qu'il finit, il
	 *                 recommence)
	 * @param relative Si la position du sond est relative au joueur ou non.
	 * 
	 * @throws Exception
	 * 
	 * @see #playSoundSource(String)
	 * @see #getSoundSource(String)
	 * @see #addSoundSource(String, SoundSource)
	 * @see #removeSoundSource(String)
	 */
	public void addSound(final String name, final String file, final Vector3f position, final boolean loop,
			final boolean relative) throws Exception {
		// On vérifie préalablement que le sont n'a pas déjà été chargé (pour évité de
		// le charger deux fois)
		SoundBuffer soundBuf = soundBufferList.get(file);
		if (soundBuf == null) {
			soundBuf = new SoundBuffer(file);
			soundBufferList.put(file, soundBuf);
		}

		// On créer la source du sond en spécifiant tout les pafamètres
		final SoundSource soundSrc = new SoundSource(loop, relative);
		soundSrc.setPosition(position);
		soundSrc.setBuffer(soundBuf.getBufferId());

		soundSourceMap.put(name, soundSrc);
	}

	public void addSoundSource(final String name, final SoundSource soundSource) {
		soundSourceMap.put(name, soundSource);
	}

	public SoundSource getSoundSource(final String name) {
		return soundSourceMap.get(name);
	}

	public void playSoundSource(final String name) {
		final SoundSource soundSource = soundSourceMap.get(name);
		if (soundSource != null && !soundSource.isPlaying())
			soundSource.play();
	}

	public void removeSoundSource(final String name) {
		soundSourceMap.remove(name);
	}

	public void addSoundBuffer(final String file) throws Exception {
		soundBufferList.put(file, new SoundBuffer(file));
	}

	public void updateListenerPosition(final Camera camera) {
		if (listener != null) {
			final Matrix4f mat = camera.getViewMatrix();

			listener.setPosition(camera.getPosition());

			final Vector3f at = new Vector3f();
			mat.positiveZ(at).negate();

			final Vector3f up = new Vector3f();
			mat.positiveY(up);

			listener.setOrientation(at, up);
		}
	}
	
	public SoundListener getListener() {
		return listener;
	}

	public void setListener(final SoundListener listener) {
		this.listener = listener;
	}

	public void setAttenuationModel(final int model) {
		alDistanceModel(model);
	}

	public void cleanup() {
		for (final SoundSource soundSource : soundSourceMap.values())
			soundSource.cleanup();
		soundSourceMap.clear();

		for (final SoundBuffer soundBuffer : soundBufferList.values())
			soundBuffer.cleanup();
		soundBufferList.clear();

		if (context != NULL)
			alcDestroyContext(context);

		if (device != NULL)
			alcCloseDevice(device);
	}
}
