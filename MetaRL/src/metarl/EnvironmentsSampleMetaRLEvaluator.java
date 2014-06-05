package metarl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.oomdp.core.State;

import optimization.OptVariables;


//TODO: Note that there are two forms of policy evaluations: isOptimal percent in RL runs and average policy accuracy...

public abstract class EnvironmentsSampleMetaRLEvaluator extends MetaRLEvaluator {



	/**
	 * The list of evnironments and tasks on which parameterized RL algorithms will be tested
	 */
	protected List<EnvironmentAndTask>			sampleEnviroments;
	
	
	
	//objects for policy accuracy evaluation
	
	/**
	 * The solved optimal policies for each of the environment and tasks provided to this object.
	 */
	protected List<Policy>						solvedPolicies;
	
	/**
	 * The state spaces for each of the environments and tasks on which the learned policy and optimal policy will be evaluated
	 */
	protected List<Collection<State>>			stateSpaces;
	
	/**
	 * The policy used to extract the final policy from a learning algorithm. The default
	 * is GreedyQ policy
	 */
	protected PlannerDerivedPolicy				rlAlgorithmFinalPolicy = new GreedyQPolicy();
	
	
	
	/**
	 * Initializes with the RLFactory defining the RL algorithm family being evaluated;
	 * the number of learning steps that each algorithm will be given; and the set of sample environments that will be tested.
	 * @param rlFactory the rl factory
	 * @param numLearningSteps the number of learning steps on an environment that a learning algorithm is given
	 * @param sampleEnvironments the set of sample environments on which RL algorithms will be evaluated
	 */
	public EnvironmentsSampleMetaRLEvaluator(ParameterizedRLFactory rlFactory,
			int numLearningSteps, List<EnvironmentAndTask> sampleEnvironments) {
		super(rlFactory, numLearningSteps);
		this.sampleEnviroments = sampleEnvironments;
	}
	
	
	
	
	/**
	 * Sets the environments and tasks on which RL algorithms will be evaluated
	 * @param sampleEnvironments the environments and tasks on which RL algorithms will be evaluated
	 */
	public void setSampleEnvironments(List<EnvironmentAndTask> sampleEnvironments){
		this.sampleEnviroments = sampleEnvironments;
	}
	
	/**
	 * This method is used for policy-based evaluators which specifies the solved optimal policies for each environment and the state space
	 * on which it will be evaluated. the ith index of solvedPoliciesForEnvrionemnts and stateSpaces should correspond to the optimal
	 * policy and state space of the ith environment and task of this objects list of sample environments, respectively. 
	 * @param solvedPoliciesForEnvrionemnts the optimal policy for each environment and task that this object can evaluate.
	 * @param stateSpaces the state space for each environment asn task that this object can evaluate.
	 */
	public void usePolicyAccuracyForEvaluation(List<Policy> solvedPoliciesForEnvrionemnts, List<Collection<State>> stateSpaces){
		this.solvedPolicies = solvedPoliciesForEnvrionemnts;
		this.stateSpaces = stateSpaces;
	}
	
	
	/**
	 * When using a policy-based evaluator, use this method to set to the final policy of a learning algorithm that will be used. This method
	 * only needs to be called if the final polic is something other than the {@link GreedyQPolicy} object, which is the default.
	 * @param rlAlgorithmFinalPolicy the final policy of a learning agent to use
	 */
	public void setRLAlgorithmPolicyToUseInPolicyAccuracyEvaluation(PlannerDerivedPolicy rlAlgorithmFinalPolicy){
		this.rlAlgorithmFinalPolicy = rlAlgorithmFinalPolicy;
	}
	
	
	/**
	 * Runs a learning algorithm with parameters vars on environment and task env and returns the accuracy of the final resulting
	 * policy with respect to a provided state space. The accuracy is computed
	 * as the fraction of states in the state space for which one of the optimal actions in the optimalPolicy can be found
	 * in the action distribution for the learned policy.
	 * @param vars the RL algorithm parameters
	 * @param env the environment and task on which to evaluate the RL algorithm
	 * @param optimalPolicy the optimal policy against which the learned policy will be compared
	 * @param stateSpace the state space on which to compare the policies
	 * @return the accuracy of the learned policy
	 */
	protected double getPolicyAccuracy(OptVariables vars, EnvironmentAndTask env, Policy optimalPolicy, Collection<State> stateSpace){
		
		LearningAgent agent = this.rlFactory.generateLearningAgentWithParamsForEnvironment(env, vars.vars);
		int remainingSteps = this.numLearningSteps;
		while(remainingSteps > 0){
			State initialState = env.initialStateGenerator.generateState();
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState, remainingSteps);
			remainingSteps -= ea.numTimeSteps() - 1; //-1 because we want the number of actions taken; do not count terminal state as a time step	
		}
		this.rlAlgorithmFinalPolicy.setPlanner((OOMDPPlanner)agent);
		double accuracy = policyAccuracy((Policy)this.rlAlgorithmFinalPolicy, optimalPolicy, stateSpace);
		
		return accuracy;
	}
	
	
	/**
	 * Returns the accuracy of a policy in comparison to a provided optimal policy for a given state space. The accuracy is computed
	 * as the fraction of states in the state space for which one of the optimal actions in the optimalPolicy can be found
	 * in the action distribution for the input policy. It is assumed that that the only stochasticity in both the input policy
	 * and optimal policy involve tie breaks between what are considered equally "good" actions.
	 * @param policy an input policy whose accuracy with respect to an optimal policy is to be checked
	 * @param optimalPolicy the optimal policy
	 * @param stateSpace the state space on which to evalute the policies
	 * @return the accuracy of the input policy
	 */
	protected static double policyAccuracy(Policy policy, Policy optimalPolicy, Collection<State> stateSpace){
		
		double sumMatch = 0.;
		for(State s : stateSpace){
			
			List<ActionProb> aDist = remove0Probs(policy.getActionDistributionForState(s));
			List<ActionProb> oDist = remove0Probs(optimalPolicy.getActionDistributionForState(s));
			
			boolean found = false;
			for(ActionProb oap : oDist){
				for(ActionProb ap : aDist){
					if(oap.ga.equals(ap.ga)){
						found = true;
						break;
					}
				}
			}
			
			if(found){
				sumMatch += 1.;
			}
			
		}
		
		double accuracy = sumMatch / (double)stateSpace.size();
		
		return accuracy;
	}
	
	
	/**
	 * This method removes from a list of action probabilities any actions that are assigned a probability of 0.
	 * @param input an input action distribution format as a list of {@link ActionProb} objects.
	 * @return a new list of {@Link ActionProb} objects which contains only those in the input with non-zero probability
	 */
	protected static List <ActionProb> remove0Probs(List <ActionProb> input){
		List <ActionProb> res = new ArrayList<Policy.ActionProb>(input.size());
		for(ActionProb ap : input){
			if(ap.pSelection > 0.){
				res.add(ap);
			}
		}
		return res;
	}

}
