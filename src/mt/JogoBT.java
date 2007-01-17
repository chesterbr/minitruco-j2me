package mt;

/**
 * Representa, no cliente, o <code>Jogo</code> que está executando no
 * servidor.
 * <p>
 * De maneira análoga à <code>JogadorBT</code>, suas responsabildiades são:
 * <p>
 * 1) Encaminhar para o celular do servidor os comandos dados pelo JogadorCPU
 * local
 * <p>
 * 2) Receber deste mesmo celular os eventos de jogo, e repassar para o
 * JogadorCPU local.
 * 
 * @author chester
 * 
 */
public class JogoBT extends Jogo {

	public void atualizaSituacao(SituacaoJogo s, Jogador j) {
		// TODO Auto-generated method stub

	}

	public void aumentaAposta(Jogador j) {
		// TODO Auto-generated method stub

	}

	public void decideMao11(Jogador j, boolean aceita) {
		// TODO Auto-generated method stub

	}

	public Carta getCartaDaMesa() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBaralhoLimpo() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isManilhaVelha() {
		// TODO Auto-generated method stub
		return false;
	}

	public void jogaCarta(Jogador j, Carta c) {
		// TODO Auto-generated method stub

	}

	public void respondeAumento(Jogador j, boolean aceitou) {
		// TODO Auto-generated method stub

	}

	public void run() {
		// TODO Auto-generated method stub

	}

}
