package metarl;

public interface MetaRLOptimizer {
	
	/**
	 * Sets a threshold for the number of RLEvaluations. This number of RL evaluations will not be exceeded by the {@link #startOrContinueRLOptimization()}
	 * method, however it may cause the method to perform less than that number if an additional iteration of the algorithm would cause it to use more
	 * then the specified thershold.
	 * @param numRLEvaluations the maximum number of RL evaluations that can be performed
	 */
	public void setRLEvaluationThreshold(int numRLEvaluations);
	
	/**
	 * Either starts optimizaiton, or continues it from where it last left off if the RLEvaluaitonThreshold has been increased since this method's last call.
	 * The number of RL evaluations performed by this method will not exceed the number set to it by the {@link #setRLEvaluationThreshold(int)}
	 */
	public void startOrContinueRLOptimization();
	
	/**
	 * Returns the total number of RL evaluations that have been performed.
	 * @return the total number of RL evaluations that have been performed.
	 */
	public int getTotalNumberOfRLEvaluations();
}
