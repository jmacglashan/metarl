package experiments.fivechain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.statehashing.DiscreteStateHashFactory;

import metarl.EnvironmentAndTask;
import metarl.optimizationtools.AverageCumulativeRewardEvaluator;

import optimization.OptVariables;
import optimization.hillclimbing.NPointHillClimbing;
import optimization.hillclimbing.NeighborhoodGenerator;
import optimization.hillclimbing.hcmodules.AccelContNeighborhood;
import optimization.optmodules.ContinuousBoundedClamp;
import optimization.optmodules.ContinuousBoundedVarGen;

public class FiveChainHillClimbingIterations {

	Random rand = new Random(847);

	FiveChainGen fivechain;
	QL3Param rlFact;
	AverageCumulativeRewardEvaluator eval;
	int nTries = 20;
	int nSteps = 1000;
	
	static int repetitions = 40;
	static int range = 20;
	static int mult = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FiveChainHillClimbingIterations exp = new FiveChainHillClimbingIterations(10);
		System.out.println("Starting.");
		List<Double> resultList = new ArrayList<Double>();
		for (int i = 10; i < range; i++) {
			double output = 0;
			for (int j = 0; j < repetitions; j++) {
				OptVariables best = exp.hillClimbingOptimize(1, 1, i * mult);

				System.out.println("Best parameters: " + best.toString());
				output += exp.evalOnTestEnvs(repetitions, best);
			}
			resultList.add(output/(double) repetitions);

			// System.out.println("Performance on test data: " +
			// exp.evalOnTestEnvs(10, best));

		}
		
		int count = 0;
		for (Double result : resultList) {
			System.out.print(count*mult);
			System.out.print(",");
			System.out.println(result);
			count++;
		}

	}

	public FiveChainHillClimbingIterations(int nTraining) {

		this.fivechain = new FiveChainGen();

		// create the parameterized learning algorithm factory
		this.rlFact = new QL3Param(new DiscreteStateHashFactory());

		// create our training dataset of MPDs
		List<EnvironmentAndTask> training = new ArrayList<EnvironmentAndTask>(
				nTraining);
		for (int i = 0; i < nTraining; i++) {
			training.add(this.sampleClass3(fivechain));
		}

		// create an evaluator for parameterized learning algorithms
		this.eval = new AverageCumulativeRewardEvaluator(training, rlFact,
				this.nTries, this.nSteps);

	}

	/**
	 * Runs hill climbing optimization
	 * 
	 * @return the best parameterixation it found
	 */
	public OptVariables hillClimbingOptimize(int numIterations,
			int numRestarts, int totalIterations) {

		// create an object that can generate random instances in our parameter
		// space
		ContinuousBoundedVarGen varGen = new ContinuousBoundedVarGen(
				rlFact.paramLowerLimits(), rlFact.paramUpperLimits());

		// create a clamp which makes sure any randomly generate instances and
		// instances generated by the optimizer and valid.
		ContinuousBoundedClamp varClamp = new ContinuousBoundedClamp(
				rlFact.paramLowerLimits(), rlFact.paramUpperLimits());

		// create a neighborhood generator for hill climbing
		NeighborhoodGenerator neighGen = new AccelContNeighborhood(0.05, 0.05,
				0.); // no acceleration in hill climbing direction

		// create our hill climber optimizer; will use 1 hill climbing point;
		// terminates when there is no improvement and tries 3 restarts
		NPointHillClimbing hc = new NPointHillClimbing(eval, varGen, varClamp,
				neighGen, 1, rlFact.nParams(), numIterations, numRestarts,
				totalIterations);

		// run it
		hc.optimize();

		OptVariables best = hc.getBest();

		return best;
	}

	/**
	 * Creates a test set of environment (drawn from the same distribution) and
	 * gets the average performance on them for a given parameterization
	 * 
	 * @param nTest
	 *            the number of test environments
	 * @param inst
	 *            the parameterization
	 * @return the average performance on them for a given parameterization
	 */
	public double evalOnTestEnvs(int nTest, OptVariables inst) {

		List<EnvironmentAndTask> testEnvs = new ArrayList<EnvironmentAndTask>(
				nTest);
		for (int i = 0; i < nTest; i++) {
			testEnvs.add(this.sampleClass3(fivechain));
		}

		AverageCumulativeRewardEvaluator testEval = new AverageCumulativeRewardEvaluator(
				testEnvs, rlFact, 1, 1000);

		return testEval.evalInstance(inst);

	}

	/**
	 * Samples from Environment class 3 of the experiment Michael originally
	 * described. This has a slip prob between 0 and 0.5 and states are always
	 * ordered from 1 to 5.
	 * 
	 * @param gen
	 *            the 5 chain generator
	 * @return
	 */
	protected EnvironmentAndTask sampleClass3(FiveChainGen gen) {

		double r = rand.nextDouble() * gen.paramLowerLimits()[0];

		EnvironmentAndTask env = gen.generateEnvironment(new double[] { r, 0,
				1, 2, 3, 4 });

		return env;
	}

}
