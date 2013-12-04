package experiments.fivechain;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.statehashing.StateHashFactory;
import metarl.EnvironmentAndTask;
import metarl.ParameterizedRLFactory;


/**
 * A parameterized Q-learning algorithm which takes 3 parameters, the learning rate, the epsilon value (for epsilon greedy exploration) and
 * the constant Q-value initialization to use for all state-action pairs.
 * @author James MacGlashan
 *
 */
public class QL3Param implements ParameterizedRLFactory {

	protected StateHashFactory		hashingFactory;
	
	public QL3Param(StateHashFactory hashingFactory){
		this.hashingFactory = hashingFactory;
	}
	
	@Override
	public int nParams() {
		return 3;
	}

	@Override
	public double[] paramLowerLimits() {
		return new double[]{0., 0., 0.};
	}

	@Override
	public double[] paramUpperLimits() {
		return new double[]{1., 1., 200.};
	}

	@Override
	public LearningAgent generateLearningAgentWithParamsForEnvironment(EnvironmentAndTask env, double[] params) {
		
		EpsilonGreedy policy = new EpsilonGreedy(params[1]);
		QLearning agent = new QLearning(env.domain, env.rf, env.tf, env.discount, this.hashingFactory, params[2], params[0], policy, Integer.MAX_VALUE);
		policy.setPlanner(agent);
		
		return agent;
	}

}
