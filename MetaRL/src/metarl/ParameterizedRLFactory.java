package metarl;

import burlap.behavior.singleagent.learning.LearningAgent;


/**
 * This interface is used for defining a factory for a reinforcement learning algorithm.
 * @author James MacGlashan
 *
 */
public interface ParameterizedRLFactory {

	/**
	 * Returns the number of parameters for this class of algorithm
	 * @return The number of parameters for this class of algorithm
	 */
	public int nParams();
	
	/**
	 * Returns the lower limit values of each parameter for this algorithm
	 * @return the lower limit values of each parameter for this algorithm
	 */
	public double [] paramLowerLimits();
	
	/**
	 * Returns the upper limit values of each parameter for this algorithm
	 * @return the upper limit values of each parameter for this algorithm
	 */
	public double [] paramUpperLimits();
	
	
	/**
	 * Generates a learning algorithm instance for the given environment and task and the parameters of the algorithm
	 * @param env the environment and task on which the algorithm will be tested
	 * @param params the parameters of the learning algorithm
	 * @return a learning algorithm
	 */
	public LearningAgent generateLearningAgentWithParamsForEnvironment(EnvironmentAndTask env, double [] params);
	
	
}
