package mt;

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
 * Jogador (no cliente) que participa de um jogo bluetooth (al�m do
 * JogadorHumano).
 * <p>
 * A classe n�o faz nada - � s� para o JogadorHumano n�o se sentir sozinho
 * (i.e., ter a quem referenciar nos eventos remotos).
 * 
 * @author Chester
 * 
 */
public class JogadorDummy extends Jogador {

	public void aceitouAumentoAposta(Jogador j, int valor) {

	}

	public void cartaJogada(Jogador j, Carta c) {

	}

	public void decidiuMao11(Jogador j, boolean aceita) {

	}

	public void informaMao11(Carta[] cartasParceiro) {

	}

	public void inicioMao() {

	}

	public void inicioPartida() {

	}

	public void jogoAbortado(int posicao) {

	}

	public void jogoFechado(int numEquipeVencedora) {

	}

	public void maoFechada(int[] pontosEquipe) {

	}

	public void pediuAumentoAposta(Jogador j, int valor) {

	}

	public void recusouAumentoAposta(Jogador j) {

	}

	public void rodadaFechada(int numRodada, int resultado,
			Jogador jogadorQueTorna) {

	}

	public void vez(Jogador j, boolean podeFechada) {

	}

}
