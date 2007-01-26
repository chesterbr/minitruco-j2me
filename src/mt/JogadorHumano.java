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

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;

/**
 * Jogador interagindo no celular.
 * 
 * @author Chester
 * 
 */
public class JogadorHumano extends Jogador implements Runnable {

	private Mesa mesa;

	private Display display;

	public JogadorHumano(Display display, Mesa mesa) {
		this.setDisplay(display);
		this.mesa = mesa;
		mesa.setJogador(this);
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

	public void cartaJogada(Jogador j, Carta c) {
		// Se a carta n�o for uma das minhas, troca a primeira carta n�o-jogada
		// do jogador na tela (que era um dummy, j� que n�o sab�amos ainda o
		// valor) por ela
		if (!this.equals(j)) {
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
		// Obs.: para as "minhas" cartas o descarte foi feito na hora (para
		// evitar o delay no multiplayer) e n�o havia um marcador para desligar
		// Em resumo: n�o fa�o nada!
	}

	/**
	 * Habilita (ou desliga) a escolha de cartas, dependendo de ser ou n�o a vez
	 * do jogador
	 */
	public void vez(Jogador j, boolean podeFechada) {

		// Aciona o indicador de "vez" para o jogador atual
		mesa.setPosicaoDaVez(posicaoNaTela(j));

		if (this.equals(j)) {
			// Minha vez: Monta a lista de cartas selecion�veis
			Vector cs = new Vector(3);
			for (int i = 0; i <= 2; i++) {
				Carta c = this.getCartas()[i];
				if (!mesa.isJogada(c)) {
					cs.addElement(c);
				}
			}
			// Atribui a lista � mesa e seleciona a primeira carta
			mesa.setCartasSelecionaveis(cs);
			mesa.setCartaSelecionada((Carta) cs.elementAt(0));

			if (!jogo.isAlguemTem11Pontos()) {
				// Adiciona o comando de aposta (truco, seis, etc.), conforme a
				// situa��o
				mesa.adicionaComandoAposta(valorProximaAposta);
			}
			mesa.setPodeFechada(podeFechada);

		} else {
			// N�o � minha vez, remove cursor de sele��o
			mesa.setCartasSelecionaveis(null);
			mesa.setCartaSelecionada(null);

			// Impede que o jogador pe�a truco, 6, etc., fora da vez
			mesa.removeComandoAposta();

		}
		mesa.repaint();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

		// Se tem algu�m trucando, eu � que n�o posso trucar
		mesa.removeComandoAposta();

		// Se for a equipe advers�ria, tenho que poder responder
		if (j.getEquipe() != this.getEquipe()) {
			mesa.adicionaOpcoesAceite();
		}

		// Mostra o bal�ozinho com o pedido
		String texto;
		switch (valor) {
		case 3:
			texto = Mesa.TEXTO_TRUCO;
			break;
		case 6:
			texto = Mesa.TEXTO_SEIS;
			break;
		case 9:
			texto = Mesa.TEXTO_NOVE;
			break;
		default:
			texto = Mesa.TEXTO_DOZE;
			break;
		}
		mesa.balao(posicaoNaTela(j), texto, 1000 + 200 * (valor / 3));
	}

	/**
	 * Valor que poderemos apostar (3=truco, 6, 9, 12) se a gente puder
	 */
	int valorProximaAposta = 0;

	public void aceitouAumentoAposta(Jogador j, int valor) {
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

		// Remove comandos de aceite (se era da outra equipe, tanto faz,
		// e se era a minha o parceiro aceitou, tanto faz o que eu escolher)
		mesa.removeOpcoesAceite();

		// Bal�ozinho
		mesa.balao(posicaoNaTela(j), "Desce!", 800);

		// Caso tenha sido eu quem pediu o truco, retoma a vez
		mesa.retomaVezDepoisDoAumento();

	}

	public void recusouAumentoAposta(Jogador j) {

		// Bal�ozinho
		mesa.balao(posicaoNaTela(j), "Tou fora", 500);

	}

	public void inicioMao() {

		// Melhor n�o sair enquanto estiver distriubindo...
		mesa.removeCommand(MiniTruco.sairPartidaCommand);

		// Limpa a mesa
		mesa.limpa();

		// Distribui as cartas em c�rculo
		for (int i = 0; i <= 2; i++) {
			for (int j = 1; j <= 4; j++) {
				if (j == 1) {
					mesa.distribui(getCartas()[i], j, i);
				} else {
					mesa.distribui(new Carta(), j, i);
				}
			}
		}

		// Distribui a carta da mesa (se for manilha nova)
		if (!jogo.isManilhaVelha()) {
			mesa.distribuiCartaDaMesa(jogo.cartaDaMesa);
		}

		// Atualiza o placar
		mesa.atualizaPlacar(pontosNos, pontosEles);

		// Libera o jogador para pedir truco
		valorProximaAposta = 3;

		// Informa que ningu�m aceitou m�o de 11 (para n�o duplicar o bal�o)
		jaAceitou = false;

		// Permite sair novamente
		mesa.addCommand(MiniTruco.sairPartidaCommand);

	}

	/**
	 * Cria a thread que efetua a jogada para esta carta
	 * 
	 * @param c
	 *            Carta que o usu�rio selecionou
	 */
	public void cartaClicada(Carta c) {
		mesa.removeComandoAposta();
		this.cartaClicada = c;
		Thread t = new Thread(this);
		t.start();
	}

	/**
	 * Carta que em que o jogador clicou (selecionou)
	 */
	private Carta cartaClicada;

	/**
	 * Efetua a jogada quando uma carta � clicada (para que as anima��es
	 * funcionem, � necess�rio que as jogadas sejam feitas em uma thread
	 * separada)
	 */
	public void run() {
		// Originalmente, n�s esper�vamos um retorno do jogo para
		// liberar a carta na mesa. No entanto, isso d� um certo
		// atraso no multiplayer.
		// Para evitar isso, vamos assumir que a interface humana s� faz
		// jogadas permitidas e j� descartar
		mesa.descarta(cartaClicada, 1);
		jogo.jogaCarta(this, cartaClicada);
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Display getDisplay() {
		return display;
	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {

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

		// for (int i = 0; i <= 3; i++) {
		// jogo.getCartasDaMao(numMao)[i].setVirada(false);
		// }

	}

	private int pontosNos, pontosEles;

	private boolean jaAceitou;

	public void maoFechada(int[] pontosEquipe) {

		// Guarda os pontos (nossos e deles) e atualiza o placar
		pontosNos = pontosEquipe[getEquipe() - 1];
		pontosEles = pontosEquipe[getEquipeAdversaria() - 1];
		mesa.atualizaPlacar(pontosNos, pontosEles);

	}

	public void jogoFechado(int numEquipeVencedora) {
		mesa.mostraMenuFimPartida();
		Thread t;
		if (numEquipeVencedora == this.getEquipe()) {
			t = new Thread() {
				public void run() {
					mesa.balao(1, "TOOOOMEM!!!", 5000);
				}
			};
		} else {
			t = new Thread() {
				public void run() {
					mesa.balao(1, ":-(", 5000);
				}
			};
		}
		t.start();
	}

	public void inicioPartida() {
		mesa.setAberturaVisivel(false);
	}

	public void decidiuMao11(Jogador j, boolean aceita) {

		// Se foi o jogador que respondeu, ou se for uma resposta
		// positiva (de quem quer que seja), garante que o menu seja
		// removido
		if (j == this || aceita) {
			mesa.removeOpcoesMao11();
		}

		// Mostra o bal�o (no caso do bal�o de aceitar, apenas se for o 1o.
		// aceite)
		if (!aceita || (aceita && !jaAceitou)) {
			mesa.balao(posicaoNaTela(j), (aceita ? "Vamos jogar!"
					: "N\u00E3o quero"), 1000);
		}

		if (aceita) {
			// Se foi o parceiro que aceitou, d� um tempinho (pra dar tempo de
			// ver as cartas dele)
			if (j != this && j.getEquipe() == this.getEquipe()) {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					// Timing, n�o faz nada
				}
			}
			// Se foi a nossa equipe que topou, esconde as cartas do parceiro
			if (j.getEquipe() == this.getEquipe()) {
				mesa.escondeCartasParceiro();
			}
			jaAceitou = true;
		}

	}

	public void informaMao11(Carta[] cartasParceiro) {

		// Exibe as cartas do parceiro
		mesa.mostraCartasParceiro(cartasParceiro);

		// Adiciona op��es de menu
		mesa.adicionaOpcoesMao11();

	}

	/**
	 * Executa comandos de menu (encaminhados pela midlet principal)
	 * 
	 * @param cmd
	 *            Comando clicado
	 */
	public void executaComando(Command cmd) {
		if (cmd == MiniTruco.aceitaCommand) {
			mesa.removeOpcoesAceite();
			jogo.respondeAumento(this, true);
		} else if (cmd == MiniTruco.recusaCommand) {
			mesa.removeOpcoesAceite();
			jogo.respondeAumento(this, false);
		} else if (cmd == MiniTruco.aceitaMao11Command) {
			jogo.decideMao11(this, true);
		} else if (cmd == MiniTruco.recusaMao11Command) {
			jogo.decideMao11(this, false);
		} else if (cmd == MiniTruco.trucoCommand
				|| cmd == MiniTruco.seisCommand || cmd == MiniTruco.noveCommand
				|| cmd == MiniTruco.dozeCommand) {
			mesa.suspendeVezParaAumento();
			jogo.aumentaAposta(this);
		}

	}

	public void jogoAbortado(int posicao) {
		int posTela = posicaoNaTela(jogo.getJogador(posicao));
		if (posTela != 1) {
			// Sacaneia quem abortou (apenas no multiplayer)
			mesa.balao(posTela, "Tchau, pessoal!", 1000);
		}

	}

}
