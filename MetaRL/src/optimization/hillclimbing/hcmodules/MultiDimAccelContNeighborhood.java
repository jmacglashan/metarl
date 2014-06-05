package optimization.hillclimbing.hcmodules;

import java.util.ArrayList;
import java.util.List;

import optimization.OptVariables;
import optimization.hillclimbing.NeighborhoodGenerator;

public class MultiDimAccelContNeighborhood implements NeighborhoodGenerator {

	protected double[]			baseVelocity;
	protected double[]			maxVelocity;
	protected double[]			acceleration;
	
	protected double			curVelocity;
	protected int				lastVariable;
	
	
	public MultiDimAccelContNeighborhood(double [] bv, double [] mxv, double [] a){
		this.baseVelocity = bv;
		this.maxVelocity = mxv;
		this.acceleration = a;
		
		this.lastVariable = -1;
	}
	
	
	@Override
	public List<OptVariables> neighborhood(OptVariables startPoint) {
		
		List <OptVariables> neighbors = new ArrayList<OptVariables>(startPoint.size());
		
		for(int i = 0; i < startPoint.size(); i++){
			OptVariables neighborPos = new OptVariables(startPoint.vars);
			OptVariables neighborNeg = new OptVariables(startPoint.vars);
			if(i != lastVariable){
				neighborPos.vars[i] += baseVelocity[i];
				neighborNeg.vars[i] -= baseVelocity[i];
			}
			else{
				if(curVelocity > 0){
					neighborPos.vars[i] += curVelocity;
					neighborNeg.vars[i] -= baseVelocity[i];
				}
				else{
					neighborPos.vars[i] += baseVelocity[i];
					neighborNeg.vars[i] += curVelocity; //use += because current velocity is signed
				}
			}
			
			neighbors.add(neighborPos);
			neighbors.add(neighborNeg);
		}
		
		
		return neighbors;
	}

	@Override
	public void selectedNeighbor(int i) {
		
		int varIndex = i/2;
		
		if(i/2 == lastVariable && ((i % 2 == 0 && curVelocity > 0) || (i % 2 == 1 && curVelocity < 0))){
			if(i % 2 == 0){
				curVelocity += acceleration[varIndex];
			}
			else{
				curVelocity -= acceleration[varIndex];
			}
		}
		else if(i != -1){
			if(i % 2 == 0){
				curVelocity = baseVelocity[varIndex] + acceleration[varIndex];
			}
			else{
				curVelocity = -(baseVelocity[varIndex] + acceleration[varIndex]);
			}
			
			lastVariable = i/2;
		}
		else{
			lastVariable = -1;
		}
		
		
		//clamp velocity
		if(i != -1){
			if(curVelocity > maxVelocity[varIndex]){
				curVelocity = maxVelocity[varIndex];
			}
			
			if(curVelocity < -maxVelocity[varIndex]){
				curVelocity = -maxVelocity[varIndex];
			}
		}

	}

	@Override
	public NeighborhoodGenerator copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
