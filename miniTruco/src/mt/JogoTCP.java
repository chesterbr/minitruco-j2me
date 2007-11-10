package mt;

/*
 * Copyright � 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 3 da 
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
 * Jogo online no qual o usu�rio celular est� conectado e jogando.
 * <p>
 * A classe atua como um proxy da classe JogoLocal que est� efetivamente rodando
 * no servidor. Ela transforma as chamadas de m�todos em comandos remotos.
 * <p>
 * Das notifica��es recebidas, parte � usada para popular cole��es locais (ex.:
 * as cartas da mesa, que podem ser solicitadas pelo jogador), e parte �
 * convertida em chamadas aos objetos Jogador.
 * 
 * @author Chester
 * 
 */
public class JogoTCP extends Jogo {

	/**
	 * Valor dummy para os pontos do modo conflito de estrat�gias
	 * (que n�o � suportado no jogo remoto anyway)
	 */
	private static final int[] VAQUINHAS_DUMMY = null;

	private ServidorTCP servidor;

	/**
	 * Esse baralho � apenas para sortear cartas quando algu�m joga uma fechada
	 * (as cartas, mesmo fechadas, t�m que ser �nicas)
	 */
	private Baralho baralho;

	/**
	 * Faz o jogador abortar o jogo atual
	 */
	public void abortaJogo(int posicao) {
		super.abortaJogo(posicao);
		// Sair da sala aborta o jogo automaticamente. Lei do menor esfor�o,
		// esteja sempre ao meu lado.
		servidor.enviaComando("S");
	}

	public JogoTCP(ServidorTCP servidor) {
		super();
		this.servidor = servidor;
	}

	public void run() {
		// Notifica o jogador humano que a partida come�ou
		getJogadorHumano().inicioPartida();
	}

	public void jogaCarta(Jogador j, Carta c) {
		servidor.enviaComando("J " + c + (c.isFechada() ? " T" : ""));
	}

	public void decideMao11(Jogador j, boolean aceita) {
		servidor.enviaComando("H " + (aceita ? "T" : "F"));
	}

	public void aumentaAposta(Jogador j) {
		if (j.equals(getJogadorHumano()))
			servidor.enviaComando("T");
	}

	public void respondeAumento(Jogador j, boolean aceitou) {
		if (j.equals(getJogadorHumano())) {
			if (aceitou)
				servidor.enviaComando("D");
			else
				servidor.enviaComando("C");
		}
	}

	private Carta cartaDaMesa;

	public Carta getCartaDaMesa() {
		return cartaDaMesa;
	}

	/**
	 * N�o implementado em jogo remoto (apenas o JogadorCPU usa isso, e ele n�o
	 * participa desses jogos).
	 * <p>
	 * Se no futuro quisermos ter bots locais em jogos online, uma id�ia seria
	 * mover a implementa��o em <code>JogoLocal</code> para <code>Jogo</code>,
	 * fazendo os ajustes necess�rios.
	 */
	public void atualizaSituacao(SituacaoJogo s, Jogador j) {

	}

	public boolean isBaralhoLimpo() {
		return servidor.getSala().regras.charAt(0) == 'T';
	}

	public boolean isManilhaVelha() {
		return servidor.getSala().regras.charAt(1) == 'T';
	}

	/**
	 * Processa uma notifica��o "in-game", gerando o evento apropriado no
	 * jogador humano
	 * 
	 * @param tipoNotificacao
	 *            caractere identificador
	 * @param parametros
	 *            dependem do caractere
	 * @see protocolo.txt
	 */
	public void processaNotificacao(char tipoNotificacao, String parametros) {

		// Uso geral
		String[] tokens = ServidorTCP.split(parametros, ' ');
		Jogador j;

		switch (tipoNotificacao) {
		case 'M':
			// In�cio da m�o
			numRodadaAtual = 1;
			cartasJogadasPorRodada = new Carta[3][4];
			baralho = new Baralho(isBaralhoLimpo());
			// Gera as cartas e notifica
			Carta[] cartas = new Carta[3];
			for (int i = 0; i <= 2; i++) {
				cartas[i] = new Carta(tokens[i]);
				baralho.tiraDoBaralho(cartas[i]);
			}
			if (!isManilhaVelha()) {
				cartaDaMesa = new Carta(tokens[3]);
				baralho.tiraDoBaralho(cartaDaMesa);
			}
			setManilha(cartaDaMesa);
			getJogadorHumano().setCartas(cartas);
			getJogadorHumano().inicioMao();
			break;
		case 'J':
			// Recupera o jogador que jogou a carta
			int posicao = Integer.parseInt(tokens[0]);
			j = getJogador(posicao);
			// Recupera a carta jogada (isso depende do jogaodr ser local ou
			// remoto, e de a carta ser aberta ou fechada)
			Carta c;
			if (getJogadorHumano().getPosicao() == posicao) {
				// Recupera a carta jogada pelo humano
				c = null;
				Carta[] cartasHumano = getJogadorHumano().getCartas();
				for (int i = 0; i < cartasHumano.length; i++) {
					if (cartasHumano[i].toString().equals(tokens[1])) {
						c = cartasHumano[i];
						break;
					}
				}
				// Se solicitou carta fechada, muda o status
				if (tokens.length > 2 && tokens[2].equals("T")) {
					c.setFechada(true);
				}
			} else {
				if (tokens.length > 1) {
					// Cria a carta jogada pela CPU
					c = new Carta(tokens[1]);
					baralho.tiraDoBaralho(c);
				} else {
					// Carta fechada, cria uma qualquer e seta o status
					c = baralho.sorteiaCarta();
					c.setFechada(true);
				}
			}
			// Guarda a carta no array de cartas jogadas, para consulta
			cartasJogadasPorRodada[numRodadaAtual - 1][posicao - 1] = c;
			// Avisa o jogador humano que a jogada foi feita
			getJogadorHumano().cartaJogada(j, c);
			break;
		case 'V':
			// Informa o jogador humano que � a vez de algu�m
			getJogadorHumano().vez(getJogador(Integer.parseInt(tokens[0])),
					tokens[1].equals("T"));
			break;
		case 'T':
			getJogadorHumano().pediuAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])),
					Integer.parseInt(tokens[1]));
			break;
		case 'D':
			getJogadorHumano().aceitouAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])),
					Integer.parseInt(tokens[1]));
			break;
		case 'C':
			getJogadorHumano().recusouAumentoAposta(
					getJogador(Integer.parseInt(tokens[0])));
			break;
		case 'H':
			// Algu�m aceitou m�o de 11, informa
			getJogadorHumano().decidiuMao11(
					getJogador(Integer.parseInt(tokens[0])),
					tokens[1].equals("T"));
			break;
		case 'F':
			// M�o de 11. Recupera as cartas do parceiro e informa o jogador
			Carta[] cartasMao11 = new Carta[3];
			for (int i = 0; i <= 2; i++) {
				cartasMao11[i] = new Carta(tokens[i]);
			}
			getJogadorHumano().informaMao11(cartasMao11);
			break;
		case 'R':
			// Fim de rodada, recupera o resultado e o jogador que torna
			int resultado = Integer.parseInt(tokens[0]);
			j = getJogador(Integer.parseInt(tokens[1]));
			getJogadorHumano().rodadaFechada(numRodadaAtual, resultado, j);
			numRodadaAtual++;
			break;
		case 'O':
			// Fim de m�o, recupera os placares
			pontosEquipe[0] = Integer.parseInt(tokens[0]);
			pontosEquipe[1] = Integer.parseInt(tokens[1]);
			getJogadorHumano().maoFechada(pontosEquipe, VAQUINHAS_DUMMY);
			break;
		case 'G':
			// Fim de jogo
			getJogadorHumano().jogoFechado(Integer.parseInt(parametros), VAQUINHAS_DUMMY);
		}
	}

	private JogadorHumano jogadorHumano;

	/**
	 * Retorna o jogador humano que est� no jogo
	 * 
	 * @return objeto que representa o humano
	 */
	public JogadorHumano getJogadorHumano() {
		if (jogadorHumano == null)
			for (int i = 1; i <= 4; i++)
				if (getJogador(i) instanceof JogadorHumano)
					jogadorHumano = (JogadorHumano) getJogador(i);
		return jogadorHumano;

	}

}
