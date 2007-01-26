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

/**
 * Base para os diversos tipos de jogador que podem participar de um jogo.
 * <p>
 * A classe Jogo se conecta em quatro instâncias desta classe para iniciar uma
 * partida. Ela informa os eventos do jogo (ex.: uma carta foi jogada, é a vez
 * do jogador, fim de rodada, etc.) chamando os métodos apropriados. Estes
 * métodos devem ser encarados como mensagens, e, sempre que necessário,
 * processados assíncronamente.
 * <p>
 * É importante notar que os métodos são acionados independente do jogador. Por
 * exemplo, se o jogador quer jogar uma carta (chaamndo o método jogaCarta do
 * Jogo), e a jogada é válida, todos os jogadores (inclusive quem jogou)
 * receberão a mensagem cartaJogada().
 * <p>
 * O tipo de subclasse determina se o jogador é o usuário do celular, um jogador
 * virtual ou um jogador de outro celular conectado remotamente.
 * 
 * @author Chester
 * 
 */
public abstract class Jogador {

	// Variáveis / Métodos úteis

	private int posicao = 0;

	private Carta[] cartas;

	/**
	 * Jogo que está sendo jogado por este jogador
	 */
	protected Jogo jogo;

	/**
	 * Recupera a posição do jogador no jogo
	 * 
	 * @return número de 1 a 4 (não necessariamente a posição dele na mesa)
	 */
	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	/**
	 * Recupera a equipe em que este jogador está (assumindo que ele já esteja
	 * aceito em um jogo)
	 * 
	 * @return 1 ou 2
	 */
	public int getEquipe() {
		return 1 + ((1 + posicao) % 2);
	}

	/**
	 * Recupera a posição do parceiro
	 * 
	 * @return número de 1 a 4
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
	 * Informa ao jogador que uma nova mão está iniciando.
	 * <p>
	 * Ao receber esta mensagem, as cartas do jogador já foram atribuídas via
	 * setCartas(), e a carta virada já está disponível via getCarta().
	 */
	public abstract void inicioMao();

	/**
	 * Informa que uma partida começou. Não é obrigatório tratar - até porque o
	 * inicioMao será chamado logo em seguida.
	 */
	public abstract void inicioPartida();

	/**
	 * Informa que é a vez de um jogador jogar.
	 * 
	 * @param j
	 *            Jogador cuja vez chegou
	 * @param podeFechada
	 *            true se o jogador pode jogar carta fechada, false se não pod
	 */
	public abstract void vez(Jogador j, boolean podeFechada);

	/**
	 * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
	 * 
	 * @param j
	 *            Jogador que pediu o aumento
	 * @param valor
	 *            Quanto a mão passará a valar se algum adversário aceitar
	 */
	public abstract void pediuAumentoAposta(Jogador j, int valor);

	/**
	 * Informa que o jogador aceitou um pedido de aumento de aposta.
	 * 
	 * @param j
	 *            Jogador que aceitou o aumento
	 * @param valor
	 *            Quanto a mão está valendo agora
	 */
	public abstract void aceitouAumentoAposta(Jogador j, int valor);

	/**
	 * Informa que o jogador recusou um pedido de aumento de aposta.
	 * <p>
	 * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
	 * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
	 * mensagem de derrota da dupla será enviada logo em seguida.
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
	 *            jogador que venceu a rodada (e que irá "tornar"), ou null se
	 *            for empate
	 */
	public abstract void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna);

	/**
	 * Informa que a mão foi concluída
	 * 
	 * @param pontosEquipe
	 *            Array com os pontos da equipe 1 e 2 (índices 0 e 1)
	 */
	public abstract void maoFechada(int[] pontosEquipe);

	/**
	 * Informa que o jogo foi concluído
	 * 
	 * @param numEquipeVencedora
	 *            Equipe que ganhou o jogo (1 ou 2)
	 */
	public abstract void jogoFechado(int numEquipeVencedora);

	/**
	 * Informa que um jogador fez sua escolha de topar ou não uma rodada quando
	 * sua equipe tinha 11 pontos
	 * 
	 * @param j
	 *            Jogador que fez a escolha
	 * @param aceita
	 *            true se o jogador topou, false se recusou
	 */
	public abstract void decidiuMao11(Jogador j, boolean aceita);

	/**
	 * Informa que o jogador é beneficiário de uma "mão de 11", e, portanto,
	 * deve decidir se aceita ou não esta rodada (se aceitar vale 3 pontos, se
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
	 *            Posição do jogador que abortou
	 */
	public abstract void jogoAbortado(int posicao);

}
