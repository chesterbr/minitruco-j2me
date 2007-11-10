package br.inf.chester.minitruco.servidor;

/*
 * Copyright � 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 3 da 
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
 * Atribui um nome ao jogador.
 * <p>
 * Par�metro: Nome a atribuir
 * <p>
 * O nome deve ser �nico, e conter apenas letras, n�meros e os caracteres em
 * CARACTERES_PERMITIDOS. Tamb�m n�o pode come�ar com "Robo_". 
 * <p>
 * O servidor guarda o upper/lowercase, mas o nome tem que ser �nico de forma case-insensitive.
 * Ex.: se o "Roberto" entrou, o "roberto" ou o "ROBERTO" n�o pdoem entrar.
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
		// Tenta mudar o nome (o if � pro caso de o cara setar o nome que
		// j� tinha)
		if (!nome.equals(j.getNome())) {
			j.setNome(nome);
		}
		// Se o nome n�o mudou, � porque j� estava em uso
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
