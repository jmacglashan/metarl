package metarl;

public interface ParameterizedEnvironmentFactory {
	
	public int nParams();
	public double [] paramLowerLimits();
	public double [] paramUpperLimits();
	
	public EnvironmentAndTask generatedEnvironment();
	
}
