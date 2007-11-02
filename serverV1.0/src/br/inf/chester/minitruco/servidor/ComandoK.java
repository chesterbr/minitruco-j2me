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

import br.inf.chester.minitruco.cliente.Jogador;

/**
 * Expulsa (Kick) um jogador da sala.
 * <p>
 * Par�metro: posi��o
 * <p>
 * S� pode ser usado pelo gerente
 * 
 * @author Chester
 * 
 */
public class ComandoK extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		Sala s = j.getSala();
		if (s != null && s.getGerente().equals(j) && args.length == 2) {
			try {
				// Recupera o jogador naquela posi��o e faz ele se retirar, via
				// comando "S" (que j� avisa todo mundo)
				int posicao = Integer.parseInt(args[1]);
				Jogador jExpulso = s.getJogador(posicao);
				if (jExpulso instanceof JogadorConectado) {
					(new ComandoS()).executa(null, (JogadorConectado) jExpulso);
					return;
				} else {
					// Caso seja uma expulsao "em falso" (posi��o vazia ou com
					// rob�), apenas atualiza a info da sala
					j.println(s.getInfo());
				}
			} catch (NumberFormatException e) {
				// Posi��o inv�lida
				j.println("X JI");
			}
		}

	}
}
