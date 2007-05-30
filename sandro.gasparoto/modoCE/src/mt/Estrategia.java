package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
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

/**
 * Base para as estratégias "plugáveis" que um jogador CPU pode utilizar.
 * <p>
 * Para criar uma nova estratégia, basta criar uma classe que implemente os
 * métodos definidos aqui (que o jogo chamará quando for a hora).
 * <p>
 * Eses métodos recebem uma "fotografia" do jogo (SituacaoJogo) no momento em
 * que a ação deles (jogar, decidir se aceita um truco, etc). é demandada. Se
 * for desejado guardar estado, o tempo de vida do objeto é o mesmo de
 * <code>Jogo</code>, ou seja, o estado (não-<code>static</code>)
 * persistirá ao longo de uma partida, mas não entre partidas.
 * <p>
 * Para testar, adicione sua estratégia ao array OPCOES_ESTRATEGIAS (e o null
 * corespondente no IMAGENS_ESTRATEGIAS) dentro da classe MiniTruco
 * <p>
 * Aviso do Chester: se o seu jogador ficar bom, eu ficaria agradecido se você
 * me mandasse ele no cd@pobox.com. Assim que eu tiver mais de uma Estrategia (o
 * EstrategiaJohnnyWalker não conta :-) ), vou incluí-las (randomicamente ou de
 * forma selecionável) no jogo.
 * <p>
 * Aviso do Chester II: esse projeto terá uma fase futura, que eu apelido de
 * TrucoCode: na linha do RoboCode, estou pensando num esquema para botar as
 * melhores estratégias para competir online. Achou interessante? Vamos
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
	 * Retorna informações de copyright e afins
	 */
	public abstract String getInfoEstrategia();

	/**
	 * Executa uma jogada.
	 * <p>
	 * Observe que, ao pedir aumento, o sistema irá interagir com a outra dupla.
	 * Se a partida seguir, o método será chamado novamente para efetivar a real
	 * jogada.
	 * <p>
	 * A estratégia é responsável por checar se o valor da próxima aposta é
	 * diferente de 0 e só pedir aumento nesta situação.
	 * <p>
	 * 
	 * @param s
	 *            Situação do jogo no momento
	 * @return posição da carta na mão a jogar (em letrasCartasJogador), ou -1
	 *         para pedir truco
	 */
	public abstract int joga(SituacaoJogo s);

	/**
	 * Decide se aceita um pedido de aumento.
	 * <p>
	 * O valor do aumento pode ser determinado verificando o valor atual da
	 * partida (que ainda não foi aumentado)
	 * 
	 * @param s
	 *            Situação do jogo no momento
	 * @return true para aceitar, false para desistir
	 */
	public abstract boolean aceitaTruco(SituacaoJogo s);

	/**
	 * Decide se aceita iniciar uma "mão de 11"
	 * 
	 * @param cartasParceiro
	 *            cartas que o parceiro possui
	 * @return true para iniciar valendo 3 pontos, false para desistir e perder
	 *         1 ponto
	 */
	public abstract boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s);

	/**
	 * Notifica que uma partida está começando.
	 */
	public abstract void inicioPartida();

	/**
	 * Notifica que uma mão está começando
	 */
	public abstract void inicioMao();

	/**
	 * Informa que um jogador pediu aumento de aposta (truco, seis, etc.).
	 * 
	 * @param posJogador
	 *            Jogador que pediu o aumento
	 * @param valor
	 *            Quanto a rodada passará a valar se algum adversário aceitar
	 */
	public abstract void pediuAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador aceitou um pedido de aumento de aposta.
	 * 
	 * @param posJogador
	 *            Jogador que aceitou o aumento
	 * @param valor
	 *            Quanto a rodada está valendo agora
	 */
	public abstract void aceitouAumentoAposta(int posJogador, int valor);

	/**
	 * Informa que o jogador recusou um pedido de aumento de aposta.
	 * <p>
	 * Obs.: isso não impede que o outro jogador da dupla aceite o pedido, é
	 * apenas para notificação visual. Se o segundo jogdor recusar o pedido, a
	 * mensagem de derrota da dupla será enviada logo em seguida.
	 * 
	 * @param posJogador
	 *            Jogador que recusou o pedido.
	 */
	public abstract void recusouAumentoAposta(int posJogador);

}
