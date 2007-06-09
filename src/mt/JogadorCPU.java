package mt;

/*
 * Copyright � 2005-2007 Carlos Duarte do Nascimento (Chester)
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

import java.util.Random;
import java.util.Vector;

/**
 * Jogador controlado pelo celular ou pelo servidor.
 * <p>
 * � preciso "plugar" uma estrat�gia para que o jogador funcione.
 * 
 * @author Chester
 * @see Estrategia
 * 
 */
public class JogadorCPU extends Jogador implements Runnable {

	/**
	 * Estrategia que est� controlando este jogador
	 */
	private Estrategia estrategia;

	/**
	 * Situa��o atual do jogo (para o estrategia)
	 */
	SituacaoJogo situacaoJogo = new SituacaoJogo();

	/**
	 * Cria um novo jogador CPU, usando a estrat�gia fornecida.
	 * 
	 * @param estrategia
	 *            Estrat�gia a ser adotada por este jogador
	 */
	public JogadorCPU(Estrategia estrategia) {
		this.estrategia = estrategia;
	}

	/**
	 * Cria um novo jogador CPU, buscando a estrat�gia pelo nome.
	 * <p>
	 * Se o nome for "Sortear", escolhe ao acaso uma estrat�gia dentre as
	 * dispon�veis
	 * 
	 * @param nomeEstrategia
	 *            Nome da estrat�gia (ex.: "Willian") ou "Sortear" para uma
	 *            aleat�ria
	 */
	public JogadorCPU(String nomeEstrategia) {
		Random r = new Random();
		while (nomeEstrategia.equals("Sortear")) {
			nomeEstrategia = MiniTruco.OPCOES_ESTRATEGIAS[(r.nextInt() >>> 1)
					% (MiniTruco.OPCOES_ESTRATEGIAS.length)];
		}
		if (nomeEstrategia.equals("Sellani")) {
			this.estrategia = new EstrategiaSellani();
		} else if (nomeEstrategia.equals("Willian")) {
			this.estrategia = new EstrategiaWillian();
		} else if (nomeEstrategia.equals("Gasparotto v1.1")) {
			this.estrategia = new EstrategiaGasparotto();
		} else {
			MiniTruco.log("estrategia invalida:" + nomeEstrategia);
		}
	}

	/**
	 * Quantidade de jogadores cuja resposta estamos esperando para um pedido de
	 * truco.
	 */
	private int numRespostasAguardando = 0;

	/**
	 * Sinaliza se os advers�rios aceitaram um pedido de truco
	 */
	private boolean aceitaramTruco;

	private static Random random = new Random();

	public void vez(Jogador j, boolean podeFechada) {

		if (this.equals(j)) {

			// D� um tempinho, pra fingir que est� "pensando"
			try {
				Thread.sleep(Math.abs(random.nextInt()) % 250 + 200);
			} catch (InterruptedException e) {
				// Nada, apenas timing...
			}

			// Atualiza a situa��o do jogo (incluindo as cartas na m�o)
			atualizaSituacaoJogo();
			situacaoJogo.podeFechada = podeFechada;

			// Solicita que o estrategia jogue
			int posCarta = estrategia.joga(situacaoJogo);

			// Se houve truco, processa, e, ap�s tudo resolvido, repete a jogada
			if (posCarta == -1) {

				// Faz a solicita��o de truco numa nova thread
				// (usando o pr�prio JogadorCPU como Runnable - era uma inner
				// class, mas otimizei para reduzir o .jar)
				aceitaramTruco = false;
				numRespostasAguardando = 2;
				Thread t = new Thread(this);
				t.start();
				// Aguarda pelas respostas
				while (numRespostasAguardando > 0) {
					Thread.yield();
				}
				// Se n�o aceitaram, desencana...
				if (!aceitaramTruco)
					return;
				// ...caso contr�rio, vamos seguir o jogo
				// atualizaSituacaoJogo();
				situacaoJogo.valorProximaAposta = 0;
				posCarta = estrategia.joga(situacaoJogo);
			}

			// Joga a carta selecionada e remove ela da m�o
			boolean isFechada = posCarta >= 10;
			if (isFechada) {
				posCarta -= 10;
			}

			Carta c = (Carta) cartasRestantes.elementAt(posCarta);
			c.setFechada(isFechada && podeFechada);
			cartasRestantes.removeElement(c);
			jogo.jogaCarta(this, c);

		}
	}

	/**
	 * Envia a notifica��o de aumento de aposta.
	 * <p>
	 * � feito em thread separada para que o vez() aguarde as respostas sem se
	 * perder.
	 */
	public void run() {
		jogo.aumentaAposta(this);
	}

	public void pediuAumentoAposta(Jogador j, int valor) {

		// Notifica o estrategia
		estrategia.pediuAumentoAposta(j.getPosicao(), valor);

		// Se foi a equipe oposta que pediu, gera uma resposta
		if (j.getEquipe() == this.getEquipeAdversaria()) {
			atualizaSituacaoJogo();
			// O if e o synchronzied garantem que, se um jogador aceitar o
			// truco, o estrategia do outro n�o � consultado (caso o fosse, ele
			// receberia informacoes posteriores ao aceite)
			synchronized (jogo) {
				if (situacaoJogo.posJogadorPedindoAumento != 0) {
					MiniTruco.log("Jogador " + this.getPosicao()
							+ " vai avaliar truco");
					boolean resposta = estrategia.aceitaTruco(situacaoJogo);
					jogo.respondeAumento(this, resposta);
				}
			}
		}
	}

	/**
	 * Atualiza a situa��o do jogo (para as estrat�gias)
	 */
	private void atualizaSituacaoJogo() {
		jogo.atualizaSituacao(situacaoJogo, this);
		if (jogo.isAlguemTem11Pontos()) {
			situacaoJogo.valorProximaAposta = 0;
		} else {
			situacaoJogo.valorProximaAposta = valorProximaAposta;
		}
		int numCartas = cartasRestantes.size();
		situacaoJogo.cartasJogador = new Carta[numCartas];
		for (int i = 0; i < numCartas; i++) {
			Carta c = (Carta) cartasRestantes.elementAt(i);
			situacaoJogo.cartasJogador[i] = new Carta(c.getLetra(), c
					.getNaipe());
		}
	}

	int valorProximaAposta;

	public void aceitouAumentoAposta(Jogador j, int valor) {

		// Notifica o estrategia
		estrategia.aceitouAumentoAposta(j.getPosicao(), valor);

		// Se estou esperando resposta, contabiliza
		if (numRespostasAguardando > 0) {
			numRespostasAguardando = 0;
			aceitaramTruco = true;
		}

		if (j.getEquipe() == this.getEquipe()) {
			// N�s aceitamos um truco, ent�o podemos aumentar
			// (i.e., se foi truco, podemos pedir 6, se for 6, podemos pedir 9,
			// etc.) at� o limite de 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}

	}

	public void recusouAumentoAposta(Jogador j) {

		// Notifica o estrategia
		estrategia.recusouAumentoAposta(j.getPosicao());

		// Se estivermos aguardando resposta, contabiliza (e deixa o advers�rio
		// perceber)
		if (numRespostasAguardando > 0) {
			numRespostasAguardando--;
			Thread.yield();
		}

	}

	public void jogadaRecusada(int numJogadores, int equipeTrucando,
			Jogador jogadorDaVez) {
		// N�o faz nada
	}

	public void rodadaFechada(int numMao, int resultado, Jogador jogadorQueTorna) {
		// N�o faz nada
	}

	public void maoFechada(int[] pontosEquipe, int[] vaquinhasNoPasto) {
		// N�o faz nada
	}

	public void jogoFechado(int numEquipeVencedora, int[] vaquinhasNoPasto) {
		// N�o faz nada
	}

	public void cartaJogada(Jogador j, Carta c) {
		// N�o faz nada
	}

	public void inicioMao() {

		// Notifica o estrategia
		estrategia.inicioMao();

		// Guarda as cartas que est�o na m�o do jogador
		cartasRestantes.removeAllElements();
		for (int i = 0; i <= 2; i++) {
			cartasRestantes.addElement(this.getCartas()[i]);
		}

		// Libera o jogador para pedir truco (se nao estivermos em mao de 11)
		valorProximaAposta = (jogo.isAlguemTem11Pontos() ? 0 : 3);

	}

	/**
	 * Cartas que ainda n�o foram jogadas
	 */
	private Vector cartasRestantes = new Vector(3);

	public void inicioPartida() {
		// Avisa o estrategia
		estrategia.inicioPartida();
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// Por ora n�o faz nada
	}

	public void informaMao11(Carta[] cartasParceiro) {
		// Pergunta ao estrategia se ele topa a m�o de 11, devolvendo
		// a resposta para o jogo
		atualizaSituacaoJogo();
		MiniTruco.log("J" + getPosicao() + " decidindo mao 11, com cartas "
				+ situacaoJogo.cartasJogador[0] + ","
				+ situacaoJogo.cartasJogador[1] + ","
				+ situacaoJogo.cartasJogador[2] + " e parceiro com "
				+ cartasParceiro[0] + "," + cartasParceiro[1] + ","
				+ cartasParceiro[2]);
		jogo.decideMao11(this, estrategia.aceitaMao11(cartasParceiro,
				situacaoJogo));

	}

	public void jogoAbortado(int posicao) {
		// N�o precisa tratar
	}

}
