package metarl;

import java.util.ArrayList;
import java.util.List;

import optimization.OptVariables;
import optimization.VarEvaluaiton;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.State;




public abstract class MetaRLEvaluator implements VarEvaluaiton {

	//general necessary data members for evaluation
	
	
	
	/**
	 * The parameterized RL algorithm factory that can take a set of input parameters and generate a learning algorithm
	 */
	protected ParameterizedRLFactory			rlFactory;
	
	/**
	 * The number of learning steps on which an RL algorithm will be run
	 */
	protected int								numLearningSteps;
	

	
	/**
	 * Initializes with the RLFactory defining the RL algorithm family being evaluated and
	 * the number of learning steps that each algorithm will be given
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 */
	public MetaRLEvaluator(ParameterizedRLFactory rlFactory, int numLearningSteps){
		this.rlFactory = rlFactory;
		this.numLearningSteps = numLearningSteps;
	}
	
	
	
	
	/**
	 * Sets the parameterized RL algorithm factory that defines the RL algorithm family being tested
	 * @param rlFactory the parameterized RL algorithm factory that defines the RL algorithm family being tested
	 */
	public void setParameterizedRLFactory(ParameterizedRLFactory rlFactory){
		this.rlFactory = rlFactory;
	}
	
	
	/**
	 * The number of learning steps for which an RL algorithm will be provided
	 * @param numLearningSteps number of learning steps for which an RL algorithm will be provided
	 */
	public void setNumLearningSteps(int numLearningSteps){
		this.numLearningSteps = numLearningSteps;
	}
	
	
	
	
	@Override
	public List<Double> evaluate(List<OptVariables> instances) {
		List<Double> performance = new ArrayList<Double>(instances.size());
		for(OptVariables v : instances){
			performance.add(this.getPeformance(v));
		}
		return performance;
	}
	
	
	/**
	 * Returns a performance evaluation for a single RL algorithm parameterization
	 * @param vars the RL parameters to evaluate
	 * @return a performance evaluation for the RL algortihm of this object's RL algorithm family/factory using the given parameterization
	 */
	public abstract double getPeformance(OptVariables vars);
	
	
	/**
	 * Returns the number of times an RL algorithm of a single parameterization is run from scratch on an environment for each call to the {@link #evaluate(List)} method.
	 * Note that the {@link #evaluate(List)} method may request the evaluation of multiple parameters in a batch. However, this method
	 * should return the number of RL algorithms runs that would be performed on a single variable/paramertization; not the the sum
	 * of RL runs over all variables that are requested to be evaluated.
	 * @return the number of times an RL algorithm is run from scratch on an environment for each call to the {@link #evaluate(List)} method.
	 */
	public abstract int numRLRunsPerEvaluate();
	
	
	
	
	/**
	 * Runs a learning algorithm with parameters vars on environment and task env and returns the the cumulative reward received
	 * during learning.
	 * @param vars the RL algorithm parameters
	 * @param env the {@link EnvironmentAndTask} on which to test the RL algorithm
	 * @return the cumulative reward
	 */
	protected double getCumulativeReturn(OptVariables vars, EnvironmentAndTask env){
		
		LearningAgent agent = this.rlFactory.generateLearningAgentWithParamsForEnvironment(env, vars.vars);
		return getCumulativeReturn(agent, env, this.numLearningSteps);
		
	}
	
	
	/**
	 * Runs a provided learning algorithm object on environment and task env for the specified number of learning steps and returns the cumulative reward
	 * received.
	 * @param agent the learning algorithm agent to run
	 * @param env the environment and task on which the agent will be run
	 * @param numLearningSteps the number of learning steps allowed overwhich the cumulative return will be returned
	 * @return the cumulative return over the number of learning steps
	 */
	protected static double getCumulativeReturn(LearningAgent agent, EnvironmentAndTask env, int numLearningSteps){
		int remainingSteps = numLearningSteps;
		double cumulativeReturn = 0.;
		while(remainingSteps > 0){
			State initialState = env.initialStateGenerator.generateState();
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState, remainingSteps);
			cumulativeReturn += ea.getDiscountedReturn(1.); //for experimental cumulative return do not discount here
			remainingSteps -= ea.numTimeSteps() - 1; //-1 because we want the number of actions taken; do not count terminal state as a time step
		}
		
		
		return cumulativeReturn;
	}
	
	
	
	
	
	
}
