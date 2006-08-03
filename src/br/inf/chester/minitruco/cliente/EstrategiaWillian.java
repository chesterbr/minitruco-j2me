package br.inf.chester.minitruco.cliente;

/*
 * Copyright � 2006 Willian Gigliotti - wgigliotti@gmail.com
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

/**
 * Estrat�gia inteligente para jogadores CPU
 * 
 * @author Willian
 * 
 */
public class EstrategiaWillian implements Estrategia {

	private static Random r = new Random();

	/**
	 * Vari�vel usada para saber se � poss�vel ou n�o jogar uma carta virada
	 * para baixo... 10 -> pode; 0 -> n�o pode;
	 */
	private static int descarte = 10;

	private int criticoParape = 12;

	/**
	 * Retorna uma string com o nome do Estrategia!
	 */
	public String getNomeEstrategia() {
		return "Willian v1.01";
	}

	/**
	 * Retorna uma String com informacao minhas
	 */
	public String getInfoEstrategia() {
		return "feito por Willian, Fev/2006";
	}

	/**
	 * retornar o indice da maior carta de um vetor de cartas
	 */
	private int maiorCarta(Carta[] cartas, char manilha) {
		int i;
		for (i = 0; i < cartas.length; i++) {
			if (cartas[i] != null)
				break;
		}

		if (i == cartas.length)
			return i;

		int maior = i;
		// System.out.println(cartas[i]);
		for (i++; i < cartas.length; i++) {
			if (cartas[i] != null) {
				if (cartas[i].getValorTruco(manilha) > cartas[maior]
						.getValorTruco(manilha)) {
					maior = i;
				}
			}
		}
		return maior;
	}

	/**
	 * retorna o indice da menor carta de um vetor de cartas
	 */
	private int menorCarta(Carta[] cartas, char manilha) {
		int i;
		for (i = 0; i < cartas.length; i++) {
			if (cartas[i] != null)
				break;
		}

		int maior = i;
		for (i++; i < cartas.length; i++) {
			if (cartas[i] != null) {
				if (cartas[i].getValorTruco(manilha) < cartas[maior]
						.getValorTruco(manilha)) {
					maior = i;
				}
			}
		}
		return maior;

	}

	private int minimoMaior(Carta[] cartas, int valor, char manilha) {
		int i;
		for (i = 0; i < cartas.length; i++) {
			if (cartas[i] != null && cartas[i].getValorTruco(manilha) > valor)
				break;
		}

		int maior = i;
		for (i++; i < cartas.length; i++) {
			if (cartas[i] != null) {
				if (cartas[i].getValorTruco(manilha) < cartas[maior]
						.getValorTruco(manilha)
						&& cartas[i].getValorTruco(manilha) > valor) {
					maior = i;
				}
			}
		}
		return maior;

	}

	/**
	 * 
	 * Julga se eu preciso, ou n�o jogar! Usando o segundo crit�rio:
	 * 
	 * @param s
	 *            Situac�o do jogo!
	 * @return Parceiro ainda n�o jogou -> false; Parceiro jogou fraco -> true;
	 *         Parceiro jogou bem -> false;
	 * 
	 */
	private boolean precisoJogar(SituacaoJogo s) {
		int mao = s.numRodadaAtual - 1;
		int jogador = s.posJogador - 1;
		int posicao = (jogador + 2) % 4;
		int valorCritico = 7; /* 7 = K */

		/*
		 * parceiro ainda nao jogou, N�o estragarei um A. ele ser� macho!
		 */
		if (s.cartasJogadas[mao][posicao] == null)
			return false;

		/* maior carta da mesa */
		int maior = maiorCarta(s.cartasJogadas[mao], s.manilha);

		/* maior carta da mesa � do parceiro */
		if (maior == posicao) {
			/* se a carta do parceiro for boa, deixa a dele :) */
			if (s.cartasJogadas[mao][maior].getValorTruco(s.manilha) <= valorCritico
					&& this.pegarVez(s, s.posJogador) != 4) {
				return true;
			}
			return false;
		}
		return true;
	}

	// Willian: comentei esse m�todo, ele sobrou. No dia que tivermos CVS eu
	// capo. Ass: Chester
	//
	// /**
	// * Diz se a equipe fez ou n�o a primeira rodada
	// *
	// * @param s
	// * @return
	// */
	//
	// private boolean fizRodada(SituacaoJogo s, int rodada) {
	// if (s.numRodadaAtual <= rodada)
	// return false;
	//
	// int posicao = (s.posJogador + 1) % 2 + 1;
	//
	// if (s.resultadoRodada[rodada - 1] == posicao)
	// return true;
	// return false;
	// }

	private int pegarVez(SituacaoJogo s, int jogador) {
		int a = s.posJogadorQueAbriuRodada;
		for (int i = 1; i <= 4; i++) {
			if (a == jogador) {
				return i;
			}
			a = a % 4 + 1;
		}
		return 1;
	}

	private int joga1(SituacaoJogo s) {
		if (precisoJogar(s)) {
			int minhaVez = pegarVez(s, s.posJogador);
			int rodada = s.numRodadaAtual - 1;
			int maiorMao = maiorCarta(s.cartasJogador, s.manilha);
			int maiorMesa = maiorCarta(s.cartasJogadas[rodada], s.manilha);

			/* ninguem jogou ainda, acho que ele nunca entra nesse if */
			if (maiorMesa == 4 || s.cartasJogadas[rodada][maiorMesa] == null) {
				return maiorMao;
			}

			/* se eu for capaz de melhorar as jogadas */
			if (s.cartasJogadas[rodada][maiorMesa].getValorTruco(s.manilha) < s.cartasJogador[maiorMao]
					.getValorTruco(s.manilha)) {
				/* se eu sou um antes do p�, vamos com tudo */
				if (minhaVez == 3) {
					// TODO parap�
					return maiorMao;
				}
				/* se eu for p�, vamos analizar */
				else
					return minimoMaior(s.cartasJogador,
							s.cartasJogadas[rodada][maiorMesa]
									.getValorTruco(s.manilha), s.manilha);
			}
		}
		return menorCarta(s.cartasJogador, s.manilha);
	}

	private int joga2(SituacaoJogo s) {
		if (pegarVez(s, s.posJogador) == 3 && s.valorProximaAposta != 0) {
			if (Math.abs(r.nextInt()) % 100 < 40) {
				return -1;
			}
		}

		if (precisoJogar(s)) {
			int minhaVez = pegarVez(s, s.posJogador);

			int rodada = s.numRodadaAtual - 1;

			int maiorMao = maiorCarta(s.cartasJogador, s.manilha);

			int maiorMesa = maiorCarta(s.cartasJogadas[rodada], s.manilha);

			/* ninguem jogou ainda, acho que ele nunca entra nesse if */
			if (maiorMesa == 4 || s.cartasJogadas[rodada][maiorMesa] == null) {

				return maiorMao;
			}

			/* sou capaz de fazer essa rodada */
			if (s.cartasJogadas[rodada][maiorMesa].getValorTruco(s.manilha) < s.cartasJogador[maiorMao]
					.getValorTruco(s.manilha)) {

				/* se for um antes do p�, vou com tudo! */
				if (minhaVez == 3) {
					if (pegarVez(s, s.posJogador) == 3
							&& s.valorProximaAposta != 0) {
						if (Math.abs(r.nextInt()) % 100 < 40
								|| s.cartasJogador[maiorMao]
										.getValorTruco(s.manilha) > 10) {
							return -1;
						}
					}

					return maiorMao;
				}
				/* sou p�, vamos analizar :) */
				else {

					/* menor carta que mata a dele */
					int minimo = minimoMaior(s.cartasJogador,
							s.cartasJogadas[rodada][maiorMesa]
									.getValorTruco(s.manilha), s.manilha);

					/*
					 * parap� � algo que vai ficar mais legal nas proximas
					 * versoes do Estrategia, por enquanto esse if n�o muda
					 * muito
					 */
					if (s.valorProximaAposta == 0
							|| s.cartasJogador[(minimo + 1) % 2]
									.getValorTruco(s.manilha) < criticoParape)
						return minimo;
					else
						return descarte == 0 ? -1 : 0;
				}
			}
		}
		return (r.nextInt() < 0 ? descarte : 0)
				+ menorCarta(s.cartasJogador, s.manilha);
	}

	private int joga3(SituacaoJogo s) {
		/* tem uns detalhes legais pra terceira rodada, logo eu volto nela... */
		return 0;
	}

	public int joga(SituacaoJogo s) {

		switch (s.numRodadaAtual) {
		case 1:
			return this.joga1(s);
		case 2:
			return this.joga2(s);
		case 3:
			return this.joga3(s);
		}
		/* se n�o for a rodada 1, nem a rodada 2, nem a 3, EU TRUCO */
		return 0;
	}

	/**
	 * Diz a quantidade de vezes que x aparece num vetor!
	 * 
	 * @param vetor
	 * @param limite
	 * @param x
	 * @return
	 */
	int procura(int[] vetor, int limite, int x) {
		int i, qt = 0;
		for (i = 0; i < limite && i < vetor.length; i++) {
			if (vetor[i] == x)
				qt++;
		}
		return qt;
	}

	/**
	 * da um valor de 0-100 para as cartas
	 * 
	 * @param s
	 *            Situa��o atual do jogo
	 * @param indiceCarta
	 *            �ndice (0 a 2) da carta do jogador que estamos avaliando
	 * @return
	 */
	int getChances(SituacaoJogo s, int indiceCarta) {
		// Se ainda nao descartamos, a carta est� em m�os (cartasJogador), caso
		// contr�rio est� na mesa (cartasJogadas)
		Carta c;
		if (indiceCarta < s.cartasJogador.length) {
			c = s.cartasJogador[indiceCarta];
		} else {
			c = s.cartasJogadas[s.numRodadaAtual - 1][s.posJogador - 1];
		}
		Logger.debug("obtendo chances de "
				+ c
				+ (indiceCarta < s.cartasJogador.length ? " em maos"
						: " na mesa"));
		int valor = c.getValorTruco(s.manilha);
		valor = valor <= 0 ? 0 : valor - 1;
		valor = valor >= 14 ? 13 : valor;
		int[] chances = { 0, 0, 0, 0, 0, 0, 0, 0, 10, 40, 60, 80, 100, 120 };
		return chances[valor];
	}

	private boolean aceitaTruco1(SituacaoJogo s) {
		/*
		 * apenas avalios as chances de se dar bem, e rolo os dados n�o tem
		 * muito o que fazer n aprimeira rodada :)
		 * 
		 */
		int chances = getChances(s, 0) + getChances(s, 1) + getChances(s, 2);

		chances = chances / 3;

		if (Math.abs(r.nextInt()) % 100 <= chances + 25)
			return true;
		return false;
	}

	private boolean aceitaTruco2(SituacaoJogo s) {
		int trucador = pegarVez(s, s.posJogadorPedindoAumento);
		int eu = pegarVez(s, s.posJogador);

		Logger.debug("Analizando, truco2 -> 1");
		/* se eu jogo depois do trucador */
		if (eu > trucador) {
			Logger.debug("Analizando, truco2 -> 2");

			/* fizemos a primeira */
			if (eu % 2 == 1) {
				Logger.debug("Analizando, truco2 -> 3");
				return true;
			} else {
				// TODO observar parceiro
				Logger.debug("Analizando, truco2 -> 4");

				/* n�o tornou trucando ... */
				if (trucador != 1 && precisoJogar(s)) {
					Logger.debug("Analizando, truco2 -> 5");
					int maiorMao = maiorCarta(s.cartasJogador, s.manilha);
					int maiorMesa = maiorCarta(s.cartasJogadas[1], s.manilha);
					Logger.debug("Analizando, truco2 -> 6");
					if (s.cartasJogador[maiorMao].getValorTruco(s.manilha) < s.cartasJogadas[1][maiorMesa]
							.getValorTruco(s.manilha))
						return false;
				}
				Logger.debug("Analizando, truco2 -> 7");
				int chances = getChances(s, 0) + getChances(s, 1);

				chances = chances / 2 - 10;
				Logger.debug("Analizando, truco2 -> 8");
				if (Math.abs(r.nextInt()) % 100 <= chances + 25)
					return true;

			}
		} else {

			Logger.debug("Analizando, truco2 -> 9");
			int chances = getChances(s, 0);

			Logger.debug("Analizando, truco2 -> 10");
			chances = chances / 2 + (eu % 2 == 1 ? 20 : -20);
			if (Math.abs(r.nextInt()) % 100 <= chances + 25)
				return true;
		}

		return false;
	}

	private boolean aceitaTruco3(SituacaoJogo s) {

		int trucador = pegarVez(s, s.posJogadorPedindoAumento);
		int eu = pegarVez(s, s.posJogador);

		int[] valor = { -2, -1, -1, 0, 0, 0, 1, 1, 2 };

		int critico = valor[Math.abs(r.nextInt()) % 8] + 10;
		Logger.debug("Analizando, truco3 -> 1");

		if (trucador != 1) {
			Logger.debug("Analizando, truco3 -> 2");
			int maiorMesa = maiorCarta(s.cartasJogadas[1], s.manilha);
			int meuValor;
			if (eu > trucador) {
				Logger.debug("Analizando, truco3 -> 3");
				if (maiorMesa % 2 != eu % 2) {
					Logger.debug("Analizando, truco3 -> 4");
					if (s.cartasJogadas[2][maiorMesa].getValorTruco(s.manilha) > s.cartasJogador[0]
							.getValorTruco(s.manilha))
						return false;
					Logger.debug("Analizando, truco3 -> 5");
					meuValor = s.cartasJogador[0].getValorTruco(s.manilha);
				} else {
					Logger.debug("Analizando, truco3 -> 6");
					meuValor = s.cartasJogador[0].getValorTruco(s.manilha) > s.cartasJogadas[2][maiorMesa]
							.getValorTruco(s.manilha) ? s.cartasJogador[0]
							.getValorTruco(s.manilha)
							: s.cartasJogadas[2][maiorMesa]
									.getValorTruco(s.manilha);
				}
				Logger.debug("Analizando, truco3 -> 7");
			} else {
				if (maiorMesa % 2 == eu % 2) {
					Logger.debug("Analizando, truco3 -> 8");
					meuValor = s.cartasJogadas[2][maiorMesa]
							.getValorTruco(s.manilha);
				} else {
					Logger.debug("Analizando, truco3 -> 9");
					return false;
				}
			}
			Logger.debug("Analizando, truco3 -> 10");
			if (meuValor > critico)
				return true;
			return false;
		}
		Logger.debug("Analizando, truco3 -> 11");
		if (s.cartasJogador[0].getValorTruco(s.manilha) > critico)
			return true;
		return false;
	}

	/**
	 * Diz se eu aceito o truco ou n�o!
	 */
	public boolean aceitaTruco(SituacaoJogo s) {
		switch (s.numRodadaAtual) {
		case 1:
			return aceitaTruco1(s);
		case 2:
			return aceitaTruco2(s);
		case 3:
			return aceitaTruco3(s);
		}

		return false;
	}

	public void inicioPartida() {

	}

	public void inicioMao() {

	}

	public void pediuAumentoAposta(int posJogador, int valor) {

	}

	public void aceitouAumentoAposta(int posJogador, int valor) {

	}

	public void recusouAumentoAposta(int posJogador) {

	}

	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) {
		return aceitaTruco(s);
	}

}
