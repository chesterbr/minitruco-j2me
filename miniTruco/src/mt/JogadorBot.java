package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright © 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
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

import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Display;

/**
 * Jogador controlado pelo celular ou pelo servidor.
 * <p>
 * É preciso "plugar" uma estratégia para que o jogador funcione.
 * 
 * Este jogador vai interagir com a mesa no papel 
 * do jogador humano, porém mantendo a funcionalidade
 * do jogador CPU.
 * 
 * Esta classe foi criada para não "zonear" com as
 * outras classes que estão redondas...
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
	 * Para interação com a mesa
	 */
	private Mesa mesa;
	private Display display;
	
	/**
	 * Estrategia que está controlando este jogador
	 */
	private Estrategia estrategia;

	/**
	 * Quantidade de jogadores cuja resposta estamos esperando para um pedido de
	 * truco.
	 */
	private int numRespostasAguardando = 0;

	/**
	 * Sinaliza se os adversários aceitaram um pedido de truco
	 */
	private boolean aceitaramTruco;
	
	/**
	 * Cartas que ainda não foram jogadas
	 */
	private Vector cartasRestantes = new Vector(3);
	
	/**
	 * Pontos da mão
	 */	
	private int pontosNos, pontosEles;
	
	/**
	 * Pontos com relação ao número de partidas
	 */	
	private int vaquinhasNoPastoDplA, vaquinhasNoPastoDplB;
	
	/**
	 * Situação atual do jogo (para o estrategia)
	 */
	SituacaoJogo situacaoJogo = new SituacaoJogo();
	
	/**
	 * Cria um novo jogador CPU (BOT), buscando a estratégia pelo nome.
	 * 
	 * @param nomeEstrategia
	 *            Nome da estratégia
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

	// Sorteia um número inteiro entre 1 e o argumento do método
	private int sorteio(int maxNumero)
	{
		return (Math.abs(random.nextInt())%maxNumero + 1);		
	}

	/**
	 * Retorna a posição do jogador na tela (assumindo que o jogador humano está
	 * na posição inferior, i.e., 1).
	 * <p>
	 * 
	 * @param j
	 * @return 1 para a posição inferior, 2 para a direita, 3 para cima, 4 para
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
	 * Atualiza a situação do jogo (para as estratégias)
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
	 * Habilita (ou desliga) a escolha de cartas, dependendo de ser ou não a vez
	 * do jogador
	 */
	public void vez(Jogador j, boolean podeFechada) {
	
		// Aciona o indicador de "vez" para o jogador atual
		mesa.setPosicaoDaVez(posicaoNaTela(j));
		
		if (this.equals(j)) {
			
			// Dá um tempinho, pra fingir que está "pensando"
			try {
				Thread.sleep(Math.abs(random.nextInt()) % 250 + 200);
			} catch (InterruptedException e) {
				// Nada, apenas timing...
			}

			// Atualiza a situação do jogo (incluindo as cartas na mão)
			atualizaSituacaoJogo();
			situacaoJogo.podeFechada = podeFechada;

			// Solicita que o estrategia jogue
			int posCarta = estrategia.joga(situacaoJogo);

			// Se houve truco, processa, e, após tudo resolvido, repete a jogada
			if (posCarta == -1) {

				// Faz a solicitação de truco numa nova thread
				// (usando o próprio JogadorCPU como Runnable - era uma inner
				// class, mas otimizei para reduzir o .jar)
				aceitaramTruco = false;
				numRespostasAguardando = 2;
				Thread t = new Thread(this);
				t.start();
				// Aguarda pelas respostas
				while (numRespostasAguardando > 0) {
					Thread.yield();
				}
				// Se não aceitaram, desencana...
				if (!aceitaramTruco)
					return;
				// ...caso contrário, vamos seguir o jogo
				// atualizaSituacaoJogo();
				situacaoJogo.valorProximaAposta = 0;
				posCarta = estrategia.joga(situacaoJogo);
			}

			// Joga a carta selecionada e remove ela da mão
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
	 * Envia a notificação de aumento de aposta.
	 * <p>
	 * É feito em thread separada para que o vez() aguarde as respostas sem se
	 * perder.
	 */
	public void run() {
		jogo.aumentaAposta(this);
	}

	public void pediuAumentoAposta(Jogador j, int valor) {

		// Mostra o balãozinho com o pedido
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
			// truco, o estrategia do outro não é consultado (caso o fosse, ele
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
			// Nós aceitamos um truco, então podemos aumentar
			// (i.e., se foi truco, podemos pedir 6, se for 6, podemos pedir 9,
			// etc.) até o limite de 12
			if (valor != 12) {
				valorProximaAposta = valor + 3;
			}
		} else {
			// Eles aceitaram um truco, temos que esperar eles pedirem
			valorProximaAposta = 0;
		}

		// Balãozinho
		String texto = "";
		texto = BALAO_TEXTOS_DESCE[sorteio(BALAO_TEXTOS_DESCE.length)-1];
		mesa.balao(posicaoNaTela(j), texto, 800);
		
		// Caso tenha sido eu quem pediu o truco, retoma a vez
		mesa.retomaVezDepoisDoAumento();
		
	}

	public void recusouAumentoAposta(Jogador j) {

		// Notifica o estrategia
		estrategia.recusouAumentoAposta(j.getPosicao());

		// Se estivermos aguardando resposta, contabiliza (e deixa o adversário
		// perceber)
		if (numRespostasAguardando > 0) {
			numRespostasAguardando--;
			Thread.yield();
		}

		// Balãozinho
		String texto = "";
		texto = BALAO_TEXTOS_RECUSA[sorteio(BALAO_TEXTOS_RECUSA.length)-1];
		mesa.balao(posicaoNaTela(j), texto, 500);
	}

	public void jogadaRecusada(int numJogadores, int equipeTrucando,
			Jogador jogadorDaVez) {
		// Não faz nada
	}

	public void rodadaFechada(int numRodada, int resultado, Jogador jogadorQueTorna) {
		// Remove o indicador de vez
		mesa.setPosicaoDaVez(0);

		// Se houver uma carta vencedora, coloca-a em destaque
		if (resultado != 3) {
			mesa.cartaVencedora = jogo.getCartasDaRodada(numRodada)[jogadorQueTorna
					.getPosicao() - 1];
		}

		// Atualiza o placar (considerando as equipes do jogo, não a posição na
		// mesa)
		int icone;
		if (resultado == getEquipe()) {
			icone = 1; // Vitória
		} else if (resultado == 3) {
			icone = 3; // Empate
		} else {
			icone = 2; // Derrota
		}
		mesa.setStatusMao(numRodada, icone);

		// Uma vez atualizado o placar (e concluída a animação), escurece as
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

		// Balãozinho
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
		// Troca a primeira carta não-jogada
		// do jogador na tela (que era um dummy, já que não sabíamos ainda o
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
			// para a mesa (a animação vai virar a carta e setar como jogada)
			mesa.descarta(c, posicaoNaTela(j));
			mesa.setPosicaoDaVez(0);
	}

	public void inicioMao() {

		// Melhor não sair enquanto estiver distriubindo...
		mesa.removeCommand(MiniTruco.sairPartidaCommand);

		// Limpa a mesa
		mesa.limpa();

		// Distribui as cartas em círculo
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

		// Informa que ninguém aceitou mão de 11 (para não duplicar o balão)
		jaAceitou = false;

		// Permite sair novamente
		mesa.addCommand(MiniTruco.sairPartidaCommand);

		// Notifica o estrategia
		estrategia.inicioMao();

		// Guarda as cartas que estão na mão do jogador
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
		
		// Início
		mesa.setAberturaVisivel(false);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {
		// Mostra o balão (no caso do balão de aceitar, apenas se for o 1o.
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
		// Pergunta ao estrategia se ele topa a mão de 11, devolvendo
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
		// Não precisa tratar
	}

}
