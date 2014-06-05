package experiments.metarlopt;

import java.util.ArrayList;
import java.util.List;

import metarl.EnvironmentAndTask;
import metarl.EnvironmentAndTaskGenerator;
import metarl.MetaRLEvaluator;
import metarl.MetaRLExperiment;
import metarl.MetaRLOptimizer;
import metarl.ParameterizedRLFactory;
import metarl.evaluators.SampledEnvironmentsMultipleAveragedRunReturn;
import metarl.evaluators.SingleEnvironmentSingleSampleReturn;
import metarl.optimizers.MetaRLCrossEntropy;
import metarl.optimizers.MetaRLHOO;
import metarl.optimizers.MetaRLHillClimbing;
import optimization.VariableClamper;
import optimization.VariableRandomGenerator;
import optimization.hillclimbing.hcmodules.MultiDimAccelContNeighborhood;
import optimization.optmodules.ContinuousBoundedClamp;
import optimization.optmodules.ContinuousBoundedVarGen;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.debugtools.RandomFactory;
import experiments.fivechain.FiveChainGen;
import experiments.fivechain.QL3Param;

public class FiveChainMRLOpt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int numLearningSteps = 1000;
		
		//generate dataset of size 15
		FiveChainClass3ETGen gen = new FiveChainClass3ETGen();
		int n = 15;
		List<EnvironmentAndTask> training = new ArrayList<EnvironmentAndTask>(n);
		for(int i = 0; i < n; i++){
			training.add(gen.generateEnvironmentAndTask());
		}
		
		//set up agent factor
		ParameterizedRLFactory agentFactory = new QL3Param(new DiscreteStateHashFactory());
		
		//set up exhaustive test
		MetaRLEvaluator exhaustiveEval = new SampledEnvironmentsMultipleAveragedRunReturn(agentFactory, numLearningSteps, training, 10);
		
		//create exp
		MetaRLExperiment exp = new MetaRLExperiment(exhaustiveEval);
		exp.initGUI(1000, 400);
		
		
		
		
		
		//common mods for optmizers
		VariableClamper clamper = new ContinuousBoundedClamp(agentFactory.paramLowerLimits(), agentFactory.paramUpperLimits());
		VariableRandomGenerator varGen = new ContinuousBoundedVarGen(agentFactory.paramLowerLimits(), agentFactory.paramUpperLimits());
		MetaRLEvaluator fullSampleEval = new SampledEnvironmentsMultipleAveragedRunReturn(agentFactory, numLearningSteps, training, 1);
		
		
		
		
		//set up bandit eval
		MetaRLEvaluator banditEval = new SingleEnvironmentSingleSampleReturn(agentFactory, numLearningSteps, training);
		MetaRLOptimizer banditOptimizer = new MetaRLHOO(banditEval, clamper,
				agentFactory.paramLowerLimits(), agentFactory.paramUpperLimits(), 0, 2000., 0.95);
		
		//run it!
		exp.plotOptimizerPerformance("HOO (single env sample)", banditOptimizer, 10, 1000, 10);
		
		MetaRLOptimizer banditFullOptimizer = new MetaRLHOO(fullSampleEval, clamper,
				agentFactory.paramLowerLimits(), agentFactory.paramUpperLimits(), 0, 200., 0.2);
		
		//run it!
		exp.plotOptimizerPerformance("HOO (full env 1 sample)", banditFullOptimizer, 10, 1000, 10);
		
		
		
		//set up cross entropy SS
		MetaRLOptimizer ceWithBanditEval = new MetaRLCrossEntropy(banditEval, varGen, 
				clamper, agentFactory.nParams(), 25, 10, 0, 0);
		
		exp.plotOptimizerPerformance("CE (single env sample)", ceWithBanditEval, 25, 1000, 25);
		
		//set up cross entropy full sample
		MetaRLOptimizer ceWithFullSample = new MetaRLCrossEntropy(fullSampleEval, varGen, 
				clamper, agentFactory.nParams(), 25, 10, 0, 0);
		
		exp.plotOptimizerPerformance("CE (full env 1 sample)", ceWithFullSample, 150, 1000, 150);
		
		
		//set up hill climbing single sample
		MetaRLOptimizer hcSS = new MetaRLHillClimbing(banditEval, varGen, clamper, 
				new MultiDimAccelContNeighborhood(new double[]{0.05, 0.05, 5.}, new double[]{0.2, 0.2, 30.}, new double[]{0.05, 0.05, 5}), 
				agentFactory.nParams(), 5);
		exp.plotOptimizerPerformance("HC (single env sample)", hcSS, 5, 1000, 2);
		
		
		
		//set up hill climbing using full sample
		MetaRLOptimizer hcFS = new MetaRLHillClimbing(fullSampleEval, varGen, clamper, 
				new MultiDimAccelContNeighborhood(new double[]{0.05, 0.05, 5.}, new double[]{0.2, 0.2, 30.}, new double[]{0.05, 0.05, 5}), 
				agentFactory.nParams(), 5);
		exp.plotOptimizerPerformance("HC (full env 1 sample)", hcFS, 25, 1000, 25);
		

	}
	
	
	
	public static class FiveChainClass3ETGen implements EnvironmentAndTaskGenerator{

		FiveChainGen gen = new FiveChainGen();
		
		@Override
		public EnvironmentAndTask generateEnvironmentAndTask() {
			
			double r = RandomFactory.getMapped(0).nextDouble()*(gen.paramUpperLimits()[0] - gen.paramLowerLimits()[0]) + gen.paramLowerLimits()[0];
			EnvironmentAndTask env = gen.generateEnvironment(new double[]{r, 0, 1, 2, 3, 4});
			
			return env;
		}
		
		
		
	}

}
