import java.io.IOException;
import java.net.URLDecoder;
import javax.servlet.http.*;

import com.dhtmlx.scheduler.PDFWriter;



@SuppressWarnings("serial")
public class PDFGenerator extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		String xml;
		xml = req.getParameter("mycoolxmlbody");
		xml = URLDecoder.decode(xml, "UTF-8");
		PDFWriter pdf = new PDFWriter();
		pdf.generate(xml, resp);
	}
}
