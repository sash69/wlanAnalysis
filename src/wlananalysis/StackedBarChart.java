/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wlananalysis;

import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author SashNamizni
 */
public class StackedBarChart{
    
    public StackedBarChart(JPanel parent, final String title, CategoryDataset dataset, String xLabel, String yLabel)
    {
        parent.removeAll();
        
        JFreeChart chart = createChart(dataset, title, xLabel, yLabel);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setSize(parent.getWidth(), parent.getHeight());
        chartPanel.setMouseWheelEnabled(true);
        
        parent.add(chartPanel);
        parent.revalidate();
        parent.repaint();
    }    
    
    private JFreeChart createChart(CategoryDataset dataset, String chartName, String xLabel, String yLabel)
    {
        JFreeChart chart = ChartFactory.createStackedBarChart(
                    chartName, 
                    xLabel,
                    yLabel, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    true, 
                    true, 
                    false);
        
        chart.removeLegend();
        
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset ds, int row, int column){
                String time = ds.getRowKey(row).toString();              
                double value = (double)ds.getValue(row, column);
                double valueInSeconds = value * 60;
                int minutes = (int)valueInSeconds / 60;
                int hours = minutes / 60;
                if (hours > 0)
                    minutes = minutes % 60;
                int seconds = (int)valueInSeconds % 60;
                
                return "( "+time+" )" + " - " + (hours > 0 ? String.format("%02d", hours)+":" : "") + String.format("%02d", minutes) + ":" + String.format("%02d", seconds); 
        }            
        });
        
        return chart;
    }
}
