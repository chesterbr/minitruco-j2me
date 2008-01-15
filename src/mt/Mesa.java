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
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Mostra a mesa de jogo (fundo verde, cartas, placares, etc.), além da animação
 * de abertura.
 * <p>
 * Obs.: O ideal seria que a abertura estivesse em uma classe separada - e que
 * ambas respondessem por seus eventos de menu (ao invés de deixar a classe
 * MiniTruco fazer isso). Só percebi isso quando fui adicionar uma terceira tela (<code>Sala</code>,
 * do multiplayer). Coisas do aprendizado - um dia desses eu separo.
 * 
 * 
 * @author Chester
 * 
 */
public class Mesa extends Canvas implements Runnable {

	/**
	 * Margem entre a mesa e as cartas
	 */
	public static final int MARGEM = 1;

	/**
	 * Guarda as cartas que foram jogadas pelos jogadores
	 */
	private Vector cartasJogadas = new Vector(12);

	/**
	 * Guarda todas as cartas na mesa (jogadas, não-jogadas e a virada)
	 */
	private Vector cartas = new Vector(13);

	/**
	 * Aponta as cartas que o jogador pode selecionar para jogar
	 */
	private Vector cartasSelecionaveis = null;

	/**
	 * Carta que está sob o cursor (e que será jogada se selecionada)
	 */
	private Carta cartaSelecionada = null;

	/**
	 * Cartas que compõem o baralhinho desenhado no cenário.
	 * <p>
	 * (não confundir com a classe Baralho, usada pela classe Jogo para sortear
	 * cartas)
	 */
	public Vector baralhoCenario = new Vector(3);

	/**
	 * Diz se o baralho deve ficar por cima das cartas ou por baixo delas.
	 * <p>
	 * (ele fica por cima durante o jogo - por causa do vira - e por baixo
	 * quando estivermos distribuindo ou recolhendo cartas)
	 */
	private boolean baralhoPorCima = true;

	/**
	 * Este objeto vai cuidar das animações (é uma thread).
	 */
	private Animador animador;

	private int topBaralho, leftBaralho;

	private int topCartaDaMesa, leftCartaDaMesa;

	// Variáveis ligadas à abertura
	private boolean aberturaVisivel = true;

	public int aberturaAlturaSas;

	public int aberturaNumCartas;

	public boolean aberturaUrlVisivel;

	/**
	 * Cria uma nova mesa
	 * 
	 * @param vaiAnimar
	 *            Se true, esconde a abertura (para mostrar na animação). Se
	 *            false, já mostra a abertura finalizada.
	 * 
	 * 
	 */
	public Mesa(boolean vaiAnimar) {

		// Cria um animador para esta mesa
		animador = new Animador(this);
		animador.start();

		montaBaralhoCenario();

		if (vaiAnimar) {
			// Esconde tudo
			this.aberturaAlturaSas = getHeight() + 1;
			this.aberturaNumCartas = 0;
			this.aberturaUrlVisivel = false;
		} else {
			// Mostra tudo no lugar
			mostraFinalAbertura();
		}

	}

	/**
	 * Executa a animação de abertura
	 * 
	 */
	public void animaAbertura() {
		// Delega para o animador, numa nova thread
		new Thread(new Runnable() {
			public void run() {
				animador.animaAbertura();
			}
		}).start();
	}

	public void montaBaralhoCenario() {
		// Desenha o baralho do cenário (e guarda as coordenadas para
		// cartas que entram ou saem dele
		baralhoCenario.setSize(3);
		for (int i = 0; i <= 2; i++) {
			Carta c = new Carta(getWidth() - Carta.larguraCartas - MARGEM
					- (2 - i) * 2 - 2, getHeight() - Carta.alturaCartas
					- MARGEM - i * 2);
			baralhoCenario.setElementAt(c, i);
			if (i == 2) {
				topBaralho = c.getTop();
				leftBaralho = c.getLeft();
			} else if (i == 0) {
				topCartaDaMesa = c.getTop();
				leftCartaDaMesa = c.getLeft() - Carta.larguraCartas * 3
						/ (Carta.isCartasGrandes() ? 5 : 4);
			}
		}
	}

	public void setCartas(Vector cartas) {
		this.cartas = cartas;
	}

	public Vector getCartas() {
		return cartas;
	}

	/**
	 * Verifica se uma carta já foi jogada na mesa
	 * 
	 * @param c
	 *            Carta a verificar
	 */
	public boolean isJogada(Carta c) {
		return cartasJogadas.contains(c);
	}

	/**
	 * Informa a mesa o jogador que está acoplado a ela.
	 * <p>
	 * Isso é feito quando o JogadorHumano é construído
	 * 
	 * @param jogador
	 */
	public void setJogador(JogadorHumano jogador) {
		this.jogador = jogador;
	}

	JogadorHumano getJogador() {
		return jogador;
	}

	private JogadorHumano jogador;

	/**
	 * Informa a mesa o JogadorBot que está acoplado a ela (isto acontece
	 * somente no modo confronto de estratégias)
	 * 
	 * @param jogador
	 */

	// [IF_FULL]
	private JogadorBot jogadorBot;
	// [ENDIF_FULL]

	private boolean modoCE = false;

	/**
	 * Mostra os nomes dos jogadores.
	 * 
	 * @param tempoMs
	 *            Tempo em que eles serão exibidos (em milissegundos)
	 */
	public void mostraNomesJogadores(int tempoMs) {
		animador.mostraNomesJogadores(tempoMs);
	}

	/**
	 * Informa à mesa o bot que jogará nela (mudando o modo para Confronto de
	 * Estratégias)
	 * 
	 * @param jogador
	 */
	// [IF_FULL]
	public void setJogadorBot(JogadorBot jogador) {
		this.jogadorBot = jogador;
		this.modoCE = true;
	}

	// [ENDIF_FULL]

	/**
	 * Ajusta a mesa para o modo normal ou confronto.
	 * 
	 * @param modoCE
	 *            true para modo Confronto de Estratégias, false para jogo
	 *            normal
	 */
	public void setModoCE(boolean modoCE) {
		this.modoCE = modoCE;
		// [IF_FULL]
		if (modoCE == false) {
			this.jogadorBot = null;
		}
		// [ENDIF_FULL]
	}

	// [IF_FULL]
	JogadorBot getJogadorBot() {
		return jogadorBot;
	}

	// [ENDIF_FULL]

	public static Font fontePlacar = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_PLAIN, Font.SIZE_SMALL);

	public static Font fonteBalao = Font.getFont(Font.FACE_PROPORTIONAL,
			Font.STYLE_PLAIN, Font.SIZE_SMALL);

	String[] stringPlacar = new String[2];

	boolean mostraNomes = false;

	int[] placar = new int[2];

	int[] placarPartidas = new int[2];

	public void atualizaPlacar(int nos, int eles) {
		if (nos == 0 && eles == 0) {
			// Inicializa o placar
			limpaPlacar();
		} else {
			// Atualiza e pisca a string do placar que mudou
			if (placar[0] != nos) {
				animador.piscaPlacar(1, nos);
			} else if (placar[1] != eles) {
				animador.piscaPlacar(2, eles);
			}
			// Guarda os resultados
			placar[0] = nos;
			placar[1] = eles;
		}
	}

	public void atualizaPlacarComVaquinhasInfo(int nos, int eles,
			int vaquinhasNoPastoDplA, int vaquinhasNoPastoDplB) {
		if (vaquinhasNoPastoDplA == 0 && vaquinhasNoPastoDplB == 0 && nos == 0
				&& eles == 0) {
			// Inicializa o placar
			limpaPlacar();
		} else {
			// Atualiza e pisca a string do placar que mudou
			if (placarPartidas[0] != vaquinhasNoPastoDplA) {
				animador
						.piscaPlacarComVaquinhasInfo(1, nos, eles,
								vaquinhasNoPastoDplA, vaquinhasNoPastoDplB,
								this.modoCE);
			} else if (placarPartidas[1] != vaquinhasNoPastoDplB) {
				animador
						.piscaPlacarComVaquinhasInfo(2, nos, eles,
								vaquinhasNoPastoDplA, vaquinhasNoPastoDplB,
								this.modoCE);
			} else if (placar[0] != nos) {
				animador
						.piscaPlacarComVaquinhasInfo(1, nos, eles,
								vaquinhasNoPastoDplA, vaquinhasNoPastoDplB,
								this.modoCE);
			} else if (placar[1] != eles) {
				animador
						.piscaPlacarComVaquinhasInfo(2, nos, eles,
								vaquinhasNoPastoDplA, vaquinhasNoPastoDplB,
								this.modoCE);
			}
			// Guarda os resultados
			placar[0] = nos;
			placar[1] = eles;
			placarPartidas[0] = vaquinhasNoPastoDplA;
			placarPartidas[1] = vaquinhasNoPastoDplB;
		}
	}

	/**
	 * Mostra o placar das partidas (somente para o modo confronto de
	 * estratégias)
	 * 
	 * @param jogador
	 */
	public void mostraResultadoFinalModoCE(int vaquinhasNoPastodplA,
			int vaquinhasNoPastodplB) {

		// Mostra placar final
		// TO DO
		// Aqui o legal seria mostrar o resultado final
		// assim como o nome das estratégias??
		// hummm talvez não... o resultado já aparece na mesa...

		// Mostra menu final
		removeCommand(MiniTruco.sairPartidaCommand);
		addCommand(MiniTruco.sairPartidaSemPerguntarCommand);

	}

	static final String STRING_NOS = Messages.getString("nos"); //$NON-NLS-1$

	static final String STRING_ELES = Messages.getString("eles"); //$NON-NLS-1$

	static final String STRING_DA = "A: ";

	static final String STRING_DB = "B: ";

	public void limpaPlacar() {
		// Limpa os resultados e as strings (sem piscar)
		placar[0] = 0;
		placar[1] = 0;
		placarPartidas[0] = 0;
		placarPartidas[1] = 0;
		if (this.modoCE) {
			stringPlacar[0] = STRING_DA + "0-0";
			stringPlacar[1] = STRING_DB + "0-0";
		} else {
			stringPlacar[0] = STRING_NOS + "0";
			stringPlacar[1] = STRING_ELES + "0";
		}
	}

	/**
	 * Buffer usado para fazer o double buffering (caso o devide não suporte
	 * fazer isso automaticamente)
	 */
	private Image offscreen = (isDoubleBuffered() ? null : Image.createImage(
			getWidth(), getHeight()));

	/**
	 * ícones que mostram o status das rodadas jogadas (vitória, empate, derrota
	 * ou não-jogada)
	 */
	protected Image[] iconesRodadas = { getIconeRodada(0), getIconeRodada(0),
			getIconeRodada(0) };

	/**
	 * Texto que aparece piscando para indicar ações possíveis do jogador (tudo
	 * por causa do $*!# menu do Series 60, que bagunça tudo)
	 */
	private String textoPiscando;

	/**
	 * Indica que estamos aguardando aceite ou recusa de um aumento de aposta
	 */
	private boolean isAguardandoAceite;

	/**
	 * Indica que estamos aguardando resposta sobre jogar ou não uma mão de 11
	 */
	private boolean isAguardandoMao11;

	/**
	 * Indica se podemos jogar carta fechada na rodada atual
	 */
	private boolean podeFechada;

	private Image imgLogoSas;

	private Image imgLogoCartas;

	private Image imgLogoCartas2;

	/**
	 * Largura dos ícones de status
	 */
	private static final int LARG_ICONE = getIconeRodada(0).getWidth();

	/**
	 * Atualiza a tela do jogo
	 */
	protected void paint(Graphics g) {

		// Se for preciso double-buffering, faz as operações usarem o buffer
		Graphics saved = g;
		if (offscreen != null) {
			g = offscreen.getGraphics();
		}

		try {

			// Fundo verde
			g.setColor(0x0000FF00);
			g.fillRect(0, 0, getWidth(), getHeight());

			// Se a abertura estiver visível, desenha sua fase atual (ela é uma
			// animação, tem várias fases)

			if (aberturaVisivel) {
				// Inicializa imagens
				try {
					if (imgLogoSas == null) {
						imgLogoSas = Image.createImage("/logosas.png");
					}
					if (imgLogoCartas == null) {
						imgLogoCartas = Image.createImage("/logotipo.png");
					}
					if (imgLogoCartas2 == null) {
						imgLogoCartas2 = imgLogoCartas; // para versão light
						// [IF_FULL]
						imgLogoCartas2 = Image.createImage("/logotipo2.png");
						// [ENDIF_FULL]
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// stone age scanners apresenta
				g.drawImage(imgLogoSas, getWidth() / 2, aberturaAlturaSas,
						Graphics.HCENTER | Graphics.TOP);
				// miniTruco em cartinhas (com rects cobrindo as ocultas, e a
				// troca
				// de bitmap representando a 10a. carta)
				if (aberturaNumCartas > 0) {
					g.drawImage(aberturaNumCartas <= 9 ? imgLogoCartas
							: imgLogoCartas2, getWidth() / 2, getHeight() / 2,
							Graphics.HCENTER | Graphics.VCENTER);
					int wCarta = (imgLogoCartas.getWidth() - 12) / 5;
					int hCarta = (imgLogoCartas.getHeight() - 12) / 2;
					g.fillRect(hCarta / 2 - 2
							+ (getWidth() - imgLogoCartas.getWidth()) / 2,
							getHeight() / 2 - hCarta, Math.max(0, (wCarta + 1)
									* (5 - aberturaNumCartas)) - 10, hCarta);
					g.fillRect(1
							+ (getWidth() - imgLogoCartas.getWidth())
							/ 2
							+ Math.max(0, (wCarta + 1)
									* (aberturaNumCartas - 4)) + 6,
							getHeight() / 2 + 1, imgLogoCartas.getWidth(),
							hCarta);
				}
				if (aberturaUrlVisivel) {
					if (!this.modoCE) {
						g.setColor(0x000000FF);
						g.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
								Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
						g.drawString("m.chester.blog.br", getWidth() / 2,
								getHeight() - 2, Graphics.HCENTER
										| Graphics.BOTTOM);
					} else {
						g.setColor(0x990033);
						g.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
								Font.STYLE_BOLD, Font.SIZE_SMALL));
						g
								.drawString(
										Messages.getString("modo_confronto"), getWidth() / 2, //$NON-NLS-1$
										getHeight() - 12, Graphics.HCENTER
												| Graphics.BOTTOM);
						g.drawString("<S. Gasparoto>", getWidth() / 2,
								getHeight() - 2, Graphics.HCENTER
										| Graphics.BOTTOM);
					}
				}
			}

			// Desenha os nomes dos outros jogadores, se for o caso (ex.: no
			// início
			// da partida)
			if (mostraNomes) {
				int topoNomes = fontePlacar.getHeight();
				int alturaNomes = getHeight() - topoNomes;
				g.setFont(fontePlacar);
				g.setColor(0x00000000);
				g.drawString(jogador.jogo.getJogador(2).getNome(),
						getWidth() - 1, topoNomes + alturaNomes / 2,
						Graphics.RIGHT | Graphics.TOP);
				g.drawString(jogador.jogo.getJogador(3).getNome(),
						getWidth() / 2, 0, Graphics.HCENTER
								| Graphics.TOP);
				g.drawString(jogador.jogo.getJogador(4).getNome(), 0, topoNomes
						+ alturaNomes / 2, Graphics.LEFT | Graphics.TOP);
				return;
			}

			// Desenha o placar, se já houver
			// (e antes do indicador de vez - em celulares pequenos eles
			// encavalam,
			// e este último é mais relevante quando se está aguardando)
			if (stringPlacar[0] != null && !aberturaVisivel) {
				// Pontuação
				g.setFont(fontePlacar);
				g.setColor(0x00000000);
				if (stringPlacar[0] != null) {
					g.drawString(stringPlacar[0], MARGEM, getHeight(),
							Graphics.BOTTOM | Graphics.LEFT);
				}
				if (stringPlacar[1] != null) {
					g.drawString(stringPlacar[1], getWidth(), MARGEM,
							Graphics.TOP | Graphics.RIGHT);
				}
				// ícones das rodadas
				for (int i = 0; i <= 2; i++) {
					g.drawImage(iconesRodadas[i], MARGEM + i * LARG_ICONE,
							MARGEM, Graphics.TOP | Graphics.LEFT);
				}
			}

			// Desenha o indicador de "vez" (antes das cartas, para evitar
			// sobrepô-las se a tela for pequena demais)
			int leftVez, topVez;
			g.setColor(0xFF0000);

			switch (posicaoDaVez) {
			case 1: // Baixo (somente no modo confronto de estratégias)
				if (this.modoCE) {
					// TO DO
					// Chester: help here!
				}
				break;
			case 2: // Direita
				leftVez = getWidth() - (Carta.larguraCartas) / 2 - MARGEM - 2;
				topVez = getHeight() / 2 - Carta.alturaCartas / 2 - 11;
				g.fillRect(leftVez + 1, topVez, 3, 5);
				g.drawLine(leftVez, topVez + 3, leftVez + 4, topVez + 3);
				g.drawLine(leftVez + 2, topVez + 5, leftVez + 2, topVez + 5);
				break;
			case 3: // Cima
				leftVez = getWidth() / 2 - Carta.larguraCartas - 7;
				topVez = Carta.alturaCartas / 2 + MARGEM - 3;
				g.fillRect(leftVez, topVez + 1, 5, 3);
				g.drawLine(leftVez + 3, topVez, leftVez + 3, topVez + 4);
				g.drawLine(leftVez + 5, topVez + 2, leftVez + 5, topVez + 2);
				break;
			case 4: // Esquerda
				leftVez = (Carta.larguraCartas) / 2 + MARGEM - 2;
				topVez = getHeight() / 2 + Carta.alturaCartas / 2 + 6;
				g.fillRect(leftVez + 1, topVez + 1, 3, 5);
				g.drawLine(leftVez, topVez + 2, leftVez + 4, topVez + 2);
				g.drawLine(leftVez + 2, topVez, leftVez + 2, topVez);
				break;
			}

			// Desenha o baralho e as cartas
			if (!aberturaVisivel) {
				if (baralhoPorCima) {
					desenhaCartas(g);
					desenhaBaralho(g);
				} else {
					desenhaBaralho(g);
					desenhaCartas(g);
				}
			}

			// Desenha o texto piscando, com sombra (mesma linha do balão)
			if (textoPiscando != null
					&& (System.currentTimeMillis() % 2000 > 1000)) {
				g.setColor(0x00000000);
				g.setFont(fontePlacar);
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						g.drawString(textoPiscando, i + getWidth() / 2, j
								+ (getHeight() - fontePlacar.getHeight()) / 2,
								Graphics.HCENTER | Graphics.TOP);

				g.setColor(0x00FFFFFF);
				g.drawString(textoPiscando, getWidth() / 2,
						(getHeight() - fontePlacar.getHeight()) / 2,
						Graphics.HCENTER | Graphics.TOP);
			}

			// Desenha, se houver, o balão de texto para um jogador
			if (posicaoBalao != 0) {

				// Determina o tamanho e a posição do balão e o quadrante da
				// ponta
				final int MARGEM_BALAO_LEFT = 10;
				final int MARGEM_BALAO_TOP = 3;
				int largBalao = fonteBalao.stringWidth(textoBalao) + 2
						* MARGEM_BALAO_LEFT;
				int altBalao = fonteBalao.getHeight() + 2 * MARGEM_BALAO_TOP;
				int x = 0, y = 0;
				int quadrantePonta = 0;
				switch (posicaoBalao) {
				case 1:
					x = (getWidth() - largBalao) / 2 - Carta.larguraCartas;
					y = getHeight() - altBalao - Carta.alturaCartas - MARGEM
							- 3;
					quadrantePonta = 4;
					break;
				case 2:
					x = getWidth() - largBalao - MARGEM - 3;
					y = (getHeight() - altBalao) / 2 + Carta.alturaCartas;
					quadrantePonta = 1;
					break;
				case 3:
					x = (getWidth() - largBalao) / 2 + Carta.larguraCartas;
					y = MARGEM + 3 + altBalao;
					quadrantePonta = 2;
					break;
				case 4:
					x = MARGEM + 3;
					y = (getHeight() - altBalao) / 2 - Carta.alturaCartas;
					quadrantePonta = 3;
					break;
				}

				// O balão tem que ser branco, com uma borda preta. Como
				// ele só aparece em um refresh, vamos pela força bruta,
				// desenhando ele deslocado em torno da posição final em
				// preto e em seguida desenhando ele em branco na posição
				g.setColor(0x00000000);
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						if (i != 0 && j != 0) {
							desenhaBalao(g, x + i, y + j, largBalao, altBalao,
									quadrantePonta);
						}
					}
				}
				g.setColor(0x00FFFFFF);
				desenhaBalao(g, x, y, largBalao, altBalao, quadrantePonta);

				// Finalmente, escreve o texto do balão
				g.setColor(0x00000000);
				g.drawString(textoBalao, x + MARGEM_BALAO_LEFT, y
						+ MARGEM_BALAO_TOP, Graphics.LEFT | Graphics.TOP);

			}

			// Imprime, se necessario, o log de mensagens
			if (Jogo.log != null) {
				g.setFont(fontePlacar);
				g.setColor(0x00000000);
				int alturaLog = fontePlacar.getHeight();
				int alturaCanvas = this.getHeight();
				for (int i = 0; (i < Jogo.log.length)
						&& ((i + 1) * alturaLog <= alturaCanvas); i++) {
					if (Jogo.log[i] != null) {
						g.drawString(Jogo.log[i], 0, i * alturaLog,
								Graphics.LEFT | Graphics.TOP);
					}
				}
			}
		} finally {

			// Descarrega, se necessário, o buffer
			// (no try... finally para permitir interromper o redraw no meio)
			if (g != saved) {
				saved.drawImage(offscreen, 0, 0, Graphics.LEFT | Graphics.TOP);
			}

		}

	}

	/**
	 * Desenha o balão de texto do cenário
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param largBalao
	 * @param altBalao
	 * @param quadrantePonta
	 *            Quadrante (cartesiano) onde aparece a ponat do balão
	 */
	private void desenhaBalao(Graphics g, int x, int y, int largBalao,
			int altBalao, int quadrantePonta) {

		// Calcula o deslocamento correto da ponta
		int deltaX = largBalao / 2;
		int deltaY = altBalao / 2;
		if (quadrantePonta == 2 || quadrantePonta == 3)
			deltaX = -deltaX;
		if (quadrantePonta == 1 || quadrantePonta == 2)
			deltaY = -deltaY * 3;

		// Elipse principal
		g.fillArc(x, y, largBalao, altBalao, 0, 360);
		// Ponta (desenhada como uma fração de elipse)
		g.fillArc(x + deltaX, y + deltaY, largBalao, altBalao * 2,
				(90 * quadrantePonta) + 120, 25);
	}

	/**
	 * Desenha o baralho do cenário
	 * 
	 * @param g
	 */
	private void desenhaBaralho(Graphics g) {
		for (int i = 0; i < baralhoCenario.size(); i++) {
			Carta c = ((Carta) baralhoCenario.elementAt(i));
			c.desenhaCarta(g);
		}
	}

	/**
	 * Desenha as cartas na mesa (incluindo o vira, se houver)
	 * 
	 * @param g
	 */
	private void desenhaCartas(Graphics g) {

		// Desenha as cartas que já foram jogadas (se houverem),
		// na ordem em que foram jogadas
		for (int i = 0; i < cartasJogadas.size(); i++) {
			Carta c = ((Carta) cartasJogadas.elementAt(i));
			c.desenhaCarta(g);
		}

		// Desenha as cartas restantes.
		for (int i = 0; i < cartas.size(); i++) {
			Carta c = ((Carta) cartas.elementAt(i));
			if (c != null && !isJogada(c)) {
				c.desenhaCarta(g);
				// Se for a carta selecionada pelo jogador, destaca
				if (c.equals(cartaSelecionada)) {
					c.desenhaCursor(g);
				}
			}
		}

		// Desenha a carta destacada por cima das outras, com uma firula
		if (cartaVencedora != null) {
			cartaVencedora.desenhaCarta(g);
			cartaVencedora.destacaVitoriosa(g);
		}

	}

	public void setCartaSelecionada(Carta cartaSelecionada) {
		this.cartaSelecionada = cartaSelecionada;
	}

	public Carta getCartaSelecionada() {
		return cartaSelecionada;
	}

	protected void keyPressed(int tecla) {

		int gameAction = getGameAction(tecla);
		switch (gameAction) {
		case RIGHT:
			if (cartasSelecionaveis != null) {
				trocaCartaSelecionada(true);
			}
			break;
		case LEFT:
			if (cartasSelecionaveis != null) {
				trocaCartaSelecionada(false);
			}
			break;
		case UP:
		case FIRE:
		case DOWN:
			if (cartasSelecionaveis != null && cartaSelecionada != null) {
				// Impede que joguqemos carta fechada quando não devemos
				if (gameAction == DOWN && !podeFechada) {
					break;
				}
				// Remove a borda da carta selecionada (mas guarda ela antes)
				Carta c = cartaSelecionada;
				cartaSelecionada = null;
				// Se for DOWN é pra jogar a carta fechada
				if (gameAction == DOWN) {
					c.setFechada(true);
				}
				// Impede o jogador de jogar novas cartas
				cartasSelecionaveis = null;
				// Informa ao JogadorHumano que a carta foi clicada
				getJogador().cartaClicada(c);
			}
			break;
		default:
			// Se não for tecla de jogo, tenta os atalhos
			switch (tecla) {
			case KEY_NUM1:
				if (isAguardandoAceite) {
					executaComando(MiniTruco.aceitaCommand);
				} else if (isAguardandoMao11) {
					executaComando(MiniTruco.aceitaMao11Command);
				}
				break;
			case KEY_NUM3:
				if (isAguardandoAceite) {
					executaComando(MiniTruco.recusaCommand);
				} else if (isAguardandoMao11) {
					executaComando(MiniTruco.recusaMao11Command);
				}
				break;
			case KEY_NUM9:
				if (jogador != null && jogador.jogo != null
						&& !jogador.jogo.jogoFinalizado)
					executaComando(MiniTruco.mostraNomesJogadoresCommand);
				break;
			}
		}

	}

	/**
	 * Troca a carta selecionada (em resposta a uma tecla)
	 * 
	 * @param proxima
	 *            true vai para a próxima carta, false vai para a anterior
	 */
	private void trocaCartaSelecionada(boolean proxima) {
		int numCarta = cartasSelecionaveis.indexOf(cartaSelecionada)
				+ (proxima ? 1 : -1);
		if (numCarta > (cartasSelecionaveis.size() - 1)) {
			numCarta = 0;
		} else if (numCarta < 0) {
			numCarta = cartasSelecionaveis.size() - 1;
		}
		cartaSelecionada = (Carta) cartasSelecionaveis.elementAt(numCarta);
		repaint();
	}

	public void setCartasSelecionaveis(Vector cartasSelecionaveis) {
		this.cartasSelecionaveis = cartasSelecionaveis;
	}

	public Vector getCartasSelecionaveis() {
		return cartasSelecionaveis;
	}

	public void setAberturaVisivel(boolean logoVisivel) {
		this.aberturaVisivel = logoVisivel;
	}

	public boolean isAberturaVisivel() {
		return aberturaVisivel;
	}

	/**
	 * Joga a carta no meio da mesa, marcando-a como jogada.
	 * 
	 * @param c
	 */
	public void descarta(Carta c, int posicao) {

		// Coloca a carta no meio da tela, mas "puxando" na direção
		// de quem jogou
		int topFinal, leftFinal;
		topFinal = getHeight() / 2 - Carta.alturaCartas / 2;
		leftFinal = getWidth() / 2 - Carta.larguraCartas / 2;
		switch (posicao) {
		case 1:
			topFinal += Carta.alturaCartas / 2;
			break;
		case 2:
			leftFinal += Carta.larguraCartas;
			break;
		case 3:
			topFinal -= Carta.alturaCartas / 2;
			break;
		case 4:
			leftFinal -= Carta.larguraCartas;
			break;
		}

		// Insere um ligeiro fator aleatório, para dar uma bagunçada na mesa
		topFinal += System.currentTimeMillis() % 5 - 2;
		leftFinal += System.currentTimeMillis() % 5 - 2;

		// Sinaliza para evitar escolhas futuras desta carta;
		cartasJogadas.addElement(c);

		// Executa a animação
		animador.moveCarta(c, topFinal, leftFinal, 200);

	}

	public void distribuiCartaDaMesa(Carta c) {
		distribui(c, 5, 0);
	}

	/**
	 * Entrega uma carta na posição apropriada
	 * <p>
	 * 
	 * @param numJogador
	 *            Posição do jogador, de 1 a 4 (1 = humano).
	 * @param i
	 *            posição da carta na mão do jogador (0 a 2)
	 */
	public void distribui(Carta c, int numJogador, int i) {

		// Determina onde vamos colocar a carta (e se ela vem virada)
		int topFinal, leftFinal;
		switch (numJogador) {
		case 1:
			leftFinal = getWidth() / 2 - Carta.larguraCartas + i
					* (Carta.larguraCartas * 2 / 3);
			topFinal = getHeight() - (Carta.alturaCartas + MARGEM);
			if (!this.modoCE)
				c.setVirada(true);
			break;
		case 2:
			leftFinal = getWidth() - Carta.larguraCartas - MARGEM;
			topFinal = getHeight() / 2 - Carta.alturaCartas / 2 - (i - 1) * 4;
			break;
		case 3:
			leftFinal = getWidth() / 2 - Carta.larguraCartas + (2 - i)
					* (Carta.larguraCartas * 2 / 3);
			topFinal = MARGEM;
			break;
		case 4:
			leftFinal = MARGEM;
			topFinal = getHeight() / 2 - Carta.alturaCartas / 2 + (i - 1) * 4;
			break;
		default: // Carta da mesa
			leftFinal = leftCartaDaMesa;
			topFinal = topCartaDaMesa;
			c.setVirada(true);
			c.setCartaEmJogo(false);
			break;
		}

		// Para o jogador da posição superior, inverte a ordem
		// (senão a exibição na mão de 11 fica bagunçada)
		if (numJogador == 3) {
			i = 2 - i;
		}

		// Adiciona a carta na mesa, em cima do baralho
		cartas.setSize(13);
		cartas.setElementAt(c, i + 3 * (numJogador - 1));
		c.setTop(topBaralho);
		c.setLeft(leftBaralho);

		// Anima a carta até a posição correta
		baralhoPorCima = false;
		animador.moveCarta(c, topFinal, leftFinal, 100);
		baralhoPorCima = true;
		repaint();
	}

	/**
	 * Mostra as cartas do parceiro (em uma mão de 11)
	 * 
	 * @param cartasParceiro
	 *            Cartas do parceiro
	 */

	public void mostraCartasParceiro(Carta[] cartasParceiro) {
		// O parceiro é sempre a posição 3, i.e., os elementos 6-8 do cartas.
		// Vamos copiar letra/naipe e exibir
		for (int i = 0; i <= 2; i++) {
			Carta c = (Carta) cartas.elementAt(6 + i);
			c.setNaipe(cartasParceiro[i].getNaipe());
			c.setLetra(cartasParceiro[i].getLetra());
			c.setVirada(true);
		}
	}

	/**
	 * Esconde as cartas do parceiro (usado após definir se a mão de 11 segue)
	 * 
	 */
	public void escondeCartasParceiro() {
		for (int i = 0; i <= 2; i++) {
			Carta c = (Carta) cartas.elementAt(6 + i);
			c.setNaipe(Carta.NAIPE_NENHUM);
			c.setLetra(Carta.LETRA_NENHUMA);
			c.setVirada(false);
		}
	}

	/**
	 * Anima a carta sendo recolhida e a remove das listas de cartas
	 * 
	 * @param c
	 */
	private void recolhe(Carta c) {

		// Tecnicamente não deveria ocorrer, mas em alguns celulares,
		// algumas cartas chegam nulas, então
		if (c == null)
			return;

		// Joga por cima do baralho
		baralhoPorCima = false;
		c.setVirada(false);
		animador.moveCarta(c, topBaralho, leftBaralho, 100);
		baralhoPorCima = true;

		// Some com ela da mesa
		cartas.removeElement(c);
		cartasJogadas.removeElement(c);
	}

	public void limpa() {

		// Limpa os ícones das mãos
		for (int i = 0; i <= 2; i++) {
			iconesRodadas[i] = getIconeRodada(0);
		}

		// Se houver cursor de cartas, desliga
		cartasSelecionaveisSuspensas = null;
		cartasSelecionaveis = null;
		cartaSelecionada = null;

		if (!cartas.isEmpty()) {

			// Recolhe o vira
			recolhe((Carta) cartas.lastElement());

			// Recolhe as cartas jogadas
			for (int i = cartasJogadas.size() - 1; i >= 0; i--) {
				recolhe((Carta) cartasJogadas.elementAt(i));
			}

			// Recolhe as restantes, se houverem
			for (int i = cartas.size() - 1; i >= 0; i--) {
				Carta c = ((Carta) cartas.elementAt(i));
				if (c != null) {
					recolhe(c);
				}
			}

		}

	}

	/**
	 * Cache dos ícones que informam o resultado das rodadas
	 */
	private static Image[] iconesResult;

	/**
	 * Atualiza o placar informando o status de uma rodada
	 * <p>
	 * Tem uma animaçãozinha aqui.
	 * 
	 * @param numRodada
	 *            rodada que queremos informar (1, 2 ou 3)
	 * @param status
	 *            1=vitória, 2=derrota, 3=empate (0=limpa, mas a mesa limpa
	 *            sozinha)
	 */

	public void setStatusMao(int numRodada, int status) {
		// O animador faz tudo (inclusive animar a matriz de status)
		animador.acendeIconeRodada(numRodada, status);
	}

	/**
	 * Recupera o ícone correspondente ao resultado da rodada
	 * 
	 * @param i
	 *            0=vazio; 1=vitória; 2=derrota; 3=empate
	 * @return
	 */
	protected static Image getIconeRodada(int tipo) {
		// Inicializa, se necessario, os bitmaps
		if (iconesResult == null) {
			try {
				Image imgNaipes = Image.createImage("/iconesresult.png");
				iconesResult = new Image[4];
				for (int i = 0; i < 4; i++) {
					iconesResult[i] = Image.createImage(11, 10);
					iconesResult[i].getGraphics().drawImage(imgNaipes, -11 * i,
							0, Graphics.TOP | Graphics.LEFT);
				}
			} catch (IOException e) {
				// Desencana, se nao achar a imagem a casa caiu mesmo
				e.printStackTrace();
			}
		}
		// Recupera o bitmap apropriado
		return iconesResult[tipo];
	}

	public void adicionaComandoAposta(int valorProximaAposta) {
		switch (valorProximaAposta) {
		case 3:
			addCommand(MiniTruco.trucoCommand);
			break;
		case 6:
			addCommand(MiniTruco.seisCommand);
			break;
		case 9:
			addCommand(MiniTruco.noveCommand);
			break;
		case 12:
			addCommand(MiniTruco.dozeCommand);
			break;
		}
	}

	public void removeComandoAposta() {
		removeCommand(MiniTruco.trucoCommand);
		removeCommand(MiniTruco.seisCommand);
		removeCommand(MiniTruco.noveCommand);
		removeCommand(MiniTruco.dozeCommand);
	}

	public void adicionaOpcoesAceite() {
		isAguardandoAceite = true;
		addCommand(MiniTruco.aceitaCommand);
		addCommand(MiniTruco.recusaCommand);
		mostraTextoPiscando(Messages.getString("1desce_3corre")); //$NON-NLS-1$
		removeCommand(MiniTruco.sairPartidaCommand);
	}

	public void removeOpcoesAceite() {
		isAguardandoAceite = false;
		removeCommand(MiniTruco.aceitaCommand);
		removeCommand(MiniTruco.recusaCommand);
		mostraTextoPiscando(null);
		addCommand(MiniTruco.sairPartidaCommand);
	}

	public void adicionaOpcoesMao11() {
		isAguardandoMao11 = true;
		addCommand(MiniTruco.aceitaMao11Command);
		addCommand(MiniTruco.recusaMao11Command);
		mostraTextoPiscando(Messages.getString("1joga_3desiste")); //$NON-NLS-1$
		removeCommand(MiniTruco.sairPartidaCommand);
	}

	public void removeOpcoesMao11() {
		isAguardandoMao11 = false;
		removeCommand(MiniTruco.aceitaMao11Command);
		removeCommand(MiniTruco.recusaMao11Command);
		mostraTextoPiscando(null);
		addCommand(MiniTruco.sairPartidaCommand);
	}

	/**
	 * Ajusta o menu para quando a partida acaba.
	 * <p>
	 * Em jogos stand-alone e no sevidor TCP, isso significa trocar o comando
	 * Encerrar (que pede confirmação) pelo Fim (que volta direto ao menu, ou,
	 * no caso do servidor, à tela da sala).
	 * <p>
	 * Em jogos onde o celular é um cliente Bluetooth, apenas removemos o
	 * comando Encerrar - é quebra de padrão não dar uma opção de encerramento
	 * *apenas* neste momento, mas é melhor que arriscar que o jogador se
	 * desconecte sem querer.
	 */
	public void mostraMenuFimPartida() {
		removeCommand(MiniTruco.sairPartidaCommand);
		// [IF_FULL]
		if (!(getJogador().jogo instanceof JogoBT))
			// [ENDIF_FULL]
			addCommand(MiniTruco.sairPartidaSemPerguntarCommand);
	}

	String textoBalao;

	int posicaoBalao;

	public synchronized void balao(int posicao, String texto, int tempo) {
		animador.mostraBalao(posicao, texto, tempo);
		repaint();
		serviceRepaints();
	}

	public boolean isAppRodando = true;

	/**
	 * Carta em destaque (é desenhada em primeiro lugar com uma firula)
	 */
	public Carta cartaVencedora;

	/**
	 * Se a carta for tocada com a caneta/mouse, seleciona-a
	 */
	protected void pointerPressed(int x, int y) {
		if (cartasSelecionaveis != null) {
			for (int i = 0; i < cartasSelecionaveis.size(); i++) {
				Carta c = (Carta) cartasSelecionaveis.elementAt(i);
				if (c.isClicado(x, y)) {
					cartaSelecionada = c;
					repaint();
				}
			}
		}
	}

	/**
	 * Garante que se a caneta for arrastada, a carta selecionada muda
	 */
	protected void pointerDragged(int x, int y) {
		pointerPressed(x, y);
	}

	/**
	 * Se a caneta/mouse for solta sobre a carta selecionada, seleciona-a
	 */
	protected void pointerReleased(int x, int y) {
		if (cartasSelecionaveis != null) {
			for (int i = 0; i < cartasSelecionaveis.size(); i++) {
				Carta c = (Carta) cartasSelecionaveis.elementAt(i);
				if (cartaSelecionada == c && c.isClicado(x, y)) {
					cartaSelecionada = null;
					// Informa ao JogadorHumano que a carta foi clicada
					getJogador().cartaClicada(c);
				}
			}
		}
	}

	/**
	 * Habilita/desliga texto piscando
	 * 
	 * @param texto
	 *            Texto a piscar, ou null para desligar
	 */
	public void mostraTextoPiscando(String texto) {
		// Apenas guarda o texto e deixa o animador cuidar do resto
		textoPiscando = texto;
		animador.piscaTexto(texto != null);
	}

	/**
	 * @param podeFechada
	 *            The podeFechada to set.
	 */
	public void setPodeFechada(boolean podeFechada) {
		this.podeFechada = podeFechada;
	}

	private int posicaoDaVez;

	/**
	 * Guarda as cartas selecionáveis quando o jogador pede truco (para
	 * recuperar depois que os outros responderem)
	 * 
	 * @see Mesa#suspendeVezParaAumento()
	 */
	private Vector cartasSelecionaveisSuspensas;

	/**
	 * Determina qual posição da tela (não confundir com número do jogador no
	 * jogo) está com a "vez" (para desenhar o indicador)
	 * 
	 * @param posicaoDaVez
	 *            1=inferior, 2=direita, 3=superior, 4=esquerda
	 * @see JogadorHumano#posicaoNaTela(Jogador)
	 */
	public void setPosicaoDaVez(int posicaoDaVez) {
		this.posicaoDaVez = posicaoDaVez;
	}

	/**
	 * Impede que o jogador descarte enquanto está aguardando uma resposta de
	 * pedido de aumento de aposta
	 * 
	 */
	public void suspendeVezParaAumento() {
		this.cartaSelecionada = null;
		this.cartasSelecionaveisSuspensas = this.cartasSelecionaveis;
		this.cartasSelecionaveis = null;
		repaint();
	}

	/**
	 * Permite ao jogador fazer o seu descarte após a situação de aumento de
	 * aposta ter se resolvido.
	 * 
	 */
	public void retomaVezDepoisDoAumento() {
		if (this.cartasSelecionaveis == null) {
			this.cartasSelecionaveis = this.cartasSelecionaveisSuspensas;
			if (this.cartasSelecionaveis != null
					&& this.cartasSelecionaveis.capacity() > 0) {
				this.cartaSelecionada = (Carta) this.cartasSelecionaveis
						.firstElement();
			}
		}
		this.cartasSelecionaveisSuspensas = null;
		repaint();
	}

	/**
	 * Mostra a tela final da abertura, com os elementos já posicionados
	 * 
	 */
	public void mostraFinalAbertura() {
		this.aberturaAlturaSas = Animador.ALTURA_SAS_FINAL;
		this.aberturaNumCartas = 10;
		this.aberturaUrlVisivel = true;
	}

	/**
	 * Comando a ser executado em uma thread separada
	 * 
	 * @see Mesa#executaComando(Command)
	 */
	private Command cmdEmThread;

	/**
	 * Executa comandos do jogo a partir de uma nova thread (permitindo que o
	 * jogador responda assíncronamente com os outros).
	 * <p>
	 * Era parte da classe ThreadComandoMenu (foi movido para cá para reduzir o
	 * .jar)
	 */
	public void executaComando(Command cmd) {
		cmdEmThread = cmd;
		Thread t = new Thread(this);
		t.start();
	}

	/**
	 * @see Mesa#executaComando(Command)
	 */
	public void run() {
		getJogador().executaComando(cmdEmThread);
	}

}
