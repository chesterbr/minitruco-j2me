package br.inf.chester.minitruco.servidor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		boolean showStat = true;
		String nome;
		String senha;
		// Nome já setado?
		boolean wasGuestBefore = false;
		boolean hasLoggedInBefore = false;
		if (!j.getNome().equals("unnamed")) {
			if (j.getIsGuest()) wasGuestBefore=true;
			hasLoggedInBefore=true;
		}
		// Checa o apelido
		try {
			if (args == null || args[1].length() < 3 || args[1].length() > 50 || args[1].equals("unnamed")) {
				j.println("X NI");
				return;
			}
		} catch (Exception e) {
			j.println("X NI");
			return;
		}
		// Valida o apelido
		nome = args[1];
		for (int i = 0; i < nome.length(); i++) {
			char c = nome.charAt(i);
			if (!(Character.isLetterOrDigit(c) || CARACTERES_PERMITIDOS
					.indexOf(c) != -1)) {
				j.println("X NI");
				return;
			}
		}
		if (nome.toLowerCase().equals(MiniTrucoServer.SERVER.get("STRATEGY").toLowerCase())) {
			j.println("X NI");
			return;
		}
		// Checa a senha (se houver)
		try {
			senha = args[2];
		} catch (Exception e) {
			senha = "";
		}
		if (hasLoggedInBefore) senha="";
		
		// 1o caso: login de usuário convidado (sem senha) ou usuário convidado/registrado solicitando alteração de nick
		if (senha.equals("")) {
			if (MiniTrucoServer.SERVER.get("MYSQL_ENABLED").equals("TRUE")) {
				// checa se tem alguém no banco de dados com o mesmo apelido
				String query = "SELECT * FROM users WHERE username=\"" + args[1] + "\"";
				if (!j.mySQL(query).equals("ERROR")) {
					j.println("X NE");
					return;
				}
			}
			// cadastra apelido
			String nameBefore = j.getNome();
			j.setNome(nome);
			// se o nome não mudou, é porque não foi permitido (em uso por outro convidado)
			if (nome.equals(j.getNome())) {
				// printout
				if (hasLoggedInBefore && wasGuestBefore) {
					j.removeConvidado(nameBefore);
					j.adicionaConvidado(nome);
					showStat = false;
				}
				if (!hasLoggedInBefore) {
					j.adicionaConvidado(nome);
					j.setIsGuest(true);
				}
				if (hasLoggedInBefore && !wasGuestBefore && MiniTrucoServer.SERVER.get("MYSQL_ENABLED").equals("TRUE")) {
					// update database with new nickname
					String query2 = "UPDATE users SET username = \"" + nome + "\" WHERE username=\"" + nameBefore + "\"";
					String reply = j.mySQL(query2);
					if (reply.equals("ERROR")) {
						j.println("X DB");
						return;
					}
					showStat = false;
				}
				j.println("N " + nome);
			} else {
				j.println("X NE");
				return;
			}
		// 2o caso: usuário registrado (com senha)
		} else {
			if (MiniTrucoServer.SERVER.get("MYSQL_ENABLED").equals("TRUE")) {
				// autentica
				String query = "SELECT * FROM users WHERE (username=\"" + nome + "\" OR email=\"" + nome + "\") AND password=\"" + senha + "\"";
				String reply = j.mySQL(query);
				if (reply.equals("ERROR")) {
					j.println("X NP");
					return;
				}
				// recupera nome no caso de usuário ter fornecido o email
				int p = reply.indexOf('|');
				nome = reply.substring(0, p);
				// cadastra apelido
				j.setNome(nome);
				// se o nome não mudou, é porque não foi permitido (em uso por outro convidado)
				if (nome.equals(j.getNome())) {
					// printout
					j.setIsGuest(false);
					j.println("N " + reply);
					// atualiza last_login
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date = sdf.format(cal.getTime());
					query = "UPDATE users SET last_login = \"" + date + "\" WHERE username=\"" + nome + "\"";
					reply = j.mySQL(query);
					// check login time
					j.setLoginTime(System.currentTimeMillis());
				} else {
					j.println("X NE");
					return;
				}
			} else {
				j.println("X DB");
				return;
			}
		}
		// stats:
		// L (means new login) registered_users guest_users available_seats_in_rooms
		if (showStat) { //control: show stats only if this command N was used for login, not for name change
			String res = j.getStats();
			String info="L " + res;
			ServerLogger.stats(info);
		}
	}
}