package metarl;

public interface ParameterizedEnvironmentFactory {
	
	
	/**
	 * Returns the number of parameters for this environment and task generator
	 * @return the number of parameters for this environment and task generator
	 */
	public int nParams();
	
	/**
	 * Returns the lower limit values of each parameter for this environment and task generator
	 * @return the lower limit values of each parameter for this environment and task generator
	 */
	public double [] paramLowerLimits();
	
	
	/**
	 * Returns the upper limit values of each parameter for this environment and task generator
	 * @return the upper limit values of each parameter for this environment and task generator
	 */
	public double [] paramUpperLimits();
	
	
	/**
	 * Returns an environment and task for the given parameters
	 * @param params the parameters for this environment and task
	 * @return an environment and task for the given parameters
	 */
	public EnvironmentAndTask generateEnvironment(double [] params);
	
}
