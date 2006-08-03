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
 * Troca as regras da sala atual.
 * <p>
 * Recebe como par�metro uma string de exatamente 2 caracteres T ou F,
 * indicando, na ordem, baralho limpo e manilha velha.
 * <p>
 * Apenas o gerente pode dar esse comando.
 * 
 * @author Chester
 * 
 */
public class ComandoR extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		Sala s = j.getSala();
		if (s.getGerente().equals(j) && args.length == 2
				&& args[1].length() == 2) {
			if (args[1].equals("TT")) {
				// N�o d� pra jogar com baralho limpo e manilha velha
				j.println("X TT");
			} else {
				// Seta as novas regras e avisa a galera
				s.baralhoLimpo = args[1].charAt(0) == 'T';
				s.manilhaVelha = args[1].charAt(1) == 'T';
				s.notificaJogadores(s.getInfo());
			}
		}

	}

}
