package metarl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import optimization.OptVariables;
import optimization.Optimization;

public class MetaRLExperiment {
	protected MetaRLEvaluator	exhaustiveEvaluator;
	
	protected XYSeriesCollection plotData;
	
	public MetaRLExperiment(MetaRLEvaluator exhaustiveEvaluator){
		this.exhaustiveEvaluator = exhaustiveEvaluator;
	}
	
	public void printOptimizerPerformance(String optimizerName, MetaRLOptimizer optimizer, int minEvaluations, int maxEvaluations, int increment){
		System.out.println("==========================================");
		System.out.println("Performance for " + optimizerName);
		System.out.println("==========================================");
		int lastNumEvalsRecored = 0;
		for(int evals = minEvaluations; evals <= maxEvaluations; evals += increment){
			optimizer.setRLEvaluationThreshold(evals);
			optimizer.startOrContinueRLOptimization();
			OptVariables best = ((Optimization)optimizer).getBest();
			int nextEvals = optimizer.getTotalNumberOfRLEvaluations();
			if(best != null && nextEvals > lastNumEvalsRecored){
				double performance = this.getExhasutivePerformance(best);
				System.out.println(nextEvals + ": " + performance);
				lastNumEvalsRecored = nextEvals;
			}
		}
	}
	
	public void plotOptimizerPerformance(String optimizerName, MetaRLOptimizer optimizer, int minEvaluations, int maxEvaluations, int increment){
		XYSeries series = new XYSeries(optimizerName);
		this.plotData.addSeries(series);
		int lastNumEvalsRecored = 0;
		for(int evals = minEvaluations; evals <= maxEvaluations; evals += increment){
			optimizer.setRLEvaluationThreshold(evals);
			optimizer.startOrContinueRLOptimization();
			OptVariables best = ((Optimization)optimizer).getBest();
			int nextEvals = optimizer.getTotalNumberOfRLEvaluations();
			if(best != null && nextEvals > lastNumEvalsRecored){
				double performance = this.getExhasutivePerformance(best);
				series.add(optimizer.getTotalNumberOfRLEvaluations(), performance);
				lastNumEvalsRecored = nextEvals;
			}
		}
	}
	
	public void initGUI(int width, int height){
		this.plotData = new XYSeriesCollection();
		JFrame frame = new JFrame();
		final JFreeChart chart = ChartFactory.createXYLineChart("Optimizer Performance", "Number of RL Evaluations", "Best Parameterization Performance", this.plotData);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(width, height));
		frame.getContentPane().add(chartPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	protected double getExhasutivePerformance(OptVariables vars){
		List<OptVariables> varsToEval = new ArrayList<OptVariables>(1);
		varsToEval.add(vars);
		List<Double> fitness = this.exhaustiveEvaluator.evaluate(varsToEval);
		return fitness.get(0);
	}
}
