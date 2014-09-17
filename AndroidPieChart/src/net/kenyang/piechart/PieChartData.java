package net.kenyang.piechart;

public class PieChartData {
    public float fPercentage;
    public String strTitle;
    
    /**
     * 
     * @param fPercentage value in pie chart
     * @param strTitle the legend title
     */
    public PieChartData(float fPercentage,String strTitle) {
        this.fPercentage = fPercentage;
        this.strTitle    = strTitle;
    }
}
