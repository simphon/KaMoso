package sfb732.kamoso.opt;

public interface Solution {

	public void compute();

	public double cost();

	public Solution createNeighbor(double noiseFactor);

	public Solution createRandom();

}
