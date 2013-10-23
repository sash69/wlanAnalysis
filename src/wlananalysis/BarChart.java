/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wlananalysis;

import java.awt.GridLayout;
import javax.swing.JDialog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author SashNamizni
 */
public class BarChart{
    
    public BarChart(JDialog parent, final String title, CategoryDataset dataset, String xLabel, String yLabel)
    {
        JFreeChart chart = createChart(dataset, title, xLabel, yLabel);
        chart.removeLegend();
        ChartPanel chartPanel = new ChartPanel(chart);
        JDialog chartDialog = new JDialog(parent);
        chartDialog.setModal(true);
        chartDialog.add(chartPanel);
        chartDialog.setLayout(new GridLayout(1, 1));
        chartDialog.setLocation(parent.getX()+100, parent.getY()+100);
        chartDialog.setSize(800, 400);
        chartDialog.setVisible(true);
    }
    
    private JFreeChart createChart(CategoryDataset dataset, String chartName, String xLabel, String yLabel)
    {
        JFreeChart chart = ChartFactory.createBarChart(
                    chartName, 
                    xLabel,
                    yLabel, 
                    dataset, 
                    PlotOrientation.VERTICAL,
                    true, 
                    true, 
                    false);
        return chart;
    }
}
