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
 * Obt�m informa��es de uma sala
 * <p>
 * Par�metro: N�mero da sala (se omitido, considera a sala atual do jogador)
 * <p>
 * Se o n�mero for v�lido, gera uma retorno com o n�mero, os nomes dos
 * jogadores, posi��o do gerente e regras
 * 
 * @author Chester
 * 
 */
public class ComandoI extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		try {
			Sala s;
			if (args.length<2) {
				s = j.getSala();
				if (s==null) {
					j.println("X FS");
					return;
				}
			} else {
				s = Sala.getSala(Integer.parseInt(args[1]));				
			}
			if (s != null) {
				j.println(s.getInfo());
			} else {
				j.println("X SI");
			}
		} catch (NumberFormatException e) {
			j.println("X SI");
		} catch (IndexOutOfBoundsException e) {
			j.println("X SI");
		}

	}
}
