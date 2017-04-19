package sfb732.kamoso.net;


/**
 * Pre-defined network types.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 *
 */
public enum NetworkType {
	/** not a pre-defined network type */
	undefined,

	/** closed regular grid network (cf. Nettle 1999) */
	regTorus,

	/** small-world network based on regular grid (cf. Watts &amp; Strogatz 1998) */
	swTorus,

	/** parochial network based on regular grid */
	parTorus,

	/** parochial small-world network */
	parSW,

	/** single component network based on random graph (without self-edges) */
	random,

	/** a fully connected network */
	full;


	/**
	 * Get string with all types defined in this enumeration
	 * @return a string
	 */
	public static String getAll() {
		return "undefined, regTorus, swTorus, parTorus, parSW, random, full";
		//TODO check in JUnit test
	}
}
