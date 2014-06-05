package optimization.bandit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.DPrint;

import optimization.OVarStringRep;
import optimization.OptVariables;
import optimization.Optimization;
import optimization.VarEvaluaiton;
import optimization.VarFitnessPair;
import optimization.VariableClamper;

public class HOO implements Optimization {

	protected VarEvaluaiton		varEval;
	protected VariableClamper	clamper;
	protected DomainSampler		domainSampler;
	
	protected double []			lowerLims;
	protected double []			upperLims;
	
	protected int				maxSamples;
	
	
	
	protected int				nSamples = 0;
	protected HOONode			root;
	protected Random			rand;
	
	protected double			v ;
	protected double			roe;
	
	protected boolean 			useGreedySampleOnReturnBest = false;
	
	protected VarFitnessPair	best = null;
	
	
	public HOO(VarEvaluaiton varEval, VariableClamper clamper, double[] lowerLims, double[] upperLims, int maxSamples, double v, double roe){
		
		this.varEval = varEval;
		this.clamper = clamper;
		this.domainSampler = new DomainSampler.UniformDomainSampler();
		
		this.lowerLims = lowerLims;
		this.upperLims = upperLims;
		this.maxSamples = maxSamples;
		
		this.root = new HOONode(lowerLims.clone(), upperLims.clone(), 0);
		
		this.v = v;
		this.roe = roe;
		
	}
	
	
	
	
	
	public void setUseGreedySampleOnReturnBest(boolean useGreedySampleOnReturnBest){
		this.useGreedySampleOnReturnBest = useGreedySampleOnReturnBest;
	}
	
	
	@Override
	public void optimize() {
		
		for(int i = 0; i < this.maxSamples; i++){
			VarFitnessPair vf = this.root.sample();
			if(this.best == null){
				this.best = vf;
			}
			else if(vf.fitness > this.best.fitness){
				this.best = vf;
			}
			DPrint.cl(74634, "" + (i+1) + " " + vf.fitness + "\t" + this.best.fitness);
		}

	}

	@Override
	public OptVariables getBest() {
		if(!this.useGreedySampleOnReturnBest){
			return this.best.var;
		}
		else{
			return this.root.sampleBest();
		}
	}

	@Override
	public double getBestFitness() {
		return this.best.fitness;
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
	
	
	public class HOONode{
		
		protected double []			lowerLims;
		protected double []			upperLims;
		
		protected int				n = 0;
		protected double			sumReturn = 0.;
		protected double			bValue = Double.POSITIVE_INFINITY;
		protected int				h;
		
		protected HOONode			child1 = null;
		protected HOONode			child2 = null;
		
		
		
		public HOONode(double[] lowerLims, double[] upperLims, int height){
			
			this.lowerLims = lowerLims;
			this.upperLims = upperLims;
			this.h = height;
			
		}
		
		
		public VarFitnessPair sample(){
			
			VarFitnessPair vf = this.sampleHelper();
			this.updateBValue();
			
			return vf;

		}
		
		public OptVariables sampleBest(){
			VarFitnessPair vf = this.sampleHelper();
			this.updateBValue();
			
			return vf.var;
		}
		
		protected VarFitnessPair sampleHelper(){
			
			if(this.isLeaf()){
				
				OptVariables var = HOO.this.domainSampler.sampleDomain(this.lowerLims, this.upperLims);
				HOO.this.clamper.clamp(var);
				List<OptVariables> listWrapepr = new ArrayList<OptVariables>(1);
				listWrapepr.add(var);
				List <Double> fs = HOO.this.varEval.evaluate(listWrapepr);
				
				VarFitnessPair vf = new VarFitnessPair(var, fs.get(0));
				
				//add to this node
				this.n++;
				this.sumReturn += vf.fitness;
				
				//update the number of total samples
				HOO.this.nSamples++;
				
				this.subDivide();
				
				
				return vf;
				
				
			}
			else{
				
				HOONode selected = this.child1;
				if(this.child2.bValue > this.child1.bValue){
					selected = this.child2;
				}
				
				
				
				VarFitnessPair vf = selected.sample();
				
				//add to this node
				this.n++;
				this.sumReturn += vf.fitness;
				
				return vf;
				
			}
			
		}
		
		
		protected OptVariables sampleBestHelper(){
			if(this.isLeaf()){
				OptVariables var = HOO.this.domainSampler.sampleDomain(this.lowerLims, this.upperLims);
				HOO.this.clamper.clamp(var);
				return var;
			}
			else{
				
				HOONode selected = this.child1;
				if(this.child2.getAverageReturn() > this.child1.getAverageReturn()){
					selected = this.child2;
				}
				
				return selected.sampleBestHelper();
			}
			
		}
		
		public boolean isLeaf(){
			return this.child1 == null;
		}
		
		public double getAverageReturn(){
			if(this.n == 0){
				return Double.POSITIVE_INFINITY;
			}
			return this.sumReturn / this.n;
		}
		
		
		public double confidenceTerm(){
			if(this.n == 0){
				return Double.POSITIVE_INFINITY;
			}
			double inner = 2 * Math.log(HOO.this.nSamples) / this.n;
			double conf = Math.sqrt(inner) + (HOO.this.v * Math.pow(HOO.this.roe, this.h));
			return conf;
		}
		
		public double upperConfidence(){
			return this.getAverageReturn() + this.confidenceTerm();
		}
		
		protected double updateBValue(){
			
			if(this.isLeaf()){
				this.bValue = this.upperConfidence();
			}
			else{
				this.child1.updateBValue();
				this.child2.updateBValue();
				double maxChildB = Math.max(this.child1.bValue, this.child2.bValue);
				this.bValue = Math.min(this.upperConfidence(), maxChildB);
			}
			
			return this.bValue;
		}
		
		protected void subDivide(){
			
			int sd = this.divisionDimension();
			double mid = (this.upperLims[sd] + this.lowerLims[sd]) / 2.;
			
			double [] c1LowerLims = this.lowerLims.clone();
			double [] c1UpperLims = this.upperLims.clone();
			c1UpperLims[sd] = mid;
			
			
			double [] c2LowerLims = this.lowerLims.clone();
			double [] c2UpperLims = this.upperLims.clone();
			c2LowerLims[sd] = mid;
			
			this.child1 = new HOONode(c1LowerLims, c1UpperLims, this.h + 1);
			this.child2 = new HOONode(c2LowerLims, c2UpperLims, this.h + 1);
			
		}
		
		protected int divisionDimension(){
			
			double maxRange = Double.NEGATIVE_INFINITY;
			int maxDim = -1;
			for(int i = 0; i < this.lowerLims.length; i++){
				double r = this.upperLims[i] - this.lowerLims[i];
				if(r > maxRange){
					maxRange = r;
					maxDim = i;
				}
			}
			
			return maxDim;
			
		}
		
		
	}

}
