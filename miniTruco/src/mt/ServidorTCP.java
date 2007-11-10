package mt;

/*
 * Copyright � 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 3 da 
 * Licen�a, ou (na sua opni�o) qualquer vers�o.
 *
 * Este programa � distribuido na esperan�a que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUA��O
 * a qualquer MERCADO ou APLICA��O EM PARTICULAR. Veja a Licen�a
 * P�blica Geral GNU para maiores detalhes.
 *
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU
 * junto com este programa, se n�o, escreva para a Funda��o do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

/**
 * Servidor no qual o celular est� conectado.
 * <p>
 * O tempo de vida dessa classe � o tempo de vida da conex�o com o servidor.
 * <p>
 * Ela tamb�m cuida da intera��o com o cliente quando o jogo n�o est� rolando,
 * exibindo listas de salas, prompts, etc., e atuando como CommandListener
 * destes di�logos
 * <p>
 * A tela dentro da sala � o Canvas em si. Os outros elementos s�o Forms e
 * Alerts
 * 
 * @author Chester
 * 
 */
public class ServidorTCP extends Canvas implements Runnable, CommandListener {

	private static final Command entrarSalaCommand = new Command("Entrar",
			Command.SCREEN, 1);

	private static final Command espiarSalaCommand = new Command("Espiar",
			Command.SCREEN, 2);

	private static final Command atualizarListaCommand = new Command(
			"Atualizar", Command.SCREEN, 3);

	private static final Command apelidoCommand = new Command("Mudar Apelido",
			Command.SCREEN, 4);

	private static final Command okApelidoCommand = new Command("Ok",
			Command.SCREEN, 1);

	private static final Command desconectarCommand = new Command(
			"Desconectar", Command.STOP, 999);

	/**
	 * Conex�o com o servidor remoto
	 */
	private StreamConnection conn;

	private OutputStream out;

	private InputStream in;

	/**
	 * Display da MIDlet (para poder exibir os forms)
	 */
	private Display display;

	/**
	 * Jogo rodando no servidor, se houver
	 */
	private JogoTCP jogo;

	/**
	 * Apelido atual do usu�rio
	 */
	public String apelido;

	private MiniTruco midlet;

	/**
	 * Cria o objeto, iniciando a conex�o com o servidor e botando uma thread
	 * pra monitorar essa conex�o
	 * 
	 * @param endereco
	 *            Endere�o do servidor, no formato host:porta
	 * @throws IOException
	 *             Caso n�o seja poss�vel conectar
	 */
	public ServidorTCP(String endereco, MiniTruco midlet) {

		// Guarda o display da MIDlet (vamos precisar dele pra mostrar forms e
		// alerts) e uma refer�ncia a ela (que vamos usar para devolver o
		// controle quando a sess�o acabar)
		this.display = Display.getDisplay(midlet);
		this.midlet = midlet;

		// Configura este Canvas para mostrar a mensagem de "aguarde"
		this.setCommandListener(this);
		this.addCommand(desconectarCommand);
		display.setCurrent(this);
		repaint();
		serviceRepaints();

		// Inicia a thread que ir� se conectar ao servidor e monitorar a conex�o
		this.URL = "socket://" + endereco;
		Thread t = new Thread(this);
		t.start();

	}

	TextField txtApelido;

	public void mostraFormApelido() {
		String sugestao = apelido;
		if (sugestao == null) {
			sugestao = Configuracoes.getConfiguracoes().nomeJogador;
			sugestao = "";
		}
		Form formApelido = new Form("Apelido");
		txtApelido = new TextField("Informe seu apelido", sugestao, 15,
				TextField.ANY);
		formApelido.append(txtApelido);
		formApelido.setCommandListener(this);
		formApelido.addCommand(okApelidoCommand);
		formApelido.addCommand(desconectarCommand);
		display.setCurrent(formApelido);
	}

	List listSalas;

	/**
	 * Exibe o formul�rio de escolha de sala
	 * 
	 * @param lista
	 *            retorno do comando "L" (qtde de pessoas em cada sala)
	 */
	public void mostraFormSalas(String lista) {
		listSalas = new List("Salas Disponiveis", List.IMPLICIT);
		StringBuffer sbDescSala = new StringBuffer();
		for (int i = 0; i < lista.length(); i += 2) {
			sbDescSala.setLength(0);
			sbDescSala.append("Sala ");
			sbDescSala.append(i / 2 + 1);
			sbDescSala.append(": ");
			char c = lista.charAt(i);
			switch (c) {
			case '0':
				sbDescSala.append("vazia");
				break;
			case '1':
				sbDescSala.append("1 pessoa");
				break;
			case '4':
				sbDescSala.append("lotada");
				break;
			default:
				sbDescSala.append(c);
				sbDescSala.append(" pessoas");
			}
			listSalas.append(sbDescSala.toString(), null);
		}
		listSalas.setCommandListener(this);
		listSalas.addCommand(entrarSalaCommand);
		listSalas.addCommand(espiarSalaCommand);
		listSalas.addCommand(apelidoCommand);
		listSalas.addCommand(desconectarCommand);
		listSalas.addCommand(atualizarListaCommand);
		display.setCurrent(listSalas);

	}

	/**
	 * Flag para permitir a finaliza��o da thread via menu
	 */
	private boolean estaVivo = true;

	private SalaTCP sala = new SalaTCP(this);

	/**
	 * Endere�o do servidor, no formato socket://host:porta
	 */
	private String URL;

	/**
	 * @return Sala em que estamos (ou que est� sendo espiada)
	 */
	public SalaTCP getSala() {
		return sala;
	}

	/**
	 * Monitora a conex�o, acionando os eventos conforme a necessidade
	 */
	public void run() {

		// Conecta no servidor. Se der erro, d� o alerta e volta.
		try {
			conn = (StreamConnection) Connector.open(this.URL);
			in = conn.openInputStream();
			out = conn.openOutputStream();
		} catch (IOException e) {
			midlet.novaMesa(false);
			midlet.startApp();
			alerta("Erro ao conectar", e.getMessage());
			return;
		}

		// Recupera as informa��es do serivdor
		enviaComando("W");

		// Loop principal: decodifica as notifica��es recebidas e as
		// processa ou encaminha ao jogador, conforme o caso
		int c;
		StringBuffer sbLinha = new StringBuffer();
		try {
			while (estaVivo && (c = in.read()) != -1) {
				if (c == '\n' || c == '\r') {
					if (sbLinha.length() > 0) {
						Jogo.log(sbLinha.toString());
						char tipoNotificacao = sbLinha.charAt(0);
						String parametros = sbLinha.delete(0, 2).toString();
						switch (tipoNotificacao) {
						case 'W':
							// Se n�o tiver recuperado a vers�o, desencana
							if (MiniTruco.versaoMidlet == null) {
								mostraFormApelido();
								break;
							}
							// Verifica se a vers�o � maior ou igual � exigida
							// pelo servidor
							String vMin = parametros;
							if (MiniTruco.versaoMidlet.compareTo(vMin) < 0) {
								alerta(
										"Jogo desatualizado",
										"Instale uma nova vers\u00E3o para jogar online",
										true);
								estaVivo = false;
							} else {
								// Ok, aprovado, vamos perguntar o apelido ao
								// jogador
								mostraFormApelido();
							}
							break;
						case 'N':
							// Setou o apelido, memoriza e vai pra lista de
							// salas
							apelido = parametros;
							Configuracoes conf = Configuracoes
									.getConfiguracoes();
							conf.nomeJogador = apelido;
							conf.salva();
							enviaComando("L");
							break;
						case 'L':
							// Recebeu a lista de salas, mostra na tela
							mostraFormSalas(parametros);
							break;
						case 'E':
							// Entrou na sala, atualiza o n�mero e mostra o
							// formul�rio (um I vai preencher os dados logo em
							// seguida)
							sala.numSala = Integer.parseInt(parametros);
							break;
						case 'S':
							// Saiu da sala: atualiza o n�mero, mata qualquer
							// jogo em andamento e vai para a lista de salas
							sala.numSala = 0;
							jogo = null;
							enviaComando("L");
							break;
						case 'I':
							// Recebeu info da sala. Enserra qualquer jogo em
							// andamento e limpa totalmente a mesa (pra n�o dar
							// sombra no jogo seuginte)
							if (jogo != null) {
								jogo = null;
								midlet.novaMesa(true);
							}

							// Valida e decodifica
							String[] tokens = split(parametros, ' ');
							if (tokens.length != 5) {
								alerta("Erro",
										"Status de sala invalido, verifique servidor: "
												+ parametros);
								break;
							}
							// numero da sala
							sala.numSala = Integer.parseInt(tokens[0]);
							// apelidos
							sala.jogadores = split(tokens[1], '|');
							// status de "quero jogar"
							sala.queroJogar = tokens[2];
							// posi��o do gerente
							sala.posGerente = Integer.parseInt(tokens[3]);
							// regras
							sala.regras = tokens[4];

							// Mostra, efetivamente, a sala com as infos
							// atualizadas
							display.setCurrent(this.sala);
							sala.atualizaSala();

							break;
						case 'P':
							// In�cio de partida, cria um jogo remoto e binda os
							// objetos-jogador apropriados a ele
							jogo = new JogoTCP(this);
							for (int i = 0; i <= 3; i++) {
								Jogador j;
								if (sala.jogadores[i].equals(apelido)) {
									j = new JogadorHumano(display, midlet.mesa);
								} else {
									j = new JogadorDummy();
								}
								j.setNome(sala.jogadores[i]);
								jogo.adiciona(j);
							}
							// Mostra a mesa e bota o jogo pra rodar
							midlet.mostraMenuAbertura(false);
							display.setCurrent(midlet.mesa);
							Thread t = new Thread(jogo);
							t.start();
							break;
						case 'M':
						case 'V':
						case 'J':
						case 'T':
						case 'D':
						case 'C':
						case 'R':
						case 'O':
						case 'G':
						case 'F':
						case 'H':
							// Estes eventos s�o do jogo, encaminha para ele (se
							// existir)
							if (jogo != null) {
								jogo.processaNotificacao(tipoNotificacao,
										parametros);
							}
							break;
						case 'X':
							if (parametros.equals("NE")) {
								mostraFormApelido();
								alerta("Erro",
										"Apelido j\u00E1 est\u00E1 em uso");
							} else if (parametros.equals("NI")) {
								mostraFormApelido();
								alerta("Erro", "Apelido inv\u00E1lido");
							} else if (parametros.equals("NO")) {
								display.setCurrent(this);
								mostraFormApelido();
							} else if (parametros.equals("CH")) {
								display.setCurrent(listSalas);
								alerta("Erro", "Sala lotada");
							} else if (parametros.equals("TT")) {
								alerta(
										"Conflito",
										"A manilha velha (fixa) exige baralho sujo.",
										true);
								enviaComando("I");
							} else {
								// Em caso de erro n�o-esperado, aborta o jogo
								alerta("Erro Inesperado", sbLinha.toString(),
										true);
								finalizaServidor();
							}
							break;
						}
					}
					sbLinha.setLength(0);
				} else {
					sbLinha.append((char) c);
				}
			}
		} catch (IOException e) {
			// � normal dar um erro de I/O quando o usu�rio pede pra desconectar
			// (pq o loop vai tentar ler a �ltima linha). S� vamos alertar se a
			// desconex�o foi for�ada.
			if (estaVivo) {
				alerta("Erro de I/O", e.getMessage(), true);
			}
			return;
		} finally {
			// Se saiu do loop e ainda estava "vivo", foi desconectado, avisa
			if (estaVivo) {
				alerta("Desconectado", "Voc� foi desconectado do servidor.",
						true);
			}
			finalizaServidor();
		}
	}

	public void finalizaServidor() {
		// Fecha tudo o que tem que fechar (se � que tem)
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (conn != null)
				conn.close();
		} catch (IOException e) {
			alerta("Erro Interno", e.getMessage());
		}

		// Sinaliza a finaliza��o para a thread
		estaVivo = false;

		// Volta para o menu principal
		midlet.novaMesa(false);
		midlet.startApp();

	}

	public void alerta(String titulo, String texto) {
		alerta(titulo, texto, false);
	}

	public void alerta(String titulo, String texto, boolean bloqueia) {
		Alert a = new Alert(titulo);
		a.setString(texto);
		a.setType(AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		display.setCurrent(a);
		if (bloqueia) {
			do {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Nevermind, apenas aguardando...
				}
			} while (display.getCurrent().equals(a));
		}
	}

	/**
	 * Envia um comando ao servidor
	 * 
	 * @param comando
	 *            texto do comando a enviar
	 */
	public void enviaComando(String comando) {
		try {
			out.write(comando.getBytes());
			out.write('\n');
			out.flush();
			Jogo.log(comando);
		} catch (IOException e) {
			alerta("Erro ao enviar", e.getMessage());
		}
	}

	/**
	 * Processa os comandos enviados para os forms/listas
	 * 
	 * @param cmd
	 * @param disp
	 */
	public void commandAction(Command cmd, Displayable disp) {
		mostraMsgAguarde();
		if (cmd == okApelidoCommand) {
			enviaComando("N " + txtApelido.getString());
		} else if ((cmd == entrarSalaCommand) || (cmd == List.SELECT_COMMAND)) {
			enviaComando("E " + (listSalas.getSelectedIndex() + 1));
		} else if (cmd == atualizarListaCommand) {
			enviaComando("L");
		} else if (cmd == apelidoCommand) {
			mostraFormApelido();
		} else if (cmd == espiarSalaCommand) {
			enviaComando("I " + (listSalas.getSelectedIndex() + 1));
		} else if (cmd == desconectarCommand) {
			finalizaServidor();
		}

	}

	/**
	 * Recupera a lista de salas no servidor.
	 * <p>
	 * 
	 * @return array onde cada elemento indica a quantidade de pessoas na sala
	 */
	// public int[] getOcupacaoSalas() {
	// enviaComando("L");
	// }
	/**
	 * Divide uma string com base em um separador (como o <code>split()</code>)
	 * da classe <code>String</code> do J2SE.
	 * <p>
	 * Ele efetua a opera��o em dois passos, mas esta abordagem tem a vantagem
	 * de n�o alocar nenhum objeto al�m das strings n�o-nulas do array.
	 */
	public static String[] split(String original, char separador) {
		// Fase 1: Contagem dos tokens (para dimensionar o array)
		int tamanho = original.length();
		int qtdeTokens = 1;
		for (int i = 0; i < tamanho; i++) {
			if (original.charAt(i) == separador) {
				qtdeTokens++;
			}
		}
		// Fase 2: Montagem do array
		String[] result = new String[qtdeTokens];
		int numTokenAtual = 0, inicioTokenAtual = 0;
		for (int i = 0; i <= tamanho; i++) {
			if ((i == tamanho) || (original.charAt(i) == separador)) {
				result[numTokenAtual] = original.substring(inicioTokenAtual, i);
				inicioTokenAtual = i + 1;
				numTokenAtual++;
			}
		}
		return result;

	}

	/**
	 * Fonte para a mensagem de "Aguarde"
	 */
	private static final Font fonteAguarde = Font.getFont(
			Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

	/**
	 * Desenha a mensagem de "aguarde" (quando n�o houver outro di�logo)
	 */
	protected void paint(Graphics g) {
		g.setColor(0x0000FF00);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(0x00FF0000);
		g.setFont(fonteAguarde);
		g.drawString("AGUARDE", getWidth() / 2, getHeight() / 2,
				Graphics.HCENTER | Graphics.BASELINE);
	}

	/**
	 * Permite que a mensagem de aguarde apare�a
	 */
	public void mostraMsgAguarde() {
		display.setCurrent(this);
	}

	/**
	 * Faz o jogo em andamento, se houver, ser abortado.
	 * <p>
	 * O processo de abort ir� levar o jogador ao lugar certo.
	 */
	public void abortaJogoAtual() {
		if (jogo != null) {
			jogo.abortaJogo(jogo.getJogadorHumano().getPosicao());
			jogo = null;
			display.setCurrent(this);
		}
	}

}
