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
 * publicada pela Fundação do Software Livre (FSF); na versão 2 da 
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
	JogadorBot jogadorBot;

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
	 * Sub-menu que permite selecionar um item da ajuda
	 */
	private List listAjuda;

	/**
	 * Sub-menu que permite selecionar entre o cliente e o servidor bluetoth
	 */
	private List listBluetooth;

	/**
	 * Estratégias escolhidas para os jogadores CPU. Os índices de 0 a 3
	 * correspondem aos jogadores de 2 a 4 (o jogador 1 é humano, não entra
	 * aqui).
	 */
	private String[] estrategias;

	/**
	 * Estratégias escolhidas para os jogadores CPU no modo confronto de
	 * estratégias. O índice 0 corresponde à dupla B (horizontal na mesa) e o
	 * índice 1 corresponde à dupla A (vertical na mesa).
	 */
	private String[] estrategiasModoCE;

	/**
	 * Variável indicativa de modo confronto de estratégias
	 * 
	 */
	boolean modoCE = false;

	/**
	 * Variável indicativa do número máximo de partidas a serem jogadas no modo
	 * confronto de estratégias
	 * 
	 */
	int nPartidasModoCE = 1;

	/**
	 * Formulário de configuração do modo confronto de estratégias
	 */
	private Form formModoCE;

	// Listas de opções para menus de ajuda e bluetooth

	private static final String[] OPCOES_AJUDA = { "Instru\u00E7\u00F5es",
			"Regras do Truco", "Sobre o miniTruco", "Voltar" };

	private static final String[] ARQUIVOS_AJUDA = { "/instrucoes.txt",
			"/regras.txt", "/sobre.txt" };

	private static final String[] OPCOES_BLUETOOTH = { "Criar jogo",
			"Procurar jogo", "Voltar" };

	// Menu principal

	public static Command iniciarCommand = new Command("Iniciar",
			Command.SCREEN, 1);

	public static Command bluetoothComand = new Command("Bluetooth",
			Command.SCREEN, 2);

	public static Command ajudaCommand = new Command("Ajuda", Command.SCREEN, 4);

	public static Command opcoesCommand = new Command("Op\u00E7\u00F5es",
			Command.SCREEN, 5);

	public static Command modoCECommand = new Command(
			"Op\u00E7\u00F5es modo CE", Command.SCREEN, 6);

	public static Command sairProgramaCommand = new Command("Sair",
			Command.EXIT, 7);

	// Menus ajuda / bluetooth / alerta

	public static Command okBluetoothCommand = new Command("Ok",
			Command.SCREEN, 1);

	public static Command okItemAjudaCommand = new Command("Ok",
			Command.SCREEN, 1);

	public static Command voltarMenuCommand = new Command("Voltar",
			Command.CANCEL, 5);

	public static Command okTexto = new Command("Ok", Command.OK, 1);

	public static Command okOpcoesCommand = new Command("Ok", Command.OK, 1);

	public static Command okModoCECommand = new Command("Ok", Command.OK, 1);

	public static Command okAvisoBTmodoCECommand = new Command("Ok",
			Command.OK, 1);

	// Menus in-game

	public static Command sairPartidaCommand = new Command("Encerrar",
			Command.STOP, 4);

	public static Command sairPartidaSemPerguntarCommand = new Command("Fim",
			Command.STOP, 4);

	public static Command aceitaCommand = new Command("Desce", Command.OK, 1);

	public static Command recusaCommand = new Command("Corre", Command.CANCEL,
			1);

	public static Command aceitaMao11Command = new Command("Joga", Command.OK,
			1);

	public static Command recusaMao11Command = new Command("Desiste",
			Command.CANCEL, 1);

	public static Command trucoCommand = new Command(Mesa.TEXTO_TRUCO,
			Command.OK, 1);

	public static Command seisCommand = new Command(Mesa.TEXTO_SEIS,
			Command.OK, 1);

	public static Command noveCommand = new Command(Mesa.TEXTO_NOVE,
			Command.OK, 1);

	public static Command dozeCommand = new Command(Mesa.TEXTO_DOZE,
			Command.OK, 1);

	public static Command simSairPartidaCommand = new Command("Sim",
			Command.OK, 1);

	public static Command naoSairPartidaCommand = new Command("N\u00E3o",
			Command.CANCEL, 2);

	// Elementos do formulario de opções

	static final String[] OPCOES_ESTRATEGIAS = { "Sellani", "Willian",
			"Gasparotto v1.1", "Sortear" };

	private static final String[] OPCOES_VISUAL = { "cartas grandes",
			"cartas animadas" };

	private static final String[] OPCOES_REGRAS = { "baralho limpo",
			"manilha velha" };

	private static final String[] OPCOES_DEBUG = { "exibir log",
			"confronto de estrat\u00E9gias" };

	private static final Image[] IMAGENS_VISUAL = { null, null };

	private static final Image[] IMAGENS_REGRAS = { null, null };

	// ** aumentar array aqui para cada nova estratégia incluída**
	// ** no momento: Willian, Sellani, Gasparotto v1.1 **
	private static final Image[] IMAGENS_ESTRATEGIAS = { null, null, null, null };

	ChoiceGroup cgParceiro = new ChoiceGroup("Parceiro", Choice.EXCLUSIVE,
			OPCOES_ESTRATEGIAS, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgAdversarioEsq = new ChoiceGroup(
			"Advers\u00E1rio \u00E0 esquerda", Choice.EXCLUSIVE,
			OPCOES_ESTRATEGIAS, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgAdversarioDir = new ChoiceGroup(
			"Advers\u00E1rio \u00E0 direita", Choice.EXCLUSIVE,
			OPCOES_ESTRATEGIAS, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgVisual = new ChoiceGroup("Visual", Choice.MULTIPLE,
			OPCOES_VISUAL, IMAGENS_VISUAL);

	ChoiceGroup cgRegras = new ChoiceGroup("Regras", Choice.MULTIPLE,
			OPCOES_REGRAS, IMAGENS_REGRAS);

	ChoiceGroup cgDebug = new ChoiceGroup("Debug", Choice.MULTIPLE,
			OPCOES_DEBUG, IMAGENS_DEBUG);

	// Elementos exclusivos do formulário de opções do modo confronto de
	// estratégias

	private static final String[] OPCOES_NPARTIDAS = { "1", "3", "11", "35" };

	private static final Image[] IMAGENS_NPARTIDAS = { null, null, null, null };

	private static final Image[] IMAGENS_DEBUG = { null, null };

	ChoiceGroup cgModoCEDuplaA = new ChoiceGroup("Dupla A (vertical)",
			Choice.EXCLUSIVE, OPCOES_ESTRATEGIAS, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgModoCEDuplaB = new ChoiceGroup("Dupla B (horizontal)",
			Choice.EXCLUSIVE, OPCOES_ESTRATEGIAS, IMAGENS_ESTRATEGIAS);

	ChoiceGroup cgModoCEnPartidas = new ChoiceGroup(
			"N\u00famero m\u00e1ximo de partidas", Choice.EXCLUSIVE,
			OPCOES_NPARTIDAS, IMAGENS_NPARTIDAS);

	// PONTO DE ENTRADA DA MIDLET

	public MiniTruco() {

		// Cria uma nova mesa, pronta pra animar
		novaMesa(true);

		// Carrega as configurações da memória do celular
		// (ou as default, se não houver nada na memória)
		Configuracoes confModoCE = Configuracoes.getConfiguracoesModoCE();
		estrategiasModoCE = confModoCE.estrategiasModoCE;
		nPartidasModoCE = confModoCE.nPartidasModoCE;
		Configuracoes conf = Configuracoes.getConfiguracoes();
		estrategias = conf.estrategias;
		Animador.setAnimacaoLigada(conf.animacaoLigada);
		cgRegras.setSelectedIndex(0, conf.baralhoLimpo);
		cgRegras.setSelectedIndex(1, conf.manilhaVelha);
		Carta.setCartasGrandes(conf.cartasGrandes);
		mesa.montaBaralhoCenario();

		// Inicializa os "displayables" da aplicação (menos os do
		// multiplayer, que são responsabilidade da classe Servidor)
		formModoCE = new Form("Op\u00E7\u00F5es do Confronto");
		formModoCE.append(cgModoCEDuplaA);
		formModoCE.append(cgModoCEDuplaB);
		formModoCE.append(cgModoCEnPartidas);
		formModoCE.addCommand(okModoCECommand);
		formModoCE.setCommandListener(this);
		for (int i = 0; i < OPCOES_ESTRATEGIAS.length; i++) {
			cgModoCEDuplaA.setSelectedIndex(i, OPCOES_ESTRATEGIAS[i]
					.equals(estrategiasModoCE[1]));
			cgModoCEDuplaB.setSelectedIndex(i, OPCOES_ESTRATEGIAS[i]
					.equals(estrategiasModoCE[0]));
		}
		for (int i = 0; i < OPCOES_NPARTIDAS.length; i++) {
			cgModoCEnPartidas.setSelectedIndex(i, OPCOES_NPARTIDAS[i]
					.equals(Integer.toString(nPartidasModoCE)));
		}

		formOpcoes = new Form("Op\u00E7\u00F5es");
		formOpcoes.append(cgVisual);
		formOpcoes.append(cgRegras);
		formOpcoes.append(cgParceiro);
		formOpcoes.append(cgAdversarioEsq);
		formOpcoes.append(cgAdversarioDir);
		formOpcoes.append(cgDebug);
		formOpcoes.addCommand(okOpcoesCommand);
		formOpcoes.setCommandListener(this);
		for (int i = 0; i < OPCOES_ESTRATEGIAS.length; i++) {
			cgAdversarioDir.setSelectedIndex(i, OPCOES_ESTRATEGIAS[i]
					.equals(estrategias[0]));
			cgParceiro.setSelectedIndex(i, OPCOES_ESTRATEGIAS[i]
					.equals(estrategias[1]));
			cgAdversarioEsq.setSelectedIndex(i, OPCOES_ESTRATEGIAS[i]
					.equals(estrategias[2]));
		}
		cgVisual.setSelectedIndex(0, Carta.isCartasGrandes());
		cgVisual.setSelectedIndex(1, Animador.isAnimacaoLigada());

		listAjuda = new List("Ajuda", List.IMPLICIT, OPCOES_AJUDA, null);
		listAjuda.addCommand(okItemAjudaCommand);
		listAjuda.addCommand(voltarMenuCommand);
		listAjuda.setCommandListener(this);

		if (isSuportaBluetooth()) {
			listBluetooth = new List("Jogo via Bluetooth", List.IMPLICIT,
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
			alerta("Erro", e.toString());
			return;
		}
		if (arqTexto.equals("/sobre.txt")) {
			if (versaoMidlet != null) {
				formTexto.append(new StringItem(null, "Vers\u00E3o: "
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
			if (isSuportaBluetooth() && !modoCE) {
				mesa.addCommand(bluetoothComand);
			} else {
				mesa.removeCommand(bluetoothComand);
			}
			mesa.addCommand(ajudaCommand);
			mesa.addCommand(opcoesCommand);
			if (modoCE) {
				mesa.addCommand(modoCECommand);
			} else {
				mesa.removeCommand(modoCECommand);
			}
			mesa.addCommand(sairProgramaCommand);
			mesa.removeComandoAposta();
			mesa.removeOpcoesAceite();
			mesa.removeOpcoesMao11();
			mesa.removeCommand(sairPartidaCommand);
		} else {
			mesa.removeCommand(sairProgramaCommand);
			mesa.removeCommand(iniciarCommand);
			mesa.removeCommand(bluetoothComand);
			mesa.removeCommand(ajudaCommand);
			mesa.removeCommand(opcoesCommand);
			mesa.removeCommand(modoCECommand);
		}

	}

	protected void startApp() {
		Display.getDisplay(this).setCurrent(mesa);
	}

	protected void pauseApp() {
	}

	protected void destroyApp(boolean bool) {
		mesa.isAppRodando = false;
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
				// Inicializa novo jogo com 4 jogadores CPU com
				// as devidas estratégias escolhidas para dupla A e B
				Jogo jogo = new JogoLocal(cgRegras.isSelected(0), cgRegras
						.isSelected(1), this.nPartidasModoCE);
				// Dupla A jogador de baixo
				jogadorBot = new JogadorBot(estrategiasModoCE[1], Display
						.getDisplay(this), (Mesa) mesa);
				jogo.adiciona(jogadorBot);
				// Dupla B jogador à direita
				jogo.adiciona(new JogadorCPU(estrategiasModoCE[0]));
				// Dupla A jogador de cima
				jogo.adiciona(new JogadorCPU(estrategiasModoCE[1]));
				// Dupla B jogador à esquerda
				jogo.adiciona(new JogadorCPU(estrategiasModoCE[0]));
				iniciaJogo(jogo);
			} else {
				// Inicializa novo jogo e adiciona o jogador humano
				Jogo jogo = new JogoLocal(cgRegras.isSelected(0), cgRegras
						.isSelected(1));
				jogadorHumano = new JogadorHumano(Display.getDisplay(this),
						(Mesa) mesa);
				jogo.adiciona(jogadorHumano);
				// Adiciona os jogadores CPU com as estratégias escolhidas
				for (int i = 0; i <= 2; i++) {
					jogo.adiciona(new JogadorCPU(estrategias[i]));
				}
				iniciaJogo(jogo);
			}
		} else if (cmd == bluetoothComand) {
			// Mostra o menu (lista) para o jogador escolher cliente ou
			// servidor
			Display.getDisplay(this).setCurrent(listBluetooth);
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
			}
			// [ENDIF_FULL]
		} else if (cmd == voltarMenuCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == sairPartidaCommand) {
			confirmaSairPartida();
		} else if (cmd == naoSairPartidaCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == simSairPartidaCommand
				|| cmd == sairPartidaSemPerguntarCommand) {
			// [IF_FULL]
			if (telaBT instanceof ClienteBT) {
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
			// [ENDIF_FULL]
			if (this.modoCE)
				encerraJogo(jogadorBot.getPosicao(), true);
			else
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
		} else if (cmd == modoCECommand) {
			Display.getDisplay(this).setCurrent(formModoCE);
		} else if (cmd == okOpcoesCommand) {
			// Seta as opções escolhidas no form (menos as regras,
			// que ficam guardadas no choiceGroup mesmo)
			estrategias[0] = OPCOES_ESTRATEGIAS[cgAdversarioDir
					.getSelectedIndex()];
			estrategias[1] = OPCOES_ESTRATEGIAS[cgParceiro.getSelectedIndex()];
			estrategias[2] = OPCOES_ESTRATEGIAS[cgAdversarioEsq
					.getSelectedIndex()];
			Carta.setCartasGrandes(cgVisual.isSelected(0));
			Animador.setAnimacaoLigada(cgVisual.isSelected(1));
			if (cgDebug.isSelected(0)) {
				MiniTruco.log = new String[6];
			} else {
				MiniTruco.log = null;
			}
			this.modoCE = cgDebug.isSelected(1);
			if (mesa != null) {
				mesa.setModoCE(this.modoCE);
			}
			if (cgRegras.isSelected(0) && cgRegras.isSelected(1)) {
				// Se houver conflito, faz o ajuste e mantém o form
				alerta("Conflito", "A manilha velha (fixa) exige baralho sujo.");
				cgRegras.setSelectedIndex(0, false);
			} else {
				// Guarda as opções na memória do celular
				Configuracoes conf = Configuracoes.getConfiguracoes();
				conf.estrategias = estrategias;
				conf.cartasGrandes = Carta.isCartasGrandes();
				conf.animacaoLigada = Animador.isAnimacaoLigada();
				conf.baralhoLimpo = cgRegras.isSelected(0);
				conf.manilhaVelha = cgRegras.isSelected(1);
				conf.salva();

				// Volta pra tela anterior
				mostraMenuAbertura(true);
				Display.getDisplay(this).setCurrent(mesa);
				mesa.montaBaralhoCenario();
				mesa.repaint();
			}
		} else if (cmd == okModoCECommand) {
			estrategiasModoCE[0] = OPCOES_ESTRATEGIAS[cgModoCEDuplaB
					.getSelectedIndex()];
			estrategiasModoCE[1] = OPCOES_ESTRATEGIAS[cgModoCEDuplaA
					.getSelectedIndex()];
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
		if (this.modoCE)
			jogadorBot = null;
		else
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
			f
					.append(new StringItem(null,
							"Deseja mesmo abandonar modo confronto de estrat\u00e9gias?"));
		else
			f.append(new StringItem(null,
					"Deseja mesmo abandonar esta partida?"));
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

	/**
	 * Log rotativo (para exibir na tela do celular)
	 */
	public static String[] log = null;

	/**
	 * Método usado para debug (permite acompanhar o jogo no console)
	 * 
	 * @param string
	 *            Mensagem informativa
	 */
	public static synchronized void log(String string) {
		// Envia para o console
		System.out.println(string);

		// Guarda no log rotativo, se estiver habilitado
		if (log != null) {
			for (int i = 0; i < log.length - 1; i++) {
				log[i] = log[i + 1];
			}
			log[log.length - 1] = string;
		}
	}

}