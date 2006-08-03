package br.inf.chester.minitruco.cliente;

/*
 * Copyright � 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 2 da 
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

import javax.microedition.lcdui.Image;

/**
 * Cuida de todas as anima��es feitas sobre a mesa.
 * <p>
 * A princ�pio poderia estar na classe <code>Mesa</code>, mas separando ajuda
 * a despoluir aquele c�digo um pouco.
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
	 * Quantidade de frames por segundo que desejamos nas anima��es
	 */
	private static final int FPS = 30;

	// Outras constantes

	private static final int NUM_PISCADAS_PLACAR = 6;

	static final int ALTURA_SAS_FINAL = 3;

	/**
	 * Chama o frame de anima��o de tempos em tempos, de forma a atingir a
	 * quantidade de frames por segundo desejada.
	 * <p>
	 * Inspira��o: http://tinyurl.com/bdlza
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
					// N�o faz nada, � s� controle de timing
				}
			}
		}

	}

	/**
	 * Executa um frame da anima��o que est� acontecendo
	 */
	public void frame() {

		// Anima��o da abertura
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

		// Anima��o de cartas se movendo
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

		// �cone da rodada piscando
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

		// Bal�o sendo exibido
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

	// Contadores gen�ricos
	private int frameAtual, numFrames;

	// Vari�veis de placar
	private String stringPlacarAtual;

	private int numPlacarPiscando = 0;

	// Constantes/Vari�veis de �cones de rodada
	private static final int NUM_PISCADAS_ICONE_RODADA = 3;

	private static final Image iconeRodadaApagado = Mesa.getIconeRodada(0);

	private int numRodadaPiscando = 0;

	private int piscadaAtual;

	private Image iconeRodadaAceso;

	// Vari�veis de cartas animadas
	private Carta cartaAnimada = null;

	private int topInicial, leftInicial, topFinal, leftFinal;

	// Vari�veis de texto piscando
	private boolean isTextoPiscando = false;

	private int numFramesPiscaTexto, numFramesPassados;

	// Vari�veis da abertura
	public boolean aberturaAnimando = false;

	public int framesCartaAtual = 0;

	/**
	 * Espera outras anima��es finalizarem (para evitar concorr�ncia entre
	 * threads e outras doideiras)
	 * 
	 */
	private void aguardaFimAnimacoes() {
		// No caso particular da anima��o da abertura, ao inv�s de aguardar,
		// mata ela (afinal, ela � perfumaria)
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
				// N�o faz nada, � s� controle de timing
			}
		}
	}

	private boolean isMostrandoBalao = false;

	/**
	 * Anima uma carta at� o seu destino
	 * 
	 * @param c
	 * @param topFinal
	 * @param leftFinal
	 * @param tempoMs
	 */
	public void moveCarta(Carta c, int topFinal, int leftFinal, int tempoMs) {

		// Aguarda a finaliza��o de outras anima��es
		aguardaFimAnimacoes();

		// Se as anima��es estiverem desligadas, move a carta e pronto
		if (!animacaoLigada) {
			c.setTop(topFinal);
			c.setLeft(leftFinal);
			mesa.repaint();
			mesa.serviceRepaints();
			return;
		}

		// Determina quantos frames vai durar essa anima��o
		this.numFrames = Math.max(1, FPS * tempoMs / 1000);

		// Guarda as coordenadas de origem e destino da carta
		this.topInicial = c.getTop();
		this.leftInicial = c.getLeft();
		this.topFinal = topFinal;
		this.leftFinal = leftFinal;

		// Inicia a anima��o (a thread monitora a propriedade cartaAnimada)
		this.frameAtual = 0;
		cartaAnimada = c;

		// Aguarda o final da anima��o
		while (mesa.isAppRodando && cartaAnimada != null) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// N�o faz nada, � s� controle de timing
			}
		}

	}

	public void animaAbertura() {

		// Aguarda a finaliza��o de outras anima��es
		// (imposs�vel, mas o futuro a Deus pertence)
		aguardaFimAnimacoes();

		// Se as anima��es estiverem desligadas, seta os valores finais
		if (!animacaoLigada) {
			mesa.mostraFinalAbertura();
			mesa.repaint();
			mesa.serviceRepaints();
			return;
		}

		// Inicializa as vari�veis
		
		mesa.aberturaAlturaSas = mesa.getHeight();
		mesa.aberturaNumCartas = 0;
		mesa.aberturaUrlVisivel = false;

		// Inicia a anima��o (a thread monitora a propriedade aberturaAnimando)
		this.frameAtual = 0;
		aberturaAnimando = true;

		// Aguarda o final da anima��o
		while (mesa.isAppRodando && aberturaAnimando) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// N�o faz nada, � s� controle de timing
			}
		}

	}

	public void mostraBalao(int posicao, String texto, int tempoMs) {

		// Aguarda a finaliza��o de outras anima��es
		aguardaFimAnimacoes();

		// Calcula a quantidade de frames em que o bal�o aparece
		frameAtual = 0;
		numFrames = Math.max(1, FPS * tempoMs / 1000);

		// Mostra o bal�o
		mesa.textoBalao = texto;
		mesa.posicaoBalao = posicao;

		isMostrandoBalao = true;

		// Aguarda o final da anima��o
		while (mesa.isAppRodando && isMostrandoBalao) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// N�o faz nada, � s� controle de timing
			}
		}

	}

	public void piscaPlacar(int numPlacar, int pontos) {

		// Aguarda a finaliza��o de outras anima��es
		aguardaFimAnimacoes();

		// Inicializa os par�metros e acende o �cone
		frameAtual = 0;
		numFrames = FPS / 8; // 1/8 de segundo por "meia-piscada"
		stringPlacarAtual = (numPlacar == 1 ? Mesa.STRING_NOS
				: Mesa.STRING_ELES)
				+ pontos;
		piscadaAtual = 0;

		// Inicia a anima��o
		numPlacarPiscando = numPlacar;

		// Aguarda o final
		while (mesa.isAppRodando && numPlacarPiscando != 0) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// N�o faz nada, � s� controle de timing
			}
		}

	}

	/**
	 * Acende o �cone de status da rodada (dando umas piscadinhas antes)
	 * 
	 * @param numRodada
	 *            Rodada (1-3) cujo �cone queremos acender
	 * @param status
	 *            Resultado da rodada (vit�ria, empate ou derrota)
	 */
	public void acendeIconeRodada(int numRodada, int status) {

		// Aguarda a finaliza��o de outras anima��es
		aguardaFimAnimacoes();

		// Inicializa os par�metros e acende o �cone
		frameAtual = 0;
		numFrames = FPS / 4; // 1/4 de segundo por "meia-piscada"
		iconeRodadaAceso = Mesa.getIconeRodada(status);
		piscadaAtual = 0;
		mesa.iconesRodadas[numRodada - 1] = iconeRodadaAceso;

		// Inicia a anima��o
		numRodadaPiscando = numRodada;

		// Aguarda o final
		while (mesa.isAppRodando && numRodadaPiscando != 0) {
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// N�o faz nada, � s� controle de timing
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

		// Esta anima��o � ass�ncrona em rela��o �s outras, logo n�o vamos
		// aguardar nenhum final, nem ficar esperando no final dela.
		//
		// Tudo o que faremos � ajustar o status, o contador de frames e o
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
