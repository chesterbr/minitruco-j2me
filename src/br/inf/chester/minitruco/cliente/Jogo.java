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

import java.io.PrintStream;

/**
 * Jogo em andamento (independente de estar rodando local ou remotamente).
 * <p>
 * As implementa��es desta classe ir�o cuidar de executar o jogo (no caso de
 * <code>JogoLocal</code>) ou manter a comunica��o com um jogo em execu��o
 * remota (<code>JogoRemoto</code>). Em qualquer caso, os objetos Jogador
 * n�o ter�o ci�ncia de onde o jogo est� se passando.
 * 
 * @see JogoLocal
 * @author Chester
 * 
 */
public abstract class Jogo implements Runnable {

	/**
	 * Refer�ncia para determinar a ordem das cartas no truco
	 */
	protected static final String letrasOrdenadas = "4567QJKA23";

	/**
	 * Rodada que estamos jogando (de 1 a 3).
	 * <p>
	 * (as implementa��es devem manter atualizado)
	 */
	int numRodadaAtual;

	private PrintStream logPrintStream = null;

	/**
	 * Determina para onde vai o log desse jogo
	 * 
	 * @param logPrintStream
	 *            Stream de sa�da do log
	 */
	public void setLogPrintStream(PrintStream logPrintStream) {
		this.logPrintStream = logPrintStream;
	}

	/**
	 * Guarda uma mensagem no log (se houver algum ativado).
	 * <p>
	 * No caso do cliente, ela vai para o console (para fins de debug)
	 * <p>
	 * O servidor vai ter um arquivo de log espec�fico para cada sala.
	 * 
	 * @param mensagem
	 */
	public void log(String mensagem) {
		if (logPrintStream != null) {
			logPrintStream.println(mensagem);
		}
	}

	/**
	 * Recupera um valor relativo para a carta, considerando as manilhas em jogo
	 * <p>
	 * Este m�todo est� na superclasse porque, no in�cio da rodada, toda a
	 * informa��o necess�ria consiste na manilha e em sua regra, e essas j�
	 * foram transmitidas, evitando assim, d�zias de comandos.
	 * 
	 * @param c
	 *            Carta cujo valor desejamos
	 */
	public static int getValorTruco(Carta c, char letraManilha) {

		if (c.isFechada()) {
			// Cartas fechadas sempre t�m valor 0
			return 0;
		}

		if (letraManilha == SituacaoJogo.MANILHA_INDETERMINADA) {
			if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_OUROS) {
				return 11;
			} else if (c.getLetra() == 'A'
					&& c.getNaipe() == Carta.NAIPE_ESPADAS) {
				return 12;
			} else if (c.getLetra() == '7' && c.getNaipe() == Carta.NAIPE_COPAS) {
				return 13;
			} else if (c.getLetra() == '4' && c.getNaipe() == Carta.NAIPE_PAUS) {
				return 14;
			}
		}

		if (c.getLetra() == letraManilha) {
			// Valor de 11 a 14, conforme o naipe
			switch (c.getNaipe()) {
			case Carta.NAIPE_OUROS:
				return 11;
			case Carta.NAIPE_ESPADAS:
				return 12;
			case Carta.NAIPE_COPAS:
				return 13;
			case Carta.NAIPE_PAUS:
				return 14;
			default:
				return 0;
			}
		} else {
			// Valor de 1 a 10 conforme a letra
			return letrasOrdenadas.indexOf(c.getLetra());
		}
	}

	/**
	 * Jogadores adicionados a este jogo
	 */
	private Jogador[] jogadores = new Jogador[4];

	/**
	 * N�mero de jogadores adicionados at� agora
	 */
	protected int numJogadores = 0;

	/**
	 * Guarda quais cartas foram jogadas em cada rodada.
	 * <p>
	 * (as implementa��es devem alimentar este array)
	 */
	protected Carta[][] cartasJogadasPorRodada;

	/**
	 * Inicia o jogo.
	 * <p>
	 * O jogo deve ser inicializado numa thread separada da principal, desta
	 * forma � mais conveniente que ele seja o Runnable desta thread, da� o nome
	 * do m�todo.
	 */
	public abstract void run();

	/**
	 * Informa que o jogador vai descartar aquela carta.
	 * <p>
	 * Tem que ser a vez dele e n�o pode haver ningu�m trucando.
	 * <p>
	 * A rotina n�o verifica se o jogador realmente possu�a aquela carta -
	 * assume-se que as inst�ncias de Jogador s�o honestas e se protegem de
	 * clientes remotos desonestos
	 * 
	 * @param j
	 * @param c
	 */
	public abstract void jogaCarta(Jogador j, Carta c);

	/**
	 * Informa ao jogo o resultado de aceite daquela m�o de 11
	 * 
	 * @param j
	 *            Jogador que est� respondendo
	 * @param aceita
	 *            true se o jogador topa jogar, false se deixar para o parceiro
	 *            decidir
	 */
	public abstract void decideMao11(Jogador j, boolean aceita);

	/**
	 * Informa que o jogador solicitou um aumento de aposta ("truco", "seis",
	 * etc.).
	 * <p>
	 * Os jogadores s�o notificados, e a aposta ser� efetivamente aumentada se
	 * um dos advers�rios responder positivamente.
	 * <p>
	 * Observe-se que a vez do jogador fica "suspensa", j� que lan�amentos de
	 * cartas s� s�o aceitos se n�o houver ningu�m trucando. Como o jogador
	 * atualmente s� pode trucar na sua vez, isso n�o � problema.
	 * 
	 * @param j
	 *            Jogador que est� solicitando o aumento
	 */
	public abstract void aumentaAposta(Jogador j);

	/**
	 * Informa que o jogador respondeu a um pedido de aumento de aposta
	 * 
	 * @param j
	 *            Jogador que respondeu ao pedido
	 * @param aceitou
	 *            <code>true</code> se ele mandou descer, <code>false</code>
	 *            se correu
	 */
	public abstract void respondeAumento(Jogador j, boolean aceitou);

	/**
	 * Retorna as cartas jogadas por cada jogador naquela rodada
	 * 
	 * @param rodada
	 *            n�mero de 1 a 3
	 * @return cartas jogadas naquela rodada (�ndice = posi��o do Jogador-1)
	 */
	public Carta[] getCartasDaRodada(int rodada) {
		return cartasJogadasPorRodada[rodada - 1];
	}

	public abstract Carta getCartaDaMesa();

	/**
	 * Atualiza um objeto que cont�m a situa��o do jogo (exceto pelas cartas do
	 * jogador)
	 * 
	 * @param s
	 *            objeto a atualizar
	 * @param j
	 *            Jogador que receber� a situa��o
	 */
	public abstract void atualizaSituacao(SituacaoJogo s, Jogador j);

	/**
	 * @return True para jogo sem os 4,5,6 e 7.
	 */
	public abstract boolean isBaralhoLimpo();

	/**
	 * @return True para manilhas fixas (sem "vira")
	 */
	public abstract boolean isManilhaVelha();

	protected int getValorTruco(Carta c) {
		return getValorTruco(c, this.getManilha());
	}

	/**
	 * Adiiciona um jogador na pr�xima posi��o dispon�vel
	 * 
	 * @param j
	 *            Jogador a adicionar
	 * @return true se adicionou o jogador, false se n�o conseguiu
	 */
	public synchronized boolean adiciona(Jogador j) {

		// A mesa tem que ter vaga
		if (numJogadores == 4) {
			return false;
		}

		// Coloca o jogador na pr�xima vaga dispon�vel
		jogadores[numJogadores] = j;
		numJogadores++;
		j.setPosicao(numJogadores);

		// Avisa a todos que o jogador entrou
		for (int i = 1; i <= numJogadores; i++) {
			getJogador(i).jogadorAceito(j, this);
		}

		return true;

	}

	/**
	 * Recupera um jogador inscrito
	 * 
	 * @param posicao
	 *            valor de 1 a 4
	 * @return Objeto correspondente �quela posi��o
	 */
	protected Jogador getJogador(int posicao) {
		return jogadores[posicao - 1];
	}

	private char manilha;

	/**
	 * Pontos de cada equipe na partida.
	 * <p>
	 * As implementa��es devem atualizar (para se saber quando � m�o de 11)
	 */
	protected int[] pontosEquipe = new int[2];

	/**
	 * Indica que o jogo foi finalizado (para evitar que os jogadoresCPU fiquem
	 * "rodando em falso" caso o jogo seja abortado
	 */
	protected boolean jogoFinalizado = false;

	/**
	 * @return Letra correspondente � manilha, ou constante em caso de manilha
	 *         fixa
	 * @see SituacaoJogo#MANILHA_INDETERMINADA
	 */
	public char getManilha() {
		return manilha;
	}

	/**
	 * Determina a letra da manilha, baseado na carta virada (o "vira").
	 * <p>
	 * Deve ser chamado a cada inicializa��o de m�o.
	 * 
	 * @param c
	 *            Carta virada. Ignorado se for jogo com manilha velha
	 */
	public void setManilha(Carta c) {

		if (isManilhaVelha()) {
			manilha = SituacaoJogo.MANILHA_INDETERMINADA;
			return;
		}

		int posManilha = letrasOrdenadas.indexOf(c.getLetra()) + 1;
		if (posManilha == letrasOrdenadas.length()) {
			posManilha = 0;
		}
		manilha = letrasOrdenadas.charAt(posManilha);

		// Detalhe: no baralho limpo, a manilha do vira 3 � o valete (e n�o o 4)
		if (isBaralhoLimpo() && c.getLetra() == '3') {
			manilha = 'Q';
		}

	}

	/**
	 * Informa se alguma das equipes tem 11 pontos (para fins de permitir
	 * trucar)
	 * <p>
	 * Isso n�o tem a ver com a "m�o de 11" - aquela em que uma das equipes
	 * apenas tem 11. Toda m�o de 11 retorna true aqui, mas o 11x11 tamb�m.
	 */
	public boolean isAlguemTem11Pontos() {
		return pontosEquipe[0] == 11 || pontosEquipe[1] == 11;
	}

	/**
	 * Indica que o jogo foi finalizado por iniciativa deste jogador.
	 * <p>
	 * Implementa��es podem sobrescrever (ex.: para notificar o servidor) mas
	 * devem chamar o super()
	 */
	public void abortaJogo(Jogador j) {
		jogoFinalizado = true;
		for (int i = 1; i <= 4; i++) {
			getJogador(i).jogoAbortado(j);
		}
	}

}