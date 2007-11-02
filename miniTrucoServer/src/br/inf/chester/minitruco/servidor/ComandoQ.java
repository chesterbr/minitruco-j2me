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
 * Informa ao servidor que o jogador deseja iniciar a partida na sala em que est�.
 * <p>
 * Quando ao menos dois jogadores emitem este comando, a mesma se inicia
 * @author Chester
 *
 */
public class ComandoQ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		Sala s = j.getSala();
		if (s!=null) {
			j.querJogar = true;
			s.notificaJogadores(s.getInfo());
			s.verificaMesaCompleta();
		} else {
			j.println("X FS");
		}
	}

}
