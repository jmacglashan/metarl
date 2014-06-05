package metarl.optimizers;

import java.util.ArrayList;
import java.util.List;

import metarl.MetaRLEvaluator;
import metarl.MetaRLOptimizer;
import optimization.OVarStringRep;
import optimization.OptVariables;
import optimization.Optimization;
import optimization.VarFitnessPair;
import optimization.VariableClamper;
import optimization.VariableRandomGenerator;
import optimization.hillclimbing.NeighborhoodGenerator;

public class MetaRLHillClimbing implements Optimization, MetaRLOptimizer {

	protected VarFitnessPair				currentPoint;
	
	protected VarFitnessPair				best;
	
	protected int							dim;
	
	protected int							numStallsBeforeReset;
	protected int							numConsecutiveStalls = 0;
	
	protected MetaRLEvaluator				evaluator;
	protected VariableRandomGenerator		varGen;
	protected VariableClamper				clamper;
	protected NeighborhoodGenerator			neighborGen;
	
	
	protected int							maxNumRLEvals;
	protected int 							numRLEvaluationsThusFar = 0;
	
	
	public MetaRLHillClimbing(MetaRLEvaluator evaluator, VariableRandomGenerator varGen, VariableClamper clamper, NeighborhoodGenerator neighborGen, int dimensions, int numStallsBeforeReset){
		this.evaluator = evaluator;
		this.varGen = varGen;
		this.clamper = clamper;
		this.neighborGen = neighborGen;
		this.dim = dimensions;
		this.numStallsBeforeReset = numStallsBeforeReset;
	}
	
	
	@Override
	public void setRLEvaluationThreshold(int numRLEvaluations) {
		this.maxNumRLEvals = numRLEvaluations;
	}

	@Override
	public void startOrContinueRLOptimization() {

		if(this.currentPoint == null && this.evaluator.numRLRunsPerEvaluate() <= this.maxNumRLEvals){
			//need to initialize
			OptVariables vars = this.varGen.getVars(this.dim);
			this.clamper.clamp(vars);
			this.currentPoint = this.evaluate(vars);
			this.best = this.currentPoint;
			this.numRLEvaluationsThusFar += this.evaluator.numRLRunsPerEvaluate();
		}
		else if(this.currentPoint != null){
			
			//do we need to restart given where we left off?
			if(this.numConsecutiveStalls >= this.numStallsBeforeReset && this.numRLEvaluationsThusFar + this.evaluator.numRLRunsPerEvaluate() <= this.maxNumRLEvals){
				OptVariables vars = this.varGen.getVars(this.dim);
				this.clamper.clamp(vars);
				this.currentPoint = this.evaluate(vars);
				this.numRLEvaluationsThusFar += this.evaluator.numRLRunsPerEvaluate();
				if(this.currentPoint.fitness > this.best.fitness){
					this.best = this.currentPoint;
				}
				
				System.out.println("Restart");
				
				this.numConsecutiveStalls = 0;
			}
			
			List<OptVariables> neighbors = this.neighborGen.neighborhood(this.currentPoint.var);
			this.clampPoints(neighbors);
			while(this.numRLEvaluationsThusFar + neighbors.size() * this.evaluator.numRLRunsPerEvaluate() < this.maxNumRLEvals){
				
				//evaluate neighbors
				List<VarFitnessPair> neighborEval = this.evaluate(neighbors);
				this.numRLEvaluationsThusFar += neighbors.size() * this.evaluator.numRLRunsPerEvaluate();
				
				//find the new best
				int selected = -1;
				for(int i = 0; i < neighborEval.size(); i++){
					VarFitnessPair point = neighborEval.get(i);
					if(point.fitness > this.currentPoint.fitness){
						this.currentPoint = point;
						selected = i;
					}
				}
				
				//tell neighborhood generator which we selected
				this.neighborGen.selectedNeighbor(selected);
				
				//update best if new point is better
				if(selected != -1){
					this.numConsecutiveStalls = 0;
					if(this.currentPoint.fitness > this.best.fitness){
						this.best = this.currentPoint;
					}
				}
				else{
					this.numConsecutiveStalls++;
				}
				
				
				//do we need a random restart?
				if(this.numConsecutiveStalls >= this.numStallsBeforeReset && this.numRLEvaluationsThusFar + this.evaluator.numRLRunsPerEvaluate() <= this.maxNumRLEvals){
					OptVariables vars = this.varGen.getVars(this.dim);
					this.clamper.clamp(vars);
					this.currentPoint = this.evaluate(vars);
					this.numRLEvaluationsThusFar += this.evaluator.numRLRunsPerEvaluate();
					if(this.currentPoint.fitness > this.best.fitness){
						this.best = this.currentPoint;
					}
					
					System.out.println("Restart");
					
					this.numConsecutiveStalls = 0;
				}
				
				
				//update neighbors to evaluate next
				neighbors = this.neighborGen.neighborhood(this.currentPoint.var);
				this.clampPoints(neighbors);

				
			}
			
		}
		
	}

	@Override
	public int getTotalNumberOfRLEvaluations() {
		return this.numRLEvaluationsThusFar;
	}

	@Override
	public void optimize() {
		throw new RuntimeException("The MetaRL version of Cross Entropy should not be invoked by the optimize method. Use the startOrContinueRLOptimization method instead.");
	}

	@Override
	public OptVariables getBest() {
		return best.var;
	}

	@Override
	public double getBestFitness() {
		return best.fitness;
	}

	@Override
	public void enableOptimzationFileRecording(int recordMode,
			OVarStringRep rep, String outputPathDirectory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableOptimizationFileRecording() {
		// TODO Auto-generated method stub

	}
	
	protected List<VarFitnessPair> evaluate(List<OptVariables> vars){
		List<Double> fitness = this.evaluator.evaluate(vars);
		List<VarFitnessPair> result = new ArrayList<VarFitnessPair>(vars.size());
		for(int i = 0; i < vars.size(); i++){
			result.add(new VarFitnessPair(vars.get(i), fitness.get(i)));
		}
		return result;
	}
	
	protected VarFitnessPair evaluate(OptVariables vars){
		List<OptVariables> l = new ArrayList<OptVariables>(1);
		l.add(vars);
		List<Double> fitness = this.evaluator.evaluate(l);
		return new VarFitnessPair(vars, fitness.get(0));
	}
	
	protected void clampPoints(List <OptVariables> points){
		for(OptVariables p : points){
			clamper.clamp(p);
		}
	}

}
