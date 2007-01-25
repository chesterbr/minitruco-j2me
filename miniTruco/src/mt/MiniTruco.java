package mt;

/*
 * Copyright © 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
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
	 * Formulário de configuração do jogo
	 */
	private Form formOpcoes;

	/**
	 * Tela Bluetooth (cliente ou servidor) em exibição no momento
	 */
	public TelaBT telaBT;

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
			"Sortear" };

	private static final String[] OPCOES_VISUAL = { "cartas grandes",
			"cartas animadas" };

	private static final String[] OPCOES_REGRAS = { "baralho limpo",
			"manilha velha" };

	private static final String[] OPCOES_DEBUG = { "Exibir log" };

	private static final Image[] IMAGENS_VISUAL = { null, null };

	private static final Image[] IMAGENS_REGRAS = { null, null };

	private static final Image[] IMAGENS_ESTRATEGIAS = { null, null, null };

	private static final Image[] IMAGENS_DEBUG = { null };

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

	// PONTO DE ENTRADA DA MIDLET

	public MiniTruco() {

		// Cria uma nova mesa, pronta pra animar
		novaMesa(true);

		// Carrega as configurações da memória do celular
		// (ou as default, se não houver nada na memória)
		Configuracoes conf = Configuracoes.getConfiguracoes();
		estrategias = conf.estrategias;
		Animador.setAnimacaoLigada(conf.animacaoLigada);
		cgRegras.setSelectedIndex(0, conf.baralhoLimpo);
		cgRegras.setSelectedIndex(1, conf.manilhaVelha);
		Carta.setCartasGrandes(conf.cartasGrandes);
		mesa.montaBaralhoCenario();

		// Inicializa os "displayables" da aplicação (menos os do
		// multiplayer, que são responsabilidade da classe Servidor)
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
			if (isSuportaBluetooth()) {
				mesa.addCommand(bluetoothComand);
			}
			mesa.addCommand(ajudaCommand);
			mesa.addCommand(opcoesCommand);
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
		} else if (cmd == bluetoothComand) {
			// Mostra o menu (lista) para o jogador escolher cliente ou servidor
			Display.getDisplay(this).setCurrent(listBluetooth);
		} else if ((cmd == List.SELECT_COMMAND || cmd == okBluetoothCommand)
				&& Display.getDisplay(this).getCurrent().equals(listBluetooth)) {
			// Inicializa o componente (cliente ou servidor) escolhido
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
		} else if (cmd == voltarMenuCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == sairPartidaCommand) {
			confirmaSairPartida();
		} else if (cmd == naoSairPartidaCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == simSairPartidaCommand
				|| cmd == sairPartidaSemPerguntarCommand) {
			if (telaBT instanceof ClienteBT) {
				// Se for um jogo cliente, derruba
				telaBT.encerraSessaoBT();
				telaBT = null;
			} else if (telaBT instanceof ServidorBT) {
				// O jogo servidor se vira sozinho (seria sacanagem derrubar
				// todo mundo)
				((ServidorBT) telaBT).desconecta(-1);
				return;
			}
			encerraJogo(jogadorHumano.getPosicao(), true);

		} else if (cmd == trucoCommand || cmd == seisCommand
				|| cmd == noveCommand || cmd == dozeCommand
				|| cmd == aceitaCommand || cmd == recusaCommand
				|| cmd == aceitaMao11Command || cmd == recusaMao11Command) {
			// Comandos que rolam durante o jogo, dispara uma thread para
			// executar
			ThreadComandoMenu tcm = new ThreadComandoMenu(mesa);
			tcm.executa(cmd);
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
				Logger.log = new String[6];
			} else {
				Logger.log = null;
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
		f.append(new StringItem(null, "Deseja mesmo abandonar esta partida?"));
		f.addCommand(simSairPartidaCommand);
		f.addCommand(naoSairPartidaCommand);
		f.setCommandListener(this);
		Display.getDisplay(this).setCurrent(f);
	}

	/**
	 * Cache do teste de suporte a bluetooth
	 * 
	 * @see MiniTruco#isSuportaBluetooth()
	 */
	private Boolean suportaBluetooth = null;

	/**
	 * Verifica se o dispostivo suporta bluetooth
	 * 
	 * @return true se encontrou a classe
	 *         <code>javax.bluetooth.localdevice</code>
	 */
	private boolean isSuportaBluetooth() {
		if (suportaBluetooth == null) {
			try {
				Class.forName("javax.bluetooth.LocalDevice");
				suportaBluetooth = new Boolean(true);
			} catch (ClassNotFoundException e) {
				suportaBluetooth = new Boolean(false);
			}
		}
		return suportaBluetooth.booleanValue();
	}

}