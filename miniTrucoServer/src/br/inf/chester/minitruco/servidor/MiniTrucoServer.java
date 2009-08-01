package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright (c) 2007-2009 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 *  
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/**
 * Ponto de entrada do servidor do miniTruco.
 * 
 * @author Chester
 * 
 */
public class MiniTrucoServer {

	public static Map<String, String> SERVER = new HashMap<String, String>();

	/**
	 * Versão do servidor
	*/
	public static final String VERSAO_SERVER = "3.20.07";

	public static DateFormat dfStartup;

	public static Date dataStartup;

	public static String strDataStartup;

	public static void main(String[] args) {

		//server configuration [default values if not re-defined by *miniTrucoServer.cfg*]
		SERVER.put("SERVER_PORT", "6912"); // default port
		SERVER.put("SERVER_ROOMS", "10");
		SERVER.put("EVENTS_SHOW", "TRUE");
		SERVER.put("STATS_SHOW", "FALSE");
		SERVER.put("APPLET_GET_FILE", "TRUE");
		SERVER.put("TELNET_WELCOME_MSG", "> Welcome to miniTrucoServer. Authorized access only.");
		SERVER.put("STRATEGY","HAL");
		SERVER.put("MYSQL_ENABLED","FALSE");
		SERVER.put("SMTP_ENABLED","FALSE");
		SERVER.put("INVITATION_CODE_ENABLED","FALSE");
		SERVER.put("SMTP_HOST_NAME","localhost");
		SERVER.put("SMTP_AUTH_USER","user@localhost");
		SERVER.put("SMTP_AUTH_PWD","password");
		SERVER.put("REG_EMAIL_MSG_TXT", "Sua conta foi criada e já encontra-se ativa. Seu email foi registrado com sucesso.<BR><BR>Obrigado e boa diversão!");
		SERVER.put("REG_EMAIL_SUBJECT_TXT", "Benvindo!");
		SERVER.put("NEW_PASS_EMAIL_MSG_TXT_BEF_PASS", "Sua nova senha de acesso é:<BR><BR>");
		SERVER.put("NEW_PASS_EMAIL_MSG_TXT_AFT_PASS", "<BR><BR>Lembre-se de alterá-la para uma senha de sua preferência após login.<BR><BR>Boa diversão!");
		SERVER.put("NEW_PASS_EMAIL_SUBJECT_TXT", "Nova senha de acesso");
		SERVER.put("MYSQL_HOST", "localhost");
		SERVER.put("MYSQL_PORT", "3306");
		SERVER.put("MYSQL_DATABASE_NAME", "database_name");
		SERVER.put("MYSQL_USER", "root");
		SERVER.put("MYSQL_PASS", "");
		
		//.miniTrucoServer.cfg must be in the same directory than miniTrucoServer.jar
		//under linux, it must be on /etc
		String aFileName;
		if (System.getProperty("os.name").toLowerCase().equals("linux")) 
			aFileName = "/etc/miniTrucoServer.cfg";
		else 
			aFileName = "miniTrucoServer.cfg";
		File fFile = new File(aFileName);
		try {
			Scanner scanner = new Scanner(fFile);
			//first use a Scanner to get each line
			while (scanner.hasNextLine() ){
				//This simple default implementation expects simple name-value pairs, separated by an '=' sign.
				//use a second Scanner to parse the content of each line 
			    String line = scanner.nextLine();
			    Scanner scanner2 = new Scanner(line);
			    if (!line.substring(0,1).equals("#")) {
			    	scanner2.useDelimiter("=");
			    	if (scanner2.hasNext()){
			    		String constant = scanner2.next(); constant = constant.trim();
			    		String value = scanner2.next(); value = value.trim();
			    		SERVER.put(constant, value);
			    	}
			    }
			    scanner2.close();
			}
			scanner.close();
		}
		catch (Exception e) {
			// nothing to be done as default constants will be used
		}

		// some adjustments (replaces <BR> by newline):
		SERVER.put("REG_EMAIL_MSG_TXT", SERVER.get("REG_EMAIL_MSG_TXT").replaceAll("<BR>", "\n"));
		SERVER.put("NEW_PASS_EMAIL_MSG_TXT_BEF_PASS", SERVER.get("NEW_PASS_EMAIL_MSG_TXT_BEF_PASS").replaceAll("<BR>", "\n"));
		SERVER.put("NEW_PASS_EMAIL_MSG_TXT_AFT_PASS", SERVER.get("NEW_PASS_EMAIL_MSG_TXT_AFT_PASS").replaceAll("<BR>", "\n"));
		
		int numSalas = Integer.parseInt(SERVER.get("SERVER_ROOMS"));
		
		// Guarda a data de início do servidor num formato apropriado para HTTP
		// vide JogadorContectado.serveArquivosApplet
		
		dfStartup = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
				Locale.US);
		dataStartup = new Date();
		strDataStartup = dfStartup.format(dataStartup);
		
		String initText = "Servidor " + VERSAO_SERVER + " inicializado, pronto para escutar na porta "
		+ SERVER.get("SERVER_PORT");
		ServerLogger.evento(initText);
		// stats:
		// I (means server initialized) server_version server_port robot_strategy number_rooms
		String addTxt1, addTxt2, addTxt3;
		if (SERVER.get("MYSQL_ENABLED").equals("TRUE")) addTxt1 = " MYSQL_ENABLED"; else addTxt1=""; 
		if (SERVER.get("SMTP_ENABLED").equals("TRUE")) addTxt2 = " SMTP_ENABLED"; else addTxt2="";
		if (SERVER.get("INVITATION_CODE_ENABLED").equals("TRUE")) addTxt3 = " INVITATION_CODE_ENABLED"; else addTxt3="";
		String initText2="I " + VERSAO_SERVER + " " + SERVER.get("SERVER_PORT") + " " + SERVER.get("STRATEGY") + " " + numSalas + addTxt1 + addTxt2 + addTxt3;
		ServerLogger.stats(initText2);

		try {
			// Inicializa as salas
			Sala.inicializaSalas(numSalas);

			try {
				String p = SERVER.get("SERVER_PORT").toString();
				int pp = Integer.parseInt(p);
				ServerSocket s = new ServerSocket(pp);
				while (true) {
					Socket sCliente = s.accept();
					JogadorConectado j = new JogadorConectado(sCliente);
					Thread t = new Thread(j);
					t.start();
				}
			} catch (IOException e) {
				ServerLogger.evento(e,
						"Erro de I/O no ServerSocket, saindo do programa");
			}

		} finally {
			ServerLogger.evento("Servidor finalizado");
		}

	}

}
