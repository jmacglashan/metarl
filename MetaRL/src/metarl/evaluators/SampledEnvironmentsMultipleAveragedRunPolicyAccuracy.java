package metarl.evaluators;

import java.util.Collection;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;

import optimization.OptVariables;
import metarl.EnvironmentAndTask;
import metarl.EnvironmentsSampleMetaRLEvaluator;
import metarl.ParameterizedRLFactory;

public class SampledEnvironmentsMultipleAveragedRunPolicyAccuracy extends
		EnvironmentsSampleMetaRLEvaluator {

	
	protected int numRunsPerEnvironment = 1;
	
	
	/**
	 * Initializes using 1 run per environment.
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 * @param solvedPoliciesForEnvrionemnts the optimal policy for each environment and task that this object can evaluate.
	 * @param stateSpaces the state space for each environment asn task that this object can evaluate.
	 */
	public SampledEnvironmentsMultipleAveragedRunPolicyAccuracy(
			ParameterizedRLFactory rlFactory, int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments,
			List<Policy> solvedPoliciesForEnvrionemnts,
			List<Collection<State>> stateSpaces) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		this.usePolicyAccuracyForEvaluation(solvedPoliciesForEnvrionemnts, stateSpaces);
	}
	
	
	/**
	 * Initializes.
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 * @param solvedPoliciesForEnvrionemnts the optimal policy for each environment and task that this object can evaluate.
	 * @param stateSpaces the state space for each environment asn task that this object can evaluate.
	 * @param numRunsPerEnvironment the number of times an algorithm is test on each envionrment to produce an average policy accuracy
	 */
	public SampledEnvironmentsMultipleAveragedRunPolicyAccuracy(
			ParameterizedRLFactory rlFactory, int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments,
			List<Policy> solvedPoliciesForEnvrionemnts,
			List<Collection<State>> stateSpaces,
			int numRunsPerEnvironment) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		this.usePolicyAccuracyForEvaluation(solvedPoliciesForEnvrionemnts, stateSpaces);
		this.numRunsPerEnvironment = numRunsPerEnvironment;
	}

	@Override
	public double getPeformance(OptVariables vars) {
		double sumAveragePerformance = 0.;
		for(EnvironmentAndTask env : this.sampleEnviroments){
			double sumEnvPerformance = 0.;
			for(int i = 0; i < this.numRunsPerEnvironment; i++){
				sumEnvPerformance += this.getPolicyAccuracy(vars, env, this.solvedPolicies.get(i), this.stateSpaces.get(i));
			}
			double avgEnvPerformance = sumEnvPerformance / (double)this.numRunsPerEnvironment;
			sumAveragePerformance += avgEnvPerformance;
		}
		
		double performance = sumAveragePerformance / (double)this.sampleEnviroments.size();
		
		return performance;
	}

	@Override
	public int numRLRunsPerEvaluate() {
		int n = this.sampleEnviroments.size()*this.numRunsPerEnvironment;
		return n;
	}

}
