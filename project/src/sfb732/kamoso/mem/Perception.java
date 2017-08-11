package sfb732.kamoso.mem;

/**
 * Interface for different perceptual systems.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public interface Perception {

	/**
	 * Type of perceptual system
	 */
	public enum Type {
		magnet,
		linear
	}


	/**
	 * Get percept according to current perceptual system.
	 * @param stimulus
	 * @param socialCloseness
	 * @return a new exemplar instance.
	 */
	public Exemplar getPercept(Exemplar stimulus, double socialCloseness);

	/**
	 * @return the type of perceptual system implemented by this instance.
	 */
	public Type getType();
	
	
	/**
	 * @return <code>true</code> if the implementation of this interface assigns
	 * the {@link sfb732.kamoso.mem.Exemplar.Type} of a stimulus according to
	 * the underlying lexicon.
	 */
	public boolean isCategorizing();

}
