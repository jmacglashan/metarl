package experiments.initialexpcode;

import metarl.EnvironmentAndTask;
import burlap.domain.singleagent.graphdefined.GraphDefinedDomain;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class ChainGenerator {

	public int [] stateOrder;
	public double slipProb;
	public boolean class5 = false;
	
	
	public ChainGenerator(int [] stateOrder, double slipProb){
		this.stateOrder = stateOrder.clone();
		this.slipProb = slipProb;
	}
	
	public ChainGenerator(int [] stateOrder, double slipProb, boolean class5){
		this.stateOrder = stateOrder.clone();
		this.slipProb = slipProb;
		this.class5 = class5;
	}
	
	public EnvironmentAndTask generateChainET(){
		
		GraphDefinedDomain gdd = new GraphDefinedDomain(stateOrder.length);
		
		int sNodeId;
		int endNode;
		
		if(!this.class5){
			
			sNodeId = stateOrder[0];
			endNode = stateOrder[stateOrder.length-1];
			
			
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
			
			
			
			
		}
		else{
			
			sNodeId = 0;
			endNode = stateOrder.length-1;
			
			
			//handle transitions for all but end node
			//states stay in fixed order; actions changed based on 0-1 of index
			for(int i = 0; i < stateOrder.length-1; i++){
				int sId = i;
				int nId = i+1;
				
				if(stateOrder[i] == 0){
					gdd.setTransition(sId, 0, nId, 1.-slipProb);
					gdd.setTransition(sId, 0, sNodeId, slipProb);
					
					gdd.setTransition(sId, 1, sNodeId, 1.-slipProb);
					gdd.setTransition(sId, 1, nId, slipProb);
				}
				else{
					gdd.setTransition(sId, 1, nId, 1.-slipProb);
					gdd.setTransition(sId, 1, sNodeId, slipProb);
					
					gdd.setTransition(sId, 0, sNodeId, 1.-slipProb);
					gdd.setTransition(sId, 0, nId, slipProb);
				}
				
			}
			
			//handle end node transition
			if(stateOrder[endNode] == 0){
				gdd.setTransition(endNode, 0, endNode, 1.-slipProb);
				gdd.setTransition(endNode, 0, sNodeId, slipProb);
				
				gdd.setTransition(endNode, 1, sNodeId, 1.-slipProb);
				gdd.setTransition(endNode, 1, endNode, slipProb);
			}
			else{
				gdd.setTransition(endNode, 1, endNode, 1.-slipProb);
				gdd.setTransition(endNode, 1, sNodeId, slipProb);
				
				gdd.setTransition(endNode, 0, sNodeId, 1.-slipProb);
				gdd.setTransition(endNode, 0, endNode, slipProb);
			}
			
			
		}
		
		
		Domain domain = gdd.generateDomain();
		RewardFunction rf = new ChainRF(sNodeId, endNode);
		TerminalFunction tf = new NullTermination();
		double discount = 0.95;
		State initialState = GraphDefinedDomain.getState(domain, sNodeId);
		StateGenerator initialStateGenerator = new ConstantStateGenerator(initialState);
		
		EnvironmentAndTask et = new EnvironmentAndTask(domain, rf, tf, discount, initialStateGenerator);
		
		
		return et;
		
		
	}
	
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer(50);
		for(int i : this.stateOrder){
			buf.append(i + " ");
		}
		buf.append(slipProb);
		return buf.toString();
	}
	
	
	class ChainRF implements RewardFunction{

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
