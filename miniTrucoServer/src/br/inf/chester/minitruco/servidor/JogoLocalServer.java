package br.inf.chester.minitruco.servidor;

import mt.JogoLocal;

/**
 * Versão do jogo local que acontece no servidor
 * 
 * Esta classe existe porque o serivdor precisa de pausas extras (que deixam o
 * cliente um pouco mais lento do que o desejado em jogos locais). Talvez fosse
 * mais simpático ter uma classe-base e duas filhas, mas no J2ME temos que
 * "economizar" classes, daí essa abordagem.
 * 
 * @author chester
 * 
 */
public class JogoLocalServer extends JogoLocal {

	public JogoLocalServer(boolean baralhoLimpo, boolean manilhaVelha) {
		super(baralhoLimpo, manilhaVelha);
	}

	@Override
	/**
	 * Faz efetivamente a pausa (ao invés de ignorá-la, como o JogoLocal faz) 
	 */
	protected void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
