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

/**
 * Jogo online no qual o usuário celular está conectado e jogando.
 * <p>
 * A classe atua como um proxy da classe JogoLocal que está efetivamente rodando
 * no servidor. Ela transforma as chamadas de métodos em comandos remotos.
 * <p>
 * Das notificações recebidas, parte é usada para popular coleções locais (ex.:
 * as cartas da mesa, que podem ser solicitadas pelo jogador), e parte é
 * convertida em chamadas aos objetos Jogador.
 * 
 * @author Chester
 * 
 */
public class JogoTCP extends Jogo {

	/**
	 * Valor dummy para os pontos do modo conflito de estratégias
	 * (que não é suportado no jogo remoto anyway)
	 */
	private static final int[] VAQUINHAS_DUMMY = null;

	private ServidorTCP servidor;

	/**
	 * Esse baralho é apenas para sortear cartas quando alguém joga uma fechada
	 * (as cartas, mesmo fechadas, têm que ser únicas)
	 */
	private Baralho baralho;

	/**
	 * Faz o jogador abortar o jogo atual
	 */
	public void abortaJogo(int posicao) {
		super.abortaJogo(posicao);
		// Sair da sala aborta o jogo automaticamente. Lei do menor esforço,
		// esteja sempre ao meu lado.
		servidor.enviaComando("S");
	}

	public JogoTCP(ServidorTCP servidor) {
		super();
		this.servidor = servidor;
	}

	public void run() {
		// Notifica o jogador humano que a partida começou
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
	 * Não implementado em jogo remoto (apenas o JogadorCPU usa isso, e ele não
	 * participa desses jogos).
	 * <p>
	 * Se no futuro quisermos ter bots locais em jogos online, uma idéia seria
	 * mover a implementação em <code>JogoLocal</code> para <code>Jogo</code>,
	 * fazendo os ajustes necessários.
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
	 * Processa uma notificação "in-game", gerando o evento apropriado no
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
			// Início da mão
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
			// Informa o jogador humano que é a vez de alguém
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
			// Alguém aceitou mão de 11, informa
			getJogadorHumano().decidiuMao11(
					getJogador(Integer.parseInt(tokens[0])),
					tokens[1].equals("T"));
			break;
		case 'F':
			// Mão de 11. Recupera as cartas do parceiro e informa o jogador
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
			// Fim de mão, recupera os placares
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
	 * Retorna o jogador humano que está no jogo
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
