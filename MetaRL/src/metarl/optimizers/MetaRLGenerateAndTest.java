package metarl.optimizers;

import java.util.ArrayList;
import java.util.List;

import metarl.MetaRLEvaluator;
import metarl.MetaRLOptimizer;
import optimization.OVarStringRep;
import optimization.OptVariables;
import optimization.Optimization;
import optimization.VarEvaluaiton;
import optimization.VarFitnessPair;
import optimization.VariableClamper;
import optimization.bandit.DomainSampler;

public class MetaRLGenerateAndTest implements Optimization, MetaRLOptimizer {

	protected VarEvaluaiton						varEval;
	protected VariableClamper					clamper;
	protected DomainSampler						domainSampler;
	
	protected double []							lowerLims;
	protected double []							upperLims;
	
	protected int 								numRLEvaluationsThusFar = 0;
	protected int								numRLEvaluations;
	
	VarFitnessPair								best;
	
	protected boolean							ignoreTrueRLEvalCost;
	
	public MetaRLGenerateAndTest(VarEvaluaiton varEval, VariableClamper clamper, double [] lowerLims, double [] upperLims){
		this(varEval, clamper, lowerLims, upperLims, false);
	}
	
	public MetaRLGenerateAndTest(VarEvaluaiton varEval, VariableClamper clamper, double [] lowerLims, double [] upperLims, boolean ignoreTrueRLEvalCost){
		this.varEval = varEval;
		this.clamper = clamper;
		this.domainSampler = new DomainSampler.UniformDomainSampler();
		this.lowerLims = lowerLims.clone();
		this.upperLims = upperLims.clone();
		this.ignoreTrueRLEvalCost = ignoreTrueRLEvalCost;
	}
	
	@Override
	public void setRLEvaluationThreshold(int numRLEvaluations) {
		this.numRLEvaluations = numRLEvaluations;
	}

	@Override
	public void startOrContinueRLOptimization() {
		while(this.numRLEvaluationsThusFar + ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate() < this.numRLEvaluations){
			
			VarFitnessPair vf = this.sample();
			this.numRLEvaluationsThusFar += this.evalInc();
			
			if(this.best == null || vf.fitness > best.fitness){
				best = vf;
			}
			
		}

	}
	
	protected VarFitnessPair sample(){
		OptVariables var = this.domainSampler.sampleDomain(this.lowerLims, this.upperLims);
		this.clamper.clamp(var);
		List<OptVariables> listWrapepr = new ArrayList<OptVariables>(1);
		listWrapepr.add(var);
		List <Double> fs = this.varEval.evaluate(listWrapepr);
		
		VarFitnessPair vf = new VarFitnessPair(var, fs.get(0));
		return vf;
	}

	@Override
	public int getTotalNumberOfRLEvaluations() {
		return this.numRLEvaluationsThusFar;
	}

	@Override
	public void optimize() {
		throw new RuntimeException("The MetaRL version of geneate and test should not be invoked by the optimize method. Use the startOrContinueRLOptimization method instead.");
	}

	@Override
	public OptVariables getBest() {
		if(best == null){
			this.best = this.sample();
			this.numRLEvaluationsThusFar += this.evalInc();
		}
		return best.var;
	}

	@Override
	public double getBestFitness() {
		if(best == null){
			this.best = this.sample();
			this.numRLEvaluationsThusFar += this.evalInc();
		}
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
	
	protected int evalInc(){
		if(this.ignoreTrueRLEvalCost){
			return 1;
		}
		else{
			return ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate();
		}
	}

}
