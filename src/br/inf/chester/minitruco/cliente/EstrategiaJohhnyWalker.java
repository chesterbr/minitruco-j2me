package br.inf.chester.minitruco.cliente;

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

import java.util.Random;

/**
 * Estrat�gia burra para jogadores CPU.
 * <p>
 * Ela joga aleatoriamente (da� o nome), e existe para permitr os diferentes
 * n�veis de dificuldade (ter um parceiro b�bado � sempre mais dif�cil,
 * advers�rios b�bados s�o mais f�ceis, e o jogo default � sem �lcool).
 * 
 * @author Chester
 * 
 */
public class EstrategiaJohhnyWalker implements Estrategia {

	Random r = new Random();

	public String getNomeEstrategia() {
		return "Johnny Walker";
	}

	public String getInfoEstrategia() {
		return "feito por Chester, Dez/2005";
	}

	public int joga(SituacaoJogo s) {
		if (s.valorProximaAposta > 0 && r.nextInt() % 3 == 0) {
			return -1; // Se der na louca, pede truco
		}
		return 0; // Devolve sempre a primeira carta da lista
	}

	public boolean aceitaTruco(SituacaoJogo s) {
		return (r.nextInt() % 2 == 0);
	}

	public boolean aceitaMao11(Carta[] cartasParceiro, SituacaoJogo s) {
		return (r.nextInt() % 2 == 0);
	}

	public void inicioPartida() {

	}

	public void inicioMao() {

	}

	public void pediuAumentoAposta(int posJogador, int valor) {

	}

	public void aceitouAumentoAposta(int posJogador, int valor) {

	}

	public void recusouAumentoAposta(int posJogador) {

	}

}
