package mt;

/*
 * Copyright © 2006 Carlos Duarte do Nascimento (Chester)
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

/**
 * Servidor no qual o celular está conectado.
 * <p>
 * O tempo de vida dessa classe é o tempo de vida da conexão com o servidor.
 * <p>
 * Ela também cuida da interação com o cliente quando o jogo não está rolando,
 * exibindo listas de salas, prompts, etc., e atuando como CommandListener
 * destes diálogos
 * <p>
 * A tela dentro da sala é o Canvas em si. Os outros elementos são Forms e
 * Alerts
 * 
 * @author Chester
 * 
 */
public class ServidorTCP extends Canvas implements Runnable, CommandListener {

	private static final Command entrarSalaCommand = new Command(Messages.getString("entrar"), //$NON-NLS-1$
			Command.SCREEN, 1);

	private static final Command espiarSalaCommand = new Command(Messages.getString("espiar"), //$NON-NLS-1$
			Command.SCREEN, 2);

	private static final Command atualizarListaCommand = new Command(
			Messages.getString("atualizar"), Command.SCREEN, 3); //$NON-NLS-1$

	private static final Command okLoginCommand = new Command(Messages.getString("ok"), //$NON-NLS-1$
			Command.SCREEN, 1);
	
	private static final Command apelidoCommand = new Command(Messages.getString("mudarapelido"), //$NON-NLS-1$
			Command.SCREEN, 4);

	private static final Command okApelidoCommand = new Command(Messages.getString("ok"), //$NON-NLS-1$
			Command.SCREEN, 1);
	
	private static final Command okRegistradoCommand = new Command(Messages.getString("ok"), //$NON-NLS-1$
			Command.SCREEN, 1);

	private static final Command desconectarCommand = new Command(
			Messages.getString("desconectar"), Command.STOP, 999); //$NON-NLS-1$

	private static final Command voltarCommand = new Command(
			Messages.getString("voltar"), Command.SCREEN, 1); //$NON-NLS-1$

	/**
	 * Conexão com o servidor remoto
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
	 * Apelido atual do usuário
	 */
	public String apelido;

	/**
	 * Email do usuário (registrado)
	 */
	public String email;
	
	/**
	 * Infos adicionais de usuário (registrado)
	 */
	String cidade;
	String estado;
	String nascimento;
	String sexo;
	int avatar;
	int vitorias;
	int derrotas;
	
	private MiniTruco midlet;
	
	/**
	 * Controle
	 */
	private boolean loggedIn = false;
	private boolean registrado = false;

	/**
	 * Cria o objeto, iniciando a conexão com o servidor e botando uma thread
	 * pra monitorar essa conexão
	 * 
	 * @param endereco
	 *            Endereço do servidor, no formato host:porta
	 * @throws IOException
	 *             Caso não seja possível conectar
	 */
	public ServidorTCP(String endereco, MiniTruco midlet) {

		// Guarda o display da MIDlet (vamos precisar dele pra mostrar forms e
		// alerts) e uma referência a ela (que vamos usar para devolver o
		// controle quando a sessão acabar)
		this.display = Display.getDisplay(midlet);
		this.midlet = midlet;

		// Configura este Canvas para mostrar a mensagem de "aguarde"
		this.setCommandListener(this);
		this.addCommand(desconectarCommand);
		display.setCurrent(this);
		repaint();
		serviceRepaints();

		// Inicia a thread que irá se conectar ao servidor e monitorar a conexão
		this.URL = "socket://" + endereco;
		Thread t = new Thread(this);
		t.start();

	}

	private static final Image[] IMAGENS_LOGIN = { null, null };
	private static final String[] OPCOES_LOGIN = { Messages.getString("convidado"), Messages.getString("registrado") };
	ChoiceGroup cgLogin;
	TextField txtApelido;
	TextField txtRegistradoEmail;
	TextField txtRegistradoPass;
	
	public void mostraFormLogin() {
		loggedIn = false;
		Form formLogin = new Form(Messages.getString("login")); //$NON-NLS-1$
		cgLogin = new ChoiceGroup(
				Messages.getString("login_msg"), Choice.EXCLUSIVE, //$NON-NLS-1$
				OPCOES_LOGIN, IMAGENS_LOGIN);
		if (Configuracoes.getConfiguracoes().convidado==false) cgLogin.setSelectedIndex(1, true);
		formLogin.append(cgLogin);
		formLogin.setCommandListener(this);
		formLogin.addCommand(okLoginCommand);
		formLogin.addCommand(desconectarCommand);
		display.setCurrent(formLogin);
	}

	public void mostraFormApelido() {
		Configuracoes conf = Configuracoes.getConfiguracoes();
		String sugestao = apelido;
		if (sugestao == null) {
			sugestao = conf.nomeJogador;
			if (sugestao == null ) sugestao="";
		}
		Form formApelido = new Form(Messages.getString("apelido")); //$NON-NLS-1$
		txtApelido = new TextField(Messages.getString("apelido_msg"), sugestao, 20, //$NON-NLS-1$
				TextField.ANY);
		formApelido.append(txtApelido);
		formApelido.setCommandListener(this);
		formApelido.addCommand(okApelidoCommand);
		formApelido.addCommand(voltarCommand);
		display.setCurrent(formApelido);
	}
	
	public void mostraFormRegistrado() {
		Configuracoes conf = Configuracoes.getConfiguracoes();
		String sugestao = email;
		if (email == null) {
			sugestao = conf.email;
			if (sugestao == null ) sugestao="";
		}
		Form formRegistrado = new Form(Messages.getString("registrado")); //$NON-NLS-1$
		txtRegistradoEmail = new TextField(Messages.getString("registrado_email_msg"), sugestao, 50, //$NON-NLS-1$
				TextField.ANY);
		txtRegistradoPass = new TextField(Messages.getString("registrado_pass_msg"), "", 15, //$NON-NLS-1$
				TextField.PASSWORD);
		formRegistrado.append(txtRegistradoEmail);
		formRegistrado.append(txtRegistradoPass);
		formRegistrado.setCommandListener(this);
		formRegistrado.addCommand(okRegistradoCommand);
		formRegistrado.addCommand(voltarCommand);
		display.setCurrent(formRegistrado);
	}
	
	List listSalas;

	/**
	 * Exibe o formulário de escolha de sala
	 * 
	 * @param lista
	 *            retorno do comando "L" (qtde de pessoas em cada sala)
	 */
	public void mostraFormSalas(String lista) {
		listSalas = new List(Messages.getString("salas_disp"), List.IMPLICIT); //$NON-NLS-1$
		StringBuffer sbDescSala = new StringBuffer();
		for (int i = 0; i < lista.length(); i += 2) {
			sbDescSala.setLength(0);
			sbDescSala.append(Messages.getString("sala")); //$NON-NLS-1$
			sbDescSala.append(i / 2 + 1);
			sbDescSala.append(": ");
			char c = lista.charAt(i);
			switch (c) {
			case '0':
				sbDescSala.append(Messages.getString("vazia")); //$NON-NLS-1$
				break;
			case '1':
				sbDescSala.append(Messages.getString("1pessoa")); //$NON-NLS-1$
				break;
			case '4':
				sbDescSala.append(Messages.getString("lotada")); //$NON-NLS-1$
				break;
			default:
				sbDescSala.append(c);
				sbDescSala.append(Messages.getString("_pessoas")); //$NON-NLS-1$
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
	 * Flag para permitir a finalização da thread via menu
	 */
	private boolean estaVivo = true;

	private SalaTCP sala = new SalaTCP(this);

	/**
	 * Endereço do servidor, no formato socket://host:porta
	 */
	private String URL;

	/**
	 * @return Sala em que estamos (ou que está sendo espiada)
	 */
	public SalaTCP getSala() {
		return sala;
	}

	/**
	 * Monitora a conexão, acionando os eventos conforme a necessidade
	 */
	public void run() {

		// Conecta no servidor. Se der erro, dá o alerta e volta.
		try {
			conn = (StreamConnection) Connector.open(this.URL);
			in = conn.openInputStream();
			out = conn.openOutputStream();
		} catch (IOException e) {
			midlet.novaMesa(false);
			midlet.startApp();
			alerta(Messages.getString("erro_conectar"), e.getMessage()); //$NON-NLS-1$
			return;
		}

		// Recupera as informações do servidor
		enviaComando("W");

		// Loop principal: decodifica as notificações recebidas e as
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
							// Se não tiver recuperado a versão, desencana
							if (MiniTruco.versaoMidlet == null) {
								mostraFormLogin();
								break;
							}
							// Verifica se a versão é maior ou igual à exigida
							// pelo servidor
							// Achei melhor desabilitar, pois assim
							// podemos colocar strings na versão
							// sem comprometer aqui... [Sandro]
							//String vMin = parametros;
							//if (MiniTruco.versaoMidlet.compareTo(vMin) < 0) {
								//alerta(
										//Messages.getString("erro_versao"), //$NON-NLS-1$
										//Messages.getString("erro_versao_msg"), //$NON-NLS-1$
										//true);
								//estaVivo = false;
							//} else {
								// Ok, aprovado, vamos perguntar o apelido ao
								// jogador
								mostraFormLogin();
							//}
							break;
						case 'N':
							// Setou o apelido, memoriza e vai pra lista de
							// salas
							String[] Ntokens = split(parametros, '|');
							apelido = Ntokens[0];
							if (parametros.indexOf("|")>0) {
								registrado = true;
								email = Ntokens[1];
								cidade = Ntokens[2];
								estado = Ntokens[3];
								nascimento = Ntokens[4];
								sexo = Ntokens[5];
								avatar = Integer.parseInt(Ntokens[6]);
								vitorias = Integer.parseInt(Ntokens[7]);
								derrotas = Integer.parseInt(Ntokens[8]);
							}
							Configuracoes conf = Configuracoes.getConfiguracoes();
							if (!registrado) {
								conf.nomeJogador = apelido;
							} else {
								// o correto seria checar se a notif. N recebida 
								// é caso de mudança de nome (new nick) ou login,
								// pois estamos sobreescrevendo no arquivo de configuração à toa...
								// mas tudo bem, sem maiores impactos
								conf.email = email;
							}
							conf.salva();
							loggedIn = true;
							enviaComando("L");
							break;
						case 'L':
							// Recebeu a lista de salas, mostra na tela
							mostraFormSalas(parametros);
							break;
						case 'E':
							// Entrou na sala, atualiza o número e mostra o
							// formulário (um I vai preencher os dados logo em
							// seguida)
							sala.numSala = Integer.parseInt(parametros);
							break;
						case 'S':
							// Saiu da sala: atualiza o número, mata qualquer
							// jogo em andamento e vai para a lista de salas
							sala.numSala = 0;
							sala.jogadoresCounter = 1;
							jogo = null;
							enviaComando("L");
							break;
						case 'I':
							// Recebeu info da sala. Encerra qualquer jogo em
							// andamento e limpa totalmente a mesa (pra não dar
							// sombra no jogo seuginte)
							if (jogo != null) {
								jogo = null;
								midlet.novaMesa(true);
							}

							// Valida e decodifica
							String[] tokens = split(parametros, ' ');
							if (tokens.length != 5) {
								alerta(Messages.getString("erro"), //$NON-NLS-1$
										Messages.getString("erro_status_msg") //$NON-NLS-1$
												+ parametros);
								break;
							}
							// numero da sala
							sala.numSala = Integer.parseInt(tokens[0]);
							// apelidos
							sala.jogadores = split(tokens[1], '|');
							// status de "quero jogar"
							sala.queroJogar = tokens[2];
							// posição do gerente
							sala.posGerente = Integer.parseInt(tokens[3]);
							// regras
							sala.regras = tokens[4];

							// Mostra, efetivamente, a sala com as infos
							// atualizadas
							display.setCurrent(this.sala);
							sala.atualizaSala();
							// controle de broadcast de mensagens
							// para não sobrecarregar a rede com
							// mensagens redundantes; somente enviar
							// caso novos jogadores sentaram-se à mesa
							int cntr = 0;
							for (int i = 0; i <= 3; i++) {
								if (!sala.jogadores[i].equals("")) cntr++;
							}
							if (cntr > sala.jogadoresCounter) {
								enviaComando("B /AVT 99"); // specific avatar info for gTruco
								if (registrado) enviaComando("B /DAT "+cidade+"|"+
										estado+"|"+nascimento+"|"+sexo+"|"+avatar+"|"+
										vitorias+"|"+derrotas); // registered data
							}
							sala.jogadoresCounter=cntr;
							break;
						case 'P':
							// Início de partida, cria um jogo remoto e binda os
							// objetos-jogador apropriados a ele
							jogo = new JogoTCP(this);
							for (int i = 0; i <= 3; i++) {
								Jogador j;
								if (sala.jogadores[i].equals(apelido)) {
									j = new JogadorHumano(display, midlet.mesa);
									midlet.jogadorHumano = (JogadorHumano)j;
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
							// Estes eventos são do jogo, encaminha para ele (se
							// existir)
							if (jogo != null) {
								jogo.processaNotificacao(tipoNotificacao,
										parametros);
							}
							break;
						case 'X':
							if (parametros.equals("NE")) {
								mostraFormApelido();
								alerta(Messages.getString("erro"), //$NON-NLS-1$
										Messages.getString("erro_apelido_msg_emuso")); //$NON-NLS-1$
							} else if (parametros.equals("NI")) {
								mostraFormApelido();
								alerta(Messages.getString("erro"), Messages.getString("erro_apelido_msg_invalido")); //$NON-NLS-1$ //$NON-NLS-2$
							} else if (parametros.equals("NP")) {
								mostraFormRegistrado();
								alerta(Messages.getString("erro"), Messages.getString("erro_registrado_msg_invalido")); //$NON-NLS-1$ //$NON-NLS-2$
							} else if (parametros.equals("NO")) {
								display.setCurrent(this);
								mostraFormApelido();
							} else if (parametros.equals("CH")) {
								display.setCurrent(listSalas);
								alerta(Messages.getString("erro"), Messages.getString("erro_sala_lotada")); //$NON-NLS-1$ //$NON-NLS-2$
							} else if (parametros.equals("TT")) {
								alerta(
										Messages.getString("conflito_man_bar"), //$NON-NLS-1$
										Messages.getString("conflito_man_bar_txt"), //$NON-NLS-1$
										true);
								enviaComando("I");
							} else if (parametros.equals("DB")) {
								// nothing to be done
							} else {
								// Em caso de erro não-esperado, aborta o jogo
								alerta(Messages.getString("erro_inesperado"), sbLinha.toString(), //$NON-NLS-1$
										true);
								finalizaServidor();
							}
							break;
						case 'U':
							String[] Utokens = split(parametros, ' ');
							if (Utokens[0].equals("UP")) {
								String[] UUPtokens = split(Utokens[1], '|');
								vitorias = Integer.parseInt(UUPtokens[0]);
								derrotas = Integer.parseInt(UUPtokens[1]);
								enviaComando("B /VIT "+vitorias+"|"+derrotas);
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
			// É normal dar um erro de I/O quando o usuário pede pra desconectar
			// (pq o loop vai tentar ler a última linha). Só vamos alertar se a
			// desconexão foi forçada.
			if (estaVivo) {
				alerta(Messages.getString("erro_io"), e.getMessage(), true); //$NON-NLS-1$
			}
			return;
		} finally {
			// Se saiu do loop e ainda estava "vivo", foi desconectado, avisa
			if (estaVivo) {
				alerta(Messages.getString("erro_desconect"), Messages.getString("erro_desconect_msg"), //$NON-NLS-1$ //$NON-NLS-2$
						true);
			}
			finalizaServidor();
		}
	}

	public void finalizaServidor() {
		// Fecha tudo o que tem que fechar (se é que tem)
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (conn != null)
				conn.close();
		} catch (IOException e) {
			alerta(Messages.getString("erro_interno"), e.getMessage()); //$NON-NLS-1$
		}

		// Sinaliza a finalização para a thread
		estaVivo = false;

		// Volta para o menu principal
		midlet.servidor = null;
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
			alerta(Messages.getString("erro_envio"), e.getMessage()); //$NON-NLS-1$
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
			if (loggedIn) {
				if (!txtApelido.getString().equals(apelido))
					enviaComando("N " + txtApelido.getString());
				else
					enviaComando("L");
			}
			else 
				enviaComando("N " + txtApelido.getString());
		} else if (cmd == okRegistradoCommand) {
			email = txtRegistradoEmail.getString();
			String pass = txtRegistradoPass.getString();
			// MD5 hash
			MD5 md5 = new MD5();
			String hash = "";
			try {
				md5.Update(pass, null);
				hash = md5.asHex();
			} catch (Exception e) {}
		   if (hash.length() == 31) hash = "0" + hash; // precaution as some implementations show this bug
			enviaComando("N " + email + " " + hash);

		} else if (cmd == okLoginCommand) {
			Configuracoes conf = Configuracoes.getConfiguracoes();
			if (cgLogin.getSelectedIndex()==0) {
				conf.convidado = true;
				conf.salva();
				mostraFormApelido();
			} else {
				conf.convidado = false;
				conf.salva();
				mostraFormRegistrado();
			}
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
		} else if (cmd == voltarCommand) {
			if (loggedIn) 
				enviaComando("L");
			else 
				mostraFormLogin();
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
	 * Ele efetua a operação em dois passos, mas esta abordagem tem a vantagem
	 * de não alocar nenhum objeto além das strings não-nulas do array.
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
	 * Desenha a mensagem de "aguarde" (quando não houver outro diálogo)
	 */
	protected void paint(Graphics g) {
		g.setColor(0x0000FF00);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(0x00FF0000);
		g.setFont(fonteAguarde);
		g.drawString(Messages.getString("AGUARDE"), getWidth() / 2, getHeight() / 2, //$NON-NLS-1$
				Graphics.HCENTER | Graphics.BASELINE);
	}

	/**
	 * Permite que a mensagem de aguarde apareça
	 */
	public void mostraMsgAguarde() {
		display.setCurrent(this);
	}

	/**
	 * Faz o jogo em andamento, se houver, ser abortado.
	 * <p>
	 * O processo de abort irá levar o jogador ao lugar certo.
	 */
	public void abortaJogoAtual() {
		if (jogo != null) {
			jogo.abortaJogo(jogo.getJogadorHumano().getPosicao());
			jogo = null;
			display.setCurrent(this);
		}
	}

}
