package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright © 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * (frases aleatórias para balões)
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

	// Sorteia um número inteiro entre 1 e o argumento do método
	private int sorteio(int maxNumero) {
		return (Math.abs(random.nextInt()) % maxNumero + 1);
	}

	// Construtor
	public JogadorHumano(Display display, Mesa mesa) {
		this.setDisplay(display);
		this.mesa = mesa;
		mesa.setJogador(this);
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

	public void cartaJogada(Jogador j, Carta c) {
		// Se a carta não for uma das minhas, troca a primeira carta não-jogada
		// do jogador na tela (que era um dummy, já que não sabíamos ainda o
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
			// para a mesa (a animação vai virar a carta e setar como jogada)
			mesa.descarta(c, posicaoNaTela(j));
			mesa.setPosicaoDaVez(0);
		}
		// Obs.: para as "minhas" cartas o descarte foi feito na hora (para
		// evitar o delay no multiplayer) e não havia um marcador para desligar
		// Em resumo: não faço nada!
	}

	/**
	 * Habilita (ou desliga) a escolha de cartas, dependendo de ser ou não a vez
	 * do jogador
	 */
	public void vez(Jogador j, boolean podeFechada) {

		// Aciona o indicador de "vez" para o jogador atual
		mesa.setPosicaoDaVez(posicaoNaTela(j));

		if (this.equals(j)) {
			// Minha vez: Monta a lista de cartas selecionáveis
			Vector cs = new Vector(3);
			for (int i = 0; i <= 2; i++) {
				Carta c = this.getCartas()[i];
				if (!mesa.isJogada(c)) {
					cs.addElement(c);
				}
			}
			// Atribui a lista à mesa e seleciona a primeira carta
			mesa.setCartasSelecionaveis(cs);
			mesa.setCartaSelecionada((Carta) cs.elementAt(0));

			if (!jogo.isAlguemTem11Pontos()) {
				// Adiciona o comando de aposta (truco, seis, etc.), conforme a
				// situação
				mesa.adicionaComandoAposta(valorProximaAposta);
			}
			mesa.setPodeFechada(podeFechada);

		} else {
			// Não é minha vez, remove cursor de seleção
			mesa.setCartasSelecionaveis(null);
			mesa.setCartaSelecionada(null);

			// Impede que o jogador peça truco, 6, etc., fora da vez
			mesa.removeComandoAposta();

		}
		mesa.repaint();

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

		// Se tem alguém trucando, eu é que não posso trucar
		mesa.removeComandoAposta();

		// Se for a equipe adversária, tenho que poder responder
		if (j.getEquipe() != this.getEquipe()) {
			mesa.adicionaOpcoesAceite();
		}

		// Mostra o balãozinho com o pedido
		String texto = "";
		switch (valor) {
		case 3:
			texto = MiniTruco.BALAO_TEXTOS_TRUCO[sorteio(MiniTruco.BALAO_TEXTOS_TRUCO.length) - 1];
			break;
		case 6:
			texto = MiniTruco.BALAO_TEXTOS_SEIS[sorteio(MiniTruco.BALAO_TEXTOS_SEIS.length) - 1];
			break;
		case 9:
			texto = MiniTruco.BALAO_TEXTOS_NOVE[sorteio(MiniTruco.BALAO_TEXTOS_NOVE.length) - 1];
			break;
		default:
			texto = MiniTruco.BALAO_TEXTOS_DOZE[sorteio(MiniTruco.BALAO_TEXTOS_DOZE.length) - 1];
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

		// Remove comandos de aceite (se era da outra equipe, tanto faz,
		// e se era a minha o parceiro aceitou, tanto faz o que eu escolher)
		mesa.removeOpcoesAceite();

		// Balãozinho
		String texto = "";
		texto = MiniTruco.BALAO_TEXTOS_DESCE[sorteio(MiniTruco.BALAO_TEXTOS_DESCE.length) - 1];
		mesa.balao(posicaoNaTela(j), texto, 800);

		// Caso tenha sido eu quem pediu o truco, retoma a vez
		mesa.retomaVezDepoisDoAumento();

	}

	public void recusouAumentoAposta(Jogador j) {

		// Balãozinho
		String texto = "";
		texto = MiniTruco.BALAO_TEXTOS_RECUSA[sorteio(MiniTruco.BALAO_TEXTOS_RECUSA.length) - 1];
		mesa.balao(posicaoNaTela(j), texto, 500);

	}

	public void inicioMao() {

		// Melhor não sair enquanto estiver distriubindo...
		mesa.removeCommand(MiniTruco.sairPartidaCommand);

		// Limpa a mesa
		mesa.limpa();

		// Distribui as cartas em círculo
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

		// Informa que ninguém aceitou mão de 11 (para não duplicar o balão)
		jaAceitou = false;

		// Permite sair novamente
		mesa.addCommand(MiniTruco.sairPartidaCommand);

	}

	/**
	 * Cria a thread que efetua a jogada para esta carta
	 * 
	 * @param c
	 *            Carta que o usuário selecionou
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
	 * Efetua a jogada quando uma carta é clicada (para que as animações
	 * funcionem, é necessário que as jogadas sejam feitas em uma thread
	 * separada)
	 */
	public void run() {
		// Originalmente, nós esperávamos um retorno do jogo para
		// liberar a carta na mesa. No entanto, isso dá um certo
		// atraso no multiplayer.
		// Para evitar isso, vamos assumir que a interface humana só faz
		// jogadas permitidas e já descartar
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

		// for (int i = 0; i <= 3; i++) {
		// jogo.getCartasDaMao(numMao)[i].setVirada(false);
		// }

	}

	private int pontosNos, pontosEles;

	private boolean jaAceitou;

	public void maoFechada(int[] pontosEquipe, int[] vaquinhasNoPasto) {

		// Guarda os pontos (nossos e deles) e atualiza o placar
		pontosNos = pontosEquipe[getEquipe() - 1];
		pontosEles = pontosEquipe[getEquipeAdversaria() - 1];
		mesa.atualizaPlacar(pontosNos, pontosEles);

	}

	public void jogoFechado(int numEquipeVencedora, int[] vaquinhasNoPasto) {
		mesa.mostraMenuFimPartida();
		Thread t;
		if (numEquipeVencedora == this.getEquipe()) {
			t = new Thread() {
				public void run() {
					// Balãozinho
					String texto = "";
					texto = MiniTruco.BALAO_TEXTOS_VENCEDOR[sorteio(MiniTruco.BALAO_TEXTOS_VENCEDOR.length) - 1];
					mesa.balao(1, texto, 5000);
				}
			};
		} else {
			t = new Thread() {
				public void run() {
					// Balãozinho
					String texto = "";
					texto = MiniTruco.BALAO_TEXTOS_DERROTADO[sorteio(MiniTruco.BALAO_TEXTOS_DERROTADO.length) - 1];
					mesa.balao(1, texto, 5000);
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

		// Mostra o balão (no caso do balão de aceitar, apenas se for o 1o.
		// aceite)
		if (!aceita || (aceita && !jaAceitou)) {
			String texto = "";
			if (aceita)
				texto = MiniTruco.BALAO_TEXTOS_ACEITAMAO11[sorteio(MiniTruco.BALAO_TEXTOS_ACEITAMAO11.length) - 1];
			else
				texto = MiniTruco.BALAO_TEXTOS_RECUSAMAO11[sorteio(MiniTruco.BALAO_TEXTOS_RECUSAMAO11.length) - 1];
			mesa.balao(posicaoNaTela(j), texto, 1000);
		}

		if (aceita) {
			// Se foi o parceiro que aceitou, dá um tempinho (pra dar tempo de
			// ver as cartas dele)
			if (j != this && j.getEquipe() == this.getEquipe()) {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					// Timing, não faz nada
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

		// Adiciona opções de menu
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
			mesa.removeComandoAposta();
			mesa.suspendeVezParaAumento();
			jogo.aumentaAposta(this);
		} else if (cmd == MiniTruco.mostraNomesJogadoresCommand) {
			mesa.mostraNomesJogadores(1000);	
		}
			

	}

	public void jogoAbortado(int posicao) {
		int posTela = posicaoNaTela(jogo.getJogador(posicao));
		if (posTela != 1) {
			// Sacaneia quem abortou (apenas no multiplayer)
			mesa.balao(posTela, "Fui...", 1200);
		}

	}

}
