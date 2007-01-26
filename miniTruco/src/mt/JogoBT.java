package mt;

import java.io.IOException;

/**
 * Representa, no cliente, o <code>Jogo</code> que est� executando no
 * servidor.
 * <p>
 * De maneira an�loga � <code>JogadorBT</code>, ela converte as notifica��es
 * do jogador local em mensagens de texto (enviando-as ao servidor) e recebe
 * mensagens de texto do servidor, transformando-as em notifica��es para o
 * jogador local.
 * <p>
 * A conex�o � gerenciada por <code>ClienteBT</code>, j� que uma conex�o pode
 * ser usada em jogos sucessivos.
 * 
 * @author chester
 * 
 */
public class JogoBT extends Jogo {

	private JogadorHumano jogadorHumano;

	private ClienteBT clienteBT;

	/**
	 * Cria um novo proxy de jogo remoto associado a um cliente
	 * 
	 * @param clienteBT
	 *            Cliente que se conectou no jogo remoto
	 */
	public JogoBT(ClienteBT clienteBT) {
		this.clienteBT = clienteBT;
	}

	/**
	 * Esse baralho � apenas para sortear cartas quando algu�m joga uma fechada
	 * (as cartas, mesmo fechadas, t�m que ser �nicas)
	 */
	private Baralho baralho;

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

	/**
	 * Processa uma notifica��o "in-game", gerando o evento apropriado no
	 * jogador humano
	 * 
	 * @param tipoNotificacao
	 *            caractere identificador
	 * @param parametros
	 *            dependem do caractere
	 */
	public void processaNotificacao(char tipoNotificacao, String parametros) {

		// Uso geral
		String[] tokens = ClienteBT.split(parametros, ' ');
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
			getJogadorHumano().maoFechada(pontosEquipe);
			break;
		case 'G':
			// Fim de jogo
			getJogadorHumano().jogoFechado(Integer.parseInt(parametros));
			break;
		case 'A':
			// Jogo abortado por algu�m
			getJogadorHumano().jogoAbortado(Integer.parseInt(parametros));
			break;
		}
	}

	/**
	 * N�o implementado em jogo bluetooth (apenas o JogadorCPU usa isso, e ele
	 * n�o participa desses jogos).
	 */
	public void atualizaSituacao(SituacaoJogo s, Jogador j) {
		// n�o faz nada
	}

	public boolean isBaralhoLimpo() {
		return clienteBT.regras.charAt(0) == 'T';
	}

	public boolean isManilhaVelha() {
		return clienteBT.regras.charAt(1) == 'T';
	}

	public void run() {
		// Notifica o jogador humano que a partida come�ou
		getJogadorHumano().inicioPartida();
	}

	/**
	 * Manda um comando para o celular do servidor.
	 * <p>
	 * Este comando � originado de alguma a��o do JogadorCPU local (jogar uma
	 * carta, pedir truco, etc.).
	 * 
	 * @param linha
	 */
	public synchronized void enviaLinha(String linha) {
		try {
			clienteBT.out.write(linha.getBytes());
			clienteBT.out.write(TelaBT.ENTER);
			System.out.println("JogoBT encaminhou" + linha);
		} catch (IOException e) {
			// TODO TRATAR!!!!!
			e.printStackTrace();
		}
	}

	public void jogaCarta(Jogador j, Carta c) {
		enviaLinha("J " + c + (c.isFechada() ? " T" : ""));
	}

	public void decideMao11(Jogador j, boolean aceita) {
		enviaLinha("H " + (aceita ? "T" : "F"));
	}

	public void aumentaAposta(Jogador j) {
		if (j.equals(getJogadorHumano()))
			enviaLinha("T");
	}

	public void respondeAumento(Jogador j, boolean aceitou) {
		if (j.equals(getJogadorHumano())) {
			if (aceitou)
				enviaLinha("D");
			else
				enviaLinha("C");
		}
	}

}
