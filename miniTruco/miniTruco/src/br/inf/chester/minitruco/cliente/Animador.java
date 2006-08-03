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

import javax.microedition.lcdui.Image;

/**
 * Cuida de todas as animações feitas sobre a mesa.
 * <p>
 * A princípio poderia estar na classe <code>Mesa</code>, mas separando ajuda
 * a despoluir aquele código um pouco.
 * 
 * @author Chester
 * 
 */
public class Animador extends Thread {

	private static boolean animacaoLigada = true;

	private Mesa mesa;

	public Animador(Mesa mesa) {
		this.mesa = mesa;
	}

	/**
	 * Quantidade de frames por segundo que desejamos nas animações
	 */
	private static final int FPS = 30;

	// Outras constantes

	private static final int NUM_PISCADAS_PLACAR = 6;

	static final int ALTURA_SAS_FINAL = 3;

	/**
	 * Chama o frame de animação de tempos em tempos, de forma a atingir a
	 * quantidade de frames por segundo desejada.
	 * <p>
	 * Inspiração: http://tinyurl.com/bdlza
	 */
	public void run() {
		long timestampInicio, timestampFim;
		// Determina o tempo ideal (em ms)
		int tempoDesejado = 1000 / FPS;
		while (mesa.isAppRodando) {
			// Executa um frame, medindo o tempo que demorou
			timestampInicio = System.currentTimeMillis();
			frame();
			timestampFim = System.currentTimeMillis();
			int tempoDecorrido = (int) (timestampFim - timestampInicio);
			// Coloca a thread para dormir o restante do tempo
			if (tempoDecorrido < tempoDesejado) {
				try {
					sleep(tempoDesejado - tempoDecorrido);
				} catch (InterruptedException e) {
					// Não faz nada, é só controle de timing
				}
			}
		}

	}

	/**
	 * Executa um frame da animação que está acontecendo
	 */
	public void frame() {

		// Animação da abertura
		if (aberturaAnimando) {
			frameAtual++;
			if (mesa.aberturaAlturaSas > ALTURA_SAS_FINAL) {
				// Sobe o "sas apresenta" seis linhas por frame
				mesa.aberturaAlturaSas = Math.max(mesa.aberturaAlturaSas - 6,
						ALTURA_SAS_FINAL);
			} else if (mesa.aberturaNumCartas < 10) {
				// A cada 10 frames, mostra uma carta
				framesCartaAtual++;
				if (framesCartaAtual > 4) {
					mesa.aberturaNumCartas++;
					framesCartaAtual = 0;
				}
			} else {
				// Aproveita o contador pra dar mais um tempinho, mostra a URL e boa
				framesCartaAtual++;
				if (framesCartaAtual > 8) {
					mesa.aberturaUrlVisivel = true;
					aberturaAnimando = false;
				}
			}
			mesa.repaint();
			mesa.serviceRepaints();
		}

		// Animação de cartas se movendo
		if (cartaAnimada != null) {
			frameAtual++;
			cartaAnimada.setTop(topInicial + (topFinal - topInicial)
					* frameAtual / numFrames);
			cartaAnimada.setLeft(leftInicial + (leftFinal - leftInicial)
					* frameAtual / numFrames);
			if (frameAtual == numFrames) {
				cartaAnimada = null;
			}
			mesa.repaint();
			mesa.serviceRepaints();
		}

		// ícone da rodada piscando
		if (numRodadaPiscando != 0) {
			if (frameAtual < numFrames) {
				frameAtual++;
			} else {
				frameAtual = 0;
				if (mesa.iconesRodadas[numRodadaPiscando - 1] == iconeRodadaAceso) {
					mesa.iconesRodadas[numRodadaPiscando - 1] = iconeRodadaApagado;
				} else {
					mesa.iconesRodadas[numRodadaPiscando - 1] = iconeRodadaAceso;
					piscadaAtual++;
				}
				if (piscadaAtual == NUM_PISCADAS_ICONE_RODADA) {
					numRodadaPiscando = 0;
				}
				mesa.repaint();
				mesa.serviceRepaints();
			}
		}

		// Placar piscando
		if (numPlacarPiscando != 0) {
			if (frameAtual < numFrames) {
				frameAtual++;
			} else {
				frameAtual = 0;
				if (!mesa.stringPlacar[numPlacarPiscando - 1].equals("")) {
					mesa.stringPlacar[numPlacarPiscando - 1] = "";
				} else {
					mesa.stringPlacar[numPlacarPiscando - 1] = stringPlacarAtual;
					piscadaAtual++;
				}
				if (piscadaAtual == NUM_PISCADAS_PLACAR) {
					numPlacarPiscando = 0;
				}
				mesa.repaint();
				mesa.serviceRepaints();
			}
		}

		// Balão sendo exibido
		if (isMostrandoBalao) {
			if (frameAtual == 0) {
				mesa.repaint();
				mesa.serviceRepaints();
			}
			frameAtual++;
			if (frameAtual > numFrames) {
				mesa.posicaoBalao = 0;
				isMostrandoBalao = false;
				// mesa.repaint();
				// mesa.serviceRepaints();
			}
		}

		// Texto piscando (atualiza a tela a cada n frames)
		if (isTextoPiscando) {
			numFramesPassados++;
			if (numFramesPassados == numFramesPiscaTexto) {
				numFramesPassados = 0;
				mesa.repaint();
				mesa.serviceRepaints();
			}
		}

	}

	// Contadores genéricos
	private int frameAtual, numFrames;

	// Variáveis de placar
	private String stringPlacarAtual;

	private int numPlacarPiscando = 0;

	// Constantes/Variáveis de ícones de rodada
	private static final int NUM_PISCADAS_ICONE_RODADA = 3;

	private static final Image iconeRodadaApagado = Mesa.getIconeRodada(0);

	private int numRodadaPiscando = 0;

	private int piscadaAtual;

	private Image iconeRodadaAceso;

	// Variáveis de cartas animadas
	private Carta cartaAnimada = null;

	private int topInicial, leftInicial, topFinal, leftFinal;

	// Variáveis de texto piscando
	private boolean isTextoPiscando = false;

	private int numFramesPiscaTexto, numFramesPassados;

	// Variáveis da abertura
	public boolean aberturaAnimando = false;

	public int framesCartaAtual = 0;

	/**
	 * Espera outras animações finalizarem (para evitar concorrência entre
	 * threads e outras doideiras)
	 * 
	 */
	private void aguardaFimAnimacoes() {
		// No caso particular da animação da abertura, ao invés de aguardar,
		// mata ela (afinal, ela é perfumaria)
		if (aberturaAnimando) {
			aberturaAnimando = false;
			mesa.mostraFinalAbertura();
		}
		
		while (mesa.isAppRodando
				&& (numRodadaPiscando != 0 || numPlacarPiscando != 0
						|| isMostrandoBalao || cartaAnimada != null)) {
			try {
				sleep(10);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}
	}

	private boolean isMostrandoBalao = false;

	/**
	 * Anima uma carta até o seu destino
	 * 
	 * @param c
	 * @param topFinal
	 * @param leftFinal
	 * @param tempoMs
	 */
	public void moveCarta(Carta c, int topFinal, int leftFinal, int tempoMs) {

		// Aguarda a finalização de outras animações
		aguardaFimAnimacoes();

		// Se as animações estiverem desligadas, move a carta e pronto
		if (!animacaoLigada) {
			c.setTop(topFinal);
			c.setLeft(leftFinal);
			mesa.repaint();
			mesa.serviceRepaints();
			return;
		}

		// Determina quantos frames vai durar essa animação
		this.numFrames = Math.max(1, FPS * tempoMs / 1000);

		// Guarda as coordenadas de origem e destino da carta
		this.topInicial = c.getTop();
		this.leftInicial = c.getLeft();
		this.topFinal = topFinal;
		this.leftFinal = leftFinal;

		// Inicia a animação (a thread monitora a propriedade cartaAnimada)
		this.frameAtual = 0;
		cartaAnimada = c;

		// Aguarda o final da animação
		while (mesa.isAppRodando && cartaAnimada != null) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}

	}

	public void animaAbertura() {

		// Aguarda a finalização de outras animações
		// (impossível, mas o futuro a Deus pertence)
		aguardaFimAnimacoes();

		// Se as animações estiverem desligadas, seta os valores finais
		if (!animacaoLigada) {
			mesa.mostraFinalAbertura();
			mesa.repaint();
			mesa.serviceRepaints();
			return;
		}

		// Inicializa as variáveis
		
		mesa.aberturaAlturaSas = mesa.getHeight();
		mesa.aberturaNumCartas = 0;
		mesa.aberturaUrlVisivel = false;

		// Inicia a animação (a thread monitora a propriedade aberturaAnimando)
		this.frameAtual = 0;
		aberturaAnimando = true;

		// Aguarda o final da animação
		while (mesa.isAppRodando && aberturaAnimando) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}

	}

	public void mostraBalao(int posicao, String texto, int tempoMs) {

		// Aguarda a finalização de outras animações
		aguardaFimAnimacoes();

		// Calcula a quantidade de frames em que o balão aparece
		frameAtual = 0;
		numFrames = Math.max(1, FPS * tempoMs / 1000);

		// Mostra o balão
		mesa.textoBalao = texto;
		mesa.posicaoBalao = posicao;

		isMostrandoBalao = true;

		// Aguarda o final da animação
		while (mesa.isAppRodando && isMostrandoBalao) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}

	}

	public void piscaPlacar(int numPlacar, int pontos) {

		// Aguarda a finalização de outras animações
		aguardaFimAnimacoes();

		// Inicializa os parâmetros e acende o ícone
		frameAtual = 0;
		numFrames = FPS / 8; // 1/8 de segundo por "meia-piscada"
		stringPlacarAtual = (numPlacar == 1 ? Mesa.STRING_NOS
				: Mesa.STRING_ELES)
				+ pontos;
		piscadaAtual = 0;

		// Inicia a animação
		numPlacarPiscando = numPlacar;

		// Aguarda o final
		while (mesa.isAppRodando && numPlacarPiscando != 0) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}

	}

	/**
	 * Acende o ícone de status da rodada (dando umas piscadinhas antes)
	 * 
	 * @param numRodada
	 *            Rodada (1-3) cujo ícone queremos acender
	 * @param status
	 *            Resultado da rodada (vitória, empate ou derrota)
	 */
	public void acendeIconeRodada(int numRodada, int status) {

		// Aguarda a finalização de outras animações
		aguardaFimAnimacoes();

		// Inicializa os parâmetros e acende o ícone
		frameAtual = 0;
		numFrames = FPS / 4; // 1/4 de segundo por "meia-piscada"
		iconeRodadaAceso = Mesa.getIconeRodada(status);
		piscadaAtual = 0;
		mesa.iconesRodadas[numRodada - 1] = iconeRodadaAceso;

		// Inicia a animação
		numRodadaPiscando = numRodada;

		// Aguarda o final
		while (mesa.isAppRodando && numRodadaPiscando != 0) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// Não faz nada, é só controle de timing
			}
		}
	}

	/**
	 * Liga/desliga o pisca-pisca de texto
	 * 
	 * @param status
	 *            true para ligar, false para desligar
	 */
	public void piscaTexto(boolean status) {

		// Esta animação é assíncrona em relação às outras, logo não vamos
		// aguardar nenhum final, nem ficar esperando no final dela.
		//
		// Tudo o que faremos é ajustar o status, o contador de frames e o
		// indicador de quando deve piscar.

		isTextoPiscando = status;
		numFramesPassados = 0;
		numFramesPiscaTexto = Math.max(1, (FPS * 20) / 100);

	}

	public static void setAnimacaoLigada(boolean a) {
		animacaoLigada = a;
	}

	public static boolean isAnimacaoLigada() {
		return animacaoLigada;
	}
}
