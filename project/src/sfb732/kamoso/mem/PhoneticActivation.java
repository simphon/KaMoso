package sfb732.kamoso.mem;




/**
 * A container for the computation of phonetic activation.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public class PhoneticActivation {

	private final Exemplar ex;
	private final double activation;
	private final double distance;

	public PhoneticActivation(Exemplar ex, double activation, double distance) {
		this.ex         = ex;
		this.activation = activation;
		this.distance   = distance;
	}

	/**
	 * @return the exemplar
	 */
	public Exemplar getEx() {
		return ex;
	}

	/**
	 * @return the activation
	 */
	public double getActivation() {
		return activation;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}
}
