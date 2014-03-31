package simplecloud;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import simplecloud.HTMLFilter;

public class IndexServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet (HttpServletRequest request,
			HttpServletResponse response) {

		try {
			// Set the attribute and Forward to index.jsp
			//request.setAttribute ("servletName", "servletToJsp");
			getServletConfig().getServletContext().getRequestDispatcher(
					"teste.jsp").forward(request, response);
		} catch (Exception ex) {
			ex.printStackTrace ();
		}
	}    

}
