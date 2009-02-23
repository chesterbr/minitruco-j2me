package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 *
 * Copyright © 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * (modo confronto de estratégias)
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
import java.io.InputStreamReader;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

/**
 * Ponto de entrada da aplicação no celular (MIDLet).
 * <p>
 * Os comentários de linha simples [IF_FULL] e [ENDIF_FULL] denotam seções de
 * código que só serão compiladas na versão "full" do jogo (e não na versão
 * "light", que visa um .jar menor). O build.xml garante isto.
 * 
 * @author Chester
 * 
 */
public class MiniTruco extends MIDlet implements CommandListener {

	static {
		// Carrega as configurações de idioma
		// (tem que ser feito no inicializador estático porque vários elementos
		// de forms dependem disso)
		try {
			Messages.carregaIdioma(Configuracoes.getConfiguracoes().idioma);
		} catch (Exception e) {
			// Se não der certo, desencana;
			e.printStackTrace();
		}
		// Agora que temos o idioma, podemos completar este array
		// (os outros elementos estão na inicialização static de
		// Jogador)
		Jogador.opcoesEstrategia[Jogador.ESTRATEGIAS.length] = Messages
				.getString("sortear"); //$NON-NLS-1$

	}

	/**
	 * Jogo (caso haja um) que está sendo jogado no momento
	 */
	Jogo jogoEmAndamento;

	/**
	 * Mesa onde está sendo exibido o jogo atual (caso haja um em andamento) ou
	 * a animação/tela de abertura
	 */
	public Mesa mesa;

	/**
	 * Jogador que está interagindo com o celular
	 */
	JogadorHumano jogadorHumano;

	/**
	 * Jogador que está substituindo o jogador humano no modo confronto de
	 * estratégias
	 */
	// [IF_FULL]
	JogadorBot jogadorBot;
	// [ENDIF_FULL]

	/**
	 * Formulário de configuração do jogo
	 */
	private Form formOpcoes;

	/**
	 * Tela Bluetooth (cliente ou servidor) em exibição no momento
	 */
	// [IF_FULL]
	public TelaBT telaBT;
	// [ENDIF_FULL]

	/**
	 * Tela do jogo TCP/IP em exibição no momento
	 */
	// [IF_FULL]
	public ServidorTCP servidor;
	// [ENDIF_FULL]

	/**
	 * Sub-menu que permite selecionar um item da ajuda
	 */
	private List listAjuda;

	/**
	 * Sub-menu que permite selecionar entre o cliente e o servidor bluetoth
	 */
	private List listBluetooth;

	/**
	 * Estratégias escolhidas para os jogadores CPU.
	 * <p>
	 * Os índices de 0 a 2 correspondem aos jogadores de 2 a 4 (o jogador 1 é
	 * humano, não entra aqui).
	 * <p>
	 * Os valores são índices do array de opções de estratégia.
	 * 
	 * @see Jogador#opcoesEstrategia
	 */
	private int[] estrategias;

	/**
	 * Estratégias escolhidas para os jogadores CPU no modo confronto de
	 * estratégias. O índice 0 corresponde à dupla B (horizontal na mesa) e o
	 * índice 1 corresponde à dupla A (vertical na mesa).
	 */
	// [IF_FULL]
	private int[] estrategiasModoCE;
	// [ENDIF_FULL]

	/**
	 * Variável indicativa de modo confronto de estratégias
	 */
	boolean modoCE = false;

	/**
	 * Variável indicativa do número máximo de partidas a serem jogadas no modo
	 * confronto de estratégias
	 * 
	 */
	// [IF_FULL]
	int nPartidasModoCE = 1;
	// [ENDIF_FULL]

	/**
	 * Formulário de configuração do modo confronto de estratégias
	 */
	// [IF_FULL]
	private Form formModoCE;
	// [ENDIF_FULL]

	// Listas de opções para menus de ajuda e bluetooth

	private static final String[] OPCOES_AJUDA = {
			Messages.getString("instrucoes"), //$NON-NLS-1$
			// [IF_FULL]
			Messages.getString("regrasTruco"), //$NON-NLS-1$
			// [ENDIF_FULL]
			Messages.getString("sobre"), Messages.getString("voltar") }; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String[] ARQUIVOS_AJUDA = {
			Messages.getString("Path.instrucoes.txt"), //$NON-NLS-1$
			// [IF_FULL]
			Messages.getString("Path.regras.txt"), //$NON-NLS-1$
			// [ENDIF_FULL]
			Messages.getString("Path.sobre.txt") }; //$NON-NLS-1$

	private static final String[] OPCOES_BLUETOOTH = {
			Messages.getString("criarjogo"), //$NON-NLS-1$
			Messages.getString("procurarjogo"), Messages.getString("voltar") }; //$NON-NLS-1$ //$NON-NLS-2$

	// Menu principal

	public static Command iniciarCommand = new Command(Messages
			.getString("iniciar"), //$NON-NLS-1$
			Command.SCREEN, 1);

	public static Command bluetoothComand = new Command(Messages
			.getString("bluetooth"), //$NON-NLS-1$
			Command.SCREEN, 2);

	public static Command tcpCommand = new Command(Messages
			.getString("internet"), Command.SCREEN, //$NON-NLS-1$
			3);

	public static Command idiomaCommand = new Command(Messages
			.getString("outro_idioma"), Command.SCREEN, //$NON-NLS-1$
			3);

	public static Command ajudaCommand = new Command(Messages
			.getString("ajuda"), Command.SCREEN, 5); //$NON-NLS-1$

	public static Command opcoesCommand = new Command(Messages
			.getString("opcoes"), //$NON-NLS-1$
			Command.SCREEN, 6);

	public static Command modoCECommand = new Command(Messages
			.getString("opcoesCE"), Command.SCREEN, 7); //$NON-NLS-1$

	public static Command sairProgramaCommand = new Command(Messages
			.getString("sair"), //$NON-NLS-1$
			Command.EXIT, 8);

	// Menus ajuda / bluetooth / alerta

	public static Command okBluetoothCommand = new Command(Messages
			.getString("ok"), //$NON-NLS-1$
			Command.SCREEN, 1);

	public static Command okItemAjudaCommand = new Command(Messages
			.getString("ok"), //$NON-NLS-1$
			Command.SCREEN, 1);

	public static Command voltarMenuCommand = new Command(Messages
			.getString("Menu.Voltar"), //$NON-NLS-1$
			Command.CANCEL, 5);

	public static Command okTexto = new Command(
			Messages.getString("ok"), Command.OK, 1); //$NON-NLS-1$

	public static Command okOpcoesCommand = new Command(Messages
			.getString("ok"), Command.OK, 1); //$NON-NLS-1$

	public static Command okModoCECommand = new Command(Messages
			.getString("ok"), Command.OK, 1); //$NON-NLS-1$

	public static Command okAvisoBTmodoCECommand = new Command(Messages
			.getString("ok"), //$NON-NLS-1$
			Command.OK, 1);

	// Menus in-game

	public static Command sairPartidaCommand = new Command(Messages
			.getString("encerrar"), //$NON-NLS-1$
			Command.STOP, 4);

	public static Command sairPartidaSemPerguntarCommand = new Command(Messages
			.getString("fim"), //$NON-NLS-1$
			Command.STOP, 4);

	public static Command aceitaCommand = new Command(Messages
			.getString("desce"), Command.OK, 1); //$NON-NLS-1$

	public static Command recusaCommand = new Command(Messages
			.getString("corre"), Command.CANCEL, //$NON-NLS-1$
			1);

	public static Command aceitaMao11Command = new Command(Messages
			.getString("joga"), Command.OK, //$NON-NLS-1$
			1);

	public static Command recusaMao11Command = new Command(Messages
			.getString("desiste"), //$NON-NLS-1$
			Command.CANCEL, 1);

	public static Command trucoCommand = new Command(Messages
			.getString("cmdTruco"), //$NON-NLS-1$
			Command.OK, 1);

	public static Command seisCommand = new Command(Messages
			.getString("cmdSeis"), //$NON-NLS-1$
			Command.OK, 1);

	public static Command noveCommand = new Command(Messages
			.getString("cmdNove"), //$NON-NLS-1$
			Command.OK, 1);

	public static Command dozeCommand = new Command(Messages
			.getString("cmdDoze"), //$NON-NLS-1$
			Command.OK, 1);

	public static Command simSairPartidaCommand = new Command(Messages
			.getString("sim"), //$NON-NLS-1$
			Command.OK, 1);

	public static Command naoSairPartidaCommand = new Command(Messages
			.getString("nao"), //$NON-NLS-1$
			Command.CANCEL, 2);

	public static Command mostraNomesJogadoresCommand = new Command(
			"X", Command.HELP, //$NON-NLS-1$
			999);

	// Elementos do formulario de opções

	private static final String[] OPCOES_VISUAL = {
			Messages.getString("cartasgrandes"), //$NON-NLS-1$
			Messages.getString("cartasanimadas") }; //$NON-NLS-1$

	private static final String[] OPCOES_REGRAS = {
			Messages.getString("baralholimpo"), //$NON-NLS-1$
			Messages.getString("manilhavelha") }; //$NON-NLS-1$

	private static final String[] OPCOES_DEBUG = {
			Messages.getString("exibirlog"), //$NON-NLS-1$
			// [IF_FULL]
			Messages.getString("confronto") //$NON-NLS-1$
	// [ENDIF_FULL]
	};

	private static final Image[] IMAGENS_VISUAL = { null, null };

	private static final Image[] IMAGENS_REGRAS = { null, null };

	// ** aumentar array aqui para cada nova estratégia incluída**
	// ** no momento: Willian, Sellani, Gasparotto **
	private static final Image[] IMAGENS_ESTRATEGIAS = { null, null, null, null };

	ChoiceGroup cgParceiro = new ChoiceGroup(
			Messages.getString("parceiro"), Choice.EXCLUSIVE, //$NON-NLS-1$
			Jogador.opcoesEstrategia, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgAdversarioEsq = new ChoiceGroup(
			Messages.getString("advesq"), Choice.EXCLUSIVE, //$NON-NLS-1$
			Jogador.opcoesEstrategia, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgAdversarioDir = new ChoiceGroup(
			Messages.getString("advdir"), Choice.EXCLUSIVE, //$NON-NLS-1$
			Jogador.opcoesEstrategia, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgVisual = new ChoiceGroup(
			Messages.getString("visual"), Choice.MULTIPLE, //$NON-NLS-1$
			OPCOES_VISUAL, IMAGENS_VISUAL);

	ChoiceGroup cgRegras = new ChoiceGroup(
			Messages.getString("regras"), Choice.MULTIPLE, //$NON-NLS-1$
			OPCOES_REGRAS, IMAGENS_REGRAS);

	ChoiceGroup cgDebug = new ChoiceGroup(
			Messages.getString("debug"), Choice.MULTIPLE, //$NON-NLS-1$
			OPCOES_DEBUG, IMAGENS_DEBUG);

	// [IF_FULL]
	TextField tfServidor = new TextField(
			Messages.getString("servidor"), null, 80, //$NON-NLS-1$
			TextField.URL);
	// [ENDIF_FULL]

	// Elementos exclusivos do formulário de opções do modo confronto de
	// estratégias

	private static final String[] OPCOES_NPARTIDAS = { "1", "3", "11", "35" };

	private static final Image[] IMAGENS_NPARTIDAS = { null, null, null, null };

	private static final Image[] IMAGENS_DEBUG = { null
	// [IF_FULL]
			, null
	// [ENDIF_FULL]
	};

	// [IF_FULL]
	ChoiceGroup cgModoCEDuplaA = new ChoiceGroup(Messages.getString("duplaA"), //$NON-NLS-1$
			Choice.EXCLUSIVE, Jogador.opcoesEstrategia, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgModoCEDuplaB = new ChoiceGroup(Messages.getString("duplaB"), //$NON-NLS-1$
			Choice.EXCLUSIVE, Jogador.opcoesEstrategia, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgModoCEnPartidas = new ChoiceGroup(Messages
			.getString("maxpart"), Choice.EXCLUSIVE, //$NON-NLS-1$
			OPCOES_NPARTIDAS, IMAGENS_NPARTIDAS);

	// [ENDIF_FULL]

	// PONTO DE ENTRADA DA MIDLET

	public MiniTruco() {

		// Cria uma nova mesa, pronta pra animar
		novaMesa(true);

		// Carrega as configurações da memória do celular
		// (ou as default, se não houver nada na memória)
		// [IF_FULL]
		Configuracoes confModoCE = Configuracoes.getConfiguracoesModoCE();
		estrategiasModoCE = confModoCE.estrategiasModoCE;
		nPartidasModoCE = confModoCE.nPartidasModoCE;
		// [ENDIF_FULL]
		Configuracoes conf = Configuracoes.getConfiguracoes();
		estrategias = conf.estrategias;
		Animador.setAnimacaoLigada(conf.animacaoLigada);
		cgRegras.setSelectedIndex(0, conf.baralhoLimpo);
		cgRegras.setSelectedIndex(1, conf.manilhaVelha);
		// [IF_FULL]
		tfServidor.setString(conf.servidor);
		// [ENDIF_FULL]
		Carta.setCartasGrandes(conf.cartasGrandes);
		mesa.montaBaralhoCenario();

		// Inicializa os "displayables" da aplicação (menos os do
		// multiplayer, que são responsabilidade da classe Servidor)
		// [IF_FULL]
		formModoCE = new Form(Messages.getString("opcoes_confronto")); //$NON-NLS-1$
		formModoCE.append(cgModoCEDuplaA);
		formModoCE.append(cgModoCEDuplaB);
		formModoCE.append(cgModoCEnPartidas);
		formModoCE.addCommand(okModoCECommand);
		formModoCE.setCommandListener(this);
		for (int i = 0; i < Jogador.opcoesEstrategia.length; i++) {
			cgModoCEDuplaA.setSelectedIndex(i, i == estrategiasModoCE[1]);
			cgModoCEDuplaB.setSelectedIndex(i, i == estrategiasModoCE[0]);
		}
		for (int i = 0; i < OPCOES_NPARTIDAS.length; i++) {
			cgModoCEnPartidas.setSelectedIndex(i, OPCOES_NPARTIDAS[i]
					.equals(Integer.toString(nPartidasModoCE)));
		}
		// [ENDIF_FULL]

		formOpcoes = new Form(Messages.getString("opcoes")); //$NON-NLS-1$
		formOpcoes.append(cgVisual);
		formOpcoes.append(cgRegras);
		formOpcoes.append(cgParceiro);
		formOpcoes.append(cgAdversarioEsq);
		formOpcoes.append(cgAdversarioDir);
		formOpcoes.append(cgDebug);
		// [IF_FULL]
		formOpcoes.append(tfServidor);
		// [ENDIF_FULL]
		formOpcoes.addCommand(okOpcoesCommand);
		formOpcoes.setCommandListener(this);
		for (int i = 0; i < Jogador.opcoesEstrategia.length; i++) {
			cgAdversarioDir.setSelectedIndex(i, i == estrategias[0]);
			cgParceiro.setSelectedIndex(i, i == estrategias[1]);
			cgAdversarioEsq.setSelectedIndex(i, i == estrategias[2]);
		}
		cgVisual.setSelectedIndex(0, Carta.isCartasGrandes());
		cgVisual.setSelectedIndex(1, Animador.isAnimacaoLigada());

		listAjuda = new List(
				Messages.getString("ajuda"), List.IMPLICIT, OPCOES_AJUDA, null); //$NON-NLS-1$
		listAjuda.addCommand(okItemAjudaCommand);
		listAjuda.addCommand(voltarMenuCommand);
		listAjuda.setCommandListener(this);

		if (isSuportaBluetooth()) {
			listBluetooth = new List(
					Messages.getString("jogoviabluetooth"), List.IMPLICIT, //$NON-NLS-1$
					OPCOES_BLUETOOTH, null);
			listBluetooth.addCommand(okBluetoothCommand);
			listBluetooth.addCommand(voltarMenuCommand);
			listBluetooth.setCommandListener(this);
		}

		versaoMidlet = getAppProperty("MIDlet-Version");

		mesa.animaAbertura();

	}

	/**
	 * Cria uma nova mesa (que será a base de uma nova partida)
	 * 
	 * @param vaiAnimar
	 *            Diz se vamos ter animação na mesa (false já mostra os
	 *            elementos no lugar)
	 */
	public void novaMesa(boolean vaiAnimar) {
		mesa = new Mesa(vaiAnimar);
		mostraMenuAbertura(true);
		mesa.setCommandListener(this);
	}

	/**
	 * Versão da midlet (é usada no "about...")
	 */
	public static String versaoMidlet;

	/**
	 * Mostra uma tela com o conteúdo de um arquivo-texto (salvo com o encoding
	 * ISO8859-1, que é o único obrigatório em J2ME), e um botão ok.
	 * <p>
	 * Se o arquivo-texto for o "/sobre.txt", precede com a versão da midlet
	 * (desde que estejamos rodando o pacote inteiro, enxergando o .jad)
	 * 
	 * @param titulo
	 *            Título da tela
	 * @param arqTexto
	 *            Nome do arquivo (preceder com "/")
	 */
	private void mostraArqTexto(String titulo, String arqTexto) {
		Form formTexto = new Form(titulo);
		StringBuffer str = new StringBuffer();
		try {
			InputStream is = this.getClass().getResourceAsStream(arqTexto);
			InputStreamReader isr = new InputStreamReader(is, "ISO8859_1");
			int ch;
			while ((ch = isr.read()) > -1) {
				str.append((char) ch);
			}
			if (isr != null)
				isr.close();
		} catch (IOException e) {
			alerta(Messages.getString("Erro"), e.toString()); //$NON-NLS-1$
			return;
		}
		if (arqTexto.equals(Messages.getString("Path.sobre.txt"))) { //$NON-NLS-1$
			if (versaoMidlet != null) {
				formTexto.append(new StringItem(null, Messages
						.getString("versao_prefixo") //$NON-NLS-1$
						+ versaoMidlet + "\n\n"));
			}
		}
		formTexto.append(new StringItem(null, str.toString()));

		formTexto.addCommand(okTexto);
		formTexto.setCommandListener(this);
		Display.getDisplay(this).setCurrent(formTexto);
	}

	/**
	 * Mostra/esconede o menu da tela de abertura
	 * 
	 * @param visivel
	 *            true para mostrar, false para esconder
	 */
	void mostraMenuAbertura(boolean visivel) {
		if (visivel) {
			mesa.addCommand(iniciarCommand);
			// [IF_FULL]
			if (isSuportaBluetooth() && !modoCE) {
				mesa.addCommand(bluetoothComand);
			} else {
				mesa.removeCommand(bluetoothComand);
			}
			mesa.addCommand(tcpCommand);
			mesa.addCommand(idiomaCommand);
			// [ENDIF_FULL]
			mesa.addCommand(ajudaCommand);
			mesa.addCommand(opcoesCommand);
			// [IF_FULL]
			if (modoCE) {
				mesa.addCommand(modoCECommand);
			} else {
				mesa.removeCommand(modoCECommand);
			}
			// [ENDIF_FULL]
			mesa.addCommand(sairProgramaCommand);
			mesa.removeComandoAposta();
			mesa.removeOpcoesAceite();
			mesa.removeOpcoesMao11();
			mesa.removeCommand(sairPartidaCommand);
		} else {
			mesa.removeCommand(sairProgramaCommand);
			mesa.removeCommand(iniciarCommand);
			mesa.removeCommand(tcpCommand);
			mesa.removeCommand(bluetoothComand);
			mesa.removeCommand(ajudaCommand);
			mesa.removeCommand(opcoesCommand);
			mesa.removeCommand(modoCECommand);
			mesa.removeCommand(idiomaCommand);
		}

	}

	protected void startApp() {
		Display.getDisplay(this).setCurrent(mesa);
	}

	protected void pauseApp() {
	}

	protected void destroyApp(boolean bool) {
		mesa.isAppRodando = false;
		// [IF_FULL]
		if (servidor != null) {
			servidor.finalizaServidor();
		}
		// [ENDIF_FULL]
	}

	/**
	 * Processa os comandos de menu (principal, in-game, ajuda, bluetooth e
	 * opções).
	 * <p>
	 * (é, ficou um certo balaio-de-gato, mas pelo menos economizou umas
	 * classes)
	 */
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == iniciarCommand) {
			// Checa se estamos no modo confronto de estratégias
			if (this.modoCE) {
				// [IF_FULL]
				// Inicializa novo jogo com 4 jogadores CPU com
				// as devidas estratégias escolhidas para dupla A e B
				Jogo jogo = new JogoLocal(cgRegras.isSelected(0), cgRegras
						.isSelected(1), this.nPartidasModoCE);
				// Dupla A jogador de baixo
				jogadorBot = new JogadorBot(
						Jogador.opcoesEstrategia[estrategiasModoCE[1]], Display
								.getDisplay(this), (Mesa) mesa);
				jogo.adiciona(jogadorBot);
				// Dupla B jogador à direita
				jogo.adiciona(new JogadorCPU(
						Jogador.opcoesEstrategia[estrategiasModoCE[0]]));
				// Dupla A jogador de cima
				jogo.adiciona(new JogadorCPU(
						Jogador.opcoesEstrategia[estrategiasModoCE[1]]));
				// Dupla B jogador à esquerda
				jogo.adiciona(new JogadorCPU(
						Jogador.opcoesEstrategia[estrategiasModoCE[0]]));
				iniciaJogo(jogo);
				// [ENDIF_FULL]
			} else {
				// Inicializa novo jogo e adiciona o jogador humano
				Jogo jogo = new JogoLocal(cgRegras.isSelected(0), cgRegras
						.isSelected(1));
				jogadorHumano = new JogadorHumano(Display.getDisplay(this),
						(Mesa) mesa);
				jogo.adiciona(jogadorHumano);
				// Adiciona os jogadores CPU com as estratégias escolhidas
				for (int i = 0; i <= 2; i++) {
					jogo.adiciona(new JogadorCPU(
							Jogador.opcoesEstrategia[estrategias[i]]));
				}
				iniciaJogo(jogo);
			}
			// [IF_FULL]
			// (os menus abaixo não existem sem bluetooth/TCP)

		} else if (cmd == bluetoothComand) {
			// Mostra o menu para o jogador escolher cliente ou servidor
			mostraMenuAbertura(false);
			Display.getDisplay(this).setCurrent(listBluetooth);
		} else if (cmd == tcpCommand) {
			// Inicia a conexão no servidor
			mostraMenuAbertura(false);
			servidor = new ServidorTCP(tfServidor.getString(), this);
		} else if (cmd == idiomaCommand) {
			// Troca o idioma atual, salva e sai do programa
			Configuracoes conf = Configuracoes.getConfiguracoes();
			if (conf.idioma.equals("English")) //$NON-NLS-1$
				conf.idioma = "Português"; //$NON-NLS-1$
			else
				conf.idioma = "English"; //$NON-NLS-1$
			conf.salva();
			mostraMenuAbertura(false);
			mesa.addCommand(sairProgramaCommand);
			alerta(Messages.getString("language_changed"), //$NON-NLS-1$
					Messages.getString("language_changed_msg")); //$NON-NLS-1$

			// [ENDIF_FULL]
		} else if (cmd == okAvisoBTmodoCECommand) {
			// Volta pra tela inicial
			Display.getDisplay(this).setCurrent(mesa);
			mesa.montaBaralhoCenario();
			mesa.repaint();
		} else if ((cmd == List.SELECT_COMMAND || cmd == okBluetoothCommand)
				&& Display.getDisplay(this).getCurrent().equals(listBluetooth)) {
			// Inicializa o componente (cliente ou servidor) escolhido
			// [IF_FULL]
			switch (listBluetooth.getSelectedIndex()) {
			case 0:
				telaBT = new ServidorBT(this);
				break;
			case 1:
				telaBT = new ClienteBT(this);
				break;
			default:
				Display.getDisplay(this).setCurrent(mesa);
				mostraMenuAbertura(true);
			}
			// [ENDIF_FULL]
		} else if (cmd == voltarMenuCommand) {
			Display.getDisplay(this).setCurrent(mesa);
			mostraMenuAbertura(true);
		} else if (cmd == sairPartidaCommand) {
			confirmaSairPartida();
		} else if (cmd == naoSairPartidaCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == simSairPartidaCommand
				|| cmd == sairPartidaSemPerguntarCommand) {
			// [IF_FULL]
			if (servidor != null) {
				if (cmd == sairPartidaSemPerguntarCommand) {
					// Fim de jogo normal
					servidor.enviaComando("I");
					Display.getDisplay(this).setCurrent(servidor);
				} else {
					// Jogador abortou espontaneamente
					servidor.abortaJogoAtual();
				}
				// Em qualquer caso, encerra o jogo sem sair e não segue
				encerraJogo(jogadorHumano.getPosicao(), false);
				return;
			} else if (telaBT instanceof ClienteBT) {
				// Se for um jogo cliente, derruba
				telaBT.encerraSessaoBT();
				telaBT = null;
			} else if (telaBT instanceof ServidorBT) {
				// Se for um jogo servidor, volta ao menu
				if (cmd == simSairPartidaCommand) {
					// Servidor abortou a partida, notifica
					((ServidorBT) telaBT).desconecta(-1);
				} else {
					// Fim de jogo normal, não notifica
					((ServidorBT) telaBT).desconecta(-2);
				}
				return;
			}
			if (this.modoCE)
				encerraJogo(jogadorBot.getPosicao(), true);
			else
				// [ENDIF_FULL]
				encerraJogo(jogadorHumano.getPosicao(), true);

		} else if (cmd == trucoCommand || cmd == seisCommand
				|| cmd == noveCommand || cmd == dozeCommand
				|| cmd == aceitaCommand || cmd == recusaCommand
				|| cmd == aceitaMao11Command || cmd == recusaMao11Command) {
			// Encaminha as ações in-game para a mesa.
			// (é, quando eu fiz esse jogo, o conceito de commandListener não
			// estava claro - a mesadeveria ser o listener de suas ações, agora
			// já foi).
			mesa.executaComando(cmd);
		} else if (cmd == sairProgramaCommand) {
			destroyApp(true);
			notifyDestroyed();
		} else if (cmd == ajudaCommand || cmd == okTexto) {
			Display.getDisplay(this).setCurrent(listAjuda);
		} else if ((cmd == List.SELECT_COMMAND || cmd == okItemAjudaCommand)
				&& Display.getDisplay(this).getCurrent().equals(listAjuda)) {
			// Mostra o arquivo de texto correspondente ao item do menu "ajuda"
			// que foi selecionado
			int itemAjuda = listAjuda.getSelectedIndex();
			if (itemAjuda > ARQUIVOS_AJUDA.length - 1) {
				Display.getDisplay(this).setCurrent(mesa);
			} else {
				mostraArqTexto(listAjuda.getString(itemAjuda),
						ARQUIVOS_AJUDA[itemAjuda]);
			}
		} else if (cmd == opcoesCommand) {
			Display.getDisplay(this).setCurrent(formOpcoes);
			// [IF_FULL]
		} else if (cmd == modoCECommand) {
			Display.getDisplay(this).setCurrent(formModoCE);
		} else if (cmd == okModoCECommand) {
			estrategiasModoCE[0] = cgModoCEDuplaB.getSelectedIndex();
			estrategiasModoCE[1] = cgModoCEDuplaA.getSelectedIndex();
			nPartidasModoCE = Integer
					.parseInt(OPCOES_NPARTIDAS[cgModoCEnPartidas
							.getSelectedIndex()]);

			// Guarda as opções na memória do celular
			Configuracoes confModoCE = Configuracoes.getConfiguracoesModoCE();
			confModoCE.estrategiasModoCE = estrategiasModoCE;
			confModoCE.nPartidasModoCE = nPartidasModoCE;
			confModoCE.salvaModoCE();

			// Volta pra tela anterior
			Display.getDisplay(this).setCurrent(mesa);
			mesa.montaBaralhoCenario();
			mesa.repaint();
			// [ENDIF_FULL]
		} else if (cmd == okOpcoesCommand) {
			// Seta as opções escolhidas no form (menos as regras,
			// que ficam guardadas no choiceGroup mesmo)
			estrategias[0] = cgAdversarioDir.getSelectedIndex();
			estrategias[1] = cgParceiro.getSelectedIndex();
			estrategias[2] = cgAdversarioEsq.getSelectedIndex();
			Carta.setCartasGrandes(cgVisual.isSelected(0));
			Animador.setAnimacaoLigada(cgVisual.isSelected(1));
			if (cgDebug.isSelected(0)) {
				Jogo.log = new String[6];
			} else {
				Jogo.log = null;
			}
			// [IF_FULL]
			this.modoCE = cgDebug.isSelected(1);
			if (mesa != null) {
				mesa.setModoCE(this.modoCE);
			}
			// [ENDIF_FULL]
			if (cgRegras.isSelected(0) && cgRegras.isSelected(1)) {
				// Se houver conflito, faz o ajuste e mantém o form
				alerta(
						Messages.getString("conflito_man_bar"), Messages.getString("conflito_man_bar_txt")); //$NON-NLS-1$ //$NON-NLS-2$
				cgRegras.setSelectedIndex(0, false);
			} else {

				// [IF_FULL]

				// Se o servidor estiver vazio, reverte ao default
				if (tfServidor.getString() == null
						|| tfServidor.getString().equals("")) {
					tfServidor.setString(Configuracoes.SERVIDOR_DEFAULT);
				}

				// Corrige problemas comuns no servidor (prefixo inválido, falta
				// de porta, case)
				tfServidor.setString(tfServidor.getString().toLowerCase());
				if (tfServidor.getString().startsWith("http://")) {
					tfServidor.setString(tfServidor.getString().substring(7));
				}
				if (tfServidor.getString().startsWith("www.")) {
					tfServidor.setString(tfServidor.getString().substring(4));
				}
				if (tfServidor.getString().indexOf(':') == -1) {
					tfServidor.setString(tfServidor.getString() + ':'
							+ Configuracoes.PORTA_DEFAULT);
				}
				// [ENDIF_FULL]

				// Guarda as opções na memória do celular
				Configuracoes conf = Configuracoes.getConfiguracoes();
				conf.estrategias = estrategias;
				conf.cartasGrandes = Carta.isCartasGrandes();
				conf.animacaoLigada = Animador.isAnimacaoLigada();
				conf.baralhoLimpo = cgRegras.isSelected(0);
				conf.manilhaVelha = cgRegras.isSelected(1);
				// [IF_FULL]
				conf.servidor = tfServidor.getString();
				// [ENDIF_FULL]
				conf.salva();

				// Volta pra tela anterior
				mostraMenuAbertura(true);
				Display.getDisplay(this).setCurrent(mesa);
				mesa.montaBaralhoCenario();
				mesa.repaint();
			}
		}
	}

	/**
	 * Inicia um jogo e o exibe.
	 * 
	 * @param jogo
	 *            Objeto jogo (já com os quatro jogadores)
	 */
	public void iniciaJogo(Jogo jogo) {
		jogoEmAndamento = jogo;
		mesa.removeComandoAposta();
		mostraMenuAbertura(false);
		mesa.addCommand(sairPartidaCommand);
		Thread t = new Thread(jogo);
		t.start();
		Display.getDisplay(this).setCurrent(mesa);
	}

	/**
	 * Encerra o jogo em andamento (se houver um) e volta para o menu principal
	 * 
	 * @param posicao
	 *            Posição do jogador que motivou o encerramento do jogo (0 caso
	 *            não haja jogo em andamento ou não se queira notificar nada)
	 * @param voltaAoMenu
	 *            se True, exibe a tela principal, caso contrário, fica onde
	 *            está
	 */
	public void encerraJogo(int posicao, boolean voltaAoMenu) {
		if (jogoEmAndamento != null) {
			if (posicao != 0) {
				jogoEmAndamento.abortaJogo(posicao);
			}
			jogoEmAndamento = null;
		}
		// [IF_FULL]
		if (this.modoCE)
			jogadorBot = null;
		else
			// [ENDIF_FULL]
			jogadorHumano = null;
		novaMesa(false);
		if (voltaAoMenu) {
			Display.getDisplay(this).setCurrent(mesa);
		}
	}

	/**
	 * Exibe uma mensagem de alerta.
	 * <p>
	 * Este método não bloqueia a execução. Ao final do alerta, a mesa é
	 * exibida.
	 * 
	 * @param titulo
	 *            Título da tela
	 * @param texto
	 *            Texto da mensagem
	 */
	public void alerta(String titulo, String texto) {
		Alert a = new Alert(titulo);
		a.setString(texto);
		a.setType(AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		Display.getDisplay(this).setCurrent(a);
	}

	private void confirmaSairPartida() {
		Form f = new Form("miniTruco");
		if (this.modoCE)
			f.append(new StringItem(null, Messages
					.getString("abandonaConfronto"))); //$NON-NLS-1$
		else
			f
					.append(new StringItem(null, Messages
							.getString("abandonaPartida"))); //$NON-NLS-1$
		f.addCommand(simSairPartidaCommand);
		f.addCommand(naoSairPartidaCommand);
		f.setCommandListener(this);
		Display.getDisplay(this).setCurrent(f);
	}

	/**
	 * Cache do teste de suporte a bluetooth.
	 * <p>
	 * (parece bobo, mas é melhor evitar testar toda hora)
	 * 
	 * @see MiniTruco#isSuportaBluetooth()
	 */
	private Boolean suportaBluetooth = null;

	/**
	 * Verifica se o dispostivo suporta bluetooth.
	 * <p>
	 * A resposta é cacheada na propriedade <code>suportaBluetooth</code>, e
	 * isso pode ser usado para "forçar" a exibição do menu através das opções
	 * de debug.
	 * 
	 * @return true se encontrou a classe
	 *         <code>javax.bluetooth.localdevice</code>
	 */
	private boolean isSuportaBluetooth() {
		if (suportaBluetooth == null) {
			suportaBluetooth = new Boolean(false);
			// [IF_FULL]
			try {
				Class.forName("javax.bluetooth.LocalDevice");
				suportaBluetooth = new Boolean(true);
			} catch (ClassNotFoundException e) {
				// Se der erro, o celular não suporta Bluetooth.
			}
			// [ENDIF_FULL]
		}
		return suportaBluetooth.booleanValue();
	}

	//
	// Textos para a gritaria Aqui pode-se incluir livremente novas opções uma
	// vez que o algoritmo checa o array todo
	//

	public static final String[] BALAO_TEXTOS_TRUCO = {
			Messages.getString("frase.t1"), //$NON-NLS-1$
			Messages.getString("frase.t2"), Messages.getString("frase.t3"), Messages.getString("frase.t4"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Messages.getString("frase.t5"), Messages.getString("frase.t6") }; //$NON-NLS-1$ //$NON-NLS-2$

	public static final String[] BALAO_TEXTOS_SEIS = {
			Messages.getString("frase.s1"), Messages.getString("frase.s2"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("frase.s3"), Messages.getString("frase.s4"), Messages.getString("frase.s5"), Messages.getString("frase.s6") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public static final String[] BALAO_TEXTOS_NOVE = {
			Messages.getString("frase.n1"), Messages.getString("frase.n2"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("frase.n3"), Messages.getString("frase.n4") }; //$NON-NLS-1$ //$NON-NLS-2$

	public static final String[] BALAO_TEXTOS_DOZE = {
			Messages.getString("frase.d1"), Messages.getString("frase.d2"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("frase.d3") }; //$NON-NLS-1$

	public static final String[] BALAO_TEXTOS_DESCE = {
			Messages.getString("frase.desce1"), //$NON-NLS-1$
			Messages.getString("frase.desce2"), Messages.getString("frase.desce3"), Messages.getString("frase.desce4"), Messages.getString("frase.desce5"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Messages.getString("frase.desce6") }; //$NON-NLS-1$

	public static final String[] BALAO_TEXTOS_RECUSA = {
			Messages.getString("frase.corre1"), //$NON-NLS-1$
			Messages.getString("frase.corre2"), Messages.getString("frase.corre3"), Messages.getString("frase.corre4") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String[] BALAO_TEXTOS_VENCEDOR = {
			Messages.getString("frase.vitoria1"), Messages.getString("frase.vitoria2"), Messages.getString("frase.vitoria3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String[] BALAO_TEXTOS_DERROTADO = {
			Messages.getString("frase.derrota1"), Messages.getString("frase.derrota2"), //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getString("frase.derrota3") }; //$NON-NLS-1$

	public static final String[] BALAO_TEXTOS_ACEITAMAO11 = {
			Messages.getString("frase.11aceita1"), //$NON-NLS-1$
			Messages.getString("frase.11aceita2"), Messages.getString("frase.11aceita3") }; //$NON-NLS-1$ //$NON-NLS-2$

	public static final String[] BALAO_TEXTOS_RECUSAMAO11 = {
			Messages.getString("frase.11recusa1"), Messages.getString("frase.11recusa2"), Messages.getString("frase.11recusa3"), Messages.getString("frase.11recusa4"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Messages.getString("frase.11recusa5") }; //$NON-NLS-1$

}