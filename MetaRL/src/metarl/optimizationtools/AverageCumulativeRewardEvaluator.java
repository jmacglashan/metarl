package metarl.optimizationtools;

import java.util.ArrayList;
import java.util.List;

import metarl.EnvironmentAndTask;
import metarl.ParameterizedRLFactory;
import optimization.OptVariables;
import optimization.VarEvaluaiton;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.State;


/**
 * Defines an evaluator class that takes a list of evaluative environments and evaluates an parameterized learning algorithms as its average performance
 * on each of those environments. Performance on an environment is measured as cumulative reward for some given number of steps and the cumulative reward 
 * is averaged over n runs of the learning algorithm on that environment.
 * @author James MacGlashan
 *
 */
public class AverageCumulativeRewardEvaluator implements VarEvaluaiton {

	protected List <EnvironmentAndTask>	evaluativeEnvironments;
	protected ParameterizedRLFactory	rlFactory;
	protected int						nTries;
	protected int						maxLearningSteps;
	
	
	
	/**
	 * Initializes.
	 * @param evaluativeEnvironments the list of environments on which a parameterized learning algorithm should be evaluated.
	 * @param rlFactory the reinforcement learning algorithm factor that will generate a learning algorithm for a given set of parameters and on a given environment
	 * @param nTries the number of times over which a learning algorithm's cumulative reward for an environment will be averaged. 
	 * @param maxLearningSteps the number of time steps a learning algorithm has in the environment
	 */
	public AverageCumulativeRewardEvaluator(List <EnvironmentAndTask> evaluativeEnvironments, ParameterizedRLFactory rlFactory, int nTries, int maxLearningSteps){
		this.evaluativeEnvironments = evaluativeEnvironments;
		this.rlFactory = rlFactory;
		this.nTries = nTries;
		this.maxLearningSteps = maxLearningSteps;
	}
	
	
	@Override
	public List<Double> evaluate(List<OptVariables> instances) {
		
		List <Double> result = new ArrayList<Double>(instances.size());
		for(OptVariables vars : instances){
			double eval = this.evalInstance(vars);
			result.add(eval);
		}
		
		return result;
	}
	
	/**
	 * Computes and returns the average performance over each environment
	 * @param vars the parameterization for the learning algorithm
	 * @return the average performance (average cumulative reward on an environment) over each environment
	 */
	public double evalInstance(OptVariables vars){
		
		double sum = 0.;
		for(EnvironmentAndTask env : this.evaluativeEnvironments){
			sum += this.evalInstanceOnEnv(env, vars);
		}
		
		return sum / this.evaluativeEnvironments.size();
		
	}
	
	/**
	 * Returns the average cumulative reward of a parameterized algorithm on the given environment
	 * @param env the environment on which to test the parameterized algorithm
	 * @param vars the parameterization of the algorithm
	 * @return average cumulative reward of a parameterized algorithm on the given environment
	 */
	public double evalInstanceOnEnv(EnvironmentAndTask env, OptVariables vars){
		
		double sumCummulativeReturnOverNTries = 0.;
		
		for(int i = 0; i < this.nTries; i++){
			
			
			LearningAgent agent = this.rlFactory.generateLearningAgentWithParamsForEnvironment(env, vars.vars);
			int remainingSteps = this.maxLearningSteps;
			double cumulativeReturn = 0.;
			while(remainingSteps > 0){
				State initialState = env.initialStateGenerator.generateState();
				EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState, remainingSteps);
				cumulativeReturn += ea.getDiscountedReturn(1.); //for experimental cumulative return do not discount here
				remainingSteps -= ea.numTimeSteps() - 1; //-1 because we want the number of actions taken; do not count terminal state as a time step
			}
			
			
			sumCummulativeReturnOverNTries += cumulativeReturn;
		}
		
		
		return sumCummulativeReturnOverNTries / this.nTries;
	}

}
