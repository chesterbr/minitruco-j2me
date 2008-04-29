package mt;
/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
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

import java.util.Random;
import java.util.Vector;

/**
 * Gerencia as cartas já distribuídas, garantindo que não se sorteie duas vezes
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
	 * O método não verifica se o baralho foi todo sorteado. Para truco não há
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
		// O >>> é pra evitar nos. negativos, cf: http://tinyurl.com/dgjxp
		return (random.nextInt() >>> 1) % (limiteSuperior + 1);
	}

}
