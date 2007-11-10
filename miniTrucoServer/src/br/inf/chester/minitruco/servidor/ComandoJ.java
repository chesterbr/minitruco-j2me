package br.inf.chester.minitruco.servidor;

import mt.Carta;

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
 * Joga uma carta na mesa.
 * <p>
 * Par�metro: Carta a ser jogada, no formato Ln (Letra/Naipe).<br>
 * Par�metro 2 (opcional, default F): Se T, joga a carta fechada
 * <p>
 * Se a jogada for v�lida, ser� informada para todos os jogadores (incluindo o
 * que jogou). Se n�o for, nenhuma mensagem � devolvida.
 * 
 * @author Chester
 * @see Carta#toString()
 * 
 */
public class ComandoJ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
		// Verifica se estamos em jogo e se recebeu argumento
		if ((!j.jogando) || (args.length<2))
			return;
		// Encontra a carta solicitada (na m�o do jogador)
		Carta[] cartas = j.getCartas();
		for (int i = 0; i < cartas.length; i++) {
			if (cartas[i] != null && cartas[i].toString().equals(args[1])) {
				// Joga a carta. Se der certo o evento vai notificar a todos.
				cartas[i].setFechada(args.length > 2 && args[2].equals("T"));
				j.getSala().getJogo().jogaCarta(j, cartas[i]);
			}
		}
	}
}
