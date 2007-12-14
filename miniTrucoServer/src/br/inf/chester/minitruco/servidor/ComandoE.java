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
 * Entra numa sala.
 * <p>
 * Parâmetro: número da sala
 * <p>
 * Obs.: Se o jogador não tiver nome, um nome implícito será dado
 * 
 * @author Chester
 * 
 */
public class ComandoE extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {

		try {
			if (j.getSala() != null) {
				j.println("X JE " + j.getSala().getNumSala());
			} else {
				Sala s = Sala.getSala(Integer.parseInt(args[1]));
				if (s == null) {
					j.println("X SI");
				} else if (j.getNome() == null) {
					j.println("X NO");
				} else if (s.adiciona(j)) {
					j.querJogar = false;
					// j.enviaTexto("A "+s.getNumSala()+" "+s.getPosicao(j));
					// s.notificaJogadores("E "+s.getPosicao(j)+"
					// "+j.getNome());
					j.println("E "+s.getNumSala());
					s.notificaJogadores(s.getInfo());
				} else {
					j.println("X CH");
				}
			}
		} catch (NumberFormatException e) {
			j.println("X SI");
		} catch (IndexOutOfBoundsException e) {
			j.println("X SI");
		}

	}

}
