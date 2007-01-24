package mt;

/*
 * Copyright © 2006 Carlos Duarte do Nascimento (Chester)
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
 * Faz o log do jogo, para fins de debug.
 * <p>
 * Foi separada da classe original, para que o servidor não tivesse dependências
 * de J2ME
 * @author chester
 *
 */
public class Logger {

	/**
	 * Log rotativo (para exibir na tela do celular)
	 */
	public static String[] log = null;

	/**
	 * Método usado para debug (permite acompanhar o jogo no console)
	 * 
	 * @param string
	 *            Mensagem informativa
	 */
	public static synchronized void debug(String string) {
		// Envia para o console
		System.out.println(string);

		// Guarda no log rotativo, se estiver habilitado
		if (log != null) {
			for (int i = 0; i < log.length - 1; i++) {
				log[i] = log[i + 1];
			}
			log[log.length - 1] = string;
		}
	}

}
