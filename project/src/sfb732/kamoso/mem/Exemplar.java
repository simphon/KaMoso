package sfb732.kamoso.mem;

import sfb732.kamoso.conf.Configuration;
import sfb732.kamoso.pop.Agent;
import sfb732.kamoso.pop.Agent.Gender;
import sfb732.kamoso.util.MyMathHelper;



/**
 * Exemplar.
 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
 */
public class Exemplar {



	/**
	 * Get the phonetic distance between two exemplars.
	 * @param a
	 * @param b
	 * @return a non-negative double
	 */
	public static double getPhoneticDistance(Exemplar a, Exemplar b) {
		return MyMathHelper.getEuclideanDistance(a.phonFeatures, b.phonFeatures);
	}


	// ===================================================================


	/**
	 * Tags for possible types of exemplars (e.g. phonetic variants).
	 * @author Daniel Duran, Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
	 */
	public enum Type {
		A,
		B,
		undefined
	}


	// ===================================================================
	//
	// ===================================================================


	protected final Type type;
	protected final double status;
	protected final Agent.Gender gender;
	protected final double closeness;
	protected final double[] phonFeatures;

	protected final double socialScores;// caching


	/**
	 * Constructor. Should not be used!
	 * @param type
	 * @param status
	 * @param gender
	 * @param closeness
	 * @param phonFeatures
	 * @param socialScores -- pre-computed scores for social features
	 */
	public Exemplar(Type type, double status, Gender gender, double closeness,
			double[] phonFeatures, double socialScores) {
		super();
		this.type = type;
		this.status = status;
		this.gender = gender;
		this.closeness = closeness;
		this.phonFeatures = phonFeatures;
		this.socialScores = socialScores;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the status
	 */
	public double getSpeakerStatus() {
		return status;
	}

	/**
	 * @return the gender
	 */
	public Agent.Gender getSpeakerGender() {
		return gender;
	}

	/**
	 * @return the closeness
	 */
	public double getSpeakerCloseness() {
		return closeness;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public double getPhoneticFeature(int i) {
		return this.phonFeatures[i];
	}


	public int getPhoneticFeatureDims() {
		return this.phonFeatures.length;
	}


	/**
	 * Get phonetic distance between this exemplar and the other one
	 * @param other
	 * @return a double
	 */
	public double getDistance(Exemplar other) {
		return MyMathHelper.getEuclideanDistance(phonFeatures, other.phonFeatures);
	}


	/**
	 * Get string representation of the phonetic feature vector
	 * @return a string
	 */
	public String getPhoneticString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Configuration.DEFAULT_LOCALE,"%.3f", this.phonFeatures[0]));
		for(int i=1; i<this.phonFeatures.length; i++) {
			sb.append(String.format(Configuration.DEFAULT_LOCALE,",%.3f", this.phonFeatures[i]));
		}
		return sb.toString();
	}


	@Override
	public String toString() {
		return "[t=" + type + ", s=" + status + ", g="
				+ gender + ", c=" + closeness + ", p="
				+ getPhoneticString() + "]";
	}


	public String getCSVHead() {
		StringBuilder sb = new StringBuilder();
		sb.append("type,speaker_status,speaker_gender,closeness");
		for(int i=0; i<this.phonFeatures.length; i++) {
			sb.append(",phon_");
			sb.append(String.format(Configuration.DEFAULT_LOCALE,"%d", i));
		}
		return sb.toString();
	}


	public String getCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.type.toString());
		sb.append(',');
		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%f", this.status));
		sb.append(',');
		sb.append(this.gender.toString());
		sb.append(',');
		sb.append(String.format(Configuration.DEFAULT_LOCALE, "%f", this.closeness));
		for(int i=0; i<this.phonFeatures.length; i++) {
			sb.append(',');
			sb.append(String.format(Configuration.DEFAULT_LOCALE,"%f", this.phonFeatures[i]));
		}
		return sb.toString();
	}


}
