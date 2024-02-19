package hengine.engine.graph;

public class RendererOption {

	/**
	 * Si la scene a ete mis a jour ( plus de lumiere, changement d'orientation de la camera...)
	 */
	public boolean sceneChanged = true;
	
	/**
	 * Lorsque ce flag est actif, le temps des différentes etapes de la frame sont affiches
	 */
	public boolean outPutGBUInfo;
	
	/**
	 * Si lors de la derniere phase de post processing l'exposure doit être appliquée
	 */
	public boolean applyExposure = true;
	
}
