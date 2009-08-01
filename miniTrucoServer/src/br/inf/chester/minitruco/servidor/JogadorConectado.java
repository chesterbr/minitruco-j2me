package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * mySQL interface implementation:
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import java.sql.*;

import mt.Carta;
import mt.Jogador;

/**
 * Representa um cliente conectado, dentro ou fora de um jogo.
 * <p>
 * A classe é capaz de processar os comandos do jogador, e, uma vez associada ao
 * jogo, interagir com ele.
 * <p>
 * A classe também responde ao comando <code>GET /applet.html</code> (na
 * verdade, a qualquer tipo de <code>GET</code>, mas é melhor usar esse para
 * compatibilidade futura), retornando o HTML que abre a applet hospedada em
 * <code>chester.inf.br</code>
 * <p>
 * Isso é feito para permitir que esta applet se conecte no servidor, sem
 * restrições de segurança (vide http://java.sun.com/sfaq/#socket).
 * <p>
 * 
 * @author Chester, Sandro (mySQL interface implementation)
 * 
 */
public class JogadorConectado extends Jogador implements Runnable {

	public static final String IF_MODIFIED_SINCE_HTTP_HEADER = "If-Modified-Since: ";

	/**
	 * Nomes de jogadores online (para evitar duplicidade)
	 */
	private static Set<String> nomes = new HashSet<String>();

	/**
	 * Nome dos convidados somente online
	 */
	private static Set<String> convidados = new HashSet<String>();
	
	/**
	 * Informa se o jogador está participando de um jogo
	 */
	public boolean jogando = false;

	/**
	 * Informa se o jogador autorizou o início da partida na sala
	 */
	public boolean querJogar = false;

	private Socket cliente;

	/**
	 * Uso interno. Para consultar a sala atual, use <code>getSala()</code>,
	 * e para adicionar/remover salas, use os métodos<code>adicionar()</code>
	 * e <code>remover()</code> de Sala.
	 * 
	 * @see JogadorConectado#getSala()
	 * @see Sala#adiciona(JogadorConectado)
	 * @see Sala#remove(JogadorConectado)
	 */
	int numSalaAtual = 0;

	/**
	 * Controle para auto disconexão após período muito longo de
	 *  inatividade
	 */
	int autoDiscoCounter = 0;
	
	/**
	 * Cria um novo jogador
	 * 
	 * @param cliente
	 *            socket-cliente através do qual o jogador se conectou
	 */
	public JogadorConectado(Socket cliente) {
		this.cliente = cliente;
	}

	/**
	 * Buffer de saída do jogador (para onde devemos "printar" os resultados dos
	 * comandos)
	 */
	private PrintStream out;

	/**
	 * Envia uma linha de texto para o cliente (tipicamente o resultado de um
	 * comando)
	 * 
	 * @param linha
	 *            linha de texto a enviar
	 */
	public void println(String linha) {
		out.print(linha);
		out.print("\r\n");
		ServerLogger.evento(this, linha);
	}

	private String[] ARQUIVOS_PERMITIDOS_HTTP = { "/applet.html",
			"/miniTruco.jar", "/microemulator.jar", "/mtskin.jar",
			"/favicon.ico", "/english.jar", "/applet_en.html" };
	
	/*
	 * public void println(String mensagem) { if (out!=null) {
	 * out.println(mensagem); } }
	 */

	/**
	 * Aguarda comandos do jogador e os executa
	 */
	public void run() {
		ServerLogger.evento(this, "conectou");
		// stats:
		// C (means new connection) registered_users guest_users available_seats_in_rooms
		String res = getStats();
		String info="C " + res;
		ServerLogger.stats(info);
		try {
			// Configura um timeout para evitar conexões presas
			//ServerLogger.evento(this, "timeout antes:" + cliente.getSoTimeout());
			//Timeout is set to 30secs
			// Alterei para 1 minuto
			//cliente.setSoTimeout(30000);
			int soTimeout = 60000;
			cliente.setSoTimeout(soTimeout);
			//ServerLogger.evento(this, "timeout depois:" + cliente.getSoTimeout());
			// Prepara o buffer de saída
			BufferedReader in = new BufferedReader(new InputStreamReader(
					cliente.getInputStream()));
			out = new PrintStream(cliente.getOutputStream());
			// welcome message
			this.println(MiniTrucoServer.SERVER.get("TELNET_WELCOME_MSG"));
			while (true) {
				String s = null;
				try {
					s = in.readLine();
				} catch (SocketTimeoutException e) {
					// A linha é só pra garantir que, no caso de uma conexão presa
					// o teste abaixo dela dê erro (seja no if, seja exception)
					println(""); 
					if (!cliente.isConnected()) {
						ServerLogger.evento("Desconexao detectada durante timeout");
						return;						
					}
					// rotina para disconexão automática após 
					// período de inatividade = soTimeout * 120
					// = 2 horas caso soTimeout = 1 minuto
					autoDiscoCounter++;
					if (autoDiscoCounter==120) {
						//ServerLogger.evento("Desconexao automatica por inatividade");
						return;
					}
					// expulsa da sala após período de inatividade de 20 minutos
					if (autoDiscoCounter==20) {
						Sala ss = getSala();
						if (ss != null) {
							char cc = 'S';
							try {
								Comando ccc = (Comando) Class.forName(
									"br.inf.chester.minitruco.servidor.Comando"
											+ cc).newInstance();
								String[] aa = "S".split(" ");
								ccc.executa(aa, this);
							} catch (Exception ee) {} 
						}
					}
					continue;
				} catch (IOException e) {
					// this handles mobile disconnections smoothly
					return;	
				}
				if (s == null) {
					// Desconexão
					return;
				}
				// Quebra a solicitação em tokens
				String[] args = s.split(" ");
				if (args.length == 0 || args[0].length() == 0) {
					continue;
				}

				// Se for um pedido de browser (para um arquivo da applet),
				// serve e desconecta
				if (args[0].equals("GET") && (args.length > 1) && MiniTrucoServer.SERVER.get("APPLET_GET_FILE").equals("TRUE")) {
					// Log do GET (incluindo headers)
					do {
						ServerLogger.evento(this, "]" + s);
						s = in.readLine();
						/*
						 * TODO Arrumar esse código, não funciona a contento //
						 * Se o cliente tiver uma cópia no cache, tenta usar if
						 * (s.startsWith(IF_MODIFIED_SINCE_HTTP_HEADER)) { try {
						 * Date dataMax = MiniTrucoServer.dfStartup .parse(s
						 * .substring(IF_MODIFIED_SINCE_HTTP_HEADER .length()));
						 * System.out.println(MiniTrucoServer.dataStartup);
						 * System.out.println(dataMax); if
						 * (!MiniTrucoServer.dataStartup.after(dataMax)) {
						 * ServerLogger.evento("304 Not Modified");
						 * out.println("HTTP/1.0 304 Not Modified");
						 * out.flush(); cliente.close(); return; } } catch
						 * (ParseException e) { // Se não conseguiu parsear,
						 * loga e desencana ServerLogger.evento(this,
						 * "!Cabecalho invalido: " + s + ". Erro: " +
						 * e.getMessage()); } }
						 */
					} while ((s != null) && (!s.equals("")));
					// Se livra dos parâmetros
					String nomeArq = args[1];
					int fimNome = nomeArq.indexOf('?');
					if (fimNome != -1)
						nomeArq = nomeArq.substring(0, fimNome);
					// Serve o arquivo
					serveArquivosApplet(nomeArq, out);
					cliente.close();
					return;
				}

				// Encontra a implementação do comando solicitado e chama
				if ((args[0] == null) || (args[0].length() != 1)) {
					continue;
				}
				autoDiscoCounter = 0; // comando enviado, então ressetamos o contador de inatividade
				char comando = Character.toUpperCase(args[0].charAt(0));
				try {
					Comando c = (Comando) Class.forName(
							"br.inf.chester.minitruco.servidor.Comando"
									+ comando).newInstance();
					c.executa(args, this);
				} catch (ClassNotFoundException e) {
					println("X CI");
				} catch (InstantiationException e) {
					println("X CI");
				} catch (IllegalAccessException e) {
					println("X CI");
				}
			}
		} catch (IOException e) {
			// Meio improvável de rolar, however...
			ServerLogger.evento(e, "Erro de I/O no loop principal do jogador");
		} finally {
			// Ao final, remove o usuário de qualquer sala em que esteja,
			// remove seu nome da lista de nomes usados e loga
			if (getSala() != null) {
				(new ComandoS()).executa(null, this);
			}
			if (!getNome().equals("unnamed")) {
				if (super.getLoginTime() != 0) { //this means it's a registered user
					long session_dur = (System.currentTimeMillis() - super.getLoginTime())/(1000*60);
					String query = "UPDATE users SET last_session_mnts = \"" + session_dur + "\" WHERE username=\"" + super.getNome() + "\"";
					mySQL(query);
				}
				liberaNome(getNome());
				removeConvidado(getNome());
			}
			ServerLogger.evento(this, "desconectou");
			// stats:
			// D (means disconnection) registered_users guest_users available_seats_in_rooms
			String res2 = getStats();
			String info2="D " + res2;
			ServerLogger.stats(info2);
		}

	}

	/**
	 * Serve (em formato HTTP) os arquivos que permitem inicializar a applet.
	 * <p>
	 * 
	 * @param nomeArq
	 *            nome do arquivo a ser servido (precedido de "/"), que deve
	 *            estar disponível no classpath e na lista de arquivos
	 *            permitidos
	 * @param out
	 *            stream para onde o HTML será servido
	 */
	private void serveArquivosApplet(String nomeArq, PrintStream out) {
		try {

			if (nomeArq.equals("/")) {
				nomeArq = "/applet.html";
			}

			boolean permitido = false;
			for (int i = 0; i < ARQUIVOS_PERMITIDOS_HTTP.length; i++) {
				if (ARQUIVOS_PERMITIDOS_HTTP[i].equals(nomeArq)) {
					permitido = true;
					break;
				}
			}

			InputStream is = getClass().getResourceAsStream(nomeArq);

			if (!permitido || (is == null)) {
				ServerLogger.evento("404 Not Found: " + nomeArq);
				out.println("HTTP/1.0 404 Not Found");
				out.println("Content-Length: 14");
				out.println();
				out.println("Nao Encontrado");
				return;
			}

			ServerLogger.evento("200 Ok: " + nomeArq);
			BufferedInputStream bis = new BufferedInputStream(is);
			out.println("HTTP/1.0 200 OK");
			if (nomeArq.endsWith(".html")) {
				out.println("Content-Type: text/html");
			} else if (nomeArq.endsWith(".jar")) {
				out.println("Content-Type: application/x-java-archive");
			} else if (nomeArq.endsWith(".ico")) {
				out.println("Content-Type: image/x-icon");
			}
			out.println("Server: miniTrucoServer/"
					+ MiniTrucoServer.VERSAO_SERVER);
			// Como não temos como recuperar as datas dos arquivos no JAR, vamos
			// usar a data de startup do servidor (que é razoavelmente estável e
			// só cresce nas CNTP)
			out.println("Last-modified:" + MiniTrucoServer.strDataStartup);
			out.println("Content-Length: " + bis.available());
			out.println();
			byte[] buf = new byte[4096];
			int numBytes;
			while ((numBytes = bis.read(buf)) != -1) {
				out.write(buf, 0, numBytes);
			}
			bis.close();
			is.close();
		} catch (IOException e) {
			ServerLogger.evento(e, "Erro de I/O ao servir " + nomeArq);
		}
	}

	// TODO: ver se não vai fazer falta
	// @Override
	// public void jogadorAceito(Jogador j, Jogo jogo) {
	// println("Y " + j.getPosicao());
	// }

	@Override
	public void cartaJogada(Jogador j, Carta c) {
		String param;
		if (c.isFechada()) {
			if (j.equals(this)) {
				param = " " + c + " T";
			} else {
				param = "";
			}
		} else {
			param = " " + c.toString();
		}
		println("J " + j.getPosicao() + param);
	}

	@Override
	public void inicioMao() {
		StringBuilder comando = new StringBuilder("M");
		for (int i = 0; i <= 2; i++)
			comando.append(" " + getCartas()[i]);
		if (!getSala().manilhaVelha) {
			comando.append(" " + getSala().getJogo().cartaDaMesa);
		}
		println(comando.toString());
	}

	@Override
	public void inicioPartida() {
		println("P " + getPosicao());
	}

	@Override
	public void vez(Jogador j, boolean podeFechada) {
		println("V " + j.getPosicao() + ' ' + (podeFechada ? 'T' : 'F'));
	}

	@Override
	public void pediuAumentoAposta(Jogador j, int valor) {
		println("T " + j.getPosicao() + ' ' + valor);
	}

	@Override
	public void aceitouAumentoAposta(Jogador j, int valor) {
		println("D " + j.getPosicao() + ' ' + valor);
	}

	@Override
	public void recusouAumentoAposta(Jogador j) {
		println("C " + j.getPosicao());
	}

	@Override
	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {
		println("R " + resultado + ' ' + jogadorQueTorna.getPosicao());
	}

	@Override
	public void maoFechada(int[] pontosEquipe, int[] vaquinhasNoPasto) {
		println("O " + pontosEquipe[0] + ' ' + pontosEquipe[1]);
	}

	@Override
	public void jogoFechado(int numEquipeVencedora, int[] vaquinhasNoPasto) {
		desvinculaJogo();
		println("G " + numEquipeVencedora);
		// updates database: table 'users'
		if (!getIsGuest()) {
			String query;
			if (numEquipeVencedora == super.getEquipe()) {
				// format query
				query = "UPDATE users SET wins = wins + 1 WHERE username=\"" + super.getNome() + "\"";
			} else {
				// format query
				query = "UPDATE users SET losses = losses + 1 WHERE username=\"" + super.getNome() + "\"";
			}
			try {
				mySQL(query);
			} catch (Exception e) {}
			// send update
			query = "SELECT wins, losses FROM users WHERE username=\"" + super.getNome() + "\"";
			String reply="ERROR";
			try {
				reply = mySQL(query);
			} catch (Exception e) {reply="ERROR";}
			if (!(reply.equals("ERROR"))) println("U UP " + reply);
		}
	}

	@Override
	public void decidiuMao11(Jogador j, boolean aceita) {
		println("H " + j.getPosicao() + (aceita ? " T" : " F"));
	}

	@Override
	public void informaMao11(Carta[] cartasParceiro) {
		StringBuilder sbComando = new StringBuilder("F ");
		for (int i = 0; i <= 2; i++) {
			sbComando.append(cartasParceiro[i]);
			if (i != 2)
				sbComando.append(' ');
		}
		println(sbComando.toString());
	}

	@Override
	public void jogoAbortado(int posicao) {
		desvinculaJogo();
		println("A " + posicao);
	}

	/**
	 * Desvincula o jogo do jogador, e, se necessário, da sala
	 * 
	 */
	private synchronized void desvinculaJogo() {
		querJogar = false;
		jogando = false;
		Sala s = getSala();
		if (s != null)
			s.liberaJogo();
	}

	/**
	 * Recupera a sala em que o jogado restá
	 * 
	 * @return objeto representando a sala, ou null se estiver fora de uma sala
	 */
	public Sala getSala() {
		return Sala.getSala(this.numSalaAtual);
	}

	@Override
	/**
	 * Atribui um nome ao jogador (apenas se não houver outro com o mesmo nome)
	 */
	public synchronized void setNome(String nome) {
		// Se já existir, desencana
		if (isNomeEmUso(nome)) {
			return;
		}
		// Se já tinha um nome, libera o seu uso
		if (!this.getNome().equals("unnamed")) {
			liberaNome(this.getNome());
		}
		// Seta o novo nome e evita novos usos
		super.setNome(nome);
		bloqueiaNome(nome);
	}

	/**
	 * Verifica se um nome está em uso por algum jogador
	 * 
	 * @param nome
	 *            nome a verificar
	 * @return true se já está em uso, false caso contrário
	 */
	public static boolean isNomeEmUso(String nome) {
		return nomes.contains(nome.toUpperCase());
	}

	/**
	 * Impede que um nome seja usado
	 * 
	 * @param nome
	 */
	public static void bloqueiaNome(String nome) {
		nomes.add(nome.toUpperCase());
	}

	/**
	 * Libera o uso de um nome
	 * 
	 * @param nome
	 */
	public static void liberaNome(String nome) {
		nomes.remove(nome.toUpperCase());
	}

	public String getIp() {
		return cliente.getInetAddress().getHostAddress();
	}

	/**
	 * Retorna versão do servidor
	 *
	 * @return string
	 */
	public String getVersaoServer() {
		return MiniTrucoServer.VERSAO_SERVER;
	}
	
	/**
	 * Retorna número de usuários conectados
	 *
	 * @return int
	 */
	public int getNumUsuariosConectados() {
		return nomes.size();
	}
	
	/**
	 * Adiciona nome à lista de convidados
	 * 
	 * @param nome
	 */
	public synchronized void adicionaConvidado(String nome) {
		convidados.add(nome.toUpperCase());
	}

	/**
	 * Remove nome da lista de convidados
	 * 
	 * @param nome
	 */
	public synchronized void removeConvidado(String nome) {
		convidados.remove(nome.toUpperCase());
	}
	
	/**
	 * Retorna número de usuários convidados conectados
	 *
	 * @return int
	 */
	public int getNumUsuariosConvidados() {
		return convidados.size();
	}
	
	public String getStats() {
		int nUsers = getNumUsuariosConectados();
		int nGue = getNumUsuariosConvidados();
		int nReg = nUsers - nGue;
		int nJogSalas = 0;
		for (int i=1;i<=Sala.getQtdeSalas();i++) {
			Sala s = Sala.getSala(i);
			nJogSalas=nJogSalas+s.getNumPessoas();
		}
		int nAv = (Sala.getQtdeSalas()*4)-nJogSalas;
		return nReg + " " + nGue + " " + nAv;
	}
	/**
	 * mySQL interface implementation
	 * 
	 * @param 'mysql command'
	 * 		'UPDATE', 'DELETE', 'SELECT', and 'INSERT' supported
	 *
	 * @return string
	 */
	public String mySQL(String args) {
		try {
			Statement stmt;
		    ResultSet rs;

		    //Register the JDBC driver for MySQL.
		    Class.forName("com.mysql.jdbc.Driver").newInstance();

		    //Define URL of database server
		    String url = "jdbc:mysql://" + MiniTrucoServer.SERVER.get("MYSQL_HOST") + ":" + MiniTrucoServer.SERVER.get("MYSQL_PORT") + "/" + MiniTrucoServer.SERVER.get("MYSQL_DATABASE_NAME");
		    
		    //Get a connection to the database
		    Connection con = DriverManager.getConnection(url, MiniTrucoServer.SERVER.get("MYSQL_USER"), MiniTrucoServer.SERVER.get("MYSQL_PASS"));

		    if(args.substring(0, 6).equals("SELECT")) {
		    	//Get a Statement object
		    	stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    
		    	//Query the database, storing the result
		    	// in an object of type ResultSet
		    	rs = stmt.executeQuery(args);

		    	//Check returned data	
		       	rs.next();
		       	String str="";
		       	try {
		       		if(!(args.indexOf("invitations") == 22)) {
		       			if (args.indexOf("password") > 0)
		       				// if we have a password, we can return everything
		       				str = rs.getString("username") + "|" + rs.getString("email") + "|" + rs.getString("city") + "|" + rs.getString("state") + "|" + rs.getDate("birth").toString() + "|" + rs.getString("sex") + "|" + rs.getInt("avatar") + "|" + rs.getInt("wins") + "|" + rs.getInt("losses") + "|" + rs.getInt("cool");
		       			// if we don't have a password, only 'public' data can be returned
		       			else if (args.indexOf("wins, losses") > 0)
		       				str = rs.getInt("wins") + "|" + rs.getInt("losses");
		       			else	
		       				str = rs.getString("username") + "|" + rs.getString("city") + "|" + rs.getString("state") + "|" + rs.getDate("birth").toString() + "|" + rs.getString("sex") + "|" + rs.getInt("avatar") + "|" + rs.getInt("wins") + "|" + rs.getInt("losses") + "|" + rs.getInt("cool");
		       		} else {
		       			str = rs.getString("available");
		       		}
		       	} catch(Exception e) {
		       		str = "ERROR";
		       	}
		    	return str;
		    }
		    
		    if(args.substring(0, 6).equals("INSERT") || args.substring(0, 6).equals("DELETE") || args.substring(0, 6).equals("UPDATE")) {
		    	//Get a Statement object
		    	stmt = con.createStatement();
		    
		    	//Update the database
		    	stmt.executeUpdate(args);
		    	
		    	return "OK";
		    	
		    }
		    
		    return "invalid argument";
		} catch(Exception e) {
			ServerLogger.evento(e,
			"Problema no acesso ao banco de dados mySQL");
			return e.toString();
		}
	}

}

