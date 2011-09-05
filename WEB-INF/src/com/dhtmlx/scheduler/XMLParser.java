package com.dhtmlx.scheduler;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLParser {
	private String xml;
	private Element root;
	private String mode;
	private String todayLabel;
	private String[][] rows;
	private String profile;
	private String header = "false";
	private String footer = "false";
	private ArrayList<SchedulerEvent> multiday = new ArrayList<SchedulerEvent>();
	private ArrayList<SchedulerEvent> events = new ArrayList<SchedulerEvent>();
	private String[] cols = null;

	public void setXML(String xml) throws DOMException, ParserConfigurationException, SAXException {
		this.xml = xml;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = null;
		try {
			dom = db.parse(new InputSource(new StringReader(this.xml)));
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		this.root = dom.getDocumentElement();
		
		NodeList n1 = this.root.getElementsByTagName("scale");
		Element scale = (Element) n1.item(0);
		this.mode = scale.getAttribute("mode");
		this.todayLabel = scale.getAttribute("today");
		this.profile = this.root.getAttribute("profile");
		this.header = this.root.getAttribute("header");
		this.footer = this.root.getAttribute("footer");
		this.eventsParsing();
	}

	public String[] monthColsParsing() {
		NodeList n1 = this.root.getElementsByTagName("column");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			this.cols = new String[n1.getLength()];
			for (int i = 0; i < n1.getLength(); i++) {
				Element col = (Element) n1.item(i);
				this.cols[i] = col.getChildNodes().item(0).getNodeValue();
			}
		}
		return this.cols;
	}
	
	public String[][] monthRowsParsing() {
		NodeList n1 = this.root.getElementsByTagName("row");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			this.rows = new String[n1.getLength()][];
			for (int i = 0; i < n1.getLength(); i++) {
				Element row = (Element) n1.item(i);
				String week = row.getChildNodes().item(0).getNodeValue();
				this.rows[i] = week.split("\\|");
			}
		}
		return this.rows;
	}

	public void eventsParsing() {
		NodeList n1 = this.root.getElementsByTagName("event");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			for (int i = 0; i < n1.getLength(); i++) {
				Element ev = (Element) n1.item(i);
				SchedulerEvent oEv = new SchedulerEvent();
				oEv.parse(ev);
				if ((oEv.getType().compareTo("event_line") == 0)&&(this.mode.compareTo("month") != 0)&&(this.mode.compareTo("timeline") != 0)) {
					multiday.add(oEv);
				} else {
					events.add(oEv);
				}
			}
		}
	}

	public SchedulerEvent[] getEvents() {
		SchedulerEvent[] events_list = new SchedulerEvent[events.size()];
		for (int i = 0; i < events.size(); i++)
			events_list[i] = events.get(i);
		return events_list;
	}
	
	public SchedulerEvent[] getMultidayEvents() {
		SchedulerEvent[] events_list = new SchedulerEvent[multiday.size()];
		for (int i = 0; i < multiday.size(); i++)
			events_list[i] = multiday.get(i);
		return events_list;
	}

	public String[] weekColsParsing() {
		if (cols != null)
			return cols;
		NodeList n1 = this.root.getElementsByTagName("column");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			cols = new String[n1.getLength()];
			for (int i = 0; i < n1.getLength(); i++) {
				Element col = (Element) n1.item(i);
				cols[i] = col.getChildNodes().item(0).getNodeValue();
			}
		}
		return cols;
	}
	
	public String[] weekRowsParsing() {
		String[] rows = null;
		NodeList n1 = this.root.getElementsByTagName("row");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			rows = new String[n1.getLength()];
			for (int i = 0; i < n1.getLength(); i++) {
				Element row = (Element) n1.item(i);
				rows[i] = row.getChildNodes().item(0).getNodeValue();
			}
		}
		return rows;
	}
	
	public SchedulerMonth[] yearParsing() {
		SchedulerMonth[] monthes = null;
		NodeList n1 = this.root.getElementsByTagName("month");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			monthes = new SchedulerMonth[n1.getLength()];
			for (int i = 0; i < n1.getLength(); i++) {
				monthes[i] = new SchedulerMonth();
				Element mon = (Element) n1.item(i);
				monthes[i].parse(mon);
			}
		}
		return monthes;
	}
	
	public String[] agendaColsParsing() {
		String[] cols = null;
		NodeList n1 = this.root.getElementsByTagName("column");
		if ((n1 != null)&&(n1.getLength() > 0)) {
			cols = new String[n1.getLength()];
			for (int i = 0; i < n1.getLength(); i++) {
				Element col = (Element) n1.item(i);
				cols[i] = col.getChildNodes().item(0).getNodeValue();
			}
		}
		return cols;
	}
	
	public String getMode() {
		return this.mode;
	}

	public String getTodatLabel() {
		return this.todayLabel;
	}
	
	public String getColorProfile() {
		return this.profile;
	}
	
	public boolean getHeader() {
		boolean result = false;
		if (this.header.compareTo("true") == 0) {
			result = true;
		}
		return result;
	}
	
	public boolean getFooter() {
		boolean result = false;
		if (this.footer.compareTo("true") == 0) {
			result = true;
		}
		return result;
	}

}
