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
 * Obtém informações de uma sala
 * <p>
 * Parâmetro: Número da sala (se omitido, considera a sala atual do jogador)
 * <p>
 * Se o número for válido, gera uma retorno com o número, os nomes dos
 * jogadores, posição do gerente e regras
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
