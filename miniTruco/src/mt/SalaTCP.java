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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Sala que está sendo exibida neste momento pelo servidor (fora do jogo).
 * <p>
 * Por uma questão de performance, o servidor usa um único objeto sala,
 * mesmo que o jogador troque de sala.
 * <p>
 * Pela mesma razão os campos públicos não foram encapsulados.
 * 
 */
public class SalaTCP extends Canvas implements CommandListener {

	private static final Command queroJogarCommand = new Command(
			"Quero jogar!", Command.SCREEN, 1);

	private static final Command sairSalaCommand = new Command("Sair da Sala",
			Command.SCREEN, 2);

	private static final Command desconectarCommand = new Command("Desconectar",
			Command.STOP, 999);

	private static final Command okEspiarCommand = new Command("Ok",
			Command.SCREEN, 1);

	private static final Command trocaParceiroCommand = new Command(
			"Troca de Parceiro", Command.SCREEN, 10);

	private static final Command inverteAdversariosCommand = new Command(
			"Inverte Advers\u00E1rios", Command.SCREEN, 11);

	private static final Command expulsaParceiroCommand = new Command(
			"Expulsa Parceiro", Command.SCREEN, 12);

	private static final Command regraManilhaCommand = new Command(
			"Muda Tipo de Manilha", Command.SCREEN, 13);

	private static final Command regraBaralhoCommand = new Command(
			"Muta Tipo de Baralho", Command.SCREEN, 14);

	public SalaTCP(ServidorTCP servidor) {
		super();
		this.servidor = servidor;
		this.addCommand(desconectarCommand);
		this.setCommandListener(this);
	}

	/**
	 * Faz com que a sala reflita as atualizações nas propriedades
	 * 
	 */
	public void atualizaSala() {

		removeMenus();
		boolean isEspiando = getPosicao(servidor.apelido) == 0;
		if (isEspiando) {
			this.addCommand(okEspiarCommand);
		} else {
			this.addCommand(sairSalaCommand);
			// Menu 'quero jogar' só aparece se o jogador tiver essa opção
			if (queroJogar.charAt(getPosicao(servidor.apelido) - 1) == 'F')
				addCommand(queroJogarCommand);
		}
		if (isJogadorGerenteDaSala()) {
			this.addCommand(trocaParceiroCommand);
			this.addCommand(inverteAdversariosCommand);
			this.addCommand(expulsaParceiroCommand);
			this.addCommand(regraManilhaCommand);
			this.addCommand(regraBaralhoCommand);
		}
		repaint();

	}

	public void removeMenus() {
		this.removeCommand(okEspiarCommand);
		this.removeCommand(sairSalaCommand);
		this.removeCommand(queroJogarCommand);
		this.removeCommand(regraManilhaCommand);
		this.removeCommand(regraBaralhoCommand);
		this.removeCommand(expulsaParceiroCommand);
		this.removeCommand(trocaParceiroCommand);
		this.removeCommand(inverteAdversariosCommand);
	}

	/**
	 * Servidor que está exibindo esta sala
	 */
	private ServidorTCP servidor;

	/**
	 * Nomes dos jogadores que estão na sala (atual ou espiada)
	 */
	public String[] jogadores;

	/**
	 * Status de "quero jogar" dos jogadores que estão na sala (atual ou
	 * espiada) (string de 4 caracteres "T" ou "F")
	 */
	public String queroJogar;

	/**
	 * Posição do gerente (usuário mais antigo) na sala (atual ou espiada), de 1
	 * a 4
	 */
	public int posGerente;

	/**
	 * Regras (string de 2 caracteres T/F, indicando baralho limpo e manilha
	 * velha, nesta ordem) para a sala atual ou espiada
	 */
	public String regras;

	/**
	 * Número da sala em que o jogador está, ou que está sendo espiada
	 * (0=nenhuma)
	 */
	public int numSala = 0;

	/**
	 * Fonte para os nomes dos jogadores "normais"
	 */
	private static final Font fonteNomes = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_PLAIN, Font.SIZE_SMALL);

	/**
	 * Fonte para o quadro de informações da sala
	 */
	private static final Font fonteInfo = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_ITALIC, Font.SIZE_SMALL);

	/**
	 * Retorna a posição de um jogador na sala (remota)
	 * 
	 * @param nome
	 *            apelido do jogador
	 * @return posição na ordem do servidor (1 a 4), ou 0 se não estiver na sala
	 *         recém-consultada.
	 */
	public int getPosicao(String nome) {
		for (int i = 0; i <= 3; i++) {
			if (nome.equals(jogadores[i]))
				return i + 1;
		}
		return 0;
	}

	/**
	 * Retorna a posição de um jogador na mesa (local)
	 * 
	 * @param nome
	 *            apelido do jogador consultado
	 * @return posição (de 1 a 4), relativa ao jogador atual (que está sempre na
	 *         1), ou absoluta se este não estiver na sala recém-consultada.
	 */
	private int getPosicaoMesa(String nome) {
		// Recupera as posições do jogador atual e do jogador consultado
		int posJogador = 0;
		int posJogadorConsultado = 0;
		for (int i = 0; i <= 3; i++) {
			if (servidor.apelido.equals(jogadores[i])) {
				posJogador = i + 1;
			}
			if (nome.equals(jogadores[i])) {
				posJogadorConsultado = i + 1;
			}
		}
		if (posJogador == 0) {
			// Se o jogador atual não está na sala, usa a posição remota mesmo
			return posJogadorConsultado;
		} else {
			// Caso ele esteja, retorna a posição relativa (considerando o atual
			// como posição 1)
			int pos = posJogadorConsultado - posJogador + 1;
			while (pos < 1)
				pos += 4;
			return pos;
		}

	}

	/**
	 * Processa os comandos enviados para os diálogos do servidor
	 * 
	 * @param cmd
	 * @param disp
	 */
	public void commandAction(Command cmd, Displayable disp) {
		removeMenus();
		if (cmd == okEspiarCommand) {
			numSala = 0;
			servidor.mostraMsgAguarde();
			servidor.enviaComando("L");
		} else if (cmd == queroJogarCommand) {
			servidor.mostraMsgAguarde();
			servidor.enviaComando("Q");
		} else if (cmd == sairSalaCommand) {
			servidor.mostraMsgAguarde();
			servidor.enviaComando("S");
		} else if (cmd == desconectarCommand) {
			servidor.finalizaServidor();
		} else if (cmd == regraBaralhoCommand) {
			servidor.enviaComando("R " + (regras.charAt(0) == 'T' ? 'F' : 'T')
					+ regras.charAt(1));
		} else if (cmd == regraManilhaCommand) {
			servidor.enviaComando("R " + regras.charAt(0)
					+ (regras.charAt(1) == 'T' ? 'F' : 'T'));
		} else if (cmd == trocaParceiroCommand) {
			servidor.enviaComando("O");
		} else if (cmd == inverteAdversariosCommand) {
			servidor.enviaComando("V");
		} else if (cmd == expulsaParceiroCommand) {
			servidor.enviaComando("K " + getPosicaoParceiro());
		}

	}

	/**
	 * Recuper a posição (na sala remota) do parceiro do jogador que está no
	 * celular
	 * 
	 * @return
	 */
	private int getPosicaoParceiro() {
		int pos = getPosicao() + 2;
		if (pos > 4)
			pos = pos - 4;
		return pos;
	}

	/**
	 * Recupera a posição (na sala remota) do jogador que está no celular
	 * 
	 * @return posição de 1 a 4
	 */
	private int getPosicao() {
		for (int i = 0; i <= 3; i++) {
			if (servidor.apelido.equals(jogadores[i])) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * Mostra os jogadores da sala em que estamos (ou que estamos espiando)
	 */
	protected void paint(Graphics g) {

		g.setColor(0x0000FF00);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (numSala != 0) {

			// Nomes dos jogadores

			int topoNomes = fonteInfo.getHeight();
			int alturaNomes = getHeight() - topoNomes;

			for (int i = 0; i <= 3; i++) {

				String nome = jogadores[i];
				if (nome != null && !"".equals(nome)) {

					// Decide onde escrever
					int pos = getPosicaoMesa(nome);

					// Indica se quer jogar (pela cor)
					g.setColor(queroJogar.charAt(i) == 'T' ? 0x00000000
							: 0x00FF0000);

					// Indica se é gerente (pelo texto)
					if ((i + 1) == posGerente) {
						nome += " (gerente)";
					}

					// Escreve
					g.setFont(fonteNomes);
					switch (pos) {
					case 1:
						g.drawString(nome, getWidth() / 2, getHeight(),
								Graphics.HCENTER | Graphics.BOTTOM);
						break;
					case 2:
						g.drawString(nome, getWidth() - 1, topoNomes
								+ alturaNomes / 2, Graphics.RIGHT
								| Graphics.TOP);
						break;
					case 3:
						g.drawString(nome, getWidth() / 2, topoNomes,
								Graphics.HCENTER | Graphics.TOP);
						break;
					case 4:
						g.drawString(nome, 0, topoNomes + alturaNomes / 2,
								Graphics.LEFT | Graphics.BOTTOM);
						break;
					}
				}
			}

			// Info da sala (acima dos nomes)

			g.setColor(0x00C0C0C0);
			g.fillRect(0, 0, getWidth(), topoNomes);

			String linha1 = "SALA " + numSala;
			String linha2 = (regras.charAt(0) == 'T' ? "b.limpo / "
					: "b.sujo /")
					+ (regras.charAt(1) == 'T' ? "m.velha " : "m.nova");

			g.setColor(0x00000000);
			g.setFont(fonteInfo);
			g.drawString(linha1, 1, 0, Graphics.TOP | Graphics.LEFT);
			g.drawString(linha2, getWidth() - 2, 0, Graphics.TOP
					| Graphics.RIGHT);

		} else {
			// Se não tiver nada pra mostrar, manda a mensagem de aguarde
			servidor.paint(g);
		}

	}

	/**
	 * Determina se o jogador no celular é o gerente da sala
	 * 
	 * @return true se for gerente
	 */

	private boolean isJogadorGerenteDaSala() {
		if (servidor.apelido == null) {
			return false;
		}
		for (int i = 0; i <= 3; i++) {
			if (servidor.apelido.equals(jogadores[i])) {
				return ((i + 1) == posGerente);
			}
		}
		return false;

	}

}
