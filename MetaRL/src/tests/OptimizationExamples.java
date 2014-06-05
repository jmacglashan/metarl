package tests;

import java.util.ArrayList;
import java.util.List;

import optimization.OptVariables;
import optimization.Optimization;
import optimization.VarEvaluaiton;
import optimization.VariableClamper;
import optimization.VariableRandomGenerator;
import optimization.bandit.HOO;
import optimization.geneticalgorithm.GeneticAlgorithm;
import optimization.geneticalgorithm.gamodules.ContinuousBlendMate;
import optimization.geneticalgorithm.gamodules.RandomKiller;
import optimization.geneticalgorithm.gamodules.RandomOrganismMutate;
import optimization.geneticalgorithm.gamodules.SoftMaxSingleParentMateSelector;
import optimization.optmodules.ContinuousBoundedClamp;
import optimization.optmodules.ContinuousBoundedVarGen;


/**
 * Shows how to use different optimization algorithms. When running, each optimization algorithm will print the number of evaluations its performed.
 * @author James MacGlashan
 *
 */
public class OptimizationExamples {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		VarEvaluaiton eval = new TwoVarFitnessFunction();
		eval = new TwoVarConvexFitnessFunction(); //uncomment to optimize the simpler convex function
		
		double [] low = new double[]{-5., -5.};
		double [] high = new double[]{5., 5.};
		
		
		VariableClamper clamp = new ContinuousBoundedClamp(low, high);
		VariableRandomGenerator varGen = new ContinuousBoundedVarGen(low, high);
		
		
		Optimization opt = null;
		
		opt = new GeneticAlgorithm(eval, varGen, clamp, new RandomKiller(), 
				new SoftMaxSingleParentMateSelector(0.1), new ContinuousBlendMate(), new RandomOrganismMutate(varGen), 
				200, 1, 1, 0.3, 10, 2);
		
		//opt = new NPointHillClimbing(eval, varGen, clamp, new AccelContNeighborhood(0.1, 3., 0.3), 1, 2, 1, 0);
		//opt = new GenerateAndTest(eval, varGen, clamp, 300, 2);
		//opt = new CrossEntropy(eval, varGen, clamp, 2, 30, 10, 30, 0.05);
		//opt = new CrossEntropy(eval, varGen, clamp, 2, 20, 10, 100, 0.01);
		opt = new HOO(eval, clamp, low, high, 300, 0., 1.);
		
		
		opt.optimize();
		System.out.println("Finished; best:");
		System.out.println(opt.getBest().toString());
		System.out.println(opt.getBestFitness());

	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * A simple non-convex 2-variable function to optimize defined by f(x,y) = x*sin(4x) + 1.1y*sin(2y). 
	 * 
	 * @author James MacGlashan
	 *
	 */
	public static class TwoVarFitnessFunction implements VarEvaluaiton{

		@Override
		public List<Double> evaluate(List<OptVariables> instances) {
			
			List <Double> fitness = new ArrayList<Double>(instances.size());
			for(OptVariables inst : instances){
				double x = inst.v(0);
				double y = inst.v(1);

				double f = (x * Math.sin(4*x)) + (1.1 * y * Math.sin(2*y));
				
				fitness.add(f);
				
			}
			
			return fitness;
		}

	}
	
	
	/**
	 * A simple convex 2-variable function to optimize defined by f(x,y) = -x^2 - y^2 + 5. Max of 5 at (0,0)
	 * @author James MacGlashan
	 *
	 */
	public static class TwoVarConvexFitnessFunction implements VarEvaluaiton{

		@Override
		public List<Double> evaluate(List<OptVariables> instances) {
			
			List <Double> fitness = new ArrayList<Double>(instances.size());
			for(OptVariables inst : instances){
				double x = inst.v(0);
				double y = inst.v(1);

				double f = (-1*(x*x) - (y*y)) + 5;
				
				fitness.add(f);
				
			}
			
			return fitness;
		}

	}

}
