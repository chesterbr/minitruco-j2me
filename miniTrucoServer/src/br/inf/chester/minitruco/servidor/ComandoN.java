package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
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

/**
 * Atribui um nome ao jogador.
 * <p>
 * Parâmetro: Nome a atribuir
 * <p>
 * O nome deve ser único, e conter apenas letras, números e os caracteres em
 * CARACTERES_PERMITIDOS. Também não pode começar com "Robo_". 
 * <p>
 * O servidor guarda o upper/lowercase, mas o nome tem que ser único de forma case-insensitive.
 * Ex.: se o "Roberto" entrou, o "roberto" ou o "ROBERTO" não pdoem entrar.
 * 
 * @author Chester
 * 
 */
public class ComandoN extends Comando {

	private static final String CARACTERES_PERMITIDOS = "!@$()-_.";

	@Override
	public void executa(String[] args, JogadorConectado j) {
		if (j.getSala() != null) {
			j.println("X NS");
		}
		String nome;
		if (args == null || args.length < 2 || args[1].equals("")) {
			j.println("X NI");
			return;
		} else {
			// Valida o nome solicitado
			nome = args[1];
			for (int i = 0; i < nome.length(); i++) {
				char c = nome.charAt(i);
				if (!(Character.isLetterOrDigit(c) || CARACTERES_PERMITIDOS
						.indexOf(c) != -1)) {
					j.println("X NI");
					return;
				}
			}
		}
		// Tenta mudar o nome (o if é pro caso de o cara setar o nome que
		// já tinha)
		if (!nome.equals(j.getNome())) {
			j.setNome(nome);
		}
		// Se o nome não mudou, é porque já estava em uso
		if (nome.equals(j.getNome())) {
			j.println("N " + nome);
			Sala s = j.getSala();
			if (s != null)
				s.notificaJogadores(s.getInfo());
		} else {
			j.println("X NE");
		}

	}

}
