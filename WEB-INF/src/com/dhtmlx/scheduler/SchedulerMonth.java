package com.dhtmlx.scheduler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SchedulerMonth {
	private String monthName;
	private String[][] rows;

	public void parse(Element parent) {
		this.monthName = parent.getAttribute("label");
		NodeList rows = parent.getElementsByTagName("row");
		NodeList cols = parent.getElementsByTagName("column");
		if ((rows != null)&&(cols != null)) {
			this.rows= new String[rows.getLength() + 1][7];
			for (int i = 0; i < cols.getLength(); i++) {
				this.rows[0][i] = cols.item(i).getChildNodes().item(0).getNodeValue();
			}
			for (int i = 1; i <= rows.getLength(); i++) {
				this.rows[i] = rows.item(i - 1).getChildNodes().item(0).getNodeValue().split("\\|");
			}
		}
	}
	
	public String getLabel() {
		return this.monthName;
	}
	
	public String[][] getRows() {
		return this.rows;
	}
	
	public String[][] getOnlyDays() {
		String[][] days = new String[this.rows.length - 1][];
		for (int i = 1; i < this.rows.length; i++) {
			days[i - 1] = this.rows[i];
		}
		return days;
	}
	
}
