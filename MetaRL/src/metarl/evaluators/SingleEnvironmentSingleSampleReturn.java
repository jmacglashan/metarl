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
	
	protected boolean shouldNormalize = false;
	protected double lowerVal;
	protected double upperVal;
	
	public SingleEnvironmentSingleSampleReturn(
			ParameterizedRLFactory rlFactory,
			int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		
	}
	
	public SingleEnvironmentSingleSampleReturn(
			ParameterizedRLFactory rlFactory,
			int numLearningSteps,
			List<EnvironmentAndTask> sampleEnvironments,
			double lowerVal, double upperVal) {
		super(rlFactory, numLearningSteps, sampleEnvironments);
		this.shouldNormalize = true;
		this.lowerVal = lowerVal;
		this.upperVal = upperVal;
		
	}
	
	
	@Override
	public double getPeformance(OptVariables vars) {
		
		double v = this.getCumulativeReturn(vars, this.sampleEnvironmentFromEnvironmentSamples());
		if(this.shouldNormalize){
			v = (v - this.lowerVal) / (this.upperVal - this.lowerVal);
		}
		
		return v;
		
	}

	@Override
	public int numRLRunsPerEvaluate() {
		return 1;
	}


	protected EnvironmentAndTask sampleEnvironmentFromEnvironmentSamples(){
		return this.sampleEnviroments.get(this.rand.nextInt(this.sampleEnviroments.size()));
	}
	
}
