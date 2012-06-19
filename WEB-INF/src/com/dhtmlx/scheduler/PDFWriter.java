package com.dhtmlx.scheduler;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.pdfjet.*;

public class PDFWriter {
	private XMLParser parser;
	private PDF pdf;
	private Font f1;
	private Page page;
	private ArrayList<Page> pages;

	private double pageWidth = 0;
	private double pageHeight = 0;
	private double offsetTop = 30;
	private double offsetBottom = 30;
	private double offsetLeft = 30;
	private double offsetRight = 30;
	private double headHeight = 20;
	private double monthDayHeaderHeight = 20;
	private double monthEventHeight = 12;
	private double leftScaleWidth = 80;
	private double timelineLeft = 80;
	private double monthEventOffsetLeft = 2;
	private double monthEventOffsetTop = 2;
	private double yearMonthOffsetLeft = 10;
	private double yearMonthOffsetTop = 10;
	private double yearMonthLabelHeight = 20;
	private double weekEventHeaderHeight = 10;
	private double agendaColOneWidth = 150;
	private double multidayLineHeight = 14;
	private double weekAgendaEventHeight = 20;

	private String linePattern = "[] 0";

	private String pageNumTemplate = "Page {pageNum}/{allNum}";
	private String pathToImgs = "d:/java/workspace/scheduler2pdf/imgs";
	private double headerImgHeight = 100;
	private double footerImgHeight = 100;

	private double monthDayWidth;
	private double monthDayHeight;
	private double contHeight;
	private double contWidth;

	private double weekDayWidth;
	private double weekDayHeight;
	private double headerHeight;
	private double multiHeight = 0;

	private String profile = "color";

	private double cellOffset = 3;

	private String bgColor = "C2D5FC";
	private String lineColor = "586A7E";
	private String headerLineColor = "FFFFFF";

	private String dayHeaderColor = "EBEFF4";
	private String watermarkTextColor;
	private String dayBodyColor = "FFFFFF";
	private String dayHeaderColorInactive = "E2E3E6";
	private String dayBodyColorInactive = "ECECEC";
	private String eventColor = "FFE763";
	private String eventTextColor = "887A2E";
	private String scaleOneColor = "FCFEFC";
	private String scaleTwoColor = "DCE6F4";
	private String eventBorderColor = "B7A543";
	private String textColor = "000000";
	private String yearDayActiveColor = "EBEFF4";
	private String yearDayInactiveColor = "D6D6D6";
	private String multidayColor = "E1E6FF";
	private String matrixEventColor = "FFFFFF";
	private String view;
	public String watermark = null;
	private HttpServletResponse resp = null;

	public void generate(String xml, HttpServletResponse resp)
			throws IOException {
		this.parser = new XMLParser();
		this.headerHeight = this.headHeight;
		this.resp = resp;

		try {
			this.parser.setXML(xml);
			this.profile = this.parser.getColorProfile();
			this.setColorProfile(this.profile);
			view = this.parser.getMode();
			if (view.compareToIgnoreCase("month") == 0) {
				this.printMonth();
			} else {
				if (view.compareTo("year") == 0) {
					this.printYear();
				} else {
					if (view.compareTo("agenda") == 0 || view.compareTo("map") == 0) {
						this.printAgenda();
					} else {
						if (view.compareTo("timeline") == 0) {
							this.printTimeline();
						} else {
							if (view.compareTo("matrix") == 0) {
								this.printMatrix();
							} else {
								if (view.compareTo("week_agenda") == 0) {
									this.printWeekAgenda();
								} else {
									this.printWeek();
								}
							}
						}
					}
				}
			}
			this.todayLabelDraw();
			this.outputPDF(resp);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void printMonth() throws Exception {
		this.createPDF(Letter.LANDSCAPE);
		this.printHeader();
		this.printFooter();
		this.monthHeaderDraw();
		this.monthContainerDraw();
		this.monthEventsDraw();
		this.printWatermark();
	}

	private void printYear() throws Exception {
		this.createPDF(Letter.LANDSCAPE);
		this.printHeader();
		this.printFooter();
		this.yearDraw();
	}

	private void printAgenda() throws Exception {
		this.createPDF(Letter.PORTRAIT);
		this.printHeader();
		this.printFooter();
		this.agendaHeaderDraw();
		this.agendaEventDraw();
		this.agendaPagesDraw();
	}

	private void printTimeline() throws Exception {
		this.leftScaleWidth = this.timelineLeft;
		this.createPDF(Letter.LANDSCAPE);
		this.printHeader();
		this.printFooter();
		this.timelineHeaderDraw();
		this.timelineContainerDraw();
		this.timelineEventsDraw();
	}

	private void printMatrix() throws Exception {
		this.leftScaleWidth = this.timelineLeft;
		this.createPDF(Letter.LANDSCAPE);
		this.printHeader();
		this.printFooter();
		this.weekHeaderDraw();
		this.matrixContainerDraw();
	}

	private void printWeek() throws Exception {
		this.createPDF(Letter.PORTRAIT);
		this.printHeader();
		this.printFooter();
		this.weekHeaderDraw();
		this.weekMultidayDraw();
		this.weekContainerDraw();
		this.weekEventsDraw();
		if (this.profile.compareTo("bw") == 0)
			this.weekBwBordersDraw();
	}

	private void printWeekAgenda() throws Exception {
		this.createPDF(Letter.PORTRAIT);
		this.printHeader();
		this.printFooter();
		SchedulerEvent[] events = this.parser.getEvents();
		while (events.length > 0) {
			this.weekAgendaContainerDraw();
			events = this.weekAgendaEventsDraw(events);
			if (events.length > 0) {
				this.page = new Page(this.pdf, Letter.PORTRAIT);
				this.pages.add(this.page);
				this.todayLabelDraw(this.page);
			}
		}
	}

	private void printWatermark() throws Exception {
		if (watermark == null) return;
		
		f1.setSize(10);
		TextLine text = new TextLine(f1, watermark);
		text.setColor(RGBColor.getColor(watermarkTextColor));
		double x = offsetLeft;
		double y = pageHeight + offsetTop + f1.getSize();
		text.setPosition(x, y);
		text.drawOn(page);
	}
	
	private void createPDF(double[] orientation) throws Exception {
		this.resp.setContentType("application/pdf");
		this.pdf = new PDF(resp.getOutputStream());

		this.f1 = new Font(pdf, "Helvetica");
		this.f1.setSize(10);

		this.pages = new ArrayList<Page>();
		this.page = new Page(this.pdf, orientation);
		this.pages.add(this.page);
		double[] sizes = orientation;
		this.pageWidth = sizes[0] - this.offsetLeft - this.offsetRight;
		this.pageHeight = sizes[1] - this.offsetTop - this.offsetBottom;
	}

	private void monthHeaderDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.headerLineColor);
		String[] cols = this.parser.monthColsParsing();
		double width = this.pageWidth / cols.length;
		double height = this.headerHeight;
		double x = this.offsetLeft;
		double y = this.offsetTop;
		for (int i = 0; i < cols.length; i++) {
			Box cell = new Box();
			cell.setSize(width, height);
			cell.setPosition(x, y);
			cell.setFillShape(true);
			cell.setColor(bgColor);
			cell.drawOn(this.page);

			if (i > 0) {
				Line borderLeft = new Line();
				borderLeft.setColor(borderColor);
				borderLeft.setPattern(this.linePattern);
				borderLeft.setStartPoint(0, 0);
				borderLeft.setEndPoint(0, height);
				borderLeft.placeIn(cell);
				borderLeft.drawOn(this.page);
			}

			TextLine text = new TextLine(this.f1, this.textWrap(cols[i], width
					- 2 * this.cellOffset, this.f1));
			double text_x = (width - this.f1.stringWidth(text.getText())) / 2;
			double text_y = (height + this.f1.getSize()) / 2;
			text.setPosition(text_x, text_y);
			text.placeIn(cell);
			text.drawOn(this.page);

			x += width;
		}
	}

	private void monthContainerDraw() throws Exception {
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] dayHeaderColor = RGBColor.getColor(this.dayHeaderColor);
		double[] dayBodyColor = RGBColor.getColor(this.dayBodyColor);
		double[] dayHeaderColorInactive = RGBColor
				.getColor(this.dayHeaderColorInactive);
		double[] dayBodyColorInactive = RGBColor
				.getColor(this.dayBodyColorInactive);
		String[][] rows = this.parser.monthRowsParsing();
		double width = this.pageWidth / 7;
		double height = (this.pageHeight - this.headerHeight) / rows.length;
		this.monthDayWidth = width;
		this.monthDayHeight = height;
		double x = this.offsetLeft;
		double y = this.offsetTop + this.headerHeight;
		for (int i = 0; i < rows.length; i++) {
			x = this.offsetLeft;
			for (int j = 0; j < rows[i].length; j++) {
				boolean activeDay = this.getActiveDay(rows, i, j);

				Box cell = new Box();
				cell.setSize(width, height);
				cell.setFillShape(true);
				if (activeDay == true) {
					cell.setColor(dayBodyColor);
				} else {
					cell.setColor(dayBodyColorInactive);
				}
				cell.setPosition(x, y);

				Box cellIn = new Box();
				cellIn.setPosition(0, 0);
				cellIn.setSize(width, this.monthDayHeaderHeight);
				cellIn.placeIn(cell, 0, 0);
				cellIn.setFillShape(true);
				if (activeDay == true) {
					cellIn.setColor(dayHeaderColor);
				} else {
					cellIn.setColor(dayHeaderColorInactive);
				}

				Line borderLeft = new Line();
				borderLeft.setColor(borderColor);
				borderLeft.setPattern(this.linePattern);
				borderLeft.setStartPoint(0, 0);
				borderLeft.setEndPoint(0, height);
				borderLeft.placeIn(cell);

				Line borderTop = new Line();
				borderTop.setColor(borderColor);
				borderTop.setPattern(this.linePattern);
				borderTop.setStartPoint(0, 0);
				borderTop.setEndPoint(width, 0);
				borderTop.placeIn(cell);

				cell.drawOn(this.page);
				cellIn.drawOn(this.page);

				x += width;

				TextLine text = new TextLine(this.f1, rows[i][j]);
				this.f1.setSize(9);
				double text_x = width - this.f1.stringWidth(text.getText())
						- this.cellOffset;
				double text_y = (this.monthDayHeaderHeight + this.f1.getSize()) / 2;
				text.setPosition(text_x, text_y);
				text.placeIn(cellIn);
				text.drawOn(this.page);
				borderLeft.drawOn(this.page);
				borderTop.drawOn(this.page);
			}
			y += height;
		}
		Line borderRight = new Line();
		borderRight.setColor(borderColor);
		borderRight.setPattern(this.linePattern);
		borderRight.setStartPoint(this.offsetLeft + this.pageWidth,
				this.offsetTop);
		borderRight.setEndPoint(this.offsetLeft + this.pageWidth,
				this.offsetTop + this.pageHeight);
		borderRight.drawOn(this.page);
		Line borderBottom = new Line();
		borderBottom.setColor(borderColor);
		borderBottom.setPattern(this.linePattern);
		borderBottom.setStartPoint(this.offsetLeft, this.offsetTop
				+ this.pageHeight);
		borderBottom.setEndPoint(this.offsetLeft + this.pageWidth,
				this.offsetTop + this.pageHeight);
		borderBottom.drawOn(this.page);
	}

	private void monthEventsDraw() throws Exception {
		double[] eventColor = RGBColor.getColor(this.eventColor);
		double[] eventBorderColor = RGBColor.getColor(this.eventBorderColor);
		double[] textColor = RGBColor.getColor(this.eventTextColor);
		SchedulerEvent[] events = this.parser.getEvents();
		int footenote = 1;
		int[][] events_grid = new int[6][];
		for (int i = 0; i < 6; i++) {
			events_grid[i] = new int[7];
			for (int j = 0; j < 7; j++) {
				events_grid[i][j] = 0;
			}
		}
		for (int i = 0; i < events.length; i++) {
			String event_text = events[i].getText();
			int day = events[i].getDay();
			int week = events[i].getWeek() - 1;

			double width = (events[i].getWidth() * this.monthDayWidth) / 100;
			String type = events[i].getType();
			String bgColor = events[i].getBackgroundColor();
			String color = events[i].getColor();
			if (type.compareTo("event_line") == 0) {
				Box cell = new Box();
				double cell_x = this.offsetLeft + day * this.monthDayWidth
						+ this.monthEventOffsetLeft;
				double cell_y = this.offsetTop;
				cell_y += this.headerHeight;
				cell_y += week * this.monthDayHeight;
				cell_y += this.monthDayHeaderHeight;
				cell_y += events_grid[week][day]
						* (this.monthEventHeight + this.monthEventOffsetTop);
				cell_y += this.monthEventOffsetTop;
				double height = this.monthEventHeight;
				cell.setSize(width, height);
				cell.setColor(eventBorderColor);
				cell.setPosition(cell_x, cell_y);

				Box cellIn = new Box();
				cellIn.setPosition(0, 0);
				cellIn.setSize(width, height);
				cellIn.placeIn(cell, 0, 0);
				cellIn.setFillShape(true);
				if ((bgColor.compareTo("transparent") != 0)
						&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
								.compareTo("color") == 0))) {
					cellIn.setColor(RGBColor.getColor(bgColor));
				} else {
					cellIn.setColor(eventColor);
				}

				this.f1.setSize(8);
				TextLine text = new TextLine(this.f1, this.textWrap(event_text,
						width - 2 * this.cellOffset, this.f1));
				double text_x = this.cellOffset;
				double text_y = (height + this.f1.getSize()) / 2;
				text.setPosition(text_x, text_y);
				text.placeIn(cellIn);
				if ((color.compareTo("transparent") != 0)
						&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
								.compareTo("color") == 0))) {
					text.setColor(RGBColor.getColor(color));
				} else {
					text.setColor(textColor);
				}

				if ((cell_y + height) >= (this.offsetTop + this.headerHeight + (week + 1)
						* this.monthDayHeight)) {
					int event_footenote = getFootenoteExists(events, week, day);
					if (event_footenote > 0) {
						events[i].setFootenote(event_footenote);
					} else {
						events[i].setFootenote(footenote);
						footenote++;
					}
				} else {
					cellIn.drawOn(this.page);
					cell.drawOn(this.page);
					text.drawOn(this.page);
				}
				events_grid[week][day]++;
				int event_length = (int) Math.floor(width / this.monthDayWidth);
				for (int j = 1; j <= event_length; j++) {
					events_grid[week][day + j]=events_grid[week][day];
				}
			} else {
				double cell_x = this.offsetLeft + day * this.monthDayWidth
						+ this.monthEventOffsetLeft;
				double cell_y = this.offsetTop;
				cell_y += this.headerHeight;
				cell_y += week * this.monthDayHeight;
				cell_y += this.monthDayHeaderHeight;
				cell_y += events_grid[week][day]
						* (this.monthEventHeight + this.monthEventOffsetTop);
				cell_y += this.monthEventOffsetTop;
				double height = this.monthEventHeight;
				this.f1.setSize(8);
				TextLine text = new TextLine(this.f1, this.textWrap(event_text,
						width - 2 * this.cellOffset, this.f1));
				if ((color.compareTo("transparent") != 0)
						&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
								.compareTo("color") == 0))) {
					text.setColor(RGBColor.getColor(color));
				} else {
					text.setColor(textColor);
				}
				double text_x = cell_x + this.cellOffset;
				double text_y = cell_y + (height + this.f1.getSize()) / 2;
				text.setPosition(text_x, text_y);
				if ((cell_y + height) >= (this.offsetTop + this.headerHeight + (week + 1)
						* this.monthDayHeight)) {
					int event_footenote = getFootenoteExists(events, week, day);
					if (event_footenote > 0) {
						events[i].setFootenote(event_footenote);
					} else {
						events[i].setFootenote(footenote);
						footenote++;
					}
				} else {
					text.drawOn(this.page);
				}
				events_grid[week][day]++;
			}
		}
		if (this.profile.compareTo("bw") == 0)
			this.monthBwBordersDraw();
		this.monthFootenotesDraw(events, footenote);
	}

	private int getFootenoteExists(SchedulerEvent[] events, int week, int day) {
		int result = -1;
		for (int i = 0; i < events.length; i++) {
			int event_week = events[i].getWeek() - 1;
			int event_day = events[i].getDay();
			if ((event_week == week) && (event_day == day)) {
				int foote = events[i].getFootenote();
				if (foote > 0) {
					result = events[i].getFootenote();
				}
			}
		}
		return result;
	}

	private void monthFootenotesDraw(SchedulerEvent[] events, int footenotes)
			throws Exception {
		String[][] rows = this.parser.monthRowsParsing();
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] eventColor = RGBColor.getColor(this.eventColor);
		double[] eventTextColor = RGBColor.getColor(this.eventTextColor);
		if (footenotes > 1) {
			this.page = new Page(this.pdf, Letter.LANDSCAPE);
			this.pages.add(this.page);
		}
		double labelHeight = 20;
		double height = 20;
		double width = 160;
		double offsetLeft = 20;
		double x = this.offsetLeft;
		double y = this.offsetTop;
		for (int i = 1; i < footenotes; i++) {
			SchedulerEvent[] foot_events = this.getFootenoteEvents(events, i);
			int day = foot_events[0].getDay();
			int week = foot_events[0].getWeek() - 1;
			if (y + labelHeight + height > this.offsetTop + this.pageHeight) {
				x += width + offsetLeft;
				if (x + width > this.offsetLeft + this.pageWidth) {
					this.page = new Page(this.pdf, Letter.LANDSCAPE);
					this.pages.add(this.page);
					x = this.offsetLeft;
				}
				y = this.offsetTop;
			}
			TextLine label = new TextLine(this.f1, rows[week][day] + "["
					+ Integer.toString(i) + "]");
			double label_x = x + (width - this.f1.stringWidth(label.getText()))
					/ 2;
			double label_y = y + labelHeight - this.cellOffset;
			label.setPosition(label_x, label_y);
			label.drawOn(this.page);
			y += labelHeight;
			for (int j = 0; j < foot_events.length; j++) {
				if (y + height > this.offsetTop + this.pageHeight) {
					x += width + offsetLeft;
					if (x + width > this.offsetLeft + this.pageWidth) {
						this.page = new Page(this.pdf, Letter.LANDSCAPE);
						this.pages.add(this.page);
						x = this.offsetLeft;
					}
					y = this.offsetTop;
					label_x = x
							+ (width - this.f1.stringWidth(label.getText()))
							/ 2;
					label_y = y + labelHeight - this.cellOffset;
					label.setPosition(label_x, label_y);
					label.drawOn(this.page);
					y += labelHeight;
				}
				Box cont = new Box();
				cont.setSize(width, height);
				cont.setPosition(x, y);
				cont.setColor(borderColor);
				cont.setPattern(this.linePattern);

				Box contBg = new Box();
				contBg.setSize(width, height);
				contBg.setPosition(0, 0);
				contBg.setFillShape(true);
				contBg.setColor(eventColor);
				contBg.placeIn(cont, 0, 0);

				contBg.drawOn(this.page);
				cont.drawOn(this.page);

				TextLine text = new TextLine(this.f1, this.textWrap(
						foot_events[j].getText(), width - 2 * this.cellOffset,
						this.f1));
				text.setColor(eventTextColor);
				double text_x = this.cellOffset;
				double text_y = (height + this.f1.getSize()) / 2;
				text.setPosition(text_x, text_y);
				text.placeIn(cont);

				text.drawOn(this.page);

				y += height;
			}
		}
		if (footenotes > 1) {
			this.agendaPagesDraw();
		}
	}

	private SchedulerEvent[] getFootenoteEvents(SchedulerEvent[] events,
			int footenote) {
		SchedulerEvent[] foot_events = null;
		int nums = 0;
		for (int i = 0; i < events.length; i++) {
			if (events[i].getFootenote() == footenote) {
				nums++;
			}
		}
		foot_events = new SchedulerEvent[nums];
		nums = 0;
		for (int i = 0; i < events.length; i++) {
			if (events[i].getFootenote() == footenote) {
				foot_events[nums] = events[i];
				nums++;
			}
		}
		return foot_events;
	}

	private void monthBwBordersDraw() throws Exception {
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double x = this.offsetLeft;
		double y = this.offsetTop;
		this.Line(borderColor, x, y, x + this.pageWidth, y);
		this.Line(borderColor, x, y, x, y + this.headerHeight);

		x = this.offsetLeft + this.pageWidth;
		this.Line(borderColor, x, y, x, y + this.headerHeight);
	}

	private void weekHeaderDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.headerLineColor);
		String[] cols = this.parser.weekColsParsing();
		double width = (this.pageWidth - this.leftScaleWidth) / cols.length;
		double height = this.headerHeight;
		this.weekDayWidth = width;
		double x = this.offsetLeft + this.leftScaleWidth;
		double y = this.offsetTop;
		for (int i = 0; i < cols.length; i++) {
			Box cell = new Box();
			cell.setSize(width, height);
			cell.setColor(bgColor);
			cell.setFillShape(true);
			cell.setPosition(x, y);
			cell.drawOn(this.page);

			Line borderLeft = new Line();
			borderLeft.setColor(borderColor);
			borderLeft.setPattern(this.linePattern);
			borderLeft.setStartPoint(0, 0);
			borderLeft.setEndPoint(0, height);
			borderLeft.placeIn(cell);

			Line borderTop = new Line();
			borderTop.setColor(borderColor);
			borderTop.setPattern(this.linePattern);
			borderTop.setStartPoint(0, 0);
			borderTop.setEndPoint(width, 0);
			borderTop.placeIn(cell);

			borderLeft.drawOn(this.page);
			borderTop.drawOn(this.page);

			this.f1.setSize(7.4);
			TextLine text = new TextLine(this.f1, this.textWrap(cols[i], width
					- 2 * this.cellOffset, this.f1));
			double text_x = (width - this.f1.stringWidth(text.getText())) / 2;
			double text_y = (height + this.f1.getSize()) / 2;
			text.setPosition(text_x, text_y);
			text.placeIn(cell);
			text.drawOn(this.page);

			x += width;
		}
	}

	
	
	private void weekMultidayDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.multidayColor);
		double[] borderColor = RGBColor.getColor(this.headerLineColor);

		multiHeight = this.getMultidayHeight();

		Box cell = new Box();
		cell.setSize(this.pageWidth, multiHeight);
		cell.setColor(bgColor);
		cell.setFillShape(true);
		double x = this.offsetLeft;
		double y = this.offsetTop + this.headerHeight;
		cell.setPosition(x, y);
		cell.drawOn(this.page);

		Line border = new Line();
		border.setColor(borderColor);
		border.setPattern(this.linePattern);
		border.setStartPoint(this.leftScaleWidth, 0);
		border.setEndPoint(this.leftScaleWidth, multiHeight);
		border.placeIn(cell);
		border.drawOn(this.page);

		border = new Line();
		border.setColor(borderColor);
		border.setPattern(this.linePattern);
		border.setStartPoint(0, 0);
		border.setEndPoint(this.pageWidth, 0);
		border.placeIn(cell);
		border.drawOn(this.page);

		this.headerHeight += multiHeight;
	}

	private double getMultidayHeight() throws Exception {
		String[] cols = this.parser.weekColsParsing();
		SchedulerEvent[] events = this.parser.getMultidayEvents();
		int[] scheme = new int[cols.length];
		for (int i = 0; i < scheme.length; i++)
			scheme[i] = 0;
		for (int i = 0; i < events.length; i++) {
			int day = events[i].getDay();
			int len = events[i].getLen();
			for (int j = day; j < day + len; j++)
				scheme[j]++;
		}
		int max = scheme[0];
		for (int i = 1; i < scheme.length; i++) {
			if (scheme[i] > max)
				max = scheme[i];
		}
		return max * this.multidayLineHeight;
	}

	private void weekContainerDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] headerLineColor = RGBColor.getColor(this.headerLineColor);
		double[] scaleOneColor = RGBColor.getColor(this.scaleOneColor);
		double[] scaleTwoColor = RGBColor.getColor(this.scaleTwoColor);
		String[] rows = this.parser.weekRowsParsing();
		double width = this.leftScaleWidth;
		double height = (this.pageHeight - this.headerHeight) / rows.length;
		this.weekDayHeight = height;
		double x = this.offsetLeft;
		double y = this.offsetTop + this.headerHeight;
		for (int i = 0; i < rows.length; i++) {
			Box cell = new Box();
			cell.setSize(width, height);
			cell.setColor(bgColor);
			cell.setFillShape(true);
			cell.setPosition(x, y);

			Box scaleOne = new Box();
			scaleOne.setPosition(x + width, y);
			scaleOne.setSize(this.pageWidth - width, height / 2);
			scaleOne.setFillShape(true);
			scaleOne.setColor(scaleOneColor);

			Box scaleTwo = new Box();
			scaleTwo.setPosition(x + width, y + height / 2);
			scaleTwo.setSize(this.pageWidth - width, height / 2);
			scaleTwo.setFillShape(true);
			scaleTwo.setColor(scaleTwoColor);

			cell.drawOn(this.page);
			scaleOne.drawOn(this.page);
			scaleTwo.drawOn(this.page);
			this.Line(headerLineColor, 0, 0, width, 0, cell);

			TextLine text = new TextLine(this.f1, this.textWrap(rows[i], width
					- 2 * this.cellOffset, this.f1));
			double text_x = (width - this.f1.stringWidth(text.getText())) / 2;
			double text_y = (height + this.f1.getSize()) / 2;
			text.setPosition(text_x, text_y);
			text.placeIn(cell);
			text.drawOn(this.page);

			y += height;
		}

		String[] cols = this.parser.weekColsParsing();
		width = (this.pageWidth - this.leftScaleWidth) / cols.length;
		x = this.offsetLeft + this.leftScaleWidth;
		y = this.offsetTop + this.headerHeight;
		for (int i = 0; i < cols.length; i++) {
			Line colBorderLeft = new Line();
			colBorderLeft.setColor(borderColor);
			colBorderLeft.setPattern(this.linePattern);
			colBorderLeft.setStartPoint(x, y);
			colBorderLeft.setEndPoint(x, this.pageHeight + this.offsetTop);
			colBorderLeft.drawOn(this.page);
			x += width;
		}

		Line borderRight = new Line();
		borderRight.setColor(borderColor);
		borderRight.setPattern(this.linePattern);
		borderRight.setStartPoint(x, y - this.multiHeight);
		borderRight.setEndPoint(x, this.offsetTop + this.pageHeight);
		borderRight.drawOn(this.page);

	}

	private void weekEventsDraw() throws Exception {
		double[] eventBorderColor = RGBColor.getColor(this.eventBorderColor);
		double[] eventColor = RGBColor.getColor(this.eventColor);
		double[] textColor = RGBColor.getColor(this.eventTextColor);
		SchedulerEvent[] events = this.parser.getEvents();
		for (int i = 0; i < events.length; i++) {
			double x = this.offsetLeft + this.leftScaleWidth + events[i].getX()
					* this.weekDayWidth / 100;
			double y = this.offsetTop + this.headerHeight + events[i].getY()
					* this.weekDayHeight / 100;
			double width = events[i].getWidth() * this.weekDayWidth / 100;
			double height = events[i].getHeight() * this.weekDayHeight / 100;
			String text = events[i].getText();
			String headerText = events[i].getHeaderText();
			String bgColor = events[i].getBackgroundColor();
			String color = events[i].getColor();

			Box eventCont = new Box();
			eventCont.setPosition(x, y);
			eventCont.setSize(width, height);
			eventCont.setColor(eventBorderColor);

			Box eventBg = new Box();
			eventBg.setPosition(0, 0);
			eventBg.setSize(width, height);
			if ((bgColor.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				eventBg.setColor(RGBColor.getColor(bgColor));
			} else {
				eventBg.setColor(eventColor);
			}
			eventBg.setFillShape(true);
			eventBg.placeIn(eventCont, 0, 0);

			Box headerCont = new Box();
			headerCont.setPosition(0, 0);
			headerCont.setSize(width, this.weekEventHeaderHeight);
			headerCont.setColor(eventBorderColor);
			headerCont.placeIn(eventCont, 0, 0);

			this.f1.setSize(7.4);
			TextLine headerTxt = new TextLine(this.f1, this.textWrap(
					headerText, width, this.f1));
			double text_x = (width - this.f1.stringWidth(headerTxt.getText())) / 2;
			double text_y = (this.weekEventHeaderHeight + this.f1.getSize()) / 2;
			headerTxt.placeIn(eventCont);
			headerTxt.setPosition(text_x, text_y);
			if ((color.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				headerTxt.setColor(RGBColor.getColor(color));
			} else {
				headerTxt.setColor(textColor);
			}

			double body_text_x = this.cellOffset;
			double body_text_y = this.weekEventHeaderHeight;
			this.f1.setSize(7.4);
			TextBox bodyTxt = new TextBox(f1, text, width - 2, height, 0, 1);
			bodyTxt.setPosition(x + body_text_x, y + body_text_y);
			bodyTxt.setDrawBackground(false);
			bodyTxt.setNoBorders();
			if ((color.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				bodyTxt.setFgColor(RGBColor.getColor(color));
			} else {
				bodyTxt.setFgColor(textColor);
			}
			bodyTxt.wrapText();

			eventBg.drawOn(this.page);
			eventCont.drawOn(this.page);
			headerCont.drawOn(this.page);
			headerTxt.drawOn(this.page);
			bodyTxt.drawOn(this.page);
		}

		// preparing scheme to calculate multiday position
		String[] cols = this.parser.weekColsParsing();
		int[] scheme = new int[cols.length];
		for (int i = 0; i < scheme.length; i++)
			scheme[i] = 0;

		events = this.parser.getMultidayEvents();
		double offset = 1;
		for (int i = 0; i < events.length; i++) {
			SchedulerEvent ev = events[i];
			int day = ev.getDay();
			int len = ev.getLen();
			String text = ev.getText();
			String bgColor = ev.getBackgroundColor();
			String color = ev.getColor();

			double width = len * this.weekDayWidth - 2 * offset;
			double height = this.monthEventHeight;
			double x = this.offsetLeft + this.leftScaleWidth + day
					* this.weekDayWidth + offset;
			double y = this.offsetTop + this.headHeight + scheme[day]
					* this.multidayLineHeight;

			Box eventCont = new Box();
			eventCont.setPosition(x, y);
			eventCont.setSize(width, height);
			eventCont.setColor(eventBorderColor);

			Box cont = new Box();
			cont.setPosition(x, y);
			cont.setSize(width, height);
			cont.setFillShape(true);
			if ((bgColor.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				cont.setColor(RGBColor.getColor(bgColor));
			} else {
				cont.setColor(eventColor);
			}
			cont.placeIn(eventCont, 0, 0);

			this.f1.setSize(7.4);
			TextLine txt = new TextLine(this.f1, this.textWrap(text, width,
					this.f1));
			double txt_x = this.cellOffset;
			double txt_y = (this.weekEventHeaderHeight + this.f1.getSize()) / 2;
			txt.placeIn(eventCont);
			txt.setPosition(txt_x, txt_y);
			if ((color.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				txt.setColor(RGBColor.getColor(color));
			} else {
				txt.setColor(textColor);
			}

			cont.drawOn(this.page);
			eventCont.drawOn(this.page);
			txt.drawOn(this.page);

			for (int j = day; j < day + len; j++)
				scheme[j]++;
		}
	}

	private void weekBwBordersDraw() throws Exception {
		double[] borderColor = RGBColor.getColor(this.lineColor);

		double x = this.offsetLeft;
		double y = this.offsetTop;
		this.Line(borderColor, x + this.leftScaleWidth, y, x + this.pageWidth,
				y);

		if (this.multiHeight > 0) {
			y = this.offsetTop + this.headerHeight - this.multiHeight;
			this.Line(borderColor, x + this.leftScaleWidth, y, x
					+ this.pageWidth, y);
			this.Line(borderColor, x, y, x, y + this.pageHeight
					- (this.headerHeight - this.multiHeight));
		}

		y = this.offsetTop + this.headerHeight;
		this.Line(borderColor, x, y, x + this.pageWidth, y);

		y = this.offsetTop + this.pageHeight;
		this.Line(borderColor, x, y, x + this.pageWidth, y);

		x = this.offsetLeft + this.leftScaleWidth;
		y = this.offsetTop;
		this.Line(borderColor, x, y, x, y + this.headerHeight);

		x = this.offsetLeft + this.pageWidth;
		this.Line(borderColor, x, y, x, y + this.headerHeight);

	}

	private Line Line(double[] color, double x1, double y1, double x2, double y2)
			throws Exception {
		Line border = this.prepareLine(color, x1, y1, x2, y2);
		border.drawOn(this.page);
		return border;
	}

	private Line Line(double[] color, double x1, double y1, double x2,
			double y2, Box parent) throws Exception {
		Line border = this.prepareLine(color, x1, y1, x2, y2);
		border.placeIn(parent);
		border.drawOn(this.page);
		return border;
	}

	private Line prepareLine(double[] color, double x1, double y1, double x2,
			double y2) throws Exception {
		Line border = new Line();
		border.setColor(color);
		border.setPattern(this.linePattern);
		border.setStartPoint(x1, y1);
		border.setEndPoint(x2, y2);
		return border;
	}

	private void timelineContainerDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] headerLineColor = RGBColor.getColor(this.headerLineColor);
		double[] scaleOneColor = RGBColor.getColor(this.scaleOneColor);
		String[] rows = this.parser.weekRowsParsing();
		double width = this.leftScaleWidth;
		double height = (this.pageHeight - this.headerHeight) / rows.length;
		this.weekDayHeight = height;
		double x = this.offsetLeft;
		double y = this.offsetTop + this.headerHeight;
		for (int i = 0; i < rows.length; i++) {
			Box cell = new Box();
			cell.setSize(width, height);
			cell.setColor(bgColor);
			cell.setFillShape(true);
			cell.setPosition(x, y);

			Box scaleOne = new Box();
			scaleOne.setPosition(x + width, y);
			scaleOne.setSize(this.pageWidth - width, height);
			scaleOne.setFillShape(true);
			scaleOne.setColor(scaleOneColor);

			cell.drawOn(this.page);
			scaleOne.drawOn(this.page);
			this.Line(headerLineColor, 0, 0, width, 0, cell);

			TextLine text = new TextLine(this.f1, this.textWrap(rows[i], width
					- 2 * this.cellOffset, this.f1));
			double text_x = (width - this.f1.stringWidth(text.getText())) / 2;
			double text_y = (height + this.f1.getSize()) / 2;
			text.setPosition(text_x, text_y);
			text.placeIn(cell);
			text.drawOn(this.page);

			y += height;
		}
		Line borderBottom = new Line();
		borderBottom.setColor(borderColor);
		borderBottom.setPattern(this.linePattern);
		borderBottom.setStartPoint(x, y);
		borderBottom.setEndPoint(x + width, y);
		borderBottom.drawOn(this.page);

		y = this.offsetTop + this.headerHeight + height;
		for (int i = 0; i < rows.length; i++) {
			Line colBorderBottom = new Line();
			colBorderBottom.setColor(borderColor);
			colBorderBottom.setPattern(this.linePattern);
			colBorderBottom.setStartPoint(
					this.offsetLeft + this.leftScaleWidth, y);
			colBorderBottom.setEndPoint(this.offsetLeft + this.pageWidth, y);
			colBorderBottom.drawOn(this.page);
			y += height;
		}

		String[] cols = this.parser.weekColsParsing();
		SecondScale[] secondScale = this.parser.getSecondScale();
		int len = secondScale.length>0 ? secondScale.length : cols.length;
		width = (this.pageWidth - this.leftScaleWidth) / len;
		x = this.offsetLeft + this.leftScaleWidth;
		y = this.offsetTop + this.headerHeight;
		for (int i = 0; i < len; i++) {
			Line colBorderLeft = new Line();
			colBorderLeft.setColor(borderColor);
			colBorderLeft.setPattern(this.linePattern);
			colBorderLeft.setStartPoint(x, y);
			colBorderLeft.setEndPoint(x, this.pageHeight + this.offsetTop);
			colBorderLeft.drawOn(this.page);
			x += width;
		}
		Line colBorderTop = new Line();
		colBorderTop.setColor(borderColor);
		colBorderTop.setPattern(this.linePattern);
		colBorderTop.setStartPoint(this.offsetLeft + this.leftScaleWidth,
				this.offsetTop + this.headerHeight);
		colBorderTop.setEndPoint(this.offsetLeft + this.pageWidth,
				this.offsetTop + this.headerHeight);
		colBorderTop.drawOn(this.page);

		Line borderRight = new Line();
		borderRight.setColor(borderColor);
		borderRight.setPattern(this.linePattern);
		borderRight.setStartPoint(x, this.offsetTop);
		borderRight.setEndPoint(x, this.offsetTop + this.pageHeight);
		borderRight.drawOn(this.page);
	}

	private void matrixContainerDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] headerLineColor = RGBColor.getColor(this.headerLineColor);
		double[] matrixEventColor = RGBColor.getColor(this.matrixEventColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		String[] rows = this.parser.weekRowsParsing();
		double height = (this.pageHeight - this.headerHeight) / rows.length;
		String[] cols = this.parser.weekColsParsing();
		SchedulerEvent[] events = this.parser.getEvents();

		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j <= cols.length; j++) {
				SchedulerEvent ev = events[i * (cols.length + 1) + j];
				String evBgColor = ev.getBackgroundColor();
				String evTextColor = ev.getColor();
				String text = ev.getText();
				double x = this.offsetLeft + Math.max(j - 1, 0)
						* this.weekDayWidth + (j != 0 ? 1 : 0)
						* this.leftScaleWidth;
				double y = this.offsetTop + this.headerHeight + i * height;

				double width = (j == 0) ? this.leftScaleWidth
						: this.weekDayWidth;

				Box cell = new Box();
				cell.setPosition(x, y);
				cell.setSize(width, height);
				cell.setFillShape(true);
				if ((evBgColor.compareTo("transparent") != 0)
						&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
								.compareTo("color") == 0))) {
					cell.setColor(RGBColor.getColor(evBgColor));
				} else {
					cell.setColor(matrixEventColor);
				}
				if (j == 0)
					cell.setColor(bgColor);
				cell.drawOn(this.page);

				// draw cell text
				this.f1.setSize((j == 0) ? 7.4 : 8.4);
				TextLine txt = new TextLine(this.f1, this.textWrap(text, width
						- 2 * this.cellOffset, this.f1));
				double text_x = (width - this.f1.stringWidth(txt.getText())) / 2;
				double text_y = (height + this.f1.getSize()) / 2;
				txt.setPosition(text_x, text_y);

				if ((evTextColor.compareTo("transparent") != 0)
						&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
								.compareTo("color") == 0))) {
					txt.setColor(RGBColor.getColor(evTextColor));
				} else {
					txt.setColor(textColor);
				}

				txt.placeIn(cell);
				txt.drawOn(this.page);

				// draw borders
				if (j == 0) {
					this.Line(headerLineColor, 0, 0, width, 0, cell);
				} else {
					this.Line(borderColor, 0, 0, width, 0, cell);
					this.Line(borderColor, 0, 0, 0, height, cell);
				}
			}
		}

		// border right
		Line border = new Line();
		border.setColor(borderColor);
		border.setPattern(this.linePattern);
		border.setStartPoint(this.offsetLeft + this.pageWidth, this.offsetTop);
		border.setEndPoint(this.offsetLeft + this.pageWidth, this.offsetTop
				+ this.pageHeight);
		border.drawOn(this.page);

		// border bottom
		border = new Line();
		border.setColor(borderColor);
		border.setPattern(this.linePattern);
		border.setStartPoint(this.offsetLeft, this.offsetTop + this.pageHeight);
		border.setEndPoint(this.offsetLeft + this.pageWidth, this.offsetTop
				+ this.pageHeight);
		border.drawOn(this.page);
	}


	private void timelineHeaderDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.headerLineColor);
		String[] cols = this.parser.weekColsParsing();
		SecondScale[] secondScale = this.parser.getSecondScale();

		double width = (this.pageWidth - this.leftScaleWidth) / (secondScale.length>0 ? secondScale.length : cols.length);
		double height = this.headerHeight;
		
		double[] widths = new double[cols.length];
		for (int i = 0; i < cols.length; i++)
			widths[i]=0;

		for (int i = 0; i < secondScale.length; i++) {
			widths[secondScale[i].getColumn()-1] += width;
		}
		
		this.weekDayWidth = width;
		double x = this.offsetLeft + this.leftScaleWidth;
		double y = this.offsetTop;
		for (int i = 0; i < cols.length; i++) {
			double w = secondScale.length > 0 ? widths[i] : width;
			
			Box cell = new Box();
			cell.setSize(w, height);
			cell.setColor(bgColor);
			cell.setFillShape(true);
			cell.setPosition(x, y);
			cell.drawOn(this.page);

			Line borderLeft = new Line();
			borderLeft.setColor(borderColor);
			borderLeft.setPattern(this.linePattern);
			borderLeft.setStartPoint(0, 0);
			borderLeft.setEndPoint(0, height);
			borderLeft.placeIn(cell);

			Line borderTop = new Line();
			borderTop.setColor(borderColor);
			borderTop.setPattern(this.linePattern);
			borderTop.setStartPoint(0, 0);
			borderTop.setEndPoint(w, 0);
			borderTop.placeIn(cell);

			borderLeft.drawOn(this.page);
			borderTop.drawOn(this.page);

			this.f1.setSize(7.4);
			TextLine text = new TextLine(this.f1, this.textWrap(cols[i], w
					- 2 * this.cellOffset, this.f1));
			double text_x = (w - this.f1.stringWidth(text.getText())) / 2;
			double text_y = (height + this.f1.getSize()) / 2;
			text.setPosition(text_x, text_y);
			text.placeIn(cell);
			text.drawOn(this.page);

			x += w;
		}


		if (secondScale.length > 0) {
			this.weekDayWidth = width;
			x = this.offsetLeft + this.leftScaleWidth;
			y = this.offsetTop + height;
			for (int i = 0; i < secondScale.length; i++) {
				double w = width;

				Box cell = new Box();
				cell.setSize(w, height);
				cell.setColor(bgColor);
				cell.setFillShape(true);
				cell.setPosition(x, y);
				cell.drawOn(page);

				Line borderLeft = new Line();
				borderLeft.setColor(borderColor);
				borderLeft.setPattern(this.linePattern);
				borderLeft.setStartPoint(0, 0);
				borderLeft.setEndPoint(0, height);
				borderLeft.placeIn(cell);

				Line borderTop = new Line();
				borderTop.setColor(borderColor);
				borderTop.setPattern(this.linePattern);
				borderTop.setStartPoint(0, 0);
				borderTop.setEndPoint(w, 0);
				borderTop.placeIn(cell);

				borderLeft.drawOn(this.page);
				borderTop.drawOn(this.page);

				this.f1.setSize(7.4);
				TextLine text = new TextLine(this.f1, this.textWrap(secondScale[i].getLabel(), w
						- 2 * this.cellOffset, this.f1));
				double text_x = (w - this.f1.stringWidth(text.getText())) / 2;
				double text_y = (height + this.f1.getSize()) / 2;
				text.setPosition(text_x, text_y);
				text.placeIn(cell);
				text.drawOn(this.page);

				x += w;
			}
			offsetTop += headerHeight;
		}
	}
	
	
	private void timelineEventsDraw() throws Exception {
		double[] eventBorderColor = RGBColor.getColor(this.eventBorderColor);
		double[] eventColor = RGBColor.getColor(this.eventColor);
		double[] textColor = RGBColor.getColor(this.eventTextColor);
		SchedulerEvent[] events = this.parser.getEvents();

		double offset = 1;
		for (int i = 0; i < events.length; i++) {
			SchedulerEvent ev = events[i];
			String text = ev.getText();
			String bgColor = ev.getBackgroundColor();
			String color = ev.getColor();

			double x = this.offsetLeft + this.leftScaleWidth + ev.getX()
					* this.weekDayWidth / 100 + offset;
			double y = this.offsetTop + this.headerHeight + ev.getY()
					* this.weekDayHeight / 100 + ev.getWeek()
					* this.weekDayHeight;
			double width = ev.getWidth() * this.weekDayWidth / 100;
			double height = this.monthEventHeight;

			Box eventCont = new Box();
			eventCont.setPosition(x, y);
			eventCont.setSize(width, height);
			eventCont.setColor(eventBorderColor);

			Box cont = new Box();
			cont.setPosition(x, y);
			cont.setSize(width, height);
			cont.setFillShape(true);
			if ((bgColor.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				cont.setColor(RGBColor.getColor(bgColor));
			} else {
				cont.setColor(eventColor);
			}
			cont.placeIn(eventCont, 0, 0);

			this.f1.setSize(7.4);
			TextLine txt = new TextLine(this.f1, this.textWrap(text, width,
					this.f1));
			double txt_x = this.cellOffset;
			double txt_y = (this.weekEventHeaderHeight + this.f1.getSize()) / 2;
			txt.placeIn(eventCont);
			txt.setPosition(txt_x, txt_y);
			if ((color.compareTo("transparent") != 0)
					&& ((this.profile.compareTo("fullcolor") == 0) || (this.profile
							.compareTo("color") == 0))) {
				txt.setColor(RGBColor.getColor(color));
			} else {
				txt.setColor(textColor);
			}

			cont.drawOn(this.page);
			eventCont.drawOn(this.page);
			txt.drawOn(this.page);
		}
	}

	private void yearDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] headerLineColor = RGBColor.getColor(this.headerLineColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		double[] eventColor = RGBColor.getColor(this.eventColor);
		double[] yearDayActiveColor = RGBColor
				.getColor(this.yearDayActiveColor);
		double[] yearDayInactiveColor = RGBColor
				.getColor(this.yearDayInactiveColor);
		SchedulerMonth[] monthes = this.parser.yearParsing();
		SchedulerEvent[] events = this.parser.getEvents();
		double width = (this.pageWidth - this.yearMonthOffsetLeft * 3) / 4;
		double height = (this.pageHeight - this.yearMonthOffsetTop * 2) / 3;
		double month_x = this.offsetLeft;
		double month_y = this.offsetTop;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 4; j++) {
				SchedulerMonth mon = monthes[i * 4 + j];
				String label = mon.getLabel();
				String[][] rows = mon.getRows();
				String[][] onlyDays = mon.getOnlyDays();
				Box monthCont = new Box();
				monthCont.setPosition(month_x, month_y);
				monthCont.setSize(width, height);
				monthCont.setFillShape(true);
				monthCont.setColor(bgColor);

				Box monthBg = new Box();
				monthBg.setPosition(0, 0);
				monthBg.setSize(width, height);
				monthBg.setColor(bgColor);
				monthBg.placeIn(monthCont, 0, 0);

				TextLine labelText = new TextLine(this.f1, this.textWrap(label,
						width, this.f1));
				double label_text_x = (width - this.f1.stringWidth(label)) / 2;
				double label_text_y = (this.yearMonthLabelHeight + this.f1
						.getSize()) / 2;
				labelText.placeIn(monthCont);
				labelText.setPosition(label_text_x, label_text_y);
				labelText.setColor(textColor);

				monthBg.drawOn(this.page);
				monthCont.drawOn(this.page);
				labelText.drawOn(this.page);

				double cell_width = width / 7;
				double cell_height = (height - this.yearMonthLabelHeight)
						/ rows.length;
				double cell_x = 0;
				double cell_y = this.yearMonthLabelHeight;
				for (int k = 0; k < rows.length; k++) {
					for (int l = 0; l < 7; l++) {
						Box cell = new Box();
						cell.setPosition(0, 0);
						cell.setSize(cell_width, cell_height);
						cell.setColor(borderColor);
						cell.setFillShape(true);
						cell.setPattern(this.linePattern);
						cell.placeIn(monthCont, cell_x, cell_y);

						int ind = this.getEventIndex(events, l, k - 1, i * 4
								+ j);
						if (k == 0) {
							cell.setColor(bgColor);
						} else {
							if (this.getActiveDay(onlyDays, k - 1, l) == true) {
								if (ind == -1) {
									cell.setColor(yearDayActiveColor);
								} else {
									SchedulerEvent ev = events[ind];
									String color = ev.getBackgroundColor();
									if ((color.compareTo("transparent") != 0)
											&& (this.profile.compareTo("color") == 0)) {
										cell.setColor(RGBColor.getColor(color));
									} else {
										cell.setColor(eventColor);
									}
								}
							} else {
								cell.setColor(yearDayInactiveColor);
							}
						}

						cell.drawOn(this.page);
						if (k == 0)
							this.Line(headerLineColor, 0, 0, cell_width, 0,
									cell);
						else
							this.Line(borderColor, 0, 0, cell_width, 0, cell);
						if (l > 0)
							if (k == 0)
								this.Line(headerLineColor, 0, 0, 0,
										cell_height, cell);
							else
								this.Line(borderColor, 0, 0, 0, cell_height,
										cell);

						TextLine cell_text = new TextLine(this.f1, rows[k][l]);
						double cell_text_x = (cell_width - this.f1
								.stringWidth(cell_text.getText())) / 2;
						double cell_text_y = (cell_height + this.f1.getSize()) / 2;
						cell_text.setPosition(cell_text_x, cell_text_y);
						cell_text.placeIn(cell);
						if (ind > -1) {
							SchedulerEvent ev = events[ind];
							String color = ev.getColor();
							if ((color.compareTo("transparent") != 0)
									&& (this.profile.compareTo("color") == 0))
								cell_text.setColor(RGBColor.getColor(color));
							else
								cell_text.setColor(textColor);
						} else
							cell_text.setColor(textColor);

						cell_text.drawOn(this.page);
						cell_x += cell_width;
					}
					cell_x = 0;
					cell_y += cell_height;
				}
				if (this.profile.compareTo("bw") == 0) {
					this.Line(headerLineColor, 0, 0, 0, height, monthCont);
					this.Line(headerLineColor, 0, 0, width, 0, monthCont);
					this.Line(headerLineColor, 0, height, width, height,
							monthCont);
					this.Line(headerLineColor, width, 0, width, height,
							monthCont);
				}
				month_x += this.yearMonthOffsetLeft + width;
			}
			month_x = this.offsetLeft;
			month_y += this.yearMonthOffsetTop + height;
		}
	}

	private void agendaHeaderDraw() throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.headerLineColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		String[] cols = this.parser.agendaColsParsing();
		Box headerCont = new Box();
		double width = this.pageWidth;
		double height = this.headerHeight;
		double x = this.offsetLeft;
		double y = this.offsetTop;
		headerCont.setSize(width, height);
		headerCont.setPosition(x, y);
		headerCont.setLineWidth(0.3);
		headerCont.setPattern(this.linePattern);
		headerCont.setColor(borderColor);

		Box headerBg = new Box();
		headerBg.setPosition(0, 0);
		headerBg.setSize(width, height);
		headerBg.setFillShape(true);
		headerBg.setColor(bgColor);
		headerBg.placeIn(headerCont, 0, 0);

		double date_width = this.agendaColOneWidth;
		double name_width = width - this.agendaColOneWidth;

		Line sep = new Line();
		sep.setColor(borderColor);
		sep.setPattern(this.linePattern);
		sep.setStartPoint(date_width, 0);
		sep.setEndPoint(date_width, this.headerHeight);
		sep.placeIn(headerCont);

		this.f1.setSize(7.4);
		TextLine dateText = new TextLine(this.f1, cols[0]);
		double date_text_x = (date_width - this.f1.stringWidth(dateText
				.getText())) / 2;
		double date_text_y = (height + this.f1.getSize()) / 2;
		dateText.setPosition(date_text_x, date_text_y);
		dateText.placeIn(headerCont);
		dateText.setColor(textColor);

		TextLine nameText = new TextLine(this.f1, cols[1]);
		double name_text_x = date_width
				+ (name_width - this.f1.stringWidth(nameText.getText())) / 2;
		double name_text_y = (height + this.f1.getSize()) / 2;
		nameText.setPosition(name_text_x, name_text_y);
		nameText.placeIn(headerCont);
		nameText.setColor(textColor);

		headerBg.drawOn(this.page);
		dateText.drawOn(this.page);
		nameText.drawOn(this.page);
		sep.drawOn(this.page);
	}

	private void agendaEventDraw() throws Exception {
		double[] scaleOneColor = RGBColor.getColor(this.scaleOneColor);
		double[] scaleTwoColor = RGBColor.getColor(this.scaleTwoColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		SchedulerEvent[] events = this.parser.getEvents();
		double width = this.pageWidth;
		double height = this.headerHeight;
		double x = this.offsetLeft;
		double y = this.offsetTop + this.headerHeight;
		this.f1.setSize(7.4);
		for (int i = 0; i < events.length; i++) {
			if (y + height > this.offsetTop + this.pageHeight) {
				Line borderBottom = new Line();
				borderBottom.setColor(borderColor);
				borderBottom.setPattern(this.linePattern);
				borderBottom.setStartPoint(x, y);
				borderBottom.setEndPoint(x + width, y);
				borderBottom.drawOn(this.page);
				this.page = new Page(this.pdf, Letter.PORTRAIT);
				this.pages.add(this.page);
				this.agendaHeaderDraw();
				y = this.offsetTop + this.headerHeight;
			}
			String headerText = events[i].getHeaderAgendaText();
			String text = events[i].getText();

			Box headerCont = new Box();
			headerCont.setSize(width, height);
			headerCont.setPosition(x, y);
			headerCont.setFillShape(true);
			headerCont.setPattern(this.linePattern);
			if (i % 2 == 0) {
				headerCont.setColor(scaleOneColor);
			} else {
				headerCont.setColor(scaleTwoColor);
			}

			double date_width = this.agendaColOneWidth;

			Box dateCont = new Box();
			dateCont.setPosition(0, 0);
			dateCont.setSize(date_width, height);
			dateCont.setColor(borderColor);
			dateCont.setPattern(this.linePattern);
			dateCont.placeIn(headerCont, 0, 0);

			TextLine dateText = new TextLine(this.f1, headerText);
			double date_text_x = (date_width - this.f1.stringWidth(dateText
					.getText())) / 2;
			double date_text_y = (height + this.f1.getSize()) / 2;
			dateText.setPosition(date_text_x, date_text_y);
			dateText.placeIn(headerCont);
			dateText.setColor(textColor);

			TextLine nameText = new TextLine(this.f1, text);
			double name_text_x = date_width + this.cellOffset;
			double name_text_y = (height + this.f1.getSize()) / 2;
			nameText.setPosition(name_text_x, name_text_y);
			nameText.placeIn(headerCont);
			nameText.setColor(textColor);

			Line borderLeft = new Line();
			borderLeft.setColor(borderColor);
			borderLeft.setPattern(this.linePattern);
			borderLeft.setStartPoint(0, 0);
			borderLeft.setEndPoint(0, height);
			borderLeft.placeIn(headerCont);

			Line borderTop = new Line();
			borderTop.setColor(borderColor);
			borderTop.setPattern(this.linePattern);
			borderTop.setStartPoint(0, 0);
			borderTop.setEndPoint(width, 0);
			borderTop.placeIn(headerCont);

			Line borderRight = new Line();
			borderRight.setColor(borderColor);
			borderRight.setPattern(this.linePattern);
			borderRight.setStartPoint(width, 0);
			borderRight.setEndPoint(width, height);
			borderRight.placeIn(headerCont);

			Line sep = new Line();
			sep.setColor(borderColor);
			sep.setPattern(this.linePattern);
			sep.setStartPoint(date_width, 0);
			sep.setEndPoint(date_width, height);
			sep.placeIn(headerCont);

			dateCont.drawOn(this.page);
			headerCont.drawOn(this.page);
			dateText.drawOn(this.page);
			nameText.drawOn(this.page);
			borderLeft.drawOn(this.page);
			borderTop.drawOn(this.page);
			sep.drawOn(this.page);
			borderRight.drawOn(this.page);
			y += height;
		}
		Line borderBottom = new Line();
		borderBottom.setColor(borderColor);
		borderBottom.setPattern(this.linePattern);
		borderBottom.setStartPoint(x, y);
		borderBottom.setEndPoint(x + width, y);
		borderBottom.drawOn(this.page);
	}

	private void agendaPagesDraw() throws Exception {
		java.util.Iterator<Page> itr = this.pages.iterator();
		int i = 1;
		while (itr.hasNext()) {
			Page page = itr.next();
			if ((this.profile.compareTo("bw") == 0)
					&& (this.parser.getMode().compareTo("month") != 0))
				this.monthBwBordersDraw();
			String str = this.pageNumTemplate;
			str = str.replace("{pageNum}", Integer.toString(i));
			str = str.replace("{allNum}", Integer.toString(this.pages.size()));
			TextLine text = new TextLine(this.f1, str);
			text.setColor(RGBColor.getColor(this.textColor));
			double x = this.pageWidth + this.offsetLeft
					- this.f1.stringWidth(str);
			double y = this.pageHeight + this.offsetTop + this.f1.getSize();
			text.setPosition(x, y);
			text.drawOn(page);
			i++;
		}
	}


	private void weekAgendaContainerDraw() throws Exception {
		String[] cols = this.parser.agendaColsParsing();
		double x = this.offsetLeft;
		double contWidth = (this.pageWidth)/2;
		double contHeight = (this.pageHeight)/3;
		this.contWidth = contWidth;
		this.contHeight = contHeight;
		
		
		for (int i = 0; i < 3; i++) {
			double y = this.offsetTop + contHeight*i;
			this.weekAgendarDayDraw(cols[i], contWidth, contHeight, x, y);
		}
		x += contWidth;
		for (int i = 0; i < 2; i++) {
			double y = this.offsetTop + contHeight*i;
			this.weekAgendarDayDraw(cols[i + 3], contWidth, contHeight, x, y);
		}
		
		double tallContHeight = contHeight/2;
		for (int i = 0; i < 2; i++) {
			double y = this.offsetTop + contHeight*2 + tallContHeight*i;
			this.weekAgendarDayDraw(cols[i + 5], contWidth, tallContHeight, x, y);
		}
		double[] headerLineColor = RGBColor.getColor(this.headerLineColor);
		for (int i = 0; i < 3; i++) {
			double y = this.offsetTop + contHeight*i;
			this.Line(headerLineColor, x, y, x, y + this.monthDayHeaderHeight);
		}
	}

	private void weekAgendarDayDraw(String name, double width, double height, double x, double y) throws Exception {
		double[] bgColor = RGBColor.getColor(this.bgColor);
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		this.f1.setSize(9.4);
		Box day_cont = new Box();
		day_cont.setSize(width, height);
		day_cont.setPosition(x, y);
		day_cont.setFillShape(false);
		day_cont.setPattern(this.linePattern);
		day_cont.setColor(bgColor);
		day_cont.drawOn(this.page);

		this.Line(borderColor, 0, 0, width, 0, day_cont);
		this.Line(borderColor, 0, 0, 0, height, day_cont);
		this.Line(borderColor, width, 0, width, height, day_cont);
		this.Line(borderColor, 0, height, width, height, day_cont);

		Box label_cont = new Box();
		label_cont.setSize(width, this.monthDayHeaderHeight);
		label_cont.setPosition(0, 0);
		label_cont.setFillShape(true);
		label_cont.setPattern(this.linePattern);
		label_cont.setColor(bgColor);
		label_cont.placeIn(day_cont, 0, 0);
		label_cont.drawOn(this.page);

		TextLine txt = new TextLine(this.f1, name);
		x = this.cellOffset + (width - cellOffset - this.f1.stringWidth(name))/2;
		y = (this.monthDayHeaderHeight + this.f1.getSize()) / 2;
		txt.setPosition(x, y);
		txt.placeIn(label_cont);
		txt.setColor(textColor);
		txt.drawOn(this.page);
	}

	private SchedulerEvent[] weekAgendaEventsDraw(SchedulerEvent[] events) throws Exception {
		double[] borderColor = RGBColor.getColor(this.lineColor);
		double[] textColor = RGBColor.getColor(this.textColor);
		int[] offsets = new int[7];
		for (int i = 0; i < offsets.length; i++)
			offsets[i] = 0;
		ArrayList<SchedulerEvent> rest = new ArrayList<SchedulerEvent>(); 

		for (int i = 0; i < events.length; i++) {
			SchedulerEvent event = events[i];
			int day = event.getDay();
			double cont_height = (day < 5) ? this.contHeight : this.contHeight/2;
			double cont_width = this.contWidth;
			double x;
			switch (day) {
				case 0:
				case 2:
				case 4:
					x = this.offsetLeft;
					break;
				default:
					x = this.offsetLeft + cont_width;
					break;
			}
			double cont_start_y = this.offsetTop + Math.floor(day/2)*this.contHeight - (day > 5 ? cont_height : 0);
			double offset = offsets[day]*this.weekAgendaEventHeight;
			double y = cont_start_y + this.monthDayHeaderHeight+ offset;

			if (cont_start_y + cont_height < y + this.weekAgendaEventHeight) {
				rest.add(event);
				continue;
			}

			Box ev = new Box();
			ev.setSize(contWidth, this.weekAgendaEventHeight);
			ev.setPosition(x, y);
			ev.setFillShape(false);
			ev.setPattern(this.linePattern);
			ev.setColor(borderColor);
			ev.drawOn(this.page);

			this.f1.setSize(9);
			String text = event.getText();
			TextLine txt = new TextLine(this.f1, text);
			x = this.cellOffset;
			y = (this.weekAgendaEventHeight + this.f1.getSize()) / 2;
			txt.setPosition(x, y);
			txt.placeIn(ev);
			txt.setColor(textColor);
			txt.drawOn(this.page);
			offsets[day]++;
		}
		SchedulerEvent[] events_list = new SchedulerEvent[rest.size()];
		for (int i = 0; i < rest.size(); i++)
			events_list[i] = rest.get(i);
		return events_list;
	}

	private void todayLabelDraw() throws Exception {
		Page p1 = this.pages.get(0);
		this.todayLabelDraw(p1);
	}

	private void todayLabelDraw(Page p1) throws Exception {
		this.f1.setSize(10);
		double[] textColor = RGBColor.getColor(this.textColor);
		String today = this.parser.getTodatLabel();
		TextLine todayText = new TextLine(this.f1, today);
		double today_x = this.offsetLeft;
		double today_y = this.offsetTop - this.cellOffset;
		todayText.setPosition(today_x, today_y);
		todayText.setColor(textColor);
		todayText.drawOn(p1);
	}
	
	private int getEventIndex(SchedulerEvent[] events, int day, int week,
			int month) throws IOException {
		for (int i = 0; i < events.length; i++) {
			int ev_day = events[i].getDay();
			int ev_week = events[i].getWeek();
			int ev_month = events[i].getMonth();
			if ((ev_day == day) && (ev_week == week) && (ev_month == month)) {
				return i;
			}
		}
		return -1;
	}

	private void outputPDF(HttpServletResponse resp) throws Throwable {
		resp.setContentType("application/pdf");
		this.pdf.flush();
	}

	private String textWrap(String text, double width, Font f) {
		if (text == null)
			return "";
		if (text.length() == 0)
			return text;
		if (f.stringWidth(text) <= width) {
			return text;
		}
		while ((f.stringWidth(text + "...") > width) && (text.length() > 0)) {
			text = text.substring(0, text.length() - 1);
		}
		return text + "...";
	}

	private boolean getActiveDay(String[][] rows, int row, int col) {
		boolean flag = true;
		int flagCount = 0;
		if (Integer.parseInt(rows[0][0]) == 1) {
			flag = true;
			flagCount = 1;
		} else {
			flag = false;
		}

		int prevDay = Integer.parseInt(rows[0][0]);
		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < rows[i].length; j++) {
				if ((Integer.parseInt(rows[i][j]) < prevDay) && (flagCount < 2)) {
					flag = !flag;
					flagCount++;
				}
				if ((i == row) && (j == col)) {
					return flag;
				}
				prevDay = Integer.parseInt(rows[i][j]);
			}
		}
		return flag;
	}

	public String getView() {
		return this.view;
	}

	private void setColorProfile(String profile) {
		if ((profile.compareTo("color") == 0)
				|| (profile.compareTo("fullcolor") == 0)) {
			this.bgColor = "C2D5FC";
			this.lineColor = "c1d4fc";
			this.headerLineColor = "FFFFFF";
			this.dayHeaderColor = "EBEFF4";
			this.dayBodyColor = "FFFFFF";
			this.dayHeaderColorInactive = "E2E3E6";
			this.dayBodyColorInactive = "ECECEC";
			this.eventColor = "FFE763";
			this.eventTextColor = "887A2E";
			this.scaleOneColor = "FCFEFC";
			this.scaleTwoColor = "DCE6F4";
			this.eventBorderColor = "B7A543";
			this.textColor = "000000";
			this.yearDayActiveColor = "EBEFF4";
			this.yearDayInactiveColor = "D6D6D6";
			this.multidayColor = "E1E6FF";
			this.matrixEventColor = "FFFFFF";
			this.watermarkTextColor = "8b8b8b";
		} else {
			if (profile.compareTo("gray") == 0) {
				this.bgColor = "D3D3D3";
				this.lineColor = "666666";
				this.headerLineColor = "FFFFFF";
				this.dayHeaderColor = "EEEEEE";
				this.dayBodyColor = "FFFFFF";
				this.dayHeaderColorInactive = "E3E3E3";
				this.dayBodyColorInactive = "ECECEC";
				this.eventColor = "DFDFDF";
				this.eventTextColor = "000000";
				this.scaleOneColor = "FFFFFF";
				this.scaleTwoColor = "E4E4E4";
				this.eventBorderColor = "9F9F9F";
				this.textColor = "000000";
				this.yearDayActiveColor = "EBEFF4";
				this.yearDayInactiveColor = "E2E3E6";
				this.multidayColor = "E7E7E7";
				this.matrixEventColor = "FFFFFF";
				this.watermarkTextColor = "8b8b8b";
			} else {
				this.bgColor = "FFFFFF";
				this.lineColor = "000000";
				this.headerLineColor = "000000";
				this.dayHeaderColor = "FFFFFF";
				this.dayBodyColor = "FFFFFF";
				this.dayHeaderColorInactive = "FFFFFF";
				this.dayBodyColorInactive = "FFFFFF";
				this.eventColor = "FFFFFF";
				this.eventTextColor = "000000";
				this.scaleOneColor = "FFFFFF";
				this.scaleTwoColor = "FFFFFF";
				this.eventBorderColor = "000000";
				this.textColor = "000000";
				this.yearDayActiveColor = "FFFFFF";
				this.yearDayInactiveColor = "FFFFFF";
				this.multidayColor = "FFFFFF";
				this.matrixEventColor = "FFFFFF";
				this.watermarkTextColor = "000000";
			}
		}
	}

	private void printHeader() throws Exception {
		Boolean header = this.parser.getHeader();
		if (header == true) {
			String fileName = this.pathToImgs + "/header.jpg";
			FileInputStream fis = new FileInputStream(fileName);
			Image im = new Image(this.pdf, fis, ImageType.JPEG);
			im.setPosition(this.offsetLeft, this.offsetTop);
			im.scaleBy(1);
			im.drawOn(this.page);
			this.pageHeight -= this.headerImgHeight;
			this.offsetTop += this.headerImgHeight;
		}
	}

	private void printFooter() throws Exception {
		Boolean footer = this.parser.getFooter();
		if (footer == true) {
			String fileName = this.pathToImgs + "/footer.jpg";
			FileInputStream fis = new FileInputStream(fileName);
			Image im = new Image(this.pdf, fis, ImageType.JPEG);
			im.setPosition(this.offsetLeft, this.pageHeight + this.offsetTop
					- this.footerImgHeight);
			im.scaleBy(1);
			im.drawOn(this.page);
			this.pageHeight -= this.footerImgHeight;
			this.offsetBottom += this.footerImgHeight;
		}
	}

	public void setWatermark(String mark) {
		watermark = mark;	
	}
}