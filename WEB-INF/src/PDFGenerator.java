import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import javax.servlet.http.*;

import com.dhtmlx.scheduler.PDFWriter;



@SuppressWarnings("serial")
public class PDFGenerator extends HttpServlet {

	protected Boolean debug = false;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		String xml;
		xml = req.getParameter("mycoolxmlbody");
		xml = URLDecoder.decode(xml, "UTF-8");

		if (this.debug) {
			FileWriter fstream = new FileWriter("error_log.xml");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(xml);
			out.close();
		}

		PDFWriter pdf = new PDFWriter();
		pdf.generate(xml, resp);
	}
}
