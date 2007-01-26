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

/**
 * Base para as estrat�gias "plug�veis" que um jogador CPU pode utilizar.
 * <p>
 * Para criar uma nova estrat�gia, basta criar uma classe que implemente os
 * m�todos definidos aqui (que o jogo chamar� quando for a hora).
 * <p>
 * Eses m�todos recebem uma "fotografia" do jogo (SituacaoJogo) no momento em
 * que a a��o deles (jogar, decidir se aceita um truco, etc). � demandada. Se
 * for desejado guardar estado, o tempo de vida do objeto � o mesmo de
 * <code>Jogo</code>, ou seja, o estado (n�o-<code>static</code>)
 * persistir� ao longo de uma partida, mas n�o entre partidas.
 * <p>
 * Para testar, adicione sua estrat�gia ao array OPCOES_ESTRATEGIAS (e o null
 * corespondente no IMAGENS_ESTRATEGIAS) dentro da classe MiniTruco
 * <p>
 * Aviso do Chester: se o seu jogador ficar bom, eu ficaria agradecido se voc�
 * me mandasse ele no cd@pobox.com. Assim que eu tiver mais de uma Estrategia (o
 * EstrategiaJohnnyWalker n�o conta :-) ), vou inclu�-las (randomicamente ou de
 * forma selecion�vel) no jogo.
 * <p>
 * Aviso do Chester II: esse projeto ter� uma fase futura, que eu apelido de
 * TrucoCode: na linha do RoboCode, estou pensando num esquema para botar as
 * melhores estrat�gias para competir online. Achou interessante? Vamos
 * conversar...
 * 
 * @author Chester
 * @see MiniTruco#OPCOES_ESTRATEGIAS
 */
public interface Estrategia {

	/**
	 * Retorna o nome "de tela" da Estrategia
	 */
	public abstract String getNomeEstrategia();

	/**
	 * Retorna informa��es de copyright e afins
	 */
	public abstract String getInfoEstrategia();

	/**
	 * Executa uma jogada.
	 * <p>
	 * Observe que, ao pedir aumento, o sistema ir� interagir com a outra dupla.
	 * Se a partida seguir, o m�todo ser� chamado novamente para efetivar a real
	 * jogada.
	 * <p>
	 * A estrat�gia � respons�vel por checar se o valor da pr�xima aposta �
	 * diferente de 0 e s� pedir aumento nesta situa��o.
	 * <p>
	 * 
	 * @param s
	 *            Situa��o do jogo no momento
	 * @return posi��o da carta na m�o a jogar (em letrasCartasJogador), ou -1
	 *         para pedir truco
	 */
	public abstract int joga(SituacaoJogo s);

	/**
	 * Decide se aceita um pedido de aumento.
	 * <p>
	 * O valor do aumento pode ser determinado verificando o valor atual da
	 * partida (que ainda n�o foi aumentado)
	 * 
	 * @param s
	 *            Situa��o do jogo no momento
	 * @return true para aceitar, false para desistir
	 */
	public abstract boolean aceitaTruco(SituacaoJogo s);

	/**
	 * Decide se aceita iniciar uma "m�o de 11"
	 * 
	 * @param cartasParceiro
	 *            cartas que o parceiro possui
	 * @return true para iniciar valendo 3 pontos, false para desistir e perder
	 *         1 ponto
	 */
	public abstract boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s);

	/**
	 * Notifica que uma partida est� come�ando.
	 */
	public abstract void inicioPartida();

	/**
	 * Notifica que uma m�o est� come�ando
	 */
	public abstract void inicioMao();

	/**
	 * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
	 * 
	 * @param posJogador
	 *            Jogador que pediu o aumento
	 * @param valor
	 *            Quanto a rodada passar� a valar se algum advers�rio aceitar
	 */
	public abstract void pediuAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador aceitou um pedido de aumento de aposta.
	 * 
	 * @param posJogador
	 *            Jogador que aceitou o aumento
	 * @param valor
	 *            Quanto a rodada est� valendo agora
	 */
	public abstract void aceitouAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador recusou um pedido de aumento de aposta.
	 * <p>
	 * Obs.: isso n�o impede que o outro jogador da dupla aceite o pedido, �
	 * apenas para notifica��o visual. Se o segundo jogdor recusar o pedido, a
	 * mensagem de derrota da dupla ser� enviada logo em seguida.
	 * 
	 * @param posJogador
	 *            Jogador que recusou o pedido.
	 */
	public abstract void recusouAumentoAposta(int posJogador);

}
