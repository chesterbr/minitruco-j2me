package br.inf.chester.minitruco.cliente;

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
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

/**
 * Ponto de entrada da aplicação no celular (MIDLet).
 * @author Chester
 *
 */
public class MiniTruco extends MIDlet implements CommandListener {

	private static final String[] OPCOES_AJUDA = { "Instru\u00E7\u00F5es",
			"Jogo Online", "Regras do Truco", "Sobre o miniTruco", "Voltar" };

	private static final String[] ARQUIVOS_AJUDA = { "/instrucoes.txt",
			"/online.txt", "/regras.txt", "/sobre.txt" };

	public static Command iniciarCommand = new Command("Iniciar",
			Command.SCREEN, 1);

	public static Command multiplayerCommand = new Command("Jogar Online",
			Command.SCREEN, 2);

	public static Command ajudaCommand = new Command("Ajuda", Command.SCREEN, 3);

	public static Command opcoesCommand = new Command("Op\u00E7\u00F5es",
			Command.SCREEN, 5);

	public static Command sairProgramaCommand = new Command("Sair",
			Command.EXIT, 7);

	public static Command okItemAjudaCommand = new Command("Ok",
			Command.SCREEN, 1);

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

	public static Command okTexto = new Command("Ok", Command.OK, 1);

	public static Command okOpcoesCommand = new Command("Ok", Command.OK, 1);

	public static Command simSairPartidaCommand = new Command("Sim",
			Command.OK, 1);

	public static Command naoSairPartidaCommand = new Command("N\u00E3o",
			Command.CANCEL, 2);

	public Mesa mesa;

	private Form formOpcoes;

	private List listAjuda;

	public MiniTruco() {

		// Cria uma nova mesa, pronta pra animar
		novaMesa(true);

		// Carrega as configurações da memória do celular
		// (ou as default, se não houver nada na memória)
		Configuracoes conf = Configuracoes.getConfiguracoes();
		nivel = conf.nivel;
		Animador.setAnimacaoLigada(conf.animacaoLigada);
		cgRegras.setSelectedIndex(0, conf.baralhoLimpo);
		cgRegras.setSelectedIndex(1, conf.manilhaVelha);
		tfServidor.setString(conf.servidor);
		if (!conf.isDefault()) {
			// Só seta o tamanho se for carregado (o default é deixar a mesa
			// auto-configurar isso). E remonta o bralho do cenário se setar.
			Carta.setCartasGrandes(conf.cartasGrandes);
			mesa.montaBaralhoCenario();
		}

		// Inicializa os "displayables" da aplicação (menos os do
		// multiplayer, que são responsabilidade da classe Servidor)
		formOpcoes = new Form("Op\u00E7\u00F5es");
		formOpcoes.append(cgDificuldade);
		formOpcoes.append(cgVisual);
		formOpcoes.append(cgRegras);
		formOpcoes.append(tfServidor);
		formOpcoes.addCommand(okOpcoesCommand);
		formOpcoes.setCommandListener(this);
		cgDificuldade.setSelectedIndex(nivel, true);
		cgVisual.setSelectedIndex(0, Carta.isCartasGrandes());
		cgVisual.setSelectedIndex(1, Animador.isAnimacaoLigada());

		listAjuda = new List("Ajuda", List.IMPLICIT, OPCOES_AJUDA, null);
		listAjuda.addCommand(okItemAjudaCommand);
		listAjuda.setCommandListener(this);

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
	 * Versão da midlet (é usada pelo servidor)
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
			mesa.addCommand(multiplayerCommand);
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
			mesa.removeCommand(multiplayerCommand);
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
	 * Jogo sendo jogado no momento
	 */
	private Jogo jogo;

	/**
	 * Jogador que está interagindo com o celular
	 */
	private JogadorHumano jogadorHumano;

	/**
	 * Servidor Em que estamos conectados
	 */
	private Servidor servidor;

	/**
	 * Nível de dificuldade: 0=fácil, 1=médio, 2=difícil
	 */
	private int nivel;

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == iniciarCommand) {
			// Inicializa novo jogo e adiciona o jogador humano
			jogo = new JogoLocal(cgRegras.isSelected(0), cgRegras.isSelected(1));
			jogadorHumano = new JogadorHumano(Display.getDisplay(this),
					(Mesa) mesa);
			jogo.adiciona(jogadorHumano);
			// jogo.adiciona(new JogadorCPU(new EstrategiaJohhnyWalker()));
			// Escolhe os outros jogadores de acordo com o nível
			switch (nivel) {
			case 0:
				// Parceiro bom, adversários bêbados
				jogo.adiciona(new JogadorCPU(new EstrategiaJohhnyWalker()));
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
				jogo.adiciona(new JogadorCPU(new EstrategiaJohhnyWalker()));
				break;
			case 1:
				// Todo mundo bom
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
			case 2:
				// Parceiro ruim, adversários bons
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
				jogo.adiciona(new JogadorCPU(new EstrategiaJohhnyWalker()));
				jogo.adiciona(new JogadorCPU(new EstrategiaWillian()));
			}
			// Inicia o novo jogo
			mesa.removeComandoAposta();
			mostraMenuAbertura(false);
			mesa.addCommand(sairPartidaCommand);
			Thread t = new Thread(jogo);
			t.start();
		} else if (cmd == multiplayerCommand) {
			mostraMenuAbertura(false);
			servidor = new Servidor(tfServidor.getString(), this);
		} else if (cmd == sairPartidaCommand) {
			confirmaSairPartida();
		} else if (cmd == naoSairPartidaCommand) {
			Display.getDisplay(this).setCurrent(mesa);
		} else if (cmd == simSairPartidaCommand
				|| cmd == sairPartidaSemPerguntarCommand) {
			if (jogo != null) {
				jogo.abortaJogo(jogadorHumano);
				jogo = null;
				jogadorHumano = null;
				novaMesa(false);
				Display.getDisplay(this).setCurrent(mesa);
			} else if (servidor != null) {
				if (cmd == sairPartidaSemPerguntarCommand) {
					// Fim de jogo normal
					servidor.enviaComando("I");
					Display.getDisplay(this).setCurrent(servidor);
				} else {
					// Jogador abortou espontaneamente
					servidor.abortaJogoAtual();
				}
				novaMesa(false);
			} else {
				// Isso teoricamente nao acontece, mas...
				alerta("Erro inesperado", "Nao ha jogo em andamento");
			}
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
			nivel = cgDificuldade.getSelectedIndex();
			Carta.setCartasGrandes(cgVisual.isSelected(0));
			Animador.setAnimacaoLigada(cgVisual.isSelected(1));

			if (cgRegras.isSelected(0) && cgRegras.isSelected(1)) {
				// Se houver conflito, faz o ajuste e mantém o form
				alerta("Conflito", "A manilha velha (fixa) exige baralho sujo.");
				cgRegras.setSelectedIndex(0, false);
			} else {
				// Se o servidor estiver vazio, reverte ao default
				if (tfServidor.getString() == null
						|| tfServidor.getString().equals("")) {
					tfServidor.setString(Servidor.SERVIDOR_DEFAULT);
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
							+ Servidor.PORTA_DEFAULT);
				}
				// Guarda as opções na memória do celular
				Configuracoes conf = Configuracoes.getConfiguracoes();
				conf.nivel = nivel;
				conf.cartasGrandes = Carta.isCartasGrandes();
				conf.animacaoLigada = Animador.isAnimacaoLigada();
				conf.baralhoLimpo = cgRegras.isSelected(0);
				conf.manilhaVelha = cgRegras.isSelected(1);
				conf.servidor = tfServidor.getString();
				conf.salva();

				// Volta pra tela anterior
				Display.getDisplay(this).setCurrent(mesa);
				mesa.montaBaralhoCenario();
				mesa.repaint();
			}
		}

	}

	private static final String[] OPCOES_DIFICULDADE = { "f\u00E1cil",
			"m\u00E9dio", "dif\u00EDcil" };

	private static final String[] OPCOES_VISUAL = { "cartas grandes",
			"cartas animadas" };

	private static final String[] OPCOES_REGRAS = { "baralho limpo",
			"manilha velha" };

	private static final Image[] IMAGENS_DIFICULDADE = { null, null, null };

	private static final Image[] IMAGENS_VISUAL = { null, null };

	private static final Image[] IMAGENS_REGRAS = { null, null };

	ChoiceGroup cgDificuldade = new ChoiceGroup("Dificuldade",
			Choice.EXCLUSIVE, OPCOES_DIFICULDADE, IMAGENS_DIFICULDADE);

	ChoiceGroup cgVisual = new ChoiceGroup("Visual", Choice.MULTIPLE,
			OPCOES_VISUAL, IMAGENS_VISUAL);

	ChoiceGroup cgRegras = new ChoiceGroup("Regras", Choice.MULTIPLE,
			OPCOES_REGRAS, IMAGENS_REGRAS);

	TextField tfServidor = new TextField("Servidor (host:porta)", null, 80,
			TextField.URL);

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

}