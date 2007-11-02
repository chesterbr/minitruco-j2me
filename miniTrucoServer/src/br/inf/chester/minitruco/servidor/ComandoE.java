package br.inf.chester.minitruco.servidor;

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
 * Entra numa sala.
 * <p>
 * Par�metro: n�mero da sala
 * <p>
 * Obs.: Se o jogador n�o tiver nome, um nome impl�cito ser� dado
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
