package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import bean.Champion;
import model.ChampionDAO;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(urlPatterns = { "/insert-champion", "/select-champions", "/show-mastery" })
public class ChampionController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	String apiKey = "?api_key=RGAPI-667506a1-aa30-4c39-b582-9866b83be204";

	private Champion champion = new Champion();
	private ChampionDAO dao = new ChampionDAO();

	public ChampionController() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getServletPath();

		switch (action) {
		case "/insert-champion":
			inserirChampion(request, response);
			break;

		case "/select-champions":
			selecionarChampions(request, response);
			break;
			
		case "/show-mastery":
			mostrarMaestria(request, response);
			break;

		default:
			throw new IllegalArgumentException("Unexpected value: " + action);
		}
	}

	protected void inserirChampion(HttpServletRequest request, HttpServletResponse response) {

		champion.setChampionId(Integer.parseInt(request.getParameter("id")));
		champion.setChampionName(request.getParameter("name"));

		try {
			Part part = request.getPart("icon");
			champion.setChampionIcon(champion.base64Encoder(part));

			dao.insertChampion(champion);
		
			response.sendRedirect("championForm.jsp");
			
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		}
	}

	protected void selecionarChampions(HttpServletRequest request, HttpServletResponse response) {
		List<Champion> championList = dao.selectChampions();
		
		
		request.setAttribute("champions", championList);

		RequestDispatcher dispatcher = request.getRequestDispatcher("listarChampions.jsp");

		try {
			dispatcher.forward(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void mostrarMaestria(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		String encryptedId = (String) session.getAttribute("encryptedId");
		
		HttpsURLConnection connection = null;
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();

		try {
			URL url = new URL("https://br1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/"
					+ encryptedId + apiKey);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			int status = (connection.getResponseCode());

			if (status > 299) {
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				while ((line = reader.readLine()) != null) {
					responseContent.append(line);

				}
				reader.close();
			} else {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					responseContent.append(line);

				}
				reader.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			connection.disconnect();
		}
		
		request.setAttribute("maestrias", responseContent.toString());
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("listarMaestria.jsp");
		
		try {
			dispatcher.forward(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

}