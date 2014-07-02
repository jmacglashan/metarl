package metarl.evaluators;

import java.util.List;

import optimization.OptVariables;
import metarl.EnvironmentAndTask;
import metarl.EnvironmentsSampleMetaRLEvaluator;
import metarl.ParameterizedRLFactory;

public class SampledEnvironmentsMultipleAveragedRunReturn extends
		EnvironmentsSampleMetaRLEvaluator {

	protected int numRunsPerEnvironment = 1;
	
	protected boolean shouldNormalize = false;
	protected double lowerVal;
	protected double upperVal;
	
	
	/**
	 * Initializes with the RLFactory defining the RL algorithm family being evaluated;
	 * the number of learning steps that each algorithm will be given; and the set of sample environments that will be tested.
	 * Each RL algorithm will be tested on each envionrmnet only once.
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 */
	public SampledEnvironmentsMultipleAveragedRunReturn(
			ParameterizedRLFactory rlFactory, int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
	}
	
	
	
	
	/**
	 * Initializes with the RLFactory defining the RL algorithm family being evaluated;
	 * the number of learning steps that each algorithm will be given the set of sample environments that will be tested;
	 * and the number of times an algorithm is test on each sample envionrment
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 * @param numRunsPerEnvironment the number of times an algorithm is test on each envionrment to produce an average return
	 */
	public SampledEnvironmentsMultipleAveragedRunReturn(
			ParameterizedRLFactory rlFactory, int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments,
			int numRunsPerEnvironment) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		this.numRunsPerEnvironment = numRunsPerEnvironment;
	}
	
	
	/**
	 * Initializes with the RLFactory defining the RL algorithm family being evaluated;
	 * the number of learning steps that each algorithm will be given the set of sample environments that will be tested;
	 * and the number of times an algorithm is test on each sample envionrment. Performance will be normalized
	 * according the lower val and upper val
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 * @param numRunsPerEnvironment the number of times an algorithm is test on each envionrment to produce an average return
	 * @param lowerVal lower performace value for normalization
	 * @param upperVal upper performance value for normalization
	 */
	public SampledEnvironmentsMultipleAveragedRunReturn(
			ParameterizedRLFactory rlFactory, int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments,
			int numRunsPerEnvironment, double lowerVal, double upperVal) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		this.numRunsPerEnvironment = numRunsPerEnvironment;
		this.shouldNormalize = true;
		this.lowerVal = lowerVal;
		this.upperVal = upperVal;
	}
	
	
	/**
	 * Sets the number of times each RL algorithm is tested on each environment
	 * @param numRunsPerEnvironment the number of times each RL algorithm is tested on each environment
	 */
	public void setNumRunsPerEnvrionemnt(int numRunsPerEnvironment){
		this.numRunsPerEnvironment = numRunsPerEnvironment;
	}

	@Override
	public double getPeformance(OptVariables vars) {
		
		double sumAveragePerformance = 0.;
		for(EnvironmentAndTask env : this.sampleEnviroments){
			double sumEnvPerformance = 0.;
			for(int i = 0; i < this.numRunsPerEnvironment; i++){
				sumEnvPerformance += this.getCumulativeReturn(vars, env);
			}
			double avgEnvPerformance = sumEnvPerformance / (double)this.numRunsPerEnvironment;
			sumAveragePerformance += avgEnvPerformance;
		}
		
		double performance = sumAveragePerformance / (double)this.sampleEnviroments.size();
		
		if(this.shouldNormalize){
			performance = (performance - this.lowerVal) / (this.upperVal  - this.lowerVal);
		}
		
		return performance;
	}

	@Override
	public int numRLRunsPerEvaluate() {
		int n = this.sampleEnviroments.size()*this.numRunsPerEnvironment;
		return n;
	}

}
