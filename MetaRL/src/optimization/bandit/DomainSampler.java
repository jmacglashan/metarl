package optimization.bandit;

import java.util.Random;

import burlap.debugtools.RandomFactory;

import optimization.OptVariables;

public interface DomainSampler {

	public OptVariables sampleDomain(double [] lowerLims, double [] upperLims);
	
	
	
	public static class UniformDomainSampler implements DomainSampler{

		protected Random			rand;
		
		public UniformDomainSampler(){
			this.rand = RandomFactory.getMapped(0);
		}
		
		@Override
		public OptVariables sampleDomain(double[] lowerLims, double[] upperLims) {
			
			int d = lowerLims.length;
			OptVariables v = new OptVariables(d);
			for(int i = 0; i < d; i++){
				double range = upperLims[i] - lowerLims[i];
				double r = (this.rand.nextDouble() * range) + lowerLims[i];
				v.vars[i] = r;
			}
			
			return v;
		}
		
		
		
	}
	
}
