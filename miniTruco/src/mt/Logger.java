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
 * Faz o log do jogo, para fins de debug.
 * <p>
 * Foi separada da classe original, para que o servidor n�o tivesse depend�ncias
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
	 * M�todo usado para debug (permite acompanhar o jogo no console)
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
