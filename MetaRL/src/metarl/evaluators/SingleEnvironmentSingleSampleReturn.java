package metarl.evaluators;

import java.util.List;
import java.util.Random;

import metarl.EnvironmentAndTask;
import metarl.EnvironmentsSampleMetaRLEvaluator;
import metarl.ParameterizedRLFactory;
import optimization.OptVariables;
import burlap.debugtools.RandomFactory;

public class SingleEnvironmentSingleSampleReturn extends EnvironmentsSampleMetaRLEvaluator {

	protected Random rand = RandomFactory.getMapped(0);
	
	
	public SingleEnvironmentSingleSampleReturn(
			ParameterizedRLFactory rlFactory,
			int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		
	}
	
	
	@Override
	public double getPeformance(OptVariables vars) {
		
		return this.getCumulativeReturn(vars, this.sampleEnvironmentFromEnvironmentSamples());
		
	}

	@Override
	public int numRLRunsPerEvaluate() {
		return 1;
	}


	protected EnvironmentAndTask sampleEnvironmentFromEnvironmentSamples(){
		return this.sampleEnviroments.get(this.rand.nextInt(this.sampleEnviroments.size()));
	}
	
}
