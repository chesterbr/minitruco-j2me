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

import java.util.Random;
import java.util.Vector;

/**
 * Gerencia as cartas j� distribu�das, garantindo que n�o se sorteie duas vezes
 * a mesma carta.
 * 
 * @author Chester
 * 
 */
public class Baralho {

	private boolean limpo;

	private Random random = new Random();

	private Vector sorteadas = new Vector();

	/**
	 * Cria um novo bararalho de truco
	 * 
	 * @param isLimpo
	 *            true para baralho limpo (sem 4, 5, 6 e 7), false para sujo
	 *            (default)
	 */
	public Baralho(boolean isLimpo) {
		limpo = isLimpo;
	}

	/**
	 * Sorteia uma carta do baralho.
	 * <p>
	 * O m�todo n�o verifica se o baralho foi todo sorteado. Para truco n�o h�
	 * problema, mas outros jogos podem eventualmente retornar um null nesse
	 * caso.
	 * 
	 * @return carta sorteada
	 */
	public Carta sorteiaCarta() {

		Carta c;
		String cartas = limpo ? "A23JQK" : "A234567JQK";
		do {
			char letra = cartas.charAt(sorteiaDeZeroA(cartas.length() - 1));
			int naipe = Carta.NAIPES[sorteiaDeZeroA(3)];
			c = new Carta(letra, naipe);
		} while (sorteadas.contains(c));
		sorteadas.addElement(c);
		return c;
	}
	
	/**
	 * Tira uma carta do baralho, evitando que ela seja sorteada
	 * @param c Carta a retirar
	 */
	public void tiraDoBaralho(Carta c) {
		sorteadas.addElement(c);
	}

	/**
	 * Sortea numeros entre 0 e um valor especificado, inclusive
	 * 
	 * @param limiteSuperior
	 */
	private int sorteiaDeZeroA(int limiteSuperior) {
		// O >>> � pra evitar nos. negativos, cf: http://tinyurl.com/dgjxp
		return (random.nextInt() >>> 1) % (limiteSuperior + 1);
	}

}
