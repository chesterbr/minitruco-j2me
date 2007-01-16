package mt;

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

/**
 * Fotografia da situa��o atual do jogo.
 * <p>
 * Foi isolada da classe Jogo para poder passar �s <code>Estrategia</code>s a
 * situa��o do jogo de forma a facilitar sua implementa��o e, ao mesmo tempo,
 * impedir que elas trapaceiem (n�o dando acesso ao <code>Jogo</code>.
 * 
 * @author Chester
 * 
 */
public class SituacaoJogo {

	/**
	 * Posi��o do jogador. 1 e 3 s�o parceiros entre si, assim como 2 e 4, e
	 * jogam na ordem num�rica.
	 */
	int posJogador;

	/**
	 * Rodada que estamos jogando (de 1 a 3)
	 */
	int numRodadaAtual;

	/**
	 * Resultados de cada rodada (1 para vit�ria da equipe 1/3, 2 para vit�ria
	 * da equipe 2/4 e 3 para empate)
	 */
	int resultadoRodada[] = new int[3];

	/**
	 * Valor atual da m�o (1, 3, 6, 9 ou 12)
	 */
	int valorMao;

	/**
	 * Valor da m�o caso o jogador pe�a aumento de aposta (se for 0, significa
	 * que n�o pode ser pedido aumento)
	 */
	int valorProximaAposta;

	/**
	 * Jogador que est� pedindo aumento de aposta (pedindo truco, 6, 9 ou 12).
	 * Se for null, ningu�m est� pedindo
	 */
	int posJogadorPedindoAumento;

	/**
	 * Posi��o (1 a 4) do do jogador que abriu a rodada
	 */
	int posJogadorQueAbriuRodada;

	/**
	 * Letra da manilha (quando aplic�vel).
	 * <p>
	 * Esta propriedade deve ser usada APENAS para chamar o m�todo
	 * Jogo.getValorTruco(), pois, no caso de jogo com manilha velha, seu valor
	 * n�o � o de uma carta
	 */
	char manilha;

	/**
	 * Valor que a proprieade manilha assume quando estamos jogando com manilha
	 * velha (n�o-fixa)
	 */
	static char MANILHA_INDETERMINADA = 'X';

	/**
	 * Pontos de cada equipe na partida
	 */
	int[] pontosEquipe = new int[2];

	/**
	 * Para cada rodada (0-2) d� as cartas jogadas pelas 4 posic�es (0-3)
	 */
	Carta[][] cartasJogadas = new Carta[3][4];

	/**
	 * Cartas que ainda est�o na m�o do jogador
	 */
	Carta[] cartasJogador;

	/**
	 * Determina se o baralho inclui as cartas 4, 5, 6 e 7 (true) ou n�o
	 * (false).
	 * <p>
	 */
	boolean baralhoSujo;

	/**
	 * Informa se vale jogar carta fechada
	 */
	boolean podeFechada;

}
