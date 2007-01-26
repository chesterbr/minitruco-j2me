package mt;

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

/**
 * Base para os diversos tipos de jogador que podem participar de um jogo.
 * <p>
 * A classe Jogo se conecta em quatro inst�ncias desta classe para iniciar uma
 * partida. Ela informa os eventos do jogo (ex.: uma carta foi jogada, � a vez
 * do jogador, fim de rodada, etc.) chamando os m�todos apropriados. Estes
 * m�todos devem ser encarados como mensagens, e, sempre que necess�rio,
 * processados ass�ncronamente.
 * <p>
 * � importante notar que os m�todos s�o acionados independente do jogador. Por
 * exemplo, se o jogador quer jogar uma carta (chaamndo o m�todo jogaCarta do
 * Jogo), e a jogada � v�lida, todos os jogadores (inclusive quem jogou)
 * receber�o a mensagem cartaJogada().
 * <p>
 * O tipo de subclasse determina se o jogador � o usu�rio do celular, um jogador
 * virtual ou um jogador de outro celular conectado remotamente.
 * 
 * @author Chester
 * 
 */
public abstract class Jogador {

	// Vari�veis / M�todos �teis

	private int posicao = 0;

	private Carta[] cartas;

	/**
	 * Jogo que est� sendo jogado por este jogador
	 */
	protected Jogo jogo;

	/**
	 * Recupera a posi��o do jogador no jogo
	 * 
	 * @return n�mero de 1 a 4 (n�o necessariamente a posi��o dele na mesa)
	 */
	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	/**
	 * Recupera a equipe em que este jogador est� (assumindo que ele j� esteja
	 * aceito em um jogo)
	 * 
	 * @return 1 ou 2
	 */
	public int getEquipe() {
		return 1 + ((1 + posicao) % 2);
	}

	/**
	 * Recupera a posi��o do parceiro
	 * 
	 * @return n�mero de 1 a 4
	 */
	public int getParceiro() {
		return 1 + ((posicao + 1) % 4);
	}

	public int getEquipeAdversaria() {
		return 1 + (posicao % 2);
	}

	public void setCartas(Carta[] cartas) {
		this.cartas = cartas;
	}

	public Carta[] getCartas() {
		return cartas;
	}

	public boolean possuiCarta(Carta c) {
		if (cartas == null) {
			return false;
		}
		for (int i = 0; i < cartas.length; i++) {
			if (cartas[i].equals(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Informa que uma carta foi jogada na mesa.
	 * 
	 * @param j
	 *            Jogador que jogou a carta
	 * @param c
	 *            Carta jogada
	 */
	public abstract void cartaJogada(Jogador j, Carta c);

	/**
	 * Informa ao jogador que uma nova m�o est� iniciando.
	 * <p>
	 * Ao receber esta mensagem, as cartas do jogador j� foram atribu�das via
	 * setCartas(), e a carta virada j� est� dispon�vel via getCarta().
	 */
	public abstract void inicioMao();

	/**
	 * Informa que uma partida come�ou. N�o � obrigat�rio tratar - at� porque o
	 * inicioMao ser� chamado logo em seguida.
	 */
	public abstract void inicioPartida();

	/**
	 * Informa que � a vez de um jogador jogar.
	 * 
	 * @param j
	 *            Jogador cuja vez chegou
	 * @param podeFechada
	 *            true se o jogador pode jogar carta fechada, false se n�o pod
	 */
	public abstract void vez(Jogador j, boolean podeFechada);

	/**
	 * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
	 * 
	 * @param j
	 *            Jogador que pediu o aumento
	 * @param valor
	 *            Quanto a m�o passar� a valar se algum advers�rio aceitar
	 */
	public abstract void pediuAumentoAposta(Jogador j, int valor);

	/**
	 * Informa que o jogador aceitou um pedido de aumento de aposta.
	 * 
	 * @param j
	 *            Jogador que aceitou o aumento
	 * @param valor
	 *            Quanto a m�o est� valendo agora
	 */
	public abstract void aceitouAumentoAposta(Jogador j, int valor);

	/**
	 * Informa que o jogador recusou um pedido de aumento de aposta.
	 * <p>
	 * Obs.: isso n�o impede que o outro jogador da dupla aceite o pedido, �
	 * apenas para notifica��o visual. Se o segundo jogdor recusar o pedido, a
	 * mensagem de derrota da dupla ser� enviada logo em seguida.
	 * 
	 * @param j
	 *            Jogador que recusou o pedido.
	 */
	public abstract void recusouAumentoAposta(Jogador j);

	/**
	 * Informa o jogador que a rodada foi fechada
	 * 
	 * @param numRodada
	 *            1 a 3, rodada que foi fechada
	 * @param resultado
	 *            1 se a equipe 1+3 venceu, 2 se a equipe 2+4 venceu, 3 se
	 *            empatou
	 * @param jogadorQueTorna
	 *            jogador que venceu a rodada (e que ir� "tornar"), ou null se
	 *            for empate
	 */
	public abstract void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna);

	/**
	 * Informa que a m�o foi conclu�da
	 * 
	 * @param pontosEquipe
	 *            Array com os pontos da equipe 1 e 2 (�ndices 0 e 1)
	 */
	public abstract void maoFechada(int[] pontosEquipe);

	/**
	 * Informa que o jogo foi conclu�do
	 * 
	 * @param numEquipeVencedora
	 *            Equipe que ganhou o jogo (1 ou 2)
	 */
	public abstract void jogoFechado(int numEquipeVencedora);

	/**
	 * Informa que um jogador fez sua escolha de topar ou n�o uma rodada quando
	 * sua equipe tinha 11 pontos
	 * 
	 * @param j
	 *            Jogador que fez a escolha
	 * @param aceita
	 *            true se o jogador topou, false se recusou
	 */
	public abstract void decidiuMao11(Jogador j, boolean aceita);

	/**
	 * Informa que o jogador � benefici�rio de uma "m�o de 11", e, portanto,
	 * deve decidir se aceita ou n�o esta rodada (se aceitar vale 3 pontos, se
	 * ambos recusarem perde 1)
	 * 
	 * @param cartasParceiro
	 *            Cartas do parceiro
	 * @see Jogo#decideMao11(Jogador, boolean)
	 * 
	 */
	public abstract void informaMao11(Carta[] cartasParceiro);

	/**
	 * Informa que o jogo foi abandonado por alguma causa externa (ex.: um
	 * jogador desistiu)
	 * 
	 * @param posicao
	 *            Posi��o do jogador que abortou
	 */
	public abstract void jogoAbortado(int posicao);

}
