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

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Cuida das mensagens e outros aspectos de internacionalização.
 * <p>
 * Enquanto nenhum idioma for carregado, a classe irá retornar textos no formato
 * !chave! (o que também ocorre caso chaves inexistentes sejam solicitadas).
 * <p>
 * Cada idioma consiste em um arquivo mensagens.IDIOMA.properties.
 * <p>
 * O nome foi o sugerido pelo Source... Externalize Strings do Eclipse.
 * 
 * @author chester
 * 
 */
public class Messages {

	/**
	 * Guarda as mensagens do idioma carregado.
	 */
	private static Hashtable localStrings = new Hashtable();

	private Messages() {
	}

	/**
	 * Carrega as strings de um determinado idioma
	 * 
	 * @param idioma
	 *            Código do idioma
	 * @throws IOException
	 */
	public static void carregaIdioma(String idioma) throws IOException {
		localStrings.clear();
		InputStream inputStreamTxt = null;
		inputStreamTxt = localStrings.getClass().getResourceAsStream(
				"/mensagens." + idioma.substring(0, 3) + ".properties");//$NON-NLS-1$$NON-NLS-2$
		if (inputStreamTxt == null) {
			throw new IOException("Internal error: language not found"); //$NON-NLS-1$
		}
		StringBuffer buf = new StringBuffer();
		int c;
		int posIgual = 0;
		while ((c = inputStreamTxt.read()) != -1) {
			char ch = (char) c;
			if (ch == '\n' || ch == '\r') {
				if (buf.length() != 0) {
					String sBuf = buf.toString();
					localStrings.put(sBuf.substring(0, posIgual), sBuf
							.substring(posIgual));
					buf.setLength(0);
				}
			} else if (ch == '=') {
				posIgual = buf.length();
			} else
				buf.append(ch);
		}
		inputStreamTxt.close();
	}

	/**
	 * Recupera o texto (do idioma atual atual) correspondente a uma chave
	 * 
	 * @param key
	 *            chave a procurar
	 * @return texto, ou "!chave!" se ela não for encontrada.
	 */
	public static String getString(String key) {
		String texto = (String) localStrings.get(key);
		if (texto != null)
			return texto;
		else
			return '!' + key + '!';
	}

}
