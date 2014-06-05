package metarl.optimizers;

import java.util.ArrayList;
import java.util.List;

import metarl.MetaRLEvaluator;
import metarl.MetaRLOptimizer;
import optimization.OptVariables;
import optimization.VarEvaluaiton;
import optimization.VarFitnessPair;
import optimization.VariableClamper;
import optimization.VariableRandomGenerator;
import optimization.crossentropy.CrossEntropy;
import optimization.crossentropy.MultiVariateNormalGenerator;

public class MetaRLCrossEntropy extends CrossEntropy implements MetaRLOptimizer {

	protected int numRLEvaluationsThusFar = 0;
	
	public MetaRLCrossEntropy(VarEvaluaiton evaluator,
			VariableRandomGenerator initialGen, VariableClamper clamp, int dim,
			int sampleSize, int eliteSize, int maxEvaluations, double maxVar) {
		super(evaluator, initialGen, clamp, dim, sampleSize, eliteSize, maxEvaluations,
				maxVar);
		
	}
	
	
	@Override
	public void optimize(){
		throw new RuntimeException("The MetaRL version of Cross Entropy should not be invoked by the optimize method. Use the startOrContinueRLOptimization method instead.");
	}

	@Override
	public void setRLEvaluationThreshold(int numRLEvaluations) {
		this.maxIterations = numRLEvaluations;
	}

	@Override
	public void startOrContinueRLOptimization() {
		
		while(this.numRLEvaluationsThusFar + this.numRLEvalsForCEIteration() <= this.maxIterations){
			
			List<OptVariables> sampleSet = this.getSample();
			this.population = VarFitnessPair.getPairList(sampleSet, this.evaluator.evaluate(sampleSet));
			List <VarFitnessPair> elite = this.getElite(this.population);
			VarFitnessPair bpair = elite.get(elite.size()-1);
			
			if(bpair.fitness > this.bestFitness || !this.maintainAbsoluteBest){
				this.best = bpair.var;
				this.bestFitness = bpair.fitness;
			}
			
			MeanCovariance mc = new MeanCovariance(elite);
			this.distributionGenerator = new MultiVariateNormalGenerator(mc.means, mc.covariance);
			
			
			this.numRLEvaluationsThusFar += this.numRLEvalsForCEIteration();
		}
		
	}

	
	@Override
	public int getTotalNumberOfRLEvaluations() {
		return numRLEvaluationsThusFar;
	}

	
	protected int numRLEvalsForCEIteration(){
		return this.sampleSize * ((MetaRLEvaluator)this.evaluator).numRLRunsPerEvaluate();
	}
	
	protected List<OptVariables> getSample(){
		
		List<OptVariables> sampleSet = new ArrayList<OptVariables>(sampleSize);
		
		if(this.population == null){
			for(int i = 0; i < sampleSize; i++){
				OptVariables s = initialGenerator.getVars(dim);
				varClamp.clamp(s);
				sampleSet.add(s);
			}
		}
		else{
			for(int i = 0; i < sampleSize; i++){
				OptVariables s = distributionGenerator.getVars(dim); //generate from new distribution
				varClamp.clamp(s);
				sampleSet.add(s);
			}
		}
		
		return sampleSet;
		
	}
	
}
