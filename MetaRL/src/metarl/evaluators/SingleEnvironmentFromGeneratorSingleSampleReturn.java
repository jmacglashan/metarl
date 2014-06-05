package metarl.evaluators;

import metarl.EnvironmentAndTaskGenerator;
import metarl.MetaRLEvaluator;
import metarl.ParameterizedRLFactory;
import optimization.OptVariables;

public class SingleEnvironmentFromGeneratorSingleSampleReturn extends
		MetaRLEvaluator {

	protected EnvironmentAndTaskGenerator envAndTaskGenerator;
	
	public SingleEnvironmentFromGeneratorSingleSampleReturn(
			ParameterizedRLFactory rlFactory, int numLearningSteps, EnvironmentAndTaskGenerator envAndTaskGenerator) {
		super(rlFactory, numLearningSteps);
		this.envAndTaskGenerator = envAndTaskGenerator;
	}

	@Override
	public double getPeformance(OptVariables vars) {
		return this.getCumulativeReturn(vars, this.envAndTaskGenerator.generateEnvironmentAndTask());
	}

	@Override
	public int numRLRunsPerEvaluate() {
		return 1;
	}

}
