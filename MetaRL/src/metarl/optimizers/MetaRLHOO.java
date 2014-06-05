package metarl.optimizers;

import metarl.MetaRLEvaluator;
import metarl.MetaRLOptimizer;
import optimization.VarEvaluaiton;
import optimization.VariableClamper;
import optimization.bandit.HOO;

public class MetaRLHOO extends HOO implements MetaRLOptimizer {

	protected int numRLEvaluationsThusFar = 0;
	
	public MetaRLHOO(VarEvaluaiton varEval, VariableClamper clamper,
			double[] lowerLims, double[] upperLims, int maxSamples, double v, double roe) {
		super(varEval, clamper, lowerLims, upperLims, maxSamples, v, roe);
		this.useGreedySampleOnReturnBest = true;
		
	}
	
	@Override
	public void optimize(){
		throw new RuntimeException("The MetaRL version of HOO should not be invoked by the optimize method. Use the startOrContinueRLOptimization method instead.");
	}

	@Override
	public void setRLEvaluationThreshold(int numRLEvaluations) {
		this.maxSamples = numRLEvaluations;
	}

	@Override
	public void startOrContinueRLOptimization() {
		while(this.numRLEvaluationsThusFar + ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate() <= this.maxSamples){
			this.root.sample();
			this.numRLEvaluationsThusFar += ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate();
		}

	}

	@Override
	public int getTotalNumberOfRLEvaluations() {
		return numRLEvaluationsThusFar;
	}

}
