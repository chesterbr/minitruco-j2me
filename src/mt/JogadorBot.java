package mt;

/*
 * Copyright � 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright � 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
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

import javax.microedition.lcdui.Display;

/**
 * Jogador controlado pelo celular ou pelo servidor.
 * <p>
 * � preciso "plugar" uma estrat�gia para que o jogador funcione.
 * 
 * Este jogador vai interagir com a mesa no papel 
 * do jogador humano, por�m mantendo a funcionalidade
 * do jogador CPU.
 * 
 * Esta classe foi criada para n�o "zonear" com as
 * outras classes que est�o redondas...
 * 
 * @author Chester / Sandro
 * @see Estrategia
 * 
 */
public class JogadorBot extends Jogador implements Runnable {

	private static Random random = new Random();
	private boolean jaAceitou;
	int valorProximaAposta = 0;
	
	/**
	 * Para intera��o com a mesa
	 */
	private Mesa mesa;
	private Display display;
	
	/**
	 * Estrategia que est� controlando este jogador
	 */
	private Estrategia estrategia;

	/**
	 * Quantidade de jogadores cuja resposta estamos esperando para um pedido de
	 * truco.
	 */
	private int numRespostasAguardando = 0;

	/**
	 * Sinaliza se os advers�rios aceitaram um pedido de truco
	 */
	private boolean aceitaramTruco;
	
	/**
	 * Cartas que ainda n�o foram jogadas
	 */
	private Vector cartasRestantes = new Vector(3);
	
	/**
	 * Pontos da m�o
	 */	
	private int pontosNos, pontosEles;
	
	/**
	 * Pontos com rela��o ao n�mero de partidas
	 */	
	private int vaquinhasNoPastoDplA, vaquinhasNoPastoDplB;
	
	/**
	 * Situa��o atual do jogo (para o estrategia)
	 */
	SituacaoJogo situacaoJogo = new SituacaoJogo();
	
	/**
	 * Cria um novo jogador CPU (BOT), buscando a estrat�gia pelo nome.
	 * 
	 * @param nomeEstrategia
	 *            Nome da estrat�gia
	 */
	public JogadorBot(String nomeEstrategia, Display display, Mesa mesa) {
		while (nomeEstrategia.equals("Sortear")) {
			nomeEstrategia = MiniTruco.OPCOES_ESTRATEGIAS[(random.nextInt() >>> 1)
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

		this.setDisplay(display);
		this.mesa = mesa;
		mesa.setJogadorBot(this);
	}

	// Sorteia um n�mero inteiro entre 1 e o argumento do m�todo
	private int sorteio(int maxNumero)
	{
		return (Math.abs(random.nextInt())%maxNumero + 1);		
	}

	/**
	 * Retorna a posi��o do jogador na tela (assumindo que o jogador humano est�
	 * na posi��o inferior, i.e., 1).
	 * <p>
	 * 
	 * @param j
	 * @return 1 para a posi��o inferior, 2 para a direita, 3 para cima, 4 para
	 *         esquerda
	 */
	private int posicaoNaTela(Jogador j) {
		int pos = j.getPosicao() - this.getPosicao() + 1;
		if (pos < 1) {
			pos = pos + 4;
		}
		return pos;
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
	
	/**
	 * Habilita (ou desliga) a escolha de cartas, dependendo de ser ou n�o a vez
	 * do jogador
	 */
	public void vez(Jogador j, boolean podeFechada) {
	
		// Aciona o indicador de "vez" para o jogador atual
		mesa.setPosicaoDaVez(posicaoNaTela(j));
		
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
		
		mesa.repaint();
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

		// Mostra o bal�ozinho com o pedido
		String texto = "";
		switch (valor) {
		case 3:
			texto = BALAO_TEXTOS_TRUCO[sorteio(BALAO_TEXTOS_TRUCO.length)-1];
			break;
		case 6:
			texto = BALAO_TEXTOS_SEIS[sorteio(BALAO_TEXTOS_SEIS.length)-1];
			break;
		case 9:
			texto = BALAO_TEXTOS_NOVE[sorteio(BALAO_TEXTOS_NOVE.length)-1];
			break;
		default:
			texto = BALAO_TEXTOS_DOZE[sorteio(BALAO_TEXTOS_DOZE.length)-1];
			break;
		}
		mesa.balao(posicaoNaTela(j), texto, 1000 + 200 * (valor / 3));

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

		// Bal�ozinho
		String texto = "";
		texto = BALAO_TEXTOS_DESCE[sorteio(BALAO_TEXTOS_DESCE.length)-1];
		mesa.balao(posicaoNaTela(j), texto, 800);
		
		// Caso tenha sido eu quem pediu o truco, retoma a vez
		mesa.retomaVezDepoisDoAumento();
		
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

		// Bal�ozinho
		String texto = "";
		texto = BALAO_TEXTOS_RECUSA[sorteio(BALAO_TEXTOS_RECUSA.length)-1];
		mesa.balao(posicaoNaTela(j), texto, 500);
	}

	public void jogadaRecusada(int numJogadores, int equipeTrucando,
			Jogador jogadorDaVez) {
		// N�o faz nada
	}

	public void rodadaFechada(int numRodada, int resultado, Jogador jogadorQueTorna) {
		// Remove o indicador de vez
		mesa.setPosicaoDaVez(0);

		// Se houver uma carta vencedora, coloca-a em destaque
		if (resultado != 3) {
			mesa.cartaVencedora = jogo.getCartasDaRodada(numRodada)[jogadorQueTorna
					.getPosicao() - 1];
		}

		// Atualiza o placar (considerando as equipes do jogo, n�o a posi��o na
		// mesa)
		int icone;
		if (resultado == getEquipe()) {
			icone = 1; // Vit�ria
		} else if (resultado == 3) {
			icone = 3; // Empate
		} else {
			icone = 2; // Derrota
		}
		mesa.setStatusMao(numRodada, icone);

		// Uma vez atualizado o placar (e conclu�da a anima��o), escurece as
		// cartas jogadas, deixando a tela "limpa"
		mesa.cartaVencedora = null;

		Carta[] cartasVelhas = jogo.getCartasDaRodada(numRodada);
		for (int i = 0; i < cartasVelhas.length; i++) {
			cartasVelhas[i].setCartaEmJogo(false);
		}
	}

	public void maoFechada(int[] pontosEquipe, int[] vaquinhasNoPasto) {

		// Guarda os pontos e atualiza o placar
		pontosNos = pontosEquipe[getEquipe() - 1];
		pontosEles = pontosEquipe[getEquipeAdversaria() - 1];
		vaquinhasNoPastoDplA = vaquinhasNoPasto[getEquipe() - 1];
		vaquinhasNoPastoDplB = vaquinhasNoPasto[getEquipeAdversaria() - 1];
		mesa.atualizaPlacarComVaquinhasInfo(pontosNos, pontosEles, vaquinhasNoPastoDplA, vaquinhasNoPastoDplB);

	}

	public void jogoFechado(int numEquipeVencedora, int[] vaquinhasNoPasto) {

		// Bal�ozinho
		String texto = "";
		if (numEquipeVencedora == this.getEquipe()) {
			texto = BALAO_TEXTOS_VENCEDOR[sorteio(BALAO_TEXTOS_VENCEDOR.length)-1];
		}
		else {
			texto = BALAO_TEXTOS_DERROTADO[sorteio(BALAO_TEXTOS_DERROTADO.length)-1];
		}
		mesa.balao(1, texto, 5000);
		mesa.mostraResultadoFinalModoCE(vaquinhasNoPasto[0], vaquinhasNoPasto[1]);
	
	}

	public void cartaJogada(Jogador j, Carta c) {
		// Troca a primeira carta n�o-jogada
		// do jogador na tela (que era um dummy, j� que n�o sab�amos ainda o
		// valor) por ela
			int posPrimeira = (posicaoNaTela(j) - 1) * 3;
			for (int i = posPrimeira; i <= posPrimeira + 2; i++) {
				// for (int i = posPrimeira+2; i >= posPrimeira ; i--) {
				Carta cartaFalsa = (Carta) mesa.getCartas().elementAt(i);
				if (!mesa.isJogada(cartaFalsa)) {
					// MiniTruco.debug("Pos:
					// "+i+":"+cartaFalsa.getTop()+","+cartaFalsa.getLeft());
					c.setTop(cartaFalsa.getTop());
					c.setLeft(cartaFalsa.getLeft());
					mesa.getCartas().setElementAt(c, i);
					break;
				}
			}
			// Desliga o indicador de "vez" e anima a carta sendo jogada
			// para a mesa (a anima��o vai virar a carta e setar como jogada)
			mesa.descarta(c, posicaoNaTela(j));
			mesa.setPosicaoDaVez(0);
	}

	public void inicioMao() {

		// Melhor n�o sair enquanto estiver distriubindo...
		mesa.removeCommand(MiniTruco.sairPartidaCommand);

		// Limpa a mesa
		mesa.limpa();

		// Distribui as cartas em c�rculo
		for (int i = 0; i <= 2; i++) {
			for (int j = 1; j <= 4; j++) {
				mesa.distribui(new Carta(), j, i);
			}
		}

		// Distribui a carta da mesa (se for manilha nova)
		if (!jogo.isManilhaVelha()) {
			mesa.distribuiCartaDaMesa(jogo.cartaDaMesa);
		}

		// Atualiza o placar
		mesa.atualizaPlacarComVaquinhasInfo(pontosNos, pontosEles, vaquinhasNoPastoDplA, vaquinhasNoPastoDplB);

		// Informa que ningu�m aceitou m�o de 11 (para n�o duplicar o bal�o)
		jaAceitou = false;

		// Permite sair novamente
		mesa.addCommand(MiniTruco.sairPartidaCommand);

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

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Display getDisplay() {
		return display;
	}

	public void inicioPartida() {
		// Avisa o estrategia
		estrategia.inicioPartida();
		
		// In�cio
		mesa.setAberturaVisivel(false);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// Mostra o bal�o (no caso do bal�o de aceitar, apenas se for o 1o.
		// aceite)
		if (!aceita || (aceita && !jaAceitou)) {
			String texto = "";
			if(aceita)
				texto = BALAO_TEXTOS_ACEITAMAO11[sorteio(BALAO_TEXTOS_ACEITAMAO11.length)-1];
			else
				texto = BALAO_TEXTOS_RECUSAMAO11[sorteio(BALAO_TEXTOS_RECUSAMAO11.length)-1];
			mesa.balao(posicaoNaTela(j), texto, 1000);
		}
		
		if(aceita)
			jaAceitou = true;
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
