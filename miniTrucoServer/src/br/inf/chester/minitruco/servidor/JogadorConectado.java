package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
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
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
 * 
 * @author Chester
 * 
 */
public class JogadorConectado extends Jogador implements Runnable {

	private static final String IF_MODIFIED_SINCE_HTTP_HEADER = "If-Modified-Since: ";

	/**
	 * Nomes de jogadores online (para evitar duplicidade)
	 */
	private static Set<String> nomes = new HashSet<String>();

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
			"/favicon.ico" };

	/*
	 * public void println(String mensagem) { if (out!=null) {
	 * out.println(mensagem); } }
	 */

	/**
	 * Aguarda comandos do jogador e os executa
	 */
	public void run() {
		ServerLogger.evento(this, "conectou");
		try {
			// Prepara o buffer de saída
			BufferedReader in = new BufferedReader(new InputStreamReader(
					cliente.getInputStream()));
			out = new PrintStream(cliente.getOutputStream());
			while (true) {
				String s = in.readLine();
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
				if (args[0].equals("GET") && (args.length > 1)) {
					// Log do GET (incluindo headers)
					do {
						ServerLogger.evento(this, "]" + s);
						s = in.readLine();
						/* TODO Arrumar esse código, não funciona a contento
						// Se o cliente tiver uma cópia no cache, tenta usar
						if (s.startsWith(IF_MODIFIED_SINCE_HTTP_HEADER)) {
							try {
								Date dataMax = MiniTrucoServer.dfStartup
										.parse(s
												.substring(IF_MODIFIED_SINCE_HTTP_HEADER
														.length()));
								System.out.println(MiniTrucoServer.dataStartup);
								System.out.println(dataMax);
								if (!MiniTrucoServer.dataStartup.after(dataMax)) {
									ServerLogger.evento("304 Not Modified");
									out.println("HTTP/1.0 304 Not Modified");
									out.flush();
									cliente.close();
									return;									
								}
							} catch (ParseException e) {
								// Se não conseguiu parsear, loga e desencana
								ServerLogger.evento(this,
										"!Cabecalho invalido: " + s
												+ ". Erro: " + e.getMessage());
							}
						}
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
			if (getNome() != null) {
				liberaNome(getNome());
			}
			ServerLogger.evento(this, "desconectou");
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
		if (this.getNome() != null) {
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

}
