package metarl.optimizers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import metarl.MetaRLEvaluator;
import metarl.MetaRLOptimizer;
import optimization.OVarStringRep;
import optimization.OptVariables;
import optimization.Optimization;
import optimization.VarEvaluaiton;
import optimization.VarFitnessPair;
import optimization.VariableClamper;
import optimization.bandit.DomainSampler;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;

public class MetaRLStoSOO implements MetaRLOptimizer, Optimization {

	protected VarEvaluaiton						varEval;
	protected VariableClamper					clamper;
	protected DomainSampler						domainSampler;
	
	protected double []							lowerLims;
	protected double []							upperLims;
	
	
	protected int 								numRLEvaluationsThusFar = 0;
	
	protected int 								k;
	protected int 								plannedFunctionEvaluations;
	protected int 								maxDepth;
	protected double 							delta;
	
	protected Map<Integer, List<StoSOONode>> 	nodesByDepth = new HashMap<Integer, List<StoSOONode>>();
	protected int 								curMaxTreeDepth = 0;
	
	protected int								numRLEvaluations;
	
	
	public MetaRLStoSOO(VarEvaluaiton varEval, VariableClamper clamper,
			double[] lowerLims, double[] upperLims, int plannedFunctionEvaluations){
		
		this.varEval = varEval;
		this.clamper = clamper;
		this.lowerLims = lowerLims.clone();
		this.upperLims = upperLims.clone();
		this.domainSampler = new DomainSampler.UniformDomainSampler();
		
		this.plannedFunctionEvaluations = plannedFunctionEvaluations;
		this.k = Math.max((int)(this.plannedFunctionEvaluations / Math.pow(Math.log(this.plannedFunctionEvaluations), 3)), 1);
		this.maxDepth = (int)Math.sqrt((double)this.plannedFunctionEvaluations/this.k);
		this.delta = 1. / Math.sqrt(this.plannedFunctionEvaluations);
		
		DPrint.cl(0, "k = " + this.k + "\nmaxDepth = " + this.maxDepth +"\ndelta = " + this.delta);
		
		StoSOONode root = new StoSOONode(lowerLims, upperLims);
		List<StoSOONode> nodes = new LinkedList<MetaRLStoSOO.StoSOONode>();
		nodes.add(root);
		nodesByDepth.put(0, nodes);
		
	}
	
	@Override
	public void setRLEvaluationThreshold(int numRLEvaluations) {
		this.numRLEvaluations = numRLEvaluations;
	}

	@Override
	public void startOrContinueRLOptimization() {
		//int c = 0;
		while(this.numRLEvaluationsThusFar + ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate() < this.numRLEvaluations){
			this.runOptimizationStep();
			//System.out.println(c);
			//c++;
		}

	}

	@Override
	public int getTotalNumberOfRLEvaluations() {
		return this.numRLEvaluationsThusFar;
	}
	
	@Override
	public void optimize() {
		throw new RuntimeException("The MetaRL version of StoSOO should not be invoked by the optimize method. Use the startOrContinueRLOptimization method instead.");
	}

	@Override
	public OptVariables getBest() {
		
		List<StoSOONode> maxDepthNonLeafs = null;
		for(int h = this.curMaxTreeDepth; h >= 0; h--){
			maxDepthNonLeafs = this.nonLeafsAtDepth(h);
			if(maxDepthNonLeafs.size() > 0){
				break;
			}
		}
		
		double maxVal = Double.NEGATIVE_INFINITY;
		StoSOONode maxNode = null;
		for(StoSOONode n : maxDepthNonLeafs){
			double r = n.averageReturn();
			if(r > maxVal || this.nodesByDepth.size() == 1){
				maxVal = r;
				maxNode = n;
			}
		}
		
		VarFitnessPair vf = maxNode.sampleWithoutUpdating();
		
		return vf.var;
	}

	@Override
	public double getBestFitness() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void enableOptimzationFileRecording(int recordMode,
			OVarStringRep rep, String outputPathDirectory) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void disableOptimizationFileRecording() {
		throw new UnsupportedOperationException();
		
	}
	
	protected void runOptimizationStep(){
		
		double bmax = Double.NEGATIVE_INFINITY;
		for(int h = 0; h <= Math.min(this.curMaxTreeDepth, this.maxDepth); h++){
			List<StoSOONode> leafs = this.leafsAtDepth(h);
			if(leafs.size() > 0){
				
				double leafMax = Double.NEGATIVE_INFINITY;
				StoSOONode maxLeaf = null;
				for(StoSOONode l : leafs){
					double b = l.bValue();
					if(b > leafMax){
						leafMax = b;
						maxLeaf = l;
					}
				}
				
				if(leafMax > bmax){
					if(maxLeaf.n < this.k){
						maxLeaf.sampleAndPerculateUp();
						this.numRLEvaluationsThusFar += ((MetaRLEvaluator)this.varEval).numRLRunsPerEvaluate();
					}
					else if(h < this.maxDepth){
						maxLeaf.expand();
						bmax = leafMax;
					}
				}
				
				
			}
		}
		
	}
	
	protected List<StoSOONode> leafsAtDepth(int h){
		List<StoSOONode> nodes = this.nodesByDepth.get(h);
		List<StoSOONode> leafs = new LinkedList<StoSOONode>();
		for(StoSOONode n : nodes){
			if(n.isLeaf()){
				leafs.add(n);
			}
		}
		
		return leafs;
	}
	
	protected List<StoSOONode> nonLeafsAtDepth(int h){
		if(this.nodesByDepth.size() == 1){
			return this.nodesByDepth.get(h);
		}
		List<StoSOONode> nodes = this.nodesByDepth.get(h);
		List<StoSOONode> nonLeafs = new LinkedList<StoSOONode>();
		for(StoSOONode n : nodes){
			if(!n.isLeaf()){
				nonLeafs.add(n);
			}
		}
		
		return nonLeafs;
	}
	
	
	public class StoSOONode{
		
		protected double []			lowerLims;
		protected double []			upperLims;
		
		protected int				n = 0;
		protected double			sumReturn = 0.;
		protected int				h;
		
		protected StoSOONode		child1 = null;
		protected StoSOONode		child2 = null;
		
		protected StoSOONode		parent = null;
		
		public StoSOONode(double [] lowerLims, double [] upperLims){
			this.lowerLims = lowerLims.clone();
			this.upperLims = upperLims.clone();
			this.h = 0;
		}
		
		public StoSOONode(double [] lowerLims, double [] upperLims, int h, StoSOONode parent){
			this.lowerLims = lowerLims.clone();
			this.upperLims = upperLims.clone();
			this.h = h;
			this.parent = parent;
		}
		
		public boolean isLeaf(){
			return this.child1 == null && this.n <= MetaRLStoSOO.this.k;
		}
		
		public double averageReturn(){
			return this.sumReturn / this.n;
		}
		
		public double confidenceTerm(){
			int N = MetaRLStoSOO.this.plannedFunctionEvaluations;
			int k = MetaRLStoSOO.this.k;
			double d = MetaRLStoSOO.this.delta;
			return Math.sqrt(Math.log(N*k/d) / (2 * this.n));
		}
		
		public double bValue(){
			if(this.n == 0){
				return Double.POSITIVE_INFINITY;
			}
			return this.averageReturn() + this.confidenceTerm();
		}
		
		public void sampleAndPerculateUp(){
			
			VarFitnessPair vf = this.sampleWithoutUpdating();
			this.perculateUp(vf.fitness);
			
		}
		
		public VarFitnessPair sampleWithoutUpdating(){
			OptVariables var = MetaRLStoSOO.this.domainSampler.sampleDomain(this.lowerLims, this.upperLims);
			MetaRLStoSOO.this.clamper.clamp(var);
			List<OptVariables> listWrapepr = new ArrayList<OptVariables>(1);
			listWrapepr.add(var);
			List <Double> fs = MetaRLStoSOO.this.varEval.evaluate(listWrapepr);
			
			VarFitnessPair vf = new VarFitnessPair(var, fs.get(0));
			return vf;
		}
		
		public void perculateUp(double r){
			this.sumReturn += r;
			this.n++;
			if(this.parent != null){
				this.parent.perculateUp(r);
			}
		}
		
		public void expand(){
			int sd = RandomFactory.getMapped(0).nextInt(this.lowerLims.length);
			double mid = (this.upperLims[sd] + this.lowerLims[sd]) / 2.;
			
			double [] c1LowerLims = this.lowerLims.clone();
			double [] c1UpperLims = this.upperLims.clone();
			c1UpperLims[sd] = mid;
			
			
			double [] c2LowerLims = this.lowerLims.clone();
			double [] c2UpperLims = this.upperLims.clone();
			c2LowerLims[sd] = mid;
			
			
			int nh = this.h+1;
			this.child1 = new StoSOONode(c1LowerLims, c1UpperLims, nh, this);
			this.child2 = new StoSOONode(c2LowerLims, c2UpperLims, nh, this);
			
			/*
			System.out.println("New children at depth: " + nh);
			System.out.println(doubleString(c1LowerLims) + "; " + doubleString(c1UpperLims));
			System.out.println(doubleString(c2LowerLims) + "; " + doubleString(c2UpperLims));
			System.out.println("----------------");			
			*/
			
			List<StoSOONode> nodesAtDepth = MetaRLStoSOO.this.nodesByDepth.get(nh);
			if(nodesAtDepth == null){
				nodesAtDepth = new LinkedList<MetaRLStoSOO.StoSOONode>();
				MetaRLStoSOO.this.nodesByDepth.put(nh, nodesAtDepth);
			}
			nodesAtDepth.add(this.child1);
			nodesAtDepth.add(this.child2);
			
			if(nh > MetaRLStoSOO.this.curMaxTreeDepth){
				MetaRLStoSOO.this.curMaxTreeDepth = nh;
			}
			
		}
		
		
		
		protected String doubleString(double [] a){
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			for(int i = 0; i < a.length; i++){
				if(i > 0){
					buffer.append(",");
				}
				buffer.append(a[i]);
			}
			buffer.append("]");
			
			return buffer.toString();
		}
		
	}


	

}
