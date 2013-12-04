package experiments.fivechain;

import java.util.HashSet;
import java.util.Set;

import metarl.EnvironmentAndTask;
import metarl.ParameterizedEnvironmentFactory;
import burlap.domain.singleagent.graphdefined.GraphDefinedDomain;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class will generate five chain environments and tasks. The first parameter represents the slip probability. The next 5 parameters represent
 * the order of the states in the chain.
 * @author James MacGlashan
 *
 */
public class FiveChainGen implements ParameterizedEnvironmentFactory{

	@Override
	public int nParams() {
		return 6;
	}

	@Override
	public double[] paramLowerLimits() {
		return new double[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public double[] paramUpperLimits() {
		return new double[]{0.5, 4, 4, 4, 4, 4};
	}

	@Override
	public EnvironmentAndTask generateEnvironment(double[] params) {
		
		//repackage our parameters and check for validity
		double slipProb = params[0];
		int [] stateOrder = new int[5];
		
		Set<Integer> used = new HashSet<Integer>(5);
		for(int i = 1; i < params.length; i++){
			int sid = (int)params[i];
			if(used.contains(sid)){
				throw new RuntimeException("Error: state order for 5 chain problem is invalid because of a duplicate state in the order");
			}
			stateOrder[i-1] = sid;
			used.add(sid);
		}
		
		
		//create our graph domain instance
		GraphDefinedDomain gdd = new GraphDefinedDomain(stateOrder.length);
		int sNodeId = stateOrder[0];
		int endNode = stateOrder[stateOrder.length-1];
		
		
		//handle transitions for all but end node
		for(int i = 0; i < stateOrder.length-1; i++){
			int sId = stateOrder[i];
			int nId = stateOrder[i+1];
			gdd.setTransition(sId, 0, nId, 1.-slipProb);
			gdd.setTransition(sId, 0, sNodeId, slipProb);
			
			gdd.setTransition(sId, 1, sNodeId, 1.-slipProb);
			gdd.setTransition(sId, 1, nId, slipProb);
			
		}
		
		//handle end node transition
		gdd.setTransition(endNode, 0, endNode, 1.-slipProb);
		gdd.setTransition(endNode, 0, sNodeId, slipProb);
		
		gdd.setTransition(endNode, 1, sNodeId, 1.-slipProb);
		gdd.setTransition(endNode, 1, endNode, slipProb);
		
		Domain domain = gdd.generateDomain();
		
		//handle task specific information
		RewardFunction rf = new ChainRF(sNodeId, endNode);
		TerminalFunction tf = new NullTermination();
		double discount = 0.95;
		State initialState = GraphDefinedDomain.getState(domain, sNodeId);
		StateGenerator initialStateGenerator = new ConstantStateGenerator(initialState);
		
		EnvironmentAndTask et = new EnvironmentAndTask(domain, rf, tf, discount, initialStateGenerator);
		
		
		return et;
	}
	
	
	/**
	 * A reward function for the 5 chain problem. If the agent transitions to the start node, they get reward of 2. If the agent acting in
	 * the last node and transitioned to the last node, they get a reward of 10.
	 * 
	 * @author James MacGlashan
	 *
	 */
	static class ChainRF implements RewardFunction{

		int endNode;
		int startNode;
		
		public ChainRF(int startNode, int endNode){
			this.startNode = startNode;
			this.endNode = endNode;
		}
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			
			int primeNodeId = GraphDefinedDomain.getNodeId(sprime);
			if(primeNodeId == this.startNode){
				return 2.;
			}
			else{
				int cNodeId = GraphDefinedDomain.getNodeId(s);
				if(cNodeId == endNode && primeNodeId == endNode){
					return 10.;
				}
			}
			
			return 0.;
		}
		
		
		
	}

}
