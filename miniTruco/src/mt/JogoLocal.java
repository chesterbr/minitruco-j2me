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

/**
 * Jogo rodando no celular.
 * <p>
 * Um jogador só passa a fazer parte do jogo se for adicionado a ele pelo método
 * <code>adiciona()</code>.
 * <p>
 * A classe notifica aos jogadores participantes os eventos relevantes (ex.:
 * início da partida, vez passando para um jogador, carta jogada, pedido de
 * truco), e os jogadores podem usar os métodos de entrada (ex.:
 * <code>jogaCarta()</code>, <code>aumentaAposta</code>, etc.) para interagir
 * com o jogo.
 * <p>
 * 
 * @author Chester
 * 
 */
public class JogoLocal extends Jogo {

	/**
	 * Resultados de cada rodada (1 para vitória da equipe 1/3, 2 para vitória
	 * da equipe 2/4 e 3 para empate)
	 */
	private int resultadoRodada[] = new int[3];

	/**
	 * Valor atual da mão (1, 3, 6, 9 ou 12)
	 */
	private int valorMao;

	/**
	 * Jogador que está pedindo aumento de aposta (pedindo truco, 6, 9 ou 12).
	 * Se for null, ninguém está pedindo
	 */
	private Jogador jogadorPedindoAumento;

	/**
	 * Status das respsotas para um pedido de aumento de aposta para cada
	 * jogador.
	 * <p>
	 * false signfica que não respondeu ainda, true que respondeu recusando
	 */
	private boolean[] recusouAumento = new boolean[4];

	/**
	 * Posição (1 a 4) do jogador da vez
	 */
	private int posJogadorDaVez;

	/**
	 * Jogador que abriu a rodada
	 */
	private Jogador jogadorAbriuRodada;

	/**
	 * Jogador que abriu a mão
	 */
	private Jogador jogadorAbriuMao;

	/**
	 * Indica, para cada jogador, se estamos aguardando a resposta para uma mão
	 * de 11
	 */
	private boolean[] aguardandoRespostaMaoDe11 = new boolean[4];

	private boolean manilhaVelha, baralhoLimpo;

	private int nPartidasModoCE;

	/**
	 * Cria um novo jogo.
	 * <p>
	 * O jogo é criado, mas apenas inicia quando forem adicionados jogadores
	 * 
	 * @param manilhaVelha
	 *            true para jogo com manilhas fixas, false para jogar ocm "vira"
	 * @param baralhoLimpo
	 *            true para baralho sem os 4, 5, 6, 7, false para baralho
	 *            completo (sujo)
	 */
	public JogoLocal(boolean baralhoLimpo, boolean manilhaVelha) {
		this.manilhaVelha = manilhaVelha;
		this.baralhoLimpo = baralhoLimpo;
		this.modoCE = false;
	}

	/**
	 * Cria um novo jogo no modo confronto de estratégias
	 * <p>
	 * O jogo é criado, mas apenas inicia quando forem adicionados jogadores
	 * 
	 * @param manilhaVelha
	 *            true para jogo com manilhas fixas, false para jogar ocm "vira"
	 * @param baralhoLimpo
	 *            true para baralho sem os 4, 5, 6, 7, false para baralho
	 *            completo (sujo)
	 */
	public JogoLocal(boolean baralhoLimpo, boolean manilhaVelha,
			int nPartidasModoCE) {
		this.manilhaVelha = manilhaVelha;
		this.baralhoLimpo = baralhoLimpo;
		this.vaquinhasNoPasto[0] = 0;
		this.vaquinhasNoPasto[1] = 0;
		this.modoCE = true;
		this.nPartidasModoCE = nPartidasModoCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#run()
	 */
	public void run() {

		// Avisa os jogadores que o jogo vai começar
		for (int i = 1; i <= numJogadores; i++) {
			getJogador(i).inicioPartida();
		}

		// Descomentar para debug da mão de 11, conforme o caso
		// pontosEquipe[0] = 10;
		// pontosEquipe[1] = 10;

		// Inicia a primeira rodada, usando o jogador na posição 1
		iniciaMao(getJogador(1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#jogaCarta(mt.Jogador, mt.Carta)
	 */
	public synchronized void jogaCarta(Jogador j, Carta c) {

		// Se o jogo acabou, a mesa não estiver completa, já houver alguém
		// trucando, estivermos aguardando ok da mão de 11 ou não for a vez do
		// cara, recusa
		if (jogoFinalizado || numJogadores < 4 || jogadorPedindoAumento != null
				|| (isAguardandoRespostaMao11())
				|| !j.equals(getJogadorDaVez())) {
			return;
		}

		// Verifica se a carta já não foi jogada anteriormente (normalmente não
		// deve acontecer - mesmo caso do check anterior)
		for (int i = 0; i <= 2; i++) {
			for (int k = 0; k <= 3; k++) {
				if (c.equals(cartasJogadasPorRodada[i][k])) {
					return;
				}
			}
		}

		// Garante que a regra para carta fechada seja respeitada
		if (!isPodeFechada()) {
			c.setFechada(false);
		}

		Jogo.log("J" + j.getPosicao() + " joga " + c);

		// Dá a carta como jogada, notificando os jogadores
		cartasJogadasPorRodada[numRodadaAtual - 1][j.getPosicao() - 1] = c;
		for (int i = 1; i <= 4; i++) {
			getJogador(i).cartaJogada(j, c);
		}

		// Passa a vez para o próximo jogador
		posJogadorDaVez++;
		if (posJogadorDaVez == 5) {
			posJogadorDaVez = 1;
		}
		if (posJogadorDaVez == jogadorAbriuRodada.getPosicao()) {

			// Completou a volta da rodada - acha o valor da maior carta da mesa
			Carta[] cartas = getCartasDaRodada(numRodadaAtual);
			int valorMaximo = 0;
			for (int i = 0; i <= 3; i++) {
				valorMaximo = Math.max(valorMaximo, getValorTruco(cartas[i]));
			}

			// Determina a equipe vencedora (1/2= equipe 1 ou 2; 3=empate) e o
			// jogador que vai "tornar", i.e., abrir a próxima rodada
			setResultadoRodada(numRodadaAtual, 0);
			Jogador jogadorQueTorna = null;
			for (int i = 0; i <= 3; i++) {
				if (getValorTruco(cartas[i]) == valorMaximo) {
					if (jogadorQueTorna == null) {
						jogadorQueTorna = getJogador(i + 1);
					}
					if (i == 0 || i == 2) {
						setResultadoRodada(numRodadaAtual,
								getResultadoRodada(numRodadaAtual) | 1);
					} else {
						setResultadoRodada(numRodadaAtual,
								getResultadoRodada(numRodadaAtual) | 2);
					}
				}
			}

			Jogo.log("Rodada fechou. Resultado: "
					+ getResultadoRodada(numRodadaAtual));

			// Se houve vencedor, passa a vez para o jogador que fechou a
			// vitória, senão deixa quem abriu a mão anterior abrir a próxima
			if (getResultadoRodada(numRodadaAtual) != 3) {
				posJogadorDaVez = jogadorQueTorna.getPosicao();
			} else {
				jogadorQueTorna = getJogadorDaVez();
			}

			// Notifica os jogadores que a mão foi feita
			for (int i = 1; i <= 4; i++) {
				getJogador(i).rodadaFechada(numRodadaAtual,
						getResultadoRodada(numRodadaAtual), jogadorQueTorna);
			}

			// 'sleep' below is for gTruco (notificação 'R')
			pause(1500);

			// Verifica se já temos vencedor na rodada
			int resultadoRodada = 0;
			if (numRodadaAtual == 2) {
				if (getResultadoRodada(1) == 3 && getResultadoRodada(2) != 3) {
					// Empate na 1a. mão, quem fez a 2a. leva
					resultadoRodada = getResultadoRodada(2);
				} else if (getResultadoRodada(1) != 3
						&& getResultadoRodada(2) == 3) {
					// Empate na 2a. mão, quem fez a 1a. leva
					resultadoRodada = getResultadoRodada(1);
				} else if (getResultadoRodada(1) == getResultadoRodada(2)
						&& getResultadoRodada(1) != 3) {
					// Quem faz as duas primeiras leva
					resultadoRodada = getResultadoRodada(2);
				}
			} else if (numRodadaAtual == 3) {
				if (getResultadoRodada(3) != 3) {
					// Quem faz a 3a. leva
					resultadoRodada = getResultadoRodada(3);
				} else {
					// Se a 3a. empatou, a 1a. decide
					resultadoRodada = getResultadoRodada(1);
				}
			}

			// Se já tivermos vencedor (ou empate final), notifica e abre uma
			// nova mao, senão segue a vida na mão seguinte
			if (resultadoRodada != 0) {
				// Soma os pontos (se não deu emptate)
				if (resultadoRodada != 3) {
					pontosEquipe[resultadoRodada - 1] += valorMao;
				}
				fechaMao();
			} else {
				numRodadaAtual++;
				jogadorAbriuRodada = jogadorQueTorna;
				notificaVez();
			}
		} else {
			notificaVez();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#decideMao11(mt.Jogador, boolean)
	 */
	public synchronized void decideMao11(Jogador j, boolean aceita) {

		// Só entra se estivermos jogando e se estivermos agurardando resposta
		// daquele jogador para a pergunta (isso é importante para evitar duplo
		// início)
		if (jogoFinalizado || !aguardandoRespostaMaoDe11[j.getPosicao() - 1])
			return;

		Jogo.log("J" + j.getPosicao() + (aceita ? "" : " nao")
				+ " quer jogar mao de 11 ");

		// Avisa os outros jogadores da decisão
		for (int i = 1; i <= 4; i++) {
			getJogador(i).decidiuMao11(j, aceita);
		}

		aguardandoRespostaMaoDe11[j.getPosicao() - 1] = false;

		if (aceita) {
			// Se aceitou, desencana da resposta do parceiro e pode tocar o
			// jogo, valendo 3
			aguardandoRespostaMaoDe11[j.getParceiro() - 1] = false;
			valorMao = 3;
			// 'sleep' below is for gTruco (notificação 'H')
			pause(2000);
			notificaVez();
		} else {
			// Se recusou (e o parceiro também), a equipe perde um ponto e
			// recomeça a mao
			if (!aguardandoRespostaMaoDe11[j.getParceiro() - 1]) {
				// 'sleep' below is for gTruco (notificação 'H')
				pause(1500);
				pontosEquipe[j.getEquipeAdversaria() - 1]++;
				fechaMao();
			}
		}

	}

	/**
	 * Verifica se estamos aguardando resposta para mão de 11
	 * 
	 * @return true se falta alguém responder, false caso contrário
	 */
	private boolean isAguardandoRespostaMao11() {
		for (int i = 0; i <= 3; i++) {
			if (aguardandoRespostaMaoDe11[i]) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#aumentaAposta(mt.Jogador)
	 */
	public void aumentaAposta(Jogador j) {

		// Se o jogo estiver fianlizado, a mesa não estiver completa, já houver
		// alguém trucando, estivermos aguardando a mão de 11 ou não for a vez
		// do cara, recusa
		if ((jogoFinalizado) || (numJogadores < 4)
				|| (jogadorPedindoAumento != null)
				|| isAguardandoRespostaMao11() || !j.equals(getJogadorDaVez())) {
			return;
		}

		Jogo.log("Jogador  " + j.getPosicao() + " pede aumento");

		// Atualiza o status e notifica os outros jogadores do pedido
		jogadorPedindoAumento = j;
		for (int i = 0; i <= 3; i++)
			recusouAumento[i] = false;
		int valor = calcValorAumento();

		// O código abaixo notifica primeiro os jogadores que não forem
		// JogadorCPU, e depois estes.
		//
		// Motivo: o JogadorCPU responde imediatamente ao aumento, assim,
		// se eu não fizer isso, um celular-cliente Bluetooth que esteja numa
		// posição maior vai mostrar o balão de resposta antes do da pergunta
		// (pois a notificação de resposta será gerada imediatamente).
		// 
		// Isso, em termos OO, é uma quebra de contrato (até então, instâncias
		// de Jogo tratavam instâncias de Jogador de forma indiscriminada).
		//
		// A solução "correta" seria fazer o JogadorCPU segurar a sua resposta,
		// mas isso implica em mais uma inner class/thread (e a aplicação já
		// está beirando os limites de tamanho), então vou cercar com as tags
		// <gambiarra> e </gambiarra>, apenas para me martirzar.

		// <gambiarra>
		for (int i = 1; i <= 4; i++) {
			Jogador ji = getJogador(i);
			if (!(ji instanceof JogadorCPU)) {
				ji.pediuAumentoAposta(j, valor);
			}
		}
		// 'sleep' below is for gTruco (notificação 'T')
		pause(750);
		for (int i = 1; i <= 4; i++) {
			Jogador ji = getJogador(i);
			if (ji instanceof JogadorCPU) {
				ji.pediuAumentoAposta(j, valor);
			}
		}
		// </gambiarra>
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#respondeAumento(mt.Jogador, boolean)
	 */
	public synchronized void respondeAumento(Jogador j, boolean aceitou) {
		// Apenas os adversários de quem trucou respondem
		if (jogadorPedindoAumento == null
				|| jogadorPedindoAumento.getEquipeAdversaria() != j.getEquipe()) {
			return;
		}

		Jogo.log("Jogador  " + j.getPosicao()
				+ (aceitou ? "aceitou" : "recusou"));

		if (aceitou) {
			// Se o jogador aceitou, seta o novo valor, notifica a galera e tira
			// o jogo da situtação de truco
			valorMao = calcValorAumento();
			jogadorPedindoAumento = null;
			for (int i = 1; i <= 4; i++) {
				getJogador(i).aceitouAumentoAposta(j, valorMao);
			}
			// 'sleep' below is for gTruco (notificação 'D')
			pause(1500);
		} else {
			// Primeiro notifica todo mundo
			for (int i = 1; i <= 4; i++) {
				getJogador(i).recusouAumentoAposta(j);
			}
			int posParceiro = (j.getPosicao() + 1) % 4 + 1;
			if (recusouAumento[posParceiro - 1]) {
				// Se o parceiro também recusou, derrota da dupla
				// 'sleep' below is for gTruco (notificação 'C')
				pause(1500);
				pontosEquipe[jogadorPedindoAumento.getEquipe() - 1] += valorMao;
				fechaMao();
			} else {
				// Sinaliza a recusa, deixando a decisão na mão do parceiro
				recusouAumento[j.getPosicao() - 1] = true;
			}
		}
	}

	/**
	 * Conclui a mão atual, e, se o jogo não acabou, inicia uma nova.
	 * 
	 * @param jogadorQueTorna
	 *            Jogador que irá abrir a próxima mão, se houver
	 */
	private void fechaMao() {

		Jogo.log("Mao fechou. Placar: " + pontosEquipe[0] + " a "
				+ pontosEquipe[1]);

		boolean acabou = false;

		// Notifica os jogadores que a rodada acabou, e, se for o caso, que o
		// jogo acabou também
		if (modoCE) {
			if (pontosEquipe[0] > 11) {
				vaquinhasNoPasto[0]++;
				pontosEquipe[0] = 0;
				pontosEquipe[1] = 0;
			}
			if (pontosEquipe[1] > 11) {
				vaquinhasNoPasto[1]++;
				pontosEquipe[0] = 0;
				pontosEquipe[1] = 0;
			}
			for (int i = 1; i <= 4; i++) {
				getJogador(i).maoFechada(pontosEquipe, vaquinhasNoPasto);
				// Checa se ainda temos que jogar mais partidas ou já está tudo
				// decidido
				if (((nPartidasModoCE - vaquinhasNoPasto[0]) < vaquinhasNoPasto[0])
						|| ((nPartidasModoCE - vaquinhasNoPasto[1]) < vaquinhasNoPasto[1])) {
					// acabou...
					acabou = true;
					if (this.vaquinhasNoPasto[0] > vaquinhasNoPasto[1])
						getJogador(i).jogoFechado(1, vaquinhasNoPasto);
					else
						getJogador(i).jogoFechado(2, vaquinhasNoPasto);
				}
			}
		} else {
			for (int i = 1; i <= 4; i++) {
				getJogador(i).maoFechada(pontosEquipe, vaquinhasNoPasto);
				if (pontosEquipe[0] > 11) {
					getJogador(i).jogoFechado(1, vaquinhasNoPasto);
				} else if (pontosEquipe[1] > 11) {
					getJogador(i).jogoFechado(2, vaquinhasNoPasto);
				}
			}
		}
		// Se ainda estivermos em jogo, incia a nova mao
		if (pontosEquipe[0] <= 11 && pontosEquipe[1] <= 11 && acabou == false) {
			int posAbre = jogadorAbriuMao.getPosicao() + 1;
			if (posAbre == 5)
				posAbre = 1;
			iniciaMao(getJogador(posAbre));
		}

		return;
	}

	/**
	 * Inicia uma mão (i.e., uma distribuição de cartas)
	 * 
	 * @param jogadorQueAbre
	 *            Jogador que abre a rodada
	 */
	private void iniciaMao(Jogador jogadorQueAbre) {

		// Pega um novo baralho e reinicia a mesa
		Baralho baralho = new Baralho(baralhoLimpo);
		cartasJogadasPorRodada = new Carta[3][4];

		// Distribui as cartas de cada jogador
		for (int j = 1; j <= 4; j++) {
			Jogador jogador = getJogador(j);
			Carta[] cartas = new Carta[3];
			for (int i = 0; i <= 2; i++) {
				cartas[i] = baralho.sorteiaCarta();
			}
			jogador.setCartas(cartas);
		}

		// Vira a carta da mesa, determinando a manilha
		cartaDaMesa = baralho.sorteiaCarta();
		setManilha(cartaDaMesa);

		// Inicializa a mão
		valorMao = 1;
		jogadorPedindoAumento = null;
		numRodadaAtual = 1;
		jogadorAbriuMao = jogadorAbriuRodada = jogadorQueAbre;

		Jogo.log("Abrindo mao com j" + jogadorQueAbre.getPosicao()
				+ ",manilha=" + getManilha());

		// Abre a primeira rodada, informando a carta da mesa e quem vai abrir
		posJogadorDaVez = jogadorQueAbre.getPosicao();
		for (int i = 1; i <= numJogadores; i++) {
			Jogo.log("Enviando inicioMao para " + i);
			getJogador(i).inicioMao();
		}

		// 'sleep' below is for gTruco (notificação 'M')
		pause(1250);

		if (pontosEquipe[0] == 11 ^ pontosEquipe[1] == 11) {
			// Se apenas uma das equipes tiver 11 pontos, verifica se eles
			// querem realmente jogar (eles podem desistir);
			if (pontosEquipe[0] == 11) {
				setEquipeAguardandoMao11(1);
				getJogador(1).informaMao11(getJogador(3).getCartas());
				getJogador(3).informaMao11(getJogador(1).getCartas());
			} else {
				setEquipeAguardandoMao11(2);
				getJogador(2).informaMao11(getJogador(4).getCartas());
				getJogador(4).informaMao11(getJogador(2).getCartas());
			}
			// 'sleep' below is for gTruco (notificação 'F')
			pause(1250);
		} else {
			// Se for uma mão normal, passa a vez para o jogador que abre
			setEquipeAguardandoMao11(0);
			notificaVez();
		}

	}

	/**
	 * Determina qual a equipe que está aguardando mão de 11
	 * 
	 * @param i
	 *            1 ou 2 para a respectiva equipe, 0 para ninguém aguardando mão
	 *            de 11 (jogo normal)
	 */
	private void setEquipeAguardandoMao11(int i) {
		aguardandoRespostaMaoDe11[0] = aguardandoRespostaMaoDe11[2] = (i == 1);
		aguardandoRespostaMaoDe11[1] = aguardandoRespostaMaoDe11[3] = (i == 2);
	}

	/**
	 * Calcula para quanto vai a rodada se for pedido aumento de aposta (truco,
	 * seis, etc.)
	 * 
	 * @return valor numérico da rodada se for aceito o pedido
	 */
	private int calcValorAumento() {
		switch (valorMao) {
		case 1:
			return 3;
		case 3:
			return 6;
		case 6:
			return 9;
		case 9:
			return 12;
		}
		return 0;
	}

	private int getResultadoRodada(int mao) {
		return resultadoRodada[mao - 1];
	}

	private void setResultadoRodada(int mao, int valor) {
		resultadoRodada[mao - 1] = valor;
	}

	/**
	 * Informa aos jogadores participantes que é a vez de um deles.
	 * <p>
	 * Faz isso em threads distintas, para que eles joguem sem se preocupar
	 */
	private void notificaVez() {

		class ThreadNotifica extends Thread {
			public int numNotificado;

			public Jogador jogadorDaVez;

			public boolean podeFechada;

			public void run() {
				Jogo.log("notifica " + numNotificado + " da vez de "
						+ jogadorDaVez.getPosicao());
				getJogador(numNotificado).vez(jogadorDaVez, podeFechada);
			}
		}

		// Esses dados têm que ser coletados *antes* de chamar as Threads.
		// Motivo: se uma delas resolver jogar, a informação para as outras pode
		// ficar destaualizada. Isso causou um/ bug *muito* hardcore de
		// encontrar nos Nokia Series 40, que provavelmente possuem uma
		// implementação minimalista de Threads
		Jogador j = getJogadorDaVez();
		boolean pf = isPodeFechada();

		for (int i = 1; i <= 4; i++) {
			// Isso é uma otimização: os JogadorCPU ignoram notificação de vez
			// que não sejam da sua própria, então podemos pular essas, evitando
			// abrir uma thread à toa.
			if ((getJogador(i) instanceof JogadorCPU) && (i != j.getPosicao())) {
				continue;
			}
			ThreadNotifica tn = new ThreadNotifica();
			tn.numNotificado = i;
			tn.jogadorDaVez = j;
			tn.podeFechada = pf;
			tn.start();
		}

	}

	/**
	 * Informa se o jogador da vez pode jogar carta fechada (se mudar a regra,
	 * basta alterar aqui).
	 * <p>
	 * Regra atual: só vale carta fechada se não for a 1a. rodada e se o
	 * parceiro não tiver jogado fechada também
	 * 
	 * @return
	 */
	private boolean isPodeFechada() {
		Carta cartaParceiro = cartasJogadasPorRodada[numRodadaAtual - 1][getJogadorDaVez()
				.getParceiro() - 1];
		return (numRodadaAtual > 1 && (cartaParceiro == null || !cartaParceiro
				.isFechada()));
	}

	/**
	 * Recupera o jogador cuja vez é a atual
	 * 
	 * @return
	 */
	private Jogador getJogadorDaVez() {
		return getJogador(posJogadorDaVez);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mt.JogoGenerico#atualizaSituacao(mt.SituacaoJogo, mt.Jogador)
	 */
	public void atualizaSituacao(SituacaoJogo s, Jogador j) {
		s.baralhoSujo = !this.baralhoLimpo;
		if (manilhaVelha) {
			s.manilha = SituacaoJogo.MANILHA_INDETERMINADA;
		} else {
			s.manilha = this.getManilha();
		}
		s.numRodadaAtual = this.numRodadaAtual;
		s.posJogador = j.getPosicao();
		s.posJogadorQueAbriuRodada = this.jogadorAbriuRodada.getPosicao();
		if (this.jogadorPedindoAumento != null)
			s.posJogadorPedindoAumento = this.jogadorPedindoAumento
					.getPosicao();
		s.valorMao = this.valorMao;

		for (int i = 0; i <= 1; i++) {
			s.pontosEquipe[i] = this.pontosEquipe[i];
			s.vaquinhasNoPasto[i] = this.vaquinhasNoPasto[i];
		}
		for (int i = 0; i <= 2; i++)
			s.resultadoRodada[i] = this.resultadoRodada[i];

		for (int i = 0; i <= 2; i++)
			for (int k = 0; k <= 3; k++) {
				Carta c = cartasJogadasPorRodada[i][k];
				if (c == null) {
					s.cartasJogadas[i][k] = null;
				} else if (s.cartasJogadas[i][k] == null) {
					s.cartasJogadas[i][k] = new Carta(c.getLetra(), c
							.getNaipe());
				} else {
					s.cartasJogadas[i][k].setLetra(c.getLetra());
					s.cartasJogadas[i][k].setNaipe(c.getNaipe());
				}
				// Se for uma carta fechada, limpa letra/naipe na cópia (pra
				// evitar que uma estratégia maligna tente espiar uma carta
				// fechada)
				if (c != null && c.isFechada()) {
					s.cartasJogadas[i][k].setFechada(true);
					s.cartasJogadas[i][k].setLetra(Carta.LETRA_NENHUMA);
					s.cartasJogadas[i][k].setNaipe(Carta.NAIPE_NENHUM);
				}
			}

	}

	/**
	 * @return True para jogo sem os 4,5,6 e 7.
	 */
	public boolean isBaralhoLimpo() {
		return baralhoLimpo;
	}

	/**
	 * @return True para manilhas fixas (sem "vira")
	 */
	public boolean isManilhaVelha() {
		return manilhaVelha;
	}

	/**
	 * Não faz nada, mas permite que subclasses (como a JogoLocalServer) façam
	 * pausas em momentos específicos do jogo.
	 * 
	 * @param ms Tempo em milissegundos da pausa.
	 */
	protected void pause(int ms) {
		return;
	}

}
