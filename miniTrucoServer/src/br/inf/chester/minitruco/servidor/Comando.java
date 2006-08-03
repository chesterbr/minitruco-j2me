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
 * Superclasse de todos os comandos que podem ser emitidos por um cliente.
 * <p>
 * Para criar um novo comando, basta criar uma sublcasse de <code>Comando</code>
 * neste mesmo package.
 * <p>
 * Os comandos do cliente s�o representados por uma letra. O nome da classe
 * dever� ter o formato <code>Comando_</code>, onde _ � a letra que ir�
 * acionar o comando.
 * <p>
 * Por exemplo, o comando <code>E</code>, que entra numa sala � representado
 * pela classe <code>ComandoE</code>.
 * 
 * @author Chester
 * 
 */
public abstract class Comando {

	/**
	 * Executa o comando
	 * 
	 * @param args
	 *            argumentos recebidos pelo comando (o 1o. elemento � o pr�prio
	 *            comando)
	 * @param j
	 *            Jogador que solicitou o comando
	 */
	public abstract void executa(String[] args, JogadorConectado j);

}
